
package main;

import component.client.List;
// import component.client.secondaries.Call_Main;
import event.EventImageView;
import event.EventOpenFile;
import event.PublicEvent;
import java.awt.Desktop;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import event.Globals;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;


public class MainClient extends javax.swing.JFrame {

    private int num;
    public MainClient(/*int num1*/) {
        //this.num=num1;
        initComponents();
        Backround.setBackground(new java.awt.Color(51, 122, 44));
        init();
        Thread ths = new Thread(() -> {
            boolean isWorking=true;
            String timeSent;
            String sender;
            String receiver;
            int lengthCheck;
            String msg;
            
            while(isWorking){
                try{   
                  
                    if (Globals.socket == null || Globals.socket.isClosed()) {
                        isWorking = false; // Sortir de la boucle
                        break;
                    }   
                    
                    msg = Globals.dis.readUTF();
                    String strArray[]=msg.split("@@@");
                    System.out.println(msg);
                    if(strArray.length>2){
                        if(strArray[1].equals("Group")){
                            sender=strArray[3];
                            timeSent=strArray[4].replace(' ', '-').replace(':','-');
                            receiver="Group "+strArray[2];
                        }
                        else{
                            sender=strArray[1].toLowerCase();
                            timeSent=strArray[2].replace(' ', '-').replace(':','-');
                            receiver=Globals.getMyEmail();
                        }
                        switch (strArray[0]) {
                            case "text":
                                if(strArray[1].equals("Group")){
                                    if(home.chat.CTitle.getUserName().equals(sender)){
                                        home.addMessage("Groupe "+strArray[2], "text", strArray[3], strArray[4], strArray[5]);
                                        home.list.pushConversationUp(sender);
                                    }
                                    else
                                        home.list.newMessage(sender);
                                    updateConversationFile(sender, receiver, timeSent, "text", strArray[5]);
                                }
                                else{
                                    if(home.chat.CTitle.getUserName().equals(sender)){
                                        home.list.pushConversationUp(sender);
                                        home.addMessage(strArray[1], "text", strArray[1].toLowerCase(), strArray[2], strArray[3]);
                                    }
                                    else
                                        home.list.newMessage(sender);
                                    if(!strArray[2].toLowerCase().equals(Globals.getMyEmail()))
                                        updateConversationFile(sender, receiver, timeSent, "text", strArray[3]);
                                }   break;
                            case "image":
                                {
                                    int len = Globals.dis.readInt();
                                    byte[] image = new byte[len];
                                    Globals.dis.readFully(image);
                                    BufferedImage bfimg = ImageIO.read(new ByteArrayInputStream(image));
                                    String imageFileName;
                                    imageFileName="Images/"+sender+" "+strArray[strArray.length-1];
                                    File outputfile = new File(imageFileName);
                                    outputfile.createNewFile();
                                    ImageIO.write(bfimg, "png", outputfile);
                                    if(home.chat.CTitle.getUserName().equals(sender)){
                                        home.list.pushConversationUp(sender);
                                        if(strArray[1].equals("Group"))
                                            home.addMessage(receiver, "image", sender, timeSent, imageFileName);
                                        else
                                            home.addMessage(sender, "image", sender, timeSent, imageFileName);
                                    }
                                    else
                                        home.list.newMessage(sender);
                                    updateConversationFile(sender, receiver, timeSent, "image", imageFileName);
                                    break;
                                }
                            case "audio":
                                {
                                    int len = Globals.dis.readInt();
                                    byte[] audio = new byte[len];
                                    Globals.dis.readFully(audio);
                                    String audioFileName="Audios/"+sender+" "+timeSent+".wav";
                                    FileOutputStream fout = new FileOutputStream(audioFileName);
                                    updateConversationFile(sender, receiver , timeSent, "audio", audioFileName);
                                    fout.write(audio);
                                    if(home.chat.CTitle.getUserName().equals(sender)){
                                        home.list.pushConversationUp(sender);
                                        if(strArray[1].equals("Group"))
                                            home.addMessage(receiver, "audio", sender, timeSent, audioFileName);
                                        else
                                            home.addMessage(sender, "audio", sender, timeSent, audioFileName);
                                    }
                                    else
                                        home.list.newMessage(sender);
                                    break;
                                }
                            case "file":
                                {
                                    int len = Globals.dis.readInt();
                                    byte[] file = new byte[len];
                                    Globals.dis.readFully(file);
                                    String fileName;
                                    fileName="Files/"+sender+" "+strArray[strArray.length-1];
                                    FileOutputStream fout = new FileOutputStream(fileName);
                                    updateConversationFile(sender, receiver , timeSent, "file", fileName);
                                    fout.write(file);
                                    if(home.chat.CTitle.getUserName().equals(sender)){
                                        home.list.pushConversationUp(sender);
                                        if(strArray[1].equals("Group"))
                                            home.addMessage(receiver, "file", sender, timeSent, fileName);
                                        else
                                            home.addMessage(sender, "file", sender, timeSent, fileName);
                                    }
                                    else
                                        home.list.newMessage(sender);
                                    break;
                                }
                                /*
                            case "videoCall":
                                {
                                    int len = Globals.dis.readInt();
                                    byte[] image = new byte[len];
                                    Globals.dis.readFully(image);
                                    BufferedImage bfimg = ImageIO.read(new ByteArrayInputStream(image));
                                    home.chat.CBottom.cm.setFrendPic(bfimg);
                                    break;
                                }
                            
                            case "answer":
                                {
                                    if(strArray[2].equals("refuse"))
                                        home.chat.CBottom.cm.callEnded();
                                    else
                                        home.chat.CBottom.cm.callStarted();
                                }*/
                            default:
                                break;
                        }
                    }
                    else{
                        switch (strArray[0]) {
                            case "connection":
                                List.connection(strArray[1], true);
                                //home.list.addPeople(strArray[1], strArray[2]);
                                break;
                            case "disconnection":
                                List.connection(strArray[1], false);
                                //home.list.addPeople(strArray[1], strArray[2]);
                                break;
                                /*
                            case "call":
                                {
                                    if(JOptionPane.showConfirmDialog(null, strArray[1]+"is Calling you, do you wish to accept?")==JOptionPane.YES_OPTION){
                                        Globals.dos.writeUTF("answer@@@"+strArray[1]+"@@@accept");
                                        home.chat.CBottom .cm=new Call_Main(strArray[1], false);
                                    }
                                    else
                                        Globals.dos.writeUTF("answer@@@"+strArray[1]+"@@@refuse");
                                }*/
                            case "exist"://confirm if contact you want to add exist in db
                                try{
                                    File myObj = new File("Conversations/"+Globals.getMyEmail()+" "+strArray[1]+".txt");
                                    if(myObj.createNewFile()){
                                        //home.list.showPeople();
                                        home.list.addConversation(strArray[1]);
                                    }
                                }
                                catch(Exception e2){
                                    e2.printStackTrace();
                                }   break;
                            case "notExist":
                                JOptionPane.showMessageDialog(null,"Email not found");
                                break;
                            default:
                                break;
                        }
                    }
                }catch (java.net.SocketException se) {
        if (Globals.socket == null || Globals.socket.isClosed()) {
            isWorking = false; // Sortir de la boucle si le socket est fermé
            break;
        } else {
            // Gérer d'autres erreurs de socket
            se.printStackTrace();
        }
    }catch(Exception e){
                    e.printStackTrace();
                }
                // Sortir de la boucle si le socket est fermé
                // Gérer d'autres erreurs de socket

            }
        });
        ths.start();    
    }
    
public static void updateConversationFile(String sender, String receiver, String clock, String type, String fileName) {
    try {
        FileWriter fw;
        String myEmail = Globals.getMyEmail().toLowerCase();
        String conversationFileName;
        
        // Determine the correct file path based on sender and receiver
        if (myEmail.equals(receiver.toLowerCase())) {
            conversationFileName = "Conversations/" + myEmail + " " + sender + ".txt";
            File myObj = new File(conversationFileName);
            myObj.createNewFile();
            
            // Decrypt the fileName
            // Decrypt the fileName
fileName = DESencryption.decrypt(fileName, Globals.getClientkey());
if (fileName == null) {
    throw new IllegalArgumentException("Decryption of fileName failed.");
} else {
    System.out.println("Decrypted message: " + fileName);
}

            System.out.println("Decrypted message: " + fileName);
            
            fw = new FileWriter(conversationFileName, true);
        } else {
            conversationFileName = "Conversations/" + myEmail + " " + receiver + ".txt";
            File myObj = new File(conversationFileName);
            myObj.createNewFile();
            fw = new FileWriter(conversationFileName, true);
        }
        
        // BufferedWriter to write data to the file
        BufferedWriter bw = new BufferedWriter(fw);
        
        // Encrypt the fileName
        String encryptedFileName = DESencryption.encrypt(fileName, Globals.getClientkey());
        if (encryptedFileName == null) {
            throw new IllegalArgumentException("Encryption of fileName failed.");
        }
        System.out.println("Encrypted message: " + encryptedFileName);
        
        // Convert secret key to string
        String key = DESencryption.convertSecretKeyToString(Globals.getClientkey());
        
        // Write data to the file
        bw.write(type + "@@@" + sender + "@@@" + clock + "@@@" + encryptedFileName + "@@@" + key + "\n");
        bw.close();
        
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error updating conversation file: " + e.getMessage());
    }
}

