BEGIN;

-- Limpieza completa de datos funcionales. Se preserva exclusivamente el administrador indicado.
DELETE FROM notification;
DELETE FROM password_reset_token;
DELETE FROM pay;
DELETE FROM invoice_line;
DELETE FROM invoice;
DELETE FROM settlement;
DELETE FROM contract_adjustment;
DELETE FROM contract_event;
DELETE FROM contract;
DELETE FROM property_image;
DELETE FROM property_amenity;
DELETE FROM property_operation_type;
DELETE FROM property;
DELETE FROM customer;
DELETE FROM company;
DELETE FROM system_setting;
DELETE FROM app_user WHERE lower(email) <> 'jere.gunsett@gmail.com';

-- Clientes: propietarios e inquilinos.
INSERT INTO customer (name, surname, document_type, document_number, cuit, birthdate, email, phone, creation_date, modification_date) VALUES
  ('Martina', 'Rossi', 'DNI', '28765432', '27-28765432-8', '1981-04-12', 'martina.rossi@demo.com', '+54 11 4555-0192', now(), now()),
  ('Diego', 'Fernández', 'DNI', '25321987', '20-25321987-3', '1977-09-03', 'diego.fernandez@demo.com', '+54 11 4777-3841', now(), now()),
  ('Lucía', 'Alonso', 'DNI', '30124567', '27-30124567-5', '1984-01-26', 'lucia.alonso@demo.com', '+54 11 4891-6620', now(), now()),
  ('Santiago', 'Méndez', 'DNI', '33456789', '20-33456789-1', '1988-06-18', 'santiago.mendez@demo.com', '+54 11 4214-9100', now(), now()),
  ('Valentina', 'Suárez', 'DNI', '35678901', '27-35678901-4', '1991-11-09', 'valentina.suarez@demo.com', '+54 11 4508-2203', now(), now()),
  ('Nicolás', 'Paz', 'DNI', '32123456', '20-32123456-7', '1986-02-21', 'nicolas.paz@demo.com', '+54 11 4345-7812', now(), now()),
  ('Carolina', 'Vega', 'DNI', '34234567', '27-34234567-0', '1989-07-14', 'carolina.vega@demo.com', '+54 11 4982-0618', now(), now()),
  ('Tomás', 'Giménez', 'DNI', '31876543', '20-31876543-5', '1985-12-02', 'tomas.gimenez@demo.com', '+54 11 4621-4009', now(), now());

-- Propiedades con una imagen de portada para cada ficha.
INSERT INTO property (name, owner_id, property_type, status, sale_price, rent_price, street, numeration, floor, department, zip_code, city, province, country, bathrooms, bedrooms, furnished, construction_year, total_area, covered_area, creation_date, modification_date)
SELECT 'Departamento luminoso en Palermo', id, 'APARTMENT', 'AVAILABLE', 235000, NULL::numeric, 'Honduras', '5860', '7', 'B', '1414', 'Ciudad de Buenos Aires', 'Buenos Aires', 'Argentina', 2, 2, true, 2018, 86, 74, now(), now() FROM customer WHERE email = 'martina.rossi@demo.com'
UNION ALL SELECT 'Casa con jardín en San Isidro', id, 'HOUSE', 'AVAILABLE', 420000, NULL::numeric, 'Don Bosco', '1250', NULL, NULL, '1642', 'San Isidro', 'Buenos Aires', 'Argentina', 3, 4, false, 2005, 310, 220, now(), now() FROM customer WHERE email = 'diego.fernandez@demo.com'
UNION ALL SELECT 'Monoambiente moderno en Belgrano', id, 'APARTMENT', 'RENTED', NULL, 620000, 'Amenábar', '2650', '4', 'A', '1428', 'Ciudad de Buenos Aires', 'Buenos Aires', 'Argentina', 1, 0, true, 2021, 39, 35, now(), now() FROM customer WHERE email = 'lucia.alonso@demo.com'
UNION ALL SELECT 'PH reciclado en Villa Crespo', id, 'CONDO', 'AVAILABLE', NULL, 950000, 'Araoz', '930', NULL, NULL, '1414', 'Ciudad de Buenos Aires', 'Buenos Aires', 'Argentina', 2, 3, false, 1998, 128, 112, now(), now() FROM customer WHERE email = 'santiago.mendez@demo.com'
UNION ALL SELECT 'Oficina premium en Microcentro', id, 'OFFICE', 'AVAILABLE', NULL, 1300000, 'San Martín', '540', '10', '1008', '1004', 'Ciudad de Buenos Aires', 'Buenos Aires', 'Argentina', 2, 0, false, 2015, 145, 145, now(), now() FROM customer WHERE email = 'valentina.suarez@demo.com'
UNION ALL SELECT 'Lote arbolado en Pilar', id, 'LOT', 'RESERVED', 98000, NULL, 'Las Acacias', '0', NULL, NULL, '1629', 'Pilar', 'Buenos Aires', 'Argentina', NULL, NULL, false, NULL, 820, NULL, now(), now() FROM customer WHERE email = 'martina.rossi@demo.com';

INSERT INTO property_operation_type (property_id, operation_type)
SELECT id, CASE status WHEN 'RENTED' THEN 'RENT' WHEN 'AVAILABLE' THEN CASE WHEN sale_price IS NOT NULL THEN 'SALE' ELSE 'RENT' END WHEN 'RESERVED' THEN 'SALE' END FROM property;

