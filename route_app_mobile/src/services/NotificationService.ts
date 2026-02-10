// src/services/NotificationService.ts
import { LocalNotifications } from '@capacitor/local-notifications';
import { Capacitor } from '@capacitor/core';
import {
  collection,
  query,
  where,
  onSnapshot,
  getDocs
} from 'firebase/firestore';
import { db } from './firebaseService';

// Cache des statuts id -> label
let statusCache: Map<number, string> = new Map();
// Cache des anciens statuts par id_signalement
const previousStatuses: Map<number, number> = new Map();
// Unsubscribe du listener Firestore
let unsubscribe: (() => void) | null = null;
// Flag pour ignorer le premier snapshot (état initial)
let isFirstSnapshot = true;
// Compteur d'IDs pour les notifications
let notifIdCounter = 1;

/**
 * Charger le cache des statuts (id_status -> libelle)
 */
const loadStatusCache = async () => {
  if (statusCache.size > 0) return;
  try {
    const snapshot = await getDocs(collection(db, 'status'));
    const map = new Map<number, string>();
    snapshot.forEach((doc) => {
      const data = doc.data();
      const id = Number(data.id_status);
      const label = data.libelle || '';
      if (Number.isFinite(id) && label) {
        map.set(id, label);
      }
    });
    statusCache = map;
  } catch {
    // Fallback si la collection est vide
  }
};

/**
 * Obtenir le libellé d'un statut
 */
const getStatusLabel = (idStatus: number): string => {
  return statusCache.get(idStatus) || `Statut ${idStatus}`;
};

/**
 * Demander la permission pour les notifications locales
 */
const requestPermission = async () => {
  if (Capacitor.isNativePlatform()) {
    const perm = await LocalNotifications.checkPermissions();
    if (perm.display !== 'granted') {
      await LocalNotifications.requestPermissions();
    }
  }
};

/**
 * Envoyer une notification locale (native) ou une notification Web (navigateur)
 */
const sendNotification = async (title: string, body: string) => {
  const id = notifIdCounter++;

  if (Capacitor.isNativePlatform()) {
    await LocalNotifications.schedule({
      notifications: [
        {
          title,
          body,
          id,
          schedule: { at: new Date(Date.now() + 500) },
          sound: undefined,
          actionTypeId: '',
          extra: null
        }
      ]
    });
  } else {
    // Fallback navigateur : Notification API
    if ('Notification' in window) {
      if (Notification.permission === 'default') {
        await Notification.requestPermission();
      }
      if (Notification.permission === 'granted') {
        new Notification(title, { body, icon: '/favicon.ico' });
      }
    }
    // Aussi déclencher un événement custom pour le toast in-app
    window.dispatchEvent(
      new CustomEvent('signalement-status-changed', {
        detail: { title, body }
      })
    );
  }
};

/**
 * Démarrer l'écoute des changements de statut des signalements de l'utilisateur.
 * Envoie une notification quand le statut d'un signalement change.
 */
export const startSignalementNotificationListener = async (idUser: number) => {
  // Arrêter le listener précédent si existant
  stopSignalementNotificationListener();

  await loadStatusCache();
  await requestPermission();

  isFirstSnapshot = true;
  previousStatuses.clear();

  const q = query(
    collection(db, 'signalement'),
    where('id_user', '==', idUser)
  );

  unsubscribe = onSnapshot(q, (snapshot) => {
    if (isFirstSnapshot) {
      // Premier snapshot : enregistrer l'état initial sans notifier
      snapshot.forEach((doc) => {
        const data = doc.data();
        const idSig = data.id_signalement;
        const idStatus = Number(data.id_status);
        if (idSig != null && Number.isFinite(idStatus)) {
          previousStatuses.set(idSig, idStatus);
        }
      });
      isFirstSnapshot = false;
      return;
    }

    // Snapshots suivants : détecter les changements de statut
    for (const change of snapshot.docChanges()) {
      if (change.type === 'modified') {
        const data = change.doc.data();
        const idSig = data.id_signalement;
        const newStatusId = Number(data.id_status);
        const oldStatusId = previousStatuses.get(idSig);

        if (
          idSig != null &&
          Number.isFinite(newStatusId) &&
          oldStatusId !== undefined &&
          oldStatusId !== newStatusId
        ) {
          const oldLabel = getStatusLabel(oldStatusId);
          const newLabel = getStatusLabel(newStatusId);
          const quartier = data.quartier || 'Inconnu';
          const province = data.province || 'Antananarivo';

          const title = `Signalement mis à jour`;
          const body =
            `Votre signalement à ${quartier} (${province}) ` +
            `est passé de "${oldLabel}" à "${newLabel}".`;

          sendNotification(title, body);

          // Mettre à jour le cache
          previousStatuses.set(idSig, newStatusId);
        }
      }

      // Nouveau signalement : enregistrer le statut initial
      if (change.type === 'added' && !isFirstSnapshot) {
        const data = change.doc.data();
        const idSig = data.id_signalement;
        const idStatus = Number(data.id_status);
        if (idSig != null && Number.isFinite(idStatus)) {
          previousStatuses.set(idSig, idStatus);
        }
      }
    }
  });
};

/**
 * Arrêter l'écoute des notifications
 */
export const stopSignalementNotificationListener = () => {
  if (unsubscribe) {
    unsubscribe();
    unsubscribe = null;
  }
  previousStatuses.clear();
  isFirstSnapshot = true;
};
