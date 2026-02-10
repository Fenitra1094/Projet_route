import React, { useEffect, useState } from 'react';
import { saveSettings, getSettings } from '../api/settingsApi';

function Settings() {
  const [duree, setDuree] = useState(30);
  const [attempts, setAttempts] = useState(3);
  const [status, setStatus] = useState('');

  useEffect(() => {
    getSettings().then(arr => {
      if (Array.isArray(arr) && arr.length > 0) {
        const p = arr[0];
        setDuree(p.dureeSession ?? p.duree_session ?? 30);
        setAttempts(p.nombreTentative ?? p.nombre_tentative ?? 3);
      }
    }).catch(() => {});
  }, []);

  const onSubmit = async (e) => {
    e.preventDefault();
    setStatus('');
    try {
      const saved = await saveSettings(duree, attempts);
      setStatus('Paramètres enregistrés');
    } catch (err) {
      console.error('Save settings failed', err);
      setStatus('Erreur: ' + (err.message || JSON.stringify(err)));
    }
  };

  return (
    <div style={{maxWidth:480, margin:'2rem auto'}}>
      <h2>Paramètres d'authentification</h2>
      <form onSubmit={onSubmit}>
        <div style={{marginBottom:12}}>
          <label>Durée de session (minutes)</label>
          <input type="number" min="1" value={duree} onChange={e=>setDuree(e.target.value)} style={{width:'100%', padding:8, marginTop:6}} />
        </div>
        <div style={{marginBottom:12}}>
          <label>Nombre de tentatives</label>
          <input type="number" min="1" value={attempts} onChange={e=>setAttempts(e.target.value)} style={{width:'100%', padding:8, marginTop:6}} />
        </div>
        <button type="submit" style={{padding:'8px 12px'}}>Enregistrer</button>
      </form>
      {status && <p style={{marginTop:12}}>{status}</p>}
    </div>
  );
}

export default Settings;
