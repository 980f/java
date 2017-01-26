package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/Associate.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.data.*; // Associateid
import net.paymate.data.sinet.*;
import net.paymate.util.EasyProperties;
import net.paymate.lang.StringX;

public class Associate extends EntityBase {

  private static final SinetClass mySinetClass = new SinetClass(SinetClass.Associate);
  public SinetClass getSinetClass() {
    return mySinetClass;
  }

  public static final String BROWSELINES = "browselines";
  public static final String ASSOCIATEID = "associateid";
  public static final String COLORSCHEMEID = "colorschemeid";
  public static final String ENABLED = "enabled";
  public static final String ENCODEDPW = "encodedpw";
  public static final String ENDB = "endb";
  public static final String ENTERPRISEID = "enterpriseid";
  public static final String ENWEB = "enweb";
  public static final String FIRSTNAME = "firstname";
  public static final String LASTNAME = "lastname";
  public static final String LOGINNAME = "loginname";
  public static final String MIDDLEINITIAL = "middleinitial";

  public void loadFromProps() {
    EasyProperties myProps = this.getProps();
    browselines = myProps.getInt(BROWSELINES);
    colorschemeid = myProps.getString(COLORSCHEMEID);
    enabled = myProps.getBoolean(ENABLED);
    firstname = myProps.getString(FIRSTNAME);
    lastname = myProps.getString(LASTNAME);
    middleinitial = myProps.getString(MIDDLEINITIAL);
    loginname = myProps.getString(LOGINNAME);
    encodedpw = myProps.getString(ENCODEDPW);
    eperms.canDB = myProps.getBoolean(ENDB);
    eperms.canWeb = myProps.getBoolean(ENWEB);
    entid = new Enterpriseid(myProps.getInt(ENTERPRISEID));
    // find your enterprise
    enterprise = EnterpriseHome.Get(entid);
    // notify your enterprise
    enterprise.linkAssociate(this);
    // get your store accesses?
    // +++
  }

  public void storeToProps() { // +++ test this!
    EasyProperties myProps = this.getProps();
    myProps.setInt(BROWSELINES, browselines);
    myProps.setString(COLORSCHEMEID, colorschemeid);
    myProps.setBoolean(ENABLED, enabled);
    myProps.setString(FIRSTNAME, firstname);
    myProps.setString(LASTNAME, lastname);
    myProps.setString(MIDDLEINITIAL, middleinitial);
    myProps.setString(LOGINNAME, loginname);
    myProps.setString(ENCODEDPW, encodedpw);
    myProps.setBoolean(ENDB, eperms.canDB);
    myProps.setBoolean(ENWEB, eperms.canWeb);
    myProps.getInt(ENTERPRISEID, entid.value());
  }

  /* package */ boolean cloneAndStore(Associate from) {
    eperms.from(from.eperms);
    enabled   = from.enabled;
    encodedpw = from.encodedpw;
    loginname = from.loginname;
    return store(); // rollback mods if it fails +++
  }


  public Associateid associateid() {
    return new Associateid(id().value());
  }

  public Enterprise enterprise = null;
  private Enterpriseid entid = null;

  public final EnterprisePermissions eperms = new EnterprisePermissions();
  public String loginname = "";
  public String encodedpw = "";
  public String firstname = "";
  public String lastname = "";
  public String middleinitial = "";
  public int browselines = 15;
  public String colorschemeid = "";
  public boolean enabled = false;

  // +++ need storepermissions !!!

  // lsat, first M.
  public String lastCommaFirstMiddle() {
    // converting 3 names/initials into one long one
    String mi = middle();
    return lastname +
        (StringX.NonTrivial(lastname) &&
         (StringX.NonTrivial(firstname) ||
          StringX.NonTrivial(mi)) ? ", " : "") + firstname + mi;
  }

  public String firstMiddleLast() {
    return firstname + middle() + lastname;
  }

  private String middle() {
    String mi = " ";
    if (StringX.NonTrivial(middleinitial)) {
      if (middleinitial.length() == 1) {
        mi = " " + middleinitial + ". ";
      }
      else {
        mi = " " + middleinitial + " ";
      }
    } else {
      //
    }
    return mi;
  }

  public boolean passes(String entered){
    return StringX.equalStrings(encodedpw, entered);
  }

  public static String DEFAULTCOLORS = ""; // set by the server on startup

  private static final int DEFAULTBROWSE = 15;
  private static final int ABSMIN = 2;
  public int browselines() {
    // browse something between 2 and N, default to 15
    int tmp = browselines;
    if (tmp < ABSMIN) {
      tmp = DEFAULTBROWSE;
    }
    return Math.max(tmp, ABSMIN);
  }

  public String toString() {
    return "" + associateid() + ":" + loginname + ":" + firstMiddleLast();
  }
}
