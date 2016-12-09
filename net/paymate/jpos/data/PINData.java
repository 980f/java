/* $Id: PINData.java,v 1.15 2001/10/22 23:33:39 andyh Exp $ */
package net.paymate.jpos.data;
import net.paymate.util.*;

public class PINData implements isEasy  {
  boolean isPresent;
  long pinAsInt;  //64 bit integer

  public long Value(){
    return pinAsInt;
  }

  public boolean NonTrivial(){
    return isPresent && pinAsInt!=0;
  }

  public void save(EasyCursor ezp){
    ezp.setLong("pinAsInt",pinAsInt);
  }

  public void load(EasyCursor ezp){
    pinAsInt=ezp.getLong("pinAsInt");
    isPresent=true;
  }

  public PINData(String asciihex16){
    pinAsInt=Safe.parseLong(asciihex16,16);
    isPresent=true;
  }

  public PINData setto(PINData old){
    pinAsInt=old.pinAsInt;
    isPresent=old.isPresent;
    return this;
  }

  public PINData(PINData old){
    this();
    setto(old);
  }

  public void Clear(){
    isPresent=false;
    pinAsInt=0;
  }

  public PINData() {
    Clear();
  }

}
//$Id: PINData.java,v 1.15 2001/10/22 23:33:39 andyh Exp $
