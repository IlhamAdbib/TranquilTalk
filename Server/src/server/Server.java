package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class Server{
    
    //////////////HERE
    private static SecretKey serverPublicKeyy;
    private static PublicKey serverPublicKey;
    private static PrivateKey serverPrivateKey;
    
    //////////////HERE
     public static SecretKey getServerkey(){
    
       return serverPublicKeyy;
    }
	public static void main(String[] args) {
            
        ServerSocket connSocket=null;
        try {
            connSocket = new ServerSocket(5059); // Example port
            System.out.println("Server is running and listening on port 5059");
        } catch (IOException e) {
            System.out.println("Erreur de connexion: " + e.getMessage());
            return;
        }
        
        //////////////HERE
            DESencryption des=new DESencryption();
            
            try {
            serverPublicKeyy = des.generateSecretKey();
            System.out.println("Server public key: " + serverPublicKeyy);
            // Génération des clés RSA
                try {
                    KeyPair keyPair = RSAKeyGenerator.generateKeyPair();
                    serverPublicKey = keyPair.getPublic();
                    serverPrivateKey = keyPair.getPrivate();
                    System.out.println("server public key of RSA Encryption"+ serverPublicKey);
                      System.out.println("server private key of RSA Encryption"+ serverPrivateKey);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return;
                }
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            // return;
        }
            
            Profile.inintaliseProfiles();
            while (true) {
                Socket commSocket = null;
                try {
                    commSocket = connSocket.accept();
                    System.out.println("Nouvelle connection : " + commSocket);
                    DataInputStream dis = new DataInputStream(commSocket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(commSocket.getOutputStream());
                    //read client public key
                      byte[] clientPublicKeyBytes = new byte[dis.readInt()];
                      dis.readFully(clientPublicKeyBytes);
                      SecretKey clientPublicKey = DESencryption.generateSecretKey();
                      
                      System.out.println("client public key of DES Encryption"+ clientPublicKey);
                      
                      System.out.println("server public key of RSA Encryption"+ serverPublicKey);
                      System.out.println("server private key of RSA Encryption"+ serverPrivateKey);
                      
                    //send server public key to client
                    byte[] serverPublicKeyBytes = serverPublicKeyy.getEncoded();
                    dos.writeInt(serverPublicKeyBytes.length);
                    dos.write(serverPublicKeyBytes);
                    Thread t = new GestionClient(commSocket, dis, dos, clientPublicKey, serverPublicKey,  serverPrivateKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
	}
}