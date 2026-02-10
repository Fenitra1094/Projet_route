<template>
  <ion-page>
    <!-- Header compact mobile -->
    <ion-header class="custom-header" mode="md">
      <ion-toolbar class="custom-toolbar">
        <ion-title class="header-title">Signalements</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="handleLogout" class="header-action-btn" aria-label="Déconnexion">
            <ion-icon :icon="logOutOutline" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <!-- CARTE plein écran -->
      <div class="map-wrapper">
        <!-- PANNEAU PHOTOS (latéral) -->
        <div v-if="selectedPhotos.length" class="photo-panel">
          <div class="photo-panel-header">
            <strong>Photos</strong>
            <ion-button fill="clear" class="photo-close-btn" @click="selectedPhotos = []">
              <ion-icon :icon="closeOutline" />
            </ion-button>
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

          <!-- Chip filtre flottant -->
          <div class="floating-filter" @click="showMineOnly = !showMineOnly">
            <ion-icon :icon="showMineOnly ? personOutline : peopleOutline" class="filter-chip-icon" />
            <span class="filter-chip-label">{{ showMineOnly ? 'Mes signalements' : 'Tous' }}</span>
          </div>

          <!-- Bouton flottant vers le récapitulatif -->
          <div class="floating-recap-btn" @click="showSummary = !showSummary">
            <ion-icon :icon="barChartOutline" class="recap-btn-icon" />
            <span class="recap-btn-label">Récapitulatif</span>
          </div>

          <!-- Panneau récapitulatif mobile (slide-up) -->
          <transition name="slide-up">
            <div v-if="showSummary" class="summary-bottom-sheet">
              <div class="sheet-handle" @click="showSummary = false"></div>
              <div class="summary-grid-mobile">
                <div class="summary-chip">
                  <span class="chip-icon">📍</span>
                  <div class="chip-data">
                    <span class="chip-value">{{ summary.totalPoints }}</span>
                    <span class="chip-label">Points</span>
                  </div>
                </div>
                <div class="summary-chip">
                  <span class="chip-icon">📏</span>
                  <div class="chip-data">
                    <span class="chip-value">{{ summary.totalSurface }}</span>
                    <span class="chip-label">m²</span>
                  </div>
                </div>
                <div class="summary-chip">
                  <span class="chip-icon">📊</span>
                  <div class="chip-data">
                    <span class="chip-value">{{ summary.avancement }}%</span>
                    <span class="chip-label">Avancement</span>
                  </div>
                </div>
                <div class="summary-chip">
                  <span class="chip-icon">💰</span>
                  <div class="chip-data">
                    <span class="chip-value">{{ summary.totalBudget }}</span>
                    <span class="chip-label">Budget</span>
                  </div>
                </div>
              </div>
            </div>
          </transition>

          <!-- FICHE DÉTAIL SIGNALEMENT (bottom sheet natif Vue) -->
          <transition name="slide-up">
            <div v-if="showDetail && selectedSignalement" class="detail-bottom-sheet">
              <div class="sheet-handle" @click="showDetail = false"></div>
              <div class="detail-header">
                <span class="detail-status-badge" :class="'status-' + getStatusLabel(selectedSignalement)">
                  {{ getStatusLabel(selectedSignalement) === 'nouveau' ? '🆕 Nouveau' : getStatusLabel(selectedSignalement) === 'en cours' ? '⚠️ En cours' : '✅ Terminé' }}
                </span>
                <button class="detail-close" @click="showDetail = false">
                  <ion-icon :icon="closeOutline" />
                </button>
              </div>
              <div class="detail-body">
                <div class="detail-row">
                  <span class="detail-label">Province</span>
                  <span class="detail-value">{{ selectedSignalement.province || selectedSignalement.provinceId || 'Antananarivo' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">Quartier</span>
                  <span class="detail-value">{{ selectedSignalement.quartier || selectedSignalement.quartierLabel || selectedSignalement.quartierId || '-' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">Date</span>
                  <span class="detail-value">{{ selectedSignalement.date_ || '-' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">Surface</span>
                  <span class="detail-value">{{ selectedSignalement.surface || '-' }} m²</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">Budget</span>
                  <span class="detail-value">{{ selectedSignalement.budget || '-' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">Entreprise</span>
                  <span class="detail-value">{{ selectedSignalement.id_entreprise || selectedSignalement.entrepriseId || '-' }}</span>
                </div>
              </div>
              <div class="detail-photos-section">
                <div v-if="detailPhotosLoading" class="detail-photos-loading">Chargement photos...</div>
                <div v-else-if="detailPhotos.length" class="detail-photos">
                  <span class="detail-photos-title">📷 {{ detailPhotos.length }} photo(s)</span>
                  <div class="detail-photos-scroll">
                    <img
                      v-for="(url, i) in detailPhotos"
                      :key="i"
                      :src="url"
                      :alt="'Photo ' + (i + 1)"
                      class="detail-photo-thumb"
                      @click="selectedPhotos = detailPhotos"
                    />
                  </div>
                </div>
                <div v-else class="detail-no-photo">Aucune photo</div>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- Modal signalement (mode bottom-sheet mobile) -->
      <ion-modal 
        :is-open="showSignalModal" 
        @didDismiss="showSignalModal = false"
        :breakpoints="[0, 0.95]"
        :initial-breakpoint="0.95"
        mode="ios"
        class="mobile-modal"
      >
        <ion-header>
          <ion-toolbar class="modal-toolbar">
            <ion-buttons slot="start">
              <ion-button @click="showSignalModal = false" class="modal-close-btn">
                <ion-icon :icon="chevronDownOutline" />
              </ion-button>
            </ion-buttons>
            <ion-title class="modal-title">Nouveau signalement</ion-title>
          </ion-toolbar>
        </ion-header>
        <ion-content class="modal-content">
          <div class="modal-form">
            <div class="form-section">
              <h3 class="section-title">
                <ion-icon :icon="informationCircleOutline" class="section-icon" />
                Informations
              </h3>
              
              <div class="form-group">
                <label class="form-label">Date</label>
                <ion-input :value="draft.date_" readonly class="custom-input" fill="outline" />
              </div>
              
              <div class="form-group">
                <label class="form-label">Province</label>
                <ion-input :value="draft.province" readonly class="custom-input" fill="outline" />
              </div>
              
              <div class="form-group">
                <label class="form-label">Quartier</label>
                <ion-input :value="draft.quartierLabel" readonly class="custom-input" fill="outline" />
              </div>
            </div>

            <div class="form-section">
              <h3 class="section-title">
                <ion-icon :icon="locationOutline" class="section-icon" />
                Localisation
              </h3>
              
              <div class="coordinates-grid">
                <div class="form-group">
                  <label class="form-label">Latitude</label>
                  <ion-input :value="draft.latitude" readonly class="custom-input" fill="outline" />
                </div>
                
                <div class="form-group">
                  <label class="form-label">Longitude</label>
                  <ion-input :value="draft.longitude" readonly class="custom-input" fill="outline" />
                </div>
              </div>
            </div>

            <div class="form-section">
              <h3 class="section-title">
                <ion-icon :icon="cameraOutline" class="section-icon" />
                Photos
              </h3>
              
              <div class="upload-area">
                <div class="photo-buttons">
                  <button class="photo-choice-btn camera-btn" @click="takePhotos">
                    <ion-icon :icon="cameraOutline" class="upload-icon" />
                    <span>Prendre des photos</span>
                  </button>
                  <button class="photo-choice-btn gallery-btn" @click="pickFromGallery">
                    <ion-icon :icon="imageOutline" class="upload-icon" />
                    <span>Choisir depuis la galerie</span>
                  </button>
                </div>
                <div v-if="draft.photos.length" class="photo-preview-list">
                  <div class="photo-preview-header">
                    {{ draft.photos.length }} photo(s) sélectionnée(s)
                  </div>
                  <div class="photo-thumbs">
                    <div v-for="(photo, i) in photoThumbnails" :key="i" class="photo-thumb-wrapper">
                      <img :src="photo" class="photo-thumb" :alt="'Photo ' + (i + 1)" />
                      <button class="photo-remove-btn" @click="removePhoto(i)">
                        <ion-icon :icon="closeOutline" />
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-actions">
              <ion-button expand="block" class="save-button" @click="handleSave" :disabled="saving">
                <ion-icon :icon="checkmarkCircleOutline" slot="start" />
                {{ saving ? 'Enregistrement...' : 'Enregistrer' }}
              </ion-button>
              <ion-button expand="block" fill="outline" class="cancel-button" @click="handleCancel">
                <ion-icon :icon="closeOutline" slot="start" />
                Annuler
              </ion-button>
            </div>
          </div>
        </ion-content>
      </ion-modal>

      <ion-loading 
        :is-open="saving" 
        message="Enregistrement..." 
        spinner="crescent"
        css-class="save-loading"
      />
      <ion-toast
        :is-open="toastOpen"
        :message="toastMessage"
        :icon="toastColor === 'success' ? checkmarkCircleOutline : alertCircleOutline"
        duration="2500"
        position="bottom"
        :class="['app-toast', toastColor === 'success' ? 'success-toast' : 'error-toast']"
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
  IonIcon,
  IonInput,
  IonLoading,
  IonModal,
  IonPage,
  IonToast,
  IonTitle,
  IonToolbar
} from '@ionic/vue';
import { 
  closeOutline, logOutOutline, cameraOutline, 
  personOutline, peopleOutline,
  chevronDownOutline, informationCircleOutline, 
  locationOutline, checkmarkCircleOutline, alertCircleOutline,
  barChartOutline, imageOutline
} from 'ionicons/icons';
import L, { type LeafletMouseEvent } from 'leaflet';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
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
const showSummary = ref(false);
const quartiers = ref<QuartierRef[]>([]);
const draft = ref<SignalementDraft>(createSignalementDraft(getUserId(), getUserEmail()));
const showMineOnly = ref(false);
const saving = ref(false);
const toastOpen = ref(false);
const toastMessage = ref('');
const selectedPhotos = ref<string[]>([]);
const toastColor = ref<'success' | 'danger'>('success');
const selectedSignalement = ref<any>(null);
const showDetail = ref(false);
const detailPhotos = ref<string[]>([]);
const detailPhotosLoading = ref(false);
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

let markerJustClicked = false;

const handleMapClick = async (event: LeafletMouseEvent) => {
  if (markerJustClicked) {
    markerJustClicked = false;
    return;
  }
  if (showDetail.value) {
    showDetail.value = false;
    return;
  }
  draft.value = await applyMapSelection(
    draft.value,
    event.latlng.lat,
    event.latlng.lng,
    quartiers.value
  );
  showSignalModal.value = true;
};

const dataURLtoFile = (dataUrl: string, filename: string): File => {
  const [header, base64] = dataUrl.split(',');
  const mime = header.match(/:(.*?);/)?.[1] || 'image/jpeg';
  const binary = atob(base64);
  const array = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) array[i] = binary.charCodeAt(i);
  return new File([array], filename, { type: mime });
};

const takePhotos = async () => {
  try {
    const photo = await Camera.getPhoto({
      quality: 80,
      resultType: CameraResultType.DataUrl,
      source: CameraSource.Camera
    });
    if (photo.dataUrl) {
      const file = dataURLtoFile(photo.dataUrl, `photo_${Date.now()}.jpg`);
      draft.value = {
        ...draft.value,
        photos: [...draft.value.photos, file]
      };
    }
  } catch (e) {
    // L'utilisateur a annulé
  }
};

const pickFromGallery = async () => {
  try {
    const photos = await Camera.pickImages({
      quality: 80
    });
    const files: File[] = [];
    for (const photo of photos.photos) {
      if (photo.webPath) {
        const response = await fetch(photo.webPath);
        const blob = await response.blob();
        files.push(new File([blob], `photo_${Date.now()}_${files.length}.jpg`, { type: 'image/jpeg' }));
      }
    }
    if (files.length) {
      draft.value = {
        ...draft.value,
        photos: [...draft.value.photos, ...files]
      };
    }
  } catch (e) {
    // L'utilisateur a annulé
  }
};

const photoThumbnails = computed(() => {
  return draft.value.photos.map((file) => URL.createObjectURL(file));
});

const removePhoto = (index: number) => {
  draft.value = {
    ...draft.value,
    photos: draft.value.photos.filter((_, i) => i !== index)
  };
};

const handleCancel = () => {
  showSignalModal.value = false;
  draft.value = createSignalementDraft(getUserId(), getUserEmail());
};

const handleSave = async () => {
  try {
    saving.value = true;
    await saveSignalement(draft.value);
    toastMessage.value = 'Signalement enregistré avec succès.';
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
      html: `<span>${iconLabel}</span>`,
      iconSize: [52, 52],
      iconAnchor: [26, 26]
    });
    const marker = L.marker([item.latitude, item.longitude], { icon, bubblingMouseEvents: false, interactive: true });
    const idSig = item.id_signalement;
    const signalItem = item;

    // Utiliser les événements DOM natifs (touchend) au lieu de Leaflet
    // car Leaflet intercepte les événements tactiles sur Android/Capacitor
    marker.once('add', () => {
      const el = marker.getElement();
      if (!el) return;
      el.style.zIndex = '1000';
      el.style.pointerEvents = 'auto';
      el.addEventListener('touchend', (e: TouchEvent) => {
        e.preventDefault();
        e.stopPropagation();
        markerJustClicked = true;
        selectedSignalement.value = signalItem;
        showDetail.value = true;
        detailPhotos.value = [];
        detailPhotosLoading.value = true;
        if (idSig) {
          getPhotosForSignalement(idSig)
            .then((urls) => { detailPhotos.value = urls; })
            .catch(() => { detailPhotos.value = []; })
            .finally(() => { detailPhotosLoading.value = false; });
        } else {
          detailPhotosLoading.value = false;
        }
      }, { passive: false });
      // Fallback pour desktop/navigateur
      el.addEventListener('click', (e: MouseEvent) => {
        e.stopPropagation();
        markerJustClicked = true;
        selectedSignalement.value = signalItem;
        showDetail.value = true;
        detailPhotos.value = [];
        detailPhotosLoading.value = true;
        if (idSig) {
          getPhotosForSignalement(idSig)
            .then((urls) => { detailPhotos.value = urls; })
            .catch(() => { detailPhotos.value = []; })
            .finally(() => { detailPhotosLoading.value = false; });
        } else {
          detailPhotosLoading.value = false;
        }
      });
    });

    marker.addTo(signalementLayer);
  }
};

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
/* ===== HEADER MOBILE ===== */
.custom-header {
  --background: #6aa84f;
}

.custom-toolbar {
  --background: #6aa84f;
  --color: white;
  --min-height: 56px;
  --padding-start: 4px;
  --padding-end: 4px;
}

.header-title {
  font-weight: 700;
  font-size: 18px;
  letter-spacing: -0.3px;
}

.header-action-btn {
  --color: white;
  width: 48px;
  height: 48px;
  min-width: 48px;
}

.header-action-btn ion-icon {
  font-size: 24px;
}

/* ===== CARTE ===== */
.map-wrapper {
  display: flex;
  height: 100%;
  width: 100%;
  position: relative;
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

/* ===== PANNEAU PHOTOS (latéral) ===== */
.photo-panel {
  width: 280px;
  min-width: 240px;
  max-width: 45%;
  height: 100%;
  overflow-y: auto;
  background: #f5f1e8;
  border-right: 3px solid #6aa84f;
  z-index: 999;
  display: flex;
  flex-direction: column;
  -webkit-overflow-scrolling: touch;
}

.photo-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: white;
  border-bottom: 2px solid #e0e0e0;
  font-size: 16px;
  font-weight: 700;
  color: #111111;
  position: sticky;
  top: 0;
  z-index: 1;
  min-height: 52px;
}

.photo-close-btn {
  --color: #555;
  min-width: 44px;
  min-height: 44px;
}

.photo-close-btn ion-icon {
  font-size: 24px;
}

.photo-panel-body {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.photo-full {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  object-fit: contain;
}

/* ===== FILTRE FLOTTANT (chip tactile) ===== */
.floating-filter {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 6px;
  background: white;
  border-radius: 24px;
  padding: 10px 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  min-height: 44px;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.floating-filter:active {
  transform: scale(0.95);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.filter-chip-icon {
  font-size: 20px;
  color: #6aa84f;
}

.filter-chip-label {
  font-size: 14px;
  font-weight: 600;
  color: #111111;
  white-space: nowrap;
}

/* ===== BOUTON FLOTTANT RÉCAPITULATIF ===== */
.floating-recap-btn {
  position: absolute;
  top: 12px;
  left: 52px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #6aa84f;
  border-radius: 24px;
  padding: 10px 16px;
  box-shadow: 0 4px 16px rgba(106, 168, 79, 0.35);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  min-height: 44px;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.floating-recap-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2px 8px rgba(106, 168, 79, 0.25);
}

.recap-btn-icon {
  font-size: 20px;
  color: white;
}

.recap-btn-label {
  font-size: 14px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
}

/* ===== BOTTOM SHEET RÉCAPITULATIF ===== */
.summary-bottom-sheet {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.12);
  padding: 8px 16px 20px;
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0px));
}

.sheet-handle {
  width: 40px;
  height: 5px;
  background: #d1d5db;
  border-radius: 3px;
  margin: 0 auto 12px;
  cursor: pointer;
}

.summary-grid-mobile {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.summary-footer {
  margin-top: 12px;
  text-align: center;
}

.recap-link-btn {
  --color: #6aa84f;
  font-size: 14px;
  font-weight: 600;
  text-transform: none;
  min-height: 44px;
}

.summary-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8f9fa;
  border-radius: 14px;
  padding: 14px;
  min-height: 60px;
}

.chip-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.chip-data {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chip-value {
  font-size: 18px;
  font-weight: 700;
  color: #111111;
  line-height: 1.1;
}

.chip-label {
  font-size: 12px;
  color: #444444;
  margin-top: 2px;
}

/* ===== MODAL MOBILE (bottom sheet) ===== */
.mobile-modal {
  --border-radius: 20px 20px 0 0;
}

.modal-toolbar {
  --background: white;
  --color: #2c3e50;
  --border-width: 0;
  --min-height: 56px;
}

.modal-title {
  font-weight: 700;
  font-size: 18px;
  color: #111111;
}

.modal-close-btn {
  --color: #7f8c8d;
  min-width: 48px;
  min-height: 48px;
}

.modal-close-btn ion-icon {
  font-size: 28px;
}

.modal-content {
  --background: #f5f1e8;
}

.modal-form {
  padding: 16px;
}

.form-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #111111;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #6aa84f;
}

.section-icon {
  font-size: 20px;
  color: #6aa84f;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #111111;
  margin-bottom: 6px;
}

.custom-input {
  --background: #f8f9fa;
  --border-radius: 12px;
  --padding-start: 16px;
  --padding-end: 16px;
  --padding-top: 12px;
  --padding-bottom: 12px;
  font-size: 16px;
  min-height: 52px;
  color: #111111;
  --color: #111111;
  --placeholder-color: #888888;
}

.coordinates-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

/* ===== UPLOAD PHOTOS ===== */
.upload-area {
  margin-top: 4px;
}

.photo-buttons {
  display: flex;
  gap: 10px;
}

.photo-choice-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 12px;
  border: 2px dashed #6aa84f;
  border-radius: 14px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  min-height: 64px;
  -webkit-tap-highlight-color: transparent;
  transition: background 0.2s ease;
  background: none;
  font-family: inherit;
}

.camera-btn {
  background: #f0faf0;
  color: #6aa84f;
}

.gallery-btn {
  background: #f0f4fa;
  color: #3d7ab5;
  border-color: #3d7ab5;
}

.photo-choice-btn:active {
  transform: scale(0.97);
  opacity: 0.85;
}

.photo-preview-count {
  margin-top: 10px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #6aa84f;
  padding: 8px;
  background: #f0faf0;
  border-radius: 10px;
}

.upload-icon {
  font-size: 28px;
}

/* ===== ACTIONS MODAL ===== */
.modal-actions {
  padding: 4px 0 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom, 0px));
}

.save-button {
  --background: linear-gradient(135deg, #6aa84f, #5a9640);
  --background-activated: #4a8630;
  --border-radius: 14px;
  --box-shadow: 0 4px 16px rgba(106, 168, 79, 0.35);
  font-weight: 700;
  font-size: 17px;
  height: 56px;
  margin: 0;
  text-transform: none;
}

.save-button ion-icon {
  font-size: 22px;
}

.cancel-button {
  --border-radius: 14px;
  --border-color: #ccc;
  --color: #666;
  font-weight: 600;
  font-size: 16px;
  height: 52px;
  margin: 10px 0 0;
  text-transform: none;
}

/* ===== PHOTOS PREVIEW ===== */
.photo-preview-list {
  margin-top: 12px;
}

.photo-preview-header {
  font-size: 14px;
  font-weight: 600;
  color: #6aa84f;
  margin-bottom: 8px;
  text-align: center;
}

.photo-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.photo-thumb-wrapper {
  position: relative;
  width: 72px;
  height: 72px;
}

.photo-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
  border: 2px solid #e0e0e0;
}

.photo-remove-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e74c3c;
  color: white;
  border: 2px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  padding: 0;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

/* ===== FICHE DÉTAIL SIGNALEMENT ===== */
.detail-bottom-sheet {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1001;
  background: white;
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.15);
  padding: 8px 16px 20px;
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0px));
  max-height: 65%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.detail-status-badge {
  font-size: 14px;
  font-weight: 700;
  padding: 6px 14px;
  border-radius: 20px;
  display: inline-block;
}

