package net.paymate.awtx;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/awtx/XRectangle2D.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

/**
 * The <code>Rectangle2D</code> class describes a rectangle
 * defined by a location (x,&nbsp;y) and dimension
 * (w&nbsp;x&nbsp;h).
 * <p>
 * This class is only the abstract superclass for all objects that
 * store a 2D rectangle.
 * The actual storage representation of the coordinates is left to
 * the subclass.
 *
 * @version 	1.27, 12/03/01
 * @author	Jim Graham
 */
public abstract class XRectangle2D extends XRectangularShape {
    /**
     * The bitmask that indicates that a point lies to the left of
     * this <code>Rectangle2D</code>.
     * @since 1.2
     */
    public static final int OUT_LEFT = 1;

    /**
     * The bitmask that indicates that a point lies above
     * this <code>Rectangle2D</code>.
     * @since 1.2
     */
    public static final int OUT_TOP = 2;

    /**
     * The bitmask that indicates that a point lies to the right of
     * this <code>Rectangle2D</code>.
     * @since 1.2
     */
    public static final int OUT_RIGHT = 4;

    /**
     * The bitmask that indicates that a point lies below
     * this <code>Rectangle2D</code>.
     * @since 1.2
     */
    public static final int OUT_BOTTOM = 8;

    /**
     * The <code>Float</code> class defines a rectangle specified in float
     * coordinates.
     * @since 1.2
     */
    public static class Float extends XRectangle2D {
  /**
   * The x coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public float x;

  /**
   * The y coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public float y;

  /**
   * The width of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public float width;

  /**
   * The height of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public float height;

  /**
   * Constructs a new <code>Rectangle2D</code>, initialized to
         * location (0.0,&nbsp;0.0) and size (0.0,&nbsp;0.0).
   * @since 1.2
   */
  public Float() {
  }

  /**
   * Constructs and initializes a <code>Rectangle2D</code>
         * from the specified float coordinates.
   * @param x,&nbsp;y the coordinates of the
         * upper left corner of the newly constructed
         * <code>Rectangle2D</code>
   * @param w the width of the newly constructed
         * <code>Rectangle2D</code>
   * @param h the height of the newly constructed
         * <code>Rectangle2D</code>
   * @since 1.2
  */
  public Float(float x, float y, float w, float h) {
      setRect(x, y, w, h);
  }

  /**
   * Returns the X coordinate of this <code>Rectangle2D</code>
         * in double precision.
         * @return the X coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double getX() {
      return (double) x;
  }

  /**
   * Returns the Y coordinate of this <code>Rectangle2D</code>
         * in double precision.
         * @return the Y coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double getY() {
      return (double) y;
  }

  /**
   * Returns the width of this <code>Rectangle2D</code>
         * in double precision.
         * @return the width of this <code>Rectangle2D</code>.
         * @since 1.2
   */
  public double getWidth() {
      return (double) width;
  }

  /**
         * Returns the height of this <code>Rectangle2D</code>
         * in double precision.
         * @return the height of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double getHeight() {
      return (double) height;
  }

  /**
   * Determines whether or not this <code>Rectangle2D</code>
         * is empty.
         * @return <code>true</code> if this <code>Rectangle2D</code>
         * is empty; <code>false</code> otherwise.
   * @since 1.2
   */
  public boolean isEmpty() {
      return (width <= 0.0f) || (height <= 0.0f);
  }

  /**
   * Sets the location and size of this <code>Rectangle2D</code>
         * to the specified float values.
         * @param x,&nbsp;y the coordinates to which to set the
         * location of the upper left corner of this
         * <code>Rectangle2D</code>
         * @param w the value to use to set the width of this
         * <code>Rectangle2D</code>
         * @param h the value to use to set the height of this
         * <code>Rectangle2D</code>
   * @since 1.2
   */
  public void setRect(float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
  }

