-- =====================================================================
--  SmartTrade  |  OPTIONAL demo data  (run AFTER smarttrade.sql)
--
--  Demo account : demo@smarttrade.lk  /  Demo@1234   (verified, approved seller)
--  Two demo products that use the sample images shipped in /uploads/product/4 and /5
--  (image URLs assume http://localhost:8080/smarttrade - change app.url if you use another port).
-- =====================================================================
USE smarttrade;

INSERT INTO users (created_at, updated_at, first_name, last_name, email, password, verification_code, status_id)
SELECT NOW(6), NOW(6), 'Demo', 'Seller', 'demo@smarttrade.lk', 'Demo@1234', '', s.id
FROM status s WHERE s.value = 'VERIFIED'
AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo@smarttrade.lk');

-- Admin account : admin@gmail.com / admin@123
INSERT INTO users (created_at, updated_at, first_name, last_name, email, password, verification_code, is_admin, status_id)
SELECT NOW(6), NOW(6), 'Admin', 'User', 'admin@gmail.com', 'admin@123', '', b'1', s.id
FROM status s WHERE s.value = 'VERIFIED'
AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com');
UPDATE users SET password = 'admin@123', is_admin = b'1' WHERE email = 'admin@gmail.com';

INSERT INTO address (line_one, line_two, postal_code, mobile, is_primary, city_id, user_id)
SELECT '12 Main Street', 'Town Centre', '11000', '0771234567', b'1', c.id, u.id
FROM users u JOIN city c ON c.name = 'Colombo'
WHERE u.email = 'demo@smarttrade.lk'
AND NOT EXISTS (SELECT 1 FROM address a WHERE a.user_id = u.id);

INSERT INTO seller (created_at, updated_at, company_name, company_mobile, company_email, status_id, user_id)
SELECT NOW(6), NOW(6), 'SmartTrade Demo Store', '0771234567', 'store@smarttrade.lk', s.id, u.id
FROM users u JOIN status s ON s.value = 'APPROVED'
WHERE u.email = 'demo@smarttrade.lk'
AND NOT EXISTS (SELECT 1 FROM seller x WHERE x.user_id = u.id);

-- product 4 : Galaxy A07 (Black, 128 GB, Brand New)
INSERT INTO product (id, created_at, updated_at, title, description, model_id, color_id, quality_id, storage_id, seller_id)
SELECT 4, NOW(6), NOW(6), 'Samsung Galaxy A07 128GB Brand New',
       '<p>Samsung Galaxy A07 - slim body, bright display and all-day battery. Sealed box with 1 year warranty.</p>',
       m.id, c.id, q.id, st.id, sl.id
FROM model m, color c, quality q, storage st, seller sl, users u
WHERE m.name = 'Galaxy A07' AND c.value = 'Black' AND q.value = 'Brand New' AND st.value = '128 GB'
  AND u.email = 'demo@smarttrade.lk' AND sl.user_id = u.id
  AND NOT EXISTS (SELECT 1 FROM product WHERE id = 4);

-- product 5 : Galaxy A07 (Silver, 64 GB, Like New)
INSERT INTO product (id, created_at, updated_at, title, description, model_id, color_id, quality_id, storage_id, seller_id)
SELECT 5, NOW(6), NOW(6), 'Samsung Galaxy A07 64GB Like New',
       '<p>Samsung Galaxy A07 in perfect condition. Includes charger and a protective case.</p>',
       m.id, c.id, q.id, st.id, sl.id
FROM model m, color c, quality q, storage st, seller sl, users u
WHERE m.name = 'Galaxy A07' AND c.value = 'Silver' AND q.value = 'Like New' AND st.value = '64 GB'
  AND u.email = 'demo@smarttrade.lk' AND sl.user_id = u.id
  AND NOT EXISTS (SELECT 1 FROM product WHERE id = 5);

INSERT INTO product_images (pr_id, images)
SELECT 4, 'http://localhost:8080/smarttrade/uploads/product/4/1763915055843.png' FROM DUAL WHERE EXISTS (SELECT 1 FROM product WHERE id = 4) AND NOT EXISTS (SELECT 1 FROM product_images WHERE pr_id = 4);
INSERT INTO product_images (pr_id, images)
SELECT 4, 'http://localhost:8080/smarttrade/uploads/product/4/1763915055852.jpg' FROM DUAL WHERE EXISTS (SELECT 1 FROM product WHERE id = 4) AND (SELECT COUNT(*) FROM product_images WHERE pr_id = 4) = 1;
INSERT INTO product_images (pr_id, images)
SELECT 4, 'http://localhost:8080/smarttrade/uploads/product/4/1763915055856.jpg' FROM DUAL WHERE EXISTS (SELECT 1 FROM product WHERE id = 4) AND (SELECT COUNT(*) FROM product_images WHERE pr_id = 4) = 2;

INSERT INTO product_images (pr_id, images)
SELECT 5, 'http://localhost:8080/smarttrade/uploads/product/5/1763915242697.jpg' FROM DUAL WHERE EXISTS (SELECT 1 FROM product WHERE id = 5) AND NOT EXISTS (SELECT 1 FROM product_images WHERE pr_id = 5);
INSERT INTO product_images (pr_id, images)
SELECT 5, 'http://localhost:8080/smarttrade/uploads/product/5/1763915242699.png' FROM DUAL WHERE EXISTS (SELECT 1 FROM product WHERE id = 5) AND (SELECT COUNT(*) FROM product_images WHERE pr_id = 5) = 1;
INSERT INTO product_images (pr_id, images)
SELECT 5, 'http://localhost:8080/smarttrade/uploads/product/5/1763915242703.jpg' FROM DUAL WHERE EXISTS (SELECT 1 FROM product WHERE id = 5) AND (SELECT COUNT(*) FROM product_images WHERE pr_id = 5) = 2;

INSERT INTO stock (created_at, updated_at, price, qty, product_id, discount_id, status_id)
SELECT NOW(6), NOW(6), 54900, 12, p.id, d.id, s.id
FROM product p, discount d, status s
WHERE p.id = 4 AND d.coupon_code = 'DEFAULT' AND s.value = 'APPROVED'
AND NOT EXISTS (SELECT 1 FROM stock WHERE product_id = 4);

INSERT INTO stock (created_at, updated_at, price, qty, product_id, discount_id, status_id)
SELECT NOW(6), NOW(6), 42500, 5, p.id, d.id, s.id
FROM product p, discount d, status s
WHERE p.id = 5 AND d.coupon_code = 'DEFAULT' AND s.value = 'APPROVED'
AND NOT EXISTS (SELECT 1 FROM stock WHERE product_id = 5);

-- Sample Demo Orders
INSERT INTO orders (id, created_at, updated_at, delivery_types_id, status_id, user_id)
SELECT 1, NOW(6), NOW(6), 1, s.id, u.id
FROM delivery_types dt, status s, users u
WHERE dt.id = 1 AND s.value = 'COMPLETED' AND u.email = 'isururathnayaka999@gmail.com'
AND NOT EXISTS (SELECT 1 FROM orders WHERE id = 1);

INSERT INTO order_items (id, qty, rating, orders_id, stock_id)
SELECT 1, 1, 5, 1, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE id = 1);

INSERT INTO orders (id, created_at, updated_at, delivery_types_id, status_id, user_id)
SELECT 2, NOW(6), NOW(6), 2, s.id, u.id
FROM delivery_types dt, status s, users u
WHERE dt.id = 2 AND s.value = 'PACKING' AND u.email = 'isururathnayaka999@gmail.com'
AND NOT EXISTS (SELECT 1 FROM orders WHERE id = 2);

INSERT INTO order_items (id, qty, rating, orders_id, stock_id)
SELECT 2, 1, 4, 2, 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE id = 2);

