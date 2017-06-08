package coms.pipe;

import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bigbywolf on 1/17/17.
 */
public class ReadPipeInputThread extends Thread{

    private volatile boolean running = true;
    private volatile PipeStream inPipe = null;
    private boolean pathProvided = false;
    private volatile JSONParser parser = new JSONParser();
    private volatile int readBuffer = 0;
    private ComCallBack callBack = null;

    private Logger logger = Logger.getLogger(ReadPipeInputThread.class.getName());

    public ReadPipeInputThread(String filePath){
        inPipe = new PipeStream(filePath);
        this.pathProvided = true;
    }

    public ReadPipeInputThread(InputStream pipeStream){
        inPipe = new PipeStream(pipeStream);
    }

    public void messageReceivedCallBack(ComCallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try{
            readBuffer = 0;
            if (pathProvided){
                inPipe.startReadStream();
            }

            while(running && inPipe != null) try {
                readBuffer = inPipe.readAvailable();
                if (readBuffer > 0) {
                    String dataOut = inPipe.readString();

                    if (callBack != null){
                        callBack.callBackMethod(dataOut);
                    }
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
        catch (IOException e){
            logger.error(e);
        }
        logger.info("End of Read Input Thread");
    }

    public void shutdown(){
        running = false;
        inPipe.StopClient();
    }
}
