package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/EasyCursorServiceConfigurator.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

public class EasyCursorServiceConfigurator implements ServiceConfigurator {

  private EasyCursor parameters = null;
  public EasyCursorServiceConfigurator(EasyCursor parameters) {
    this.parameters = parameters;
  }

  // this class ignores the servicename!

  public String getServiceParam(String serviceName, String paramname, String defaultValue) {
    return parameters.getString(paramname, defaultValue);
  }
  public boolean getBooleanServiceParam(String serviceName, String paramname, boolean defaultValue) {
    return parameters.getBoolean(paramname, defaultValue);
  }
  public double getDoubleServiceParam(String serviceName, String paramname, double defaultValue) {
    return parameters.getNumber(paramname, defaultValue);
  }
  public long getLongServiceParam(String serviceName, String paramname, long defaultValue) {
    return parameters.getLong(paramname, defaultValue);
  }
  public int getIntServiceParam(String serviceName, String paramname, int defaultValue) {
    return parameters.getInt(paramname, defaultValue);
  }
  public EasyCursor getServiceParams(String serviceName, EasyCursor ezc) {
    return parameters;
  }
  public EasyCursor getAllServiceParams(String serviceName) {
    return parameters;
  }
  public EasyCursor setServiceParams(String serviceName, EasyCursor ezc) {
    parameters.addMore(ezc);
    return parameters;
  }
  public boolean setServiceParam(String serviceName, String paramname, String paramvalue) {
    parameters.setString(paramname, paramvalue);
    return true;
  }
}

