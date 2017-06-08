package coms.bluetooth;

/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java
 * language and environment is gratefully acknowledged.
 *
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */

import java.io.*;

/**
 * a tiny version of Ward Christensen's MODEM program for UNIX.
 * Written ~ 1980 by Andrew Scott Beals. Last revised 1982.
 * A.D. 2000 - dragged from the archives for use in Java Cookbook.
 *
 * @author C version by Andrew Scott Beals, sjobrg.andy%mit-oz@mit-mc.arpa.
 * @author Java version by Ian F. Darwin, http://www.darwinsys.com/
 * $Id: TModem.java,v 1.10 2004/04/11 23:50:40 ian Exp $
 */
public class TModem {

    protected final byte CPMEOF = 26;    /* control/z */
    protected final int MAXERRORS = 10;    /* max times to retry one block */
    protected final int SECSIZE  = 128;    /* cpm sector, transmission block */
    protected final int SENTIMOUT = 30;    /* timeout time in send */
    protected final int  SLEEP  = 30;    /* timeout time in recv */

  /* Protocol characters used */

    protected final byte  SOH  = 1;  /* Start Of Header */
    protected final byte  EOT  = 4;  /* End Of Transmission */
    protected final byte  ACK  = 6;  /* ACKnowlege */
    protected final byte  NAK  = 0x15;  /* Negative AcKnowlege */

    protected InputStream inStream;
    protected PrintWriter outStream;

    /** Construct a TModem */
    public TModem(InputStream is, PrintWriter os, PrintWriter errs) {
        inStream = is;
        outStream = os;
    }

    /** Construct a TModem with default files (stdin and stdout). */
//    public TModem() {
//        inStream = System.in;
//        outStream = System.out;
//    }

    /** A main program, for direct invocation. */
    public static void main(String[] argv) throws
            IOException, InterruptedException {

    /* argc must == 2, i.e., `java TModem -s filename' */
        if (argv.length != 2)
            usage();

        if (argv[0].charAt(0) != '-')
            usage();

//        TModem tm = new TModem();
//        tm.setStandalone(true);
//
//        boolean OK = false;
//        switch (argv[0].charAt(1)){
//            case 'r':
//                OK = tm.receive(argv[1]);
//                break;
//            case 's':
//                OK = tm.send(argv[1]);
//                break;
//            default:
//                usage();
//        }
//        System.out.print(OK?"Done OK":"Failed");
//        System.exit(0);
    }

    /* give user minimal usage message */
    protected static void usage()
    {
        System.err.println("usage: TModem -r/-s file");
        // not System.out, not die(), since this is static.
        System.exit(1);
    }

    /** If we're in a standalone app it is OK to System.exit() */
    protected boolean standalone = false;
    public void setStandalone(boolean is) {
        standalone = is;
    }
    public boolean isStandalone() {
        return standalone;
    }

    /** A flag used to communicate with inner class IOTimer */
    protected boolean gotChar;

    /** An inner class to provide a read timeout for alarms. */
    class IOTimer extends Thread {
        String message;
        long milliseconds;

        /** Construct an IO Timer */
        IOTimer(long sec, String mesg) {
            milliseconds = 1000 * sec;
            message = mesg;
        }

