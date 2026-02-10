-- Nouvelles données pour la table signalement avec les nouveaux champs
-- Ces données incluent : latitude, longitude, firebaseDocId, et quartier

-- données additionnelles pour les entités de référence (si nécessaire)
INSERT INTO role (libelle) VALUES
('superviseur'),
('technicien');

INSERT INTO province (province) VALUES
('Toliara'),
('Fianarantsoa'),
('Mahajanga');

INSERT INTO quartier (quartier, positionX, positionY, Id_province) VALUES
('Ambatobe', -18.85420000, 47.49210000, 1),
('Andohatapenaka', -18.91230000, 47.53450000, 1),
('Belafy', -19.02120000, 47.61580000, 2),
('Antalaha', -20.25000000, 50.15000000, 3);

INSERT INTO status (libelle) VALUES
('Nouveau'),
('Assigné'),
('En inspection'),
('Reparé');

INSERT INTO entreprise (entreprise) VALUES
('Merlin'),
('COMMAT'),
('Sadef');

INSERT INTO user_ (nom, prenom, email, Id_role) VALUES
('Jean Dupont', 'Jean', 'jean.dupont@example.com', 1),      -- superviseur
('Marie Claire', 'Marie', 'marie.claire@example.com', 2),    -- technicien
('Pierre Martin', 'Pierre', 'pierre.martin@example.com', 1), -- superviseur
('Sophie Dubois', 'Sophie', 'sophie.dubois@example.com', 2); -- technicien

INSERT INTO status_blocage (status) VALUES
('Bloqué temporaire'),
('Débloqué définitif');

-- Nouvelles données pour signalement avec TOUS les champs requis
INSERT INTO signalement (date_, surface, budget, longitude, latitude, firebase_doc_id, quartier, id_province, id_user, id_entreprise, id_status) VALUES
('2025-02-01 08:00:00', 150.75, 6500000, 47498, -18908, 'DOC_001_firebase', 'Ambatobe', 1, 1, 1, 1);
('2025-02-02 10:30:00', 95.50, 4200000, 47523, -18912, 'DOC_002_firebase', 'Analakely', 1, 2, 2, 2),
('2025-02-03 14:15:00', 220.00, 9000000, 47615, -19021, 'DOC_003_firebase', 'Belafy', 2, 3, 1, 3),
('2025-02-04 11:00:00', 180.30, 7500000, 50150, -20250, 'DOC_004_firebase', 'Antalaha', 3, 1, 2, 3),
('2025-02-05 09:45:00', 125.60, 5100000, 47549, -18905, 'DOC_005_firebase', 'Andohatapenaka', 1, 1, 2, 1),
('2025-02-06 13:20:00', 250.80, 10500000, 47512, -18920, 'DOC_006_firebase', 'Anosy', 1, 2, 2, 2),
('2025-02-07 15:00:00', 160.45, 6800000, 47534, -18895, 'DOC_007_firebase', 'Ivandry', 1, 3, 3, 3);

-- Ajout des historiques de statut pour tester les relations
INSERT INTO historique_status (date_, id_signalement, id_status) VALUES
('2025-02-01', 1, 1),
('2025-02-02', 1, 2),
('2025-02-03', 2, 1),
('2025-02-04', 2, 3),
('2025-02-05', 3, 1),
('2025-02-06', 3, 2),
('2025-02-07', 3, 4);

-- Ajout des historiques de blocage pour les utilisateurs
INSERT INTO historique_blocage (date_, Id_status_blocage, Id_user) VALUES
('2025-02-01', 1, 1),
('2025-02-02', 2, 2),
('2025-02-03', 1, 3),
('2025-02-04', 2, 4);
