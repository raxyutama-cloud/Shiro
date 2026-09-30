// nativeSync.js — native SharedPreferences bridge & platform inspection
import { platformVal } from "../core.js";

/**
 * Synchronizes a single Shiro setting to Android SharedPreferences for background & share activities
 */
export function syncSettingToNative(key, val) {
  if (window.ShiroMainBridge?.saveSetting) {
    try {
      window.ShiroMainBridge.saveSetting(key, String(val));
    } catch (e) {
      console.error("syncSettingToNative error", e);
    }
  }
}

/**
 * Iterates through all core setting keys and syncs them to native SharedPreferences
 */
export function syncAllSettingsToNative() {
  const keys = [
    "shiro_lang",
    "shiro_theme",
    "shiro_font",
    "shiro_prefer_server",
    "shiro_download_path",
    "shiro_auto_folder",
    "shiro_filename",
    "shiro_incognito",
    "shiro_auto_download",
    "shiro_wifi_only",
  ];
  keys.forEach((key) => {
    const val = localStorage.getItem(key);
    if (val !== null) {
      syncSettingToNative(key, val);
    }
  });
}

/**
 * Inspects device environment and updates the platform display label in settings
 */
export function initPlatformDisplay() {
  if (!platformVal) return;

  const tauriInvoke =
    window.__TAURI__?.core?.invoke ||
    window.__TAURI_INTERNALS__?.invoke ||
    window.__TAURI__?.invoke;
  const isDesktop = Boolean(tauriInvoke && !window.Capacitor?.isNativePlatform?.());

  if (isDesktop) {
    const ua = (navigator.userAgent || "").toLowerCase();
    if (ua.includes("mac")) {
      platformVal.textContent = "macOS";
    } else if (ua.includes("win")) {
      platformVal.textContent = "Windows";
    } else if (ua.includes("linux")) {
      platformVal.textContent = "Linux";
    } else {
      platformVal.textContent = "Desktop";
    }
  } else {
    const capPlatform = window.Capacitor?.getPlatform?.();
    if (capPlatform === "ios") {
      platformVal.textContent = "iOS";
    } else if (capPlatform === "android") {
      platformVal.textContent = "Android";
    } else {
      platformVal.textContent = "Web Browser";
    }
  }
}
