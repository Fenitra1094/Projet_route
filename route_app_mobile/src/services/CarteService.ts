import L, { LatLngBoundsExpression, Map as LeafletMap } from 'leaflet';

const ANTANANARIVO_CENTER: [number, number] = [-18.8792, 47.5079];
const ANTANANARIVO_BOUNDS: LatLngBoundsExpression = [
  [-18.95, 47.45],
  [-18.80, 47.60]
];

export type CarteInstance = {
  map: LeafletMap;
  destroy: () => void;
};

export const initCarte = (containerId: string): CarteInstance => {
  const map = L.map(containerId, {
    zoomControl: true,
    minZoom: 12,
    maxZoom: 18,
    maxBounds: ANTANANARIVO_BOUNDS,
    maxBoundsViscosity: 1.0,
    doubleClickZoom: false
  } as L.MapOptions).setView(ANTANANARIVO_CENTER, 13);

  // Désactiver le tap handler de Leaflet (conflit avec Capacitor/Android)
  (map as any).tap?.disable();

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  map.fitBounds(ANTANANARIVO_BOUNDS);

  return {
    map,
    destroy: () => map.remove()
  };
};
