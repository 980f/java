package net.paymate.net.ftp;

/*
   Title:       Simple FTP Client in  Java
   Version:
   Copyright:   Copyright (c) 1999
   Company:     HAC
   Description: Definition of a File Entry
*/

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.lang.Integer;
import java.text.*;
import java.util.GregorianCalendar;

public class FTPFile {

  String			file[];

  public static final int UNKNOWN = 0;
  public static final int DIRECTORY = 1;
  public static final int FILE = 2;
  public static final int LINK = 3;

  //
  // Construction
  //

  public FTPFile(String sLine) {

    this.file = parseLine(sLine);

  }

  public String getFileName() {
    return file[8];
  }

  public String getFilePermissions() {
    return file[0];
  }

  public String getLinkCount() {
    return file[1];
  }

  public String getUserID() {
    return file[2];
  }

  public String getGroupID() {
    return file[3];
  }

  public String getFileSize() {
    return file[4];
  }

  public String getMonth() {
    return file[5];
  }

  public String getDayOfMonth() {
    return file[6];
  }

  public String getYear() {
    return file[7];
  }

  public String getTimeOfDay() {
    return file[7];
  }

  public String toString() {
    return getFileName();
  }

  public GregorianCalendar getFileModifiedTime() {
    GregorianCalendar	dtModifiedTime = null;

    dtModifiedTime  = new GregorianCalendar(
              ((file[7].indexOf(':')) != -1) ?
              Calendar.getInstance().get(Calendar.YEAR) :
              Integer.parseInt(file[7].toString()),
              file[5].equals("Jan") ? Calendar.JANUARY :
              file[5].equals("Feb") ? Calendar.FEBRUARY :
              file[5].equals("Mar") ? Calendar.MARCH :
              file[5].equals("Apr") ? Calendar.APRIL :
              file[5].equals("May") ? Calendar.MAY :
              file[5].equals("Jun") ? Calendar.JUNE :
              file[5].equals("Jul") ? Calendar.JULY :
              file[5].equals("Aug") ? Calendar.AUGUST :
              file[5].equals("Sep") ? Calendar.SEPTEMBER :
              file[5].equals("Oct") ? Calendar.OCTOBER :
              file[5].equals("Nov") ? Calendar.NOVEMBER :
              Calendar.DECEMBER,
              Integer.parseInt(file[6]),

              (file[7].indexOf(':') == -1) ? 0 :
              Integer.parseInt(file[7].substring(0,
                              file[7].indexOf(':')).trim()),
              (file[7].indexOf(':') == -1) ? 0 :
              Integer.parseInt(
              file[7].substring(
                file[7].indexOf(':') + 1,
                file[7].length())));

    return dtModifiedTime;

  }

  public String getFileModifiedTimeString() {

    return file[5] + " " + file[6] + " " + file[7];

  }

  int getFileType() {
    char		cType = getFilePermissions().charAt(0);

    if (cType == 'd') return FTPFile.DIRECTORY;
    if (cType == '-') return FTPFile.FILE;
    if (cType == 'l') return FTPFile.LINK;

    return FTPFile.UNKNOWN;

  }

  String getFileTypeValue() {

    switch (getFileType()) {
      case FTPFile.DIRECTORY:
        return "DIR";

      case FTPFile.FILE:
        return "FILE";

      case FTPFile.LINK:
        return "LINK";


    }

    return "UNKNOWN";

  }


  //
  // Parse the Line
  //

  String[] parseLine(String sLine) {

    String				sData[] = new String[9];
    StringTokenizer		sTokens = new StringTokenizer(sLine, " \t");

    sData[0] = sTokens.nextToken().trim();		// Permissions
    sData[1] = sTokens.nextToken().trim();		// Link Count
    sData[2] = sTokens.nextToken().trim();		// User ID

    if (sData[2].length() > 8) {
      sData[3] = sData[2].substring(9);
      sData[2] = sData[2].substring(0, 9).trim();
    } else {
      sData[3] = sTokens.nextToken().trim();
    }

    if (sData[3].length() > 8) {				// Group
      sData[4] = sData[3].substring(9);
      sData[3] = sData[3].substring(0, 9).trim();
    } else {
      sData[4] = sTokens.nextToken().trim();  // File Size
    }

    sData[5] = sTokens.nextToken().trim();		// Month
    sData[6] = sTokens.nextToken().trim();		// Day
    sData[7] = sTokens.nextToken().trim();		// Time/Year

    sData[8] = sTokens.nextToken().trim();		// File Name

    return sData;

  }

}
