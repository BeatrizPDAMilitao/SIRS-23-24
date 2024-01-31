package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import org.example.Client;

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
            System.out.println(/*"Usage: java -jar Client.jar "*/"<Args Path>");
            return;
        }
        String[] arguments = readArgumentsFromFile(args[0]);
        if (arguments.length != 7) {
            System.out.println(/*"Usage: java -jar Client.jar "*/"One per line: <ipServer> <portServer> <privateClientKeyPath> <publicClientKeyPath> <privateClient2KeyPath> <publicClient2KeyPath> <publicServerKeyPath>");
            return;
        }
        Client client = new Client(arguments[0], Integer.parseInt(arguments[1]), arguments[2], arguments[3],arguments[4], arguments[5],arguments[6]); //ipServer, portServer, privateClientKeyPath, publicClientKeyPath, privateClient2KeyPath, publicClient2KeyPath, publicServerKeyPath
        Scanner parser = new Scanner(System.in);

        printUsage();

        System.out.print("Insert your name: ");
        String name = parser.nextLine();
        System.out.println("...Registering!...\n");

        client.Register(name);
        while (true) {
            System.out.print("> ");
            String command = parser.nextLine();  // Read user input

            switch (command) {
                case "info", "i":
                    System.out.println("Insert restaurant name:");
                    String rest = parser.nextLine();
                    client.GetInfo(name,rest);
                    break;
                case "reserve", "u":
                    client.Reserve(name);
                    break;
                case "sendVoucher", "v":
                    client.SendVoucher(name);
                    break;
                case "sendReview", "r":
                    client.Review(name);
                    break;
                case "removeReview", "d":
                    client.DeleteReview(name);
                    break;
                case "help", "h":
                    printUsage();
                    break;
                case "quit", "q":
                    System.out.println("Quiting!");
                    return;
                default:
                    System.out.println("Wrong Command!");
                    break;
            }
        }
    }

    private static void printUsage(){
        System.out.println("\n-----------------------------------------------");
        System.out.println("Available restaurants:\n\tDona_Maria\n\tTasca_do_Chico\n\tBoteco_de_Lavre\n");
        System.out.println("-----------------------------------------------");
        System.out.println("Usage:");
        System.out.println("\tinfo / i: To receive the information of a restaurant!");
        System.out.println("\treserve / u: Reserve a table in a restaurant!");
        System.out.println("\tsendVoucher / v: This operation sends a vouchers to another person!");
        System.out.println("\tsendReview / r: Give a review!");
        System.out.println("\tremoveReview / d: Delete a review!");
        System.out.println("\thelp / h: Print usage!\n");
        System.out.println("\tquit / q: Quit\n");
        System.out.println("-----------------------------------------------\n");
    }
}