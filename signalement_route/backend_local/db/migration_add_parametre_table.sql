-- Migration: create parametre table for auth settings
CREATE TABLE IF NOT EXISTS parametre (
  id SERIAL PRIMARY KEY,
  duree_session INTEGER,
  nombre_tentative INTEGER
);

-- Optionally insert a default row if none exists
INSERT INTO parametre (duree_session, nombre_tentative)
  SELECT 30, 3
  WHERE NOT EXISTS (SELECT 1 FROM parametre);
