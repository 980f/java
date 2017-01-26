package net.paymate.ivicm.et1K;

import java.util.Vector;
import net.paymate.util.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/BlockCommand.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: BlockCommand.java,v 1.11 2002/12/19 04:14:58 mattm Exp $
 */

public class BlockCommand extends Command {
  private int cursor=-1;
//only one thread has authority to use any one object of this class so we don't bother with 'synchronize'

  private Vector content=new Vector();

  public LrcBuffer outgoing(){//LrcBuffer I/F
    if(cursor<0){
      firstCommand();
    }
    return outgoing;
  }

  void addCommand(LrcBuffer cmd) {
    if(cmd==null){
      dbg.ERROR("null command attempted in block command "+errorNote+" size:"+content.size());
    } else {
      content.add(cmd);
    }
  }

  void insert(int index,LrcBuffer cmd){
    content.insertElementAt(cmd,index);
  }

  int size(){
    return content.size();
  }

  LrcBuffer elementAt(int index){
    return (LrcBuffer) content.elementAt(index);
  }

  void insertBlock(BlockCommand bc){
    for(int i=bc.size();i-->0;){//reverse iteration required
      content.insertElementAt(bc.elementAt(i),0);
    }
  }

  boolean hasMore(){
    return cursor<size();
  }

  public Command next(){
    if(cursor<0) {//+_+ someone forgot to first()
      cursor=0;
    }
    outgoing=hasMore() ? (LrcBuffer) content.elementAt(cursor++) : null;
    return outgoing!=null?this:null;
  }

  public Command truncate(){//when hopeless error ocurs during sendings
    content.clear();
    addCommand(FormCommand.AbortCommand);
    return restart();
  }

  public Command restart(){//called to restart
    firstCommand();
    return super.restart();
  }

  /*package*/ Command firstCommand(){
    cursor=0;
    return next();
  }

  void setElementAt(LrcBuffer cmd,int index){
    content.setElementAt(cmd,index);
  }

  BlockCommand(String errnote){
    super(errnote);
  }

  void dumpSent(ErrorLogStream somedbg,int severity) {
    somedbg.rawMessage(severity,"subcommand "+(cursor-1)+" out of:"+size());
    for(int i=cursor;i-->0;){
      somedbg.rawMessage(severity,"cmd["+i+"] "+elementAt(i).toSpam(5));
    }
  }

}
//$Id: BlockCommand.java,v 1.11 2002/12/19 04:14:58 mattm Exp $
