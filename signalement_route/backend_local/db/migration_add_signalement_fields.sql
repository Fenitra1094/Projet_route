-- Migration: Ajouter les nouvelles colonnes à la table signalement
-- Ajout des champs latitude, longitude, firebaseDocId et id_province

ALTER TABLE signalement
  ADD COLUMN IF NOT EXISTS latitude INTEGER,
  ADD COLUMN IF NOT EXISTS longitude INTEGER,
  ADD COLUMN IF NOT EXISTS firebase_doc_id VARCHAR(50),
  ADD COLUMN IF NOT EXISTS id_province INTEGER;

-- Optionnel : Ajouter une clé étrangère vers province
ALTER TABLE signalement
  ADD CONSTRAINT IF NOT EXISTS fk_signalement_province
  FOREIGN KEY (id_province) REFERENCES province(id_province);

-- Mettre à jour les données existantes avec des valeurs par défaut
UPDATE signalement 
SET latitude = CASE 
    WHEN id_quartier = 1 THEN -18908
    WHEN id_quartier = 2 THEN -18912
    WHEN id_quartier = 3 THEN -18905
    ELSE -18900
END,
longitude = CASE 
    WHEN id_quartier = 1 THEN 47498
    WHEN id_quartier = 2 THEN 47523
    WHEN id_quartier = 3 THEN 47549
    ELSE 47500
END,
firebase_doc_id = CONCAT('DOC_', id_signalement, '_firebase'),
id_province = 1
WHERE latitude IS NULL OR longitude IS NULL OR firebase_doc_id IS NULL OR id_province IS NULL;
