import { useState, useEffect } from 'react'

interface PhotoModalProps {
  id: number | null
  visible: boolean
  onClose: () => void
}

export default function PhotoModal({ id, visible, onClose }: PhotoModalProps) {
  const [photos, setPhotos] = useState<string[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const loadPhotos = async () => {
      setPhotos([])
      if (!id) return
      setLoading(true)
      try {
        // Endpoint to implement on backend if you have photos
        // const resp = await axios.get(`/api/signalements/${id}/photos`)
        // setPhotos(resp.data)
        // placeholder
        setPhotos(['/libs/placeholder-photo.png'])
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    loadPhotos()
  }, [id])

  if (!visible) return null

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <header>
          <h3>Photos - Signalement #{id}</h3>
          <button onClick={onClose}>✖</button>
        </header>
        <section className="content">
          {loading ? (
            <p>Chargement des photos...</p>
          ) : (
            <div>
              {photos.length === 0 ? (
                <div>Aucune photo disponible</div>
              ) : (
                <div className="gallery">
                  {photos.map((p, i) => (
                    <img key={i} src={p} alt={`Photo ${i + 1}`} />
                  ))}
                </div>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
