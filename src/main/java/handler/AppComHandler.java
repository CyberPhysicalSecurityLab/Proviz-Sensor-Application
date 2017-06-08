package handler;

import coms.pipe.ComCallBack;
import coms.pipe.ReadPipeInputThread;
import org.apache.log4j.Logger;
import proper.ProvizProperties;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bigbywolf on 1/17/17.
 */
public class AppComHandler {

    private Process proc = null;
    private InputStream in = null;
    private InputStream err = null;


    private ReadPipeInputThread readInThread = null;
    private ReadPipeInputThread errorInThread = null;
    private MessageListener messageListener;

    private ProvizProperties properties;

    private Logger logger = Logger.getLogger(AppComHandler.class.getName());

    public AppComHandler() {

        properties = ProvizProperties.getInstance();
        properties.loadProperties();

        String applicationCommand = "java -jar " + properties.getRemoteProvizDir() + "app/" + properties.getAppName();

        try {
            proc = Runtime.getRuntime().exec(applicationCommand);

            // Then retrieve the process output
            in = proc.getInputStream();
            err = proc.getErrorStream();

            // start read thread
            readInThread = new ReadPipeInputThread(in);
            readInThread.messageReceivedCallBack(inCallBack);
            readInThread.start();

            // start error thread
            errorInThread = new ReadPipeInputThread(err);
            errorInThread.messageReceivedCallBack(errCallBack);
            errorInThread.start();

        } catch (IOException e) {
            logger.error("Exception: IO could not start process", e);
        }
    }

    public void closeCom() {
        proc.destroy();
        readInThread.shutdown();
        proc = null;
        in = null;
        err = null;
        messageListener = null;
    }

    // Log all errors from sensor application
    private ComCallBack errCallBack = new ComCallBack() {
        @Override
        public void callBackMethod(String message) {
            logger.error("Sensor Application error: " + message);
            if (messageListener != null){
                messageListener.messageEmitted(message);
            }
        }
    };

    // Handle messages from sensor application
    private ComCallBack inCallBack = new ComCallBack() {
        @Override
        public void callBackMethod(String message) {
            logger.debug("in sensor stream: " + message);
            if (messageListener != null){
                messageListener.messageEmitted(message);
            }
        }
    };

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }
}


