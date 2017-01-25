/**
 * Title:        PackageArrayTableGen<p>
 * Description:  Generates the table content for a PackageArray<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: PackageArrayTableGen.java,v 1.9 2001/10/15 22:41:38 andyh Exp $
 */

package net.paymate.web.table;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.web.color.*;
import  org.apache.ecs.*;
import  net.paymate.util.Safe;

public class PackageArrayTableGen extends TableGen {

  // logging facilities
  private static final ErrorLogStream dbg=new ErrorLogStream(PackageArrayTableGen.class.getName());

  Package[] pkgs = null;

  public PackageArrayTableGen(String title, ColorScheme colors, Package[] pkgs, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    super(title, colors, headers, absoluteURL, howMany, sessionid);
    this.pkgs = pkgs;
  }

  public static final Element output(String title, ColorScheme colors, Package[] pkgs, String sessionid) {
    return new PackageArrayTableGen(title, colors, pkgs, null, null, -1, sessionid);
  }

  public RowEnumeration rows() {
    return new PackageArrayRowEnumeration(pkgs);
  }

  public static final int numCols     = 9;
  // @EN@ looks like an enumeration, huh? ...
  public static final int NAME        = 0;
  public static final int IMPTITLE    = 1;
  public static final int IMPVENDOR   = 2;
  public static final int IMPVER      = 3;
  public static final int SPECTITLE   = 4;
  public static final int SPECVENDOR  = 5;
  public static final int SPECVERSION = 6;
  public static final int SEALED      = 7;
  public static final int HASH        = 8;

  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = null;
    if(pkgs != null) {
      headers = new HeaderDef[numCols];
      headers[NAME]        = new HeaderDef(AlignType.LEFT, "Name");
      headers[IMPTITLE]    = new HeaderDef(AlignType.LEFT, "Implementation Title");
      headers[IMPVENDOR]   = new HeaderDef(AlignType.LEFT, "Implementation Vendor");
      headers[IMPVER]      = new HeaderDef(AlignType.LEFT, "Implementation Version");
      headers[SPECTITLE]   = new HeaderDef(AlignType.LEFT, "Specification Title");
      headers[SPECVENDOR]  = new HeaderDef(AlignType.LEFT, "Specification Vendor");
      headers[SPECVERSION] = new HeaderDef(AlignType.LEFT, "Specification Version");
      headers[SEALED]      = new HeaderDef(AlignType.LEFT, "Sealed");
      headers[HASH]        = new HeaderDef(AlignType.LEFT, "Hash");
    }
    return headers;
  }

  public void close() {
    super.close();
  }
}


class PackageArrayRowEnumeration implements RowEnumeration {

  Package [] pkgs = null;
  private int curRow = -1;

  public PackageArrayRowEnumeration(Package [] pkgs) {
    this.pkgs = pkgs;
    sort();
  }

  private void sort() {
    if(pkgs == null) {
      return;
    }
    // +_+ create a whole new array instead of modifying the passed one?
    //  might even be faster than this algorithm since we could binary search with insert and add(append)
    boolean swapped = true;
    while(swapped) {
      swapped = false;
      for(int i = pkgs.length; i-->1;) {
        int     lowerIndex  = i-1;
        int     higherIndex = i;
        Package lower       = pkgs[lowerIndex];
        Package higher      = pkgs[higherIndex];
        String  lowerName   = lower.getName();
        String  higherName  = higher.getName();
        if(higherName.compareTo(lowerName) < 0) { // +++ check this
          // swap
          pkgs[lowerIndex]  = higher;
          pkgs[higherIndex] = lower;
          swapped = true;
        }
      }
    }
  }

  public boolean hasMoreRows() {
    return (curRow+1) < pkgs.length;
  }

  public TableGenRow nextRow() {
    return (++curRow < pkgs.length) ?
            new PackageArrayTableGenRow(pkgs[curRow]) :
            null;
  }
}

class PackageArrayTableGenRow implements TableGenRow {
  Package pkg = null;

  public PackageArrayTableGenRow(Package pkg) {
    this.pkg = pkg;
  }

  public int numColumns() {
    return PackageArrayTableGen.numCols;
  }

  public Element column(int col) {
    String reply = null;
    if(pkg != null) {
      switch(col) {
        case PackageArrayTableGen.NAME: {
          reply = pkg.getName();
        } break;
        case PackageArrayTableGen.IMPTITLE: {
          reply = pkg.getImplementationTitle();
        } break;
        case PackageArrayTableGen.IMPVENDOR: {
          reply = pkg.getImplementationVendor();
        } break;
        case PackageArrayTableGen.IMPVER: {
          reply = pkg.getImplementationVersion();
        } break;
        case PackageArrayTableGen.SPECTITLE: {
          reply = pkg.getSpecificationTitle();
        } break;
        case PackageArrayTableGen.SPECVENDOR: {
          reply = pkg.getSpecificationVendor();
        } break;
        case PackageArrayTableGen.SPECVERSION: {
          reply = pkg.getSpecificationVersion();
        } break;
        case PackageArrayTableGen.SEALED: {
          reply = pkg.isSealed() ? "YES" : "NO";
        } break;
        case PackageArrayTableGen.HASH: {
          reply = "" + pkg.hashCode();
        } break;
      }
    }
    return new StringElement(Safe.TrivialDefault(reply, ""));
  }
}
