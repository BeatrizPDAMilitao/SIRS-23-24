package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

class ContactDBandPrepareResponse
{
    private final String _privateServerKeyPath;
    private final String _publicServerKeyPath;
    private final String _publicClientKeyPath;
    private final String _publicClient2KeyPath;

    private final DBConection _dbConection;

    // Constructor
    public ContactDBandPrepareResponse(String ip, int port, String userName, String password, String privateServerKeyPath, String publicServerKeyPath, String publicClientKeyPath, String publicClient2KeyPath)
    {
        _privateServerKeyPath = privateServerKeyPath;
        _publicServerKeyPath = publicServerKeyPath;
        _publicClientKeyPath = publicClientKeyPath;
        _publicClient2KeyPath = publicClient2KeyPath;
        _dbConection = new DBConection(ip, port, userName, password);
    }

    public String[] SendRestInfoRequest(String clientID, String restName, int counter){
        if(clientID == null || restName == null) return new String[]{ null,null };
        //qualquer cena faz se mais pedidos
        int counterDB = _dbConection.getCount(clientID, counter);
        if (counterDB == -1){
            return new String[]{ null,null };
        }
        JsonObject response = _dbConection.SendRestInfoRequest(clientID, restName);
        if (response == null) {
            return new String[]{ null, Integer.toString(counterDB) };
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(response));
        String s = CryptoLibrary.Protect(response, counterDB, _privateServerKeyPath, _dbConection.getKey(clientID), false);
        /*
        try {
            FileOutputStream fis2 = new FileOutputStream("enc");
            fis2.write(s.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
        return new String[]{ s,Integer.toString(counterDB) };
    }
    public int SendRegisterRequest(String clientID) {
        FileInputStream fis = null;
        if (clientID == null) return -1;

        try {
            if(clientID.equals("a")){
                fis = new FileInputStream(_publicClient2KeyPath);
            }
            else {
                fis = new FileInputStream(_publicClientKeyPath);
            }
            byte[] content = new byte[fis.available()];
            fis.read(content);
            fis.close();
            System.out.println(clientID + " "+ Base64.getEncoder().encodeToString(content));
            return _dbConection.SendRegisterRequest(clientID, Base64.getEncoder().encodeToString(content));
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

    }

    public int[] SendVoucher(String clientSrc, String clientDst, int counter, int voucherID) {
        if (clientSrc  == null || clientDst == null) return new int[]{ -1, -1};
        int counterDB = _dbConection.getCount(clientSrc, counter);
        if (counterDB == -1){
            return new int[]{ -1, -1};
        }
        boolean suc = _dbConection.sendVoucherRequest(clientSrc, clientDst, voucherID);
        if (!suc){
            return new int[]{ -1,counterDB };
        }
        return new int[]{ 0,counterDB };
    }

    public int[] useVoucher(String client, int counter, int voucherID) {
        if (client  == null) return new int[]{ -1, -1};

        int counterDB = _dbConection.getCount(client, counter);
        if (counterDB == -1){
            return new int[]{ -1, -1};
        }
        boolean suc = _dbConection.useVoucherRequest(client, voucherID);
        if (!suc){
            return new int[]{ -1,counterDB };
        }
        return new int[]{ 0,counterDB };
    }

    public int[] SendReview(String clientID, String restName, int counter, int reviewVal, String reviewDescription, String validation) {
        if (clientID  == null || restName == null) return new int[]{ -1, -1};
        int counterDB = _dbConection.getCount(clientID, counter);
        if (counterDB == -1){
            return new int[]{ -1, -1};
        }
        if (reviewVal < 0 || reviewVal > 5) {
            return new int[]{ -1, counterDB};
        }
        boolean suc = verifyReview(clientID, reviewVal, reviewDescription,validation);
        System.out.println("validated: " + suc);
        if (!suc){
            return new int[]{ -1,counterDB };
        }
        suc = _dbConection.addReview(clientID, restName, reviewVal, reviewDescription, validation);
        if (!suc){
            return new int[]{ -1,counterDB };
        }
        return new int[]{ 0,counterDB };
    }

    public int[] removeReview(String client, int counter, int reviewID) {
        if (client  == null) return new int[]{ -1, -1};
        int counterDB = _dbConection.getCount(client, counter);
        if (counterDB == -1){
            return new int[]{ -1, -1};
        }
        boolean suc = _dbConection.removeReview(client, reviewID);
        if (!suc){
            return new int[]{ -1,counterDB };
        }
        return new int[]{ 0,counterDB };
    }

    public boolean verifyReview(String clientID, int reviewVal, String reviewDescription, String validation) {
        JsonObject validate = new JsonObject();
        validate.addProperty("clientName", clientID);
        validate.addProperty("stars", reviewVal);
        validate.addProperty("comment", reviewDescription);
        String keyB64 = _dbConection.getKey(clientID);
        return CryptoLibrary.verifyReview(validate, validation, keyB64);
    }
}
