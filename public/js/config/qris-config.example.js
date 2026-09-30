// qris-config.example.js — salin ke qris-config.js lalu isi URL proxy kamu.
//
// Konfigurasi ini hanya berisi URL. Token buatqris.site TIDAK boleh ada di
// sini: simpan di server proxy (EnvironmentFile .env) supaya tidak ikut
// ter-embed ke dalam APK.
window.__SHIRO_QRIS__ = {
  // proxy yang meneruskan ke https://api.buatqris.site
  apiUrl: "https://www.api-shiro.my.id/qris",
};
