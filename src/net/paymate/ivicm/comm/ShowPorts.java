package net.paymate.ivicm.comm;

import java.io.*;
import java.util.*;
import javax.comm.*;

public class ShowPorts {
  static CommPortIdentifier portId;
  static Enumeration portList;

  public static final void main(String[] args) {
    portList = CommPortIdentifier.getPortIdentifiers();//4debug, manual invocation only

    while (portList.hasMoreElements()) {
      portId = (CommPortIdentifier) portList.nextElement();
      if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        System.out.println(portId.getName());
      }
    }
  }

}
//$Id: ShowPorts.java,v 1.4 2001/08/14 23:25:29 andyh Exp $
