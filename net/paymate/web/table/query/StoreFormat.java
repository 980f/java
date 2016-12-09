package net.paymate.web.table.query;

/**
* Title:        StoreFormat<p>
* Description:  The canned query for the Stores screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: StoreFormat.java,v 1.6 2001/11/17 06:16:59 mattm Exp $
*/

import  net.paymate.database.ours.query.*; // StoreInfo
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //HeaderDef
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
// for the enum:
import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;


public class StoreFormat extends RecordFormat {
  private static final ErrorLogStream dbg = new ErrorLogStream(StoreFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new StoreFormatEnum()).numValues()];
  static {
    theHeaders[StoreFormatEnum.NameCol]           = new HeaderDef(AlignType.LEFT, "Name");
    theHeaders[StoreFormatEnum.Address1Col]       = new HeaderDef(AlignType.LEFT, "Address1");
    theHeaders[StoreFormatEnum.Address2Col]       = new HeaderDef(AlignType.LEFT, "Address2");
    theHeaders[StoreFormatEnum.CityCol]           = new HeaderDef(AlignType.LEFT, "City");
    theHeaders[StoreFormatEnum.StateCol]          = new HeaderDef(AlignType.LEFT, "State");
    theHeaders[StoreFormatEnum.ZipcodeCol]        = new HeaderDef(AlignType.LEFT, "Zipcode");
    theHeaders[StoreFormatEnum.CountryCol]        = new HeaderDef(AlignType.LEFT, "Country");
    theHeaders[StoreFormatEnum.EnterpriseNameCol] = new HeaderDef(AlignType.LEFT, "EnterpriseName");
    theHeaders[StoreFormatEnum.TimeZoneCol]       = new HeaderDef(AlignType.LEFT, "TimeZone");
    theHeaders[StoreFormatEnum.HomepageCol]       = new HeaderDef(AlignType.LEFT, "Homepage");
  }

  private StoreInfoRow storeInfo = null;
  public static final String CAID = "storeid";
  public StoreFormat(LoginInfo linfo, StoreInfoRow storeInfo, String title, String absoluteURL, int howMany, String sessionid) {
    super(linfo.colors, title, storeInfo, absoluteURL, howMany, sessionid, linfo.ltf);
    this.storeInfo = storeInfo;
    HeaderDef[] myHeaders = new HeaderDef[theHeaders.length];
    System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length);
    // to give any headers unique info or links, do it here ...
    headers = myHeaders;
  }

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();
      if(tgr != null) {
        // do the real data
        setColumn(StoreFormatEnum.NameCol, new A(Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.ss)).Image() + "&"+CAID+"=" + storeInfo.storeid, storeInfo.storename));
        setColumn(StoreFormatEnum.Address1Col, storeInfo.address1);
        setColumn(StoreFormatEnum.Address2Col, storeInfo.address2);
        setColumn(StoreFormatEnum.CityCol, storeInfo.city);
        setColumn(StoreFormatEnum.StateCol, storeInfo.state);
        setColumn(StoreFormatEnum.ZipcodeCol, storeInfo.zipcode);
        setColumn(StoreFormatEnum.CountryCol, storeInfo.country);
        setColumn(StoreFormatEnum.EnterpriseNameCol, storeInfo.EnterpriseName);
        setColumn(StoreFormatEnum.TimeZoneCol, storeInfo.javatz);
        setColumn(StoreFormatEnum.HomepageCol, storeInfo.storehomepage); // should be a link by default
      } else {
        dbg.WARNING("RecordFormat.next() returned null!");
      }
    } catch (Exception t2) {
      dbg.WARNING("Unknown and general exception generating next row content.");
    } finally {
      dbg.Exit();
      return (tgr == null) ? null : this;
    }
  }
}

// +++ make external and auto-generated?
class StoreFormatEnum extends TrueEnum {
  public final static int NameCol           =0;
  public final static int Address1Col       =1;
  public final static int Address2Col       =2;
  public final static int CityCol           =3;
  public final static int StateCol          =4;
  public final static int ZipcodeCol        =5;
  public final static int CountryCol        =6;
  public final static int EnterpriseNameCol =7;
  public final static int TimeZoneCol       =8;
  public final static int HomepageCol       =9;

  public int numValues(){ return 10; }
  static final TextList myText = TrueEnum.nameVector(StoreFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final StoreFormatEnum Prop=new StoreFormatEnum();
  public StoreFormatEnum(){
    super();
  }
  public StoreFormatEnum(int rawValue){
    super(rawValue);
  }
  public StoreFormatEnum(String textValue){
    super(textValue);
  }
  public StoreFormatEnum(UnsettledTransactionFormatEnum rhs){
    this(rhs.Value());
  }
}