  /**
   * Sets the location and size of this <code>Rectangle2D</code>
         * to the specified double values.
         * @param x,&nbsp;y the coordinates to which to set the
         * location of the upper left corner of this
         * <code>Rectangle2D</code>
         * @param w the value to use to set the width of this
         * <code>Rectangle2D</code>
         * @param h the value to use to set the height of this
         * <code>Rectangle2D</code>
   * @since 1.2
   */
  public void setRect(double x, double y, double w, double h) {
      this.x = (float) x;
      this.y = (float) y;
      this.width = (float) w;
      this.height = (float) h;
  }

  /**
   * Sets this <code>Rectangle2D</code> to be the same as the
         * specified <code>Rectangle2D</code>.
         * @param r the specified <code>Rectangle2D</code>
   * @since 1.2
   */
  public void setRect(XRectangle2D r) {
      this.x = (float) r.getX();
      this.y = (float) r.getY();
      this.width = (float) r.getWidth();
      this.height = (float) r.getHeight();
  }

  /**
   * Determines where the specified float coordinates lie with respect
   * to this <code>Rectangle2D</code>.
   * This method computes a binary OR of the appropriate mask values
   * indicating, for each side of this <code>Rectangle2D</code>,
         * whether or not the specified coordinates are on the same side
         * of the edge as the rest of this <code>Rectangle2D</code>.
         * @param x,&nbsp;y the specified coordinates
         * @return the logical OR of all appropriate out codes.
   * @see Rectangle2D#OUT_LEFT
   * @see Rectangle2D#OUT_TOP
   * @see Rectangle2D#OUT_RIGHT
   * @see Rectangle2D#OUT_BOTTOM
   * @since 1.2
   */
  public int outcode(double x, double y) {
      /*
       * Note on casts to double below.  If the arithmetic of
       * x+w or y+h is done in float, then some bits may be
       * lost if the binary exponents of x/y and w/h are not
       * similar.  By converting to double before the addition
       * we force the addition to be carried out in double to
       * avoid rounding error in the comparison.
       *
       * See bug 4320890 for problems that this inaccuracy causes.
       */
      int out = 0;
      if (this.width <= 0) {
    out |= OUT_LEFT | OUT_RIGHT;
      } else if (x < this.x) {
    out |= OUT_LEFT;
      } else if (x > this.x + (double) this.width) {
    out |= OUT_RIGHT;
      }
      if (this.height <= 0) {
    out |= OUT_TOP | OUT_BOTTOM;
      } else if (y < this.y) {
    out |= OUT_TOP;
      } else if (y > this.y + (double) this.height) {
    out |= OUT_BOTTOM;
      }
      return out;
  }

  /**
   * Returns the high precision bounding box of this
         * <code>Rectangle2D</code>.
         * @return the bounding box of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public XRectangle2D getBounds2D() {
      return new Float(x, y, width, height);
  }

  /**
   * Returns a new <code>Rectangle2D</code> object
         * representing the intersection of
   * this <code>Rectangle2D</code> with the specified
         * <code>Rectangle2D</code>.
   * @param r the <code>Rectangle2D</code> that is
         * intersected with this <code>Rectangle2D</code>
   * @return the largest <code>Rectangle2D</code>
         * contained in both the specified
         * <code>Rectangle2D</code> and in this
         * <code>Rectangle2D</code>.
   * @since 1.2
   */
  public XRectangle2D createIntersection(XRectangle2D r) {
      XRectangle2D dest;
      if (r instanceof Float) {
    dest = new XRectangle2D.Float();
      } else {
    dest = new XRectangle2D.Double();
      }
      XRectangle2D.intersect(this, r, dest);
      return dest;
  }

