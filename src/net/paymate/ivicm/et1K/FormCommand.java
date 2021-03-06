package net.paymate.ivicm.et1K;
/* $Id: FormCommand.java,v 1.20 2003/07/27 05:35:04 mattm Exp $ */


//general purpose programming aids:
import net.paymate.util.*;
import java.io.*;
import java.util.Vector;
import net.paymate.lang.ObjectX;
import net.paymate.data.*; // Packet;//used to buld oversized command.
import net.paymate.terminalClient.IviForm.*;


/**
* to implement a test that allows us to purge multiple showForms in the queue
*/
class StoredFormCommand extends BlockCommand {
  public StoredFormCommand(String errnote){
    super(errnote);
  }

  /**
   * all stored form commands are to be treated as equal.
   * this lets the putUnique functionality remove multiple form commands from entouch queue.
   */
  public boolean equals(Object o){
    return o instanceof StoredFormCommand;
  }

}//end StoredFormCommand

public class FormCommand {
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(FormCommand.class);
  private static final int MAX_BODY = 218;//only first packet needs this limit
  //but the code applied it to all for convenience (or in ignorance)
  private static final int MAX_BOXES = 8;

  static final LrcBuffer AbortCommand=Command.JustOpcode(OpCode.ABORT);

  private Form pmForm;//pm form definition object


  private static final TextList storedPcxes=new TextList();

  public final static int badGraf=ObjectX.INVALIDINDEX;

  protected int grafIndex(){//seems that 0 is verboten
    dbg.VERBOSE("is storedPcxes null?"+(storedPcxes==null));
    String pcxfilename=GetPCXFileName();
    dbg.VERBOSE("is name null?"+pcxfilename);
    return 1+storedPcxes.indexOf(pcxfilename);
  }

  /**
  * put image download at head of form download, if not stored
  */
  private int prepareImage(BlockCommand wad) {
    try {
      int grafNumber=grafIndex();
      if(grafNumber>0){//already been sent (unless comm failed!)
        return grafNumber;
      }
      String pcxname=GetPCXFileName();//4 debug
      dbg.VERBOSE("storing image:"+pcxname);
      return formatImage(new FileInputStream(pmForm.pcxFile),wad);
    } catch (Exception ex){
      dbg.WARNING("Caught while processing Image for download:"+ex);
      return badGraf; //ought to break something....if ignored
    }
  }

  private final static int pcxHeaderSize=128;
  private final static int pcxBestBlock=200; //++_+ get from et1K

  public static final FormCommand fromForm(Form form){
    FormCommand newone=new FormCommand();
    newone.pmForm= form;
    return newone;
  }

  public boolean hasGraphic(){
    return pmForm.hasGraphic();
  }

  /**
  * only called for new image downloads
  */
  int formatImage(InputStream pcx,BlockCommand wad) {
    try {
      int grafNumber=1+storedPcxes.size();//new number
      int block=0;
      int blocksize=pcxHeaderSize;//first block
      int rof; //size of rest of file

      while((rof=pcx.available())>0){
        //1st block of 128 is pcx header
        //200 is five scan lines of image.
        if(rof < blocksize){
          blocksize= rof; //should be last block
        }
        dbg.VERBOSE("adblock:"+block+" len:"+blocksize);
        LrcBuffer buf=Command.Buffer(OpCode.SEND_ADVERTISEMENT,blocksize+2);
        buf.append(block++);  //block number
        buf.append(grafNumber); //screen number
        while(blocksize-->0){
          buf.append(pcx.read());
        }
        buf.end();
        wad.addCommand(buf);
        blocksize= pcxBestBlock; //best size for NExt block
      }
      LrcBuffer buf=Command.Buffer(OpCode.SEND_ADVERTISEMENT,2);
      buf.append(block++);  //block number
      buf.append(grafNumber); //screen number
      buf.end();
      wad.addCommand(buf);
      //manual talks about appending control box info here....not needed, can overlay a form.
      storedPcxes.Add(pmForm.pcxResource);//this is here so that a new form is not registered if we get exceptions while formatting it
      return grafNumber;
    } catch(IOException ioex) {
      dbg.Caught("Couldn't fetch pcx data:",ioex);
      return badGraf; //ought to break something....
    }
  }

  public boolean HasSignature(){
    return pmForm.hasSignature();
  }


  public boolean HasButtons(){
    return pmForm.buttonCount>0;
  }

  private static final int FORM_FORM = 3;
  private static final int FORM_STORED = 4;


  public Form pmForm(){
    return pmForm;
  }
  public int FormNumber(){
    if(pmForm.myNumber <0) {
      return 1;
    } else if(pmForm.myNumber >40) {//???who imposed this rule???
      return 1; //SIC
    } else {
      return pmForm.myNumber;
    }
  }

  protected static final int packNibbles(int high,int low){
    return ((high&15)<<4) + (low&15);
  }

