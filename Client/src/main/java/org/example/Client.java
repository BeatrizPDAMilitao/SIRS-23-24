package org.example;

import java.util.Scanner;

import com.google.gson.*;

public class Client {
    private String _privateClientKeyPath;
    private String _publicClientKeyPath;
    private String _privateClient2KeyPath;
    private String _publicClient2KeyPath;
    private String _publicServerKeyPath;
    private int _counter = 0;
    private String ip;
    private int port;
    HttpsClient https;
    public Client(String ipServer, int portServer, String privateClientKeyPath, String publicClientKeyPath, String privateClient2KeyPath, String publicClient2KeyPath, String publicServerKeyPath) { //ipServer, portServer, privateClientKeyPath, publicClientKeyPath, privateClient2KeyPath, publicClient2KeyPath, publicServerKeyPath
        https = new HttpsClient();
        ip = ipServer;
        port = portServer;
        _privateClientKeyPath = privateClientKeyPath;
        _publicClientKeyPath = publicClientKeyPath;
        _privateClient2KeyPath = privateClient2KeyPath;
        _publicClient2KeyPath = publicClient2KeyPath;
        _publicServerKeyPath = publicServerKeyPath;
    }

    public void Register(String name) {
        //connect to the backend
        //send a register request
        String[] response = null;
        try {
            response = https.sendGet("https://" + ip +":"+port+ "/register/?user=" + name);
        } catch (Exception e) { //error 400
            e.printStackTrace();
            System.out.println("ERROR: WRONG COUNTER");
            return;
        }
        if (response[0].equals("400")) {
            System.out.println("ERROR: WRONG COUNTER");
            _counter--;
            return;
        }
        if (response[0].equals("401")) {
            System.out.println("ERROR:COULD NOT REGISTER");
            return;
        }

        System.out.println(response[1]);
        _counter = Integer.parseInt(response[1]);
    }

    public void GetInfo(String name,String restaurant) {
        String[] response = null;
        try {
            response = https.sendGet("https://" + ip +":"+port+ "/restInfo/?user="+ name +
                    "&restName="+ restaurant +
                    "&counter="+ _counter++
            );
        } catch (Exception e) { //error 400
            e.printStackTrace();
            System.out.println("ERROR: WRONG COUNTER");
            return;
        }
        if (response[0].equals("400")) {
            System.out.println("ERROR: WRONG COUNTER");
            _counter--;
            return;
        }
        if (response[0].equals("401")) {
            System.out.println("ERROR:COULD NOT RECEIVE INFO");
            System.out.println("received counter: " + response[1]);
            _counter++;
            return;
        }

        //use the library to unprotect the document received
        Unprotect(response[1], name);
        _counter++;
    }

    public void SendVoucher(String name) {
        Scanner parser = new Scanner(System.in);
        System.out.println("Insert the name of the person you want to send the voucher to: ");
        String other = parser.nextLine();
        System.out.println("Insert the voucher id: ");

        String id = parser.nextLine();
        int vid;
        while (true) {
            try {
                vid = Integer.parseInt(id);
                break;
            } catch (NumberFormatException nfe) {}
            System.out.println("Please insert a valid number: ");
            id = parser.nextLine();
        }
        String[] response = null;
        try {
            response = https.sendGet("https://" + ip +":"+port+ "/sendVoucher/?userSrc=" + name +
                    "&userDst="+ other +
                    "&counter="+ _counter++ +
                    "&voucherID="+ vid
            );
        } catch (Exception e) { //error 400
            e.printStackTrace();
            System.out.println("ERROR: WRONG COUNTER");
            return;
        }
        if (response[0].equals("400")) {
            System.out.println("ERROR: WRONG COUNTER");
            _counter--;
            return;
        }
        if (response[0].equals("401")) {
            System.out.println("ERROR: COULD NOT SEND VOUCHER");
            System.out.println("received counter: " + response[1]);
            _counter++;
            return;
        }

        if (Integer.parseInt(response[1]) == -1 || _counter != Integer.parseInt(response[1])) {
            System.out.println("ERROR: COUNTER RECEIVED IS DIFFERENT");
            return;
        }
        System.out.println(response[1]);
        _counter++;
    }

