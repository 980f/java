package net.paymate.database;

/**
 * Title:        TableInfoList
 * Description:  List of Table Information structures, for database backup, creation, and copying
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TableInfoList.java,v 1.3 2001/10/27 07:17:28 mattm Exp $
 */

import java.util.Vector;
import net.paymate.util.*;

public class TableInfoList extends Vector{

  // Since this extends Vector, synchronizing makes sense

  public synchronized TableInfo itemAt(int i) {
    TableInfo ti = null;
    if(i < size() && i>-1) {
      ti = (TableInfo)elementAt(i);
    }
    return ti;
  }
  public synchronized int indexOf(String name) {
    int ret = -1;
    for(int i = size(); i-->0;) {
      if(itemAt(i).name().equals(name)) {
        ret = i;
        break;
      }
    }
    return ret;
  }
  public synchronized TextList justNames() {
    TextList tl = new TextList(size());
    for(int i = 0; i < size(); i++) {
      tl.add(itemAt(i).name());
    }
    return tl;
  }
}

