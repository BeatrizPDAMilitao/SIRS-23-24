package org.example;

import com.google.gson.*;

import java.sql.*;
import java.util.Properties;

public class DBConection {
    private Connection conn;

    public DBConection(String ip, int port, String userName, String password) {
        String url = "jdbc:postgresql://" + ip + ":" + port + "/" + "bombapetit";

        Properties props = new Properties();
        props.setProperty("user", userName);
        props.setProperty("password", password);
        props.setProperty("ssl", "true");
        props.setProperty("sslmode", "verify-full");
        props.setProperty("sslrootcert", "Server_BackEnd/src/main/resources/database.crt");
        try {
            Class.forName("org.postgresql.Driver");
            this.conn = DriverManager.getConnection(url, props);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public int SendRegisterRequest(String clientID, String keyEncoded) {
        // Use sql function to register de client
        String query = "SELECT registerPerson(?, ?)";

        try {
            // PreparedStatement to prevent sql injection
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, clientID);
            preparedStatement.setString(2, keyEncoded);
            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        return -1;
    }
    public JsonObject SendRestInfoRequest(String clientID, String restName) {
        String query = "SELECT getRestInfo(?)";
        JsonObject restaurantInfo = null;

        try {
            // PreparedStatement to prevent sql injection
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, restName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getObject(1).toString();
                restaurantInfo = JsonParser.parseString(result).getAsJsonObject();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (restaurantInfo == null) {
            return null;
        }
        // Prepare the json the way we need it
        JsonObject restInfo = new JsonObject();
        JsonArray reviews = getRestaurantReviews(restName);
        JsonObject reviewers = getReviewersPubKeys(restName);
        JsonArray vouchers = getPersonVouchers(clientID,restName);
        restaurantInfo.add("reviews", reviews);
        restInfo.add("restaurantInfo",restaurantInfo);
        restInfo.add("vouchers", vouchers);
        restInfo.add("reviewers", reviewers);
        return restInfo;
    }
    public JsonArray getPersonVouchers(String clientID, String restName) {
        String query = "SELECT * FROM getPersonVouchersInRest(?, ?)";
        JsonArray vouchers = new JsonArray();

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, clientID);
            preparedStatement.setString(2, restName);
            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", result.getString("id"));
                jsonObject.addProperty("code", result.getString("code"));
                jsonObject.addProperty("description", result.getString("description"));
                int used = result.getInt("used");

                if (used==0) jsonObject.addProperty("used", "not used");
                else if (used==1) jsonObject.addProperty("used", "used");
                else jsonObject.addProperty("used", "?");

                vouchers.add(jsonObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vouchers;
    }
    public JsonArray getRestaurantReviews(String restaurantName) {
        String query = "SELECT * FROM restaurantReviews(?)";
        JsonArray reviews = new JsonArray();

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, restaurantName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                JsonObject review = new JsonObject();
                review.addProperty("id", resultSet.getInt("id"));
                review.addProperty("clientName", resultSet.getString("personId"));
                review.addProperty("stars", resultSet.getInt("stars"));
                review.addProperty("comment", resultSet.getString("comment"));
                review.addProperty("validation", resultSet.getString("validation"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public JsonObject getReviewersPubKeys(String restaurantName) {
        String query = "SELECT * FROM getReviewersPubKeys(?)";
        JsonObject reviewers = new JsonObject();

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, restaurantName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                reviewers.addProperty(resultSet.getString("client"), resultSet.getString("pubKeyB64"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviewers;
    }

    public int getCount(String clientID, int counterRecv){
        String query = "SELECT getCounterAndIncrement(?,?)";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, clientID);
            preparedStatement.setInt(2, counterRecv);
            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean sendVoucherRequest(String clientSrc, String clientDst, int voucherID) {
        String query = "SELECT transferVoucher(?, ?, ?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, clientSrc);
            preparedStatement.setString(2, clientDst);
            preparedStatement.setInt(3, voucherID);
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean useVoucherRequest(String client, int voucherID) {
        String query = "SELECT useVoucher(?, ?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, client);
            preparedStatement.setInt(2, voucherID);
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean addReview(String personName, String restaurantName, int numberStars, String comment, String valid) {
        String query = "SELECT addReview(?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, personName);
            preparedStatement.setString(2, restaurantName);
            preparedStatement.setInt(3, numberStars);
            preparedStatement.setString(4, comment);
            preparedStatement.setString(5, valid);
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean removeReview(String client, int reviewID) {
        String query = "SELECT removeReview(?, ?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, client);
            preparedStatement.setInt(2, reviewID);
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public String getKey(String client) {
        String query = "SELECT getKey(?)";

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, client);
            preparedStatement.executeQuery();
            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                return result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
