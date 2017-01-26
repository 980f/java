package net.paymate.awtx.print;
/**
 * <p>Title: $Source: /cvs/src/net/paymate/awtx/print/DoubleWider.java,v $</p>
 * <p>Description: control characters needed for double wide printer text</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class DoubleWider {
  public int wider;
  public int normal;

  public DoubleWider(int wider,int normal) {
    this.wider=wider;
    this.normal=normal;
  }

}

/**
 * hypercom ice5000  14,20   (SO,DC4)
 * verifon 250       30,31 (RS/US really weird)
 */

//$Id: DoubleWider.java,v 1.1 2004/01/09 23:45:08 andyh Exp $