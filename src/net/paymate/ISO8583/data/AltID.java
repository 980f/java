/**
* Title:        AltID
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: AltID.java,v 1.9 2001/07/19 01:06:44 mattm Exp $
*/
package net.paymate.ISO8583.data;

import net.paymate.util.*;

public class AltID implements isEasy {
  public AltIDType idType;
  protected final static String idTypeKey="idType";
  public String Number;
  protected final static String NumberKey="Number";

  public void Clear(){
    if(idType==null){
      idType=new AltIDType();
    }
    idType.Clear();
    Number="";
  }

  AltID(){
    idType=new AltIDType(AltIDType.OT);
    Number="";
  }

  public AltID(AltID old){
    idType=new AltIDType(old.idType);
    Number=new String(old.Number);
  }

  public boolean isPresent(){
    return Safe.NonTrivial(Number);
  }

  public static final boolean NonTrivial(AltID anID){
    return anID!=null && anID.isPresent();
  }

  public void save(EasyCursor ezp){
    ezp.setString(NumberKey, Number);
    ezp.saveEnum(idTypeKey,idType);
  }

  public void load(EasyCursor ezp){
    Number=ezp.getString(NumberKey);
    ezp.loadEnum(idTypeKey,idType);
  }

  public AltID(EasyCursor ezp){
    this();
    load(ezp);
  }

}
//$Id: AltID.java,v 1.9 2001/07/19 01:06:44 mattm Exp $
