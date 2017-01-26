/**
* Title:        EnumValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: EnumValue.java,v 1.8 2003/07/27 05:34:57 mattm Exp $
*/
package net.paymate.data;

import net.paymate.util.*;
import net.paymate.lang.Value;
import net.paymate.lang.ContentType;
import net.paymate.lang.TrueEnum;

// +++ move to np.lang?

public class EnumValue extends Value {
  TrueEnum content;

  public ContentType charType(){
    return new ContentType(ContentType.select);
  }

  public TrueEnum Content(){
    return content;
  }

  public TrueEnum Value(){
    return TrueEnum.Clone(content);
  }

  public String ImageFor(int digit){//for keyboard picking
    content.setto(digit);
    return content.menuImage();
  }

  public String Image(){
    return content.Image();
  }

  public boolean setto(String image){
    content.setto(image);
    return content.isLegal();
  }

  public boolean setto(int token){
    content.setto(token);
    return content.isLegal();
  }

  public int asInt(){
    return content.Value();
  }

  public void Clear(){
    content.setto(content.Invalid());
  }

  /**
   * @param design is called that as its value may be discarded, we are mostly interested in its underlying type
   */
  public EnumValue(TrueEnum design){
    content=design;
  }

}

//$Id: EnumValue.java,v 1.8 2003/07/27 05:34:57 mattm Exp $
