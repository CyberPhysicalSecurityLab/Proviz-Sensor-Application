package ui.systray;

import org.apache.log4j.Logger;
import proper.ProvizProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by bigbywolf on 1/9/17.
 */
@SuppressWarnings("Since15")
public class SysTray{

    private SysTrayListener sysTrayListener;
    private PopupMenu popup;
    private TrayIcon trayIcon;
    private SystemTray tray;
    private ProvizProperties properties = ProvizProperties.getInstance();
    private Logger logger = Logger.getLogger(SysTray.class.getName());

    public SysTray(){
        showSystray();
    }

    private void showSystray() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            logger.error("SystemTray is not supported");
            return;
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage(properties.getRemoteProvizDir()+"static/Letter-P-icon.png");

        popup = new PopupMenu();
        trayIcon = new TrayIcon(image);
        tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
        cb1.setState(true);

        CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");

        Menu bluetooth = new Menu("Bluetooth");
        MenuItem btStatus = new MenuItem("BT status");
        MenuItem btConnect = new MenuItem("BT connect");
        MenuItem btDisconnect = new MenuItem("BT disconnect");

        Menu application = new Menu("Application");
        MenuItem startApplication = new MenuItem("Start");
        MenuItem stopApplication = new MenuItem("Stop");
        MenuItem restartApplication = new MenuItem("Restart");
        MenuItem openMonitor = new MenuItem("Open Monitor");


        MenuItem exitItem = new MenuItem("Exit");

        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.add(cb2);
        popup.addSeparator();
//        popup.add(selectDir);

        popup.add(bluetooth);
        bluetooth.add(btStatus);
        bluetooth.add(btConnect);
        bluetooth.add(btDisconnect);

        popup.add(application);
        application.add(startApplication);
        application.add(stopApplication);
        application.add(restartApplication);
        application.add(openMonitor);
        popup.add(exitItem);

        trayIcon.setImageAutoSize(true);
        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            logger.error("TrayIcon could not be added.");
            return;
        }


        btStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sysTrayListener != null){
                    sysTrayListener.selectedMenuItem("btStatus");
                }
            }
        });

        btConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sysTrayListener != null){
                    sysTrayListener.selectedMenuItem("btConnect");
                }
            }
        });

        btDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sysTrayListener != null){
                    sysTrayListener.selectedMenuItem("btDisconnect");
                }
            }
        });

        startApplication.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sysTrayListener != null){
                    sysTrayListener.selectedMenuItem("Start");
                }
            }
        });

        restartApplication.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sysTrayListener != null){
                    sysTrayListener.selectedMenuItem("Restart");
                }
            }
        });

        stopApplication.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sysTrayListener != null){
                    sysTrayListener.selectedMenuItem("Stop");
                }
            }
        });

        openMonitor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sysTrayListener != null){
                    sysTrayListener.selectedMenuItem("Monitor");
                }
            }
        });

        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "Proviz IOT Client");
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "Proviz IOT Client");
            }
        });

        cb1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED){
                    trayIcon.setImageAutoSize(true);
                } else {
                    trayIcon.setImageAutoSize(false);
                }
            }
        });

        cb2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb2Id = e.getStateChange();
                if (cb2Id == ItemEvent.SELECTED){
                    trayIcon.setToolTip("Sun TrayIcon");
                } else {
                    trayIcon.setToolTip(null);
                }
            }
        });


        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }

    public void setSysTrayListner(SysTrayListener listner){this.sysTrayListener = listner;}

}
