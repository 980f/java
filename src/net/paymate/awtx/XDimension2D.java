package net.paymate.awtx;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/awtx/XDimension2D.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

/**
 * The <code>Dimension2D</code> class is to encapsulate a width
 * and a height dimension.
 * <p>
 * This class is only the abstract superclass for all objects that
 * store a 2D dimension.
 * The actual storage representation of the sizes is left to
 * the subclass.
 *
 * @version 	1.11, 12/03/01
 * @author	Jim Graham
 */
public abstract class XDimension2D implements Cloneable {
    /**
     * This is an abstract class that cannot be instantiated directly.
     * Type-specific implementation subclasses are available for
     * instantiation and provide a number of formats for storing
     * the information necessary to satisfy the various accessor
     * methods below.
     *
     * @see XDimension
     */
    protected XDimension2D() {
    }

    /**
     * Returns the width of this <code>Dimension</code> in double
     * precision.
     * @return the width of this <code>Dimension</code>.
     */
    public abstract double getWidth();

    /**
     * Returns the height of this <code>Dimension</code> in double
     * precision.
     * @return the height of this <code>Dimension</code>.
     */
    public abstract double getHeight();

    /**
     * Sets the size of this <code>Dimension</code> object to the
     * specified width and height.
     * @param width  the new width for the <code>Dimension</code>
     * object
     * @param height  the new height for the <code>Dimension</code>
     * object
     */
    public abstract void setSize(double width, double height);

    /**
     * Sets the size of this <code>Dimension2D</code> object to
     * match the specified size.
     * This method is included for completeness, to parallel the
     * <code>getSize</code> method of <code>Component</code>.
     * @param d  the new size for the <code>Dimension2D</code>
     * object
     */
    public void setSize(XDimension2D d) {
  setSize(d.getWidth(), d.getHeight());
    }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public Object clone() {
  try {
      return super.clone();
  } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
  }
    }
}