INSERT INTO property_amenity (property_id, amenity)
SELECT p.id, a.amenity FROM property p JOIN (VALUES
  ('Departamento luminoso en Palermo', 'BALCONY'), ('Departamento luminoso en Palermo', 'SWIMMING_POOL'), ('Departamento luminoso en Palermo', 'GYM'), ('Departamento luminoso en Palermo', 'SECURITY'),
  ('Casa con jardín en San Isidro', 'GARDEN'), ('Casa con jardín en San Isidro', 'SWIMMING_POOL'), ('Casa con jardín en San Isidro', 'BARBECUE_AREA'), ('Casa con jardín en San Isidro', 'GARAGE'),
  ('Monoambiente moderno en Belgrano', 'ELEVATOR'), ('Monoambiente moderno en Belgrano', 'AIR_CONDITIONING'), ('Monoambiente moderno en Belgrano', 'LAUNDRY'),
  ('PH reciclado en Villa Crespo', 'PATIO'), ('PH reciclado en Villa Crespo', 'TERRACE'), ('PH reciclado en Villa Crespo', 'BARBECUE_AREA'),
  ('Oficina premium en Microcentro', 'ELEVATOR'), ('Oficina premium en Microcentro', 'SECURITY'), ('Oficina premium en Microcentro', 'AIR_CONDITIONING')
) AS a(property_name, amenity) ON p.name = a.property_name;

INSERT INTO property_image (url, is_cover, property_id, creation_date, modification_date)
SELECT image_url, true, p.id, now(), now() FROM property p JOIN (VALUES
  ('Departamento luminoso en Palermo', 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1600&q=85'),
  ('Casa con jardín en San Isidro', 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1600&q=85'),
  ('Monoambiente moderno en Belgrano', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1600&q=85'),
  ('PH reciclado en Villa Crespo', 'https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1600&q=85'),
  ('Oficina premium en Microcentro', 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1600&q=85'),
  ('Lote arbolado en Pilar', 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1600&q=85')
) AS images(property_name, image_url) ON p.name = images.property_name;

-- Dos contratos activos y su información financiera para cubrir el circuito de alquileres.
INSERT INTO contract (property_id, owner_id, tenant_id, start_date, end_date, base_rental_amount, adjustment_frequency, first_adjustment_date, currency, billing_frequency, status, contract_type, late_fee_percentage, creation_date, modification_date)
SELECT p.id, owner_c.id, tenant_c.id, DATE '2026-03-01', DATE '2028-02-29', 620000, 'QUARTERLY', DATE '2026-06-01', 'ARS', 'MONTHLY', 'ACTIVE', 'RESIDENTIAL', 5, now(), now()
FROM property p, customer owner_c, customer tenant_c WHERE p.name = 'Monoambiente moderno en Belgrano' AND owner_c.email = 'lucia.alonso@demo.com' AND tenant_c.email = 'nicolas.paz@demo.com'
UNION ALL
SELECT p.id, owner_c.id, tenant_c.id, DATE '2026-07-01', DATE '2028-06-30', 1300000, 'QUARTERLY', DATE '2026-10-01', 'ARS', 'MONTHLY', 'ACTIVE', 'COMMERCIAL', 5, now(), now()
FROM property p, customer owner_c, customer tenant_c WHERE p.name = 'Oficina premium en Microcentro' AND owner_c.email = 'valentina.suarez@demo.com' AND tenant_c.email = 'carolina.vega@demo.com';

INSERT INTO contract_adjustment (contract_id, effective_date, adjustment_type, value, active, creation_date)
SELECT id, first_adjustment_date, 'IPC', 8.50, true, now() FROM contract;

INSERT INTO invoice (code, customer_id, contract_id, type, status, date, due_date, total, auto_generated, billing_period, creation_date, modification_date)
SELECT 'ALQ-2026-08-001', tenant_id, id, 'RENT', 'PAID', DATE '2026-08-01', DATE '2026-08-05', 672700, true, '2026-08', now(), now() FROM contract WHERE base_rental_amount = 620000
UNION ALL SELECT 'ALQ-2026-08-002', tenant_id, id, 'RENT', 'ISSUED', DATE '2026-08-01', DATE '2026-08-05', 1410500, true, '2026-08', now(), now() FROM contract WHERE base_rental_amount = 1300000;

INSERT INTO invoice_line (invoice_id, concept, unit_price, quantity, subtotal, creation_date, modification_date)
SELECT id, 'Alquiler agosto 2026', total, 1, total, now(), now() FROM invoice;

INSERT INTO pay (amount, date, medium, invoice_id, creation_date, modification_date)
SELECT total, '2026-08-05', 'BANK_TRANSFER', id, now(), now() FROM invoice WHERE status = 'PAID';

INSERT INTO settlement (owner_id, contract_id, period, total_charged, commission, tax, net_pay, creation_date, modification_date)
SELECT c.owner_id, c.id, '2026-08', i.total, round(i.total * 0.08, 2), round(i.total * 0.0016, 2), round(i.total * 0.9184, 2), now(), now() FROM contract c JOIN invoice i ON i.contract_id = c.id WHERE i.status = 'PAID';

INSERT INTO notification (user_id, contract_id, invoice_id, type, title, message, due_date, read_status, creation_date, modification_date)
SELECT u.id, i.contract_id, i.id, 'RENTAL_AMOUNT_UPDATE', 'Próximo ajuste de alquiler', 'Hay un ajuste de alquiler programado para el próximo período.', '2026-10-01', false, now(), now()
FROM app_user u JOIN invoice i ON i.status = 'ISSUED' WHERE lower(u.email) = 'jere.gunsett@gmail.com';

COMMIT;
