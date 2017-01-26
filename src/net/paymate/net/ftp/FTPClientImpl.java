package net.paymate.net.ftp;
/*
 * FTPClientImpl.java
 *
 * An FTP client class which implements remote peer-to-peer transfer.
 * All rights reserved.
 * This software may be reproduced or used for non-commercial purposes.
 * No warrantee or guarantee is made or implied.
 *
 */

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * An FTP client class which implements remote peer-to-peer transfer.
 */

public class FTPClientImpl implements FTPClient {
  String			    host;
  String			    user;
  InetAddress		  localInetAddress;
  int				      localDataPort;
  boolean			    Mode = false;
  String			    response;
  BufferedReader	is;
  BufferedWriter	ps;
  //PrintStream ps;

  String			code = null;
  Socket			controlSocket;
  private long	nBytesRead = 0;

  /**
   * Open a connection to a remote host that runs an FTP server.
   */

  public void openConnection(String host, String user, String password)
  throws IOException, Exception {
    this.host = host;
    this.user = user;
    controlSocket = new Socket(host, 21);
    is = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
    ps = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));

    readResponse();

    sendCommand("user " + user);
    sendCommand("pass " + password);

    localInetAddress = InetAddress.getLocalHost();

    System.out.println("openConnection");

  }

  public void quit()
  throws IOException, Exception {
    sendCommand("quit");
    if (!code.equals("221")) {
      readResponse();
    }

  }

  /**
   * Send a command to the host, and read the response.
   */

  public void sendCommand(String command)
  throws IOException, Exception {
    ps.write(command);
    ps.newLine();
    ps.flush();
    System.out.println("Send Command: " + command);
    readResponse();
    char c = response.charAt(0);
    if ((c != '1') && (c != '2') && (c != '3')) {
      throw new Exception("FTP: " + host + " - " + response);
    }

    System.out.println("Command Processed: " + command);

  }

  /**
   * Read a response from the host.
   */

  public void readResponse()
  throws IOException {
    // Read 3-digit code

    response = is.readLine();
    System.out.println("response is: " + response);

    // If the code is followed by a "-", read lines until we read the
    // same code again.
    code = response.substring(0, 3);

    System.out.println("Code is :'" + code + "'");

    String    line = response;

    if ((response.length() > 3) && (response.charAt(3) == '-')) {

      for (;;) {
        line = is.readLine();
        response += line;

        if (line.length() > 3 && response.substring(0, 3).equals(code) && line.charAt(3) != '-') {
          break;
        }

      }
    }

  }

  /**
   * Manufacture the kind of host/port address required by FTP.
   * The syntax is:
   *
   *   PORT h1,h2,h3,h4,p1,p2
   *
	 *   	where h1 is the high order 8 bits of the internet host
	 *   	address.
   */

  String makePortAddress(InetAddress inad, int port) {

    byte	b[] = inad.getAddress();
    String[]	pa = new String[6];

    // Convert the IP address to ascii representation of its byte values

    pa[0] = toUnsignedDecimal(b[0]);
    pa[1] = toUnsignedDecimal(b[1]);
    pa[2] = toUnsignedDecimal(b[2]);
    pa[3] = toUnsignedDecimal(b[3]);

    // Convert the port to an ascii representation of its octal value

    byte bhi = (byte)(port >> 8 & 0xff);
    pa[4] = toUnsignedDecimal(bhi);
    byte blo = (byte)(port & 0xff);
    pa[5] = toUnsignedDecimal(blo);

    // Now concatenate these pieces, and return the value

    String s = pa[0] + "," + pa[1] + "," + pa[2] + "," + pa[3] +
        "," + pa[4] + "," + pa[5];
    System.out.println("Port is " + s);
    return s;
  }

  /**
   * Utility function to convert a byte to an unsigned decimal string
   * representation
   */

  String toUnsignedDecimal(byte b) {
    int			i = 0;

    i |= (int)b;
    i &= 0x000000ff;

    return String.valueOf(i);

  }

  /**
   * Set the transfer mode to Ascii.
   */

  public void setAscii()
  throws IOException, Exception {

    sendCommand("type a");

  }

  /**
   * Set the transfer mode to Image (also referred to as "binary").
   */

  public void setBinary()
  throws IOException, Exception {

    sendCommand("type i");

  }

  /**
   * Change to the specified directory, which may be relative or absolute.
   */

  public void cwd(String dir)
  throws IOException, Exception {

    sendCommand("cwd " + dir);

  }

  /**
   * Delete the File
   */

  public void delete(String sDirectory, String sFile)
  throws IOException, Exception {

    sendCommand("cwd " + sDirectory);
    sendCommand("dele " + sFile);

  }

  /**
   * Request that the host send a file to another host. The destination host is
   * specified by the port string, which conforms to the syntax used by
   * makePortAddress().
   */

  public void send(String sDirectory, String filename, FTPSender ftpSender)
  throws IOException, Exception {

    ServerSocket		ssock = new ServerSocket(0, 1);
    Socket				sock;
    DataOutputStream	dos = null;
    int					dataport = ssock.getLocalPort();

    sendCommand("port " + makePortAddress(localInetAddress, dataport));

    if(code.charAt(0) != '2') return;
    sendCommand("cwd " + sDirectory);
    setBinary();
    sendCommand("stor " + filename);
    if(code.charAt(0) != '1') return;

    try {
      sock = ssock.accept();
      dos = new DataOutputStream(sock.getOutputStream());
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }

    ftpSender.send(dos);
    byte		buffer[] = new byte[1024];
    dos.write(buffer, 0, 0);

    /** close dataOutputStream for dataconnection  **/

    try {
      sock.close();
      ssock.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

  /**
   * Request a listing of the current directory. The result is sent on a new
   * data connection.
   */

  public Vector list()
  throws IOException, Exception {
    Vector				files = new Vector();

    class ListFactory implements FTPFactory {
      Vector			files;
      long			nBytesRead = 0;

      public ListFactory(Vector files) {
        this.files = files;
      }

      public void processSocket(Socket socket)
      throws IOException {
        BufferedReader		dis = null;

        try {
          dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));

          for (;;) {
            String sLine = dis.readLine();

            if (sLine == null) break; // Check for EOF

            nBytesRead += sLine.length();

            System.out.println("Line read from socket: " + sLine);

            if (sLine.length() > 53) {
              System.out.println("Line added: " + sLine.length());
              files.addElement(new FTPFile(sLine));
            }

          }

        } catch (IOException e) {
          System.out.println("list " + e);
        } finally {
        dis.close();
        }

      }

      public long dtaMonBytesTransf(){
        return nBytesRead;
      }

    }

    //
    // Main processing begins here
    //

    ServerSocket		ssock = new ServerSocket(0, 1);
    IncomingDataMonitor thread = new IncomingDataMonitor(ssock,
                  new ListFactory(files));
    thread.start();
    int port = ssock.getLocalPort();

    sendCommand("port " + makePortAddress(localInetAddress, port));
    sendCommand("list");
    readResponse();

    try {
      thread.join();
    } catch (InterruptedException e) {}

    thread = null;
    return files;

  }

  /**
   * Request to receive the specified file.
   */

  public void receive(String directory, String filename,
            FTPVisitor ftpVisitor)
  throws IOException, Exception {

    class ReceiveFactory implements FTPFactory {
      FTPVisitor		ftpVisitor;
      long			nBytesRead;

      public ReceiveFactory(FTPVisitor ftpVisitor) {
        this.ftpVisitor = ftpVisitor;
      }

      public void processSocket(Socket socket)
      throws IOException{
        DataInputStream src = new DataInputStream(socket.getInputStream());

        System.out.println("Entered read Bytes: ");

        try {
          byte		b[] = new byte[1024];  // 1K
          int			nAmount = 0;

          nBytesRead = 0;

          while ( ( nAmount = src.read( b ) ) != -1 ) {
            ftpVisitor.write(b, nAmount);
            nBytesRead += nAmount;
          }

        } catch(IOException e) {
          src.close();
        }

      }

      public long dtaMonBytesTransf(){
        return nBytesRead;
      }

    }

    ServerSocket ssock = new ServerSocket(0, 1);
    IncomingDataMonitor thread = new IncomingDataMonitor(
                  ssock,
                  new ReceiveFactory(ftpVisitor));
    thread.start();

    //
    // Connection to the Server
    //

    int			port = ssock.getLocalPort();

    System.out.println("Port is " + port);
    sendCommand("port " + makePortAddress(localInetAddress, port));
    sendCommand("cwd " + directory);
    setBinary();
    sendCommand("retr " + filename);
    readResponse();

    nBytesRead = thread.dtaMonBytesTransf();

    thread = null;

  }

  /**
   * Parses a port address from the string returned by the "pasv" FTP command.
   * Assumes that the port address is enclosed in parentheses.
   * Extract it and return it as is.
   */

  String extractPortAddress(String s)
  throws Exception {
    int start = s.indexOf('(');
    int finish = s.indexOf(')');
    if (finish <= start)
      throw new Exception("Ill-formatted port address, in " + s);
    String r = s.substring(start + 1, finish);
    return r;
  }

  /**
   * Gets the number of bytes read
   */
  public long bytesTransf(){
    return nBytesRead;


  }

  /**
   * Returns FTP server response
   */
  public String retResponse(){
    return response;
  }
}

/**
 * A thread for downloading data from the host. Currently only used for downloading
 * data retrieved via the list() method.
 */

class IncomingDataMonitor extends Thread {
  ServerSocket	ssock;
  FTPFactory		ftpFactory;

  /**
    * Constructor for get file of remote host
    */

  public IncomingDataMonitor(ServerSocket ssock, FTPFactory ftpFactory) {
    this.ssock	    = ssock;
    this.ftpFactory	= ftpFactory;
  }

  /**
   * The thread's run method.
   */

  public synchronized void run() {
    Socket sock = null;

    System.out.println("**Monitor waiting for connection request**");

    try {
      sock = ssock.accept();
      System.out.println("**Monitor accepted connection**");

      ftpFactory.processSocket(sock);

    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    try {
      sock.close();
      ssock.close();

      System.out.println("Socket closed");
    } catch (IOException ex) {
      System.out.println("can not close Sockets ");
      ex.printStackTrace();
    }

  }

  public synchronized long dtaMonBytesTransf() {
    return ftpFactory.dtaMonBytesTransf();
  }

}