  /**
   * Returns a new <code>Rectangle2D</code> object
         * representing the union of this <code>Rectangle2D</code>
         * with the specified <code>Rectangle2D</code>.
   * @param r the <code>Rectangle2D</code> to be combined with
         * this <code>Rectangle2D</code>
   * @return the smallest <code>Rectangle2D</code> containing
         * both the specified <code>Rectangle2D</code> and this
         * <code>Rectangle2D</code>.
   * @since 1.2
   */
  public XRectangle2D createUnion(XRectangle2D r) {
      XRectangle2D dest;
      if (r instanceof Float) {
    dest = new XRectangle2D.Float();
      } else {
    dest = new XRectangle2D.Double();
      }
      XRectangle2D.union(this, r, dest);
      return dest;
  }

  /**
   * Returns the <code>String</code> representation of this
         * <code>Rectangle2D</code>.
         * @return a <code>String</code> representing this
         * <code>Rectangle2D</code>.
   * @since 1.2
   */
  public String toString() {
      return getClass().getName()
    + "[x=" + x +
    ",y=" + y +
    ",w=" + width +
    ",h=" + height + "]";
  }
    }

    /**
     * The <code>Double</code> class defines a rectangle specified in
     * double coordinates.
     * @since 1.2
     */
    public static class Double extends XRectangle2D {
  /**
   * The x coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double x;

  /**
   * The y coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double y;

  /**
   * The width of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double width;

  /**
   * The height of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double height;

  /**
   * Constructs a new <code>Rectangle2D</code>, initialized to
         * location (0,&nbsp;0) and size (0,&nbsp;0).
   * @since 1.2
   */
  public Double() {
  }

  /**
   * Constructs and initializes a <code>Rectangle2D</code>
         * from the specified double coordinates.
   * @param x,&nbsp;y the coordinates of the upper left corner
         * of the newly constructed <code>Rectangle2D</code>
   * @param w the width of the
         * newly constructed <code>Rectangle2D</code>
   * @param h the height of the
         * newly constructed <code>Rectangle2D</code>
   * @since 1.2
   */
  public Double(double x, double y, double w, double h) {
      setRect(x, y, w, h);
  }

  /**
   * Returns the X coordinate of this <code>Rectangle2D</code> in
         * double precision.
         * @return the X coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double getX() {
      return x;
  }

  /**
   * Returns the Y coordinate of this <code>Rectangle2D</code> in
         * double precision.
         * @return the Y coordinate of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double getY() {
      return y;
  }

  /**
   * Returns the width of this <code>Rectangle2D</code> in
         * double precision.
         * @return the width of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double getWidth() {
      return width;
  }

  /**
   * Returns the height of this <code>Rectangle2D</code> in
         * double precision.
         * @return the height of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public double getHeight() {
      return height;
  }

  /**
   * Determines whether or not this <code>Rectangle2D</code>
         * is empty.
         * @return <code>true</code> if this <code>Rectangle2D</code>
         * is empty; <code>false</code> otherwise.
   * @since 1.2
   */
  public boolean isEmpty() {
      return (width <= 0.0) || (height <= 0.0);
  }

  /**
   * Sets the location and size of this <code>Rectangle2D</code>
         * to the specified double values.
         * @param x,&nbsp;y the coordinates to which to set the
         * upper left corner of this <code>Rectangle2D</code>
         * @param w the value to use to set the width of this
         * <code>Rectangle2D</code>
         * @param h the value to use to set the height of this
         * <code>Rectangle2D</code>
   * @since 1.2
   */
  public void setRect(double x, double y, double w, double h) {
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
  }

  /**
   * Sets this <code>Rectangle2D</code> to be the same as the
         * specified <code>Rectangle2D</code>.
         * @param r the specified <code>Rectangle2D</code>
   * @since 1.2
   */
  public void setRect(XRectangle2D r) {
      this.x = r.getX();
      this.y = r.getY();
      this.width = r.getWidth();
      this.height = r.getHeight();
  }

