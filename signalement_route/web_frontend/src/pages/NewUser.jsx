import React, { useState } from 'react';
import { register } from '../api/authApi';
import './NewUser.css';

function NewUser() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [nom, setNom] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (password !== confirmPassword) {
            setError('Les mots de passe ne correspondent pas');
            return;
        }

        if (password.length < 6) {
            setError('Le mot de passe doit contenir au moins 6 caractères');
            return;
        }

        try {
            await register({
                email,
                password,
                nom,
                id_role: 1
            });
            setSuccess('Utilisateur créé avec succès !');
            setEmail('');
            setPassword('');
            setConfirmPassword('');
            setNom('');
            setTimeout(() => window.location.pathname = '/signalements', 2000);
        } catch (err) {
            const errorMessage = err.message || 'Erreur lors de la création de l\'utilisateur';
            setError(errorMessage);
        }
    };

    return (
        <div className="newuser-container">
            <div className="newuser-card">
                <h2>Créer un Nouvel Utilisateur</h2>

                <form className="newuser-form" onSubmit={handleSubmit}>
                    <input
                        className="newuser-input"
                        type="text"
                        placeholder="Nom complet"
                        value={nom}
                        onChange={(e) => setNom(e.target.value)}
                        required
                    />

                    <input
                        className="newuser-input"
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />

                    <input
                        className="newuser-input"
                        type="password"
                        placeholder="Mot de passe"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />

                    <input
                        className="newuser-input"
                        type="password"
                        placeholder="Confirmer le mot de passe"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                    />

                    <button className="newuser-button" type="submit">
                        Créer l'utilisateur
                    </button>
                </form>

                {error && <p className="newuser-error">{error}</p>}
                {success && <p className="newuser-success">{success}</p>}

              
            </div>
        </div>
    );
}

export default NewUser;
