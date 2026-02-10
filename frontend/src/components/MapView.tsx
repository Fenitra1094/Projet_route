import { forwardRef, useEffect, useRef, useImperativeHandle } from 'react'
import L from 'leaflet'
import axios from 'axios'

interface MapViewProps {
  onStats: (payload: any) => void
  onOpenPhotos: (id: number) => void
}

interface MapViewHandle {
  flyToSignalement: (id: number) => boolean
}

const MapView = forwardRef<MapViewHandle, MapViewProps>(({ onStats, onOpenPhotos }, ref) => {
  const mapEl = useRef<HTMLDivElement | null>(null)
  const mapInstance = useRef<L.Map | null>(null)
  let interval: number | undefined
  const markersLayer = useRef<L.LayerGroup | null>(null)
  const markersById = useRef<Map<number, L.CircleMarker>>(new Map())

  const formatDateISO = (d: string | undefined) => {
    if (!d) return 'N/A'
    const date = new Date(d)
    return date.toLocaleDateString('fr-FR', { year: 'numeric', month: 'short', day: 'numeric' })
  }

  const loadSignalementsIntoMap = async () => {
    try {
      const resp = await axios.get('/api/signalements')
      const data = resp.data

      // Nettoyer les anciens marqueurs
      if (markersLayer.current) markersLayer.current.clearLayers()

      const features = data
        .filter((s: any) => s.quartier?.positionX && s.quartier?.positionY)
        .map((s: any) => ({
          type: 'Feature',
          properties: {
            id: s.idSignalement,
            status: s.status?.libelle || null,
            date: s.date_,
            surface: s.surface,
            budget: s.budget,
            quartier: s.quartier?.quartier || null,
            entreprise: s.entreprise?.entreprise || null
          },
          geometry: { type: 'Point', coordinates: [s.quartier.positionX, s.quartier.positionY] }
        }))

      // Ajouter les marqueurs
      features.forEach((feature: any) => {
        const p = feature.properties
        const [lng, lat] = feature.geometry.coordinates

        const statusColor = p.status === 'Nouveau' ? '#FF4444' : p.status === 'En cours' ? '#FFAA00' : '#44AA44'
        const statusBg = p.status === 'Nouveau' ? '#ffe8e8' : p.status === 'En cours' ? '#fff4e6' : '#e8f5e9'

        const html = `
          <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; min-width: 260px;">
            <div style="padding: 14px 16px; background: linear-gradient(to right, #667eea 0%, #764ba2 100%); color: white; border-radius: 8px 8px 0 0;">
              <div style="font-weight: 700; font-size: 16px;">📍 ${p.quartier || 'Quartier #' + p.id}</div>
            </div>
            <div style="padding: 12px 16px; background: #fff; border-radius: 0 0 8px 8px;">
              <div style="font-size: 13px; color: #666; margin-bottom: 8px;">
                <span>📅</span> <strong>Date:</strong> ${formatDateISO(p.date)}
              </div>
              <div style="font-size: 13px; color: #666; margin-bottom: 10px;">
                <span>📌</span> <strong>Statut:</strong> 
                <span style="background: ${statusBg}; color: ${statusColor}; padding: 3px 10px; border-radius: 14px; font-weight: 600; font-size: 12px;">${p.status || 'N/A'}</span>
              </div>
              ${p.surface ? '<div style="font-size: 13px; color: #666; margin-bottom: 6px;"><strong>📐 Surface:</strong> ' + p.surface + ' m²</div>' : ''}
              ${p.budget ? '<div style="font-size: 13px; color: #666; margin-bottom: 6px;"><strong>💰 Budget:</strong> ' + p.budget + ' K MGA</div>' : ''}
              ${p.entreprise ? '<div style="font-size: 13px; color: #666; margin-bottom: 10px;"><strong>🏢 Entreprise:</strong> ' + p.entreprise + '</div>' : ''}
              <button onclick="window.photoClick && window.photoClick(${p.id})" style="width: 100%; padding: 11px; background: linear-gradient(to right, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 6px; font-weight: 600; font-size: 13px; cursor: pointer;">📸 Voir les photos</button>
            </div>
          </div>
        `

        const marker = L.circleMarker([lat, lng], {
          radius: 7,
          fillColor: statusColor,
          color: '#fff',
          weight: 2,
          opacity: 0.95,
          fillOpacity: 0.85
        })
          .bindPopup(html, { maxWidth: 300, className: 'signalement-popup' })
          .on('mouseover', function (this: L.CircleMarker) {
            this.setRadius(12)
            this.setStyle({ weight: 3 })
          })
          .on('mouseout', function (this: L.CircleMarker) {
            this.setRadius(7)
            this.setStyle({ weight: 2 })
          })
          .on('click', () => {
            onOpenPhotos(p.id)
          })

        markersLayer.current?.addLayer(marker)
        if (p.id) markersById.current.set(Number(p.id), marker as L.CircleMarker)
      })

      // Rendre la fonction accessible pour le popup
      ;(window as any).photoClick = (id: number) => {
        onOpenPhotos(id)
      }

      onStats({ total: features.length, percent: 0 })
    } catch (err) {
      console.error('❌ Erreur chargement signalements:', err)
    }
  }

  const flyToSignalement = (id: number): boolean => {
    const m = markersById.current.get(id)
    if (!m || !mapInstance.current) return false
    const latlng = m.getLatLng() as L.LatLng
    mapInstance.current.setView([latlng.lat, latlng.lng], 16, { animate: true })
    m.openPopup()
    return true
  }

  useImperativeHandle(ref, () => ({
    flyToSignalement
  }))

  useEffect(() => {
    if (!mapEl.current) return

    console.log('🗺️ Initialisation Leaflet...')

    // Créer la carte (centre Antananarivo: lat, lng)
    const map = L.map(mapEl.current).setView([-18.896542, 47.531662], 13)
    mapInstance.current = map

    // Tenter plusieurs endpoints locaux (PNG) fournis par TileServer.
    // Si aucun endpoint local ne répond correctement, basculer vers OSM en ligne.
    const candidateUrls = [
      `${window.location.origin}/data/antananarivo/{z}/{x}/{y}.png`,
      `${window.location.origin}/styles/antananarivo/{z}/{x}/{y}.png`,
      `${window.location.origin}/data/antananarivo/{z}/{x}/{y}.png?format=png`
    ]

    const errorTile = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=='

    let tryIndex = 0
    function tryNextTile() {
      if (tryIndex >= candidateUrls.length) {
        console.warn('⚠️ Aucun endpoint local PNG disponible — fallback OSM en ligne')
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          minZoom: 8,
          attribution: '© OpenStreetMap contributors'
        }).addTo(map)
        return
      }

      const url = candidateUrls[tryIndex++]
      const tl = L.tileLayer(url, {
        maxZoom: 19,
        minZoom: 8,
        attribution: '© TileServer / MapTiler',
        crossOrigin: 'anonymous',
        errorTileUrl: errorTile
      })

      // Si on reçoit une erreur de tuile, on retire et on essaie le suivant.
      let errored = false
      const onTileError = () => {
        if (errored) return
        errored = true
        tl.off('tileerror' as any, onTileError)
        try { map.removeLayer(tl) } catch (e) {}
        tryNextTile()
      }

      tl.on('tileerror' as any, onTileError)
      tl.addTo(map)
    }

    tryNextTile()

    // Créer le groupe de marqueurs
    const layer = L.layerGroup().addTo(map)
    markersLayer.current = layer

    // Ajouter les contrôles Leaflet
    L.control.zoom({ position: 'topright' }).addTo(map)
    L.control.scale({ metric: true, position: 'bottomleft' }).addTo(map)

    // Charger les signalements
    setTimeout(() => {
      loadSignalementsIntoMap()
      interval = window.setInterval(loadSignalementsIntoMap, 60000)
    }, 300)

    console.log('✅ Carte Leaflet initialisée!')

    return () => {
      if (interval) clearInterval(interval)
      if (mapInstance.current) mapInstance.current.remove()
    }
  }, [])

  return <div ref={mapEl} className="map-root" id="map-container"></div>
})

MapView.displayName = 'MapView'

export default MapView
