package component.client.secondaries;

import java.awt.Color;
import javax.swing.Icon;


public class Chat_Left extends javax.swing.JLayeredPane {
    public Chat_Left() {
        initComponents();
        txt.setBackground(new Color(230,230,230));
        // txt.setForeground(new Color(145,240,134));
        // txt.setForeground(new Color(51,122,44));
    }
    
    public void setUserprofile(String user){
        txt.setUserprofile(user);
        // txt.setColor();
        txt.setForeground(new Color(51,122,44));
    }
    
    public void setText(String text){
        if(text.equals("")){
            txt.hideText();
        }else{
            txt.setText(text);
        }
    }
    public void setTime(String time){
        txt.setTime(time);
    }
    
    public void setImage(Icon... image){
        txt.setImage(false, image);
    }
    
    public void setFile(String fileName,String fileSize,String path){
        txt.setFile(fileName, fileSize,path);
    }
    
    public void setAudio(String fileName){
        txt.setAudio(fileName);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txt = new component.client.secondaries.Chat_Item();

        setLayer(txt, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.client.secondaries.Chat_Item txt;
    // End of variables declaration//GEN-END:variables
}
