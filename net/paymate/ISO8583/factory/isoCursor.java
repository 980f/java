package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/isoCursor.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;

public class isoCursor {//string chomper
    int chi;
    String masterCopy;

    public isoCursor(String given,int offset){
      chi=offset; //index into block
      masterCopy=given;
    }

    public String nextPiece(int length){
      if(length>0){
        if(stillHave(length)){
          return masterCopy.substring(chi,chi+=length);
        }
        else {
          ErrorLogStream.Debug.WARNING("Ran out of input"); //not enough info for a good message.
        }
      }
      return "";
    }

    long byRadix(int length, int radix){
      try{
        String piece=nextPiece(length);
        return Safe.parseLong(piece,radix);
      } catch(NumberFormatException ignored){
        return Long.MAX_VALUE; //all iso fields are unsigned
      } catch(IndexOutOfBoundsException ignored){ //short field
        return -1; //should blow mightily if used.
      }
    }

    public long decimal(int length){ //convert these many chars to an integer
      return byRadix(length,10);
    }

    public long hexadecimal(int length){ //convert these many chars to an integer
      return byRadix(length,16);
    }

    public boolean stillHave(int howmany){
      return (chi+howmany)<=masterCopy.length();
    }

  }
//$Id: isoCursor.java,v 1.1 2001/11/14 13:53:45 andyh Exp $