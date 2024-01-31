package org.example;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.*;

public class CryptoLibrary {
    ///////////////////////////////////////////////////////////////////////// General-func ///////////////////////////////////////////////////////////////////////////////////
    // PRIV/PUB KEYS READ
    public static PublicKey readPublicKeyB64(String publicKeyB64) {
        try{
            byte[] pubEncoded = Base64.getDecoder().decode(publicKeyB64) ;
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicK = keyFactory.generatePublic(pubSpec);
            return publicK;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static PublicKey readPublicKey(String publicKeyPath) {
        try{
            byte[] pubEncoded = readFile(publicKeyPath);
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicK = keyFactory.generatePublic(pubSpec);
            return publicK;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static PrivateKey readPrivateKey(String privateKeyPath) {
        try{
            byte[] privEncoded = readFile(privateKeyPath);
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priv = keyFactory.generatePrivate(privSpec);
            return priv;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static byte[] readFile(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }
    // JSON TO BYTES 
    private static byte[] jsonToBytes (JsonObject json) {
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }
    private static byte[] jsonToBytes (JsonArray json) {
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    ///////////////////////////////////////////////////////////////////////// Protect-Subfunc ///////////////////////////////////////////////////////////////////////////////////

    // SYM KEY
    private static Key genarateSymKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		    keyGen.init(128);
		    Key key = keyGen.generateKey();
            return key;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    // DIGITAL SIGNATURE
    public static String digitalSignature(JsonObject json, PrivateKey privateKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(jsonToBytes(json));
            byte[] signature = sig.sign();
            return signatureToBase64(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String signatureToBase64(byte[] signature) {
        return Base64.getEncoder().encodeToString(signature);
    }
    // IV
    private static byte[] generateIV () {
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
    // CIPHERS 
    private static String cipherMessage (byte[] json, Key symKey, byte[] ivB) {
		try{
            IvParameterSpec iv = new IvParameterSpec(ivB);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, symKey, iv);
            byte[] enc = cipher.doFinal(json);
            String encText = Base64.getEncoder().encodeToString(enc);
        return encText;
        } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String cipherSymKey (Key symKey, PublicKey publicKey) {
        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","SunJCE");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            //testar se o getEncoded funciona
            return Base64.getEncoder().encodeToString(cipher.doFinal(symKey.getEncoded()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String cipherIV (byte[] iv, PublicKey publicKey) {
        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","SunJCE");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(iv));
        } catch (IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchProviderException | NoSuchPaddingException e ){
            e.printStackTrace();
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////// UnProtect-Subfunc ///////////////////////////////////////////////////////////////////////////////////


    private static Key decodeSymKey(String symKeyEnc, PrivateKey privateKey) {
        try {
            byte[] symKeyEncBytes = Base64.getDecoder().decode(symKeyEnc);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","SunJCE");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decripted = cipher.doFinal(symKeyEncBytes);

            SecretKeySpec keySpec = new SecretKeySpec(decripted, "AES");
            return keySpec;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchProviderException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static byte[] decodeIV(String IVEnc, PrivateKey privateKey) {
        try {
            byte[] IVEncBytes = Base64.getDecoder().decode(IVEnc);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","SunJCE");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] IV = cipher.doFinal(IVEncBytes);

            return IV;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException  e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonArray decriptVouchers(String jsonEnc, Key symKey, byte[] IVB) {
        try{
            byte[] decoded = Base64.getDecoder().decode(jsonEnc);
            //decrypt
            IvParameterSpec iv = new IvParameterSpec(IVB);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, symKey, iv);
            byte[] decipherBytes = cipher.doFinal(decoded);
            return new JsonParser().parse(new String(decipherBytes, StandardCharsets.UTF_8)).getAsJsonArray();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException  e) {
            e.printStackTrace();
            return null;
        }
    }
    private static JsonObject decriptReviewers(String jsonEnc, Key symKey, byte[] IVB) {
        try{
            byte[] decoded = Base64.getDecoder().decode(jsonEnc);
            //decrypt
            IvParameterSpec iv = new IvParameterSpec(IVB);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, symKey, iv);
            byte[] decipherBytes = cipher.doFinal(decoded);
            return new JsonParser().parse(new String(decipherBytes, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException  e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static JsonObject decodeJsonObject(String jsonString) {
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

    ///////////////////////////////////////////////////////////////////////// Check-Subfunc ///////////////////////////////////////////////////////////////////////////////////

    private static boolean verifyDigitalSignature(String receivedSignature, byte[] bytes, PublicKey pubKey) {
		try {
            byte[] DSbytes = Base64.getDecoder().decode(receivedSignature);
            // verify the signature with the public key
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(bytes);
			return sig.verify(DSbytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException se) {
			System.err.println("Caught exception while verifying " + se);
			return false;
		}
	}

    public static void verifyReviews(JsonObject json, JsonObject reviewers) {
        JsonArray reviews = json.getAsJsonObject("restaurantInfo").getAsJsonArray("reviews");
        for (JsonElement jsonElement : reviews) {
            JsonObject review = jsonElement.getAsJsonObject();
            String name = review.get("clientName").getAsString();
            String validation = review.get("validation").getAsString();
            int id = review.get("id").getAsInt();
            String keyB64 = reviewers.get(name).getAsString();
            if (keyB64 == null){
                review.remove("validation");
                review.addProperty("validation", "Not Verified");
                continue;
            }
            review.remove("validation");
            review.remove("id");
            try {
                byte[] pubEncoded = Base64.getDecoder().decode(keyB64);
                X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicK = keyFactory.generatePublic(pubSpec);

                boolean suc = verifyDigitalSignature(validation, jsonToBytes(review), publicK);
                if(suc){
                    review.addProperty("validation", "Verified");
                    review.addProperty("id", id);
                    continue;
                }
                review.addProperty("validation", "Not Verified");

            } catch (IllegalArgumentException | InvalidKeySpecException | NoSuchAlgorithmException e) {
                review.addProperty("validation", "Not Verified");
            }
            review.addProperty("id", id);
        }
    }

    public static boolean verifyReview(JsonObject review, String validation, String keyB64) {
        if (keyB64 == null){
            return false;
        }
        try {
            byte[] pubEncoded = Base64.getDecoder().decode(keyB64);
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicK = keyFactory.generatePublic(pubSpec);

            boolean suc = verifyDigitalSignature(validation, jsonToBytes(review), publicK);
            if(suc){
                return true;
            }

        } catch (IllegalArgumentException | InvalidKeySpecException | NoSuchAlgorithmException ignored) {}
        return false;
    }
    ///////////////////////////////////////////////////////////////////////// Protect ///////////////////////////////////////////////////////////////////////////////////
    public static String Protect(JsonObject json, int counter, String pathPrivServerKey, String pathPubClientKey, boolean terminal) {
        PublicKey publicKey= null;
        if(terminal){
            publicKey = readPublicKey(pathPubClientKey);
        }
        else{
            publicKey = readPublicKeyB64(pathPubClientKey);
        }
        PrivateKey privateKey = readPrivateKey(pathPrivServerKey);
        Key symKey = genarateSymKey();
        byte[] ivB = generateIV();

        JsonArray vouchers = json.get("vouchers").getAsJsonArray();
        JsonObject validation_Keys = json.get("reviewers").getAsJsonObject();
        // add counter
        JsonObject auth = new JsonObject();
        auth.addProperty("counter", counter);
        json.add("auth", auth);
        // Remove from msg vouchers
        json.remove("vouchers");
        json.add("vouchers",vouchers);
        // Remove from msg validation_Keys
        json.remove("reviewers");
        json.add("reviewers",validation_Keys);
        //create and add DS
        String ds = digitalSignature(json,privateKey);
        json.get("auth").getAsJsonObject().addProperty("DS", ds);

        // Remove from msg
        json.remove("vouchers");

        //Encript everything needed
        String encIV = cipherIV(ivB, publicKey);
        String encText = cipherMessage(jsonToBytes(vouchers), symKey, ivB);
        String encReviewKeys = cipherMessage(jsonToBytes(validation_Keys), symKey, ivB);
        String encSymKey = cipherSymKey (symKey, publicKey);

        //Add the vouchers back to the json, but encripted and in base64
        json.addProperty("vouchers", encText);
        json.addProperty("reviewers", encReviewKeys);
        json.get("auth").getAsJsonObject().addProperty("encIV", encIV);
        json.get("auth").getAsJsonObject().addProperty("encSymKey", encSymKey);

        return json.toString();
    }


    ///////////////////////////////////////////////////////////////////////// UnProtect ///////////////////////////////////////////////////////////////////////////////////


    public static JsonObject Unprotect(String jsonString, int counter, String privatePathString, String publicPathKey) {
        PrivateKey privateKey = readPrivateKey(privatePathString);
        JsonObject json = decodeJsonObject(jsonString);
        
        String encSymKey = json.get("auth").getAsJsonObject().get("encSymKey").getAsString();
        String ivCrypted = json.get("auth").getAsJsonObject().get("encIV").getAsString();
        String DS = json.get("auth").getAsJsonObject().get("DS").getAsString();
        String voucherEnc = json.get("vouchers").getAsString();
        String validation_KeysEnc = json.get("reviewers").getAsString();

        json.remove("vouchers");
        json.remove("reviewers");
        json.get("auth").getAsJsonObject().remove("DS");
        json.get("auth").getAsJsonObject().remove("encIV");
        json.get("auth").getAsJsonObject().remove("encSymKey");

        Key symKey = decodeSymKey(encSymKey, privateKey);
        byte[] IV = decodeIV(ivCrypted, privateKey);


        JsonArray vouchers = decriptVouchers(voucherEnc, symKey, IV);

        json.add("vouchers", vouchers);

        JsonObject validation_Keys= decriptReviewers(validation_KeysEnc, symKey, IV);
        json.add("reviewers", validation_Keys);

        if (!Check(json, DS, counter, publicPathKey)){
            return null;
        }
        //json.remove("validation_Keys");
        return json;
    }
    ///////////////////////////////////////////////////////////////////////// Check ///////////////////////////////////////////////////////////////////////////////////

    public static Boolean Check(JsonObject json, String DS, int counter, String publicPathKey) {
        PublicKey publicKey = readPublicKey(publicPathKey);
        if (counter != json.get("auth").getAsJsonObject().get("counter").getAsInt()){
            return false;
        }
        boolean verified =  verifyDigitalSignature (DS, jsonToBytes(json), publicKey);
        verifyReviews(json, json.getAsJsonObject("reviewers"));
        return verified;
    }
}

