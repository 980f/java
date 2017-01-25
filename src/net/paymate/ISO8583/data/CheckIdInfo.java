package net.paymate.ISO8583.data;
/* $Id: CheckIdInfo.java,v 1.20 2001/07/19 01:06:44 mattm Exp $ */

import net.paymate.util.*;
import net.paymate.data.*;

public class CheckIdInfo implements isEasy {
  public long ssn;
  protected final static String ssnKey="SSN";
  public DriversLicense license=new DriversLicense();
  public AltID otherCard=new AltID();

  public CheckIdInfo Clear(){
    if(license==null){
      license=new DriversLicense();
    }
    license.Clear();
    if(otherCard==null){
      otherCard=new AltID();
    }
    otherCard.Clear();
    ssn=0;
    return this;
  }

  public CheckIdInfo enForced(boolean forceit){//+_+ cute name
    return forceit?  Clear(): this;
  }

  public boolean isPresent(){
    return DriversLicense.NonTrivial(license) || AltID.NonTrivial(otherCard);
  }

  public static final boolean useAble(CheckIdInfo idInfo){
    return idInfo!=null && idInfo.isPresent();
  }

  public CheckIdInfo(){
    ssn=0;
    license=new DriversLicense();
    otherCard=new AltID();
  }

  public CheckIdInfo(CheckIdInfo old){
    ssn=old.ssn;
    license=new DriversLicense(old.license);
    otherCard=new AltID(old.otherCard);
  }
  ///////////////////////////////////////////////////////
  public String Spam(){
    return " "+license.Image();
  }

  public String toSpam() {
    return "license="+license.Image();
  }

  ///////////////////////////////////////////////////////

  public void save(EasyCursor ezp){
    if(ssn!=0){
      ezp.setLong(ssnKey,  ssn);
    }
    if(DriversLicense.NonTrivial(license)){//+_+ might have to weaken to 'present'
      license.save(ezp);
    }
    if(AltID.NonTrivial(otherCard)){
      otherCard.save(ezp);
    }
  }

  public void load(EasyCursor ezp){
    ssn=  ezp.getLong(ssnKey);
    license   = new DriversLicense(ezp);
    otherCard = new AltID(ezp);
    //begin patch for wierd save's:
    if(!license.isLegal()){
      if(otherCard.isPresent()){
        //attempt to do a license parsing on the "other card"

      }
    }
  }

  public CheckIdInfo(EasyCursor ezp){
    this();
    load(ezp);
  }

}
//$Id: CheckIdInfo.java,v 1.20 2001/07/19 01:06:44 mattm Exp $
