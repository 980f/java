/* $Id: SubString.java,v 1.4 2000/06/05 00:08:57 alien Exp $ */
package net.paymate.ISO8583.factory;

public class SubString extends SubField {//hmmm, possibly confusing name
  protected String essence;

  public String Value(){
    return essence;
  }

  public String setto(String value){
    return essence=value;
  }

  public SubString(int s, int e, String value){
    super(s,e);
    setto(value);
  }

  public SubString(int s, int e){
    this(s,e,"");
  }

  public String insertInto(StringBuffer field){
    return super.inserter(field, essence, false/* not prefix */,' ');
  }

  public String setFrom(String msg){
    return setto(parse(msg));
  }

}
//$Id: SubString.java,v 1.4 2000/06/05 00:08:57 alien Exp $
