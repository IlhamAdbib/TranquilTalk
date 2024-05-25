package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.*;

////////HERE
import javax.crypto.SecretKey;

class Profile {

    private int id;
    private String email;
    private String name;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Statement stmt;
    private static Map<Profile, DataOutputStream> mapDos;
    private static Map<Profile, SecretKey> mapkeys;
    private Lock lock;
    private ArrayList<Group> myGroups;
    private static ArrayList<Group> allGroups;
    
    ////////HERE
    private SecretKey clientkey;

    ////////HERE
    public Profile(int id, String email, String name, DataOutputStream dos, DataInputStream dis, Statement stmt,SecretKey clientPublicKey) {
        this.email = email.toLowerCase();
        this.name = name;
        this.id = id;
        this.dos = dos;
        this.dis = dis;
        this.stmt = stmt;
        this.lock = new ReentrantLock();
        ////////HERE
        this.clientkey = clientPublicKey;

        mapDos.put(this, this.dos);
        ////////HERE
        mapkeys.put(this, clientPublicKey);
        this.myGroups = new ArrayList<>();
        addMyGroups();
    }
    
    
    
    public static Map<Profile, DataOutputStream> getMap(){return mapDos;}
     public static Map<Profile, SecretKey> getkeys(){return mapkeys;}
    public static void inintaliseProfiles() {
        if (mapDos == null) {
            System.out.print("Demarrage ...");
            mapDos = new HashMap<>();
            mapkeys = new HashMap<>();
            allGroups = new ArrayList<>();
            System.out.println(" Done !");
        }
    }

