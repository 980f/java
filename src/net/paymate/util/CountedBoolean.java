/* $Id: CountedBoolean.java,v 1.5 2001/10/03 12:51:08 mattm Exp $ */
/**
a communal flag that is true so long as any one has set it.
if someone sets it and never clears it then it is true for evermore.

*/

// +++ make this threadsafe

package net.paymate.util;

public class CountedBoolean {
  protected int count=0;

  public boolean booleanValue(){//make us look like Boolean
    return count>0;
  }

  public boolean set(){
    ++count;
    return booleanValue();
  }

  public boolean clr(){
    if(booleanValue()){
      --count;
    }
    return booleanValue();
  }

  public boolean setto(boolean enableit){
    return enableit? set() : clr();
  }

  public void Reset(){
    count=0;
  }

  public CountedBoolean(){
    Reset();
  }

  public CountedBoolean(boolean enableit){
    this();
    setto(enableit);
  }

}
//$Id: CountedBoolean.java,v 1.5 2001/10/03 12:51:08 mattm Exp $
