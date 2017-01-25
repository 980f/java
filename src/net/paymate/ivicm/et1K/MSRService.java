/* $Id: MSRService.java,v 1.27 2001/11/17 00:38:34 andyh Exp $ */
package net.paymate.ivicm.et1K;
import net.paymate.jpos.data.*;

import net.paymate.jpos.common.*;

import net.paymate.util.ErrorLogStream;
import net.paymate.util.Safe;

import java.util.Vector;

import jpos.*;
import jpos.events.*;
import jpos.services.EventCallbacks;
import jpos.services.MSRService14;


public class MSRService extends Service implements InputServer, MSRService14, MSRConst, JposConst{
  static final String VersionInfo = "MSR Service, (C) PayMate.net 2000 $Revision: 1.27 $";
  static final ErrorLogStream dbg=new ErrorLogStream(MSRService.class.getName());

  public static double PollRate=2.5; //samples per second

  //some names to keep the tracks straight
  protected final static int T1=0;
  protected final static int T2=1;
  protected final static int T3=2;

  protected int TracksToRead;  //3 bit bitmap

  private boolean DecodeData;
  private boolean ParseDecodeData;
  private int reportByTrack;

  MSRData get=new MSRData(); //data read back buffer
  PolledCommand msrread;//construct in oepn()

  private byte[] TrackError; //??? shared for duration of parsing, not synched with 'get'

  public MSRService(String s,ET1K hw) {
    super(s,hw);
    identifiers(VersionInfo,Version1dot4,"EnTouch 1000, Card Reader");
    TrackError = new byte[3];
    msrread=new PolledCommand(finish(new Command(Codes.POLL_MSR_DATA,"Polling Card Data"),new OnCardResponse()),PollRate,this,dbg);
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    reportByTrack = MSR_ERT_CARD;
    TracksToRead = 7; //was just 1&2, spec said all three
    //beware that enTouch will give you an error if you let TTR value persist.
    ParseDecodeData = DecodeData = false;
    super.open(s,eventcallbacks);
  }

  public void prepareForDataEvent(Object blob){//about to post DataEvent
    get.Clear();
    if(blob instanceof byte []){
      SplitTracks(get, (byte []) blob);
    }
  }


  public void setDataEventEnabled(boolean beEnabled) throws JposException {
    super.setDataEventEnabled(beEnabled);
    //for every read of the card we need to ask again for the tracks:
    if(beEnabled){
      sendConfig();//needed to counter-act form cancel which also cancels card reading
      msrread.Start();
    } else {
      msrread.Stop();
    }
  }

  private boolean readTrack(int i){//0..2 for track1..track3 to match other array indexing
    return (TracksToRead & (1<<i))!=0;
  }

