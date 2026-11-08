// Service Worker for Gemini Mod Prompt Generator PWA (iOS/Android 离线应用支持)
const CACHE_NAME = 'gemini-mod-prompt-v1';
const ASSETS_TO_CACHE = [
  './index.html',
  './css/gemini.css',
  './css/prompt-generator.css',
  './js/icons.js',
  './js/prompt-generator.js',
  './js/prompt-ui.js'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS_TO_CACHE)).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => Promise.all(
      keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))
    )).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((cached) => cached || fetch(event.request))
  );
});
