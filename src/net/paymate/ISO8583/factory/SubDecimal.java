/* $Id: SubDecimal.java,v 1.5 2000/07/14 11:25:37 mattm Exp $ */
package net.paymate.ISO8583.factory;

import net.paymate.util.Safe;

public class SubDecimal extends SubField {//numeric field
  protected long essence;

  public long Value(){
    return essence;
  }

  public long setto(long value){
    return essence=value;
  }

  public SubDecimal(int s, int l, long value){
    super(s,l);
    setto(value);
  }

  public SubDecimal(int s, int l){
    this(s,l,0);
  }

  public String insertInto(StringBuffer field){
    return super.inserter(field, Long.toString(essence),true/* prefix */,'0');
  }

  public long setFrom(String msg){
    return setto(Safe.parseLong(parse(msg)));
  }

}
//$Id: SubDecimal.java,v 1.5 2000/07/14 11:25:37 mattm Exp $
