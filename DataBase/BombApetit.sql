DROP TABLE IF EXISTS Voucher;
DROP TABLE IF EXISTS Menu;
DROP TABLE IF EXISTS Dish;
DROP TABLE IF EXISTS Restaurant;
DROP TABLE IF EXISTS Person;

/* FUNCTIONS */

CREATE FUNCTION give_vouchers()
RETURNS TRIGGER
AS
$$
BEGIN
    INSERT INTO Voucher (code, person_id, restaurant_id, description, used) VALUES
    ('VOUCHER1', NEW.id, '1', 'Redeem this code for a 20% discount in the meal. Drinks not included.', 0),
    ('VOUCHER2', NEW.id, '2', 'Redeem this code for free drinks', 0),
    ('VOUCHER3', NEW.id, '3', 'Redeem this code for a free dessert', 0);
    RETURN NULL;
END
$$ LANGUAGE plpgsql;

/* TABLES */

CREATE TABLE Person (
    id VARCHAR(255) NOT NULL,
    counter NUMERIC(8,0),
    pubKeyB64 TEXT,
    PRIMARY KEY(id)
);


CREATE TABLE Restaurant (
    id SERIAL,
    owner VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE(name)
);

CREATE TABLE RestaurantGenre (
    restaurant_name VARCHAR(255) NOT NULL,
    genre_name VARCHAR(255) NOT NULL,
    PRIMARY KEY(restaurant_name, genre_name),
    FOREIGN KEY(restaurant_name) REFERENCES Restaurant(name)
);

