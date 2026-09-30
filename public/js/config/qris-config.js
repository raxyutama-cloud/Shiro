// qris-config.js — titik akhir QRIS untuk Shiro.
//
// File ini TIDAK berisi rahasia apa pun (aman di-commit): account_id dan
// secret_token hidup di server proxy (`/var/www/qris-proxy/.env` di server
// Shiro), jadi token tidak pernah masuk ke repo maupun ke dalam APK.
//
// Endpoint yang dipakai aplikasi:
//   POST <apiUrl>/create  { amount, description }
//   POST <apiUrl>/status  { transaction_id }
window.__SHIRO_QRIS__ = {
  apiUrl: "https://www.api-shiro.my.id/qris",
};
