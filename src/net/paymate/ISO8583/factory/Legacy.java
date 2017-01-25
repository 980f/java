package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/Legacy.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class Legacy implements Factory {
  Protocol protocol=new LegacyProtocol();

  Create creator;
  Extract extractor;


  class creater extends Create {

  }

  class extracter extends Extract {

  }

  public Create Creator(){
    if(creator==null){
      creator=new creater();
    }
    return creator;
  }

  public Extract Extractor(){
    if(extractor==null){
      extractor=new extracter();
    }
    return extractor;
  }

  public Legacy() {

  }

}
//$Id: Legacy.java,v 1.1 2001/11/14 13:53:45 andyh Exp $