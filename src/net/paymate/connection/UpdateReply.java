package net.paymate.connection;

/**
 * Title:       $Source: /cvs/src/net/paymate/connection/UpdateReply.java,v $
 * Description:  For server to send notices to client
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: UpdateReply.java,v 1.4 2001/07/06 18:56:38 andyh Exp $
 */

import net.paymate.util.*;

public class UpdateReply extends AdminReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.update);
  }
/**
 * things that aren't storewide but also aren't terminal specific
 * future possibilities are dialup info...
 */
  public ApplianceOptions opt=new ApplianceOptions();
  final static String optKey="options";
/**
 * deathcodes 128 and above are owned by linux itself
 * deathcodes starting from 1 going up are application manager codes
 * use 127 and down for talking to shell script.
 * OR we need to build and use an Enum.
 */
  public int deathCode=0;
  final static String deathCodeKey="deathCode";

  public void save(EasyCursor ezc){
    super.save(ezc);
    opt.saveas(optKey,ezc);
    ezc.setInt(deathCodeKey,deathCode);
  }

  public void load(EasyCursor ezc){
    super.load(ezc);
    opt.loadfrom(optKey,ezc);
    deathCode=ezc.getInt(deathCodeKey);
  }

  public UpdateReply() {
  //
  }

}
//$Id: UpdateReply.java,v 1.4 2001/07/06 18:56:38 andyh Exp $