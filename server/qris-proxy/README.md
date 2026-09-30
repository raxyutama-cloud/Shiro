# shiro-qris-proxy

Proxy QRIS untuk aplikasi **Shiro**. Tugasnya sederhana: menyimpan kredensial
`buatqris.site` di server dan meneruskan permintaan aplikasi, sehingga **token
tidak pernah masuk ke dalam APK/IPA/binaries desktop**.

```
Aplikasi (Android / iOS / Desktop / Web)
  ├─ POST <base>/create   { amount, description }
  ├─ POST <base>/status   { transaction_id }
  └─ (webhook) buatqris.site → <base>/webhook
        └─ qris-proxy  127.0.0.1:3460
             └─ POST https://api.buatqris.site   (account_id + secret_token dari .env)
```

## Endpoint

| Method | Path | Body | Keterangan |
| --- | --- | --- | --- |
| GET  | `/health`  | – | `{ ok, configured }` tanpa membocorkan kredensial |
| POST | `/create`  | `{ amount, description? }` | membuat QRIS; `amount` Rp1.000–Rp10.000.000 |
| POST | `/status`  | `{ transaction_id }` | status pembayaran (dari cache/throttle) |
| POST | `/webhook` | payload buatqris.site | menerima notifikasi pembayaran |

## Keamanan & pembatasan

- Kredensial hanya dibaca dari `.env` lewat `EnvironmentFile` systemd — tidak
  pernah dikirim ke aplikasi, tidak pernah di-commit.
- Rate limit per IP: 15 pembuatan QR dan 300 cek status per 10 menit.
- `transaction_id` divalidasi formatnya, nominal divalidasi rentangnya, catatan
  dibersihkan dari karakter kontrol, timeout upstream 20 detik.
- Respons disaring (whitelist field) sehingga tidak ada data sensitif yang
  ikut terkirim.
- Webhook diverifikasi dengan HMAC-SHA256 (`X-BuatQris-Signature`) memakai
  `crypto.timingSafeEqual` — payload palsu ditolak dengan 401.
- `/status` menyimpan cache per transaksi dan hanya memanggil upstream
  maksimal 1x / 20 detik per transaksi, karena `api.buatqris.site`
  membatasi pengecekan status (± 1x / 15–20 detik). Pembaruan sebenarnya
  datang lewat webhook.

## Pemasangan

```bash
# 1. salin direktori ini, lalu
npm install --omit=dev

# 2. buat .env (chmod 600, jangan pernah di-commit)
cat > .env <<'EOF'
PORT=3460
BQ_API_URL=https://api.buatqris.site
BQ_ACCOUNT_ID=user_...
BQ_SECRET_TOKEN=sk_live_...
BQ_WEBHOOK_SECRET=whsec_...
BQ_CALLBACK_URL=https://domain-kamu/qris/webhook
BQ_QRIS_METHOD=qris_two
BQ_FEE_BY=user
BQ_STATUS_INTERVAL_MS=20000
EOF

# 3. systemd
cp qris-proxy.service.example /etc/systemd/system/qris-proxy.service
#    sesuaikan User, WorkingDirectory, EnvironmentFile
systemctl daemon-reload && systemctl enable --now qris-proxy

# 4. nginx (balasan prefix /qris/)
location ^~ /qris/ {
    proxy_pass http://127.0.0.1:3460/;   # trailing slash = prefix dibuang
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout 30s;
    proxy_buffering off;
    add_header Cache-Control "no-cache";
}
nginx -t && systemctl reload nginx
```

Aplikasi tinggal menunjuk `public/js/config/qris-config.js` ke URL publik
itu (mis. `https://www.api-shiro.my.id/qris`).

> Catatan Cloudflare: zona dengan *browser integrity check* memblokir UA
> `python-urllib`; webhook server-to-server memakai UA biasa sehingga tetap
> lolos. Pastikan aturan bot tidak memblokir UA webhook penyedia pembayaran.
