package org.example;

import com.sun.net.httpserver.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class HttpsServerConnections {
    private final ContactDBandPrepareResponse _contactDBandPrepareResponse;

    private static final String KEYSTORE_FILE = "Server_BackEnd/src/main/resources/server.p12";
    private static final String KEYSTORE_PASSWORD = "changeme";
    public HttpsServerConnections(String ip, int port, String userName, String password, String privateServerKeyPath, String publicServerKeyPath, String publicClientKeyPath, String publicClient2KeyPath) {
        // ira guardar de alguma maneira ip e port
        _contactDBandPrepareResponse = new ContactDBandPrepareResponse(ip, port, userName, password, privateServerKeyPath, publicServerKeyPath, publicClientKeyPath, publicClient2KeyPath);
    }
    public void loop(int port) {
        // ira fazer o loop de pedidos
        try {
            char[] password = KEYSTORE_PASSWORD.toCharArray();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(KEYSTORE_FILE);
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the HTTPS context and parameters
            SSLContext sslContext = SSLContext.getInstance("TLS");

            //System.setProperty("https.protocols", "TLSv1");
            sslContext.init(kmf.getKeyManagers(), null, null);

            HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 0);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext){
                @Override
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = getSSLContext();
                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });

            server.createContext("/register", new RegisterHandler());
            server.createContext("/restInfo", new RestInfoHandler());
            server.createContext("/sendVoucher", new SendVoucher());
            server.createContext("/useVoucher", new UseVoucher());
            server.createContext("/sendReview", new SendReview());
            server.createContext("/removeReview", new RemoveReview());
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("SERVER OPEN " + port);
            while(true);
        } catch (KeyManagementException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }
    }
    private static Map<String, String> parseQuery(String query) {
        System.out.println(query);
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            System.out.println(param);
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
    /********************************************REGISTER*********************************************************/
    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestedUri = t.getRequestURI();
            String query = requestedUri.getRawQuery();
            Map<String, String> parameters = parseQuery(query);

            int suc = _contactDBandPrepareResponse.SendRegisterRequest(
                    parameters.get("user")
            );
            String response;
            OutputStream os = null;
            if(suc != -1){
                t.sendResponseHeaders(200, Integer.toString(suc).length());
                os = t.getResponseBody();
                os.write(Integer.toString(suc).getBytes());
            }
            else {
                t.sendResponseHeaders(400, 0);
                os = t.getResponseBody();
            }
            os.close();
        }
    }
    /********************************************RESTINFO*********************************************************/
    class RestInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            URI requestedUri = t.getRequestURI();
            String query = requestedUri.getRawQuery();
            Map<String, String> parameters = parseQuery(query);

            String[] suc = _contactDBandPrepareResponse.SendRestInfoRequest(
                    parameters.get("user"),
                    parameters.get("restName"),
                    Integer.parseInt(parameters.get("counter"))
            );
            if (suc[0]==null) {
                OutputStream os = null;
                if(suc[1] == null){
                    t.sendResponseHeaders(400, 0);
                    os = t.getResponseBody();
                }
                else{
                    t.sendResponseHeaders(401, suc[1].length());
                    os = t.getResponseBody();
                    os.write(suc[1].getBytes());
                }
                os.close();
                return;
            }// localhost:5000/restInfo/?user=trolha&restName=Dona_Maria&counter=0

            t.sendResponseHeaders(200, suc[0].length() + 1);
            OutputStream os = null;
            try {
                os = t.getResponseBody();
                byte[] responseBytes = suc[0].getBytes();
                int offset = 0;
                while (offset < responseBytes.length) {
                    int chunkSize = Math.min(1024, responseBytes.length - offset);
                    os.write(responseBytes, offset, chunkSize);
                    offset += chunkSize;
                }
            } catch (IOException e) {
                // Handle exception
            } finally {
                os.close();
            }
        }
    }
    /********************************************SENDVOUCHER*********************************************************/
    class SendVoucher implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestedUri = t.getRequestURI();
            String query = requestedUri.getRawQuery();
            Map<String, String> parameters = parseQuery(query);

            int[] suc = _contactDBandPrepareResponse.SendVoucher(
                    parameters.get("userSrc"),
                    parameters.get("userDst"),
                    Integer.parseInt(parameters.get("counter")),
                    Integer.parseInt(parameters.get("voucherID"))
            );
            String response;
            OutputStream os = null;
            if (suc[0] != -1) {
                response = Integer.toString(suc[1]);
                t.sendResponseHeaders(200, response.length());
                os = t.getResponseBody();
                os.write(response.getBytes());
            } else {

                response = Integer.toString(suc[1]);
                if(suc[1] == -1){
                    t.sendResponseHeaders(400, 0);
                    os = t.getResponseBody();
                }
                else{
                    t.sendResponseHeaders(401, response.length());
                    os = t.getResponseBody();
                    os.write(response.getBytes());
                }
            }
            os.close();
        }
    }
    /********************************************USEVOUCHER*********************************************************/
    class UseVoucher implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestedUri = t.getRequestURI();
            String query = requestedUri.getRawQuery();
            Map<String, String> parameters = parseQuery(query);

            int[] suc = _contactDBandPrepareResponse.useVoucher(
                    parameters.get("user"),
                    Integer.parseInt(parameters.get("counter")),
                    Integer.parseInt(parameters.get("voucherID"))
            );
            String response;
            OutputStream os = null;
            if (suc[0] != -1) {
                response = Integer.toString(suc[1]);
                t.sendResponseHeaders(200, response.length());
                os = t.getResponseBody();
                os.write(response.getBytes());
            } else {

                response = Integer.toString(suc[1]);
                if(suc[1] == -1){
                    t.sendResponseHeaders(400, 0);
                    os = t.getResponseBody();
                }
                else{
                    t.sendResponseHeaders(401, response.length());
                    os = t.getResponseBody();
                    os.write(response.getBytes());
                }
            }
            os.close();
        }
    }
    /********************************************SENDREVIEW*********************************************************/
    class SendReview implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestedUri = t.getRequestURI();
            String query = requestedUri.getRawQuery();
            Map<String, String> parameters = parseQuery(query);

            int[] suc = _contactDBandPrepareResponse.SendReview(
                    parameters.get("user"),
                    parameters.get("restName"),
                    Integer.parseInt(parameters.get("counter")),
                    Integer.parseInt(parameters.get("reviewVal")),
                    parameters.get("reviewDescription"),
                    parameters.get("validation")
            ); // http://localhost:5000/sendReview/?user=trolha&restName=Dona_Maria&counter=0&reviewVal=5&reviewDescription=ATUAMAE&validation=naovaifuncionaja
            String response;
            OutputStream os = null;
            if (suc[0] != -1) {
                response = Integer.toString(suc[1]);
                t.sendResponseHeaders(200, response.length());
                os = t.getResponseBody();
                os.write(response.getBytes());
            } else {

                response = Integer.toString(suc[1]);
                if(suc[1] == -1){
                    t.sendResponseHeaders(400, 0);
                    os = t.getResponseBody();
                }
                else{
                    t.sendResponseHeaders(401, response.length());
                    os = t.getResponseBody();
                    os.write(response.getBytes());
                }
            }
            os.close();
        }
    }
    /********************************************REMOVEREVIEW*********************************************************/
    class RemoveReview implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestedUri = t.getRequestURI();
            String query = requestedUri.getRawQuery();
            Map<String, String> parameters = parseQuery(query);

            int[] suc = _contactDBandPrepareResponse.removeReview(
                    parameters.get("user"),
                    Integer.parseInt(parameters.get("counter")),
                    Integer.parseInt(parameters.get("reviewID"))
            );
            String response;
            OutputStream os = null;
            if (suc[0] != -1) {
                response = Integer.toString(suc[1]);
                t.sendResponseHeaders(200, response.length());
                os = t.getResponseBody();
                os.write(response.getBytes());
            } else {

                response = Integer.toString(suc[1]);
                if(suc[1] == -1){
                    t.sendResponseHeaders(400, 0);
                    os = t.getResponseBody();
                }
                else{
                    t.sendResponseHeaders(401, response.length());
                    os = t.getResponseBody();
                    os.write(response.getBytes());
                }
            }
            os.close();
        }
    }
}
