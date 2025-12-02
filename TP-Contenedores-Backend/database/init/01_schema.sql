-- 2) GEOLOCALIZACION
CREATE TABLE geolocalizacion (
                                 id_geo              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 direccion           VARCHAR,
                                 latitud             REAL NOT NULL,
                                 longitud            REAL NOT NULL,
                                 CONSTRAINT geoloc_lat_chk CHECK (latitud BETWEEN -90 AND 90),
                                 CONSTRAINT geoloc_lon_chk CHECK (longitud BETWEEN -180 AND 180)
);

-- 3) ESTADOS
CREATE TABLE estados (
                         id_estado           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         contexto            VARCHAR NOT NULL,
                         descripcion         VARCHAR,
                         CONSTRAINT chk_estados_contexto CHECK (contexto IN ('CONTENEDOR','SOLICITUD','TRAMO'))
);

-- 4) CLIENTES
CREATE TABLE clientes (
                          dni_cliente         INTEGER PRIMARY KEY,
                          nombre              VARCHAR NOT NULL,
                          apellido            VARCHAR NOT NULL,
                          telefono            VARCHAR
);

-- 5) TRANSPORTISTAS
CREATE TABLE transportistas (
                                id_transportista    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                nombre              VARCHAR NOT NULL,
                                apellido            VARCHAR NOT NULL,
                                telefono            VARCHAR,
                                email               VARCHAR,
                                dni                 VARCHAR,
                                fecha_nacimiento    DATE,
                                activo              BOOLEAN NOT NULL DEFAULT TRUE
);

-- 6) CAMIONES
CREATE TABLE camiones (
                          dominio_camion          VARCHAR PRIMARY KEY,
                          id_transportista        INTEGER,
                          id_geo                  INTEGER,
                          capacidad_peso_max      REAL NOT NULL,
                          capacidad_volumen_max   REAL NOT NULL,
                          disponibilidad          BOOLEAN DEFAULT TRUE,
                          consumo_prom_km         REAL NOT NULL,
                          costo_traslado          REAL NOT NULL,
                          CONSTRAINT fk_camion_transportista
                              FOREIGN KEY (id_transportista) REFERENCES transportistas(id_transportista)
                                  ON UPDATE CASCADE ON DELETE RESTRICT,
                          CONSTRAINT fk_camion_geo
                              FOREIGN KEY (id_geo) REFERENCES geolocalizacion(id_geo)
                                  ON UPDATE CASCADE ON DELETE SET NULL
);

-- 7) DEPOSITOS
CREATE TABLE depositos (
                           id_deposito             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           nombre                  VARCHAR NOT NULL,
                           id_geo      INTEGER NOT NULL,
                           costo_estadia_diaria    REAL NOT NULL,
                           CONSTRAINT fk_deposito_geo
                               FOREIGN KEY (id_geo) REFERENCES geolocalizacion(id_geo)
                                   ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 8) TIPO_TRAMO
CREATE TABLE tipo_tramo (
                            id_tipo_tramo   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            nombre_tipo     VARCHAR NOT NULL
);

-- =========================================================
-- Entidades operativas
-- =========================================================

