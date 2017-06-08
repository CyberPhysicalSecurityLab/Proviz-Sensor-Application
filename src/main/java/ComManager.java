import proper.ProvizProperties;

import javax.swing.*;


/**
 * Created by bigbywolf on 1/11/17.
 */
public class ComManager {

    public static void main(String[] args) {

        // start and load proviz properties file
        ProvizProperties properties = ProvizProperties.getInstance();

        String user = System.getenv("SUDO_USER");
        if (user == null)
            user = System.getProperty("user.name");

        String configFilePath = "/home/" + user + "/.proviz/.config.properties";

        properties.setConfigFilePath(configFilePath);
        properties.loadProperties();



        // Change ui theme
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        JFrame.setDefaultLookAndFeelDecorated( true );
        JDialog.setDefaultLookAndFeelDecorated( true );

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainFrame();
            }
        });

    }
}
