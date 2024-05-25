package component.client;

import component.client.secondaries.Item_Add;
import component.client.secondaries.Item_Group;
import net.miginfocom.swing.MigLayout;
import component.client.secondaries.Item_People;
import swing.ScrollBar;
import event.Globals;
import static event.Globals.dis;
import static event.Globals.dos;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import main.MainLogin;
import main.ModelUser;

public class List extends javax.swing.JPanel {
    private static Map<String, Item_People> ItemPeopleMap;
    private static ArrayList<Item_People> orderdedItemPeople;
    public List() {
        if(ItemPeopleMap==null){
            ItemPeopleMap=new HashMap<>();
            orderdedItemPeople=new ArrayList<>();
        }
        initComponents();
        init();
        addLogoutButton(); 
    }
  // Méthode pour ajouter le bouton de déconnexion
    private void addLogoutButton() {
        // Charger l'image à partir du fichier logout.png
    ImageIcon icon = new ImageIcon(getClass().getResource("/icon/logout.png"));
    
    // Créer le bouton avec l'image et sans texte
    JButton btnDisconnect = new JButton(icon);
    
    // Éliminer les bordures du bouton
    btnDisconnect.setBorderPainted(false);
    
    // Éliminer le fond du bouton
    btnDisconnect.setContentAreaFilled(false);
    
    
//        JButton btnDisconnect = new JButton("Déconnexion");
        btnDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout(); // Méthode pour gérer la déconnexion
            }
        });
        
        // Ajouter le bouton à votre panneau menu
        menu.add(btnDisconnect);
    }
 // Méthode pour gérer la déconnexion
   private void logout() {
    try {
        // Envoyer un message au serveur pour notifier la déconnexion
        dos.writeUTF("Disconnect");

        // Fermer la connexion côté client
        dis.close();
        dos.close();

        // Effacer la liste de contacts existante avant de mettre à jour
        ItemPeopleMap.clear();
        orderdedItemPeople.clear();

        // Fermer la fenêtre actuelle
        javax.swing.SwingUtilities.getWindowAncestor(this).dispose();

        // Ouvrir une nouvelle instance de la fenêtre de connexion
        MainLogin mainLogin = new MainLogin();
        mainLogin.setVisible(true);
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}


    
    private void init(){
        this.setBackground(new Color(255,255,255));
        sp.setVerticalScrollBar(new ScrollBar());
        menu.setLayout(new MigLayout("fillx","2%[fill,96%]2%","5[]5"));
        menuList.setLayout(new MigLayout("fillx","3%[fill,92%]3%","5[]5"));
        showPeople();
        //
        // showProfileOptions();

    }
    public void showPeople(){//only use this once
        menuList.removeAll();
        String myEmail=Globals.getMyEmail();
        File directoryPath = new File("./Conversations");
        String contents[] = directoryPath.list();
        for(int i = 0; i < contents.length; i++){
            String conv[]=contents[i].split(" ", 2);
            if (conv[0].toLowerCase().equals(myEmail)){
                addConversation(conv[1].substring(0, conv[1].length()-4));
            }
        }
        reprintAllConversations();
    }
    public void addConversation(String email){
        Item_People ip=new Item_People(email);
        orderdedItemPeople.add(ip);
        ItemPeopleMap.put(email, ip);
        pushConversationUp(email);
    }
    public void newMessage(String email){
        pushConversationUp(email);
        messageNotif(email);
    }
    public void pushConversationUp(String emailToPush){
        int j=-1;
        for(int i=0; i<orderdedItemPeople.size();i++){
            if(orderdedItemPeople.get(i).getEmail().equals(emailToPush))
                j=i;
        }
        if(j==-1){
            File myObj = new File("Conversations/"+Globals.getMyEmail()+" "+emailToPush+".txt");
            try {
                if(myObj.createNewFile()){
                    addConversation(emailToPush);
                    pushConversationUp(emailToPush);
                }
            } catch (IOException ex) {
                Logger.getLogger(List.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        Item_People tmp;
        for(int i=j;i>0;i--){
            tmp = orderdedItemPeople.get(i);
            orderdedItemPeople.set(i, orderdedItemPeople.get(i-1));
            orderdedItemPeople.set(i-1, tmp);
        }
        reprintAllConversations();
    }
    public void reprintAllConversations(){
        menuList.removeAll();
        for(int i=0;i<orderdedItemPeople.size();i++){
            menuList.add(orderdedItemPeople.get(i),"wrap");
        }
        addAddition();
        refreshMenu();
    }
    
    public static void connection(String key, boolean connected){
        Iterator<Map.Entry<String, Item_People>> iterator = ItemPeopleMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Item_People> entry = iterator.next();
            if(entry.getKey().equals(key))
                entry.getValue().setConnection(connected);
        }
    }
    public static void messageNotif (String key){
        Iterator<Map.Entry<String, Item_People>> iterator = ItemPeopleMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Item_People> entry = iterator.next();
            if(entry.getKey().equals(key))
                entry.getValue().addMessageNotif();
        }
    }
    
    private void addAddition(){
        Item_Add i=new Item_Add("Ajouter contact");
        menuList.add(i);
        i.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String Email=JOptionPane.showInputDialog(null,"Ajouter contact");
                if(Email!=null){
                    try{
                        Globals.dos.writeUTF("checkIfExist@@@"+Email.toLowerCase());
                    }
                    catch(Exception e2){
                        System.out.println("Failed to add contact");
			e2.printStackTrace();
                    }
                }
            }
        });
        
    }
    
    private JTextField usernameField;
