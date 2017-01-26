package net.paymate.net.ftp;


/**
 * FTP Client
 *
 * @author   Neil Brittliff
 * @version  1.00, 13 September 1999
 */

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.lang.Integer;
import java.text.*;

public class Ftp {

  FTPClient	ftpClient;

  /**
   * Constructs/Creates an FTP Session with a Nominated Host
   *
   * @param		sHost The name of the Host
   * @param		sUserID The FTP Userid
   * @param		sPassword The FTP Password
   * @exception	Exception if things go wrong
  */

  public Ftp(String sHost, String sUserID, String sPassword)
  throws Exception {

    ftpClient = new FTPClientImpl();

    try {
      ftpClient.openConnection(sHost, sUserID, sPassword);
    } catch (Exception e) {
      log("RIO3001E: Unable to open ftp connection");
      e.printStackTrace();
      throw new Exception("FTP: " + e.toString());
    }

  }

  /**
   * Retrieves a Directory List from the nominated Host
   *
   * @param		sDirectory the name of the Nominated Directory
   * @returns		a Vectory of FTP File Objects
   * @exception	Exception if things go wrong
  */

  public Vector directoryList(String sDirectory)
  throws Exception {

    ftpClient.cwd(sDirectory);
    return ftpClient.list();

  }

  /**
   * Process a File from the Nominated Host
   *
   * @param		sDirectory the name of the Nominated Directory
   * @param		sFile the name of the File within the Directory
   * @param		ftpVisitor factory to process the File
   * @exception	Exception if things go wrong
  */

  public void getFile(String sDirectory, String sFile, FTPVisitor ftpVisitor)
  throws Exception {
    try {

      log("RIO3005I: getFile " + sDirectory + " " + sFile);

      ftpClient.receive(sDirectory, sFile, ftpVisitor);

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("getFile:" + e.toString());
    }

  }

  /**
   * Send a File to the Nominated Host
   *
   * @param		sDirectory the name of the Nominated Directory
   * @param		sFile the name of the File within the Directory
   * @param		ftpSender factory to send the File
   * @exception	Exception if things go wrong
  */

  public void sendFile(String sDirectory, String sFile, FTPSender ftpSender)
  throws Exception {
    try {

      log("RIO3005I: sendFile " + sDirectory + " " + sFile);

      ftpClient.send(sDirectory, sFile, ftpSender);

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("sendFile:" + e.toString());
    }

  }

  /**
   * Delete a File from the Nominated Host
   *
   * @param		sDirectory the name of the Nominated Directory
   * @param		sFile the name of the File within the Directory
   * @exception	Exception if things go wrong
  */

  public void deleteFile(String sDirectory, String sFile)
  throws Exception {
    try {

      log("RIO3006I: delete - " + sDirectory + "/" + sFile);

      ftpClient.delete(sDirectory, sFile);

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("deleteFile:" + e.toString());
    }

  }
  /**
  * Send an FTP Directory (MKD - Make a directory)
  *
  * @param		sCommand the Command
  * @exception	Exception if things go wrong
  */

  public void sendCommand(String sCommand)
  throws Exception {
    try {

      log("RIO3005I: sendCommand " + sCommand);

      ftpClient.sendCommand(sCommand);

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("sendCommand:" + e.toString());
    }

  }


  /**
   * Log Writer
   *
   * @param		sMesasge the message to write to the Log
  */

  public void log(String sMessage) {

    if (!Boolean.getBoolean(System.getProperty("debugOn", "true"))) {
      return;
    }

    Date      date  = new Date(System.currentTimeMillis());

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    System.out.println(sdf.format(date) + " " + sMessage);

  }

}