  public BlockCommand fullCommand(boolean forStoring){
    dbg.Enter("fullCommand");//#gc
    try {
      BlockCommand wad = new BlockCommand("SendFormCommand");
      if(!forStoring){
        wad.addCommand(AbortCommand);//stop input
        wad.addCommand(Command.JustOpcode(OpCode.CLEAR_SCREEN));//erase background
      }
      if(pmForm.hasGraphic()){//needs to be stored
        int grafNumber=prepareImage(wad);  //which creates a bunch of commands to send pcx file, if needed
        if(grafNumber>=0){
          if(!forStoring){
            //if there is a grpahic we have to show it now
            wad.addCommand(Command.OpArg(OpCode.DISPLAY_STORED_FORM,grafNumber));
          }
        } else {
          dbg.ERROR("graphic background not found or is corrupt");
        }
      }
      //
      Packet whole = new Packet(1024);//whole command, will partition later
      //for each text item
      for(int k = pmForm.size(); k --> 0;){
        FormItem thing=pmForm.item(k);
        Legend text=null;

        if(thing instanceof Legend){// a legend is text...
          text=(Legend)thing;
        } else if (thing instanceof TextButton) {//Other things HAVE text
          text=((TextButton) thing).Legend();
        }
        if(text!=null){
          dbg.VERBOSE("text item:"+text.getText());
          //text box UD data
          whole.append(text.getText().length());
          whole.append(text.y());
          whole.append(text.x());
          int attrib = text.attr();
          int font = text.code();
          if(font == 3){//jpos font to entouch font translation , sole mismatch
            font = 6;
          }
          whole.append(packNibbles(attrib,font));
          whole.append(text.getText());
        }
      }
////////
// got to collate buttons
      Vector cBoxes=new Vector(pmForm.buttonCount);
      //for each button item
      for(int k = pmForm.size(); k --> 0;){
        FormItem thing=pmForm.item(k);
        if(thing instanceof Button){
          cBoxes.add(thing);
        }
      }


      int cbs=cBoxes.size();
      if(cbs > 0) {
        dbg.VERBOSE("control box count="+cbs);
        whole.append(cbs);
        whole.append(0);
        whole.append(0);
        whole.append(4);
        for(int k = cbs; k-->0;){
          Button thisbox = (Button)cBoxes.elementAt(k);
          dbg.VERBOSE("control box #"+thisbox.guid);
          whole.append(thisbox.guid);
          whole.append(thisbox.y());
          whole.append(thisbox.x());
          whole.append(packNibbles(thisbox.Width() - 1 ,thisbox.Height() - 1));//NOT5992
        }
      }
///////////////
      //now partition...due to maximum size of commands as sent to entouch

      int seqn=0;
      int total=whole.ptr();//number of bytes
      LrcBuffer cmd;
      //1st command needed some info about the ones to follow.
      cmd=Command.Op(OpCode.SendForm);
      cmd.append(seqn++);
      cmd.append(0); //unused XOR feature
      if(forStoring){
        cmd.append(FORM_STORED);
        cmd.append(pmForm.myNumber);//ID for retrieval by showStoredForm
        cmd.append(0);//unused survey count
        //guessing little endian in absence of doc
        //total bytes in form definition:
        cmd.append(total);
        cmd.append(total>>8);
      } else {
        cmd.append(FORM_FORM);//form style, we always use Form!!!
      }

      SigBox sb=pmForm.signature();
      if(sb!=null) {
        dbg.VERBOSE("has signature");
        cmd.append(sb.y());
        cmd.append(sb.x());
        cmd.append(sb.Height());
        cmd.append(sb.Width());
      } else {
        dbg.VERBOSE("no signature");
        cmd.append(0);
        cmd.append(0);
        cmd.append(0);
        cmd.append(0);
      }

      cmd.end();
      wad.addCommand(cmd);

      for(int start=0 , thisblock=0 ; start < total ; start += thisblock){
        dbg.VERBOSE("Block:"+seqn);
        cmd=Command.Op(OpCode.SendForm);
        cmd.append(seqn++);
        //.      cmd.append(0);//NOT5992
        thisblock=Math.min(total-start,MAX_BODY);
        cmd.append(whole.extract(start, thisblock));
        cmd.end();
        wad.addCommand(cmd);
      }

      if(pmForm.hasGraphic()){//. NOT5992
        dbg.VERBOSE("Adding bitmap flag");
        cmd=Command.Op(OpCode.SendForm);
        cmd.append(seqn++);
        //.the next 4 bytes are undocumented majic to make the graphic show behind a form
        cmd.append( 1);
        cmd.append( 0);
        cmd.append( 0);
        cmd.append(0xff);
        cmd.end();
        wad.addCommand(cmd);
      }

      //final nullish block terminates command set
      cmd=Command.Op(OpCode.SendForm);
      cmd.append(seqn);
      cmd.end();
      wad.addCommand(cmd);
      return wad;
    } finally {
      dbg.Exit();//#gc
    }
  }

  private String GetPCXFileName(){
    return pmForm!=null?pmForm.pcxResource:null;
  }

  /**
  * if form is already stored we don't need to make the wad.
  */
  BlockCommand asStored(){
    dbg.Enter("asStored");//#gc
    try {
      BlockCommand newone=new StoredFormCommand("StoredForm");
      newone.addCommand(Command.JustOpcode(OpCode.ABORT));
      newone.addCommand(Command.JustOpcode(OpCode.CLEAR_SCREEN));
      if(pmForm.hasGraphic()){
        newone.addCommand(Command.OpArg(OpCode.DISPLAY_STORED_FORM,grafIndex()));
      }
      newone.addCommand(Command.OpTwoArg(OpCode.DISPLAY_STORED_FORM,0,pmForm.myNumber));
      return newone;
    }
    finally{
      dbg.Exit();//#gc
    }
  }

  public String toSpam(){
    return "Form:"+    this.FormNumber()+
    (this.HasButtons()?" #Buttons:"+this.pmForm.buttonCount: " No buttons")+
    (this.hasGraphic()?" pcx:"+this.GetPCXFileName():" No graphics")+
    (this.HasSignature()?" has ":" no ")+"signature slot"+
    " pmform:"+ this.pmForm.toSpam()
    ;
  }

}
//$Id: FormCommand.java,v 1.20 2003/07/27 05:35:04 mattm Exp $
