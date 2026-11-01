CREATE DATABASE IF NOT EXISTS gest_cab_med
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE gest_cab_med;


-- =========================================
-- 1. TABLE UTILISATEUR
-- =========================================

CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL
);

-- =========================================
-- 2. TABLE PATIENT
-- =========================================

CREATE TABLE IF NOT EXISTS patient (
    id_utilisateur INT PRIMARY KEY,
    date_naissance DATE,
    adresse VARCHAR(255),
    actif BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_patient_utilisateur
    FOREIGN KEY (id_utilisateur)
    REFERENCES utilisateur(id_utilisateur)
    ON DELETE CASCADE
);

-- =========================================
-- 3. TABLE DOCTEUR
-- =========================================

CREATE TABLE IF NOT EXISTS docteur (
    id_utilisateur INT PRIMARY KEY,
    specialite VARCHAR(100) NOT NULL,
    numero_ordre VARCHAR(50) NOT NULL UNIQUE,

    CONSTRAINT fk_docteur_utilisateur
    FOREIGN KEY (id_utilisateur)
    REFERENCES utilisateur(id_utilisateur)
    ON DELETE CASCADE
);

-- =========================================
-- 4. TABLE SECRETAIRE
-- =========================================

CREATE TABLE IF NOT EXISTS secretaire (
    id_utilisateur INT PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,

    CONSTRAINT fk_secretaire_utilisateur
    FOREIGN KEY (id_utilisateur)
    REFERENCES utilisateur(id_utilisateur)
    ON DELETE CASCADE
);


-- =========================================
-- 5. TABLE RENDEZ_VOUS
-- =========================================

CREATE TABLE IF NOT EXISTS rendez_vous (
    id_rdv INT AUTO_INCREMENT PRIMARY KEY,
    date_heure DATETIME NOT NULL,
    motif VARCHAR(255),
    statut ENUM(
        'DEMANDE',
        'CONFIRME',
        'ANNULE',
        'TERMINE'
    ) NOT NULL DEFAULT 'DEMANDE',

    id_patient INT NOT NULL,
    id_docteur INT NOT NULL,

    CONSTRAINT fk_rdv_patient
    FOREIGN KEY (id_patient)
    REFERENCES patient(id_utilisateur),

    CONSTRAINT fk_rdv_docteur
    FOREIGN KEY (id_docteur)
    REFERENCES docteur(id_utilisateur)
);


-- =========================================
-- 6. TABLE CONSULTATION
-- =========================================

CREATE TABLE IF NOT EXISTS consultation (
    id_consultation INT AUTO_INCREMENT PRIMARY KEY,
    date_consultation DATETIME NOT NULL,
    diagnostic TEXT,
    observations TEXT,

    id_rdv INT NOT NULL UNIQUE,

    CONSTRAINT fk_consultation_rdv
    FOREIGN KEY (id_rdv)
    REFERENCES rendez_vous(id_rdv)
);


-- =========================================
-- 7. TABLE MEDICAMENT
-- =========================================

CREATE TABLE IF NOT EXISTS medicament (
    id_medicament INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    dosage VARCHAR(100) NOT NULL
);


-- =========================================
-- 8. TABLE ORDONNANCE
-- =========================================

CREATE TABLE IF NOT EXISTS ordonnance (
    id_ordonnance INT AUTO_INCREMENT PRIMARY KEY,
    date_creation DATE NOT NULL,

    id_consultation INT NOT NULL,

    CONSTRAINT fk_ordonnance_consultation
    FOREIGN KEY (id_consultation)
    REFERENCES consultation(id_consultation)
);


-- =========================================
-- 9. TABLE PRESCRIPTION
-- =========================================

CREATE TABLE IF NOT EXISTS prescription (
    id_prescription INT AUTO_INCREMENT PRIMARY KEY,
    posologie VARCHAR(255) NOT NULL,
    frequence VARCHAR(100) NOT NULL,
    duree VARCHAR(100) NOT NULL,
    quantite INT NOT NULL,

    id_ordonnance INT NOT NULL,
    id_medicament INT NOT NULL,

    CONSTRAINT fk_prescription_ordonnance
    FOREIGN KEY (id_ordonnance)
    REFERENCES ordonnance(id_ordonnance)
    ON DELETE CASCADE,

    CONSTRAINT fk_prescription_medicament
    FOREIGN KEY (id_medicament)
    REFERENCES medicament(id_medicament)
);
