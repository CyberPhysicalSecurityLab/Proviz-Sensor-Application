package ui.monitor;

import javax.swing.*;
import java.awt.*;


/**
 * Created by bigbywolf on 1/17/17.
 */
public class Monitor extends JFrame{

    private JTextArea textArea;
    private JScrollPane scroll ;

    public Monitor(){
        super("Monitor");
        textArea = new JTextArea();
        textArea.setEditable(false);

        scroll = new JScrollPane(textArea);
        new SmartScroller(scroll);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);

        setSize(600, 700);
        setVisible(false);
    }

    public void appendText(String text){
        textArea.append(text);
    }

    public void setMonitorVisible(boolean visible){
        setVisible(visible);
    }

    void setMonitorAppend(boolean monitorAppend){

    }

    public void clearMonitor(){
        textArea.setText("");
    }
}
