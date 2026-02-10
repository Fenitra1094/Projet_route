import React, { useState, useEffect } from 'react';
import { getAllUsers, updateUser } from '../api/signalementApi';
import './UserList.css';

function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingUser, setEditingUser] = useState(null);
  const [editForm, setEditForm] = useState({});

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const userList = await getAllUsers();
      setUsers(userList);
      setError(null);
    } catch (err) {
      setError(err.message);
      console.error('Erreur lors du chargement des utilisateurs:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (user) => {
    setEditingUser(user.id_user);
    setEditForm({
      email: user.email || '',
      nom: user.nom || '',
      prenom: user.prenom || '',
      id_role: user.id_role || '',
      firebaseDocId: user.firebaseDocId || '',
      synced: user.synced || false,
    });
  };

  const handleCancelEdit = () => {
    setEditingUser(null);
    setEditForm({});
  };

  const handleSaveEdit = async () => {
    try {
      const updatedUser = await updateUser(editingUser, editForm);
      setUsers(users.map(u => u.id_user === editingUser ? updatedUser : u));
      setEditingUser(null);
      setEditForm({});
    } catch (err) {
      setError(err.message);
      console.error('Erreur lors de la mise à jour:', err);
    }
  };

  const handleFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setEditForm({
      ...editForm,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  if (loading) return <div className="loading">Chargement...</div>;
  if (error) return <div className="error">Erreur: {error}</div>;

  return (
    <div className="user-list">
      <h1>Liste des Utilisateurs</h1>
      {users.length > 0 ? (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Email</th>
                <th>Nom</th>
                <th>Prénom</th>
                <th>Rôle</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id_user}>
                  <td>{user.id_user}</td>
                  {editingUser === user.id_user ? (
                    <>
                      <td><input type="email" name="email" value={editForm.email} onChange={handleFormChange} /></td>
                      <td><input type="text" name="nom" value={editForm.nom} onChange={handleFormChange} /></td>
                      <td><input type="text" name="prenom" value={editForm.prenom} onChange={handleFormChange} /></td>
                      <td><input type="number" name="id_role" value={editForm.id_role} onChange={handleFormChange} /></td>
                      <td>
                        <button onClick={handleSaveEdit}>Sauvegarder</button>
                        <button onClick={handleCancelEdit}>Annuler</button>
                      </td>
                    </>
                  ) : (
                    <>
                      <td>{user.email}</td>
                      <td>{user.nom}</td>
                      <td>{user.prenom}</td>
                      <td>{user.id_role}</td>
                      <td><button onClick={() => handleEdit(user)}>Modifier</button></td>
                    </>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="no-data">Aucun utilisateur trouvé</p>
      )}
      <div className="actions">
        <button onClick={loadUsers} className="refresh-btn">Actualiser</button>
        <a href="/dashboard" className="back-link">Retour au Dashboard</a>
      </div>
    </div>
  );
}

export default UserList;