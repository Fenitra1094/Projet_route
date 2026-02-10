CREATE TABLE IF NOT EXISTS historique_status(
   id_historique_status SERIAL PRIMARY KEY,
   date_ DATE,
   id_signalement INTEGER NOT NULL,
   id_status INTEGER NOT NULL,
   FOREIGN KEY(id_signalement) REFERENCES signalement(id_signalement),
   FOREIGN KEY(id_status) REFERENCES status(id_status)
);
