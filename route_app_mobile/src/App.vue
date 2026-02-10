<template>
  <ion-app>
    <ion-router-outlet />
    
    <!-- Toast de déblocage utilisateur -->
    <ion-toast
      :is-open="toastOpen"
      message="Compte débloqué. Tentatives réinitialisées."
      color="success"
      duration="3000"
      position="bottom"
      :icon="checkmarkCircleOutline"
      class="app-toast success-toast"
      @didDismiss="toastOpen = false"
    />
    
    <!-- Toast de notification de signalement -->
    <ion-toast
      :is-open="notifToastOpen"
      :message="notifToastMessage"
      :header="notifToastTitle"
      duration="5000"
      position="top"
      :icon="notificationsOutline"
      class="app-toast notification-toast"
      @didDismiss="notifToastOpen = false"
    />
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet, IonToast } from '@ionic/vue';
import { notificationsOutline, checkmarkCircleOutline } from 'ionicons/icons';
import { onBeforeUnmount, onMounted, ref } from 'vue';

const toastOpen = ref(false);
const notifToastOpen = ref(false);
const notifToastTitle = ref('');
const notifToastMessage = ref('');

const handleUnblocked = () => {
  toastOpen.value = true;
};

const handleStatusChanged = (e: Event) => {
  const detail = (e as CustomEvent).detail;
  notifToastTitle.value = detail.title || '📬 Notification';
  notifToastMessage.value = detail.body || '';
  notifToastOpen.value = true;
};

onMounted(() => {
  window.addEventListener('user-unblocked', handleUnblocked);
  window.addEventListener('signalement-status-changed', handleStatusChanged);
});

onBeforeUnmount(() => {
  window.removeEventListener('user-unblocked', handleUnblocked);
  window.removeEventListener('signalement-status-changed', handleStatusChanged);
});
</script>

<style>
/* ===== STYLES GLOBAUX MOBILE-FIRST ===== */
:root {
  --ion-color-primary: #6aa84f;
  --ion-color-primary-rgb: 106, 168, 79;
  --ion-color-primary-contrast: #ffffff;
  --ion-color-primary-contrast-rgb: 255, 255, 255;
  --ion-color-primary-shade: #5a9640;
  --ion-color-primary-tint: #7ab661;

  --ion-color-secondary: #f5f1e8;
  --ion-color-secondary-rgb: 245, 241, 232;
  --ion-color-secondary-contrast: #2c3e50;
  --ion-color-secondary-contrast-rgb: 44, 62, 80;
  --ion-color-secondary-shade: #d8d4cc;
  --ion-color-secondary-tint: #f6f2ea;

  /* Mobile touch target minimum */
  --touch-target-min: 48px;
  --safe-area-top: env(safe-area-inset-top, 0px);
  --safe-area-bottom: env(safe-area-inset-bottom, 0px);
}

/* ===== RESET MOBILE ===== */
* {
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
}

/* ===== TOASTS REDESIGN ===== */
.app-toast {
  --border-radius: 16px;
  --box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  --min-height: 56px;
  --button-color: white;
  --start: 12px;
  --end: 12px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.1px;
}

/* Toast succès */
.success-toast {
  --background: #2d8a4e;
  --color: white;
}

/* Toast erreur */
.error-toast {
  --background: #d63031;
  --color: white;
}

/* Toast notification signalement */
.notification-toast {
  --background: linear-gradient(135deg, #2c3e50, #34495e);
  --color: white;
  --border-radius: 18px;
  --box-shadow: 0 12px 48px rgba(0, 0, 0, 0.25);
  --min-height: 64px;
}

.notification-toast::part(header) {
  font-weight: 700;
  font-size: 15px;
  color: #6ee7b7;
}

.notification-toast::part(message) {
  font-size: 14px;
  color: #e0e0e0;
  margin-top: 2px;
}

.notification-toast::part(icon) {
  color: #6ee7b7;
  font-size: 24px;
}

/* ===== SCROLLBAR (caché sur mobile) ===== */
::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

/* ===== TRANSITIONS ===== */
ion-page {
  transition: opacity 0.2s ease;
}

/* ===== LOADING REDESIGN ===== */
ion-loading {
  --background: white;
  --spinner-color: #6aa84f;
  --backdrop-opacity: 0.4;
  --border-radius: 20px;
  --min-height: 120px;
  --min-width: 120px;
  --max-width: 200px;
  --box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
}

ion-loading::part(content) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 20px;
}

ion-loading::part(message) {
  font-size: 14px;
  font-weight: 600;
  color: #111111;
  margin-top: 8px;
}

ion-loading::part(spinner) {
  width: 40px;
  height: 40px;
}

/* ===== MODAL MOBILE ===== */
ion-modal {
  --border-radius: 20px 20px 0 0;
}

/* ===== BOUTONS MOBILES (tailles tactiles) ===== */
ion-button {
  text-transform: none;
  letter-spacing: 0.3px;
  --padding-start: 16px;
  --padding-end: 16px;
}

/* ===== INPUTS MOBILES ===== */
ion-input {
  --highlight-height: 2px;
  --highlight-color-focused: #6aa84f;
  --color: #111111;
  --placeholder-color: #888888;
  color: #111111;
}

/* ===== POPUP LEAFLET MOBILE ===== */
.leaflet-popup-content-wrapper {
  border-radius: 14px !important;
  padding: 4px !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15) !important;
}

.leaflet-popup-content {
  font-size: 14px !important;
  line-height: 1.5 !important;
  margin: 12px 14px !important;
}

.leaflet-popup-close-button {
  width: 32px !important;
  height: 32px !important;
  font-size: 20px !important;
  display: flex !important;
  align-items: center;
  justify-content: center;
  top: 6px !important;
  right: 6px !important;
}

/* Liens dans les popups */
.voir-photos-link {
  display: inline-block;
  padding: 8px 14px;
  margin-top: 8px;
  background: #6aa84f;
  color: white !important;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  min-height: 40px;
  line-height: 24px;
}

/* ===== ACTIONSHEET & ALERT MOBILE ===== */
ion-action-sheet {
  --button-min-height: 52px;
}

ion-alert {
  --min-width: 300px;
}

ion-alert .alert-button {
  min-height: 48px;
  font-size: 16px;
}

/* ===== SAFE AREA PADDING ===== */
ion-content {
  --padding-bottom: env(safe-area-inset-bottom, 0px);
}

/* ===== ANIMATIONS DE FEEDBACK TACTILE ===== */
@keyframes tap-feedback {
  0% { transform: scale(1); }
  50% { transform: scale(0.96); }
  100% { transform: scale(1); }
}
</style>