// qris-proxy — proxy QRIS (buatqris.site) untuk aplikasi Shiro.
//
// Tujuan: TOKEN TIDAK PERNAH keluar dari server ini.
// Aplikasi hanya meminta "buat QR" dan "cek status"; server yang
// menyimpan account_id / secret_token dan meneruskan ke api.buatqris.site.
//
// Endpoint (di belakang nginx  /qris/*):
//   GET  /health     -> cek layanan (tanpa membocorkan kredensial)
//   POST /create     { amount, description? }  -> QRIS dinamis
//   POST /status     { transaction_id }        -> status pembayaran
//   POST /webhook    -> callback dari buatqris.site (verifikasi HMAC)
//
// Alur status: aplikasi boleh polling sesering yang dia mau ke endpoint ini;
// server yang menahan panggilan ke upstream (maks. 1x / 20 detik per
// transaksi, sesuai limit buatqris) dan memakai webhook untuk memperbarui
// status begitu pembayaran masuk.
//
// Batas: rate-limit per IP + validasi nominal, supaya token tidak bisa
// dipakai orang lain untuk membuat QR sembarangan.
"use strict";

const express = require("express");
const cors = require("cors");
const crypto = require("crypto");

const PORT = Number(process.env.PORT || 3460);
const BQ_API = process.env.BQ_API_URL || "https://api.buatqris.site";
const ACCOUNT_ID = process.env.BQ_ACCOUNT_ID || "";
const SECRET_TOKEN = process.env.BQ_SECRET_TOKEN || "";
const QRIS_METHOD = process.env.BQ_QRIS_METHOD || "qris_two";
const FEE_BY = process.env.BQ_FEE_BY || "user";
const WEBHOOK_SECRET = process.env.BQ_WEBHOOK_SECRET || "";
const CALLBACK_URL = process.env.BQ_CALLBACK_URL || ""; // opsional
// limit buatqris: "Terlalu sering cek status. Coba lagi dalam 16 detik."
const MIN_STATUS_INTERVAL_MS = Number(process.env.BQ_STATUS_INTERVAL_MS || 20_000);

const MIN_AMOUNT = 1_000;
const MAX_AMOUNT = 10_000_000;
const DESC_MAX = 60;
const TERMINAL = new Set(["success", "expired", "failed"]);

// cache status per transaksi (di-update webhook + polling upstream)
const txState = new Map(); // tx -> { status, amount, ..., lastCheck, source }
const TX_CACHE_MAX = 5000;

function rememberTx(tx, patch) {
  const prev = txState.get(tx) || {};
  const next = { ...prev, ...patch };
  txState.set(tx, next); // Map: insert order dipakai untuk evicting LRU-ish
  while (txState.size > TX_CACHE_MAX) {
    const oldest = txState.keys().next().value;
    txState.delete(oldest);
  }
  return next;
}


// ---------------------------------------------------------------- rate limit
const hits = new Map(); // key -> [timestamp]

function allow(key, limit, windowMs) {
  const now = Date.now();
  const list = (hits.get(key) || []).filter((t) => now - t < windowMs);
  if (list.length >= limit) {
    hits.set(key, list);
    return false;
  }
  list.push(now);
  hits.set(key, list);
  return true;
}

// bersihkan peta tiap 10 menit supaya tidak bocor memori
setInterval(() => {
  const now = Date.now();
  for (const [k, v] of hits) {
    if (v.length === 0 || now - v[v.length - 1] > 60 * 60 * 1000) hits.delete(k);
  }
}, 10 * 60 * 1000).unref();

function clientIp(req) {
  const cf = req.headers["cf-connecting-ip"];
  if (cf) return String(cf).split(",")[0].trim();
  const fwd = req.headers["x-forwarded-for"];
  if (fwd) return String(fwd).split(",")[0].trim();
  return req.socket.remoteAddress || "unknown";
}

// ------------------------------------------------------------ upstream call
async function callBq(fields) {
  if (!ACCOUNT_ID || !SECRET_TOKEN) {
    const err = new Error("QRIS is not configured on this server");
    err.status = 503;
    throw err;
  }
  const body = new URLSearchParams({
    account_id: ACCOUNT_ID,
    secret_token: SECRET_TOKEN,
    ...fields,
  });

  const res = await fetch(BQ_API, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
    signal: AbortSignal.timeout(20_000),
  });

  const text = await res.text();
  let json;
  try {
    json = JSON.parse(text);
  } catch {
    const err = new Error("Upstream returned an invalid response");
    err.status = 502;
    throw err;
  }
  return { upstreamStatus: res.status, json };
}