.detail-status-badge.status-nouveau {
  background: #e3f2fd;
  color: #1565c0;
}

.detail-status-badge.status-en\ cours {
  background: #fff3e0;
  color: #e65100;
}

.detail-status-badge.status-termine {
  background: #e8f5e9;
  color: #2e7d32;
}

.detail-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #666;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  min-width: 44px;
  min-height: 44px;
}

.detail-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.detail-row {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 10px 12px;
}

.detail-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 2px;
}

.detail-value {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #111;
}

.detail-photos-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.detail-photos-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 8px;
}

.detail-photos-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 4px;
}

.detail-photo-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  flex-shrink: 0;
  cursor: pointer;
  border: 2px solid #e0e0e0;
}

.detail-photo-thumb:active {
  opacity: 0.7;
}

.detail-photos-loading {
  font-size: 13px;
  color: #999;
  text-align: center;
  padding: 12px;
}

.detail-no-photo {
  font-size: 13px;
  color: #999;
  text-align: center;
  padding: 8px;
}

/* ===== TRANSITIONS ===== */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

<!-- Styles NON scoped pour les éléments Leaflet injectés dans le DOM -->
<style>
.signalement-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px !important;
  height: 52px !important;
  font-size: 30px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  border: 3px solid #6aa84f;
  pointer-events: auto !important;
  touch-action: none;
  z-index: 1000 !important;
}

.signalement-icon span {
  pointer-events: none;
}
</style>