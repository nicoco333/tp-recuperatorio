-- CLIENTES
INSERT INTO clientes (dni_cliente, nombre, apellido, telefono) VALUES
                                                                   (20123456, 'María',  'González', '11-4567-8901'),
                                                                   (23987654, 'Juan',   'Pérez',    '11-2345-6789'),
                                                                   (30111222, 'Lucía',  'Fernández','341-555-1234'),
                                                                   (27876543, 'Carlos', 'Romero',   '351-444-7890');

-- ESTADOS
INSERT INTO estados (contexto, descripcion) VALUES
                                                ('CONTENEDOR', 'DISPONIBLE'),
                                                ('SOLICITUD',  'CREADA'),
                                                ('TRAMO',      'PENDIENTE');

-- TRANSPORTISTAS
INSERT INTO transportistas (nombre, apellido, telefono, email, dni, fecha_nacimiento, activo) VALUES
                                                                                                  ('Sofía',    'Martínez',  '11-4890-1122', 'sofia.martinez@transporte.com',  '30111222', '1989-05-14', TRUE),
                                                                                                  ('Matías',   'López',     '351-445-7788', 'matias.lopez@transporte.com',    '27888999', '1985-11-02', TRUE),
                                                                                                  ('Valentina','Suárez',    '341-556-9033', 'valentina.suarez@transporte.com', '33222111', '1992-03-27', TRUE),
                                                                                                  ('Diego',    'Pereyra',   '221-477-6655', 'diego.pereyra@transporte.com',   '28999000', '1987-09-18', FALSE);

-- GEOLOCALIZACION
INSERT INTO geolocalizacion (direccion, latitud, longitud) VALUES
                                                               ('Av. Corrientes 1234, CABA',   -34.6037, -58.3816),
                                                               ('Bv. San Juan 750, Córdoba',   -31.4167, -64.1833);

-- CONTENEDORES
INSERT INTO contenedores (id_estado, id_cliente, peso_kg, volumen_m3, costo_base_km) VALUES
                                                                                         (1, 20123456, 12000, 33.5, 1.80),
                                                                                         (1, 23987654,  8000, 22.0, 1.60),
                                                                                         (1, 30111222, 15000, 40.0, 2.00),
                                                                                         (1, 27876543,  5000, 12.0, 1.40);

-- DEPOSITOS
INSERT INTO depositos (nombre, id_geo, costo_estadia_diaria) VALUES
                                                                 ('Depósito CABA - Centro',    1, 150.0),
                                                                 ('Depósito Córdoba - Centro', 2, 120.0),
                                                                 ('Depósito CABA - Norte',     1, 140.0),
                                                                 ('Depósito Córdoba - Sur',    2, 110.0);

-- TIPO_TRAMO
INSERT INTO tipo_tramo (nombre_tipo) VALUES
                                         ('ORIGEN - DEPOSITO'),
                                         ('DEPÓSITO - DESTINO'),
                                         ('DEPOSITO - DEPOSITO'),
                                         ('ORIGEN - DESTINO'),
                                         ('TRASLADO'),
                                         ('DEPOSITO');

-- CONTENEDORES (3 nuevos)
INSERT INTO contenedores (id_estado, id_cliente, peso_kg, volumen_m3, costo_base_km) VALUES
                                                                                         (1, 20123456, 10000, 28.0, 1.70),
                                                                                         (1, 23987654,  7000, 18.5, 1.55),
                                                                                         (1, 27876543, 13000, 36.0, 1.95);

-- CAMIONES
INSERT INTO camiones (dominio_camion, id_transportista, id_geo, capacidad_peso_max, capacidad_volumen_max, disponibilidad, consumo_prom_km, costo_traslado) VALUES
                                                                                                                                                                ('AJ345KL', 1, 2, 29000, 62.0,  TRUE, 0.33,  97.0),
                                                                                                                                                                ('AL678MN', 2, 1, 31000, 70.0,  TRUE, 0.36, 108.0),
                                                                                                                                                                ('AP901QR', 3, 2, 26000, 58.0, FALSE, 0.31,  90.0),
                                                                                                                                                                ('AR234ST', 4, 1, 30000, 66.0,  TRUE, 0.34, 103.0);

-- SOLICITUDES
INSERT INTO solicitudes (id_contenedor, dni_cliente, id_estado, costo_estimado, tiempo_estimado)
VALUES
    (
        (SELECT id_contenedor FROM contenedores WHERE id_cliente = 20123456 ORDER BY id_contenedor LIMIT 1),
    20123456,
    (SELECT id_estado FROM estados WHERE contexto = 'SOLICITUD' AND descripcion = 'CREADA' LIMIT 1),
    125000.0, 48
    ),
  (
    (SELECT id_contenedor FROM contenedores WHERE id_cliente = 23987654 ORDER BY id_contenedor LIMIT 1),
    23987654,
    (SELECT id_estado FROM estados WHERE contexto = 'SOLICITUD' AND descripcion = 'CREADA' LIMIT 1),
    98000.0, 36
  ),
  (
    (SELECT id_contenedor FROM contenedores WHERE id_cliente = 27876543 ORDER BY id_contenedor LIMIT 1),
    27876543,
    (SELECT id_estado FROM estados WHERE contexto = 'SOLICITUD' AND descripcion = 'CREADA' LIMIT 1),
    143500.0, 60
  );