    public void Review(String name) { //client envia nome, nomeRest, counter e inteiro de review
        System.out.println("Insert restaurant name:");
        Scanner parser = new Scanner(System.in);
        String rest = parser.nextLine();

        System.out.println("Insert the number of stars (0-5): ");
        String rate = parser.nextLine();
        int n;
        while (true) {
            try {
                n = Integer.parseInt(rate);
                if ( 0<= n && n <= 5) {
                    break;
                }
            } catch (NumberFormatException nfe) {}
            System.out.println("Please insert a valid number: ");
            rate = parser.nextLine();
        }
        System.out.println("Write a description: ");
        String desc = parser.nextLine();
        String valid = CreateValidation(name, n, desc);
        System.out.println("Valid: " + valid);
        String[] response = null;
        try {
            response = https.sendGet("https://" + ip +":"+port+ "/sendReview/?user=" + name +
                    "&restName="+ rest +
                    "&counter="+ _counter++ +
                    "&reviewVal="+ n +
                    "&reviewDescription="+ desc +
                    "&validation="+ valid
            );
        } catch (Exception e) { //error 400
            e.printStackTrace();
            System.out.println("ERROR: WRONG COUNTER");
            return;
        }
        if (response[0].equals("400")) {
            System.out.println("ERROR: WRONG COUNTER");
            _counter--;
            return;
        }
        if (response[0].equals("401")) {
            System.out.println("ERROR: COULD NOT SEND REVIEW");
            System.out.println("received counter: " + response[1]);
            _counter++;
            return;
        }

        if (Integer.parseInt(response[1]) == -1 || _counter != Integer.parseInt(response[1])) {
            System.out.println("ERROR: COUNTER RECEIVED IS DIFFERENT");
            return;
        }

        System.out.println(response[1]);
        _counter++;
    }

    public void DeleteReview(String name) {
        System.out.println("Insert review id:");
        Scanner parser = new Scanner(System.in);
        String id = parser.nextLine();
        int rid;
        while (true) {
            try {
                rid = Integer.parseInt(id);
                break;
            } catch (NumberFormatException nfe) {}
            System.out.println("Please insert a valid number: ");
            id = parser.nextLine();
        }
        //int rid = Integer.parseInt(id);

        String[] response = null;
        try {
            response = https.sendGet("https://" + ip +":"+port+ "/removeReview/?user=" + name +
                    "&counter="+ _counter++ +
                    "&reviewID="+ rid
            );
        } catch (Exception e) { //error 400
            e.printStackTrace();
            System.out.println("ERROR: WRONG COUNTER");
            return;
        }
        if (response[0].equals("400")) {
            System.out.println("ERROR: WRONG COUNTER");
            _counter--;
            return;
        }
        if (response[0].equals("401")) {
            System.out.println("ERROR: COULD NOT DELETE REVIEW");
            System.out.println("received counter: " + response[1]);
            _counter++;
            return;
        }

        if (Integer.parseInt(response[1]) == -1 || _counter != Integer.parseInt(response[1])) {
            System.out.println("ERROR: COUNTER RECEIVED IS DIFFERENT");
            return;
        }

        System.out.println(response[1]);
        _counter++;
    }

    public void Reserve(String name) {
        System.out.println("Insert restaurant name:");
        Scanner parser = new Scanner(System.in);
        String rest = parser.nextLine();

        System.out.println("Insert voucher id:");
        String id = parser.nextLine();
        int vid;
        while (true) {
            try {
                vid = Integer.parseInt(id);
                break;
            } catch (NumberFormatException nfe) {}
            System.out.println("Please insert a valid number: ");
            id = parser.nextLine();
        }

        String[] response = null;
        try {
            response = https.sendGet("https://" + ip +":"+port+ "/useVoucher/?user=" + name +
                    "&counter="+ _counter++ +
                    "&voucherID="+ vid
            );
        } catch (Exception e) { //error 400
            e.printStackTrace();
            System.out.println("ERROR: WRONG COUNTER");
            return;
        }
        if (response[0].equals("400")) {
            System.out.println("ERROR: WRONG COUNTER");
            _counter--;
            return;
        }
        if (response[0].equals("401")) {
            System.out.println("ERROR: COULD NOT USE VOUCHER");
            System.out.println("received counter: " + response[1]);
            _counter++;
            return;
        }

        if (Integer.parseInt(response[1]) == -1 || _counter != Integer.parseInt(response[1])) {
            System.out.println("ERROR: COUNTER RECEIVED IS DIFFERENT");
            return;
        }

        System.out.println(response[1]);
        _counter++;

        System.out.printf("Successfully reserved a table at %s! The reservation is in the name: %s\n", rest, name);
    }

    private void Unprotect(String document, String name) {
        JsonObject unprotected = null;
        if(name.equals("a")){
            unprotected = CryptoLibrary.Unprotect(document, _counter, _privateClient2KeyPath, _publicServerKeyPath);
        }
        else{
            unprotected = CryptoLibrary.Unprotect(document, _counter, _privateClientKeyPath, _publicServerKeyPath);
        }
        if (unprotected != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(unprotected));
            System.out.println("Warning: If a review says \"Not Verified\" then something went wrong validating the authenticity/non-repudiation of that review");
        }
        else{
            System.out.println("ERROR:COULD NOT UNPROTECT");
        }
    }

    private String CreateValidation(String name, int rate, String desc) {
        JsonObject review = new JsonObject();
        review.addProperty("clientName", name);
        review.addProperty("stars", rate);
        review.addProperty("comment", desc);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(review));
        if(name.equals("a")) {
            return CryptoLibrary.digitalSignature(review, CryptoLibrary.readPrivateKey(_privateClient2KeyPath));
        }
        return CryptoLibrary.digitalSignature(review, CryptoLibrary.readPrivateKey(_privateClientKeyPath));
    }
}