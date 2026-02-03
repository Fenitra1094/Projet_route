INSERT INTO role (libelle) VALUES 
('MANAGER'),
('ADMIN'),
('USER');

INSERT INTO status_blocage (status) VALUES
('Bloqué'),
('Actif');

INSERT INTO user_ (email, password, nom, prenom, id_role, synced, firebase_uid) 
VALUES (
    'test@example.com',
    'Test123!',
    'Dupont',
    'Jean',
    (SELECT id_role FROM role WHERE libelle = 'USER'),
    true,
    'IuWrRfXBywchr1DgKDFobW4pQTQ2' -- À remplacer par un vrai UID
);

INSERT INTO province (province) VALUES 
('Antananarivo'),
('Antsiranana'),
('Fianarantsoa'),
('Mahajanga'),
('Toamasina'),
('Toliara');

-- Insertion des quartiers (exemples pour Antananarivo)
INSERT INTO quartier (quartier, positionX, positionY, id_province) VALUES
('Analakely', 47.5175, -18.9111, (SELECT id_province FROM province WHERE province = 'Antananarivo')),
('Isoraka', 47.5250, -18.9056, (SELECT id_province FROM province WHERE province = 'Antananarivo')),
('Andravoahangy', 47.5300, -18.9000, (SELECT id_province FROM province WHERE province = 'Antananarivo')),
('Anosy', 47.5200, -18.9150, (SELECT id_province FROM province WHERE province = 'Antananarivo')),
('Ambohijatovo', 47.5150, -18.9083, (SELECT id_province FROM province WHERE province = 'Antananarivo'));

INSERT INTO status (libelle) VALUES
('En attente'),
('Validé'),
('Rejeté'),
('En cours'),
('Terminé'),
('Annulé');


INSERT INTO entreprise (entreprise) VALUES
('JIRAMA'),
('SOTEMA'),
('TIA'),
('Communauté Urbaine'),
('Entreprise Privée');


INSERT INTO signalement (date_, surface, budget, Id_user, Id_entreprise, Id_quartier, Id_status) VALUES
('2025-01-18 10:30:00', 120.50, 5000000, 
(SELECT id_user FROM user_ WHERE email = 'test@example.com'),
(SELECT id_entreprise FROM entreprise WHERE entreprise = 'JIRAMA'),
(SELECT id_quartier FROM quartier WHERE quartier = 'Analakely'), 
(SELECT id_status FROM status WHERE libelle = 'En attente'));