// ambil hanya field yang dibutuhkan app — jangan teruskan apa pun yang
// bersifat rahasia (tidak ada token di sini, tapi tetap whitelist saja)
function pickCreate(d) {
  const out = {};
  for (const k of [
    "transaction_id",
    "qr_url",
    "qris_image",
    "payment_url",
    "amount",
    "total_amount",
    "admin_fee",
    "credit_amount",
    "status",
    "expired_at",
    "description",
    "qris_method",
  ]) {
    if (d[k] !== undefined) out[k] = d[k];
  }
  return out;
}

function pickStatus(d) {
  const out = {};
  for (const k of [
    "transaction_id",
    "status",
    "amount",
    "total_amount",
    "admin_fee",
    "credit_amount",
    "updated_at",
  ]) {
    if (d[k] !== undefined) out[k] = d[k];
  }
  return out;
}

// ---------------------------------------------------------------- app
const app = express();
app.disable("x-powered-by");
app.use(cors()); // app dipanggil dari WebView/Tauri/web -> izinkan semua origin
// verify -> simpan body mentah untuk verifikasi tanda tangan webhook
app.use(
  express.json({
    limit: "64kb",
    verify: (req, _res, buf) => {
      req.rawBody = Buffer.from(buf);
    },
  }),
);
app.use(express.urlencoded({ extended: false, limit: "16kb" }));

// log ringkas tanpa rahasia
app.use((req, res, next) => {
  res.on("finish", () => {
    console.log(
      `${new Date().toISOString()} ${req.method} ${req.originalUrl} -> ${res.statusCode} ip=${clientIp(req)}`,
    );
  });
  next();
});

app.get("/health", (_req, res) => {
  res.json({
    ok: true,
    service: "shiro-qris-proxy",
    configured: Boolean(ACCOUNT_ID && SECRET_TOKEN),
  });
});

app.post("/create", async (req, res) => {
  const ip = clientIp(req);
  if (!allow(`create:${ip}`, 15, 10 * 60 * 1000)) {
    return res.status(429).json({ success: false, message: "Too many requests" });
  }

  const amount = Number(req.body && req.body.amount);
  if (!Number.isInteger(amount) || amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
    return res.status(400).json({
      success: false,
      message: `Amount must be an integer between ${MIN_AMOUNT} and ${MAX_AMOUNT}`,
    });
  }

  const rawDesc = req.body && req.body.description;
  const description = String(rawDesc == null ? "Donasi Shiro" : rawDesc)
    // buang karakter kontrol supaya aman dikirim ke upstream
    // eslint-disable-next-line no-control-regex
    .replace(/[\u0000-\u001f\u007f]/g, " ")
    .trim()
    .slice(0, DESC_MAX);

  try {
    const { json } = await callBq({
      action: "api_create_qris",
      amount: String(amount),
      description: description || "Donasi Shiro",
      qris_method: QRIS_METHOD,
      fee_by: FEE_BY,
      // webhook -> status diperbarui oleh buatqris, bukan oleh polling
      ...(CALLBACK_URL ? { callback_url: CALLBACK_URL } : {}),
    });

    if (json && json.success && json.data && json.data.transaction_id) {
      rememberTx(json.data.transaction_id, {
        ...pickStatus(json.data),
        lastCheck: Date.now(),
        source: "create",
      });
      return res.json({ success: true, data: pickCreate(json.data) });
    }
    return res.status(502).json({
      success: false,
      message: (json && (json.message || json.error)) || "Failed to create QRIS",
    });
  } catch (e) {
    const status = e.status || 502;
    if (status !== 503) console.error("create failed:", e.message);
    return res.status(status).json({ success: false, message: e.message });
  }
});

