/* $Id: FormButtonData.java,v 1.6 2000/09/08 01:56:48 andyh Exp $ */
package net.paymate.jpos.Terminal;

public class FormButtonData implements Event {
  public EventType Type(){
    return new EventType(EventType.FormButtonData);
  }

  public class Fbutton {
    public byte ID;
    public boolean wasPressed;
    public Fbutton(byte even, byte odd){
      ID=even;
      wasPressed= odd!=0;
    }
  }

  public Fbutton [] button;
  public void parseButtons(byte[] raw){
    if(raw!=null){//driver occasionally fubars and doesn't send the buttons!
    int len=raw.length;
    //+++if 0 or odd object
    button= new Fbutton [len/2]; //two bytes per button
    for(int i=0;i<len;i++){
      button[i/2]=new Fbutton(raw[i++],raw[i]);
    }
    } else {
    button= new Fbutton[0];
    }
  }

  public FormButtonData (byte [] packed){
    parseButtons(packed);
  }

  //not using surveys yet,
  //not using generic keypads, customer only enters PINs

}
//$Id: FormButtonData.java,v 1.6 2000/09/08 01:56:48 andyh Exp $
