// Service Worker for Pusher Beams Push Notifications
// This file must be at the root level (public/) or /src/

// The Pusher SDK will inject notification handling here
// Basic service worker that allows Pusher Beams to function

self.addEventListener('install', (event) => {
  console.log('[Service Worker] Installing...');
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  console.log('[Service Worker] Activating...');
  self.clients.claim();
});

self.addEventListener('push', (event) => {
  console.log('[Service Worker] Push notification received:', event);
  // Pusher Beams SDK handles the push notification display
  if (event.data) {
    const data = event.data.json();
    console.log('[Service Worker] Notification data:', data);
  }
});

self.addEventListener('notificationclick', (event) => {
  console.log('[Service Worker] Notification clicked:', event);
  event.notification.close();
});
