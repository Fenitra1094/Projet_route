import { useState, useEffect, useRef } from 'react'
import axios from 'axios'
import MapView from './components/MapView'
import PhotoModal from './components/PhotoModal'

export default function App() {
  const [total, setTotal] = useState(0)
  const [percent, setPercent] = useState(0)
  const [showPhotos, setShowPhotos] = useState(false)
  const [photoId, setPhotoId] = useState<number | null>(null)
  const [signalements, setSignalements] = useState<any[]>([])
  const [showSidebar, setShowSidebar] = useState(true)
  const mapRef = useRef<any>(null)

  const onStats = (payload: any) => {
    setTotal(payload.total)
    setPercent(payload.percent)
  }

  const onOpenPhotos = (id: number) => {
    setPhotoId(id)
    setShowPhotos(true)
  }

  const onClosePhotos = () => {
    setShowPhotos(false)
    setPhotoId(null)
  }

  const loadList = async () => {
    try {
      const r = await axios.get('/api/signalements')
      setSignalements(r.data)
    } catch (e) {
      console.error('Erreur chargement liste', e)
    }
  }

  const flyTo = (id: number) => {
    if (mapRef.current && mapRef.current.flyToSignalement) {
      mapRef.current.flyToSignalement(id)
    }
  }

  useEffect(() => {
    loadList()
  }, [])

  return (
    <div id="app-container">
      <div className="topbar">
        <div className="topbar-left">
          <h1>🗺️ Signalements Routiers</h1>
          <div className="topbar-subtitle">Antananarivo</div>
        </div>
        <div className="topbar-right">
          <div className="stat-badge">
            <span>📍</span>
            <div>
              <div style={{ fontSize: '16px', lineHeight: '1' }}>
                <strong>{total}</strong>
              </div>
              <div>Problèmes</div>
            </div>
          </div>
          <div className="stat-badge">
            <span>✅</span>
            <div>
              <div style={{ fontSize: '16px', lineHeight: '1' }}>
                <strong>{percent}%</strong>
              </div>
              <div>Résolus</div>
            </div>
          </div>
        </div>
      </div>

      <div className={`sidebar ${!showSidebar ? 'collapsed' : ''}`}>
        <div className="sidebar-header">
          <h3>Liste des signalements</h3>
          <div className="small">Total: {total} — Résolus: {percent}%</div>
        </div>
        <div className="sidebar-body">
          <ul>
            {signalements.map((s) => (
              <li key={s.idSignalement} className="item">
                <div className="item-main" onClick={() => flyTo(s.idSignalement)}>
                  <div className="item-title">
                    {s.quartier?.quartier || 'Quartier #' + s.idSignalement}
                  </div>
                  <div className="item-meta">
                    {new Date(s.date_).toLocaleDateString('fr-FR')} • {s.status?.libelle}
                  </div>
                </div>
                <div className="item-actions">
                  <button onClick={(e) => {
                    e.stopPropagation()
                    onOpenPhotos(s.idSignalement)
                  }}>
                    📸
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <MapView ref={mapRef} onStats={onStats} onOpenPhotos={onOpenPhotos} />

      <PhotoModal id={photoId} visible={showPhotos} onClose={onClosePhotos} />
    </div>
  )
}
