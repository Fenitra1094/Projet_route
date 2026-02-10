<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Carte</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="handleLogout">Deconnexion</ion-button>
        </ion-buttons>
      </ion-toolbar>
      <ion-toolbar>
        <ion-item lines="none">
          <ion-label>Afficher mes signalements uniquement</ion-label>
          <ion-toggle v-model="showMineOnly" />
        </ion-item>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div class="map-wrapper">
        <!-- PANNEAU PHOTOS (gauche) -->
        <div v-if="selectedPhotos.length" class="photo-panel">
          <div class="photo-panel-header">
            <strong>Photos du signalement</strong>
            <ion-button fill="clear" size="small" @click="selectedPhotos = []">✕</ion-button>
          </div>
          <div class="photo-panel-body">
            <img
              v-for="(url, i) in selectedPhotos"
              :key="i"
              :src="url"
              :alt="'Photo ' + (i + 1)"
              class="photo-full"
            />
          </div>
        </div>

        <!-- CARTE -->
        <div class="map-side">
          <div id="map" class="map-container"></div>
          <div class="summary-panel">
            <div class="summary-item">Points: {{ summary.totalPoints }}</div>
            <div class="summary-item">Surface: {{ summary.totalSurface }} m2</div>
            <div class="summary-item">Avancement: {{ summary.avancement }}%</div>
            <div class="summary-item">Budget: {{ summary.totalBudget }}</div>
          </div>
        </div>
      </div>

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
              <ion-input :value="draft.province" readonly />
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
              <ion-label position="stacked">Photos</ion-label>
              <input
                class="photo-input"
                type="file"
                accept="image/*"
                multiple
                @change="handlePhotoChange"
              />
            </ion-item>
          </ion-list>
          <ion-button expand="block" class="ion-margin-top" @click="handleSave">
            Enregistrer
          </ion-button>
        </ion-content>
      </ion-modal>
      <ion-loading :is-open="saving" message="Enregistrement..." />
      <ion-toast
        :is-open="toastOpen"
        :message="toastMessage"
        :color="toastColor"
        duration="2500"
        @didDismiss="toastOpen = false"
      />
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
  IonLoading,
  IonList,
  IonModal,
  IonPage,
  IonToggle,
  IonToast,
  IonTitle,
  IonToolbar
} from '@ionic/vue';
import L, { type LeafletMouseEvent } from 'leaflet';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { clearSession, logoutUser } from '@/services/LoginService';
import { stopSignalementNotificationListener } from '@/services/NotificationService';
import { initCarte, type CarteInstance } from '@/services/CarteService';
import { getPhotosForSignalement } from '@/services/firebaseService';
import {
  applyMapSelection,
  createSignalementDraft,
  fetchQuartiers,
  fetchStatuses,
  listenSignalements,
  saveSignalement,
  updateDraftPhotos,
  type QuartierRef,
  type SignalementDraft
} from '@/services/SignalementService';
import 'leaflet/dist/leaflet.css';

const router = useRouter();
let carteInstance: CarteInstance | null = null;
const showSignalModal = ref(false);
const quartiers = ref<QuartierRef[]>([]);
const draft = ref<SignalementDraft>(createSignalementDraft(getUserId(), getUserEmail()));
const showMineOnly = ref(false);
const saving = ref(false);
const toastOpen = ref(false);
const toastMessage = ref('');
const selectedPhotos = ref<string[]>([]);
const toastColor = ref<'success' | 'danger'>('success');
let signalementLayer: L.LayerGroup | null = null;
let signalementUnsub: (() => void) | null = null;
const currentUserId = getUserId();
const allSignalements = ref<any[]>([]);
const visibleSignalements = ref<any[]>([]);
const statusLabelById = ref<Map<number, string>>(new Map());

