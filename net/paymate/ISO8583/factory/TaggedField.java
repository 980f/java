package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/TaggedField.java,v $
 * Description:  a tagged field is a two char tag and content.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;

public class TaggedField {
  String tag;
  public String content;

  public TaggedField setto(String newcontent){
    content=newcontent;
    return this;
  }

  public TaggedField(String tag,String initialValue) {
    this.tag=tag;
    this.content=initialValue;
  }

  public TaggedField(String tag) {
    this(tag,"");
  }

}