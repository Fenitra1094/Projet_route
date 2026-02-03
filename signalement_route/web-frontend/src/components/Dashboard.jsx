import React, { useState, useEffect } from 'react';
import { getSignalementRecapitulatif, syncFromFirebase, syncToFirebase, fullSync } from '../api/signalementApi';
import './Dashboard.css';
import './Dashboard.css';

function Dashboard() {
  const [recapData, setRecapData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [syncLoading, setSyncLoading] = useState(false);
  const [syncMessage, setSyncMessage] = useState(null);

  useEffect(() => {
    loadRecapitulatif();
  }, []);

  const loadRecapitulatif = async () => {
    try {
      setLoading(true);
      const data = await getSignalementRecapitulatif();
      setRecapData(data);
      setError(null);
    } catch (err) {
      setError(err.message);
      console.error('Erreur lors du chargement du récapitulatif:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSyncFromFirebase = async () => {
    try {
      setSyncLoading(true);
      setSyncMessage(null);
      const message = await syncFromFirebase();
      setSyncMessage(message);
      await loadRecapitulatif();
    } catch (err) {
      setSyncMessage('Erreur lors de la synchronisation depuis Firebase: ' + err.message);
    } finally {
      setSyncLoading(false);
    }
  };

  const handleSyncToFirebase = async () => {
    try {
      setSyncLoading(true);
      setSyncMessage(null);
      const message = await syncToFirebase();
      setSyncMessage(message);
    } catch (err) {
      setSyncMessage('Erreur lors de la synchronisation vers Firebase: ' + err.message);
    } finally {
      setSyncLoading(false);
    }
  };

  const handleFullSync = async () => {
    try {
      setSyncLoading(true);
      setSyncMessage(null);
      const message = await fullSync();
      setSyncMessage(message);
      await loadRecapitulatif();
    } catch (err) {
      setSyncMessage('Erreur lors de la synchronisation complète: ' + err.message);
    } finally {
      setSyncLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading">Chargement des données...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <div className="error">
          <h2>Erreur de chargement</h2>
          <p>{error}</p>
          <button onClick={loadRecapitulatif} className="retry-btn">
            Réessayer
          </button>
        </div>
      </div>
    );
  }

  if (!recapData) {
    return (
      <div className="dashboard-container">
        <div className="no-data">Aucune donnée disponible</div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <h1>Récapitulatif des Signalements</h1>

      {/* Statistiques générales */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Nombre de points signalés</h3>
          <div className="stat-value">{recapData.nombrePointsSignales || 0}</div>
        </div>

        <div className="stat-card">
          <h3>Surface totale traitée</h3>
          <div className="stat-value">
            {recapData.surfaceTotalTraitee ? `${recapData.surfaceTotalTraitee} m²` : '0 m²'}
          </div>
        </div>

        <div className="stat-card">
          <h3>Taux d'avancement</h3>
          <div className="stat-value">
            {recapData.tauxAvancement ? `${recapData.tauxAvancement.toFixed(1)}%` : '0%'}
          </div>
        </div>

        <div className="stat-card">
          <h3>Budget total alloué</h3>
          <div className="stat-value">
            {recapData.budgetTotalAlloue ?
              `${recapData.budgetTotalAlloue.toLocaleString()} Ar` :
              '0 Ar'
            }
          </div>
        </div>
      </div>

      {/* Détails par arrondissement */}
      <div className="arrondissement-section">
        <h2>Détails par Arrondissement</h2>
        {recapData.detailsParArrondissement && recapData.detailsParArrondissement.length > 0 ? (
          <div className="table-container">
            <table className="arrondissement-table">
              <thead>
                <tr>
                  <th>Arrondissement</th>
                  <th>Points</th>
                  <th>Surface</th>
                  <th>Avancement</th>
                  <th>Budget</th>
                </tr>
              </thead>
              <tbody>
                {recapData.detailsParArrondissement.map((arr, index) => (
                  <tr key={index}>
                    <td className="arrondissement-name">{arr.arrondissement}</td>
                    <td className="points">{arr.points}</td>
                    <td className="surface">
                      {arr.surface ? `${arr.surface} m²` : '0 m²'}
                    </td>
                    <td className="avancement">
                      {arr.avancement ? `${arr.avancement.toFixed(1)}%` : '0%'}
                    </td>
                    <td className="budget">
                      {arr.budget ?
                        `${arr.budget.toLocaleString()} Ar` :
                        '0 Ar'
                      }
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="no-data">Aucun détail par arrondissement disponible</p>
        )}
      </div>

      <div className="actions">
        <button onClick={loadRecapitulatif} className="refresh-btn">
          Actualiser
        </button>
        <button onClick={handleSyncFromFirebase} className="sync-btn" disabled={syncLoading}>
          {syncLoading ? 'Sync depuis Firebase...' : 'Sync depuis Firebase'}
        </button>
        <button onClick={handleSyncToFirebase} className="sync-btn" disabled={syncLoading}>
          {syncLoading ? 'Sync vers Firebase...' : 'Sync vers Firebase'}
        </button>
        <button onClick={handleFullSync} className="sync-btn" disabled={syncLoading}>
          {syncLoading ? 'Sync complète...' : 'Sync complète'}
        </button>
        {syncMessage && <p className="sync-message">{syncMessage}</p>}
        <a href="/users" className="back-link">👥 Voir les Utilisateurs</a>
        <a href="/" className="back-link">Retour à l'accueil</a>
      </div>
    </div>
  );
}

export default Dashboard;