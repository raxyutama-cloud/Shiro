// qris-config.example.js — Salin ke qris-config.js lalu isi kredensial asli.
// qris-config.js TIDAK di-commit (lihat .gitignore); di CI file ini dibuat
// otomatis dari GitHub Actions secrets (BQ_ACCOUNT_ID / BQ_SECRET_TOKEN).
//
// PENTING: secret_token bersifat rahasia. Jangan pernah commit token asli.
window.__SHIRO_QRIS__ = {
  apiUrl: "https://api.buatqris.site",
  accountId: "YOUR_ACCOUNT_ID",
  secretToken: "YOUR_SECRET_TOKEN",
  qrisMethod: "qris_two",
  feeBy: "user",
};
