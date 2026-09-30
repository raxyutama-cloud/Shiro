// support.js — menu donasi (QRIS dinamis lewat proxy server Shiro)
//
// Aplikasi TIDAK menyimpan rahasia apa pun. Konfigurasi hanya berisi URL
// proxy (js/config/qris-config.js, ikut di-commit) — account_id dan
// secret_token tinggal di server, jadi token tidak pernah ikut ke APK.
// Jika konfigurasi tidak ada, modal jatuh ke QR statis.
import { t } from "../i18n/index.js";
import { CapacitorHttp, showToast } from "../utils/index.js";

const POLL_MS = 5000;
const MIN_AMOUNT = 1000;

const overlay = document.getElementById("donationOverlay");
const supportBtn = document.getElementById("supportBtn");
const closeBtn = document.getElementById("closeDonation");
const amountInput = document.getElementById("donationAmount");
const descInput = document.getElementById("donationDesc");
const generateBtn = document.getElementById("generateQrisBtn");
const formEl = document.getElementById("donationForm");
const statusEl = document.getElementById("donationStatus");
const qrBox = document.getElementById("qrBox");
const qrImage = document.getElementById("qrImage");
const qrMeta = document.getElementById("qrMeta");
const paymentLink = document.getElementById("paymentLink");

const tauriInvoke =
  window.__TAURI__?.core?.invoke ||
  window.__TAURI_INTERNALS__?.invoke ||
  window.__TAURI__?.invoke;

let pollTimer = null;
let currentTx = null;

function getConfig() {
  const c = window.__SHIRO_QRIS__ || null;
  if (!c || !c.apiUrl) return null;
  if (String(c.apiUrl).startsWith("YOUR_")) return null;
  return c;
}

/** POST JSON ke proxy QRIS, lintas platform (WebView / Tauri / web). */
async function postJson(path, payload) {
  const cfg = getConfig();
  const base = String(cfg.apiUrl).replace(/\/+$/, "");
  const url = `${base}${path}`;
  const body = JSON.stringify(payload);
  const headers = { "Content-Type": "application/json" };

  let raw;
  if (CapacitorHttp) {
    const res = await CapacitorHttp.post({ url, headers, data: body });
    raw = res?.data ?? res?.body ?? res;
  } else if (tauriInvoke && !window.Capacitor?.isNativePlatform?.()) {
    const res = await tauriInvoke("tauri_http_request", {
      url,
      method: "POST",
      headers,
      body,
    });
    raw = res?.data ?? res?.body ?? res;
  } else {
    const res = await fetch(url, { method: "POST", headers, body });
    raw = await res.text();
  }

  if (typeof raw === "string") return JSON.parse(raw);
  if (raw && typeof raw.data === "string") {
    try {
      return JSON.parse(raw.data);
    } catch (_) {
      /* already an object */
    }
  }
  return raw;
}

function setStatus(kind, text) {
  if (!statusEl) return;
  statusEl.className = "donation-status " + (kind || "");
  statusEl.textContent = text || "";
}

function clearStatus() {
  if (!statusEl) return;
  statusEl.className = "donation-status hidden";
  statusEl.textContent = "";
}

function rupiah(n) {
  const v = Number(n) || 0;
  return "Rp" + v.toLocaleString("id-ID");
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
  currentTx = null;
}

function renderResult(data) {
  currentTx = data.transaction_id;

  const src = data.qr_url || data.qris_image || "";
  qrImage.onerror = () => {
    qrImage.onerror = null;
    if (data.qris_image) qrImage.src = data.qris_image;
  };
  qrImage.src = src;

  qrMeta.innerHTML = `
    <div class="donation-row"><span>${t("donate-amount-label")}</span><b>${rupiah(data.amount)}</b></div>
    <div class="donation-row"><span>${t("donate-total")}</span><b>${rupiah(data.total_amount)}</b></div>
    ${
      data.admin_fee
        ? `<div class="donation-row"><span>${t("donate-fee")}</span><b>${rupiah(data.admin_fee)}</b></div>`
        : ""
    }
    <div class="donation-row"><span>${t("donate-tx")}</span><b>${data.transaction_id}</b></div>
  `;

  if (data.payment_url) {
    paymentLink.href = data.payment_url;
    paymentLink.classList.remove("hidden");
  }

  qrBox.classList.remove("hidden");
  setStatus("pending", t("donate-status-pending"));
  startPolling(data.transaction_id);
}

function startPolling(txId) {
  stopPolling();
  currentTx = txId;
  const tick = async () => {
    if (!currentTx || overlay?.classList.contains("hidden")) return;
    try {
      const res = await postJson("/status", {
        transaction_id: currentTx,
      });
      const st = res?.data?.status;
      if (!st) return;
      if (st === "success") {
        setStatus("success", t("donate-status-success"));
        showToast(t("donate-status-success"));
        stopPolling();
      } else if (st === "expired") {
        setStatus("expired", t("donate-status-expired"));
        stopPolling();
      } else if (st === "failed") {
        setStatus("failed", t("donate-status-failed"));
        stopPolling();
      } else {
        setStatus("pending", t("donate-status-pending"));
      }
    } catch (_) {
      /* diamkan error jaringan, coba lagi di tick berikutnya */
    }
  };
  pollTimer = setInterval(tick, POLL_MS);
}

async function generate() {
  if (!getConfig()) {
    setStatus("failed", t("donate-not-configured"));
    return;
  }
  const amount = parseInt(String(amountInput?.value || "").trim(), 10);
  if (!Number.isFinite(amount) || amount < MIN_AMOUNT) {
    setStatus("failed", t("donate-invalid-amount"));
    amountInput?.focus();
    return;
  }

  const label = generateBtn?.querySelector("span") || generateBtn;
  const original = label ? label.textContent : "";
  if (generateBtn) generateBtn.disabled = true;
  if (label) label.textContent = t("btn-processing") || "…";
  setStatus("creating", t("donate-creating"));
  qrBox?.classList.add("hidden");
  stopPolling();

  try {
    const res = await postJson("/create", {
      amount,
      description: (descInput?.value || "Donasi Shiro").slice(0, 60),
    });
    if (res?.success && res?.data?.transaction_id) {
      clearStatus();
      renderResult(res.data);
    } else {
      setStatus(
        "failed",
        res?.message || res?.error || t("donate-error"),
      );
    }
  } catch (e) {
    console.error("QRIS create failed:", e);
    setStatus("failed", t("donate-error"));
  } finally {
    if (generateBtn) generateBtn.disabled = false;
    if (label) label.textContent = original;
  }
}

function openDonation() {
  if (!overlay) return;
  stopPolling();
  qrBox?.classList.add("hidden");
  if (getConfig()) {
    clearStatus();
    if (formEl) formEl.classList.remove("hidden");
    if (!amountInput?.value) amountInput.value = "10000";
    setTimeout(() => amountInput?.focus(), 120);
  } else {
    if (formEl) formEl.classList.add("hidden");
    setStatus("failed", t("donate-not-configured"));
  }
  overlay.classList.remove("hidden");
}

function closeDonation() {
  stopPolling();
  overlay?.classList.add("hidden");
}

supportBtn?.addEventListener("click", openDonation);
closeBtn?.addEventListener("click", closeDonation);
overlay?.addEventListener("click", (e) => {
  if (e.target === overlay) closeDonation();
});
generateBtn?.addEventListener("click", generate);
amountInput?.addEventListener("keydown", (e) => {
  if (e.key === "Enter") generate();
});

// Jangan biarkan polling jalan saat pindah halaman/splash lock
window.addEventListener("pagehide", stopPolling);