CREATE TABLE Dish (
    id SERIAL,
    itemName VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    price NUMERIC(8,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    UNIQUE(id),
    PRIMARY KEY(itemName, category, price, currency)
);

CREATE TABLE Voucher (
    id SERIAL,
    code VARCHAR(255) NOT NULL,
    person_id VARCHAR(255) NOT NULL,
    restaurant_id INT,
    description VARCHAR(255) NOT NULL,
    used INT,
    UNIQUE(id),
    PRIMARY KEY(id),
    FOREIGN KEY(person_id) REFERENCES Person(id),
    FOREIGN KEY(restaurant_id) REFERENCES Restaurant(id)
);

CREATE TABLE Menu (
    id SERIAL,
    restaurant_id INT,
    dish_id INT,
    UNIQUE(id),
    PRIMARY KEY(restaurant_id, dish_id),
    FOREIGN KEY(restaurant_id) REFERENCES Restaurant(id),
    FOREIGN KEY(dish_id) REFERENCES Dish(id)
);

CREATE TABLE Review (
    id SERIAL,
    restaurant_id INT,
    person_id VARCHAR(255),
    number_stars INT NOT NULL,
    comment TEXT,
    valid TEXT,
    UNIQUE(id),
    PRIMARY KEY(id, restaurant_id, person_id),
    FOREIGN KEY(person_id) REFERENCES Person(id),
    FOREIGN KEY(restaurant_id) REFERENCES Restaurant(id)
);

/* TRIGGERS */

CREATE TRIGGER create_user
AFTER INSERT ON Person
FOR EACH ROW EXECUTE PROCEDURE give_vouchers();

/* POPULATE */

INSERT INTO Restaurant (owner, name, address) VALUES
('rc093', 'Dona_Maria', 'Rua da Glória, 22, Lisboa'),
('ba345', 'Tasca_do_Chico', 'Travessa do Murtal, 5, Lisboa'),
('eroew', 'Boteco_de_Lavre', 'Lavre, Évora');

INSERT INTO RestaurantGenre (restaurant_name, genre_name) VALUES
('Dona_Maria', 'Portuguese'),
('Dona_Maria', 'Traditional do bairro'),
('Tasca_do_Chico', 'Portuguese'),
('Tasca_do_Chico', 'Traditional da esquina'),
('Boteco_de_Lavre', 'Portuguese'),
('Boteco_de_Lavre', 'Traditional');



INSERT INTO Person VALUES ('1456w', 0, 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD4AAR2Rcq3imsqT3fp9z2wcNuX5vfI8IyryypPGoqNM7NJq1sGtYrAOaXce9AJo7ttQgm8OgrdOW64VJ4bCvZKx55hZ9gsTDtC+N1H3/nEkZka4Xs/R7YFYLTFm6gAqg3I39cb4dvHLq9lHXLEoP9yCae7PMINePjrJnig4HO0LwIDAQAB');
INSERT INTO Person VALUES ('ba345', 0, 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD4AAR2Rcq3imsqT3fp9z2wcNuX5vfI8IyryypPGoqNM7NJq1sGtYrAOaXce9AJo7ttQgm8OgrdOW64VJ4bCvZKx55hZ9gsTDtC+N1H3/nEkZka4Xs/R7YFYLTFm6gAqg3I39cb4dvHLq9lHXLEoP9yCae7PMINePjrJnig4HO0LwIDAQAB');
INSERT INTO Person VALUES ('9ifj0', 0, 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD4AAR2Rcq3imsqT3fp9z2wcNuX5vfI8IyryypPGoqNM7NJq1sGtYrAOaXce9AJo7ttQgm8OgrdOW64VJ4bCvZKx55hZ9gsTDtC+N1H3/nEkZka4Xs/R7YFYLTFm6gAqg3I39cb4dvHLq9lHXLEoP9yCae7PMINePjrJnig4HO0LwIDAQAB');
INSERT INTO Person VALUES ('eroew', 0, 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD4AAR2Rcq3imsqT3fp9z2wcNuX5vfI8IyryypPGoqNM7NJq1sGtYrAOaXce9AJo7ttQgm8OgrdOW64VJ4bCvZKx55hZ9gsTDtC+N1H3/nEkZka4Xs/R7YFYLTFm6gAqg3I39cb4dvHLq9lHXLEoP9yCae7PMINePjrJnig4HO0LwIDAQAB');
INSERT INTO Person VALUES ('qwmcj', 0, 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD4AAR2Rcq3imsqT3fp9z2wcNuX5vfI8IyryypPGoqNM7NJq1sGtYrAOaXce9AJo7ttQgm8OgrdOW64VJ4bCvZKx55hZ9gsTDtC+N1H3/nEkZka4Xs/R7YFYLTFm6gAqg3I39cb4dvHLq9lHXLEoP9yCae7PMINePjrJnig4HO0LwIDAQAB');
INSERT INTO Person VALUES ('rc093', 0, 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD4AAR2Rcq3imsqT3fp9z2wcNuX5vfI8IyryypPGoqNM7NJq1sGtYrAOaXce9AJo7ttQgm8OgrdOW64VJ4bCvZKx55hZ9gsTDtC+N1H3/nEkZka4Xs/R7YFYLTFm6gAqg3I39cb4dvHLq9lHXLEoP9yCae7PMINePjrJnig4HO0LwIDAQAB');

INSERT INTO Dish (itemName, category, description, price, currency) VALUES 
('House Steak', 'Meat', 'A succulent sirloin grilled steak.', '24.99', 'EUR'),
('Sardines', 'Fish', 'A Portuguese staple, accompanied by potatos and salad.', '21.99', 'EUR'),
('Mushroom Risotto', 'Vegetarian', 'Creamy Arborio rice cooked with assorted mushrooms and Parmesan cheese.', '16.99', 'EUR'),
('Bitoque', 'Meat', 'Steak accompanied by rice, fries and a fried egg.', '8.99', 'EUR'),
('Shrimp Linguini', 'Pasta', 'Pasta with shrimp', '12.99', 'EUR');

INSERT INTO MENU (restaurant_id, dish_id) VALUES
('1','1'),
('1','2'),
('1','3'),
('2','2'),
('2','3'),
('2','4'),
('3','3'),
('3','4'),
('3','5');

INSERT INTO Review (restaurant_id , person_id, number_stars, comment, valid) VALUES
(1, '1456w', 5, 'LOVED IT!!', 'validacao, nao vai funcionar pre feita'),
(2, 'ba345', 4, 'LOVED IT!!', 'validacao, nao vai funcionar pre feita'),
(3, '9ifj0', 4, 'LOVED IT!!', 'validacao, nao vai funcionar pre feita'),
(1, 'eroew', 1, 'LOVED IT!!', 'validacao, nao vai funcionar pre feita'),
(2, 'qwmcj', 3, 'LOVED IT!!', 'validacao, nao vai funcionar pre feita'),
(3, 'rc093', 4, 'LOVED IT!!', 'validacao, nao vai funcionar pre feita'),
(1, '1456w', 5, 'LOVED IT!!', 'validacao, nao vai funcionar pre feita');






