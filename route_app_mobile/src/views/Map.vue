<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Carte</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="openSignalementForm">Signaler</ion-button>
          <ion-button @click="handleLogout">Deconnexion</ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div id="map" class="map-container"></div>

      <ion-modal :is-open="showSignalModal" @didDismiss="showSignalModal = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Nouveau signalement</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="showSignalModal = false">Fermer</ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <ion-list>
            <ion-item>
              <ion-label position="stacked">Date</ion-label>
              <ion-input :value="draft.date_" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Province</ion-label>
              <ion-input :value="draft.provinceId" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Quartier</ion-label>
              <ion-input :value="draft.quartierLabel" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Latitude</ion-label>
              <ion-input :value="draft.latitude" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Longitude</ion-label>
              <ion-input :value="draft.longitude" readonly />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Surface (m2)</ion-label>
              <ion-input v-model="draft.surface" type="number" />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Budget</ion-label>
              <ion-input v-model="draft.budget" type="number" />
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Entreprise</ion-label>
              <ion-select v-model="draft.entrepriseId" placeholder="Choisir">
                <ion-select-option
                  v-for="item in entreprises"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.label }}
                </ion-select-option>
              </ion-select>
            </ion-item>
            <ion-item>
              <ion-label position="stacked">Statut</ion-label>
              <ion-select v-model="draft.statusId" placeholder="Choisir">
                <ion-select-option
                  v-for="item in statuses"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.label }}
                </ion-select-option>
              </ion-select>
            </ion-item>
          </ion-list>
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonModal,
  IonPage,
  IonSelect,
  IonSelectOption,
  IonTitle,
  IonToolbar
} from '@ionic/vue';
import type { LeafletMouseEvent } from 'leaflet';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { clearSession, logoutUser } from '@/services/LoginService';
import { initCarte, type CarteInstance } from '@/services/CarteService';
import {
  applyMapSelection,
  createSignalementDraft,
  getEntreprises,
  getStatuses,
  type SignalementDraft
} from '@/services/SignalementService';
import 'leaflet/dist/leaflet.css';

const router = useRouter();
let carteInstance: CarteInstance | null = null;
const showSignalModal = ref(false);
const entreprises = getEntreprises();
const statuses = getStatuses();
const draft = ref<SignalementDraft>(createSignalementDraft(getUserId()));

const openSignalementForm = () => {
  showSignalModal.value = true;
};

const handleMapClick = (event: LeafletMouseEvent) => {
  draft.value = applyMapSelection(
    draft.value,
    event.latlng.lat,
    event.latlng.lng
  );
  showSignalModal.value = true;
  carteInstance?.map.openPopup('Signaler', event.latlng);
};

function getUserId(): string {
  const raw = localStorage.getItem('user');
  if (!raw) return '';

  try {
    const parsed = JSON.parse(raw);
    return parsed?.uid || '';
  } catch {
    return '';
  }
}

onMounted(() => {
  carteInstance = initCarte('map');
  carteInstance.map.on('click', handleMapClick);
});

onBeforeUnmount(() => {
  if (carteInstance) {
    carteInstance.map.off('click', handleMapClick);
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