        public void run() {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                // can't happen
            }
            /** Implement the timer */
            if (!gotChar)
                System.out.println("Timed out waiting for " + message);
            try {
                die(1);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /*
     * send a file to the remote
     */
    public boolean send(String tfile) throws IOException, InterruptedException
    {
        char checksum, index, blocknumber, errorcount;
        byte character;
        byte[] sector = new byte[SECSIZE];
        int nbytes;
        DataInputStream foo;

        foo = new DataInputStream(new FileInputStream(tfile));
        System.out.println( "file open, ready to send");
        errorcount = 0;
        blocknumber = 1;

        // The C version uses "alarm()", a UNIX-only system call,
        // to detect if the read times out. Here we do detect it
        // by using a Thread, the IOTimer class defined above.
        gotChar = false;
        new IOTimer(SENTIMOUT, "NAK to start send").start();

        do {
            character = getchar();
            gotChar = true;
            if (character != NAK && errorcount < MAXERRORS)
                ++errorcount;
        } while (character != NAK && errorcount < MAXERRORS);

        System.out.println( "transmission beginning");
        if (errorcount == MAXERRORS) {
            xerror();
        }

        while ((nbytes=inStream.read(sector))!=0) {
            if (nbytes<SECSIZE)
                sector[nbytes]=CPMEOF;
            errorcount = 0;
            while (errorcount < MAXERRORS) {
                System.out.println( "{" + blocknumber + "} ");
                putchar(SOH);  /* here is our header */
                putchar(blocknumber);  /* the block number */
                putchar(~blocknumber);  /* & its complement */
                checksum = 0;
                for (index = 0; index < SECSIZE; index++) {
                    putchar(sector[index]);
                    checksum += sector[index];
                }
                putchar(checksum);  /* tell our checksum */
                if (getchar() != ACK)
                    ++errorcount;
                else
                    break;
            }
            if (errorcount == MAXERRORS)
                xerror();
            ++blocknumber;
        }
        boolean isAck = false;
        while (!isAck) {
            putchar(EOT);
            isAck = getchar() == ACK;
        }
        System.out.println( "Transmission complete.");
        return true;
    }

    /*
     * receive a file from the remote
     */
    public boolean receive(String tfile) throws IOException, InterruptedException
    {
        char checksum, index, blocknumber, errorcount;
        byte character;
        byte[] sector = new byte[SECSIZE];
        DataOutputStream foo;

        foo = new DataOutputStream(new FileOutputStream(tfile));

        System.out.println("you have " + SLEEP + " seconds...");

    /* wait for the user or remote to get his act together */
        gotChar = false;
        new IOTimer(SLEEP, "receive from remote").start();

        System.out.println("Starting receive...");
        putchar(NAK);
        errorcount = 0;
        blocknumber = 1;
        rxLoop:
        do {
            character = getchar();
            gotChar = true;
            if (character != EOT) {
                try {
                    byte not_ch;
                    if (character != SOH) {
                        System.out.println( "Not SOH");
                        if (++errorcount < MAXERRORS)
                            continue rxLoop;
                        else
                            xerror();
                    }
                    character = getchar();
                    not_ch = (byte)(~getchar());
                    System.out.println( "[" +  character + "] ");
                    if (character != not_ch) {
                        System.out.println( "Blockcounts not ~");
                        ++errorcount;
                        continue rxLoop;
                    }
                    if (character != blocknumber) {
                        System.out.println( "Wrong blocknumber");
                        ++errorcount;
                        continue rxLoop;
                    }
                    checksum = 0;
                    for (index = 0; index < SECSIZE; index++) {
                        sector[index] = getchar();
                        checksum += sector[index];
                    }
                    if (checksum != getchar()) {
                        System.out.println( "Bad checksum");
                        errorcount++;
                        continue rxLoop;
                    }
                    putchar(ACK);
                    blocknumber++;
                    try {
                        foo.write(sector);
                    } catch (IOException e) {
                        System.out.println("write failed, blocknumber " + blocknumber);
                    }
                } finally {
                    if (errorcount != 0)
                        putchar(NAK);
                }
            }
        } while (character != EOT);

        foo.close();

        putchar(ACK);  /* tell the other end we accepted his EOT   */
        putchar(ACK);
        putchar(ACK);

        System.out.println("Receive Completed.");
        return true;
    }

    protected byte getchar() throws IOException {
        return (byte)inStream.read();
    }

    protected void putchar(int c) throws IOException {
        outStream.write(c + "\r\n");
        outStream.flush();
    }

    protected void xerror()
    {
        System.out.println("too many errors...aborting");
        try {
            die(1);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void die(int how) throws Exception
    {
        if (standalone)
            System.exit(how);
        else throw new Exception("Error code " + Integer.toString(how));
    }
}
