<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Carte</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div id="map" class="map-container"></div>

      <ion-button class="ion-margin-top" expand="block" @click="handleLogout">
        Deconnexion
      </ion-button>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonButton,
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar
} from '@ionic/vue';
import { onBeforeUnmount, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { clearSession, logoutUser } from '@/services/LoginService';
import { initCarte, type CarteInstance } from '@/services/CarteService';
import 'leaflet/dist/leaflet.css';

const router = useRouter();
let carteInstance: CarteInstance | null = null;

onMounted(() => {
  carteInstance = initCarte('map');
});

onBeforeUnmount(() => {
  if (carteInstance) {
    carteInstance.destroy();
    carteInstance = null;
  }
});

const handleLogout = async () => {
  await logoutUser();
  clearSession();
  await router.replace('/login');
};
</script>

<style scoped>
.map-container {
  height: 100%;
  width: 100%;
}
</style>
