package net.paymate.ivicm.et1K;

import java.util.Vector;
import net.paymate.util.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/BlockCommand.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: BlockCommand.java,v 1.6 2001/10/30 18:54:42 andyh Exp $
 */

public class BlockCommand extends Command {
  int cursor=-1;
//only one thread has authority to use this object so we don't bother with 'synchronize'

  Vector content=new Vector();

  public LrcBuffer outgoing(){
    if(cursor<0){
      firstCommand();
    }
    return outgoing;
  }

  public void addCommand(LrcBuffer cmd) {
    if(cmd==null){
      ErrorLogStream.Debug.ERROR("null command attempted in block command "+errorNote+" size:"+content.size());
    } else {
      content.add(cmd);
    }
  }

  public void insert(int index,LrcBuffer cmd){
    content.insertElementAt(cmd,index);
  }

  int size(){
    return content.size();
  }

  LrcBuffer elementAt(int index){
    return (LrcBuffer) content.elementAt(index);
  }

  public void insertBlock(BlockCommand bc){
    for(int i=bc.size();i-->0;){//reverse iteration required
      content.insertElementAt(bc.elementAt(i),0);
    }
  }

  public boolean hasMore(){
    return cursor<size();
  }

  public Command next(){
//    ErrorLogStream.Debug.ERROR("BlockCommand.nextCommand("+cursor+")/"+size());
    outgoing=hasMore() ? (LrcBuffer) content.elementAt(cursor++) : null;
    return outgoing!=null?this:null;
  }

  public Command restart(){//called to restart
    firstCommand();
    return super.restart();
  }

  public Command firstCommand(){
    cursor=0;
    return next();
  }

  public void setElementAt(LrcBuffer cmd,int index){
    content.setElementAt(cmd,index);
  }

  public BlockCommand(String errnote){
    super();
    errorNote=errnote;
  }

  public BlockCommand(){
  //
  }

  public void dumpSent(ErrorLogStream somedbg,int severity) {
    somedbg.rawMessage(severity,"subcommand "+cursor+" out of:"+size());
    for(int i=cursor;i-->0;){
      somedbg.rawMessage(severity,"cmd["+i+"] "+elementAt(i).toSpam(5));
    }
  }

}
//$Id: BlockCommand.java,v 1.6 2001/10/30 18:54:42 andyh Exp $
