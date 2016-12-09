package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/tester.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;

public class tester {
  static Tracer dbg=new Tracer(tester.class.getName());

  public static void testMTA(String[] args) {
    String input=args[1];
    MajorTaxArea mta=MajorTaxArea.FromIIN(Safe.parseInt(input));
    dbg.ERROR("mta from iin:"+mta.Image());
    dbg.ERROR("iin from mta:"+mta.IIN());
  }

  public static void main(String[] args) {
    try {
      String testname=args[0];
      if(testname.equalsIgnoreCase("MTA")){
        testMTA(args);
      }
    }
    catch (Exception ex) {
      dbg.Caught(ex);
    }
  }
}
//$Id: tester.java,v 1.1 2001/08/14 23:27:50 andyh Exp $