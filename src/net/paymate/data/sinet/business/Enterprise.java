package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/Enterprise.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.25 $
 */

import net.paymate.data.sinet.EntityBase;
import net.paymate.data.sinet.SinetClass;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.util.TextList;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.EasyProperties;

public class Enterprise extends EntityBase {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Enterprise.class);
  private static final SinetClass mySinetClass = new SinetClass(SinetClass.Enterprise);

  public void linkStore(Store store) {
    dbg.ERROR("linkStore("+store+")");
    stores.addStore(store);
  }

  public Enterpriseid enterpriseid() {
    return new Enterpriseid(id().value());
  }

  public Store newStore() {
    // create 1 store
    Store store = StoreHome.New(new Enterpriseid(id().value()));
    if(store != null) {
      stores.addStore(store);
    }
    return store;
  }

  public void linkAssociate(Associate associate) {
    dbg.ERROR("linkAssociate("+associate+")");
    associates.addAssociate(associate);
  }

  public Associate newAssociate() {
    // create 1 associate
    Associate associate = AssociateHome.New(new Enterpriseid(id().value()));
    if(associate != null) {
      associates.addAssociate(associate);
    }
    return associate;
  }

  public Associate cloneAssociate(Associate from) {
    // create it
    Associate newgawd = newAssociate();
    // edit and save it
    newgawd.cloneAndStore(from); // check return value?
    return newgawd;
  }

  public SinetClass getSinetClass() {
    return mySinetClass;
  }

  public static final String STATEDEFAULT = "TX";
  public static final String CITYDEFAULT = "Austin";
  public static final String COUNTRYDEFAULT = "US";
  public static final String ENABLEDDEFAULT = Bool.TRUE();

  // +++ put these into a TrueEnum?
  public static final String ENTERPRISE     = "enterprise";
  // fieldname constants - ALL MUST be lower case!
  public static final String ADDRESS1       = "address1";
  public static final String ADDRESS2       = "address2";
  public static final String CITY           = "city";
  public static final String COUNTRY        = "country";
  public static final String ENABLED        = "enabled";
  public static final String ENTERPRISEID   = "enterpriseid";
  public static final String ENTERPRISENAME = "enterprisename";
//  public static final String ENTERPRISETYPE = "enterprisetype";
  public static final String NOTES          = "notes";
  public static final String PHONE          = "phone";
  public static final String STATE          = "state";
  public static final String ZIPCODE        = "zipcode";

  // +++ at class load time, compare the TableProfile and the Entity class
  // +++ to see that they have the same fields?

  public void loadFromProps() {
    EasyProperties myprops = getProps();
    address1=myprops.getString(ADDRESS1);
    address2=myprops.getString(ADDRESS2);
    city=myprops.getString(CITY);
    country=myprops.getString(COUNTRY);
    enabled=myprops.getBoolean(ENABLED);
    enterprisename=myprops.getString(ENTERPRISENAME);
//    enterprisetype=myprops.getString(ENTERPRISETYPE);
    notes=myprops.getString(NOTES);
    phone=myprops.getString(PHONE);
    state=myprops.getString(STATE);
    zipcode=myprops.getString(ZIPCODE);
  }
  public void storeToProps() {
    EasyProperties myprops = getProps();
    myprops.setString(ADDRESS1, address1);
    myprops.setString(ADDRESS2, address2);
    myprops.setString(CITY, city);
    myprops.setString(COUNTRY, country);
    myprops.setBoolean(ENABLED, enabled);
    myprops.setString(ENTERPRISENAME, enterprisename);
//    myprops.setString(ENTERPRISETYPE, enterprisetype);
    myprops.setString(NOTES, notes);
    myprops.setString(PHONE, phone);
    myprops.setString(STATE, state);
    myprops.setString(ZIPCODE, zipcode);
    // don't save the stores out, as they are saved out on creation and edit.
    // You can't move a store between enterprises or delete it, only disable it,
    // so there are no issues here.
  }

  public String address1="";
  public String address2="";
  public String city="";
  public String country="";
  public boolean enabled=false;
  public String enterprisename="";
//  public String enterprisetype="";
  public String notes="";
  public String phone="";
  public String state="";
  public String zipcode="";

  public AssociateList associates = new AssociateList();
  public StoreList stores = new StoreList();

  public String toString() {
    return ""+id()+":"+enterprisename;
  }

}
