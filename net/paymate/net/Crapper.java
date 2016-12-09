package net.paymate.net;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: Crapper.java,v 1.2 2001/07/19 01:06:51 mattm Exp $
 */

import java.io.*;
import java.util.Random;
import java.security.SecureRandom;


class NotRandom extends Random {
/**
 * override the only function that we know is used
 */
  public int nextInt() {
    return 0; //chosen as it is the identity value of xor
  }
  /**
   * +_+ should override all data producers. will move this class to np.util when we do so.
   */
}

/**
 * Crapper is a factory for creating Crapy IO streams. It contains the rules for
 * providing the keys and randomizers to the underlying streams.
 * A Crappy stream converts its flow into what appears to be crap, i.e. encryption
 * A crappy stream can be contained in another one to implement multiple key encryption.
 */
public class Crapper {
  Random crap;

  public InputStream In(InputStream is){
    return CrapIn.New(is,crap);
  }

  public OutputStream Out(OutputStream os){
    return CrapOut.New(os,crap);
  }

  void start(long screed){
    crap=new SecureRandom();
    crap.setSeed(screed);//someone else makes up a key
  }

////////////////////////
// will add more variants of these as time goes on.
  public static final Crapper Rev1(File f) {//a stupid but repeatable key:
    long screed=f.hashCode()<<27| 0xDEADBEEFFEEDFACEL;
    Crapper newone=new Crapper();
    newone.start(screed);
    return newone;
  }

  public static final Crapper Rev0(File f) {
    Crapper newone=new Crapper();
    newone.crap=new NotRandom();
    return newone;
  }

}
//$Id: Crapper.java,v 1.2 2001/07/19 01:06:51 mattm Exp $