  /**
   * Determines where the specified double coordinates lie with respect
   * to this <code>Rectangle2D</code>.
         * This method computes a binary OR of the appropriate mask values
         * indicating, for each side of this <code>Rectangle2D</code>,
         * whether or not the specified coordinates are on the same side
         * of the edge as the rest of this <code>Rectangle2D</code>.
         * @param x,&nbsp;y the specified coordinates
         * @return the logical OR of all appropriate out codes.
         * @see Rectangle2D#OUT_LEFT
   * @see Rectangle2D#OUT_TOP
   * @see Rectangle2D#OUT_RIGHT
   * @see Rectangle2D#OUT_BOTTOM
   * @since 1.2
   */
  public int outcode(double x, double y) {
      int out = 0;
      if (this.width <= 0) {
    out |= OUT_LEFT | OUT_RIGHT;
      } else if (x < this.x) {
    out |= OUT_LEFT;
      } else if (x > this.x + this.width) {
    out |= OUT_RIGHT;
      }
      if (this.height <= 0) {
    out |= OUT_TOP | OUT_BOTTOM;
      } else if (y < this.y) {
    out |= OUT_TOP;
      } else if (y > this.y + this.height) {
    out |= OUT_BOTTOM;
      }
      return out;
  }

  /**
   * Returns the high precision bounding box of this
         * <code>Rectangle2D</code>.
         * @return the bounding box of this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public XRectangle2D getBounds2D() {
      return new Double(x, y, width, height);
  }

  /**
   * Returns a new <code>Rectangle2D</code> object representing
         * the intersection of this <code>Rectangle2D</code> with the
         * specified <code>Rectangle2D</code>.
   * @param r the <code>Rectangle2D</code> to be intersected
         * with this <code>Rectangle2D</code>
   * @return the largest <code>Rectangle2D</code> contained in
         * both the specified <code>Rectangle2D</code> and in this
         * <code>Rectangle2D</code>.
   * @since 1.2
   */
  public XRectangle2D createIntersection(XRectangle2D r) {
      XRectangle2D dest = new XRectangle2D.Double();
      XRectangle2D.intersect(this, r, dest);
      return dest;
  }

  /**
   * Returns a new <code>Rectangle2D</code> object representing
         * the union of this <code>Rectangle2D</code> with the
         * specified <code>Rectangle2D</code>.
   * @param r the <code>Rectangle2D</code> to be combined with
         * this <code>Rectangle2D</code>
   * @return  the smallest <code>Rectangle2D</code> containing
         * both the specified <code>Rectangle2D</code> and this
         * <code>Rectangle2D</code>.
   * @since 1.2
   */
  public XRectangle2D createUnion(XRectangle2D r) {
      XRectangle2D dest = new XRectangle2D.Double();
      XRectangle2D.union(this, r, dest);
      return dest;
  }

  /**
   * Returns the <code>String</code> representation of this
         * <code>Rectangle2D</code>.
         * @return a <code>String</code> representing this
         * <code>Rectangle2D</code>.
   * @since 1.2
   */
  public String toString() {
      return getClass().getName()
    + "[x=" + x +
    ",y=" + y +
    ",w=" + width +
    ",h=" + height + "]";
  }
    }

    /**
     * This is an abstract class that cannot be instantiated directly.
     * Type-specific implementation subclasses are available for
     * instantiation and provide a number of formats for storing
     * the information necessary to satisfy the various accessor
     * methods below.
     *
     * @see XRectangle2D.Float
     * @see XRectangle2D.Double
     * @see XRectangle
     */
    protected XRectangle2D() {
    }

    /**
     * Sets the location and size of this <code>Rectangle2D</code>
     * to the specified double values.
     * @param x,&nbsp;y the coordinates to which to set the
     * location of the upper left corner of this
     * <code>Rectangle2D</code>
     * @param w the value to use to set the width of this
     * <code>Rectangle2D</code>
     * @param h the value to use to set the height of this
     * <code>Rectangle2D</code>
     * @since 1.2
     */
    public abstract void setRect(double x, double y, double w, double h);

