<template>
  <ion-page>
    <ion-header class="custom-header" mode="md">
      <ion-toolbar class="custom-toolbar">
        <ion-buttons slot="start">
          <ion-back-button default-href="/map" text="" class="back-btn" />
        </ion-buttons>
        <ion-title class="header-title">Récapitulatif</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="recap-content" :fullscreen="true">
      <div class="recap-container">
        <!-- En-tête compact -->
        <div class="recap-header">
          <div class="header-icon-wrapper">
            <ion-icon :icon="barChartOutline" class="header-icon-svg" />
          </div>
          <h1>Vue d'ensemble</h1>
          <p class="subtitle">Statistiques des signalements</p>
        </div>

        <!-- Cartes de statistiques (grille 2x2 mobile) -->
        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-emoji">📍</span>
            <div class="stat-content">
              <div class="stat-value">--</div>
              <div class="stat-label">Total</div>
            </div>
          </div>

          <div class="stat-card">
            <span class="stat-emoji">🆕</span>
            <div class="stat-content">
              <div class="stat-value">--</div>
              <div class="stat-label">Nouveaux</div>
            </div>
          </div>

          <div class="stat-card">
            <span class="stat-emoji">⚠️</span>
            <div class="stat-content">
              <div class="stat-value">--</div>
              <div class="stat-label">En cours</div>
            </div>
          </div>

          <div class="stat-card">
            <span class="stat-emoji">✅</span>
            <div class="stat-content">
              <div class="stat-value">--</div>
              <div class="stat-label">Terminés</div>
            </div>
          </div>
        </div>

        <!-- Message de construction -->
        <div class="construction-box">
          <div class="construction-emoji">🚧</div>
          <h2>En développement</h2>
          <p>
            Le récapitulatif détaillé est en cours de construction.
            Bientôt disponible : graphiques, tableaux et statistiques.
          </p>
          <div class="features-list">
            <div class="feature-item">
              <ion-icon :icon="trendingUpOutline" class="feature-icon-svg" />
              <span>Graphiques d'évolution</span>
            </div>
            <div class="feature-item">
              <ion-icon :icon="gridOutline" class="feature-icon-svg" />
              <span>Tableaux interactifs</span>
            </div>
            <div class="feature-item">
              <ion-icon :icon="downloadOutline" class="feature-icon-svg" />
              <span>Export des données</span>
            </div>
            <div class="feature-item">
              <ion-icon :icon="documentTextOutline" class="feature-icon-svg" />
              <span>Rapports personnalisés</span>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonPage,
  IonTitle,
  IonToolbar
} from '@ionic/vue';
import { 
  barChartOutline, trendingUpOutline, gridOutline, 
  downloadOutline, documentTextOutline 
} from 'ionicons/icons';
</script>

<style scoped>
/* ===== HEADER ===== */
.custom-header {
  --background: #6aa84f;
}

.custom-toolbar {
  --background: #6aa84f;
  --color: white;
  --min-height: 56px;
}

.header-title {
  font-weight: 700;
  font-size: 18px;
}

.back-btn {
  --color: white;
  min-width: 48px;
  min-height: 48px;
}

/* ===== CONTENU ===== */
.recap-content {
  --background: #f5f1e8;
}

.recap-container {
  padding: 20px 16px;
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0px));
}

/* ===== EN-TÊTE ===== */
.recap-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6aa84f, #4a8630);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 6px 20px rgba(106, 168, 79, 0.3);
}

.header-icon-svg {
  font-size: 28px;
  color: white;
}

.recap-header h1 {
  font-size: 24px;
  font-weight: 800;
  color: #111111;
  margin: 0;
  letter-spacing: -0.3px;
}

.subtitle {
  font-size: 14px;
  color: #333333;
  margin-top: 4px;
}

/* ===== GRILLE STATS (2x2 mobile) ===== */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  min-height: 72px;
  -webkit-tap-highlight-color: transparent;
  transition: transform 0.15s ease;
}

.stat-card:active {
  transform: scale(0.97);
}

.stat-emoji {
  font-size: 28px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #111111;
  line-height: 1.1;
}

.stat-label {
  font-size: 12px;
  color: #444444;
  margin-top: 2px;
  font-weight: 500;
}

/* ===== CONSTRUCTION BOX ===== */
.construction-box {
  background: white;
  border-radius: 20px;
  padding: 28px 20px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.construction-emoji {
  font-size: 48px;
  margin-bottom: 8px;
}

.construction-box h2 {
  font-size: 20px;
  font-weight: 700;
  color: #111111;
  margin: 0 0 8px 0;
}

.construction-box p {
  font-size: 14px;
  color: #333333;
  line-height: 1.5;
  margin: 0 auto 20px auto;
}

/* ===== LISTE FONCTIONNALITÉS ===== */
.features-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #f8f9fa;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  color: #111111;
  text-align: left;
  min-height: 48px;
}

.feature-icon-svg {
  color: #6aa84f;
  font-size: 22px;
  flex-shrink: 0;
}
</style>