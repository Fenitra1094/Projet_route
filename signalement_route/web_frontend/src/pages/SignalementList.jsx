import React, { useState, useEffect } from 'react';
import { getAllSignalements, updateSignalement, getAllEntreprises, getAllStatus, updateSignalementStatus, getSignalementsByDateRange } from '../api/signalementApi';
import './SignalementList.css';

function SignalementList() {
    const [signalements, setSignalements] = useState([]);
    const [entreprises, setEntreprises] = useState([]);
    const [statuses, setStatuses] = useState([]);
    const [editingId, setEditingId] = useState(null);
    const [editForm, setEditForm] = useState({
        budget: '',
        surface: '',
        entrepriseId: '',
        statusId: ''
    });
    const [dateDebut, setDateDebut] = useState('');
    const [dateFin, setDateFin] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            let signalementData;
            
            if (dateDebut && dateFin) {
                signalementData = await getSignalementsByDateRange(dateDebut, dateFin);
            } else {
                signalementData = await getAllSignalements();
            }
            
            const [entrepriseData, statusData] = await Promise.all([
                getAllEntreprises(),
                getAllStatus()
            ]);
            setSignalements(signalementData);
            setEntreprises(entrepriseData);
            setStatuses(statusData);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };
    
    const handleFilterByDate = () => {
        if (dateDebut && dateFin) {
            loadData();
        } else {
            alert('Veuillez sélectionner la date de début et fin');
        }
    };
    
    const handleResetFilter = () => {
        setDateDebut('');
        setDateFin('');
    };

    const handleEdit = (signalement) => {
        setEditingId(signalement.idSignalement);
        setEditForm({
            budget: signalement.budget || '',
            surface: signalement.surface || '',
            entrepriseId: signalement.entreprise?.idEntreprise || '',
            statusId: signalement.status?.idStatus || ''
        });
    };

    const handleCancel = () => {
        setEditingId(null);
        setEditForm({ budget: '', surface: '', entrepriseId: '', statusId: '' });
    };

    const handleSave = async (signalement) => {
        try {
            const updateData = {
                budget: editForm.budget ? parseFloat(editForm.budget) : null,
                surface: editForm.surface ? parseFloat(editForm.surface) : null,
                entrepriseId: editForm.entrepriseId ? parseInt(editForm.entrepriseId) : null
            };

            const newStatusId = editForm.statusId ? parseInt(editForm.statusId) : null;
            const currentStatusId = signalement.status?.idStatus || null;
            
            await updateSignalement(signalement.idSignalement, updateData);
            if (newStatusId && newStatusId !== currentStatusId) {
                await updateSignalementStatus(signalement.idSignalement, newStatusId);
            }
            await loadData();
            setEditingId(null);
            setEditForm({ budget: '', surface: '', entrepriseId: '', statusId: '' });
        } catch (err) {
            alert('Erreur lors de la sauvegarde: ' + err.message);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR');
    };

    if (loading) {
        return <div className="signalement-container"><p>Chargement...</p></div>;
    }

    if (error) {
        return <div className="signalement-container"><p className="error">Erreur: {error}</p></div>;
    }

    return (
        <div className="signalement-container">
            <h1>Liste des Signalements</h1>
            
            <div className="filter-section">
                <div className="filter-group">
                    <label>Date de début:</label>
                    <input
                        type="date"
                        value={dateDebut}
                        onChange={(e) => setDateDebut(e.target.value)}
                        className="date-input"
                    />
                </div>
                <div className="filter-group">
                    <label>Date de fin:</label>
                    <input
                        type="date"
                        value={dateFin}
                        onChange={(e) => setDateFin(e.target.value)}
                        className="date-input"
                    />
                </div>
                <button className="btn-filter" onClick={handleFilterByDate}>Filtrer</button>
                <button className="btn-reset" onClick={handleResetFilter}>Réinitialiser</button>
            </div>
            
            <div className="signalement-table-wrapper">
                <table className="signalement-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Date</th>
                            <th>Surface (m²)</th>
                            <th>Budget (Ar)</th>
                            <th>Entreprise</th>
                            <th>Quartier</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {signalements.map((sig) => (
                            <tr key={sig.idSignalement}>
                                <td>{sig.idSignalement}</td>
                                <td>{formatDate(sig.date)}</td>
                                <td>
                                    {editingId === sig.idSignalement ? (
                                        <input
                                            type="number"
                                            step="0.01"
                                            value={editForm.surface}
                                            onChange={(e) => setEditForm({...editForm, surface: e.target.value})}
                                            className="edit-input"
                                        />
                                    ) : (
                                        sig.surface || 'N/A'
                                    )}
                                </td>
                                <td>
                                    {editingId === sig.idSignalement ? (
                                        <input
                                            type="number"
                                            step="0.01"
                                            value={editForm.budget}
                                            onChange={(e) => setEditForm({...editForm, budget: e.target.value})}
                                            className="edit-input"
                                        />
                                    ) : (
                                        sig.budget ? sig.budget.toLocaleString('fr-FR') : 'N/A'
                                    )}
                                </td>
                                <td>
                                    {editingId === sig.idSignalement ? (
                                        <select
                                            value={editForm.entrepriseId}
                                            onChange={(e) => setEditForm({...editForm, entrepriseId: e.target.value})}
                                            className="edit-select"
                                        >
                                            <option value="">-- Sélectionner --</option>
                                            {entreprises.map(ent => (
                                                <option key={ent.idEntreprise} value={ent.idEntreprise}>
                                                    {ent.entreprise}
                                                </option>
                                            ))}
                                        </select>
                                    ) : (
                                        sig.entreprise?.entreprise || 'N/A'
                                    )}
                                </td>
                                <td>{sig.quartier?.quartier || 'N/A'}</td>
                                <td>
                                    {editingId === sig.idSignalement ? (
                                        <select
                                            value={editForm.statusId}
                                            onChange={(e) => setEditForm({...editForm, statusId: e.target.value})}
                                            className="edit-select"
                                        >
                                            <option value="">-- Sélectionner --</option>
                                            {statuses.map(status => (
                                                <option key={status.idStatus} value={status.idStatus}>
                                                    {status.libelle}
                                                </option>
                                            ))}
                                        </select>
                                    ) : (
                                        <span className={`status-badge status-${sig.status?.libelle?.toLowerCase()}`}>
                                            {sig.status?.libelle || 'N/A'}
                                        </span>
                                    )}
                                </td>
                                <td>
                                    {editingId === sig.idSignalement ? (
                                        <div className="action-buttons">
                                            <button 
                                                className="btn-save"
                                                onClick={() => handleSave(sig)}
                                            >
                                                Sauvegarder
                                            </button>
                                            <button 
                                                className="btn-cancel"
                                                onClick={handleCancel}
                                            >
                                                Annuler
                                            </button>
                                        </div>
                                    ) : (
                                        <button 
                                            className="btn-edit"
                                            onClick={() => handleEdit(sig)}
                                        >
                                            Modifier
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default SignalementList;