    /**
     * Sets this <code>Rectangle2D</code> to be the same as the specified
     * <code>Rectangle2D</code>.
     * @param r the specified <code>Rectangle2D</code>
     * @since 1.2
     */
    public void setRect(XRectangle2D r) {
  setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Tests if the specified line segment intersects the interior of this
     * <code>Rectangle2D</code>.
     * @param x1,&nbsp;y1 the first endpoint of the specified
     * line segment
     * @param x2,&nbsp;y2 the second endpoint of the specified
     * line segment
     * @return <code>true</code> if the specified line segment intersects
     * the interior of this <code>Rectangle2D</code>; <code>false</code>
     * otherwise.
     * @since 1.2
     */
    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
  int out1, out2;
  if ((out2 = outcode(x2, y2)) == 0) {
      return true;
  }
  while ((out1 = outcode(x1, y1)) != 0) {
      if ((out1 & out2) != 0) {
    return false;
      }
      if ((out1 & (OUT_LEFT | OUT_RIGHT)) != 0) {
    double x = getX();
    if ((out1 & OUT_RIGHT) != 0) {
        x += getWidth();
    }
    y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
    x1 = x;
      } else {
    double y = getY();
    if ((out1 & OUT_BOTTOM) != 0) {
        y += getHeight();
    }
    x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
    y1 = y;
      }
  }
  return true;
    }

    /**
     * Tests if the specified line segment intersects the interior of this
     * <code>Rectangle2D</code>.
     * @param l the specified {@link Line2D} to test for intersection
     * with the interior of this <code>Rectangle2D</code>
     * @return <code>true</code> if the specified <code>Line2D</code>
     * intersects the interior of this <code>Rectangle2D</code>;
     * <code>false</code> otherwise.
     * @since 1.2
     */
//    public boolean intersectsLine(XLine2D l) {
//  return intersectsLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
//    }

    /**
     * Determines where the specified coordinates lie with respect
     * to this <code>Rectangle2D</code>.
     * This method computes a binary OR of the appropriate mask values
     * indicating, for each side of this <code>Rectangle2D</code>,
     * whether or not the specified coordinates are on the same side
     * of the edge as the rest of this <code>Rectangle2D</code>.
     * @param x,&nbsp;y the specified coordinates
     * @return the logical OR of all appropriate out codes.
     * @see #OUT_LEFT
     * @see #OUT_TOP
     * @see #OUT_RIGHT
     * @see #OUT_BOTTOM
     * @since 1.2
     */
    public abstract int outcode(double x, double y);

    /**
     * Determines where the specified {@link Point2D} lies with
     * respect to this <code>Rectangle2D</code>.
     * This method computes a binary OR of the appropriate mask values
     * indicating, for each side of this <code>Rectangle2D</code>,
     * whether or not the specified <code>Point2D</code> is on the same
     * side of the edge as the rest of this <code>Rectangle2D</code>.
     * @param p the specified <code>Point2D</code>
     * @return the logical OR of all appropriate out codes.
     * @see #OUT_LEFT
     * @see #OUT_TOP
     * @see #OUT_RIGHT
     * @see #OUT_BOTTOM
     * @since 1.2
     */
    public int outcode(XPoint2D p) {
  return outcode(p.getX(), p.getY());
    }

    /**
     * Sets the location and size of the outer bounds of this
     * <code>Rectangle2D</code> to the specified rectangular values.
     * @param x,&nbsp;y the coordinates to which to set the
     * location of the upper left corner of the outer bounds of
     * this <code>Rectangle2D</code>
     * @param w the value to use to set the width of the outer
     * bounds of this <code>Rectangle2D</code>
     * @param h the value to use to set the height of the outer
     * bounds of this <code>Rectangle2D</code>
     * @since 1.2
     */
    public void setFrame(double x, double y, double w, double h) {
  setRect(x, y, w, h);
    }