const summary = computed(() => {
  const totalPoints = visibleSignalements.value.length;
  const totalSurface = visibleSignalements.value.reduce((sum, item) => {
    const value = Number(item.surface ?? 0);
    return sum + (Number.isFinite(value) ? value : 0);
  }, 0);
  const totalBudget = visibleSignalements.value.reduce((sum, item) => {
    const value = Number(item.budget ?? 0);
    return sum + (Number.isFinite(value) ? value : 0);
  }, 0);

  const totalProgress = visibleSignalements.value.reduce((sum, item) => {
    const status = getStatusLabel(item);
    if (status === 'termine') return sum + 100;
    if (status === 'en cours') return sum + 50;
    return sum + 0;
  }, 0);
  const avancement = totalPoints ? Math.round(totalProgress / totalPoints) : 0;

  return {
    totalPoints,
    totalSurface: Math.round(totalSurface * 100) / 100,
    totalBudget: Math.round(totalBudget * 100) / 100,
    avancement
  };
});

const handleMapClick = async (event: LeafletMouseEvent) => {
  draft.value = await applyMapSelection(
    draft.value,
    event.latlng.lat,
    event.latlng.lng,
    quartiers.value
  );
  showSignalModal.value = true;
  carteInstance?.map.openPopup('Signaler', event.latlng);
};

const handlePhotoChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  draft.value = updateDraftPhotos(draft.value, input.files);
};

const handleSave = async () => {
  try {
    saving.value = true;
    await saveSignalement(draft.value);
    toastMessage.value = 'Signalement enregistre.';
    toastColor.value = 'success';
    toastOpen.value = true;
    showSignalModal.value = false;
    draft.value = createSignalementDraft(getUserId(), getUserEmail());
  } catch (error) {
    toastMessage.value = 'Erreur lors de l\'enregistrement.';
    toastColor.value = 'danger';
    toastOpen.value = true;
  } finally {
    saving.value = false;
  }
};

function getUserId(): number {
  const raw = localStorage.getItem('user');
  if (!raw) return 0;

  try {
    const parsed = JSON.parse(raw);
    return Number(parsed?.id_user || 0);
  } catch {
    return 0;
  }
}

const normalizeStatusLabel = (value: string) =>
  value
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace('_', ' ');

const getStatusLabel = (item: any) => {
  const raw = item.status || item.statusLabel || item.statusId || item.id_status;
  if (typeof raw === 'string') {
    const normalized = normalizeStatusLabel(raw);
    if (normalized.includes('en cours')) return 'en cours';
    if (normalized.includes('termine')) return 'termine';
    if (normalized.includes('nouveau')) return 'nouveau';
  }

  const numeric = Number(raw);
  if (Number.isFinite(numeric)) {
    const label = statusLabelById.value.get(numeric);
    if (label) return normalizeStatusLabel(label);
  }

  return 'nouveau';
};

function getUserEmail(): string {
  const raw = localStorage.getItem('user');
  if (!raw) return '';

  try {
    const parsed = JSON.parse(raw);
    return parsed?.email || '';
  } catch {
    return '';
  }
}

const renderSignalements = (items: any[]) => {
  allSignalements.value = items;
  applyFilter();
};

