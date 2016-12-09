package net.paymate.awtx;

import java.awt.Dimension;
import java.awt.Point;

/**
 * Title:        X fo rextended Dimension
 * Description:  adds obvious function left out by java's Dimension
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: XDimension.java,v 1.1 2000/12/06 03:01:35 andyh Exp $
 */

public class XDimension extends Dimension {

  public XDimension() {
    super();
  }

  public XDimension(Dimension d) {
    super();
  }

  public XDimension(int width, int height) {
    super();
  }

  public XDimension setSize(Point one, Point two){
    setSize(Math.abs(one.x-two.x),Math.abs(one.y-two.y));
    return this;
  }

  public XDimension(Point one, Point two) {//unordered
    super();
    setSize(one,two);
  }

}
//$Id: XDimension.java,v 1.1 2000/12/06 03:01:35 andyh Exp $