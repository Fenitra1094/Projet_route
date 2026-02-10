INSERT INTO user_ (email, nom, prenom, id_role, synced) VALUES ('test@example.com', 'Dupont', 'Jean', 1, true);
INSERT INTO province (province) VALUES ('Antananarivo'), ('Toamasina'), ('Fianarantsoa'), ('Mahajanga'), ('Toliara'), ('Antsiranana');
INSERT INTO quartier (quartier, positionX, positionY, id_province) VALUES ('Centre-ville', -18.8792, 47.5079, 1);

-- Insertion des statuts corrects
INSERT INTO status (id_status, libelle) VALUES 
(1, 'en cours'),
(2, 'nouveau'),
(3, 'termine')
ON CONFLICT (id_status) DO NOTHING;

INSERT INTO entreprise (entreprise) VALUES ('Entreprise ABC');

-- Création de la table historique_status si elle n'existe pas
CREATE TABLE IF NOT EXISTS historique_status (
    id_historique_status SERIAL PRIMARY KEY,
    date_ TIMESTAMP NOT NULL,
    id_signalement INTEGER NOT NULL REFERENCES signalement(id_signalement),
    id_status INTEGER NOT NULL REFERENCES status(id_status)
);

INSERT INTO signalement (date_, surface, budget, id_user, id_entreprise, id_quartier, id_status) 
VALUES 
('2026-02-04 14:30:00', 200.75, 75000.00, 3, NULL, 1, 1);

-- Insertion des données de test pour historique_status pour le signalement id=1 (celui qu'on vient d'insérer)
INSERT INTO historique_status (date_, id_signalement, id_status) VALUES
('2026-01-15 09:30:00', 68, 2),  -- Signalement créé avec statut "nouveau"
('2026-01-20 14:15:00', 68, 1),  -- Changement à "en cours" 
('2026-02-05 16:45:00', 68, 3);  -- Finalisation avec "termine"