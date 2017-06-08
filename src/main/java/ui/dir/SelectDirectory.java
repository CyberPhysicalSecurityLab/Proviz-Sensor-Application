package ui.dir;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by bigbywolf on 1/6/17.
 */
public class SelectDirectory extends JPanel{

    private JLabel selectDirLabel;
    private JTextField selectDirField;
    private JButton okBtn;
    private JButton selectDirBtn;
    private SelectDirectoryListener pathListener;

    public SelectDirectory(){

        selectDirField = new JTextField(40);
        okBtn = new JButton("OK");
        selectDirBtn = new JButton("...");


        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        selectDirBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openDir();
            }
        });

        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!selectDirField.getText().isEmpty()){
                    if(pathListener != null){
                        pathListener.directorySelected(selectDirField.getText());
                    }
                }
                else {
                    JOptionPane.showMessageDialog(null,
                            "Please select a directory");
                }
            }
        });

        Border innerBorder = BorderFactory.createTitledBorder("Select Proviz Directory");
        Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        setBorder(BorderFactory.createCompoundBorder(innerBorder, outerBorder));

        // select field //
        gc.weightx = 1;
        gc.weighty = 0.1;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0,20, 0, 0);
        gc.anchor = GridBagConstraints.LINE_START;
        add(selectDirField, gc);

        // select dir btn //
        gc.weightx = 1;
        gc.weighty = 0.1;
        gc.gridx = 1;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0,0, 0, 50);
        gc.anchor = GridBagConstraints.LINE_START;
        add(selectDirBtn, gc);

        // ok btn //
        gc.weightx = 1;
        gc.weighty = 1.0;
        gc.gridx = 1;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(10,0, 0, 50);
        gc.anchor = GridBagConstraints.LINE_START;
        add(okBtn, gc);

    }

    public File openDir(){
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectDirField.setText(""+chooser.getSelectedFile());
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public void setSelectDirectoryListener(SelectDirectoryListener listener){
        this.pathListener = listener;
    }
}
