package org.example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class CryptoTerminal {
    public static void main(String[] args) throws Exception {

        // check args
        if (args.length < 1) {
            System.err.println("Usage: criptoLibrary [p|u|c]");
            return;
        }
        if (args[0].equals("p") && args.length < 5) {
            System.err.println("Usage: criptoLibrary p jsonPath counter BackEndPrivPath ClientPubPath");
            return;
        }
        if (args[0].equals("u") && args.length < 5) {
            System.err.println("Usage: criptoLibrary p encFilePath counter ClientPrivPath BackendPubPath");
            return;
        }
        if (args[0].equals("c") && args.length < 5) {
            System.err.println("Usage: criptoLibrary p jsonPath DS counter BackendPubPath");
            return;
        }

        final String mode = args[0];
        if (mode.toLowerCase().startsWith("p")) {
            args = new String[]{"p","json.json","0","Server_BackEnd/src/main/resources/BackendPriv.key","Server_BackEnd/src/main/resources/ClientPub.key"};

            String jsonPath = args[1];

            FileInputStream fis = new FileInputStream(jsonPath);
            byte[] content = new byte[fis.available()];
            fis.read(content);
            fis.close();
            JsonObject json = CryptoLibrary.decodeJsonObject((new String(content, StandardCharsets.UTF_8)));
            System.out.print("Please input the counter: ");
            int counter = Integer. parseInt(args[2]);

            System.out.print("Please input the privateServerKeyPath: ");
            String privateServerKeyPath = args[3];
            System.out.print("Please input the publicClientKeyPath: ");
            String publicClientKeyPath = args[4];
            System.out.println();
            String s = CryptoLibrary.Protect(json, counter, privateServerKeyPath, publicClientKeyPath, true);
            //System.out.print(s);
            FileOutputStream fis2 = new FileOutputStream("enc");
            fis2.write(s.getBytes());
        }
        else if (mode.toLowerCase().startsWith("u")) {
            args = new String[]{"u","enc","0","Client/src/main/resources/ClientPriv.key","Client/src/main/resources/BackendPub.key"};

            String jsonPath = args[1];
            System.out.print("Please input the json path: ");
            FileInputStream fis = new FileInputStream(jsonPath);
            byte[] content = new byte[fis.available()];
            fis.read(content);
            fis.close();

            System.out.print("Please input the counter: ");
            int counter =  Integer. parseInt(args[2]);

            System.out.print("Please input the privateClientKeyPath: ");
            String privateClientKeyPath = args[3];
            System.out.print("Please input the publicServerKeyPath: ");
            String publicServerKeyPath = args[4];
            System.out.println();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject response = CryptoLibrary.Unprotect(new String(content, StandardCharsets.UTF_8), counter, privateClientKeyPath, publicServerKeyPath);
            System.out.println(gson.toJson(response));
        }
        else if (mode.toLowerCase().startsWith("c")) {
            System.out.print("Please input the json path: ");
            String jsonPath = args[1];

            FileInputStream fis = new FileInputStream(jsonPath);
            byte[] content = new byte[fis.available()];
            fis.read(content);
            fis.close();
            JsonObject json = CryptoLibrary.decodeJsonObject((new String(content, StandardCharsets.UTF_8)));

            System.out.print("Please input the DS: ");
            String DS = args[2];

            System.out.print("Please input the counter: ");
            int counter =  Integer. parseInt(args[3]);
            System.out.print("Please input the publicServerKeyPath: ");
            String publicServerKeyPath =  args[4];

            System.out.print(CryptoLibrary.Check(json, DS, counter, publicServerKeyPath));
        }
        else {
            System.out.println("ERROR: mode error");
        }
        System.out.println("Done.");
    }
}