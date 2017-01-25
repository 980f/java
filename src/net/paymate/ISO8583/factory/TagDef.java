package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/TagDef.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */


import net.paymate.data.*;
import net.paymate.util.*;

public class TagDef extends ContentDefinition {
  String tag;//usually two characters

  public TagDef(String tag, ContentType ctype, int variableLength, int length) {
    super(ctype, variableLength, length);
    this.tag=tag;
  }

  public static final TagDef Invalid =new TagDef("", new ContentType(ContentType.unknown),0,-1);

  public boolean isValid(){
    return this!=Invalid;
  }


}
//$Id: TagDef.java,v 1.1 2001/11/14 13:53:45 andyh Exp $