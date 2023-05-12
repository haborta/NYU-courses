package javafinal.tankwar.display;

import javafinal.tankwar.Predefined;

import javax.swing.*;
import java.awt.*;

public class LoginBackground extends JPanel {
    private static final long serialVersionUID = -6352788025440244338L;
    private Image image = Predefined.loadImageIcon("tank_bg.png").getImage();
    public LoginBackground() {
        //this.image = image;
    }

    public void paintComponent(Graphics g) {
        //super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }
    

}