  protected Command OnCardResponseBody(Command cmd){//endrun
    try{
      if(gotData(cmd)){
        dbg.VERBOSE("poll got swipe");
        PostData(cmd.payload(1));//the one drops the SEQ field, which is always 0
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

  public void clearInput() throws JposException {
    super.clearInput();
  }

  public String getAccountNumber() throws JposException {
    assertEnabled();
    return get.accountNumber.Image();
  }

  public boolean getCapISO() throws JposException {
    assertClaimed();
    return true;
  }

  public boolean getCapJISOne() throws JposException {
    assertClaimed();
    return false;
  }

  public boolean getCapJISTwo() throws JposException {
    assertClaimed();
    return false;
  }

  public boolean getDecodeData() throws JposException {
    assertClaimed();
    return DecodeData;
  }

  public int getErrorReportingType() throws JposException {
    assertClaimed();
    return reportByTrack;
  }

  public String getExpirationDate() throws JposException {
    assertEnabled();
    return get.expirationDate.YYmm();
  }

  public String getFirstName() throws JposException {
    assertEnabled();
    return get.person.FirstName;
  }

  public String getMiddleInitial() throws JposException {
    assertEnabled();
    return get.person.MiddleInitial;
  }

  public boolean getParseDecodeData() throws JposException {
    assertEnabled();
    return ParseDecodeData;
  }

  public String getServiceCode() throws JposException {
    assertEnabled();
    return get.ServiceCode;
  }

  public String getSuffix() throws JposException {
    assertEnabled();
    return get.person.Suffix;
  }

  public String getSurname() throws JposException {
    assertEnabled();
    return get.person.Surname;
  }

  public String getTitle() throws JposException {
    assertEnabled();
    return get.person.Title;
  }

  public byte[] getTrack1Data() throws JposException {
    assertEnabled();
    return get.track(0).Data().getBytes();
  }

  public byte[] getTrack1DiscretionaryData() throws JposException {
    assertEnabled();
    return get.track(0).DiscretionaryData().getBytes();
  }

  public byte[] getTrack2Data() throws JposException {
    assertEnabled();
    return get.track(T2).Data().getBytes();
  }

  public byte[] getTrack2DiscretionaryData() throws JposException {
    assertEnabled();
    return get.track(T2).DiscretionaryData().getBytes();
  }

  public byte[] getTrack3Data() throws JposException {
    assertEnabled();
    return get.track(T3).Data().getBytes();
  }
//////////////////
//for use by pinpad idiocy
  public void setTrack1Data(byte abyte0[]) throws JposException {
    NotImplemented("setting trackdata for pinpad");
  }

  public void setTrack2Data(byte abyte0[]) throws JposException {
    NotImplemented("setting trackdata for pinpad");
  }

  public void setTrack3Data(byte abyte0[]) throws JposException {
    NotImplemented("setting trackdata for pinpad");
  }
//end pinpad idiocy
//////////////
  public int getTracksToRead() throws JposException {
    assertEnabled();
    return TracksToRead;
  }

  public void setDecodeData(boolean flag) throws JposException {
    assertOpened();
    DecodeData = flag;
    if(!DecodeData) {
      ParseDecodeData = false;
    }
  }

  public void setErrorReportingType(int i) throws JposException {
    assertClaimed();
    Illegal(i != MSR_ERT_CARD && i != MSR_ERT_TRACK, "Invalid reporting type");
    reportByTrack = i;
  }

  public void setParseDecodeData(boolean flag) throws JposException {
    assertClaimed();
    Illegal(flag,"Parsing not supported in service routine");
  }

  public void setTracksToRead(int i) throws JposException {
    assertEnabled();
    Illegal(i < 1 || i > 7,"Tracks out of bounds.");
    TracksToRead=i;
  }

  /**
   * track error text, ivicm indexed
   */
  protected String terrorText[]={
    "No Error",
    "missing start sentinal",
    "track parity error",
    "missing end sentinal",
    "track LRC error",
    "track data overflow",
    "track not on card",
    "track not requested",
  };

  String terrorText(byte code){
    if(code<0 || code>terrorText.length){
      return "Unknown Error Code:"+Safe.ox2(code);
    } else {
      return terrorText[code];
    }
  }

  int jposErrorFor(byte ivicmErrorcode){
    switch(ivicmErrorcode){//converts et1k codes into jpos code:
      case 0: return 0;
      case 1: return JPOS_EMSR_START; //missing start sentinal
      case 2: return JPOS_EMSR_PARITY; //track parity error
      case 3: return JPOS_EMSR_END; //missing end sentinal
      case 4: return JPOS_EMSR_LRC; //track LRC error
      //the following have no jpos equivalent
      case 5: return ET1K_EMSR_TDOV; //track data overflow
      case 6: return ET1K_EMSR_TDNP; //track not on card
      case 7: return ET1K_EMSR_TDIS; //track was disabled by host
      default:return JPOS_E_FAILURE;
    }
  }

  private int ReportTrackErr(int i){
    if(i == -1){//all
      for(i=T1;i<=T3;i++){
        if(ReportTrackErr(i)!=0){//report first error, not combination
          return ReportTrackErr(i);
        }
      }
      return 0;
    }
    return jposErrorFor(TrackError[i]);
  }

  /**
  * @param char from magstripe dump
  * @return track index if it is a track start marker, or -1
  */
  protected int trackStarter(char c){
    return "abc".indexOf((int)c);
  }

  /**
  * @param char from magstripe dump
  * @return track index if it is a track FAULT marker, or -1
  */
  protected int trackFault(char c){
    return "\u00F1\u00F2\u0073".indexOf((int)c);
  }

  private void SplitTracks(MSRData newone, byte [] swipe){
    final byte endSentinal='o';//lower case oh
    //assigning values just to make debugger keep these guys in view.
    int END=-1;
    int start=-1;
    int c=-1;
    int track=-1;

    for(track=TrackError.length;track-->0;){//preset error markers
      TrackError[track]= (byte)(readTrack(track)?ET1K_EMSR_TDNR:0);//not received
    }
    dbg.VERBOSE("raw swipe:"+Safe.hexImage(swipe));
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
            String arf=new String(Safe.subString(swipe,start+1,END));
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
            newone.setError(track,jposErrorFor(TrackError[track]));
            newone.setTrack(track,terrorText(TrackError[track]));//which should make it to server but be corrupt as hell
            String errmsg=TrackData.Name(track)+" has "+ terrorText(TrackError[track]);
            dbg.WARNING(errmsg);
            newone.addError(errmsg);
            END=start;
          } break;
        }//end switch
      }//end for
      if(END>0){
        dbg.ERROR("Left overs! "+Safe.hexImage(Safe.subString(swipe,start+1,END)));
      }
      for(int i=TrackError.length;i-->0;){
        if(TrackError[i] ==ET1K_EMSR_TDNR){
          dbg.ERROR("enTouch forgot to send track "+i);
          String hrswipe=Safe.hexImage(swipe).toString();//human readable
          dbg.ERROR("raw msg from enTouch:"+hrswipe);
          newone.setTrack(i,hrswipe);//which should make it to server but be corrupt as hell
        }
      }
      newone.setTrack(T3,Long.toString(Safe.utcNow())); //hack through jpos layers.
      return;
    }
    catch(ArrayIndexOutOfBoundsException ex){
      dbg.ERROR("AIOB "+END+" start:"+start);
    }
  }

  protected int trackEnables(int tracks) throws JposException {
    if(tracks>=0&&tracks<=7){//+_+ jpos constants.. are simple bit map.
      return Codes.trackSelect[tracks];
    } else {
      return 0;
    }
  }

  private void sendConfig() throws JposException {
    QueueCommand(new Command(Codes.ENABLE_MSR, trackEnables(TracksToRead),1,"trackselects"),new WantZero("Configuring reader"));
  }

}
//$Id: MSRService.java,v 1.27 2001/11/17 00:38:34 andyh Exp $
