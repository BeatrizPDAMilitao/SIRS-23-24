package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static String[] readArgumentsFromFile(String filePath)  {
        // Reads the args file
        try {
            return Files.readAllLines(Paths.get(filePath)).toArray( new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(/*"Usage: java -jar Server_BackEnd.jar "*/"<Args Path>");
            return;
        }
        String[] arguments = readArgumentsFromFile(args[0]);
        if (arguments.length != 9) {
            System.out.println(/*"Usage: java -jar Server_BackEnd.jar "*/"One per line: <ipDB> <portDB> <userDB> <passWordUserDB> <privateServerKeyPath> <publicServerKeyPath> <publicClientKeyPath> <publicClient2KeyPath> <ourPort>");
            return;
        }
        HttpsServerConnections httpsServerConnection = new HttpsServerConnections(arguments[0], Integer.parseInt(arguments[1]), arguments[2], arguments[3], arguments[4], arguments[5], arguments[6], arguments[7]);
        httpsServerConnection.loop(Integer.parseInt(arguments[8]));
    }
}