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
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet, IonToast } from '@ionic/vue';
import { onBeforeUnmount, onMounted, ref } from 'vue';

const toastOpen = ref(false);

const handleUnblocked = () => {
  toastOpen.value = true;
};

onMounted(() => {
  window.addEventListener('user-unblocked', handleUnblocked);
});

onBeforeUnmount(() => {
  window.removeEventListener('user-unblocked', handleUnblocked);
});
</script>
