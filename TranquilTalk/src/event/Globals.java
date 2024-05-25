package event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import main.DESencryption;

public class Globals {

    /**
     * @return the myEmail
     */
    public static String getMyEmail() {
        return myEmail;
    }

    /**
     * @param aMyEmail the myEmail to set
     */
    public static void setMyEmail(String aMyEmail) {
        myEmail = aMyEmail.toLowerCase();
    }
    
    public static Globals instance;
    public static Socket socket;
    public static InetAddress ip;
    public static DataInputStream dis;
    public static DataOutputStream dos;
    public static Lock myWriteLock;
    private static String myEmail;
    private static SecretKey clientpublickey;
    private static SecretKey serverPublicKey;
    private static PublicKey clientRSAPublicKey;
    private static PrivateKey clientRSAPrivateKey;
    private static PublicKey serverRSAPublicKey;
    private static KeyPair clientRSAKeyPair;

    public static void initGlobals(){
        try {
            ip = InetAddress.getByName("localhost");
            socket = new Socket(ip, 5059);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            myWriteLock = new ReentrantLock();

            // DES Encryption
            DESencryption des = new DESencryption();
            try {
                clientpublickey = des.generateSecretKey();
                System.out.println("client public key of DES Encryption: " + clientpublickey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] clientPublicKeyBytes = clientpublickey.getEncoded();
            dos.writeInt(clientPublicKeyBytes.length);
            dos.write(clientPublicKeyBytes);
            // Receive server's public key
            byte[] serverPublicKeyBytes = new byte[dis.readInt()];
            dis.readFully(serverPublicKeyBytes);
            serverPublicKey = des.convertToSecretKeyFromBytes(serverPublicKeyBytes);
            System.out.println("server public key of DES Encryption: " + serverPublicKey);

            // RSA Encryption
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            clientRSAKeyPair = keyGen.generateKeyPair();
            clientRSAPublicKey = clientRSAKeyPair.getPublic();
            System.out.println("client public key of RSA Encryption: " + clientRSAPublicKey);

            clientRSAPrivateKey = clientRSAKeyPair.getPrivate();
            System.out.println("client private key of RSA Encryption: " + clientRSAPrivateKey);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }

    public static SecretKey getServerkey(){
       return serverPublicKey;
    }

    public static SecretKey getClientkey(){
       return clientpublickey;
    }

    public static PublicKey getServerRSAPublicKey() {
        return serverRSAPublicKey;
    }

    public static KeyPair getClientRSAKeyPair() {
        return clientRSAKeyPair;
    }

    private Globals() {
    }
}
