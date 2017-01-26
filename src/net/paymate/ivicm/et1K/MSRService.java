/* $Id: MSRService.java,v 1.37 2003/07/27 05:35:04 mattm Exp $ */
package net.paymate.ivicm.et1K;

import net.paymate.jpos.data.*;
import net.paymate.lang.StringX;
import net.paymate.util.*;
import net.paymate.text.Formatter;
import java.util.Vector;

public class MSRService extends Service {
  static final String VersionInfo = "MSR Service, (C) PayMate.net 2000 $Revision: 1.37 $";
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(MSRService.class);

  public static double PollRate=2.5; //samples per second

  //some names to keep the tracks straight
  private final static int T1=0;
  private final static int T2=1;
  private final static int T3=2;

  private int TracksToRead=1+2;  //would add 4 for track three

  private boolean DecodeData=false;
  private boolean ParseDecodeData=false;

  PolledCommand msrread;//construct in oepn()

  private byte[] TrackError; //??? shared for duration of parsing, not synched with 'get'

  public MSRService(String s,ET1K hw) {
    super(s,hw);
//    identifiers(VersionInfo,Version1dot4,"EnTouch 1000, Card Reader");
    TrackError = new byte[3];
    msrread=new PolledCommand(finish(new Command(OpCode.POLL_MSR_DATA,"Polling Card Data"),new OnCardResponse()),PollRate,this,dbg);
  }

  public void Acquire(){
    sendConfig();//needed to counter-act form cancel which also cancels card reading
    msrread.Start();//+_+ should wait for response from sendConfig() command
  }

  public void Flush(){
    msrread.Stop();
  }

/////////////////////////////////////

  private boolean readTrack(int i){//0..2 for track1..track3 to match other array indexing
    return (TracksToRead & (1<<i))!=0;
  }

  private Command OnCardResponseBody(Command cmd){//endrun
    try{
      if(gotData(cmd)){
        dbg.VERBOSE("poll got swipe");
        MSRData get= new MSRData();
        SplitTracks(get, cmd.payload(1));//the one drops the SEQ field, which is always 0
        PostData(get);
      } else {
        dbg.VERBOSE("poll again");
        msrread.Start(); //read until we get data
      }
      return null;
      // .. as we can always get the whole track in one command since T3 is not implemented
    } catch(Exception silly){
      dbg.Caught("receiving swipe got:",silly);
      return null;//+_+ should retry
    }
  }

  class OnCardResponse implements Callback {//66
    public Command Post(Command cmd){
      return OnCardResponseBody(cmd);//endrun
    }
  }


  private String ErrorFor(byte ivicmErrorcode){
    switch(ivicmErrorcode){//converts et1k codes into jpos code:
      case 0: return "";
      case 1: return "missing start sentinal";
      case 2: return "track parity error";
      case 3: return "missing end sentinal";
      case 4: return "track LRC error";
      //the following have no jpos equivalent
      case 5: return "track data overflow";
      case 6: return "track not on card";
      case 7: return "track was disabled by host";
      default:return "unspecified faiure";
    }
  }


  /**
  * @param char from magstripe dump
  * @return track index if it is a track start marker, or -1
  */
  private int trackStarter(char c){
    return "abc".indexOf((int)c);
  }

  /**
  * @param char from magstripe dump
  * @return track index if it is a track FAULT marker, or -1
  */
  private int trackFault(char c){
    return "\u00F1\u00F2\u0073".indexOf((int)c);
  }

  private void SplitTracks(MSRData newone, byte [] swipe){
    final byte endSentinal=Ascii.o;//'o';//lower case oh
    //assigning values just to make debugger keep these guys in view.
    int END=-1;
    int start=-1;
    int c=-1;
    int track=-1;

    for(track=TrackError.length;track-->0;){//preset error markers
      TrackError[track]= (byte)(readTrack(track)?ET1K_EMSR_TDNR:0);//not received
    }
    dbg.VERBOSE("raw swipe:"+Formatter.hexImage(swipe));
    END=swipe.length;
    try {
      for(start=END;start-->0;){//from 'o' looking towards start
        c= swipe[start]&0xff;
        switch(c){
          case 0x6f:{ //'o'
            END=start;
            dbg.VERBOSE("End sentinal is at:"+END);
          } break;

          case 0x61:
          case 0x62:
          case 0x63: {
            track=(c&3)-1;
            dbg.VERBOSE("Extracting "+TrackData.Name(track));
            String arf=new String(ByteArray.subString(swipe,start+1,END));
            newone.setTrack(track,arf);
            TrackError[track]=0;
            dbg.VERBOSE(TrackData.Name(track)+"="+arf);
            END=start;
          } break;

          case 0xF1:
          case 0xF2:
          case 0x73: { //yep, at least according to spec
            track= (c&3)-1;
            TrackError[track]=swipe[start+1];
            String edetail=ErrorFor(TrackError[track]);
            newone.setTrack(track,edetail);//which should make it to server but be corrupt as hell
            String errmsg=TrackData.Name(track)+" has "+ edetail;
            dbg.WARNING(errmsg);
            newone.addError(errmsg);
            END=start;
          } break;
        }//end switch
      }//end for
      if(END>0){
        dbg.ERROR("Left overs! "+Formatter.hexImage(ByteArray.subString(swipe,start+1,END)));
      }
      for(int i=TrackError.length;i-->0;){
        if(TrackError[i] ==ET1K_EMSR_TDNR){
          dbg.ERROR("enTouch forgot to send track "+i);
          String hrswipe=String.valueOf(Formatter.hexImage(swipe));//human readable
          dbg.ERROR("raw msg from enTouch:"+hrswipe);
          newone.setTrack(i,hrswipe);//which should make it to server but be corrupt as hell
        }
      }
      newone.setTrack(T3,String.valueOf(UTC.Now())); //hack through jpos layers.
      return;
    }
    catch(ArrayIndexOutOfBoundsException ex){
      dbg.ERROR("AIOB "+END+" start:"+start);
    }
  }

  private int trackEnables(int tracks){
    if(tracks>=0&&tracks<=7){//+_+ jpos constants.. are simple bit map.
      return Codes.trackSelect[tracks];
    } else {
      return 0;
    }
  }

  private void sendConfig(){
    QueueCommand(new Command(OpCode.ENABLE_MSR, trackEnables(TracksToRead),1,"trackselects"),new WantZero("Configuring reader"));
  }
//////////////////////
//errors related to tracks
  public static final int JPOS_EMSR_START = 201;
  public static final int JPOS_EMSR_END = 202;
  public static final int JPOS_EMSR_PARITY = 203;
  public static final int JPOS_EMSR_LRC = 204;
  //constants added by paymate for IVICM (need to check jpos1.5):
  public static final int ET1K_EMSR_TDOV=205;//track data overflow
  public static final int ET1K_EMSR_TDNP=206;//track not on card according to device
  public static final int ET1K_EMSR_TDIS=207;//track was disabled by host
  public static final int ET1K_EMSR_TDNR=208;//track was not received

}
//$Id: MSRService.java,v 1.37 2003/07/27 05:35:04 mattm Exp $
