INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_MANAGER');
INSERT IGNORE INTO roles (id, name) VALUES (3, 'ROLE_USER');

INSERT IGNORE INTO users (id, username, email, password, enabled, created_at)
VALUES
  (1, 'admin', 'admin@ensam.ma', '$2a$10$Due7v3Dn0SgPgdOvKMa6nekEvFUYM/CgCPQifNtjDYlmn14iocUNO', true, NOW()),
  (2, 'manager', 'manager@ensam.ma', '$2a$10$16OkPWnzTJFt0CSCpraOruOVTpmm2M0s3Az419zbeVUP1UGOIXumq', true, NOW()),
  (3, 'user', 'user@ensam.ma', '$2a$10$0VKCWJhhaN4vH2BVwput4eAhK6Kp.JnKR./B6XwEo9uvV6Oz6h3km', true, NOW());

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (3, 3);

INSERT IGNORE INTO categories (id, name, description, color) VALUES
  (1, 'Informatique', 'Livres de programmation et systèmes', '#3B82F6'),
  (2, 'Mathématiques', 'Analyse, algèbre, probabilités', '#10B981'),
  (3, 'Sciences', 'Physique, chimie, biologie', '#F59E0B');

INSERT IGNORE INTO books (id, title, author, isbn, stock, description, published_date, created_at, updated_at)
VALUES
  (1, 'Spring Boot in Action', 'Craig Walls', '978-1617292545', 5, 'Une introduction complète à Spring Boot.', '2019-01-01', NOW(), NOW()),
  (2, 'Clean Code', 'Robert C. Martin', '978-0132350884', 3, 'Principes de conception et bonnes pratiques pour les développeurs.', '2008-08-01', NOW(), NOW());

INSERT IGNORE INTO book_categories (book_id, category_id) VALUES (1, 1);
INSERT IGNORE INTO book_categories (book_id, category_id) VALUES (2, 1);
