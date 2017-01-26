package net.paymate.hypercom;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/SignatureParser.java,v $
 * Description:  receives hypercom native iso formatted request and gives back responses
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
  * @todo: work stopped when we gave up on using hypercom's native interface
 */

import net.paymate.awtx.XPoint;
import java.util.Vector;
import net.paymate.util.BitSetIterator;
import net.paymate.util.ErrorLogStream;
import java.io.*;
import java.util.*;
import net.paymate.util.codec.Base64Codec;
import net.paymate.lang.StringX;

// this function was derived from hypercom.erc.util.receipt.SignatureImageGen
// it still contains many coding wierdnesses and inefficiencies.

class xyspec {
  int x;
  int y;
  xyspec(int x, int y){
    this.x=x;
    this.y=y;
  }
}

public class SignatureParser {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SignatureParser.class);

  public static XPoint[] parse(byte [] rawData) {
    xyspec width[] = { new xyspec(6,6), new xyspec(10,7)};//drawto,moveto
        int[][] consts = { { -31, 31, -31, 31, 6, 6 }, { 0, 639, 0, 127, 10, 7 } };
    BitSetIterator bs = new BitSetIterator(rawData);
    if (!checkFormat(bs, width)) {
      bs.rewind();//recover format bits... really should bail if we don't know format!
    }
    boolean jump = false;
    int x;
    int y;
    int maxy = 0;
    Vector vector = new Vector(); // +++ instead, calculate the max number of points that could be in the array (13 bits each?) and presize a point array?
    dbg.Enter("hyperSignatureParse");

    XPoint cursor = new XPoint(0,0);
    XPoint readpt = null;
    int which;
    while(bs.haveMore()){
      boolean shouldjump = bs.next();
      which = shouldjump ? 1 : 0;
      x = getNumber(bs, !shouldjump, width[which].x);
      y = getNumber(bs, !shouldjump, width[which].y);
      jump = shouldjump;
      if(jump) {
        // output break MARK
        dbg.VERBOSE("Outputting mark");
        vector.add(net.paymate.jpos.data.Signature.MARK.clone());
        // set the reference
        cursor.setLocation(x, y);
      } else {
        // apply offset
        cursor.translate(x, y);
      }
      XPoint p = new XPoint(cursor);
      dbg.WARNING((jump ? "JUMP" : "DRAW") + "ing to point " + p);
      vector.add(p); // output new point from clone
      // find the max y at the same time (we have to vertically invert the image)
      maxy = Math.max(p.y, maxy);
    }
    // put the vector into an array, while doing the vertical inversion
    XPoint [] pa = new XPoint[vector.size()];
    for(int i = pa.length; i-->0;) {
      pa[i] = (XPoint)vector.elementAt(i);
      // !!! --- This was the bug where signatures were totally screwed.
      // this code wasn't checking for MARK, but was automatically doing the translation,
      // since MARK is just a reference, it was actually MODIFYING the MARK
      if(!pa[i].equals(net.paymate.jpos.data.Signature.MARK)) {
        // I don't have time to calculate the aspect ratio today,
        // but it appears that the height is about 2/3 of what it should be.
        // so, I'm going to manually make adjustments here, for now.
        // make adjustments to the aspect ratio of the hancock (or wherever), later instead
        // hypercom display: hor=66.5 mm x ver=43.5 mm , resolution of 160x240
        // So, why are they scaling it for 640 x 128?  +++ look into going from the B64 to the hancock directly !!!
        pa[i].y = (int)Math.round( (maxy - pa[i].y) * /*1.5*/ 2.0 ); // bill likes the look of 2.0 better
      }
    }
    return pa;
  }

  private static boolean checkFormat(BitSetIterator bs, xyspec[] width) {
    if (!bs.next()) { //if first bit is 0
      width[1].y = 10;
      getNumber(bs, true, 7); //blow off next 7 bits
      int len = getNumber(bs, true, 8); //get 8 bit signed number
      getNumber(bs, true, len * 8); //blow off that number of bytes
      return true;
    }
    width[1].y = 7;
    return false;
  }

  private static int getNumber(BitSetIterator bs, boolean signed, int bits)  {
    boolean isNegative = false;
    if (signed) {
      isNegative = bs.peek();
    }
    int n = 0;
    for (int i = 0; i < bits; i++) {
      int bit = bs.next() ? 1 : 0;
      n = n << 1 | bit;
    }
    if (isNegative) {
      n |= ~0x3f; //!!! only true for 6 bit width! @todo: get rid of support for the non-variable width
    }
    return n;
  }


/////////////////////////////////
// for grabbing from sample file
/////////////////////////////////

  public static final void main(String [] argv) {
    net.paymate.Main app=new net.paymate.Main(SignatureParser.class);
    app.stdStart(argv);
    if (argv.length == 0) {
      System.out.println("Usage: SigToBytes InputFileName");
      System.exit(1);
    } else {
      byte [] bytes = SigHexFileToBytes(argv[0]);
      System.out.println("encoded="+Base64Codec.toString(bytes));
      XPoint [] points = parse(bytes);
      for(int i = 0; i < points.length; i++) {
        System.out.println("point["+i+"]="+points[i]);
      }
    }
  }

  // this is just for the test signature in a file
  public static final byte [] SigHexFileToBytes(String filename) {
    byte[] sImage = new byte[0];
    File file;

    try {
      file = new File(filename);
      if (file.exists()) {
        System.out.println("file being opened: "+filename);
        byte [] temp = new byte[(int) file.length()];
        if (file.canRead()) {
          BufferedReader in = new BufferedReader(new FileReader(file));
          String str = in.readLine();
          in.close();
          StringTokenizer st = new StringTokenizer(str, " ");
          System.out.println("count should be " + str.length()/3);
          System.out.println("string is "+str);
          int count = 0;
          while (st.hasMoreElements()) {
            String s = st.nextToken();
            int i = StringX.parseInt(s, 16);
            temp[count] = (byte) i;
            count++;
          }
          System.out.println("count is " + count);
          sImage = new byte[count];
          System.arraycopy(temp, 0, sImage, 0, count);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.out.println("FileNotFoundException thrown while reading a file" + e);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("IOException thrown " + e);
    } finally {
      return sImage;
    }
  }
}
//$Id: SignatureParser.java,v 1.10 2003/12/08 22:45:41 mattm Exp $