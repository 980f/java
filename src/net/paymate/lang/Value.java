/**
* Title:        Value
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Value.java,v 1.2 2003/07/27 18:54:34 mattm Exp $
*/
package net.paymate.lang;

import net.paymate.lang.ContentType;
import net.paymate.lang.ContentValid;

public class Value implements Comparable {//support for editing typed content
/*
 to simplify getting the users of this class hierarchy operational
 we have stubbed what will eventually be abstract members so that we
 can use this base class as a placeholder for NYI extended classes.
 */

  public ContentType charType(){//stub
    return new ContentType(ContentType.unknown);
  }

  public String Image(){//stub
    return "BaseValueError";
  }

  public long asLong(){//override when it makes sense
    return 0L;
  }

  public int asInt(){//override when it makes sense
    return 0;
  }

  public boolean asBoolean(){//override when it makes sense
    return false;
  }

  public boolean setto (String image){//stub
    return false;
  }

  public void Clear(){//stub
    return;
  }

  public boolean acceptChar(char keyStroke){
    return ContentValid.legalChar( charType(), keyStroke);
  }

  public boolean acceptString(String proposed){
    return ContentValid.legalChars(charType(), proposed) && setto(proposed);
  }
  /**
   * @param mask rules:A alpha N number S symbol X anything ucase==required lcase==optional
   * @return true if the value is compatible with the mask
   */
  public static final boolean fitsMask(String value,String mask){
    char ch;
    char type;
    int scanner;
    if(value.length()>mask.length()){
      return false; //string too long
    }

    int stop=value.length();
    for(scanner=0;scanner<stop;scanner++){
      ch=value.charAt(scanner);
      type=mask.charAt(scanner);
      if(!ContentValid.legalChar(ContentValid.typeForCode(type),ch)){
        return false;
      }
    }
    //remaining mask chars must all be optional
    while(scanner<mask.length()){
      if(Character.isUpperCase(mask.charAt(scanner))){//ucase flags mandatory
        return false;
      }
    }
    return true; //passed all tests
  }

   public int compareTo(Object o){
    if(o instanceof Value){
      return Image().compareTo(((Value)o).Image());
    }
    if(o instanceof String){
      return Image().compareToIgnoreCase(((String)o));
    }
    throw new ClassCastException();
   }

}
//$Id: Value.java,v 1.2 2003/07/27 18:54:34 mattm Exp $
