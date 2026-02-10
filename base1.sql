CREATE TABLE role(
   Id_role SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(Id_role)
);

CREATE TABLE province(
   Id_province SERIAL,
   province VARCHAR(50) ,
   PRIMARY KEY(Id_province)
);

CREATE TABLE status(
   Id_status SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(Id_status)
);

CREATE TABLE entreprise(
   Id_entreprise SERIAL,
   entreprise VARCHAR(50) ,
   PRIMARY KEY(Id_entreprise)
);

CREATE TABLE status_blocage(
   Id_status_blocage SERIAL,
   status VARCHAR(50) ,
   PRIMARY KEY(Id_status_blocage)
);

CREATE TABLE avancement(
   Id_avancement SERIAL,
   libelle VARCHAR(50) ,
   pourcentage INTEGER,
   PRIMARY KEY(Id_avancement)
);

CREATE TABLE Parametre(
   Id_Parametre SERIAL,
   session_vie INTEGER,
   nombre_tentation INTEGER,
   PRIMARY KEY(Id_Parametre)
);

CREATE TABLE user_(
   Id_user SERIAL,
   nom VARCHAR(50) ,
   firebase_uid VARCHAR(50) ,
   Id_status_blocage INTEGER NOT NULL,
   Id_role INTEGER NOT NULL,
   PRIMARY KEY(Id_user),
   FOREIGN KEY(Id_status_blocage) REFERENCES status_blocage(Id_status_blocage),
   FOREIGN KEY(Id_role) REFERENCES role(Id_role)
);

CREATE TABLE signalement(
   Id_signalement SERIAL,
   date_ TIMESTAMP,
   surface NUMERIC(15,2)  ,
   budget NUMERIC(15,2)  ,
   longitude INTEGER,
   latitude INTEGER,
   firebase_doc_id VARCHAR(50) ,
   quartier VARCHAR(50) ,
   Id_province INTEGER NOT NULL,
   Id_user INTEGER NOT NULL,
   Id_entreprise INTEGER,
   Id_status INTEGER NOT NULL,
   PRIMARY KEY(Id_signalement),
   FOREIGN KEY(Id_province) REFERENCES province(Id_province),
   FOREIGN KEY(Id_user) REFERENCES user_(Id_user),
   FOREIGN KEY(Id_entreprise) REFERENCES entreprise(Id_entreprise),
   FOREIGN KEY(Id_status) REFERENCES status(Id_status)
);

CREATE TABLE historique_blocage(
   Id_historique_blocage SERIAL,
   date_ DATE,
   Id_status_blocage INTEGER NOT NULL,
   Id_user INTEGER NOT NULL,
   PRIMARY KEY(Id_historique_blocage),
   FOREIGN KEY(Id_status_blocage) REFERENCES status_blocage(Id_status_blocage),
   FOREIGN KEY(Id_user) REFERENCES user_(Id_user)
);

CREATE TABLE photo_signalement(
   Id_photo_signalement SERIAL,
   libelle VARCHAR(50) ,
   Id_signalement INTEGER,
   PRIMARY KEY(Id_photo_signalement),
   FOREIGN KEY(Id_signalement) REFERENCES signalement(Id_signalement)
);

CREATE TABLE historique_avancement(
   Id_signalement INTEGER,
   Id_avancement INTEGER,
   date_ DATE,
   PRIMARY KEY(Id_signalement, Id_avancement),
   FOREIGN KEY(Id_signalement) REFERENCES signalement(Id_signalement),
   FOREIGN KEY(Id_avancement) REFERENCES avancement(Id_avancement)
);
