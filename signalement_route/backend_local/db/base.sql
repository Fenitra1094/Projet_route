INSERT INTO entreprise (entreprise)
VALUES
('Colas Madagascar'),
('Razel-Bec'),
('SMATP');

INSERT INTO status (libelle)
VALUES
('nouveau'),
('en cours'),
('termine');

INSERT INTO quartier (positionx, positiony, quartier, id_province)
VALUES
(-18.8792, 47.5079, 'Analakely', 1),
(-18.9100, 47.5250, 'Anosy', 1),
(-18.9005, 47.5230, 'Isoraka', 1),
(-18.8790, 47.5200, 'Antaninarenina', 1);

INSERT INTO province (province)
VALUES
('Antananarivo'),
('Toamasina');

INSERT INTO signalement
(budget, date_, surface, id_entreprise, id_quartier, id_status, id_user)
VALUES
(5000000, CURRENT_DATE, 120, 1, 1, 1, 1),
(12000000, CURRENT_DATE, 300, 2, 2, 2, 2);

