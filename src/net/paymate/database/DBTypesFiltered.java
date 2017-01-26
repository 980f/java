/**
 * Title:        DBTypesFiltered
 * Description:  DBTypes is useless as it is, as the type names that come from the
 *               database contain numbers, underscores, and spaces
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DBTypesFiltered.java,v 1.20 2003/07/27 05:35:00 mattm Exp $
 */

package net.paymate.database;
import  net.paymate.util.ErrorLogStream;
import net.paymate.lang.StringX;

/*
 We only use a few types:
 TEXT    [unlimited in PG]
 BOOLEAN [a TRUE boolean datatype]
 INTEGER [4 bytes]
 LONG    [8 bytes]

 For config data, these fields can be as long as we think they will ever need to be.
 For log data, make them as short as you think they can ever stand to be (for speed due to smaller table sizes)
*/

public class DBTypesFiltered extends DBTypes {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(DBTypesFiltered.class, ErrorLogStream.WARNING);

  public DBTypesFiltered(String textValue) {
    super(textValue);
    if (!isLegal()) {
      map(textValue);
    }
    dbg.VERBOSE("textType '" + textValue + "' mapped to '" + Image() + "'.");
  }

  private void map(String test) {
    for(int i = THEMAP.length; i-->0;) {
      TypeMap mapping = THEMAP[i];
      if(mapping.is(test)) {
        setto(mapping.type);
        break;
      }
    }
  }

  // put the most frequently used at the bottom, as it will be scanned bottom-up
  private static final TypeMap [ ] THEMAP = {
      new TypeMap("DEC"    ,DBTypes.DECIMAL),
      new TypeMap("INT"    ,DBTypes.INT4),
      new TypeMap("INTEGER",DBTypes.INT4),
      new TypeMap("BOOLEAN",DBTypes.BOOL),
  };

  public DBTypesFiltered(int dbtype){
    super(dbtype);
  }
}

class TypeMap {
  String representation;
  int type;
  public TypeMap(String representation, int type) {
    this.representation = representation;
    this.type = type;
  }
  public boolean is(String text) {
    return StringX.equalStrings(representation, text, true); // ignore case
  }
}
