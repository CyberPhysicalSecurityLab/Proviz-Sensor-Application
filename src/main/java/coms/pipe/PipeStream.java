package coms.pipe;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by bigbywolf on 1/12/17.
 *
 * The PipeStream class is used to make the use of named pipes easier for interprocess communication.
 * A couple things to note****
 * 1. The a reader has to be created and be open/listening before a write can be performed.
 * 2. The reader should be used in a separate thread and always be listening and removing from the pipe.
 */
public class PipeStream {

    private InputStream pipeIn;
    private OutputStream pipeOut;
    private String pipePath = null;
    private boolean pathProvided = false;
    private short MAX_PACKET_LENGTH = 32767;
    private Logger logger = Logger.getLogger(PipeStream.class.getName());

    public PipeStream(String pipePath){
        this.pipePath = pipePath;
        this.pathProvided = true;
    }

    public PipeStream(InputStream pipeStream){
        this.pipeIn = pipeStream;
    }

    private boolean startWriteStream(){
        boolean startSuccessful = true;

        if (pathProvided){
            //initialize the pipe after a small sleep time
            File pipe = new File(pipePath);

            try{
                pipeOut = new FileOutputStream(pipe);
            }catch(FileNotFoundException e){
                logger.error("Pipe could not be found", e);
                return false;
            }
        }

        return startSuccessful;
    }

    public boolean startReadStream(){
        boolean startSuccessful = true;

        if (pathProvided){
            //initialize the pipe after a small sleep time
            File pipe = new File(pipePath);

            try{
                pipeIn = new FileInputStream(pipe);
            }catch(FileNotFoundException e){
                logger.error("Pipe could not be found", e);
                return false;
            }
        }

        return startSuccessful;
    }


    public void StopClient() {
        pipeIn = null;
        pipeOut = null;
    }

    public void StartClient() {
        File pipe = new File(pipePath);

        try{
            pipeOut = new FileOutputStream(pipe);
            pipeIn = new FileInputStream(pipe);
        }catch(FileNotFoundException e){
            logger.error("Pipe could not be found", e);
        }
    }
    public boolean write(byte[] m_pTxDataBuffer, short totalpktLength) {
        boolean wSuccess = false;
        try {
            pipeOut.write(m_pTxDataBuffer, 0, totalpktLength);
            pipeOut.flush();
            wSuccess = true;
        } catch (NullPointerException e) {
            logger.error("Write buffer is null", e);
        }catch(IndexOutOfBoundsException e){
            logger.error("Write length exceeds write buffer size or total packets to write contain a invalid value", e);
        }catch (IOException e){
            logger.error("IOException while writing to the pipe", e);
        }
        return wSuccess;
    }

    public boolean writeWithTimeOut(byte[] m_pTxDataBuffer, short totalpktLength) {
        final byte[] buff = m_pTxDataBuffer;
        final short len = totalpktLength;
        boolean wSuccess = false;

        Callable<Void> start = new Callable<Void>() {
            public Void call() throws Exception {
                startWriteStream();
                return null;
            }
        };

        Callable<Void> writeTo = new Callable<Void>() {
            public Void call() throws Exception {
                write(buff, len);
                return null;
            }
        };

        try {
            new SimpleTimeLimiter().callWithTimeout(start, 500, TimeUnit.MILLISECONDS, true);
            new SimpleTimeLimiter().callWithTimeout(writeTo, 500, TimeUnit.MILLISECONDS, true);
            wSuccess = true;

        } catch (InterruptedException e) {
            logger.error("Write thread interrupted", e);
        } catch (ExecutionException e) {
            logger.error(e);
        } catch (TimeoutException e) {
            logger.error("write thread timeout, make sure listener is working", e);
        } catch (Exception e){
            logger.error("Exception: Can't initialize pipe, make sure pipe reader is active", e);
        }
        return wSuccess;
    }

    public boolean writeWithTimeOut(String dataBuffer) {

        return writeWithTimeOut(dataBuffer.getBytes(), (short)dataBuffer.length());
    }


    public int read(byte[] pchBuff, int inBufferSize) {
        int byteTransfer = 0;
        try{
            int toRead = pipeIn.available();
            byteTransfer = pipeIn.read(pchBuff, 0, toRead);
        } catch (NullPointerException e) {
            logger.error("Exception! Read buffer is null.", e);
        }catch (IndexOutOfBoundsException e) {
            logger.error("Exception! Read length exceeds read buffer size.", e);
        }catch (IOException e) {
            logger.error("IOException while reading from the pipe.", e);
        }
            return byteTransfer;
    }

    public String readString(){
        try {
            int toRead = pipeIn.available();
            byte[] data = new byte[toRead]; // create buffer to hold message
            read(data, toRead);
            return new String(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int readAvailable() throws IOException {
       return pipeIn.available();
    }


}
