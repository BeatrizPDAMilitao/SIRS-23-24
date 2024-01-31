/* Since the queries have require user input functions were added*/


-- Register
CREATE OR REPLACE FUNCTION registerPerson(name VARCHAR(255), pubKeyB64 TEXT)
RETURNS NUMERIC AS $$
DECLARE
    counterDB NUMERIC;
BEGIN
    -- We get the counter for the name received
    SELECT counter INTO counterDB FROM Person WHERE id = name;

    -- counterDB is null if name does not exist
    IF counterDB IS NULL THEN
        -- Create a new person with that name and with counter = 0
        INSERT INTO Person (id, counter, pubKeyB64) VALUES (name, 0, pubKeyB64);
        counterDB := 0;
    END IF;

    -- Return the counter
    RETURN counterDB;
END;
$$ LANGUAGE plpgsql;

-- Rest Info
CREATE OR REPLACE FUNCTION getRestInfo(restName VARCHAR(255))
RETURNS json AS $$
DECLARE
    restaurantId INT;
    restInfo json;
BEGIN
    SELECT id INTO restaurantId FROM Restaurant WHERE name = restName;
    IF restaurantId IS NULL THEN
            RAISE EXCEPTION 'Restaurant not found';
    END IF;


    SELECT json_build_object(
        'owner', rest.owner,
        'name', rest.name,
        'address', rest.address,
        'genre', (
            SELECT json_agg(
                DISTINCT restGenre.genre_name
                )
            FROM RestaurantGenre restGenre
            WHERE rest.name = restGenre.restaurant_name
            ),
        'menu', (
            SELECT json_agg(
                json_build_object(
                    'name', dish.itemName,
                    'category', dish.category,
                    'description', dish.description,
                    'price', dish.price,
                    'currency',
                    dish.currency)
                )
                FROM Menu menu
                JOIN Dish dish
                ON menu.dish_id = dish.id
                WHERE rest.id = menu.restaurant_id
            )
    ) INTO restInfo
    FROM Restaurant rest
    WHERE rest.name = restName;

    RETURN restInfo;
END;
$$ LANGUAGE plpgsql;


-- VOUCHERS
CREATE OR REPLACE FUNCTION getPersonVouchersInRest(personName VARCHAR(255), restName VARCHAR(255))
RETURNS TABLE (
    id INTEGER,
    code VARCHAR(255),
    description VARCHAR(255),
    used INT
) AS $$
BEGIN
    RETURN QUERY
    SELECT voucher.id, voucher.code, voucher.description, voucher.used
    FROM Voucher voucher
    JOIN Restaurant rest
    ON voucher.restaurant_id = rest.id
    WHERE voucher.person_id = personName
    AND rest.name = restName;
END;
$$ LANGUAGE plpgsql;

/* COUNTER */
CREATE OR REPLACE FUNCTION getCounterAndIncrement(personName VARCHAR(255), counterRecv NUMERIC)
RETURNS NUMERIC AS $$
DECLARE
    current_counter NUMERIC;
BEGIN
    SELECT counter
    INTO current_counter
    FROM Person
    WHERE id = personName;

    IF current_counter IS NULL THEN
        RAISE EXCEPTION 'Person not found';
    END IF;

    IF current_counter != counterRecv THEN
        RETURN -1;
    END IF;

    UPDATE Person
    SET counter = counter + 2
    WHERE id = personName;

    RETURN current_counter+1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION transferVoucher(clientSrcId VARCHAR(255), clientDstId VARCHAR(255), voucherID INT)
RETURNS VOID AS $$
BEGIN
    UPDATE Voucher
    SET person_id = clientDstId
    WHERE id = voucherID
    AND person_id = clientSrcId
    AND used = 0;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'ERROR';
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION useVoucher(clientId VARCHAR(255), voucherID INT)
RETURNS VOID AS $$
BEGIN
    UPDATE Voucher
    SET used = 1
    WHERE id = voucherID
    AND person_id = clientId
    AND used = 0;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'ERROR';
    END IF;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION addReview(personName VARCHAR(255), restName VARCHAR(255), numberStars INT, commentRcv TEXT, validRcv TEXT)
RETURNS VOID AS $$
DECLARE
    restaurantId INT;
    personId VARCHAR(255);
BEGIN
    SELECT id INTO restaurantId FROM Restaurant WHERE name = restName;
    IF restaurantId IS NULL THEN
        RAISE EXCEPTION 'Restaurant not found';
    END IF;

    SELECT id INTO personId FROM Person WHERE id = personName;
    IF personId IS NULL THEN
        RAISE EXCEPTION 'Person not found';
    END IF;

    INSERT INTO Review (restaurant_id , person_id, number_stars, comment, valid)
    VALUES (restaurantId, personName, numberStars, commentRcv, validRcv);
END;
$$ LANGUAGE plpgsql;



CREATE FUNCTION restaurantReviews (restName VARCHAR(255))
RETURNS TABLE (
    id INTEGER,
    personId VARCHAR(255),
    stars INT,
    comment TEXT,
    validation TEXT
)
AS
$$
BEGIN
    RETURN QUERY
    SELECT review.id, review.person_id, review.number_stars, review.comment, review.valid
    FROM Restaurant restaurant
    INNER JOIN Review review
    ON restaurant.id = review.restaurant_id
    WHERE restaurant.name = restName;
END
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION getReviewersPubKeys(restName VARCHAR(255))
RETURNS TABLE (
    client VARCHAR(255), 
    pubKeyB64 TEXT
) AS $$
BEGIN
    RETURN QUERY 
    SELECT DISTINCT review.person_id, person.pubKeyB64
    FROM Review review
    JOIN Person person 
    ON review.person_id = person.id
    JOIN Restaurant rest
    ON review.restaurant_id = rest.id
    WHERE rest.name = restName;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getKey(personName VARCHAR(255))
RETURNS TABLE (
    pubKeyB64 TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT Person.pubKeyB64
    FROM Person
    WHERE id = personName;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION removeReview(personName VARCHAR(255), reviewId INT)
RETURNS VOID AS
$$
BEGIN
    DELETE FROM Review WHERE id = reviewId AND person_id = personName;
END;
$$ LANGUAGE plpgsql;

-------------------------------------------------------------------------------------------------------------------------
CREATE FUNCTION restaurant_menu(res_name VARCHAR(255))
RETURNS TABLE (
    itemName VARCHAR(255),
    category VARCHAR(255),
    description VARCHAR(255),
    price NUMERIC(8,2),
    currency VARCHAR(255)
)
AS
$$
BEGIN
    RETURN QUERY
    SELECT final_menu.itemName, final_menu.category, final_menu.description,
        final_menu.price, final_menu.currency  FROM ((SELECT dish_id FROM
            (Menu INNER JOIN Restaurant ON Menu.restaurant_id = Restaurant.id) AS aux
            WHERE aux.name = res_name)
        AS res INNER JOIN DISH ON res.dish_id = Dish.id) AS final_menu;
END
$$ LANGUAGE plpgsql;

CREATE FUNCTION restaurant_vouchers(client_id VARCHAR(255), res_name VARCHAR(255))
RETURNS TABLE (
    code VARCHAR(255),
    description VARCHAR(255)
)
AS
$$
BEGIN
    RETURN QUERY
    SELECT aux.code, aux.description FROM (Restaurant INNER JOIN Voucher
        ON Voucher.restaurant_id = Restaurant.id) AS aux
        WHERE aux.person_id = client_id AND aux.name = res_name;
END
$$ LANGUAGE plpgsql;



