package net.paymate.net;

/**
 * Title:        SmtpMail
 * Description:  An implementation of the SMTP protocol for sending email.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: SmtpMail.java,v 1.3 2001/10/28 07:01:55 mattm Exp $
 *
 * Note: Make one of these for EVERY email you are going to send!
 *       This class is not multithreaded!
 *
 * See main(), at the bottom of this file for an example of usage.
 *
 * +++ Pass an outputstream into the constructor so that it can log its activities to a file?
 */

import java.io.*;
import java.net.*;
import java.util.*;
import net.paymate.util.*;

public class SmtpMail {

  private static final ErrorLogStream dbg = new ErrorLogStream(SmtpMail.class.getName(), ErrorLogStream.WARNING);

  private static final int DEFAULT_MAIL_PORT = 25;
  private static final int MAX_LINE_LENGTH = 80;

  private int mailPort = DEFAULT_MAIL_PORT;

  private boolean precheck = true;

  private String mailServer = "";
  private String mailFrom = "";
  private String mailMessage = "";
  private String mailSubject = "";
  private String heloHost = "DUDE";
  private String errorMessage = "";
  private String popIP = "";
  private String popUser = "";
  private String popPwd = "";
  private Vector mailTo = new Vector();
  private Vector mailCC = new Vector();
  private Vector mailBCC = new Vector();

  private BufferedReader readSocket;
  private PrintWriter writeSocket;
  private Socket socket = null;


  private SmtpMail() {
    // not usable
  }

  // +++ make this class use the IP:port stuff in IPSpec !!!

  /**
   * Constructor requires the resolvable hostname or IP address
   * of the SMTP mail server.
   *
   * @param mailServer Resolvable hostname or IP address of an SMTP mail server.
   * @param precheck Whether or not to check pop mail before sending this email.
   */
  public SmtpMail(String mailServer, boolean precheck, String popIP, String popUser, String popPwd) {
    this();
    try {
      //determine name of the local machine for HELO handshake
      heloHost = InetAddress.getLocalHost().toString();
    } catch (UnknownHostException e) {
      // stub
    }
    this.mailServer = mailServer;
    this.popIP = popIP;
    this.popUser = popUser;
    this.popPwd = popPwd;
  }


  /**
   * Returns the hostname or IP address of the mail server.
   *
   * @return Hostname or IP address of the mail server.
   */
  public String getMailServer() {
    return mailServer;
  }


  /**
   * Sets the location of the mail server.
   *
   * @param mailServer Hostname or IP address of the mail server.
   */
  public void setMailServer(String mailServer) {
    this.mailServer = mailServer;
  }


  /**
   * Returns the port on which the mail server is listening.
   *
   * @return Port number on which the mail server is listening.
   */
  public int getMailPort() {
    return mailPort;
  }


  /**
   * Sets the mail port on which the mail server is listening.
   *
   * @param mailPort Port number on which the mail server is
   *  listening.
   */
  public void setMailPort(int mailPort) {
    this.mailPort = mailPort;
  }


  /**
   * Returns the sender's e-mail address.
   *
   * @return Sender's e-mail address.
   */
  public String getMailFrom() {
    return mailFrom;
  }


  /**
   * Sets the sender's e-mail address.
   *
   * @param mailFrom Sender's e-mail address.
   */
  public void setMailFrom(String mailFrom) {
    this.mailFrom = mailFrom;
  }


  /**
   * Returns a vector containing e-mail addresses of all
   * recipients.
   *
   * @return Vector containing e-mail addresses of all
   *  recipients.
   */
  public Vector getMailTo() {
    return mailTo;
  }


  /**
   * Adds a single e-mail address to list of recipients.
   *
   * @param address E-mail address to add to list of recipients.
   */
  public void addMailTo(String address) {
    mailTo.add(address);
  }


  /**
   * Populates the recipients list from either a single e-mail
   * address or a comma-delimited list of addresses.
   *
   * @param address E-mail address or comma-delimited list of
   *  addresses to add to recipient list.
   */
  public void setMailTo(String address) {
    String delimiter = ",";
    if (address.indexOf(";") != -1) {
      delimiter = ";";
    }
    StringTokenizer addressTokens = new StringTokenizer(address, delimiter);
    //iterate through all of the addresses in the comma-delimited
    //list adding each one to the recipients Vector
    while (addressTokens.hasMoreTokens()) {
      addMailTo(addressTokens.nextToken());
    }
  }


  /**
   * Clears the recipient list.
   */
  public void clearMailTo() {
    mailTo.clear();
  }


  /**
   * Returns a vector containing e-mail addresses of all carbon
   * copy recipients.
   *
   * @return Vector containing e-mail addresses of all carbon
   *  copy recipients.
   */
  public Vector getMailCC() {
    return mailCC;
  }


