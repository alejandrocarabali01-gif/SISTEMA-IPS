-- =====================================================================
-- IPS Unicatolica de la gente - VERSION FINAL (v3: validaciones y IDs de citas por agenda)
-- Tablas: pacientes, medico, cita
-- Motor: MySQL 8.x
-- ATENCION: este script BORRA y vuelve a crear la base de datos.
-- =====================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS ips_unicatolica;
CREATE DATABASE ips_unicatolica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ips_unicatolica;

-- ---------------------------------------------------------------------
-- TABLA PACIENTES (RF01-RF04)  RN01: identificacion unica
-- ---------------------------------------------------------------------
CREATE TABLE pacientes (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    identificacion   VARCHAR(20)  NOT NULL,
    nombre_completo  VARCHAR(100) NOT NULL,
    telefono         VARCHAR(20)  NOT NULL,
    correo           VARCHAR(100) NULL,
    fecha_nacimiento DATE         NULL,
    CONSTRAINT uq_pacientes_identificacion UNIQUE (identificacion),
    -- Validaciones de formato tambien en la base de datos (MySQL 8.0.16 o superior)
    CONSTRAINT ck_pacientes_identificacion CHECK (identificacion REGEXP '^[0-9]{5,16}$'),
    CONSTRAINT ck_pacientes_telefono CHECK (telefono REGEXP '^[0-9]{7,15}$'),
    CONSTRAINT ck_pacientes_nombre CHECK (nombre_completo REGEXP '^\\p{L}+( \\p{L}+)*$'),
    CONSTRAINT ck_pacientes_correo CHECK (correo IS NULL OR correo REGEXP '^[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$')
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLA MEDICO (RF05-RF08)  RN02: identificacion unica
-- ---------------------------------------------------------------------
CREATE TABLE medico (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    identificacion   VARCHAR(20)  NOT NULL,
    nombre_completo  VARCHAR(100) NOT NULL,
    especialidad     VARCHAR(60)  NOT NULL,
    telefono         VARCHAR(20)  NOT NULL,
    correo           VARCHAR(100) NOT NULL,
    CONSTRAINT uq_medico_identificacion UNIQUE (identificacion),
    CONSTRAINT ck_medico_identificacion CHECK (identificacion REGEXP '^[0-9]{5,16}$'),
    CONSTRAINT ck_medico_telefono CHECK (telefono REGEXP '^[0-9]{7,15}$'),
    CONSTRAINT ck_medico_nombre CHECK (nombre_completo REGEXP '^\\p{L}+( \\p{L}+)*$'),
    CONSTRAINT ck_medico_correo CHECK (correo REGEXP '^[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$')
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLA CITA (RF09-RF13)
-- RN03: paciente y medico obligatorios
-- RN04: un medico no puede tener dos citas en la misma fecha y hora
-- RN05: toda cita nueva inicia como PROGRAMADA
-- RN06: una cita CANCELADA no ocupa el horario (hora_activa = NULL)
-- ON UPDATE CASCADE: al renumerar IDs, las citas siguen con la misma persona
-- ON DELETE RESTRICT: no se elimina quien tenga citas asociadas
-- ---------------------------------------------------------------------
CREATE TABLE cita (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente  INT          NOT NULL,
    id_medico    INT          NOT NULL,
    fecha        DATE         NOT NULL,
    hora         TIME         NOT NULL,
    motivo       VARCHAR(200) NOT NULL,
    estado       ENUM('PROGRAMADA','ATENDIDA','CANCELADA')
                 NOT NULL DEFAULT 'PROGRAMADA',
    hora_activa  TIME GENERATED ALWAYS AS
                 (IF(estado = 'CANCELADA', NULL, hora)) VIRTUAL,
    CONSTRAINT fk_cita_paciente FOREIGN KEY (id_paciente)
        REFERENCES pacientes(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cita_medico FOREIGN KEY (id_medico)
        REFERENCES medico(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_cita_medico_fecha_hora
        UNIQUE (id_medico, fecha, hora_activa),
    -- Motivo: solo letras, numeros y puntuacion basica (puede estar vacio)
    CONSTRAINT ck_cita_motivo CHECK (motivo REGEXP '^[\\p{L}\\p{N} .,;:()¿?¡!/%&#+"''-]*$')
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- PROCEDIMIENTOS DE RENUMERACION (IDs consecutivos: 1, 2, 3...)
-- ---------------------------------------------------------------------
DELIMITER $$

CREATE PROCEDURE sp_renumerar_pacientes()
BEGIN
    SET @n = 0;
    UPDATE pacientes SET id = (@n := @n + 1) ORDER BY id;
    SET @sql = CONCAT('ALTER TABLE pacientes AUTO_INCREMENT = ', @n + 1);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END$$

CREATE PROCEDURE sp_renumerar_medicos()
BEGIN
    SET @n = 0;
    UPDATE medico SET id = (@n := @n + 1) ORDER BY id;
    SET @sql = CONCAT('ALTER TABLE medico AUTO_INCREMENT = ', @n + 1);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END$$

-- Los IDs de las citas siguen el orden de la agenda: la cita con fecha y hora mas cercana es la 1.
-- Fase 1 mueve los IDs por encima del maximo para que no choquen entre si (llave primaria);
-- fase 2 los numera de 1 a n por fecha y hora.
CREATE PROCEDURE sp_renumerar_citas()
BEGIN
    DECLARE v_max INT DEFAULT 0;
    SELECT COALESCE(MAX(id), 0) INTO v_max FROM cita;
    UPDATE cita SET id = id + v_max;
    SET @n = 0;
    UPDATE cita SET id = (@n := @n + 1) ORDER BY fecha, hora, id;
    SET @sql = CONCAT('ALTER TABLE cita AUTO_INCREMENT = ', @n + 1);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END$$

-- ---------------------------------------------------------------------
-- PROCEDIMIENTOS DE ELIMINAR (eliminan y renumeran en un solo paso)
-- Si el DELETE falla (ej. tiene citas asociadas), no se renumera.
-- Desde Java:  {CALL sp_eliminar_paciente(?)}
-- ---------------------------------------------------------------------
CREATE PROCEDURE sp_eliminar_paciente(IN p_id INT)
BEGIN
    DELETE FROM pacientes WHERE id = p_id;
    CALL sp_renumerar_pacientes();
END$$

CREATE PROCEDURE sp_eliminar_medico(IN p_id INT)
BEGIN
    DELETE FROM medico WHERE id = p_id;
    CALL sp_renumerar_medicos();
END$$

CREATE PROCEDURE sp_eliminar_cita(IN p_id INT)
BEGIN
    DELETE FROM cita WHERE id = p_id;
    CALL sp_renumerar_citas();
END$$

-- ---------------------------------------------------------------------
-- VALIDACIONES DE FECHAS (la base de datos tambien rechaza datos invalidos)
-- Solo se validan cuando la fecha cambia, para no bloquear la renumeracion de IDs.
-- Para crear datos de prueba con fechas pasadas: SET @ips_pruebas = 1;  (solo en esa sesion)
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_pacientes_bi BEFORE INSERT ON pacientes FOR EACH ROW
BEGIN
    IF NEW.fecha_nacimiento IS NOT NULL AND (NEW.fecha_nacimiento > CURDATE()
       OR NEW.fecha_nacimiento <= DATE_SUB(CURDATE(), INTERVAL 121 YEAR)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fecha de nacimiento no valida: futura o con edad mayor a 120 anios';
    END IF;
END$$

CREATE TRIGGER trg_pacientes_bu BEFORE UPDATE ON pacientes FOR EACH ROW
BEGIN
    IF NOT (NEW.fecha_nacimiento <=> OLD.fecha_nacimiento) AND NEW.fecha_nacimiento IS NOT NULL
       AND (NEW.fecha_nacimiento > CURDATE()
            OR NEW.fecha_nacimiento <= DATE_SUB(CURDATE(), INTERVAL 121 YEAR)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fecha de nacimiento no valida: futura o con edad mayor a 120 anios';
    END IF;
END$$

CREATE TRIGGER trg_cita_bi BEFORE INSERT ON cita FOR EACH ROW
BEGIN
    IF @ips_pruebas IS NULL AND (NEW.fecha < CURDATE() OR NEW.fecha > DATE_ADD(CURDATE(), INTERVAL 1 YEAR)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fecha de cita no valida: debe ser desde hoy hasta un anio hacia adelante';
    END IF;
END$$

CREATE TRIGGER trg_cita_bu BEFORE UPDATE ON cita FOR EACH ROW
BEGIN
    IF @ips_pruebas IS NULL AND NOT (NEW.fecha <=> OLD.fecha)
       AND (NEW.fecha < CURDATE() OR NEW.fecha > DATE_ADD(CURDATE(), INTERVAL 1 YEAR)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fecha de cita no valida: debe ser desde hoy hasta un anio hacia adelante';
    END IF;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------
-- DATOS DE PRUEBA (del mockup)
-- ---------------------------------------------------------------------
INSERT INTO pacientes (identificacion, nombre_completo, telefono, correo, fecha_nacimiento) VALUES
    ('10134751', 'Pepito Perez',  '313540744',  'PP@gmail.com', '1990-05-14'),
    ('11232445', 'Maria Cardelo', '3126544615', 'MC@gmail.com', '1985-09-02');

INSERT INTO medico (identificacion, nombre_completo, especialidad, telefono, correo) VALUES
    ('154589351', 'Alejandro Mazu', 'Pediatria', '3125584212', 'AM@gmail.com');

INSERT INTO cita (id_paciente, id_medico, fecha, hora, motivo) VALUES
    (1, 1, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '09:00:00', 'Chequeo');

-- ---------------------------------------------------------------------
-- CONSULTA: horas OCUPADAS de un medico en una fecha
-- (las CANCELADAS no cuentan). Al modificar una cita, agregar: AND id <> ?
-- ---------------------------------------------------------------------
-- SELECT hora FROM cita
-- WHERE id_medico = ? AND fecha = ? AND estado <> 'CANCELADA';

-- ---------------------------------------------------------------------
-- PRUEBAS RAPIDAS
-- ---------------------------------------------------------------------
-- RN01 (debe fallar):
-- INSERT INTO pacientes (identificacion, nombre_completo, telefono)
--     VALUES ('10134751', 'Otro Paciente', '300000000');
-- RN04 (debe fallar):
-- INSERT INTO cita (id_paciente, id_medico, fecha, hora, motivo)
--     VALUES (2, 1, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '09:00:00', 'Control');
-- Eliminar con renumeracion (falla si el paciente tiene citas):
-- CALL sp_eliminar_paciente(1);