const applyFilter = () => {
  if (!carteInstance) return;

  if (!signalementLayer) {
    signalementLayer = L.layerGroup().addTo(carteInstance.map);
  }

  signalementLayer.clearLayers();

  const statusIcons: Record<string, string> = {
    nouveau: '🆕',
    'en cours': '⚠️',
    termine: '✅'
  };

  const items = allSignalements.value;
  const visibleItems = showMineOnly.value
    ? items.filter((item) => Number(item.id_user) === currentUserId)
    : items;

  visibleSignalements.value = visibleItems;

  for (const item of visibleItems) {
    if (item.latitude == null || item.longitude == null) continue;
    const status = getStatusLabel(item);
    const normalizedStatus = status.toLowerCase();
    const iconLabel = statusIcons[normalizedStatus] || '📍';
    const icon = L.divIcon({
      className: 'signalement-icon',
      html: `<span>${iconLabel}</span>`
    });
    const marker = L.marker([item.latitude, item.longitude], { icon });
    const idSig = item.id_signalement;
    const content = `
      <strong>${item.province || item.provinceId || 'Antananarivo'}</strong><br />
      Quartier: ${item.quartier || item.quartierLabel || item.quartierId || 'Quartier'}<br />
      Date: ${item.date_ || ''}<br />
      Statut: ${status}<br />
      Surface: ${item.surface || ''} m2<br />
      Budget: ${item.budget || ''}<br />
      Entreprise: ${item.id_entreprise || item.entrepriseId || ''}<br />
      <span class="photo-placeholder" data-id="${idSig}">Chargement photos...</span>
    `;
    marker.bindPopup(content);
    marker.on('popupopen', async () => {
      if (!idSig) return;
      const urls = await getPhotosForSignalement(idSig);
      const el = document.querySelector(`.photo-placeholder[data-id="${idSig}"]`);
      if (el) {
        if (urls.length) {
          el.innerHTML = `<a href="#" class="voir-photos-link" data-id="${idSig}">📷 Voir photos (${urls.length})</a>`;
          const link = el.querySelector('.voir-photos-link');
          if (link) {
            link.addEventListener('click', (e) => {
              e.preventDefault();
              selectedPhotos.value = urls;
            });
          }
        } else {
          el.textContent = 'Aucune photo';
        }
      }
    });
    marker.addTo(signalementLayer);
  }
};

// Re-filtrer quand le toggle change
watch(showMineOnly, () => {
  applyFilter();
});

onMounted(() => {
  carteInstance = initCarte('map');
  carteInstance.map.on('click', handleMapClick);
  signalementLayer = L.layerGroup().addTo(carteInstance.map);

  fetchQuartiers()
    .then((quartiersData) => {
      quartiers.value = quartiersData;
    })
    .catch(() => {
      // Ignore load errors for now.
    });

  fetchStatuses()
    .then((options) => {
      const map = new Map<number, string>();
      for (const option of options) {
        const id = Number(option.id);
        if (Number.isFinite(id)) {
          map.set(id, option.label);
        }
      }
      statusLabelById.value = map;
    })
    .catch(() => {
      // Ignore load errors for now.
    });

  signalementUnsub = listenSignalements(renderSignalements);
});

onBeforeUnmount(() => {
  if (carteInstance) {
    carteInstance.map.off('click', handleMapClick);
    carteInstance.destroy();
    carteInstance = null;
  }

  if (signalementLayer) {
    signalementLayer.clearLayers();
    signalementLayer = null;
  }

  if (signalementUnsub) {
    signalementUnsub();
    signalementUnsub = null;
  }
});

const handleLogout = async () => {
  stopSignalementNotificationListener();
  await logoutUser();
  clearSession();
  await router.replace('/login');
};
</script>

<style scoped>
.map-wrapper {
  display: flex;
  height: 100%;
  width: 100%;
  position: relative;
}

.photo-panel {
  width: 280px;
  min-width: 220px;
  max-width: 40%;
  height: 100%;
  overflow-y: auto;
  background: #f8f9fa;
  border-right: 2px solid #dee2e6;
  z-index: 999;
  display: flex;
  flex-direction: column;
}

.photo-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fff;
  border-bottom: 1px solid #dee2e6;
  font-size: 14px;
  position: sticky;
  top: 0;
  z-index: 1;
}

.photo-panel-body {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.photo-full {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  object-fit: contain;
}

.map-side {
  flex: 1;
  position: relative;
  min-width: 0;
}

.map-container {
  height: 100%;
  width: 100%;
}

.summary-panel {
  position: absolute;
  top: 64px;
  left: 12px;
  right: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 10px 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  font-size: 14px;
}

.summary-item {
  color: #1f2933;
  font-weight: 600;
}

.signalement-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  font-size: 22px;
}
</style>