  /**
   * Adds a single e-mail address to list of carbon copy
   * recipients.
   *
   * @param address E-mail address to add to list of carbon copy
   *  recipients.
   */
  public void addMailCC(String ccAddress) {
    mailCC.add(ccAddress);
  }


  /**
   * Populates the carbon copy recipients list from either a
   * single e-mail address or a comma-delimited list of
   * addresses.
   *
   * @param address E-mail address or comma-delimited list of
   *  addresses to add to the carbon copy recipient list.
   */
  public void setMailCC(String ccAddress) {
    String delimiter = ",";
    if (ccAddress.indexOf(";") != -1) {
      delimiter = ";";
    }
    StringTokenizer ccAddressTokens = new StringTokenizer(ccAddress, delimiter);
    //iterate through all of the cc addresses in the comma-
    //delimited list adding each one to the recipients Vector
    while (ccAddressTokens.hasMoreTokens()) {
      addMailCC(ccAddressTokens.nextToken());
    }
  }


  /**
   * Clears the carbon copy recipient list.
   */
  public void clearMailCC() {
    mailCC.clear();
  }


  /**
   * Returns a vector containing e-mail addresses of all blind
   * carbon copy recipients.
   *
   * @return Vector containing e-mail addresses of all blind
   *  carbon copy recipients.
   */
  public Vector getMailBCC() {
    return mailBCC;
  }


  /**
   * Adds a single e-mail address to list of blind carbon copy
   * recipients.
   *
   * @param address E-mail address to add to list of blind carbon
   *  copy recipients.
   */
  public void addMailBCC(String bccAddress) {
    mailBCC.add(bccAddress);
  }


  /**
   * Populates the blind carbon copy recipients list from either
   * a single e-mail address or a comma-delimited list of
   * addresses.
   *
   * @param address E-mail address or comma-delimited list of
   *  addresses to add to the blind carbon copy recipient list.
   */
  public void setMailBCC(String bccAddress) {
    String delimiter = ",";
    if (bccAddress.indexOf(";") != -1) {
      delimiter = ";";
    }
    StringTokenizer bccAddressTokens = new StringTokenizer(bccAddress, delimiter);
    //iterate through all of the bcc addresses in the comma-
    //delimited list adding each one to the recipients Vector
    while (bccAddressTokens.hasMoreTokens()) {
      addMailBCC(bccAddressTokens.nextToken());
    }
  }


  /**
   * Clears the recipient list.
   */
  public void clearMailBCC(){
    mailBCC.clear();
  }


  /**
   * Returns the subject of the current message.
   *
   * @return Subject of the current message.
   */
  public String getMailSubject() {
    return mailSubject;
  }


  /**
   * Sets the subject of the current message.
   *
   * @param mailSubject Subject of the current message.
   */
  public void setMailSubject(String mailSubject) {
    this.mailSubject = mailSubject;
  }


  /**
   * Gets the current mail message.
   *
   * @return Current mail message.
   */
  public String getMailMessage() {
    return mailMessage;
  }


  /**
   * Sets the current mail message.
   *
   * @param mailMessage Current mail message.
   */
  public void setMailMessage(String mailMessage) {
    this.mailMessage = mailMessage;
  }


  /**
   * Returns the name of this machine.
   *
   * @return Name of this machine sent in the HELO handshake.
   */
  public String getHeloHost() {
    return heloHost;
  }


  /**
   * Sets the name of this server to send in HELO SMTP handshake.
   *
   * @param heloHost Name of this server used in HELO handshake.
   */
  public void setHeloHost(String heloHost) {
    this.heloHost = heloHost;
  }


  /**
   * Returns an error message in case an exception is thrown.
   *
   * @return Error message.
   */
  public String getErrorMessage() {
    return errorMessage;
  }


  // <SUPERHACK>
  // +++ Move this to a PopMail class when we have one.
  /**
   * Allows you to use POP to precheck the mailbox for systems that require that for general internet SMTP access.
   */
  private boolean popPreCheck(String toIP, String username, String password) {
    boolean ret = false;
    Socket socket = null;
    BufferedReader readSocket = null;
    PrintWriter writeSocket = null;
    try {
      dbg.Enter("popPreCheck");
      socket = new Socket(toIP, 110);
      readSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writeSocket = new PrintWriter(socket.getOutputStream(), true);
      for(int i = 0; i < 4; i++) {
        String cmd = "";
        String rr = "";
        switch(i) {
          case 0: {
            cmd = "USER "+username;
          } break;
          case 1: {
            cmd = "PASS "+password;
          } break;
          case 2: {
            cmd = "STAT";
          } break;
          case 3: {
            cmd = "QUIT";
          } break;
        }
        writeSocket.println(cmd);
        String rets = readSocket.readLine(); //read response
        dbg.WARNING("Sending \""+cmd+"\" returned \""+rets+"\".");
        if(!rets.startsWith("+OK")) {
          errorMessage = "SmtpMail Error: " + rets;
          throw new Exception(errorMessage);
        }
      }
      ret = true;
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      try {
        if(readSocket != null) {
          readSocket.close();
        }
        if(writeSocket != null) {
          writeSocket.close();
        }
        if(socket != null) {
          socket.close();
        }
      } catch (Exception e) {
        dbg.Caught(e);
      }
      return ret;
    }
  }
  // </SUPERHACK>


