-- Migration: add status-related tables and update user_ table
-- Creates new tables if they don't already exist and updates user_ to reference status_blocage

CREATE TABLE IF NOT EXISTS role(
   Id_role SERIAL PRIMARY KEY,
   libelle VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS province(
   Id_province SERIAL PRIMARY KEY,
   province VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS quartier(
   Id_quartier SERIAL PRIMARY KEY,
   quartier VARCHAR(50),
   positionX NUMERIC(15,8),
   positionY NUMERIC(15,8),
   Id_province INTEGER NOT NULL,
   FOREIGN KEY(Id_province) REFERENCES province(Id_province)
);

CREATE TABLE IF NOT EXISTS status(
   Id_status SERIAL PRIMARY KEY,
   libelle VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS entreprise(
   Id_entreprise SERIAL PRIMARY KEY,
   entreprise VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS status_blocage(
   Id_status_blocage SERIAL PRIMARY KEY,
   status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS avancement(
   Id_avancement SERIAL PRIMARY KEY,
   libelle VARCHAR(50),
   pourcentage INTEGER
);

-- Update user_ table: drop boolean "bloquer" if present, add Id_status_blocage reference
ALTER TABLE user_
  DROP COLUMN IF EXISTS bloquer;

ALTER TABLE user_
  ADD COLUMN IF NOT EXISTS Id_status_blocage INTEGER;

-- Populate status_blocage with default values and set existing users to 'actif' (1)
INSERT INTO status_blocage (Id_status_blocage, status)
   SELECT 1, 'actif' WHERE NOT EXISTS (SELECT 1 FROM status_blocage WHERE Id_status_blocage = 1);
INSERT INTO status_blocage (Id_status_blocage, status)
   SELECT 2, 'bloque' WHERE NOT EXISTS (SELECT 1 FROM status_blocage WHERE Id_status_blocage = 2);

-- Set existing users to 'actif' by default if not set
UPDATE user_ SET Id_status_blocage = 1 WHERE Id_status_blocage IS NULL;

-- Make column NOT NULL now that defaults are set
ALTER TABLE user_ ALTER COLUMN Id_status_blocage SET NOT NULL;

ALTER TABLE user_
   ADD CONSTRAINT IF NOT EXISTS fk_user_status_blocage FOREIGN KEY (Id_status_blocage) REFERENCES status_blocage(Id_status_blocage);

-- Create signalement and related tables
CREATE TABLE IF NOT EXISTS signalement(
   Id_signalement SERIAL PRIMARY KEY,
   date_ TIMESTAMP,
   surface NUMERIC(15,2),
   budget NUMERIC(15,2),
   Id_user INTEGER NOT NULL,
   Id_entreprise INTEGER,
   Id_quartier INTEGER NOT NULL,
   Id_status INTEGER NOT NULL,
   FOREIGN KEY(Id_user) REFERENCES user_(Id_user),
   FOREIGN KEY(Id_entreprise) REFERENCES entreprise(Id_entreprise),
   FOREIGN KEY(Id_quartier) REFERENCES quartier(Id_quartier),
   FOREIGN KEY(Id_status) REFERENCES status(Id_status)
);

CREATE TABLE IF NOT EXISTS historique_blocage(
   Id_historique_blocage SERIAL PRIMARY KEY,
   date_ DATE,
   Id_status_blocage INTEGER NOT NULL,
   Id_user INTEGER NOT NULL,
   FOREIGN KEY(Id_status_blocage) REFERENCES status_blocage(Id_status_blocage),
   FOREIGN KEY(Id_user) REFERENCES user_(Id_user)
);

CREATE TABLE IF NOT EXISTS photo_signalement(
   Id_photo_signalement SERIAL PRIMARY KEY,
   libelle VARCHAR(50),
   Id_signalement INTEGER,
   FOREIGN KEY(Id_signalement) REFERENCES signalement(Id_signalement)
);

CREATE TABLE IF NOT EXISTS historique_avancement(
   Id_signalement INTEGER,
   Id_avancement INTEGER,
   date_ DATE,
   PRIMARY KEY(Id_signalement, Id_avancement),
   FOREIGN KEY(Id_signalement) REFERENCES signalement(Id_signalement),
   FOREIGN KEY(Id_avancement) REFERENCES avancement(Id_avancement)
);
