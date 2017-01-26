package net.paymate.connection;

/**
 * Title:       $Source: /cvs/src/net/paymate/connection/UpdateReply.java,v $
 * Description:  For server to send notices to client
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: UpdateReply.java,v 1.7 2003/08/06 16:59:51 andyh Exp $
 */

import net.paymate.util.*;

public class UpdateReply extends AdminReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.update);
  }
  final static String optKey="options";
  final static String piggyBackKey="piggyBack";
  final static String deathCodeKey="deathCode";

  public ApplianceOptions opt=new ApplianceOptions();//avert NPE's
  public MultiReply piggyBack=new MultiReply();//avert NPE's
/**
 * deathcodes 128 and above are owned by linux itself
 * deathcodes starting from 1 going up are application manager codes
 * use 127 and down for talking to shell script.
 * OR we need to build and use an Enum.
 */
  public int deathCode=0; //ENUM ExitCode

  public void save(EasyCursor ezc){
    super.save(ezc);
    ezc.setBlock(opt,optKey);
    ezc.setInt(deathCodeKey,deathCode);
    ezc.setObject(piggyBackKey,piggyBack);
  }

  public void load(EasyCursor ezc){
    super.load(ezc);
    ezc.getBlock(opt,optKey);
    deathCode=ezc.getInt(deathCodeKey);
    piggyBack=(MultiReply) ezc.getObject(piggyBackKey,MultiReply.class);
  }

  public UpdateReply() {
  //
  }

}
//$Id: UpdateReply.java,v 1.7 2003/08/06 16:59:51 andyh Exp $