app.post("/status", async (req, res) => {
  const ip = clientIp(req);
  if (!allow(`status:${ip}`, 300, 10 * 60 * 1000)) {
    return res.status(429).json({ success: false, message: "Too many requests" });
  }

  const tx = String((req.body && req.body.transaction_id) || "").trim();
  if (!/^[A-Za-z0-9_-]{4,64}$/.test(tx)) {
    return res.status(400).json({ success: false, message: "Invalid transaction id" });
  }

  // jawab dari cache kalau: status sudah final ATAU masih di jeda throttle
  // (limit buatqris ± 1 cek / 15-20 detik per transaksi)
  const now = Date.now();
  const cached = txState.get(tx);
  if (
    cached &&
    (TERMINAL.has(cached.status) ||
      now - (cached.lastCheck || 0) < MIN_STATUS_INTERVAL_MS)
  ) {
    return res.json({
      success: true,
      data: pickStatus(cached),
      cached: true,
      source: cached.source || "cache",
    });
  }

  try {
    const { json } = await callBq({ action: "api_check_status", transaction_id: tx });
    if (json && json.success && json.data) {
      rememberTx(tx, { ...pickStatus(json.data), lastCheck: Date.now(), source: "upstream" });
      return res.json({ success: true, data: pickStatus(json.data) });
    }
    // upstream menolak karena rate limit -> sajikan cache terakhir, coba lagi nanti
    if (json && json.error === "rate_limited" && cached) {
      return res.json({
        success: true,
        data: pickStatus(cached),
        cached: true,
        source: cached.source || "cache",
      });
    }
    return res.status(502).json({
      success: false,
      message: (json && (json.message || json.error)) || "Failed to check status",
    });
  } catch (e) {
    const status = e.status || 502;
    if (status !== 503) console.error("status failed:", e.message);
    return res.status(status).json({ success: false, message: e.message });
  }
});

// ---------------------------------------------------------------- webhook
function firstHeader(req, pattern) {
  for (const [k, v] of Object.entries(req.headers)) {
    if (pattern.test(k)) return String(Array.isArray(v) ? v[0] : v);
  }
  return "";
}

function hmacHex(value) {
  return crypto.createHmac("sha256", WEBHOOK_SECRET).update(value).digest("hex");
}

function safeEqualHex(a, b) {
  const ab = Buffer.from(String(a), "hex");
  const bb = Buffer.from(String(b), "hex");
  return ab.length === bb.length && ab.length > 0 && crypto.timingSafeEqual(ab, bb);
}

// Tanda tangan bisa berupa HMAC(body) atau HMAC(delivery + "." + body);
// keduanya diterima supaya skema tanda tangan upstream tetap cocok.
function webhookAuthorized(req) {
  if (!WEBHOOK_SECRET) return true; // belum diset -> jangan tolak mentah-mentah
  const given = firstHeader(req, /signature/i).replace(/^sha256=/i, "").trim();
  if (!given) return false;
  const delivery = firstHeader(req, /delivery/i);
  const raw = (req.rawBody && req.rawBody.toString("utf8")) || "";
  return (
    safeEqualHex(given, hmacHex(raw)) ||
    (delivery ? safeEqualHex(given, hmacHex(`${delivery}.${raw}`)) : false) ||
    (delivery ? safeEqualHex(given, hmacHex(delivery)) : false)
  );
}

app.post("/webhook", (req, res) => {
  if (!webhookAuthorized(req)) {
    console.log(`webhook rejected: bad signature ip=${clientIp(req)}`);
    return res.status(401).json({ success: false, message: "Invalid signature" });
  }

  const body = req.body || {};
  const event = firstHeader(req, /event/i);
  const delivery = firstHeader(req, /delivery/i);
  const tx = String(body.transaction_id || delivery || "").trim();
  const status = String(
    body.status || (String(event).includes("success") ? "success" : ""),
  ).trim();

  if (tx && /^[A-Za-z0-9_-]{4,64}$/.test(tx) && status) {
    rememberTx(tx, {
      status,
      amount: body.amount,
      total_amount: body.total_amount,
      admin_fee: body.admin_fee,
      credit_amount: body.credit_amount,
      updated_at: body.paid_at || new Date().toISOString(),
      lastCheck: Date.now(),
      source: "webhook",
    });
    console.log(`webhook ${event || "event"} ${tx} -> ${status}`);
  } else {
    console.log(`webhook ignored (event=${event || "-"} status=${status || "-"})`);
  }

  // balas 200 agar upstream menandai terkirim
  res.json({ success: true, received: true });
});

app.use((_req, res) => res.status(404).json({ success: false, message: "Not found" }));

app.listen(PORT, "127.0.0.1", () => {
  console.log(`qris-proxy listening on 127.0.0.1:${PORT} (configured=${Boolean(
    ACCOUNT_ID && SECRET_TOKEN,
  )})`);
});
