/* $Id: FieldDef.java,v 1.22 2001/11/14 01:47:49 andyh Exp $ */
package net.paymate.ISO8583.factory;

import net.paymate.data.*;
import net.paymate.util.*;

public class FieldDef extends ContentDefinition {
  public int isobit=-1; //guaranteed illegal

  public FieldDef(int isobit, ContentType ctype, int variableLength, int length){
    super(ctype, variableLength, length);
    this.isobit=         isobit;
  }

  public static final FieldDef Invalid =new FieldDef(-1, new ContentType(ContentType.unknown),0,-1);

  public boolean isValid(){
    return this!=Invalid;
  }

  public static FieldDef FieldSpec(int isobit, FieldDef lookup[]){//look up in packed storage
    for(int i=lookup.length;i-->0;){
      if(isobit==lookup[i].isobit){
        return  lookup[i];
      }
    }
    return FieldDef.Invalid; //error!!!
  }

}
//$Id: FieldDef.java,v 1.22 2001/11/14 01:47:49 andyh Exp $
