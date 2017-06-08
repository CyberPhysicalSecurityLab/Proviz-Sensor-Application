package ui.dir;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Created by bigbywolf on 1/9/17.
 */
public class SelectDirectoryFrame extends JFrame{

    private SelectDirectory selectDirectory;
    private HeaderPanel headerPanel;
    private SelectDirectoryListener selectDirectoryListener;

    public SelectDirectoryFrame(){
        super("Select Directory");

        selectDirectory = new SelectDirectory();
        headerPanel = new HeaderPanel();

        setLayout(new BorderLayout());
        setJMenuBar(createMenuBar());

        selectDirectory.setSelectDirectoryListener(new SelectDirectoryListener() {
            public void directorySelected(String path) {
                setVisible(false);
                selectDirectoryListener.directorySelected(path);
            }
        });

        add(selectDirectory, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        setSize(620, 400);

    }

    public void setDirectoryVisible(boolean visible){
        setVisible(visible);
    }

    private JMenuBar createMenuBar(){
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exportDateItem = new JMenuItem("Export Data...");
        JMenuItem importDateItem = new JMenuItem("Import Data...");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(exportDateItem);
        fileMenu.add(importDateItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        fileMenu.setMnemonic(KeyEvent.VK_F);
        exitItem.setMnemonic(KeyEvent.VK_X);

        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        return menuBar;
    }

    public void setSelectDirectoryListener(SelectDirectoryListener listener){
        this.selectDirectoryListener = listener;
    }
}