    /**
     * Returns the high precision bounding box of this
     * <code>Rectangle2D</code>.
     * @return the bounding box of this <code>Rectangle2D</code>.
     * @since 1.2
     */
    public XRectangle2D getBounds2D() {
  return (XRectangle2D) clone();
    }

    /**
     * Tests if a specified coordinate is inside the boundary of this
     * <code>Rectangle2D</code>.
     * @param x,&nbsp;y the coordinates to test
     * @return <code>true</code> if the specified coordinates are
     * inside the boundary of this <code>Rectangle2D</code>;
     * <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean contains(double x, double y) {
  double x0 = getX();
  double y0 = getY();
  return (x >= x0 &&
    y >= y0 &&
    x < x0 + getWidth() &&
    y < y0 + getHeight());
    }

    /**
     * Tests if the interior of this <code>Rectangle2D</code>
     * intersects the interior of a specified set of rectangular
     * coordinates.
     * @param x,&nbsp;y the coordinates of the upper left corner
     * of the specified set of rectangular coordinates
     * @param w the width of the specified set of rectangular
     * coordinates
     * @param h the height of the specified set of rectangular
     * coordinates
     * @return <code>true</code> if this <code>Rectangle2D</code>
     * intersects the interior of a specified set of rectangular
     * coordinates; <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {
  if (isEmpty() || w <= 0 || h <= 0) {
      return false;
  }
  double x0 = getX();
  double y0 = getY();
  return (x + w > x0 &&
    y + h > y0 &&
    x < x0 + getWidth() &&
    y < y0 + getHeight());
    }

    /**
     * Tests if the interior of this <code>Rectangle2D</code> entirely
     * contains the specified set of rectangular coordinates.
     * @param x,&nbsp;y the coordinates of the upper left corner
     * of the specified set of rectangular coordinates
     * @param w the width of the specified set of rectangular
     * coordinates
     * @param h the height of the specified set of rectangular
     * coordinates
     * @return <code>true</code> if this <code>Rectangle2D</code>
     * entirely contains specified set of rectangular
     * coordinates; <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
  if (isEmpty() || w <= 0 || h <= 0) {
      return false;
  }
  double x0 = getX();
  double y0 = getY();
  return (x >= x0 &&
    y >= y0 &&
    (x + w) <= x0 + getWidth() &&
    (y + h) <= y0 + getHeight());
    }

    /**
     * Returns a new <code>Rectangle2D</code> object representing the
     * intersection of this <code>Rectangle2D</code> with the specified
     * <code>Rectangle2D</code>.
     * @param r the <code>Rectangle2D</code> to be intersected with
     * this <code>Rectangle2D</code>
     * @return the largest <code>Rectangle2D</code> contained in both
     * 		the specified <code>Rectangle2D</code> and in this
     *		<code>Rectangle2D</code>.
     * @since 1.2
     */
    public abstract XRectangle2D createIntersection(XRectangle2D r);

    /**
     * Intersects the pair of specified source <code>Rectangle2D</code>
     * objects and puts the result into the specified destination
     * <code>Rectangle2D</code> object.  One of the source rectangles
     * can also be the destination to avoid creating a third Rectangle2D
     * object, but in this case the original points of this source
     * rectangle will be overwritten by this method.
     * @param src1 the first of a pair of <code>Rectangle2D</code>
     * objects to be intersected with each other
     * @param src2 the second of a pair of <code>Rectangle2D</code>
     * objects to be intersected with each other
     * @param dest the <code>Rectangle2D</code> that holds the
     * results of the intersection of <code>src1</code> and
     * <code>src2</code>
     * @since 1.2
     */
    public static void intersect(XRectangle2D src1,
         XRectangle2D src2,
         XRectangle2D dest) {
  double x1 = Math.max(src1.getMinX(), src2.getMinX());
  double y1 = Math.max(src1.getMinY(), src2.getMinY());
  double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
  double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
  dest.setFrame(x1, y1, x2-x1, y2-y1);
    }

