CREATE DATABASE cloud;
use cloud;

CREATE TABLE role(
   Id_role SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(Id_role)
);

CREATE TABLE user_(
   Id_user SERIAL,
   nom VARCHAR(50) ,
   Id_role INTEGER NOT NULL,
   PRIMARY KEY(Id_user),
   FOREIGN KEY(Id_role) REFERENCES role(Id_role)
);

CREATE TABLE province(
   Id_province SERIAL,
   province VARCHAR(50) ,
   PRIMARY KEY(Id_province)
);

CREATE TABLE quartier(
   Id_quartier SERIAL,
   quartier VARCHAR(50) ,
   positionX NUMERIC(15,8)  ,
   positionY NUMERIC(15,8)  ,
   Id_province INTEGER NOT NULL,
   PRIMARY KEY(Id_quartier),
   FOREIGN KEY(Id_province) REFERENCES province(Id_province)
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

CREATE TABLE historique_blocage(
   Id_historique_blocage SERIAL,
   date_ DATE,
   Id_status_blocage INTEGER NOT NULL,
   Id_user INTEGER NOT NULL,
   PRIMARY KEY(Id_historique_blocage),
   FOREIGN KEY(Id_status_blocage) REFERENCES status_blocage(Id_status_blocage),
   FOREIGN KEY(Id_user) REFERENCES user_(Id_user)
);

CREATE TABLE signalement(
   Id_signalement SERIAL,
   date_ TIMESTAMP,
   surface NUMERIC(15,2)  ,
   budget NUMERIC(15,2)  ,
   Id_user INTEGER NOT NULL,
   Id_entreprise INTEGER,
   Id_quartier INTEGER NOT NULL,
   Id_status INTEGER NOT NULL,
   PRIMARY KEY(Id_signalement),
   FOREIGN KEY(Id_user) REFERENCES user_(Id_user),
   FOREIGN KEY(Id_entreprise) REFERENCES entreprise(Id_entreprise),
   FOREIGN KEY(Id_quartier) REFERENCES quartier(Id_quartier),
   FOREIGN KEY(Id_status) REFERENCES status(Id_status)
);
