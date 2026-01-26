INSERT INTO role (libelle) VALUES
('manager'),
('admin');

INSERT INTO province (province) VALUES
('Antananarivo');

INSERT INTO quartier (quartier, positionX, positionY, Id_province) VALUES
('Analakely', -18.90870000, 47.52660000, 1),
('Anosy', -18.92120000, 47.51580000, 1),
('Ivandry', -18.86950000, 47.54530000, 1);


INSERT INTO status (libelle) VALUES
('En attente'),
('Validé'),
('Rejeté');


INSERT INTO entreprise (entreprise) VALUES
('Socobis'),
('Jirama'),
('Telma');


INSERT INTO user_ (nom, Id_role) VALUES
('Rakoto', 1), -- manager
('Rabe', 2),   -- admin
('Soa', 1);    -- manager


INSERT INTO status_blocage (status) VALUES
('Bloqué'),
('Débloqué');

INSERT INTO signalement (date_, surface, budget, Id_user, Id_entreprise, Id_quartier, Id_status) VALUES
('2025-01-18 10:30:00', 120.50, 5000000, 1, 1, 1, 1),
('2025-01-19 14:00:00', 80.00, 3000000, 3, 2, 2, 2),
('2025-01-20 09:15:00', 200.75, 8000000, 1, NULL, 3, 1);