    /**
     * Returns a new <code>Rectangle2D</code> object representing the
     * union of this <code>Rectangle2D</code> with the specified
     * <code>Rectangle2D</code>.
     * @param r the <code>Rectangle2D</code> to be combined with
     * this <code>Rectangle2D</code>
     * @return the smallest <code>Rectangle2D</code> containing both
     * the specified <code>Rectangle2D</code> and this
     * <code>Rectangle2D</code>.
     * @since 1.2
     */
    public abstract XRectangle2D createUnion(XRectangle2D r);

    /**
     * Unions the pair of source <code>Rectangle2D</code> objects
     * and puts the result into the specified destination
     * <code>Rectangle2D</code> object.  One of the source rectangles
     * can also be the destination to avoid creating a third Rectangle2D
     * object, but in this case the original points of this source
     * rectangle will be overwritten by this method.
     * @param src1 the first of a pair of <code>Rectangle2D</code>
     * objects to be combined with each other
     * @param src2 the second of a pair of <code>Rectangle2D</code>
     * objects to be combined with each other
     * @param dest the <code>Rectangle2D</code> that holds the
     * results of the union of <code>src1</code> and
     * <code>src2</code>
     * @since 1.2
     */
    public static void union(XRectangle2D src1,
           XRectangle2D src2,
           XRectangle2D dest) {
  double x1 = Math.min(src1.getMinX(), src2.getMinX());
  double y1 = Math.min(src1.getMinY(), src2.getMinY());
  double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
  double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
  dest.setFrameFromDiagonal(x1, y1, x2, y2);
    }

