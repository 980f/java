package net.paymate.database;

/**
 * Title:         $Source: /cvs/src/net/paymate/database/TimeZoneInt.java,v $
 * Description: bridge old implementation's integer zones to java names.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: TimeZoneInt.java,v 1.3 2001/07/19 01:06:48 mattm Exp $
 */
import java.util.TimeZone;
import net.paymate.util.FormattedLines;
import net.paymate.util.FormattedLineItem;

import org.apache.ecs.html.*;

public class TimeZoneInt {

  public static final String NameFor(int hash){
    String[] zone=TimeZone.getAvailableIDs();
    int zl=zone.length;
    FormattedLines arf = new FormattedLines(zl);
    while(zl-->0){
      if(zone[zl].hashCode()==hash){
        return zone[zl];
      }
    }
    return "UTC"; //for any fialure
  }

  public static final int NumberFor(String officialName){
    return officialName.hashCode();
  }

  public static final FormattedLines All() {
    String[] zone=TimeZone.getAvailableIDs();
    int zl=zone.length;
    FormattedLines arf = new FormattedLines(zl);
    while(zl-->0){
      arf.add(new FormattedLineItem(zone[zl],Integer.toString(zone[zl].hashCode())));
    }
    return arf;
  }

  public static final Table Tableize(FormattedLines list){      //two column table
    Table html=new Table(1);
    int len=list.size();
    while(len-->0){
      FormattedLineItem line=list.itemAt(len);
      TR row=new TR();
      row.addElement(new TD(line.name)).addElement(new TD(line.value));
      html.addElement(row);
    }
    return html;
  }

  public static final void main(String argv[]){
    FormattedLines lzlist=All();
    Table table=Tableize(lzlist);
    System.out.print(table.toString());
  }

}