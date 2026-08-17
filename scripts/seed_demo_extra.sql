BEGIN;

-- 32 contactos adicionales: permiten probar búsquedas, filtros y formularios.
INSERT INTO customer (name, surname, document_type, document_number, cuit, birthdate, email, phone, creation_date, modification_date)
SELECT
  'Cliente', 'Demo ' || lpad(n::text, 2, '0'), 'DNI', (41000000 + n)::text,
  '20-' || (41000000 + n)::text || '-1', DATE '1980-01-01' + (n * 137),
  'demo.cliente' || lpad(n::text, 2, '0') || '@inmobiliaria.demo', '+54 11 5000-' || lpad(n::text, 4, '0'), now(), now()
FROM generate_series(1, 32) AS n;

-- 30 nuevas propiedades con precios, ubicaciones, estados e imágenes de portada.
INSERT INTO property (name, owner_id, property_type, status, sale_price, rent_price, street, numeration, floor, department, zip_code, city, province, country, bathrooms, bedrooms, furnished, construction_year, total_area, covered_area, creation_date, modification_date)
SELECT
  'Demo Propiedad ' || lpad(n::text, 2, '0'), c.id,
  CASE n % 6 WHEN 0 THEN 'HOUSE' WHEN 1 THEN 'APARTMENT' WHEN 2 THEN 'CONDO' WHEN 3 THEN 'OFFICE' WHEN 4 THEN 'LOT' ELSE 'DUPLEX' END,
  CASE n % 9 WHEN 0 THEN 'RENTED' WHEN 1 THEN 'RESERVED' ELSE 'AVAILABLE' END,
  CASE WHEN n % 3 IN (0, 2) THEN (85000 + n * 12000)::numeric ELSE NULL::numeric END,
  CASE WHEN n % 3 IN (1, 2) THEN (450000 + n * 35000)::numeric ELSE NULL::numeric END,
  CASE n % 5 WHEN 0 THEN 'Av. Libertador' WHEN 1 THEN 'Cabildo' WHEN 2 THEN 'Av. Santa Fe' WHEN 3 THEN 'Olazábal' ELSE 'Rivadavia' END,
  (900 + n * 37)::text,
  CASE WHEN n % 4 = 0 THEN NULL ELSE ((n % 12) + 1)::text END,
  CASE WHEN n % 4 = 0 THEN NULL ELSE chr(65 + (n % 4)) END,
  (1400 + n)::text,
  CASE n % 5 WHEN 0 THEN 'Vicente López' WHEN 1 THEN 'Ciudad de Buenos Aires' WHEN 2 THEN 'San Isidro' WHEN 3 THEN 'Tigre' ELSE 'Pilar' END,
  'Buenos Aires', 'Argentina',
  CASE WHEN n % 5 = 4 THEN NULL ELSE (1 + n % 3) END,
  CASE WHEN n % 5 = 4 THEN NULL ELSE (1 + n % 4) END,
  n % 2 = 0, 2000 + n % 24, (45 + n * 7)::numeric, (38 + n * 5)::numeric, now(), now()
FROM generate_series(1, 30) AS n
JOIN customer c ON c.email = 'demo.cliente' || lpad((((n - 1) % 16) + 1)::text, 2, '0') || '@inmobiliaria.demo';

INSERT INTO property_operation_type (property_id, operation_type)
SELECT id, 'SALE' FROM property WHERE name LIKE 'Demo Propiedad %' AND sale_price IS NOT NULL
UNION ALL
SELECT id, 'RENT' FROM property WHERE name LIKE 'Demo Propiedad %' AND rent_price IS NOT NULL;

INSERT INTO property_amenity (property_id, amenity)
SELECT id, CASE (id % 6) WHEN 0 THEN 'SWIMMING_POOL' WHEN 1 THEN 'BALCONY' WHEN 2 THEN 'GARDEN' WHEN 3 THEN 'ELEVATOR' WHEN 4 THEN 'GARAGE' ELSE 'AIR_CONDITIONING' END
FROM property WHERE name LIKE 'Demo Propiedad %';

INSERT INTO property_amenity (property_id, amenity)
SELECT id, CASE (id % 4) WHEN 0 THEN 'SECURITY' WHEN 1 THEN 'BARBECUE_AREA' WHEN 2 THEN 'TERRACE' ELSE 'HEATING' END
FROM property WHERE name LIKE 'Demo Propiedad %';

