package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/IpcPort.java,v $
 * Description:  essentially named pipes, not all OS's have them.
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.io.ByteFifo;

public class IpcPort extends Port {
  private static Hashtable registry;

  ByteFifo baf;
  IpcPort writeto;

  public boolean openas(Parameters parms){
    writeto= (IpcPort)registry.get(parms.getPortName());
    if(writeto!=null){
      setStreams(baf.getInputStream(),writeto.baf.getOutputStream());
      return true;
    } else {
      return false;
    }
  }

  public Port close(){
    writeto=null;  //but leave it in registry
    super.close();
    return this;
  }

  public void finalize(){
    registry.remove(this);//formal. won't happen until app exits.
  }

  public IpcPort(String name) {
    super(name);
    baf=new ByteFifo(true /*blocking*/);
    registry.put(this.nickName(),this);
  }

}
//$Id: IpcPort.java,v 1.3 2004/01/19 17:03:25 mattm Exp $