CREATE TABLE contenedores (
                              id_contenedor       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              id_estado           INTEGER NOT NULL,
                              id_cliente          INTEGER NOT NULL,
                              peso_kg             REAL NOT NULL,
                              volumen_m3          REAL NOT NULL,
                              costo_base_km       REAL,
                              CONSTRAINT fk_cont_estado
                                  FOREIGN KEY (id_estado)  REFERENCES estados(id_estado)
                                      ON UPDATE CASCADE ON DELETE RESTRICT,
                              CONSTRAINT fk_cont_cliente
                                  FOREIGN KEY (id_cliente) REFERENCES clientes(dni_cliente)
                                      ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE solicitudes (
                             nro_solicitud       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             id_contenedor       INTEGER NOT NULL,
                             dni_cliente          INTEGER NOT NULL,
                             id_estado           INTEGER NOT NULL,
                             costo_estimado      REAL,
                             tiempo_estimado     INTEGER,
                             costo_real          REAL,
                             tiempo_real         INTEGER,
                             fecha_creacion      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                             CONSTRAINT fk_sol_contenedor
                                 FOREIGN KEY (id_contenedor) REFERENCES contenedores(id_contenedor)
                                     ON UPDATE CASCADE ON DELETE RESTRICT,
                             CONSTRAINT fk_sol_cliente
                                 FOREIGN KEY (dni_cliente)    REFERENCES clientes(dni_cliente)
                                     ON UPDATE CASCADE ON DELETE RESTRICT,
                             CONSTRAINT fk_sol_estado
                                 FOREIGN KEY (id_estado)     REFERENCES estados(id_estado)
                                     ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE rutas (
                       id_ruta             INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       nro_solicitud       INTEGER,
                       cantidad_tramos     INTEGER DEFAULT 0,
                       cantidad_depositos  INTEGER DEFAULT 0,
                       CONSTRAINT fk_ruta_solicitud
                           FOREIGN KEY (nro_solicitud) REFERENCES solicitudes(nro_solicitud)
                               ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE tramos (
                        id_tramo                        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        id_ruta                         INTEGER NOT NULL,
                        origen_geo                      INTEGER,
                        destino_geo                     INTEGER,
                        origen_deposito_id              INTEGER,
                        destino_deposito_id             INTEGER,
                        tipo_tramo                      INTEGER NOT NULL,
                        id_estado                       INTEGER NOT NULL,
                        orden                           INTEGER NOT NULL,
                        fechahora_inicio_estimada       TIMESTAMP WITHOUT TIME ZONE,
                        fechahora_fin_estimada          TIMESTAMP WITHOUT TIME ZONE,
                        fechahora_inicio_real           TIMESTAMP WITHOUT TIME ZONE,
                        fechahora_fin_real              TIMESTAMP WITHOUT TIME ZONE,
                        costo_aproximado                REAL,
                        costo_real                      REAL,
                        dominio_camion                  VARCHAR,
                        CONSTRAINT fk_tramo_ruta
                            FOREIGN KEY (id_ruta) REFERENCES rutas(id_ruta)
                                ON UPDATE CASCADE ON DELETE RESTRICT,
                        CONSTRAINT fk_tramo_origen_geo
                            FOREIGN KEY (origen_geo) REFERENCES geolocalizacion(id_geo)
                                ON UPDATE CASCADE ON DELETE RESTRICT,
                        CONSTRAINT fk_tramo_destino_geo
                            FOREIGN KEY (destino_geo) REFERENCES geolocalizacion(id_geo)
                                ON UPDATE CASCADE ON DELETE RESTRICT,
                        CONSTRAINT fk_tramo_ori_dep
                            FOREIGN KEY (origen_deposito_id) REFERENCES depositos(id_deposito)
                                ON UPDATE CASCADE ON DELETE SET NULL,
                        CONSTRAINT fk_tramo_des_dep
                            FOREIGN KEY (destino_deposito_id) REFERENCES depositos(id_deposito)
                                ON UPDATE CASCADE ON DELETE SET NULL,
                        CONSTRAINT fk_tramo_tipo
                            FOREIGN KEY (tipo_tramo) REFERENCES tipo_tramo(id_tipo_tramo)
                                ON UPDATE CASCADE ON DELETE RESTRICT,
                        CONSTRAINT fk_tramo_estado
                            FOREIGN KEY (id_estado) REFERENCES estados(id_estado)
                                ON UPDATE CASCADE ON DELETE RESTRICT,
                        CONSTRAINT fk_tramo_camion
                            FOREIGN KEY (dominio_camion) REFERENCES camiones(dominio_camion)
                                ON UPDATE CASCADE ON DELETE SET NULL,
                        CONSTRAINT chk_tramo_orden CHECK (orden >= 1)
);

CREATE TABLE tarifas (
                         id_tarifa               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         dominio_camion          VARCHAR NOT NULL,
                         tipo_tarifa             VARCHAR NOT NULL,
                         costo_litro_combustible REAL,
                         cargo_gestion_tramo     REAL,
                         CONSTRAINT fk_tarifa_camion
                             FOREIGN KEY (dominio_camion) REFERENCES camiones(dominio_camion)
                                 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX idx_contenedores_estado     ON contenedores (id_estado);
CREATE INDEX idx_contenedores_cliente    ON contenedores (id_cliente);

CREATE INDEX idx_solicitudes_estado      ON solicitudes (id_estado);
CREATE INDEX idx_solicitudes_contenedor  ON solicitudes (id_contenedor);
CREATE INDEX idx_solicitudes_cliente    ON solicitudes (dni_cliente);
CREATE INDEX idx_solicitudes_creacion    ON solicitudes (fecha_creacion DESC);

CREATE INDEX idx_rutas_solicitud         ON rutas (nro_solicitud);

CREATE INDEX idx_tramos_ruta             ON tramos (id_ruta, orden);
CREATE INDEX idx_tramos_estado           ON tramos (id_estado);
CREATE INDEX idx_tramos_camion           ON tramos (dominio_camion);
CREATE INDEX idx_tramos_origen_geo       ON tramos (origen_geo);
CREATE INDEX idx_tramos_destino_geo      ON tramos (destino_geo);

CREATE INDEX idx_depositos_geo           ON depositos (id_geo);

CREATE INDEX idx_camiones_transportista  ON camiones (id_transportista);
CREATE INDEX idx_camiones_geo            ON camiones (id_geo);