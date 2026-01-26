-- Connexion à la base postgres (base par défaut)
\c postgres

-- Création ou recréation de la base de données
DROP DATABASE IF EXISTS projetcloud;
CREATE DATABASE projetcloud;

-- Connexion à la nouvelle base
\c projetcloud

-- =========================
-- Table des rôles
-- =========================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50) UNIQUE NOT NULL -- Ex : UTILISATEUR / MANAGER
);

-- =========================
-- Table des actions par rôle (permissions)
-- =========================
CREATE TABLE action_role (
    id SERIAL PRIMARY KEY,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL
);

-- =========================
-- Table des entreprises
-- =========================
CREATE TABLE entreprises (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) UNIQUE NOT NULL,
    adresse VARCHAR(255),
    contact VARCHAR(100)
);

-- =========================
-- Table des utilisateurs
-- =========================
CREATE TABLE utilisateurs (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL, -- Hashé avec BCrypt
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    role_id INT REFERENCES roles(id) ON DELETE SET NULL,
    tentatives_echouees INT DEFAULT 0,
    compte_bloque BOOLEAN DEFAULT FALSE,
    date_blocage TIMESTAMP,
    date_creation TIMESTAMP DEFAULT NOW()
);

-- =========================
-- Table des signalements
-- =========================
CREATE TABLE signalements (
    id SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    statut INTEGER DEFAULT 1, -- 1: Nouveau, 11/21: En cours, 99: Terminé, 0: Effacé
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    surface_m2 DOUBLE PRECISION,
    budget DOUBLE PRECISION,
    id_entreprise INT REFERENCES entreprises(id) ON DELETE SET NULL,
    id_utilisateur INT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    date_creation TIMESTAMP DEFAULT NOW()
);

-- =========================
-- Historique des statuts
-- =========================
CREATE TABLE signalement_historique (
    id SERIAL PRIMARY KEY,
    id_signalement INT NOT NULL REFERENCES signalements(id) ON DELETE CASCADE,
    ancien_statut INTEGER NOT NULL,
    nouveau_statut INTEGER NOT NULL,
    date_changement TIMESTAMP DEFAULT NOW(),
    id_utilisateur INT REFERENCES utilisateurs(id)
);

-- =========================
-- Table des sessions (offline / JWT)
-- =========================
CREATE TABLE sessions (
    id SERIAL PRIMARY KEY,
    id_utilisateur INT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    token_jwt VARCHAR(512) NOT NULL,
    expiration TIMESTAMP NOT NULL,
    date_creation TIMESTAMP DEFAULT NOW()
);