  /**
   * Sends the mail message using the current settings.
   */
  public boolean send() {
    return (precheck ? popPreCheck(popIP, popUser, popPwd) : true) && sendMessage();
  }


  /**
   * Sends the specified message using the current settings.
   *
   * @param mailMessage Message to send.
   */
  public boolean send(String mailMessage) {
    this.mailMessage = mailMessage;
    return send();
  }


  /**
   * Sends a mail message using the current settings.
   *
   * @return True indicates success.
   */
  private boolean sendMessage() {
    errorMessage = "";
    if (mailServer.equals("")) {
      errorMessage = "SmtpMail Error: No mail server";
      return false;
    }
    try {
      dbg.Enter("sendMessage");
      openSocket();       //open socket to mail server
      getInputStream();   //get handle to input stream
      getOutputStream();  //get handle to output stream
      connect();          //SMTP handshaking
      sendEnvelope();     //send addressing information
      sendData();         //send message content
      disconnect();       //end SMTP session
    } catch(Exception e) {
      errorMessage = e.toString();
    } finally {
      dbg.Exit();
      closeOutputStream();
      closeInputStream();
      closeSocket();
      return (errorMessage == ""); //return true if no errors
    }
  }


  /**
   * Opens a socket connection to the mail server.
   *
   * @throws Exception if unable to open socket to mail server.
   */
  private void openSocket() throws Exception {
    socket = new Socket(mailServer, mailPort);
  }


