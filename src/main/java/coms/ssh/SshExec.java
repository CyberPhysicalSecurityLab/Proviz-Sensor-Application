package coms.ssh;

import com.jcraft.jsch.*;
import coms.pipe.PipeStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bigbywolf on 1/26/17.
 */
public class SshExec {

    private String user = null;
    private String host = null;
    private String pass = null;
    private int port = 22;
    private String knowHostFile = null;
    String identityfile = null;

    private JSch jsch = null;
    private Session session = null;
    private Channel channel = null;
    private ChannelExec exec = null;
    private PipeStream inStream = null;

    public SshExec(String user, String host, int port, String pass, String knowHostFile) throws JSchException {
        this.user = user;
        this.host = host;
        this.port = port;
        this.pass = pass;
        this.knowHostFile = knowHostFile;

        jsch = new JSch();
        session=jsch.getSession(user, host, 22);
        session.setPassword(pass);
        jsch.setKnownHosts(knowHostFile);
    }

    public SshExec(String user, String host, int port, String pass, String knowHostFile, String identityFile) throws JSchException {
        this.user = user;
        this.host = host;
        this.port = port;
        this.pass = pass;
        this.knowHostFile = knowHostFile;
        this.identityfile = identityFile;

        jsch = new JSch();
        session=jsch.getSession(user, host, 22);
        session.setPassword(pass);
        jsch.setKnownHosts(knowHostFile);
        jsch.addIdentity(identityfile);
    }

    public void connect() throws JSchException {

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
        System.out.println("ssh session open");

        channel = session.openChannel("exec");
        exec = (ChannelExec)channel;
        System.out.println("ssh channel open");
    }

    public String sendCommand(String command){

        ((ChannelExec)channel).setCommand(command);
        //channel.setInputStream(System.in);
        channel.setInputStream(null);

        ((ChannelExec)channel).setErrStream(System.err);
        String dataOut = null;

        try {
            channel.connect();

            InputStream in = channel.getInputStream();
            inStream = new PipeStream(in);

            int timeout = 0;

            while(true){
                while(inStream.readAvailable() > 0){
                    dataOut = inStream.readString();
                }
                if(channel.isClosed()){
                    dataOut += "\nexit-status: "+channel.getExitStatus();
                    break;
                }
                if (timeout >= 5){
                    dataOut = "timed-out";
                    break;
                }
                timeout ++;
                try{Thread.sleep(100);}catch(Exception ee) {ee.printStackTrace();}
            }
        } catch (JSchException e) {
            e.printStackTrace();
            return "JSchException";
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException";
        }

        return dataOut;
    }

    public void disconnect(){
        channel.disconnect();
        session.disconnect();
    }


    public static void main(String[] args) {

        try{
            JSch jsch=new JSch();
            String user = "pi";
            String host = "10.42.0.250";
            String pass = "raspberry";
            String userName = System.getProperty("user.name");
            String khfile = "/home/"+userName+"/.ssh/known_hosts";
            String identityfile = "/home/"+userName+"/.ssh/id_rsa";

            SshExec sshExec = new SshExec(user, host, 22, pass, khfile);
            sshExec.connect();

            String command="ps -ef;date;hostname";
            String output = sshExec.sendCommand(command);
            System.out.println(output);

            sshExec.disconnect();
        }
        catch(Exception e){
            System.out.println("Main exception");
            System.out.println(e);
        }
    }       //end main
}
