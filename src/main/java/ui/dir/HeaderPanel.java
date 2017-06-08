package ui.dir;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by bigbywolf on 1/6/17.
 */
public class HeaderPanel extends JPanel{



    public HeaderPanel(){
        try {
            String username = System.getProperty("user.name");
            String imagePath = "/home/" + username + "/provizclient.com.manager/src/main/Static/pro.png";
            BufferedImage img = ImageIO.read(new File(imagePath));
            ImageIcon icon = new ImageIcon(img);
            JLabel label = new JLabel(icon);
            add(label);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
