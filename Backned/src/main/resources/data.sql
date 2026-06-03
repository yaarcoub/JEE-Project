INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_MANAGER');
INSERT IGNORE INTO roles (id, name) VALUES (3, 'ROLE_USER');

-- Default passwords (before hashing):
-- admin: admin123
-- manager: manger123
-- user: user123456
INSERT IGNORE INTO users (id, username, email, password, enabled, created_at)
VALUES
  (1, 'admin', 'admin@ensam.ma', '$2a$10$Due7v3Dn0SgPgdOvKMa6nekEvFUYM/CgCPQifNtjDYlmn14iocUNO', true, NOW()),
  (2, 'manager', 'manager@ensam.ma', '$2a$10$kN9lQ1qsXCeTllbgTabul.vhMbJ9EGKn5lRG5lCB/KdWFdxlB4O1m', true, NOW()),
  (3, 'user', 'user@ensam.ma', '$2a$10$5hVdb0sqD3XHE/b/687AVuZ5NsLAdj4rAdglWdewPjAfn3qP7Y8Uq', true, NOW());

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (3, 3);

INSERT IGNORE INTO categories (id, name, description, color) VALUES
  (1, 'Informatique', 'Livres de programmation et systèmes', '#3B82F6'),
  (2, 'Mathématiques', 'Analyse, algèbre, probabilités', '#10B981'),
  (3, 'Sciences', 'Physique, chimie, biologie', '#F59E0B'),
  (4, 'Littérature', 'Romans et contes littéraires', '#EC4899'),
  (5, 'Histoire', 'Récits historiques et biographies', '#F97316'),
  (6, 'Développement Personnel', 'Bien-être et amélioration personnelle', '#8B5CF6'),
  (7, 'Économie', 'Affaires, finance et gestion', '#06B6D4');

INSERT IGNORE INTO books (id, title, author, isbn, stock, description, published_date, created_at, updated_at, version)
VALUES
  (1, 'Spring Boot in Action', 'Craig Walls', '978-1617292545', 5, 'Une introduction complète à Spring Boot pour les développeurs Java.', '2019-01-01', NOW(), NOW(), 0),
  (2, 'Clean Code', 'Robert C. Martin', '978-0132350884', 3, 'Principes de conception et bonnes pratiques pour les développeurs.', '2008-08-01', NOW(), NOW(), 0),
  (3, 'Design Patterns', 'Gang of Four', '978-0201633610', 4, 'Motifs de conception réutilisables pour la programmation orientée objet.', '1994-10-31', NOW(), NOW(), 0),
  (4, 'The Pragmatic Programmer', 'Hunt & Thomas', '978-0201616224', 6, 'Guide pratique pour améliorer ses compétences en programmation.', '1999-10-20', NOW(), NOW(), 0),
  (5, 'Calcul Différentiel', 'Jacques Dixmier', '978-2100589265', 2, 'Étude approfondie du calcul différentiel et intégral.', '2015-06-15', NOW(), NOW(), 0),
  (6, 'Algèbre Linéaire', 'Serge Lang', '978-0387940007', 3, 'Introduction à l''algèbre linéaire et ses applications.', '1987-01-01', NOW(), NOW(), 0),
  (7, 'Physique Quantique', 'Claude Cohen-Tannoudji', '978-2100010134', 2, 'Cours complet de mécanique quantique avec exercices.', '1997-09-01', NOW(), NOW(), 0),
  (8, 'Biologie Moléculaire', 'Alberts Bruce', '978-0815341529', 4, 'La biologie moléculaire de la cellule, édition de référence.', '2014-12-01', NOW(), NOW(), 0),
  (9, 'Les Misérables', 'Victor Hugo', '978-2253096344', 7, 'Chef-d''œuvre de la littérature française, histoire épique.', '1862-04-03', NOW(), NOW(), 0),
  (10, 'Le Seigneur des Anneaux', 'J.R.R. Tolkien', '978-2253048589', 5, 'Épopée fantastique, l''un des plus grands classiques.', '1954-07-29', NOW(), NOW(), 0),
  (11, 'Orgeuil et Préjugés', 'Jane Austen', '978-2253096870', 6, 'Roman de l''amour et des relations sociales en Angleterre.', '1813-01-28', NOW(), NOW(), 0),
  (12, 'Histoire de France', 'Jules Michelet', '978-2070362264', 3, 'Récit complet de l''histoire du peuple français.', '1833-01-01', NOW(), NOW(), 0),
  (13, 'Habitudes Atomiques', 'James Clear', '978-2253190288', 8, 'Comment construire de bonnes habitudes et briser les mauvaises.', '2018-10-16', NOW(), NOW(), 0),
  (14, 'Le Pouvoir du Moment Présent', 'Eckhart Tolle', '978-2253085522', 5, 'Guide spirituel pour vivre dans le moment présent.', '2003-08-01', NOW(), NOW(), 0),
  (15, 'Sapiens', 'Yuval Noah Harari', '978-2253071068', 7, 'Histoire brève de l''humanité, du Big Bang à nos jours.', '2011-01-01', NOW(), NOW(), 0),
  (16, 'L''Intelligence Artificielle', 'Yann LeCun', '978-2100798704', 4, 'Révolution de l''IA et ses impacts sur la société.', '2019-09-01', NOW(), NOW(), 0),
  (17, 'Microéconomie', 'Paul Krugman', '978-2100790822', 2, 'Principes et applications de la microéconomie.', '2015-02-01', NOW(), NOW(), 0),
  (18, 'Les Mensonges que l''on se raconte', 'Mark Manson', '978-2253128496', 6, 'Guide honnête sur la vie et les relations humaines.', '2016-09-13', NOW(), NOW(), 0),
  (19, 'Thinking in Systems', 'Donella Meadows', '978-1603580557', 3, 'Comprendre et naviguer les systèmes complexes.', '2008-12-03', NOW(), NOW(), 0),
  (20, 'L''Art de la Négociation', 'Roger Fisher', '978-2707134975', 4, 'Techniques et stratégies pour négocier efficacement.', '1981-01-01', NOW(), NOW(), 0);

INSERT IGNORE INTO book_categories (book_id, category_id) VALUES 
  (1, 1),
  (2, 1),
  (3, 1),
  (4, 1),
  (5, 2),
  (6, 2),
  (7, 3),
  (8, 3),
  (9, 4),
  (10, 4),
  (11, 4),
  (12, 5),
  (13, 6),
  (14, 6),
  (15, 5),
  (16, 1),
  (17, 7),
  (18, 6),
  (19, 2),
  (20, 7);

-- Données d'emprunts de démonstration
INSERT IGNORE INTO loans (id, user_id, book_id, loan_date, expected_return_date, actual_return_date, status)
VALUES
  (1, 3, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), NULL, 'ACTIVE'),
  (2, 3, 9, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 26 DAY), NULL, 'ACTIVE'),
  (3, 2, 5, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 1 DAY), 'RETURNED'),
  (4, 2, 13, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), NULL, 'ACTIVE'),
  (5, 1, 15, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), NULL, 'ACTIVE');

INSERT IGNORE INTO loan_details (id, loan_id, itemcondition, notes, renewal_count)
VALUES
  (1, 1, 'Excellent', 'Très bon état, pas de marques', 0),
  (2, 2, 'Good', 'Quelques traces d''usure', 0),
  (3, 3, 'Excellent', 'Rendu en excellent état', 0),
  (4, 4, 'Good', 'Bon état général', 0),
  (5, 5, 'Excellent', 'Livre neuf, parfait état', 0);
