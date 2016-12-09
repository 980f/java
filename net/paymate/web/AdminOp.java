package net.paymate.web;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: AdminOp.java,v 1.1 2001/06/01 02:01:28 mattm Exp $
 */

public class AdminOp {
  private String name;
  private AdminOpCode code;

  public AdminOp(String name, AdminOpCode code) {
    this.name=name;
    this.code=code;
  }
  public static final String adminPrefix = "adm";
  public String name() {
    return name;
  }
  public String url() {
    return (code != null) ? (adminPrefix + "=" + code.Image()) : "";
  }
  public String codeImage() {
    return code.Image();
  }
}

