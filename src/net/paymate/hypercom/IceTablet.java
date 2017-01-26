package net.paymate.hypercom;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IceTablet.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import net.paymate.peripheral.*;
import net.paymate.data.*;
import net.paymate.jpos.data.*;
import net.paymate.util.*;
import net.paymate.ivicm.et1K.SignatureType;//horribly misplaced!
import net.paymate.terminalClient.SigningOption;
import net.paymate.util.codec.Base64Codec;

public class IceTablet extends Tablet {
  IceTerminal parent;

  private boolean isFunctional=false;

  /**
   * @return whether tablet is in usable condition
   */
  public boolean isFunctional(){
    return isFunctional;
  }

  public PosPeripheral setEnable(boolean beon){
    parent.sendEnableCommand(IceCommand.Sigcap,beon);
    return this;
  }

  public boolean getSignature(){
    return parent.sendCommand(IceCommand.Simple(IceCommand.Sigcap,IceCommand.GetBlock)); //+_+doit(false,true);
  }
  private static SignatureType IceSigType=new SignatureType(SignatureType.Hypercom);
  /**
   * @param packet should be an iceCommand
   * N ncra H hypercom with header P points list E error
   */
  public void process(AsciiBufferParser bp) {
    switch (bp.getChar()) {
      case 'K': {
        int soption;
        switch ( (int) bp.getDecimalFrame()) {
          default: //join most severe option
          case Ascii.CAN:
            soption = SigningOption.SignPaper;
            break;
          case Ascii.BS:
            soption = SigningOption.StartOver;
            break;
          case Ascii.CR:
            soption = SigningOption.DoneSigning;
            break;
        }
        Post(new SigningOption(soption));
      }
      break;
      case 'E': { //"Timeout","Failed"
        int soption;
        String failure;
        switch (bp.getChar()) {
          case 'H': {
            failure = bp.getROF();
            soption = SigningOption.StartOver;
          }
          break;
          case 'S': {
            failure = bp.getROF();
            soption = SigningOption.SignPaper;
          }
          break;
          case 'T': {
            failure = "Too Much Ink";
            soption = SigningOption.DoneSigning;
          }
          break;
          default: {
            failure = "Unknown Response";
            soption = SigningOption.SignPaper;
          }
          break;

        }
        parent.dbg.ERROR("SignatureCaptureError:" + failure);
        Post(new SigningOption(soption));
      }
      break;
      case 'H': { //remaining bytes are base64 encoded hypercom stream.
        parent.Post(SigData.CreateFrom(Base64Codec.decode(bp.getROF().
            toCharArray()), IceSigType));
      }
      break;
      default:
        break;
    }
    parent.sigCapturing = false; //regardless of response the iceterminal has stopped
  }

  public IceTablet(IceTerminal parent) {
    super(parent); //IceTerminal wraps a PosPeripheral queue
    this.parent=parent; //and wraps access to serial port
  }
}
//$Id: IceTablet.java,v 1.10 2003/12/08 22:45:41 mattm Exp $