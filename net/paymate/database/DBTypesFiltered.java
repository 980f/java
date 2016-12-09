/**
 * Title:        DBTypesFiltered
 * Description:  DBTypes is useless as it is, as the type names that come from the
 *               database contain numbers, underscores, and spaces
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DBTypesFiltered.java,v 1.8 2001/10/24 04:14:18 mattm Exp $
 */

package net.paymate.database;
import  net.paymate.util.Safe;
import  net.paymate.util.ErrorLogStream;

import  java.text.MessageFormat;
import  java.text.ParsePosition;

public class DBTypesFiltered extends DBTypes {
  static final ErrorLogStream dbg=new ErrorLogStream(DBTypesFiltered.class.getName(), ErrorLogStream.WARNING);
  String fullSetting = "";

  public DBTypesFiltered(String textValue){
    super(textValue);
  }

  public DBTypesFiltered(int dbtype){
    super(dbtype);
  }

  public String Image(){
    switch(Value()){
      default: return super.Image();
      //problem type names go here:
      case DBTypes.DATETIME:{
      //+++ need to map the size. to extend this
        return super.Image();
      }
    }
  }

/**
 * Qualifier Field Valid Entries
 *   YEAR A year numbered from 1 to 9,999 (A.D.)
 *   MONTH A month numbered from 1 to 12
 *   DAY A day numbered from 1 to 31, as appropriate to the month
 *   HOUR An hour numbered from 0 (midnight) to 23
 *   MINUTE A minute numbered from 0 to 59
 *   SECOND A second numbered from 0 to 59
 *   FRACTION A decimal fraction of a second with up to 5 digits of precision.
 *     The default precision is 3 digits (a thousandth of a second). To
 *       indicate explicitly other precisions, write FRACTION(n), where n
 *         is the desired number of digits from 1 to 5.
 *
 *     Delimiter Placement in DATETIME Expression
 *     Hyphen Between the YEAR, MONTH, and DAY portions of the value
 *     Space Between the DAY and HOUR portions of the value
 *     Colon Between the HOUR and MINUTE and the MINUTE and SECOND portions of the value
 *     Decimal point Between the SECOND and FRACTION portions of the value
 *
 *   2000-01-16 12:42:06.00100
 *                       Fraction
 *                    Second
 *                 Minute
 *              Hour
 *           Day
 *        Month
 *   Year
 *
 * Need to look for the first '-', since the year could be a 2-number year.
 * Then, index after that for the other parts:
 * Year = 0 (if present)
 * Month = if year is present, first '-' + 1, else 0; width 2
 * Day = if Month is present, Month + 3, else 0; width 2
 * Hour =
 * Minute =
 * Second =
 * Fraction =
 *
 *
 * byte
 * char
 * decimal
 * int
 * serial
 * smallint
 * text
 * varchar
 */
/*
  public int setto(String textValue){
    int goodChars = 0;
    dbg.Enter("setto");
    try {
      textValue = textValue.toUpperCase();
      // this counts how many good chars we found in the string;
      fullSetting = textValue;
      while((goodChars < textValue.length()) && Character.isLetter(textValue.charAt(goodChars))) {
        goodChars++;
      }
      // strips the crap off the end (see DBTypes.Enum)
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
    return super.setto(textValue.substring(0, goodChars));
  }
*/

