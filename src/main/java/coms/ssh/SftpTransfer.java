package coms.ssh;


import com.jcraft.jsch.*;

/**
 * Created by bigbywolf on 1/26/17.
 *
 * Basic sftp implementation using jsch library, to use this class
 * first create a constructor then call the connect() method to connect to remote host.
 * Once connected use the get getSftp() method to get back an ChannelSftp object which
 * can be used to send files like any ftp server and uses the same command names.
 *
 * ---> Important: when finished use the SftpTransfer classes disconnect() method which
 *                 will shutdown both the session and channel.
 *
 * Refer to full library documentation for full command list.
 * https://epaul.github.io/jsch-documentation/javadoc/
 */
public class SftpTransfer {

    private String user = null;
    private String host = null;
    private String pass = null;
    private int port = 22;
    private String knowHostFile = null;
    private String identityfile = null;

    private JSch jsch = null;
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp sftp = null;

    public SftpTransfer(String user, String host, int port, String pass, String knowHostFile) throws JSchException {
        this.user = user;
        this.host = host;
        this.port = port;
        this.pass = pass;
        this.knowHostFile = knowHostFile;

        jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(pass);
        jsch.setKnownHosts(knowHostFile);
    }

    public SftpTransfer(String user, String host, int port, String pass, String knowHostFile, String identityFile) throws JSchException {

        this.user = user;
        this.host = host;
        this.port = port;
        this.pass = pass;
        this.knowHostFile = knowHostFile;
        this.identityfile = identityFile;

        jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(pass);
        jsch.setKnownHosts(knowHostFile);
        jsch.addIdentity(identityfile);
    }

    public void put(String src, String dst) throws SftpException {
        System.out.println("Starting File Upload:");
        sftp.put(src, dst);
    }

    public void get(String src, String dst) throws SftpException {
        System.out.println("Starting File Upload:");
        sftp.get(src, dst);
    }

    public void connect() throws JSchException {

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        System.out.println("sftp Session open");
        session.connect();

        channel = session.openChannel("sftp");
        channel.connect();

        System.out.println("sftp connected to remote host");

        sftp = (ChannelSftp) channel;
        System.out.println("sftp channel open");
    }

    public void disconnect(){
        sftp.disconnect();
        session.disconnect();
    }

    public ChannelSftp getSftp() {
        return sftp;
    }

    public static void main(String[] args) {
        String user = "pi";
        String host = "10.42.0.250";
        String pass = "raspberry";

        String userName = System.getProperty("user.name");

        String khfile = "/home/"+userName+"/.ssh/known_hosts";
        String identityfile = "/home/"+userName+"/.ssh/id_rsa";

        try {
            SftpTransfer sftpTransfer = new SftpTransfer(user, host, 22, pass, khfile, identityfile);
            sftpTransfer.connect();
            ChannelSftp sftpChannel = sftpTransfer.getSftp();

            sftpChannel.put("/tmp/testDir/*", "/tmp/testDir/");
            sftpChannel.get("/tmp/testDir/*", "/tmp/testDir/");
//            sftpChannel.rm("/tmp/testDir/*");

            sftpTransfer.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}
