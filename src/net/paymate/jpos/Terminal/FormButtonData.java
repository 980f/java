/* $Id: FormButtonData.java,v 1.7 2001/12/04 06:52:53 andyh Exp $ */
package net.paymate.jpos.Terminal;

public class FormButtonData  {
  public FormButtonStatus [] button;
  public void parseButtons(byte[] raw){
    if(raw!=null){//driver occasionally fubars and doesn't send the buttons!
      int len=raw.length;
      //+++if length 0 or odd complain
      button= new FormButtonStatus [len/2]; //two bytes per button
      for(int i=0;i<len;i++){
        button[i/2]=new FormButtonStatus(raw[i++],raw[i]);
      }
    } else {
      button= new FormButtonStatus[0];
    }
  }

  public FormButtonData (byte [] packed){
    parseButtons(packed);
  }

}
//$Id: FormButtonData.java,v 1.7 2001/12/04 06:52:53 andyh Exp $
