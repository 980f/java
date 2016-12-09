/* $Id: PollResponder.java,v 1.4 2001/06/26 01:35:21 andyh Exp $ */
package net.paymate.ivicm.et1K;

public class PollResponder implements Callback {
  String locus;
  Callback parser;

  public Command Post(Command cmd){
    int rsp=cmd.response();
    if(cmd.nothingThere()){
      return null; //+_+ make this trigger another poll? rather than poll spamming?
    }
    if(rsp== Codes.MORE_DATA_READY || rsp==Codes.SUCCESS ){//more data, last data
      if(parser!=null){
        parser.Post(cmd);
      }
      return null;
    }
    cmd.service.PostFailure(locus+" Error From Device:"+cmd.response());
    return null;//+_+
  }

  public PollResponder(String commando,Callback pp){
    locus =commando;
    parser=pp;
  }

}
//$Id: PollResponder.java,v 1.4 2001/06/26 01:35:21 andyh Exp $