    public int getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }
    
    ////////HERE
    public SecretKey getkey() {
        return this.clientkey;
    }
    

    public void addMyGroups() {
        String sql = "select * from Groupe, users_groups where users_groups.user_id = '" + this.id + "'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int group_id = rs.getInt("group_id");
                boolean groupFound = false;
                for (int i = 0; i < allGroups.size(); i++) {
                    if (allGroups.get(i).getId() == group_id) {
                        this.myGroups.add(allGroups.get(i));
                        groupFound = true;
                        break;
                    }
                }
                if (!groupFound) {
                    Group g = new Group(group_id, rs.getString("groupe_name"), rs.getString("groupe_description"), rs.getInt("Groupe_admin_id"));
                    this.myGroups.add(g);
                    allGroups.add(g);
                }
            }
            for (int i = 0; i < this.myGroups.size(); i++) {
                if (!this.myGroups.get(i).isMember(this.getId())) {
                    //get group memebers from database
                    sql = "select * from users_groups where group_id = '" + this.myGroups.get(i).getId() + "'";
                    rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        this.myGroups.get(i).addMember(rs.getInt("user_id"));
                    }
                }
                this.myGroups.get(i).memberConnected(this.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Profile isOnline(String searchedEmail) {
        Iterator<Map.Entry<Profile, DataOutputStream>> iterator = mapDos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Profile, DataOutputStream> entry = iterator.next();
            if (searchedEmail.toLowerCase().equals(entry.getKey().getEmail().toLowerCase())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void connected(DataOutputStream dos) {
        Iterator<Map.Entry<Profile, DataOutputStream>> iterator = Profile.mapDos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Profile, DataOutputStream> entry = iterator.next();
            if (!entry.getKey().getEmail().equals(this.getEmail())) {
                try {
                    entry.getKey().lockMe();
                    entry.getValue().writeUTF("connection@@@" + this.getEmail());
                    this.lockMe();
                    dos.writeUTF("connection@@@" + entry.getKey().getEmail());
                    this.unlockMe();
                    entry.getKey().unlockMe();
                } catch (Exception e) {
                    this.unlockMe();
                    entry.getKey().unlockMe();
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean sendMessage(String targetEmail, String msg) {
    String[] messageParts = msg.split("@@@");
    byte[] bytes = null;

    // Déterminer si le message contient des données autres que le texte (fichiers, images, etc.).
    if (!messageParts[0].equals("text")) {
        try {
            int length = dis.readInt();
            bytes = new byte[length];
            dis.readFully(bytes);
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture des données binaires pour " + targetEmail);
            e.printStackTrace();
            return false;
        }
    }

    // Vérifier si le destinataire est en ligne
    Profile receiverProfile = isOnline(targetEmail);
    if (receiverProfile != null) {
        // Si le destinataire est en ligne, envoyer le message directement.
        try {
            DataOutputStream receiverDos = mapDos.get(receiverProfile);
            receiverProfile.lockMe();  // Verrouillage pour éviter des conditions de concurrence
            receiverDos.writeUTF(msg);
            if (!messageParts[0].equals("text")) {
                receiverDos.writeInt(bytes.length);
                receiverDos.write(bytes);
            }
            receiverProfile.unlockMe();  // Déverrouillage après l'envoi
            System.out.println("Message envoyé à " + targetEmail);
            return true;
        } catch (IOException e) {
            System.out.println("Erreur lors de l'envoi du message à " + targetEmail);
            e.printStackTrace();
            receiverProfile.unlockMe();
            return false;
        }
    } else {
        // Si le destinataire n'est pas en ligne, stocker le message dans la base de données pour un envoi ultérieur.
        String sql;
        if (messageParts[0].equals("text")) {
            sql = "INSERT INTO messages (sender_Email, receiver_Email, message, messageType, date) VALUES ('" 
                + this.getEmail() + "', '" + targetEmail + "', '" + messageParts[3].replace("'", "''") 
                + "', '" + messageParts[0] + "', '" + messageParts[2] + "')";
        } else {
            sql = "INSERT INTO messages (sender_Email, receiver_Email, message, messageType, fileName, date) VALUES ('"
                + this.getEmail() + "', '" + targetEmail + "', '" + Base64.getEncoder().encodeToString(bytes) 
                + "', '" + messageParts[0] + "', '" + messageParts[3].replace("'", "''") + "', '" + messageParts[2] + "')";
        }
        try {
            stmt.executeUpdate(sql);
            System.out.println("Message sauvegardé pour " + targetEmail);
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'enregistrement du message dans la base de données pour " + targetEmail);
            e.printStackTrace();
            return false;
        }
    }
}
    
     public boolean sendMessageToGroup(int target, String msg) {
        String s[] = msg.split("@@@");
        System.out.println(this.getEmail() + " is sending " + s[0] + " to group " + target);
        byte bytes[] = new byte[0];
        if (!s[0].equals("text")) {
            try {
                int i = dis.readInt();
                bytes = new byte[i];
                dis.readFully(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < this.myGroups.size(); i++) {
            if (this.myGroups.get(i).getId() == target) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String messageSendingTimeStamp = dtf.format(now);
                Iterator<Map.Entry<Profile, DataOutputStream>> iterator = mapDos.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Profile, DataOutputStream> entry = iterator.next();
                    if (this.myGroups.get(i).isMember(entry.getKey().getId())) {
                        if (this.myGroups.get(i).isConnected(entry.getKey().getId()) && entry.getKey().getId() != this.getId()) {
                            try {
                                entry.getKey().lockMe();
                                entry.getValue().writeUTF(msg);
                                if (!s[0].equals("text")) {
                                    entry.getValue().writeInt(bytes.length);
                                    entry.getValue().write(bytes);
                                }
                                entry.getKey().unlockMe();
                            } catch (IOException e) {
                                e.printStackTrace();
                                entry.getKey().unlockMe();
                            }
                        }
                    }
                }
                String[] offlineMembers = this.myGroups.get(i).getDisconnectedMembers().split("\n");
                if (offlineMembers.length > 0) {
                    String sql;
                    if (s[0].equals("text")) {
                        sql = "insert into Groupe_Message_content (Groupe_id,sender_name,sender_Email,messageType,content,date) values ('"
                            + this.myGroups.get(i).getId() + "','" + this.getName() + "','" + this.getEmail() + "','" + s[0] + "','"+ s[5].replace("'", "\\'") + "','" + s[4] + "')";
                    } else {
                        sql = "insert into Groupe_Message_content (Groupe_id,sender_name,sender_Email,messageType,content, fileName,date) values ('"
                            + this.myGroups.get(i).getId() + "','" + this.getName() + "','" + this.getEmail() + "','" + s[0] + "','"+ (new String(Base64.getEncoder().encode(bytes))) + "','" + s[4].replace("'", "\\'") + "','"+s[5]+"')";
                    }
                    try {
                        stmt.executeUpdate(sql);
                        sql = "select * from Groupe_Message_content where sender_Email = '" + this.getEmail() + "' order by GMC_id desc limit 1";
                        ResultSet rs = stmt.executeQuery(sql);
                        int GMC_id = 0;
                        if (rs.next()) {
                            GMC_id = rs.getInt("GMC_id");
                        }
                        for (String elm : offlineMembers) {
                            sql = "insert into Groupe_Messages (receiver_id, GMC_id) values ('" + elm + "','" + GMC_id + "')";
                            try {
                                stmt.executeUpdate(sql);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        return false;
    }

  

    public void lockMe() {
        this.lock.lock();
    }

    public void unlockMe() {
        this.lock.unlock();
    }

    public void disconnect() {
        mapDos.remove(this);
        for (int i = 0; i < this.myGroups.size(); i++) {
            this.myGroups.get(i).memberDisconnected(this.getId());
            if (this.myGroups.get(i).allOffline()) {
                allGroups.remove(this.myGroups.get(i));
            }
        }
        Iterator<Map.Entry<Profile, DataOutputStream>> iterator = mapDos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Profile, DataOutputStream> entry = iterator.next();
            entry.getKey().lockMe();
            try {
                entry.getValue().writeUTF("disconnection@@@" + this.getEmail());
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            entry.getKey().unlockMe();
        }
    }
}