-- RUTAS
INSERT INTO rutas (nro_solicitud, cantidad_tramos, cantidad_depositos) VALUES
                                                                           (
                                                                               (SELECT nro_solicitud FROM solicitudes WHERE dni_cliente = 20123456 ORDER BY nro_solicitud LIMIT 1),
    3, 1
    ),
  (
    (SELECT nro_solicitud FROM solicitudes WHERE dni_cliente = 23987654 ORDER BY nro_solicitud LIMIT 1),
    2, 0
  ),
  (
    (SELECT nro_solicitud FROM solicitudes WHERE dni_cliente = 27876543 ORDER BY nro_solicitud LIMIT 1),
    4, 2
  );

-- TRAMOS
-- 1) Ruta de cliente 20123456 - tramo 1
INSERT INTO tramos (
    id_ruta, origen_geo, destino_deposito_id, tipo_tramo, id_estado, orden,
    costo_aproximado, dominio_camion
) VALUES (
             (SELECT r.id_ruta
              FROM rutas r
                       JOIN solicitudes s ON s.nro_solicitud = r.nro_solicitud
              WHERE s.dni_cliente = 20123456
              ORDER BY r.id_ruta LIMIT 1),
         (SELECT id_geo FROM geolocalizacion WHERE direccion LIKE 'Av. Corrientes 1234%' LIMIT 1),
         (SELECT id_deposito FROM depositos WHERE nombre = 'Depósito CABA - Centro' LIMIT 1),
         (SELECT id_tipo_tramo FROM tipo_tramo WHERE nombre_tipo = 'TRASLADO' LIMIT 1),
         (SELECT id_estado FROM estados WHERE contexto = 'TRAMO' AND descripcion = 'PENDIENTE' LIMIT 1),
    1,
    25000.0,
    'AJ345KL'
    );

-- 2) Ruta de cliente 20123456 - tramo 2
INSERT INTO tramos (
    id_ruta, origen_deposito_id, destino_geo, tipo_tramo, id_estado, orden,
    costo_aproximado, dominio_camion
) VALUES (
             (SELECT r.id_ruta
              FROM rutas r
                       JOIN solicitudes s ON s.nro_solicitud = r.nro_solicitud
              WHERE s.dni_cliente = 20123456
              ORDER BY r.id_ruta LIMIT 1),
         (SELECT id_deposito FROM depositos WHERE nombre = 'Depósito CABA - Centro' LIMIT 1),
         (SELECT id_geo FROM geolocalizacion WHERE direccion LIKE 'Bv. San Juan 750%' LIMIT 1),
         (SELECT id_tipo_tramo FROM tipo_tramo WHERE nombre_tipo = 'TRASLADO' LIMIT 1),
         (SELECT id_estado FROM estados WHERE contexto = 'TRAMO' AND descripcion = 'PENDIENTE' LIMIT 1),
    2,
    47000.0,
    'AL678MN'
    );

-- 3) Ruta de cliente 23987654 - tramo único
INSERT INTO tramos (
    id_ruta, origen_geo, destino_geo, tipo_tramo, id_estado, orden,
    costo_aproximado, dominio_camion
) VALUES (
             (SELECT r.id_ruta
              FROM rutas r
                       JOIN solicitudes s ON s.nro_solicitud = r.nro_solicitud
              WHERE s.dni_cliente = 23987654
              ORDER BY r.id_ruta LIMIT 1),
         (SELECT id_geo FROM geolocalizacion WHERE direccion LIKE 'Av. Corrientes 1234%' LIMIT 1),
         (SELECT id_geo FROM geolocalizacion WHERE direccion LIKE 'Bv. San Juan 750%' LIMIT 1),
         (SELECT id_tipo_tramo FROM tipo_tramo WHERE nombre_tipo = 'TRASLADO' LIMIT 1),
         (SELECT id_estado FROM estados WHERE contexto = 'TRAMO' AND descripcion = 'PENDIENTE' LIMIT 1),
    1,
    52000.0,
    'AP901QR'
    );

-- 4) Ruta de cliente 27876543 - tramo 1
INSERT INTO tramos (
    id_ruta, origen_deposito_id, destino_deposito_id, tipo_tramo, id_estado, orden,
    costo_aproximado, dominio_camion
) VALUES (
             (SELECT r.id_ruta
              FROM rutas r
                       JOIN solicitudes s ON s.nro_solicitud = r.nro_solicitud
              WHERE s.dni_cliente = 27876543
              ORDER BY r.id_ruta LIMIT 1),
         (SELECT id_deposito FROM depositos WHERE nombre = 'Depósito Córdoba - Centro' LIMIT 1),
         (SELECT id_deposito FROM depositos WHERE nombre = 'Depósito Córdoba - Sur' LIMIT 1),
         (SELECT id_tipo_tramo FROM tipo_tramo WHERE nombre_tipo = 'DEPOSITO' LIMIT 1),
         (SELECT id_estado FROM estados WHERE contexto = 'TRAMO' AND descripcion = 'PENDIENTE' LIMIT 1),
    1,
    8000.0,
    'AR234ST'
    );

-- TARIFAS
INSERT INTO tarifas (dominio_camion, tipo_tarifa, costo_litro_combustible, cargo_gestion_tramo) VALUES
                                                                                                    ('AJ345KL', 'BASE',      950.0, 2500.0),
                                                                                                    ('AL678MN', 'NOCTURNO', 1000.0, 3200.0),
                                                                                                    ('AP901QR', 'URBANO',    980.0, 2800.0);