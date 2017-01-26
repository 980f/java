package net.paymate.awtx;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/awtx/XPoint.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

/**
 * A point representing a location in (x, y) coordinate space, specified
 * in integer precision.
 *
 * @version 	1.35, 03/20/02
 * @author 	Sami Shaio
 * @since       JDK1.0
 */
public class XPoint extends XPoint2D implements java.io.Serializable {
    /**
     * The <i>x</i> coordinate.
     * If no <i>x</i> coordinate is set it will default to 0.
     *
     * @serial
     * @see #getLocation()
     * @see #move(int, int)
     */
    public int x;

    /**
     * The <i>y</i> coordinate.
     * If no <i>y</i> coordinate is set it will default to 0.
     *
     * @serial
     * @see #getLocation()
     * @see #move(int, int)
     */
    public int y;

    /**
     * Constructs and initializes a point at the origin
     * (0,&nbsp;0) of the coordinate space.
     * @since       JDK1.1
     */
    public XPoint() {
  this(0, 0);
    }

    /**
     * Constructs and initializes a point with the same location as
     * the specified <code>XPoint</code> object.
     * @param       p a point
     * @since       JDK1.1
     */
    public XPoint(XPoint p) {
  this(p.x, p.y);
    }

    /**
     * Constructs and initializes a point at the specified
     * (<i>x</i>,&nbsp;<i>y</i>) location in the coordinate space.
     * @param       x   the <i>x</i> coordinate
     * @param       y   the <i>y</i> coordinate
     */
    public XPoint(int x, int y) {
  this.x = x;
  this.y = y;
    }

    /**
     * Returns the X coordinate of the point in double precision.
     * @return the X coordinate of the point in double precision
     */
    public double getX() {
  return x;
    }

    /**
     * Returns the Y coordinate of the point in double precision.
     * @return the Y coordinate of the point in double precision
     */
    public double getY() {
  return y;
    }

    /**
     * Returns the location of this point.
     * This method is included for completeness, to parallel the
     * <code>getLocation</code> method of <code>Component</code>.
     * @return      a copy of this point, at the same location
     * @see         XPoint#setLocation(XPoint)
     * @see         XPoint#setLocation(int, int)
     * @since       JDK1.1
     */
    public XPoint getLocation() {
  return new XPoint(x, y);
    }

    /**
     * Sets the location of the point to the specified location.
     * This method is included for completeness, to parallel the
     * <code>setLocation</code> method of <code>Component</code>.
     * @param       p  a point, the new location for this point
     * @see         XPoint#getLocation
     * @since       JDK1.1
     */
    public void setLocation(XPoint p) {
  setLocation(p.x, p.y);
    }

    /**
     * Changes the point to have the specified location.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setLocation</code> method of <code>Component</code>.
     * Its behavior is identical with <code>move(int,&nbsp;int)</code>.
     * @param       x  the <i>x</i> coordinate of the new location
     * @param       y  the <i>y</i> coordinate of the new location
     * @see         XPoint#getLocation
     * @see         XPoint#move(int, int)
     * @since       JDK1.1
     */
    public void setLocation(int x, int y) {
  move(x, y);
    }

    /**
     * Sets the location of this point to the specified double coordinates.
     * The double values will be rounded to integer values.
     * Any number smaller than <code>Integer.MIN_VALUE</code>
     * will be reset to <code>MIN_VALUE</code>, and any number
     * larger than <code>Integer.MAX_VALUE</code> will be
     * reset to <code>MAX_VALUE</code>.
     *
     * @param x the <i>x</i> coordinate of the new location
     * @param y the <i>y</i> coordinate of the new location
     * @see #getLocation
     */
    public void setLocation(double x, double y) {
  this.x = (int) Math.floor(x+0.5);
  this.y = (int) Math.floor(y+0.5);
    }

    /**
     * Moves this point to the specified location in the
     * (<i>x</i>,&nbsp;<i>y</i>) coordinate plane. This method
     * is identical with <code>setLocation(int,&nbsp;int)</code>.
     * @param       x  the <i>x</i> coordinate of the new location
     * @param       y  the <i>y</i> coordinate of the new location
     */
    public void move(int x, int y) {
  this.x = x;
  this.y = y;
    }

    /**
     * Translates this point, at location (<i>x</i>,&nbsp;<i>y</i>),
     * by <code>dx</code> along the <i>x</i> axis and <code>dy</code>
     * along the <i>y</i> axis so that it now represents the point
     * (<code>x</code>&nbsp;<code>+</code>&nbsp;<code>dx</code>,
     * <code>y</code>&nbsp;<code>+</code>&nbsp;<code>dy</code>).
     * @param       dx   the distance to move this point
     *                            along the <i>x</i> axis
     * @param       dy    the distance to move this point
     *                            along the <i>y</i> axis
     */
    public void translate(int dx, int dy) {
  this.x += dx;
  this.y += dy;
    }

    /**
     * Determines whether an instance of <code>Point2D</code> is equal
     * to this point.  Two instances of <code>Point2D</code> are equal if
     * the values of their <code>x</code> and <code>y</code> member
     * fields, representing their position in the coordinate space, are
     * the same.
     * @param      obj   an object to be compared with this point
     * @return     <code>true</code> if the object to be compared is
     *                     an instance of <code>XPoint</code> and has
     *                     the same values; <code>false</code> otherwise
     */
    public boolean equals(Object obj) {
  if (obj instanceof XPoint) {
      XPoint pt = (XPoint)obj;
      return (x == pt.x) && (y == pt.y);
  }
  return super.equals(obj);
    }

    /**
     * Returns a string representation of this point and its location
     * in the (<i>x</i>,&nbsp;<i>y</i>) coordinate space. This method is
     * intended to be used only for debugging purposes, and the content
     * and format of the returned string may vary between implementations.
     * The returned string may be empty but may not be <code>null</code>.
     *
     * @return  a string representation of this point
     */
    public String toString() {
  return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }
}
