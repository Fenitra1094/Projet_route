INSERT INTO user_ (email, nom, prenom, id_role, synced) VALUES ('test@example.com', 'Dupont', 'Jean', 1, true);
INSERT INTO province (province) VALUES ('Antananarivo'), ('Toamasina'), ('Fianarantsoa'), ('Mahajanga'), ('Toliara'), ('Antsiranana');
INSERT INTO quartier (quartier, positionX, positionY, id_province) VALUES ('Centre-ville', -18.8792, 47.5079, 1);
INSERT INTO status (libelle) VALUES ('En attente');
INSERT INTO entreprise (entreprise) VALUES ('Entreprise ABC');

INSERT INTO signalement (date_, surface, budget, id_user, id_entreprise, id_quartier, id_status) 
VALUES 
('2026-02-04 14:30:00', 200.75, 75000.00, 3, NULL, 1, 1);