  /**
   * Closes the socket connection with the mail server.
   */
  private void closeSocket() {
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
      }
    }
  }


  /**
   * Opens an input stream from the mail server.
   *
   * @throws Exception if there is a problem opening the input
   *  stream from the mail server.
   */
  private void getInputStream() throws Exception {
    readSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }


  /**
   * Closes the input stream from the mail server.
   */
  private void closeInputStream() {
    if (readSocket != null) {
      try {
        readSocket.close();
      } catch (IOException e) {
      }
    }
  }


  /**
   * Opens the output stream to the mail server.
   *
   * @throws Exception if there is a problem opening the output
   *  stream to the mail server.
   */
  private void getOutputStream() throws Exception {
    writeSocket = new PrintWriter(socket.getOutputStream(), true);
  }


  /**
   * Closes the output stream to the mail server.
   */
  private void closeOutputStream() {
    if (writeSocket != null) {
      writeSocket.close();
    }
  }


  /**
   * Initiates SMTP session with initial handshaking.
   *
   * @throws Exception if mail server returns an unexpected
   *  response.
   */
  private void connect() throws Exception {
    getResponse("220");
    sendCommand("HELO " + heloHost, "250");
  }


  /**
   * Ends the SMTP session
   *
   * @throws Exception if mail server returns an unexpected
   *  response.
   */
  private void disconnect() throws Exception {
    sendCommand("QUIT", "221");
  }


  /**
   * Sends addressing information for this message to the mail
   * server.
   *
   * @throws Exception if mail server returns an unexpected
   *  response.
   */
  private void sendEnvelope() throws Exception {
    String value = "";
    if (mailTo.isEmpty()) {
      throw new Exception("SmtpMail Error: No recipients");
    }
    sendCommand("MAIL FROM: " + getEmail(mailFrom), "250");
    //send list of recipients
    Enumeration enum = mailTo.elements();
    while (enum.hasMoreElements()) {
      value = getEmail((String)enum.nextElement());
      sendCommand("RCPT TO: " + value, "250");
    }

    //send list of carbon copy recipients
    enum = mailCC.elements();
    while (enum.hasMoreElements()) {
      value = getEmail((String)enum.nextElement());
      sendCommand("RCPT TO: " + value, "250");
    }

    //send list of blind carbon copy recipients
    enum = mailBCC.elements();
    while (enum.hasMoreElements()) {
      value = getEmail((String)enum.nextElement());
      sendCommand("RCPT TO: " + value, "250");
    }
  }


  /**
   * Parses out the email portion of an address in the following
   * format: Dustin Callaway <dustin@sourcestream.com>
   * This format allows a mail client to display the user's name
   * in the From and To fields rather than email addresses.
   *
   * @param address Name/email of a to, cc, or bcc recipient.
   * @return Email portion of the name/email string.
   */
  private String getEmail(String address) {
    int beginEmail = address.indexOf("<");
    if (beginEmail != -1) {
      int endEmail = address.indexOf(">");
      if ((endEmail == -1) && (endEmail < beginEmail)) {
        address = address.substring(beginEmail + 1);
      } else {
        address = address.substring(beginEmail + 1, endEmail);
      }
    }
    return address;
  }


  /**
   * Sends the message content to the mail server. Send Date,
   * From, To, CC, and Subject headings to allow mail client to
   * display them.
   *
   * @throws Exception if mail server returns an unexpected
   *  response.
   */
  private void sendData() throws Exception {
    String value = "";
    sendCommand("DATA", "354");
    writeSocket.println("Date: " + new Date());
    writeSocket.println("From: " + mailFrom);
    //send list of recipients
    Enumeration enum = mailTo.elements();
    while (enum.hasMoreElements()) {
      value = (String)enum.nextElement();
      writeSocket.println("To: " + value);
    }
    //send list of carbon copy recipients
    enum = mailCC.elements();
    while (enum.hasMoreElements()) {
      value = (String)enum.nextElement();
      writeSocket.println("CC: " + value);
    }
    writeSocket.println("Subject: " + mailSubject);
    writeSocket.println();
    //wrap all lines in message that exceed MAX_LINE_LENGTH
    mailMessage = wordWrap(mailMessage, MAX_LINE_LENGTH);
    //create a buffered reader to read message one line at a time
    BufferedReader messageReader = new BufferedReader(new StringReader(mailMessage));
    String line = "";
    //send each line of the message to the mail server
    while((line = messageReader.readLine()) != null) {
      if (line.equals(".")) {
        line = ".."; //prevents user from ending message
      }
      writeSocket.println(line);
    }
    writeSocket.println();   //send blank line
    sendCommand(".", "250"); //end message with single period
  }


  /**
   * Sends a command to the mail server and receives a reply.
   *
   * @param command Command to send to mail server.
   * @param expectedResponse Response expected from mail server
   *  if no errors occur.
   * @throws Exception if mail server returns an unexpected
   *  response.
   */
  private String sendCommand(String command, String expectedResponse) throws Exception {
    writeSocket.println(command);
    String ret = getResponse(expectedResponse);
    dbg.WARNING("Sending \""+command+"\" returned \""+ret+"\".");
    return ret;
  }


  /**
   * Receives a response from the mail server.
   *
   * @param expectedResponse Value expected from mail server in
   *  response to prior command.
   * @return Response from mail server.
   * @throws Exception if mail server returns an unexpected response.
   */
  private String getResponse(String expectedResponse) throws Exception {
    String response = readSocket.readLine(); //read response
    //if response is not what we expected, throw exception
    if (!response.startsWith(expectedResponse)) {
      errorMessage = "SmtpMail Error: " + response;
      throw new Exception(errorMessage);
    }
    //discard the rest of the valid response lines
    while(response.startsWith(expectedResponse + "-")) {
      response = readSocket.readLine();
    }
    return response;
  }


  /**
   * Wraps message lines longer than specified length.
   *
   * @param message Message to wrap.
   * @param lineLength Maximum length of line before wrapping.
   */
  private String wordWrap(String message, int lineLength) {
    String word;
    int column=0;
    int length;
    StringBuffer messageBuffer = new StringBuffer();
    //tokenize the message by spaces (break into single words)
    if(Safe.NonTrivial(message)) {
      StringTokenizer words = new StringTokenizer(message, " ");
      while (words.hasMoreTokens()) { //iterate through each word
        word = words.nextToken();
        length = word.length();
        //word exceeds line length, print on next line
        if (column > 0 && (column + length) > lineLength) {
          messageBuffer.append("\n" + word + " ");
          column = length + 1;
        } else if (word.endsWith("\n")) { //word ends current line
          messageBuffer.append(word);
          column = 0;
        } else { //word does not exceed line length or end line
          messageBuffer.append(word + " ");
          column += length + 1;
        }
      }
    }
    return messageBuffer.toString(); //return wrapped message
  }

  public static final void main(String [] args) {
    SmtpMail mailer = new SmtpMail("smtp.paymate.net", true, "smtp.paymate.net", "info", "pm1234");
    try {
      mailer.setHeloHost("DUDE");
      mailer.setMailFrom("PayMate.net <info@paymate.net>");
      mailer.setMailTo("Matt Mello <alien@spaceship.com>");
      mailer.setMailSubject("SUPERTEST");
      System.out.println("Send() returned " + mailer.send("This is a test!"));
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
    String error = mailer.getErrorMessage();
    if((error != null) && (error.length() > 0)) {
      System.out.println("Error: " + error);
    }
  }
}