  // format: DATETIME largest_qualifier TO smallest_qualifier
  static final String formatStr = "DATETIME {0} TO {1}";
  MessageFormat mf = new MessageFormat(formatStr);
  /**
   * The date should be formatted like so: YYYY-MM-DD HH:mm:ss.iiiii
   */
  public String generateDate(String date, String rawFormat) {
    String newFormat = date;
    if(this.is(DBTypes.DATETIME)/* && Safe.NonTrivial(date) && Safe.NonTrivial(rawFormat)*/) {
      dbg.Enter("generateDate");
      try {
        rawFormat = Safe.TrivialDefault(rawFormat, "").toUpperCase();
        date = date.trim(); // just in case
        // we can only proceed if the year is there (probably) +_+ verify this
        // let's see how long the year is (everything else is standard:
        int yearLength = date.indexOf('-');
        if(yearLength > -1) {
          // get the date tokens
          ParsePosition parsePosition = new ParsePosition(0);
          Object[] objs = mf.parse(rawFormat, parsePosition);
          if(objs == null) {
            dbg.ERROR("ERROR! OBJs = null; parse erred at position " + parsePosition.getErrorIndex() + "; generateDate() couldn't parse the date '" + date + "' with format '" + rawFormat + "' using formatter '" + formatStr + "'!");
          } else {
            if((objs[0] == null) || (objs[1] == null)) {
              dbg.ERROR("ERROR! OBJlength=0; generateDate() couldn't parse the date '" + date + "' with format '" + rawFormat + "' using formatter '" + formatStr + "'!");
            }
            String token = objs[0].toString();
            DateToken first = new DateToken(token);
            token = objs[1].toString();
            DateToken last  = new DateToken(token);
            // determine the start point in the string to grab
            int startpos = 0;
            switch(first.Value()) {
              case DateToken.FRACTION: {
                startpos+=3; // 3 = "SS.".length
              }
              case DateToken.SECOND: {
                startpos+=3; // 3 = "mm:".length
              }
              case DateToken.MINUTE: {
                startpos+=3; // 3 = "HH:".length
              }
              case DateToken.HOUR: {
                startpos+=3; // 3 = "DD ".length
              }
              case DateToken.DAY: {
                startpos+=3; // 3 = "MM-".length
              }
              case DateToken.MONTH: {
                startpos+=yearLength+1; // 1 = "-".length
              } break;
              case DateToken.YEAR: {
              }
              default: {
                // leave it as zero
              } break;
            }
            // determine the end point in the string to grab
            int endpos = 0;
            switch(last.Value()) {
              case DateToken.FRACTION: {
                // +_+ maybe truncate this to just one decimal position (do we really need the rest?)
                int dotpos = date.indexOf('.');
                if(dotpos < 0) {
                  endpos+= date.length()-1;
                } else {
                  endpos+= date.length()-dotpos; // gets ".#####" (or whatever length it is)
                }
              }
              case DateToken.SECOND: {
                endpos+=3; // 3 = ":SS".length
              }
              case DateToken.MINUTE: {
                endpos+=3; // 3 = ":mm".length
              }
              case DateToken.HOUR: {
                endpos+=3; // 3 = " HH".length
              }
              case DateToken.DAY: {
                endpos+=3; // 3 = "-DD".length
              }
              case DateToken.MONTH: {
                endpos+=3; // 3 = "-MM".length
              }
              case DateToken.YEAR: {
                endpos+=yearLength;
              }
              default: {
                // leave it as zero
              } break;
            }
            if(endpos > startpos) {
              newFormat = date.substring(startpos,endpos);
              dbg.VERBOSE("Converted '" + date + "' to '" + newFormat + "' based on format '" + rawFormat + "'.");
            }
          }
        }
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        dbg.Exit();
      }
    }
    return newFormat;
  }


  public static final void main(String [] args) {

    // output test
    Object[] testArgs = {new Long(3), "MyDisk"};
    String msgFormatOut = "The disk \"{1}\" contains {0} file(s).";
    MessageFormat form = new MessageFormat(msgFormatOut);
    String allargs = "";
    for(int i = 0; i < testArgs.length; i++) {
      allargs += (i==0 ? "'" : "','") + testArgs[i].toString();
    }
    allargs += "'";
    System.out.println("Formatting the arguments: " + allargs + " with format '" +
      msgFormatOut + "' gives: '" + form.format(testArgs) + "'");

    // input test
    String [] tests = {
      "datetime hour to second",
      "datetime month to day",
      "datetime year to day",
      "datetime year to fraction(5)",
      "datetime year to minute",
      "datetime year to second",
    };
    String msgFormat = "datetime {0} to {1}";
    MessageFormat mf = new MessageFormat(msgFormat);
    for(int j = 0; j < tests.length; j++) {
      Object[] objs = mf.parse(tests[j], new ParsePosition(0));
      System.out.println("Output for parsing '" + tests[j] + "' + with format '" +
        msgFormat + "' is [" + objs.length + "]:");
      for(int i = 0; i < objs.length; i++) {
        if(objs[i] == null) {
          // ignore it
        } else {
          System.out.println("  " + i + ": '" + objs[i].toString() + "'");
        }
      }
    }
  }
}