    private void init(){
        setIconImage(new ImageIcon(getClass().getResource("/icon/Logo.png")).getImage());
        view_Image.setVisible(false);
        home.setVisible(true);
        // home.setBackground(Color.GREEN);
        setTitle("TranquilTalk - "+Globals.getMyEmail());
        initEvent();
    }
    private void initEvent(){
        PublicEvent.getInstance().addEventImageView(new EventImageView() {
            @Override
            public void viewImage(Icon image) {
                view_Image.viewImage(image);
            }  
        });
        PublicEvent.getInstance().addEventOpenFile(new EventOpenFile() {
            @Override
            public void openFile(String path) {
                try{
                    File file = new File(path);
                    Desktop.getDesktop().open(file);
                }catch(Exception e){
                    e.printStackTrace(); 
                }
            }
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Backround = new javax.swing.JPanel();
        body = new javax.swing.JLayeredPane();
        view_Image = new component.client.View_Image();
        home = new component.client.Home();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(950, 640));
        setPreferredSize(new java.awt.Dimension(900, 600));

        Backround.setBackground(new java.awt.Color(129, 167, 214));
        Backround.setPreferredSize(new java.awt.Dimension(950, 600));

        body.setLayout(new java.awt.CardLayout());
        body.setLayer(view_Image, javax.swing.JLayeredPane.POPUP_LAYER);
        body.add(view_Image, "card3");
        body.add(home, "card3");

        javax.swing.GroupLayout BackroundLayout = new javax.swing.GroupLayout(Backround);
        Backround.setLayout(BackroundLayout);
        BackroundLayout.setHorizontalGroup(
            BackroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(body, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        BackroundLayout.setVerticalGroup(
            BackroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(body, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Backround, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Backround, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainClient(/*socket*/).setVisible(true);
            } 
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Backround;
    private javax.swing.JLayeredPane body;
    private component.client.Home home;
    private component.client.View_Image view_Image;
    // End of variables declaration//GEN-END:variables
}
