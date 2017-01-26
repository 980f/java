/* $Id: PollResponder.java,v 1.5 2001/12/14 02:40:11 andyh Exp $ */
package net.paymate.ivicm.et1K;

public class PollResponder implements Callback {
  String locus;
  Callback parser;

  public Command Post(Command cmd){
    int rsp=cmd.response();
    if(cmd.nothingThere()){
      return null; //+_+ make this trigger another poll? rather than poll spamming?
    }
    if(rsp== ResponseCode.MORE_DATA_READY || rsp==ResponseCode.SUCCESS ){//more data, last data
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
//$Id: PollResponder.java,v 1.5 2001/12/14 02:40:11 andyh Exp $
