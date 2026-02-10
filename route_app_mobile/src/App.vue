<template>
  <ion-app>
    <ion-router-outlet />
    <ion-toast
      :is-open="toastOpen"
      message="Compte debloque. Tentatives reinitialisees."
      color="success"
      duration="2500"
      @didDismiss="toastOpen = false"
    />
    <ion-toast
      :is-open="notifToastOpen"
      :message="notifToastMessage"
      :header="notifToastTitle"
      color="primary"
      duration="4000"
      position="top"
      @didDismiss="notifToastOpen = false"
    />
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet, IonToast } from '@ionic/vue';
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
  notifToastTitle.value = detail.title || 'Notification';
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