INSERT INTO property_image (url, is_cover, property_id, creation_date, modification_date)
SELECT
  CASE id % 6
    WHEN 0 THEN 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1600&q=85'
    WHEN 1 THEN 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1600&q=85'
    WHEN 2 THEN 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1600&q=85'
    WHEN 3 THEN 'https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1600&q=85'
    WHEN 4 THEN 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1600&q=85'
    ELSE 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1600&q=85'
  END,
  true, id, now(), now()
FROM property WHERE name LIKE 'Demo Propiedad %';

-- Diez contratos de alquiler para el circuito operativo.
WITH rental_properties AS (
  SELECT p.id, p.owner_id, p.rent_price, row_number() OVER (ORDER BY p.id) AS rn
  FROM property p WHERE p.name LIKE 'Demo Propiedad %' AND p.rent_price IS NOT NULL
  ORDER BY p.id LIMIT 10
)
INSERT INTO contract (property_id, owner_id, tenant_id, start_date, end_date, base_rental_amount, adjustment_frequency, first_adjustment_date, currency, billing_frequency, status, contract_type, late_fee_percentage, creation_date, modification_date)
SELECT rp.id, rp.owner_id, c.id, DATE '2026-02-01', DATE '2028-01-31', rp.rent_price, 'QUARTERLY', DATE '2026-05-01', 'ARS', 'MONTHLY', 'ACTIVE', 'RESIDENTIAL', 5, now(), now()
FROM rental_properties rp
JOIN customer c ON c.email = 'demo.cliente' || lpad((16 + rp.rn)::text, 2, '0') || '@inmobiliaria.demo';

INSERT INTO contract_adjustment (contract_id, effective_date, adjustment_type, value, active, creation_date)
SELECT c.id, DATE '2026-08-01', 'IPC', 7.25, true, now()
FROM contract c JOIN property p ON p.id = c.property_id WHERE p.name LIKE 'Demo Propiedad %';

INSERT INTO invoice (code, customer_id, contract_id, type, status, date, due_date, total, auto_generated, billing_period, creation_date, modification_date)
SELECT
  'DEMO-ALQ-2026-08-' || lpad(c.id::text, 4, '0'), c.tenant_id, c.id, 'RENT',
  CASE WHEN c.id % 3 = 0 THEN 'ISSUED' ELSE 'PAID' END,
  TIMESTAMP '2026-08-01 09:00:00', DATE '2026-08-10', round(c.base_rental_amount * 1.0725, 2), true, '2026-08', now(), now()
FROM contract c JOIN property p ON p.id = c.property_id WHERE p.name LIKE 'Demo Propiedad %';

INSERT INTO invoice_line (invoice_id, concept, unit_price, quantity, subtotal, creation_date, modification_date)
SELECT i.id, 'Alquiler agosto 2026', i.total, 1, i.total, now(), now()
FROM invoice i WHERE i.code LIKE 'DEMO-ALQ-%';

INSERT INTO pay (amount, date, medium, invoice_id, creation_date, modification_date)
SELECT i.total, DATE '2026-08-05', CASE WHEN i.id % 2 = 0 THEN 'BANK_TRANSFER' ELSE 'DIGITAL_WALLET' END, i.id, now(), now()
FROM invoice i WHERE i.code LIKE 'DEMO-ALQ-%' AND i.status = 'PAID';

INSERT INTO settlement (owner_id, contract_id, period, total_charged, commission, tax, net_pay, creation_date, modification_date)
SELECT c.owner_id, c.id, '2026-08', i.total, round(i.total * 0.08, 2), round(i.total * 0.0016, 2), round(i.total * 0.9184, 2), now(), now()
FROM contract c JOIN invoice i ON i.contract_id = c.id
WHERE i.code LIKE 'DEMO-ALQ-%' AND i.status = 'PAID';

INSERT INTO notification (user_id, contract_id, invoice_id, type, title, message, due_date, read_status, creation_date, modification_date)
SELECT u.id, i.contract_id, i.id, 'RENTAL_AMOUNT_UPDATE', 'Próximo ajuste de alquiler', 'Se aproxima el próximo ajuste de alquiler de una propiedad de demo.', DATE '2026-10-01' + ((i.id % 20)::integer), false, now(), now()
FROM app_user u JOIN invoice i ON i.code LIKE 'DEMO-ALQ-%'
WHERE lower(u.email) = 'jere.gunsett@gmail.com';

COMMIT;
