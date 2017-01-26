package net.paymate.net.ftp;

/*
 * FTPClient.java
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

public interface FTPClient
{
  public static final int UNKNOWN = 0;
  public static final int DIRECTORY = 1;
  public static final int FILE = 2;
  public static final int LINK = 3;
  public static final int GETLIST = 4;
  public static final int GETFILE = 5;

  /**
   * Open a connection to a remote host that runs an FTP server.
   */

  public void openConnection(String host, String user, String password)
  throws IOException, Exception;

      /**
       * Send a command to the host, and read the response.
       */

      public void sendCommand(String command)
      throws IOException,
      Exception;		// if the response is an error response

      /**
       * Read a response from the host.
       */

      public void readResponse()
      throws IOException;

      /**
       * Set the transfer mode to Ascii.
       */

      public void setAscii()
      throws IOException, Exception;

      /**
       * Set the transfer mode to Image (also referred to as "binary").
       */

      public void setBinary()
      throws IOException, Exception;

      /**
       * Change to the specified directory, which may be relative or absolute.
       */

      public void cwd(String dir)
      throws IOException, Exception;

      /**
       * Request a listing of the current directory. The result is sent on a new
       * data connection.
      */

      public Vector list()
      throws IOException, Exception;

      /**
       * Request to delete the specified file.
      */

      public void delete(String sDirectory, String sFile)
      throws IOException, Exception;

      /**
       * Request that the host allocate a port socket and listen for a data connection
       * from another host on that port. The port that it allocated is returned;
       * this is the port that is to be passed to the send() method.
      */

      public void send(String directory, String filename,
              FTPSender ftpSender)
      throws IOException, Exception;


      /**
       * Request that the host allocate a port socket and listen for a data connection
       * from another host on that port. The port that it allocated is returned;
       * this is the port that is to be passed to the receive() method.
      */

      public void receive(String directory, String filename,
                FTPVisitor ftpVisitor)
      throws IOException, Exception;

      /**
       * Close ftp connection
       */
      public void quit()
      throws IOException, Exception;
      /**
       * Gets the number of bytes read
       */

      public long bytesTransf();
      /**
       * Returns FTP server response
       */

      public String retResponse();
    }
