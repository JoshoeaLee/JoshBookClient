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
 *
 * @author ahmed
 */
public class AES {
    
    /**
     * Private Key that I have
     */
    private SecretKey secretkey; 
    
    public PublicKey serverPublicKey;
    
    
    public AES() throws NoSuchAlgorithmException 
    {
    }
    
    
    /**
	* Step 1. Generate a AES key using KeyGenerator 
    */
    
    public SecretKey generateKey() throws NoSuchAlgorithmException 
    {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        this.setSecretkey(keyGen.generateKey());       
        return this.secretkey;
         
    }

    
    /**
     * Retrieves the publicServerKey
     * @return
     * @throws FileNotFoundException
     */
        public void getPublicServerKey() throws FileNotFoundException{
            File file = new File( "./lib/serverPublicKey.txt");
        Scanner sc = new Scanner(file);
        String serverPublicKey = sc.nextLine();
        sc.close();
        System.out.println("Printing out public key");
        System.out.println(serverPublicKey);
        byte[] serverPublic = Base64.getMimeDecoder().decode(serverPublicKey);
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(serverPublic));
            this.setServerPublicKey(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    

    
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

      
    public byte[] encryptUsingServerPublic (String strDataToEncrypt) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException, FileNotFoundException
    {
        if(serverPublicKey==null){
            this.getPublicServerKey();
System.out.println("GotServerKey!");
        }
        Cipher rsaCipher = Cipher.getInstance("RSA"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!
        rsaCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
        byte[] byteCipherText = rsaCipher.doFinal(byteDataToEncrypt);       
        return byteCipherText;
    }

    
    
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

    /**
     * @return the secretkey
     */
    public SecretKey getSecretkey() {
        return secretkey;
    }

    /**
     * @param secretkey the secretkey to set
     */
    public void setSecretkey(SecretKey secretkey) {
        this.secretkey = secretkey;
    }

    public PublicKey getServerPublicKey(){
        return serverPublicKey;
    }

    public void setServerPublicKey(PublicKey serverKey){
        this.serverPublicKey = serverKey;
    }
}
