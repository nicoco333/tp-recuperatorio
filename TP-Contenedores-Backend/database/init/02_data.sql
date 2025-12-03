-- =========================================================
-- DATOS INICIALES DEL SISTEMA (SEED MANUAL CORREGIDO)
-- =========================================================

-- 1. LIMPIEZA TOTAL
TRUNCATE TABLE 
    tarifas, tramos, rutas, solicitudes, contenedores, depositos, 
    camiones, transportistas, geolocalizacion, clientes, tipo_tramo, estados 
RESTART IDENTITY CASCADE;

-- 2. ESTADOS
INSERT INTO estados (contexto, descripcion) VALUES
('CONTENEDOR', 'DISPONIBLE'),  -- ID 1
('SOLICITUD',  'CREADA'),      -- ID 2
('TRAMO',      'PENDIENTE'),   -- ID 3
('SOLICITUD',  'EN CURSO'),    -- ID 4
('SOLICITUD',  'FINALIZADA'),  -- ID 5
('CONTENEDOR', 'OCUPADO'),     -- ID 6
('TRAMO',      'EN CURSO'),    -- ID 7
('TRAMO',      'FINALIZADO');  -- ID 8

-- 3. CLIENTES
INSERT INTO clientes (dni_cliente, nombre, apellido, telefono) VALUES
(20123456, 'María',  'González', '11-4567-8901'),
(23987654, 'Juan',   'Pérez',    '11-2345-6789'),
(30111222, 'Lucía',  'Fernández','341-555-1234'),
(27876543, 'Carlos', 'Romero',   '351-444-7890'),
-- CLIENTE DE PRUEBA
(11223344, 'Roberto', 'Gómez',   '341-111-2222'), 
(55667788, 'Ana',     'Ruiz',    '261-333-4444');

-- 4. TRANSPORTISTAS
INSERT INTO transportistas (nombre, apellido, telefono, email, dni, fecha_nacimiento, activo) VALUES
('Sofía',    'Martínez',  '11-4890-1122', 'sofia.martinez@transporte.com',  '30111222', '1989-05-14', TRUE),
('Matías',   'López',     '351-445-7788', 'matias.lopez@transporte.com',    '27888999', '1985-11-02', TRUE),
('Valentina','Suárez',    '341-556-9033', 'valentina.suarez@transporte.com', '33222111', '1992-03-27', TRUE),
('Diego',    'Pereyra',   '221-477-6655', 'diego.pereyra@transporte.com',   '28999000', '1987-09-18', FALSE);

-- 5. GEOLOCALIZACION
INSERT INTO geolocalizacion (direccion, latitud, longitud) VALUES
('Av. Corrientes 1234, CABA',                    -34.6037, -58.3816), -- ID 1
('Bv. San Juan 750, Córdoba',                    -31.4167, -64.1833), -- ID 2
('Av. Pellegrini 1500, Rosario, Santa Fe',       -32.9545, -60.6550), -- ID 3
('Av. Colón 2000, Mar del Plata, Buenos Aires',  -38.0004, -57.5562), -- ID 4
('Av. San Martín 500, Mendoza Capital',          -32.8908, -68.8272), -- ID 5
('Calle Falsa 123, Lanús, Buenos Aires',         -34.7053, -58.3917); -- ID 6

-- 6. DEPOSITOS
INSERT INTO depositos (nombre, id_geo, costo_estadia_diaria) VALUES
('Depósito CABA - Centro',         1, 150.0), -- ID 1
('Depósito Córdoba - Centro',      2, 120.0), -- ID 2
('Depósito CABA - Norte',          1, 140.0), -- ID 3
('Depósito Córdoba - Sur',         2, 110.0), -- ID 4
('Depósito Rosario - Puerto',      3, 130.0), -- ID 5
('Depósito Mar del Plata - Puerto',4, 145.0); -- ID 6

-- 7. TIPO_TRAMO
INSERT INTO tipo_tramo (nombre_tipo) VALUES
('ORIGEN - DEPOSITO'),   -- ID 1
('DEPÓSITO - DESTINO'),  -- ID 2
('DEPOSITO - DEPOSITO'), -- ID 3
('ORIGEN - DESTINO'),    -- ID 4
('TRASLADO'),            -- ID 5
('DEPOSITO');            -- ID 6

-- 8. CONTENEDORES
INSERT INTO contenedores (id_estado, id_cliente, peso_kg, volumen_m3, costo_base_km) VALUES
(1, 20123456, 12000, 33.5, 1.80),
(1, 23987654,  8000, 22.0, 1.60),
(1, 30111222, 15000, 40.0, 2.00),
(1, 27876543,  5000, 12.0, 1.40),
(1, 20123456, 10000, 28.0, 1.70),
(1, 23987654,  7000, 18.5, 1.55),
(1, 27876543, 13000, 36.0, 1.95),
-- CONTENEDOR DE PRUEBA
(1, 11223344, 20000, 45.0, 2.10), -- ID 8
(1, 55667788, 5000,  10.0, 1.30); -- ID 9

-- 9. CAMIONES
INSERT INTO camiones (dominio_camion, id_transportista, id_geo, capacidad_peso_max, capacidad_volumen_max, disponibilidad, consumo_prom_km, costo_traslado) VALUES
('AJ345KL', 1, 2, 29000, 62.0,  TRUE, 0.33,  97.0),
('AL678MN', 2, 1, 31000, 70.0,  TRUE, 0.36, 108.0),
('AP901QR', 3, 2, 26000, 58.0, FALSE, 0.31,  90.0),
('AR234ST', 4, 1, 30000, 66.0,  TRUE, 0.34, 103.0),
-- Camión de prueba
('ZZ999ZZ', 1, 3, 40000, 80.0,  TRUE, 0.45, 120.0);

-- 10. TARIFAS
INSERT INTO tarifas (dominio_camion, tipo_tarifa, costo_litro_combustible, cargo_gestion_tramo) VALUES
('AJ345KL', 'BASE',            950.0, 2500.0),
('AL678MN', 'NOCTURNO',       1000.0, 3200.0),
('AP901QR', 'URBANO',          980.0, 2800.0),
('ZZ999ZZ', 'LARGA_DISTANCIA',1100.0, 5000.0);

-- 11. SOLICITUDES DE EJEMPLO
-- CORRECCION: Agregamos NOW() explícitamente en la fecha
INSERT INTO solicitudes (id_contenedor, dni_cliente, id_estado, costo_estimado, tiempo_estimado, fecha_creacion)
VALUES
(1, 20123456, 2, 125000.0, 48, NOW()), -- ID 1
(2, 23987654, 2, 98000.0, 36, NOW());  -- ID 2

-- 12. RUTAS DE EJEMPLO
INSERT INTO rutas (nro_solicitud, cantidad_tramos, cantidad_depositos) VALUES
(1, 1, 0),
(2, 1, 0);

-- 13. TRAMOS DE EJEMPLO
INSERT INTO tramos (id_ruta, origen_geo, destino_deposito_id, tipo_tramo, id_estado, orden, costo_aproximado, dominio_camion)
VALUES (1, 1, 1, 1, 3, 1, 25000.0, 'AJ345KL');