    /**
     * Adds a point, specified by the double precision arguments
     * <code>newx</code> and <code>newy</code>, to this
     * <code>Rectangle2D</code>.  The resulting <code>Rectangle2D</code>
     * is the smallest <code>Rectangle2D</code> that
     * contains both the original <code>Rectangle2D</code> and the
     * specified point.
     * <p>
     * After adding a point, a call to <code>contains</code> with the
     * added point as an argument does not necessarily return
     * <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the right or bottom
     * edges of a rectangle. Therefore, if the added point falls on
     * the left or bottom edge of the enlarged rectangle,
     * <code>contains</code> returns <code>false</code> for that point.
     * @param newx,&nbsp;newy the coordinates of the new point
     * @since     JDK1.0
     */
    public void add(double newx, double newy) {
  double x1 = Math.min(getMinX(), newx);
  double x2 = Math.max(getMaxX(), newx);
  double y1 = Math.min(getMinY(), newy);
  double y2 = Math.max(getMaxY(), newy);
  setRect(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Adds the <code>Point2D</code> object <code>pt</code> to this
     * <code>Rectangle2D</code>.
     * The resulting <code>Rectangle2D</code> is the smallest
     * <code>Rectangle2D</code> that contains both the original
     * <code>Rectangle2D</code> and the specified <code>Point2D</code>.
     * <p>
     * After adding a point, a call to <code>contains</code> with the
     * added point as an argument does not necessarily return
     * <code>true</code>. The <code>contains</code>
     * method does not return <code>true</code> for points on the right
     * or bottom edges of a rectangle. Therefore, if the added point falls
     * on the left or bottom edge of the enlarged rectangle,
     * <code>contains</code> returns <code>false</code> for that point.
     * @param     pt the new <code>Point2D</code> to add to this
     * <code>Rectangle2D</code>.
     * @since     JDK1.0
     */
    public void add(XPoint2D pt) {
  add(pt.getX(), pt.getY());
    }

    /**
     * Adds a <code>Rectangle2D</code> object to this
     * <code>Rectangle2D</code>.  The resulting <code>Rectangle2D</code>
     * is the union of the two <code>Rectangle2D</code> objects.
     * @param r the <code>Rectangle2D</code> to add to this
     * <code>Rectangle2D</code>.
     * @since     JDK1.0
     */
    public void add(XRectangle2D r) {
  double x1 = Math.min(getMinX(), r.getMinX());
  double x2 = Math.max(getMaxX(), r.getMaxX());
  double y1 = Math.min(getMinY(), r.getMinY());
  double y2 = Math.max(getMaxY(), r.getMaxY());
  setRect(x1, y1, x2 - x1, y2 - y1);
    }

//    /**
//     * Returns an iteration object that defines the boundary of this
//     * <code>Rectangle2D</code>.
//     * The iterator for this class is multi-threaded safe, which means
//     * that this <code>Rectangle2D</code> class guarantees that
//     * modifications to the geometry of this <code>Rectangle2D</code>
//     * object do not affect any iterations of that geometry that
//     * are already in process.
//     * @param at an optional <code>AffineTransform</code> to be applied to
//     * the coordinates as they are returned in the iteration, or
//     * <code>null</code> if untransformed coordinates are desired
//     * @return    the <code>PathIterator</code> object that returns the
//     *          geometry of the outline of this
//     *          <code>Rectangle2D</code>, one segment at a time.
//     * @since 1.2
//     */
//    public PathIterator getPathIterator(AffineTransform at) {
//  return new RectIterator(this, at);
//    }

    /**
     * Returns an iteration object that defines the boundary of the
     * flattened <code>Rectangle2D</code>.  Since rectangles are already
     * flat, the <code>flatness</code> parameter is ignored.
     * The iterator for this class is multi-threaded safe, which means
     * that this <code>Rectangle2D</code> class guarantees that
     * modifications to the geometry of this <code>Rectangle2D</code>
     * object do not affect any iterations of that geometry that
     * are already in process.
     * @param at an optional <code>AffineTransform</code> to be applied to
     * the coordinates as they are returned in the iteration, or
     * <code>null</code> if untransformed coordinates are desired
     * @param flatness the maximum distance that the line segments used to
     * approximate the curved segments are allowed to deviate from any
     * point on the original curve.  Since rectangles are already flat,
     * the <code>flatness</code> parameter is ignored.
     * @return    the <code>PathIterator</code> object that returns the
     *          geometry of the outline of this
     *          <code>Rectangle2D</code>, one segment at a time.
     * @since 1.2
     */
//    public PathIterator getPathIterator(AffineTransform at, double flatness) {
//  return new RectIterator(this, at);
//    }

    /**
     * Returns the hashcode for this <code>Rectangle2D</code>.
     * @return the hashcode for this <code>Rectangle2D</code>.
     */
    public int hashCode() {
  long bits = java.lang.Double.doubleToLongBits(getX());
  bits += java.lang.Double.doubleToLongBits(getY()) * 37;
  bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
  bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
  return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Determines whether or not the specified <code>Object</code> is
     * equal to this <code>Rectangle2D</code>.  The specified
     * <code>Object</code> is equal to this <code>Rectangle2D</code>
     * if it is an instance of <code>Rectangle2D</code> and if its
     * location and size are the same as this <code>Rectangle2D</code>.
     * @param obj an <code>Object</code> to be compared with this
     * <code>Rectangle2D</code>.
     * @return     <code>true</code> if <code>obj</code> is an instance
     *                     of <code>Rectangle2D</code> and has
     *                     the same values; <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean equals(Object obj) {
  if (obj == this) {
      return true;
  }
  if (obj instanceof XRectangle2D) {
      XRectangle2D r2d = (XRectangle2D) obj;
      return ((getX() == r2d.getX()) &&
        (getY() == r2d.getY()) &&
        (getWidth() == r2d.getWidth()) &&
        (getHeight() == r2d.getHeight()));
  }
  return false;
    }
}