package org.example;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.io.FileInputStream;

public class HttpsClient {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String KEYSTORE_FILE = "Client/src/main/resources/user.p12";
    private static final String TRUSTSTORE_FILE = "Client/src/main/resources/usertruststore.jks";
    private static final String KEYSTORE_PASSWORD = "changeme";

    // HTTP GET request
    public String[] sendGet(String url) throws Exception {
        // Load the keystore
        char[] password = KEYSTORE_PASSWORD.toCharArray();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_FILE)) {
            ks.load(fis, password);
        }
        KeyStore ks2 = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(TRUSTSTORE_FILE)) {
            ks2.load(fis, password);
        }
        // Setup the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password);

        // Initialize the trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks2);

        // Setup the HTTPS context and parameters
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setSSLSocketFactory(sslContext.getSocketFactory());

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in;
        if (200 <= responseCode && responseCode <= 299) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        String inputLine;
        StringBuffer response = new StringBuffer(); //dasdasdasdasdasasdsada-------------------------------------------

        int i = 0;
        String[] list = new String[10];
        list[0] = Integer.toString(responseCode);
        while ((inputLine = in.readLine()) != null) {
            if (i == 0){

                System.out.println(inputLine +  "\n");
            }
            list[i+1] = inputLine;
            i++;
        }
        in.close();

        return list;
    }
}