package net.paymate.jpos.Terminal;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/KeyFiddler.java,v $
 * Description:  fancy shift logic for getting alpha from numeric keypad.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class KeyFiddler {
  /*
  the caret '^' brackets sets of characters to rotate through.
  Note that lower case is not enterable nor shifted.
  */

  String keyloops="^1QZ.^2ABC^3DEF^4GHI^5JKL^6MNO^7PRS^8TUV^9WXY^*,'\"^0- ^";
  char bracket=keyloops.charAt(0); //this is true regardless of which char is designated as a bracket

  public char shift(char previous){
    int present=keyloops.indexOf(previous);
    if(present>=0){
      if(keyloops.charAt(++present)==bracket){//rewind
        present=keyloops.lastIndexOf(bracket,present-1)+1;
      }
      return previous=keyloops.charAt(present);
    } else {
      return 0;
    }
  }

  public KeyFiddler() {
  //
  }
}
//$Id: KeyFiddler.java,v 1.2 2002/05/03 22:33:58 andyh Exp $