private JTextField emailField;
private JPasswordField passwordField1;
private JPasswordField passwordField2;
private JButton modifyButton;
private Connection connection; // Déplacer la connexion à un niveau supérieur de portée

private void showGroup() {
    // test showing
    menuList.removeAll();

    usernameField = new JTextField(20);
    emailField = new JTextField(20);
    passwordField1 = new JPasswordField(20);
    passwordField2 = new JPasswordField(20);
    modifyButton = new JButton("Modifier Infos Profil");

    menuList.setLayout(new MigLayout("wrap 2", "[][grow]", ""));

    String myEmail = Globals.getMyEmail();

    // Chargement du pilote JDBC
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        e.printStackTrace();
        return; // Sortir de la fonction si le pilote n'est pas trouvé
    }

    String query = "SELECT password, name FROM users WHERE email = ?";
    try {

        connection = DriverManager.getConnection("jdbc:mysql://localhost:3308/tranquiltalk", "root", "");
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, myEmail);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String password = resultSet.getString("password");
            String name = resultSet.getString("name");
            System.out.println("Name: " + name + ", Password: " + password);

            menuList.add(new JLabel("Nom d'utilisateur:"), "gapleft 50,gaptop 10");
            menuList.add(new JLabel(" "));
            menuList.add(usernameField, "growx, wmin 100, wmax 200, hmin 35, gapleft 20, gaptop 10, wrap");

            usernameField.setText(name);
            usernameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            usernameField.setForeground(Color.BLACK);

            menuList.add(new JLabel("Email:"), "gapleft 80,gaptop 10");
            menuList.add(new JLabel(" "));
            menuList.add(emailField, "growx, wmin 100, wmax 200, hmin 35,gapleft 20, gaptop 10, wrap");


            emailField.setText(myEmail);
            emailField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            emailField.setForeground(Color.BLACK);

            menuList.add(new JLabel("Nouveau mot de passe:"), "gapleft 50,gaptop 10");
            menuList.add(new JLabel(" "));
            menuList.add(passwordField1, "growx, wmin 100, wmax 200, hmin 35, gapleft 20, gaptop 10, wrap");

            passwordField1.setText(password);
            passwordField1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            passwordField1.setForeground(Color.BLACK);

            menuList.add(new JLabel("Retaper le mot de passe:"), "gapleft 50,gaptop 10");
            menuList.add(new JLabel(" "));
            menuList.add(passwordField2, "growx, wmin 100, wmax 200, hmin 35, gapleft 20, gaptop 10, wrap");

            passwordField2.setText(password);
            passwordField2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//            passwordField2.setBackground(new Color(51, 122, 44)); // Couleur définie
            passwordField2.setForeground(Color.BLACK);

            menuList.add(new JLabel(" "));
           menuList.add(modifyButton, "skip 1, gapleft 50");


            modifyButton.setBackground(new Color(51, 122, 44)); // Couleur définie
            modifyButton.setForeground(Color.WHITE);

            modifyButton.addActionListener(e -> {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password1 = new String(passwordField1.getPassword());
                String password2 = new String(passwordField2.getPassword());

                if (!password1.equals(password2)) {
                    JOptionPane.showMessageDialog(null, "Les mots de passe ne correspondent pas.");
                    return; // Quitter la méthode si les mots de passe ne correspondent pas
                }

                String updateQuery = "UPDATE users SET name = ?, password = ? WHERE email = ?";
                try {
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, username);
                    updateStatement.setString(2, password1);
                    updateStatement.setString(3, email);

                    int rowsAffected = updateStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Informations du profil mises à jour avec succès.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Erreur lors de la mise à jour des informations du profil.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erreur lors de la mise à jour des informations du profil.");
                }
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    refreshMenu();
}

   

    private void refreshMenu(){
        menuList.repaint();
        menuList.revalidate();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menu = new javax.swing.JLayeredPane();
        menuMessage = new component.client.secondaries.MenuButton();
        menuGroup = new component.client.secondaries.MenuButton();
        sp = new javax.swing.JScrollPane();
        menuList = new javax.swing.JPanel();

        setBackground(new java.awt.Color(64, 50, 116));

        menu.setForeground(new java.awt.Color(255, 51, 51));
        menu.setLayout(new java.awt.GridLayout(1, 0));

        menuMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Chat.png"))); // NOI18N
        menuMessage.setSelected(true);
        menuMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMessageActionPerformed(evt);
            }
        });
        menu.add(menuMessage);

        menuGroup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/Group.png"))); // NOI18N
        menuGroup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        menuGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuGroupActionPerformed(evt);
            }
        });
        menu.add(menuGroup);

        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        menuList.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout menuListLayout = new javax.swing.GroupLayout(menuList);
        menuList.setLayout(menuListLayout);
        menuListLayout.setHorizontalGroup(
            menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        menuListLayout.setVerticalGroup(
            menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 315, Short.MAX_VALUE)
        );

        sp.setViewportView(menuList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(sp))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sp)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void menuMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMessageActionPerformed
        if(!menuMessage.isSelected()){
            menuMessage.setSelected(true);
            menuGroup.setSelected(false);
            reprintAllConversations();
        }
            
    }//GEN-LAST:event_menuMessageActionPerformed

    private void menuGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGroupActionPerformed
        if(!menuGroup.isSelected()){
            menuMessage.setSelected(false);
            menuGroup.setSelected(true);
            showGroup();
        }
    }//GEN-LAST:event_menuGroupActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane menu;
    private component.client.secondaries.MenuButton menuGroup;
    private javax.swing.JPanel menuList;
    private component.client.secondaries.MenuButton menuMessage;
    private javax.swing.JScrollPane sp;
    // End of variables declaration//GEN-END:variables
}
