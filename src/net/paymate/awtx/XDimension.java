package net.paymate.awtx;

/**
 * Title:        $Source: /cvs/src/net/paymate/awtx/XDimension.java,v $
 * Description:  adds obvious function left out by java's Dimension
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Revision: 1.4 $
 */

/**
 * The <code>XDimension</code> class encapsulates the width and
 * height of a component (in integer precision) in a single object.
 * The class is
 * associated with certain properties of components. Several methods
 * defined by the <code>Component</code> class and the
 * <code>LayoutManager</code> interface return a
 * <code>XDimension</code> object.
 * <p>
 * Normally the values of <code>width</code>
 * and <code>height</code> are non-negative integers.
 * The constructors that allow you to create a dimension do
 * not prevent you from setting a negative value for these properties.
 * If the value of <code>width</code> or <code>height</code> is
 * negative, the behavior of some methods defined by other objects is
 * undefined.
 */

public class XDimension
    extends XDimension2D
    implements java.io.Serializable {
//  public XDimension() {
//    super();
//  }
//
//  public XDimension(XDimension d) {
//    super(d);
//  }
//
//  public XDimension(int width, int height) {
//    super(width,height);
//  }

  public XDimension setSize(XPoint one, XPoint two) {
    setSize(Math.abs(one.x - two.x), Math.abs(one.y - two.y));
    return this;
  }

  public XDimension(XPoint one, XPoint two) { //unordered
    super();
    setSize(one, two);
  }

  // new stuff from here down ...
  /**
   * The width dimension; negative values can be used.
   *
   * @serial
   * @see #getSize
   * @see #setSize
   */
  public int width;

  /**
   * The height dimension; negative values can be used.
   *
   * @serial
   * @see #getSize
   * @see #setSize
   */
  public int height;

  /**
   * Creates an instance of <code>XDimension</code> with a width
   * of zero and a height of zero.
   */
  public XDimension() {
    this(0, 0);
  }

  /**
   * Creates an instance of <code>XDimension</code> whose width
   * and height are the same as for the specified dimension.
   *
   * @param    d   the specified dimension for the
   *               <code>width</code> and
   *               <code>height</code> values
   */
  public XDimension(XDimension d) {
    this(d.width, d.height);
  }

  /**
   * Constructs a <code>XDimension</code> and initializes
   * it to the specified width and specified height.
   *
   * @param width the specified width
   * @param height the specified height
   */
  public XDimension(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Returns the width of this dimension in double precision.
   * @return the width of this dimension in double precision
   */
  public double getWidth() {
    return width;
  }

  /**
   * Returns the height of this dimension in double precision.
   * @return the height of this dimension in double precision
   */
  public double getHeight() {
    return height;
  }

  /**
   * Sets the size of this <code>XDimension</code> object to
   * the specified width and height in double precision.
   * Note that if <code>width</code> or <code>height</code>
   * are larger than <code>Integer.MAX_VALUE</code>, they will
   * be reset to <code>Integer.MAX_VALUE</code>.
   *
   * @param width  the new width for the <code>XDimension</code> object
   * @param height the new height for the <code>XDimension</code> object
   */
  public void setSize(double width, double height) {
    this.width = (int) Math.ceil(width);
    this.height = (int) Math.ceil(height);
  }

  /**
   * Gets the size of this <code>XDimension</code> object.
   * This method is included for completeness, to parallel the
   * <code>getSize</code> method defined by <code>Component</code>.
   *
   * @return   the size of this dimension, a new instance of
   *           <code>XDimension</code> with the same width and height
   * @see      XDimension#setSize
   */
  public XDimension getSize() {
    return new XDimension(width, height);
  }

  /**
       * Sets the size of this <code>XDimension</code> object to the specified size.
   * This method is included for completeness, to parallel the
   * <code>setSize</code> method defined by <code>Component</code>.
   * @param    d  the new size for this <code>XDimension</code> object
   * @see      XDimension#getSize
   * @since    JDK1.1
   */
  public void setSize(XDimension d) {
    setSize(d.width, d.height);
  }

  /**
   * Sets the size of this <code>XDimension</code> object
   * to the specified width and height.
   * This method is included for completeness, to parallel the
   * <code>setSize</code> method defined by <code>Component</code>.
   *
   * @param    width   the new width for this <code>XDimension</code> object
   * @param    height  the new height for this <code>XDimension</code> object
   * @see      XDimension#getSize
   * @since    JDK1.1
   */
  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Checks whether two dimension objects have equal values.
   */
  public boolean equals(Object obj) {
    if (obj instanceof XDimension) {
      XDimension d = (XDimension) obj;
      return (width == d.width) && (height == d.height);
    }
    return false;
  }

  /**
   * Returns the hash code for this <code>XDimension</code>.
   *
   * @return    a hash code for this <code>XDimension</code>
   */
  public int hashCode() {
    int sum = width + height;
    return sum * (sum + 1) / 2 + width;
  }

  /**
   * Returns a string representation of the values of this
   * <code>XDimension</code> object's <code>height</code> and
   * <code>width</code> fields. This method is intended to be used only
   * for debugging purposes, and the content and format of the returned
   * string may vary between implementations. The returned string may be
   * empty but may not be <code>null</code>.
   *
   * @return  a string representation of this <code>XDimension</code>
   *          object
   */
  public String toString() {
    return getClass().getName() + "[width=" + width + ",height=" + height + "]";
  }
}
