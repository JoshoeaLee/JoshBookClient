package Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/**
 * METHODS INVOLVING ENCRYPTION AND DECRYPTION ARE TAKEN FROM ALI'S CODE.
 * I added on the methods involving the 'Server Public Key'.
 */
public class EncryptionHandler {
    
    /**
     * Private Key that I have
     */
    private SecretKey sessionKey; 
    public PublicKey serverPublicKey;
    
    
    public EncryptionHandler() throws NoSuchAlgorithmException 
    {
    }
    
    
  
    /**
     * Generates an AES session key and sets it.
     * @return the AES session key generated
     */
    public SecretKey generateKey() throws NoSuchAlgorithmException 
    {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        this.setSecretkey(keyGen.generateKey());       
        return this.sessionKey;
         
    }

    
  
           
    /**
     * Reads in the server public key I have made and sets it.
     * @throws FileNotFoundException
     */
    public void getPublicServerKey() throws FileNotFoundException{
        File file = new File( "./lib/serverPublicKey.txt");
        Scanner sc = new Scanner(file);
        String serverPublicKey = sc.nextLine();
        sc.close();
        byte[] serverPublic = Base64.getMimeDecoder().decode(serverPublicKey);
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(serverPublic));
            this.setServerPublicKey(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    

    /**
     * Encrypts a message using the AES Session Key set
     * @param strDataToEncrypt The message to encrypt
     * @return A byte array of the encrypted message
     * @author ALI AHMED
     */
    public byte[] encrypt (String strDataToEncrypt) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException
    {
        Cipher aesCipher = Cipher.getInstance("AES"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!
        aesCipher.init(Cipher.ENCRYPT_MODE, this.getSecretkey());
        byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
        byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt);       
        return byteCipherText;
    }

    /**
     * Encrypts a string message into a byte array using THE PUBLIC SERVER KEY
     * @param strCipherText
     * @return byte array which is encrypted.
     * @author ALI AHMED 
     */  
    public byte[] encryptUsingServerPublic (String strDataToEncrypt) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException, FileNotFoundException
    {
        if(serverPublicKey==null){
            this.getPublicServerKey();
        }
        Cipher rsaCipher = Cipher.getInstance("RSA"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!
        rsaCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
        byte[] byteCipherText = rsaCipher.doFinal(byteDataToEncrypt);       
        return byteCipherText;
    }

    
    /**
     * Decrypts a byte array into a string using the session key
     * @param strCipherText
     * @return String message which is decrypted
     * @author ALI AHMED
     */
    public String decrypt (byte[] strCipherText) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException
    {        
        Cipher aesCipher = Cipher.getInstance("AES"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!				
        aesCipher.init(Cipher.DECRYPT_MODE, this.getSecretkey());        
        byte[] byteDecryptedText = aesCipher.doFinal(strCipherText);        
        return new String(byteDecryptedText);
    }   

    //////GETTERS AND SETTERS
    public SecretKey getSecretkey() {
        return sessionKey;
    }

    public void setSecretkey(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    public PublicKey getServerPublicKey(){
        return serverPublicKey;
    }

    public void setServerPublicKey(PublicKey serverKey){
        this.serverPublicKey = serverKey;
    }
}
