package net.paymate.database;
import  net.paymate.util.*;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TableInfo.java,v 1.2 2001/06/17 16:48:40 mattm Exp $
 */

public class TableInfo {

  private String catalog;
  private String schema;
  private String name;
  private String type;
  private String remark;

  public TableInfo(String catalog, String schema, String name, String type, String remark) {
    this.catalog=Safe.TrivialDefault(catalog, "");
    this.schema=Safe.TrivialDefault(schema, "");
    this.name=Safe.TrivialDefault(name, "");
    this.type=Safe.TrivialDefault(type, "");
    this.remark=Safe.TrivialDefault(remark, "");
  }

  public TableInfo(String name) {
    this("", "", name, "", "");
  }

  public String catalog() {
    return catalog;
  }
  public String schema() {
    return schema;
  }
  public String name() {
    return name;
  }
  public String type() {
    return type;
  }
  public String remark() {
    return remark;
  }
}
