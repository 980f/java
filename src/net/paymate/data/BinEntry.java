package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/BinEntry.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.27 $
 */

import net.paymate.jpos.data.*;
import net.paymate.util.*;

import java.util.*;

public class BinEntry implements isEasy, Comparable {

  public Institution issuer;
  public AccountType act;
  public boolean expires=true;
  public boolean enMod10ck=true;
  public int low;
  public int high;

  /*private removed: shouldn't prevent me from making my own! (esp since the server will make them from the database)*/
  public BinEntry(int low,int high,Institution inst,AccountType act,boolean expires, boolean enMod10ck) {
    this.low=low;
    this.high=high;
    this.issuer=inst;
    this.act=act;
    this.expires=expires;
    this.enMod10ck=enMod10ck;
  }

  public boolean contains(int cardno){
    if(high==0){
      return low==cardno;
    } else {
      return low<=cardno && cardno <=high;
    }
//    return false;
  }

  private int compareTo(BinEntry rhs){
    if(rhs.high==0){
      if(this.high==0){ //both singlets
        return this.low-rhs.low;
      } else {
        return 0-1; //singlet always greater than range
      }
    } else {
      if(this.high==0){ //just this is a singlet
        return 1-0; //singlet always greater than range
      } else {
        return this.low-rhs.low; //this trusts that all ranges make sense.
      }
    }
  }

  /**
   * @throws CLassCastException
   */
  public int compareTo(Object o){
    return compareTo((BinEntry)o);
  }

  //////////////////////////////
  // isEasy
  //these must match CardTable for liberty4 validator:

  public final static String issuerKey="institution";//see dbconstants.ISSUER
  public final static String actKey="paytype";//also must be same as databse ///
  public final static String lowKey="lowbin";
  public final static String highKey="highbin";
  public final static String expiresKey="exp";
  public final static String enMod10ckKey="enmod10ck";

  public void save(EasyCursor ezp){
    ezp.setString(issuerKey,issuer.Abbreviation());
    ezp.saveEnum(actKey,act);
    ezp.setInt(lowKey,low);
    ezp.setInt(highKey,high);
    ezp.setBoolean(expiresKey,expires);
    ezp.setBoolean(enMod10ckKey,enMod10ck);
  }

  public void load(EasyCursor ezp){
    issuer= CardIssuer.getFrom2(ezp.getString(issuerKey));//hack
    ezp.loadEnum(actKey,act);
    low=ezp.getInt(lowKey,1000000);//make entry useless if don't have a low
    high=ezp.getInt(highKey,0); //singlets are indicated by high=0
    expires=ezp.getBoolean(expiresKey,true);
    enMod10ck=ezp.getBoolean(enMod10ckKey,!act.is(AccountType.Debit.Value())); // default true except for debit
  }

  //////////////////////////////

//  new BinEntry(int low,int high,Institution inst,AccountType act,boolean expires)
  static BinEntry unknown=null;//new BinEntry(0,0,IdIssuer.Unknown,AccountType.Unknown,true);

  public boolean isIssuedBy(Institution abank){
    return issuer!=null && issuer == abank;
  }

  public static boolean BinIssuedBy(BinEntry bin,Institution abank){
    return bin!=null && bin.isIssuedBy(abank);
  }

  /**
 * will be reverse iterated during search. put specials at end.
 * put embedded ranges after super range. +++ write code to do that all when the class loads!
 */
  public final static BinEntry guesser[]={//public for PaymateDB validator
    // gross ranges
    new BinEntry(180000,180099,CardIssuer.JCB,AccountType.Credit,true,true),
    new BinEntry(213100,213199,CardIssuer.JCB,AccountType.Credit,true,true),
    new BinEntry(300000,399999,CardIssuer.JCB,AccountType.Credit,true,true),
    new BinEntry(350000,359999,CardIssuer.JCB,AccountType.Credit,true,true),
    new BinEntry(308300,332999,CardIssuer.JCB,AccountType.Credit,true,true),
//contained by other    new BinEntry(352800,358999,CardIssuer.JCB,AccountType.Credit,true),

    new BinEntry(300000,305999,CardIssuer.DinersClub,AccountType.Credit,true,true),
    new BinEntry(360000,369999,CardIssuer.DinersClub,AccountType.Credit,true,true),
    new BinEntry(380000,389999,CardIssuer.DinersClub,AccountType.Credit,true,true),
    new BinEntry(340000,349999,CardIssuer.AmericanExpress,AccountType.Credit,true,true),
    new BinEntry(370000,379999,CardIssuer.AmericanExpress,AccountType.Credit,true,true),
    new BinEntry(393000,394999,CardIssuer.CarteBlanche,AccountType.Credit,true,true),  //cb

    new BinEntry(400000,499999,CardIssuer.Visa,AccountType.Credit,true,true),
//    new BinEntry(405501,405504,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(405550,405554,CardIssuer.Visa,AccountType.Credit,true),//pcII
    new BinEntry(410000,449999,CardIssuer.Visa,AccountType.Credit,true,true),//frequently rejected +_+
//    new BinEntry(415928,415928,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(424604,424605,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(427533,427533,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(428800,428899,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(443085,443085,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(448400,448699,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(471500,471699,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(480400,480499,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(485900,485999,CardIssuer.Visa,AccountType.Credit,true),//pcII
//    new BinEntry(486500,486699,CardIssuer.Visa,AccountType.Credit,true),//pcII

    new BinEntry(500000,509999,CardIssuer.MasterCard,AccountType.Debit,true,false),
    new BinEntry(510000,559999,CardIssuer.MasterCard,AccountType.Credit,true,true),
    new BinEntry(540500,540599,CardIssuer.MasterCard,AccountType.Credit,true,true),//pcII
    new BinEntry(555000,555999,CardIssuer.MasterCard,AccountType.Credit,true,true),//pcII
    new BinEntry(556000,556999,CardIssuer.MasterCard,AccountType.Credit,true,true), //Fleet 16
    new BinEntry(560000,599999,CardIssuer.MasterCard,AccountType.Debit,false,false),//includes alh's ATM, made expiration moot for his sake.

    new BinEntry(601100,601199,CardIssuer.Discover,AccountType.Credit,true,true),

//---don't take these until we find out who they belong to   new BinEntry(640000,649999,CardIssuer.Unknown,AccountType.Debit,true),
//   new BinEntry(636000,636999,CardIssuer.DriversLicenseBureaus,AccountType.Unknown,false),
//    new BinEntry(547206,548018,CardIssuer.MasterCard,AccountType.Credit,true), //corporate
//    new BinEntry(552500,553599,CardIssuer.MasterCard,AccountType.Credit,true), //corporate
//    new BinEntry(558000,558999,CardIssuer.MasterCard,AccountType.Credit,true), //corporate

//    new BinEntry(412345,0,CardIssuer.CardSystems,AccountType.Credit,true,true),
//    new BinEntry(401288,0,CardIssuer.Paymentech,AccountType.Credit,true),
//    new BinEntry(405501,0,CardIssuer.Paymentech,AccountType.Credit,true,true), //visa purchasing
//    new BinEntry(545454,0,CardIssuer.Paymentech,AccountType.Credit,true),

    new BinEntry(603571,0,CardIssuer.Paymentech,AccountType.GiftCard,false,true),
    new BinEntry(999999,0,CardIssuer.Paymentech,AccountType.Debit,false,false), //@todo try to remove this entry

//    new BinEntry(308136,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),  //private label card
//    new BinEntry(601580,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),  // Site 19
//    new BinEntry(690007,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),  //private label card, source data added digits '10'
//    new BinEntry(690046,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),  //WEX 19
//    new BinEntry(707685,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),// GasCard 17
//    new BinEntry(707943,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),// GasCard 17
//    new BinEntry(707649,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),// GasCard 17
//    new BinEntry(   0,999,IdIssuer.PrivateLabel,AccountType.Unknown,false),// GasCard 17
//    new BinEntry(708009,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),// GO! Systems 17
//    new BinEntry(708309,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),//(original data had defect,we hope) Best 19
//    new BinEntry(760300,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),// 76030050 PH&H USA 19
//    new BinEntry(744003,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),// PH&H Canada 16
//    new BinEntry(797282,0,IdIssuer.PrivateLabel,AccountType.Unknown,false),// Staples Office Supplies

//    new BinEntry(708885,708889,IdIssuer.PrivateLabel,AccountType.Unknown,false),// Voyager 19

  };

/*
gas cards

problems sorted by BIN

415896=0211 101 *
427178=0312 101 *
435603=0312 101 *
435689=0406 101 *
441716=0111 101 *
482086=0206 101 *

501841=4912 120
503706=0405 120
505334=0503 120
505730=0202 120
545454=0612 101  hypercom test card
566509=0203 120
568227=2007 120
571646=4912 120
575628=4912 120
581551=0207 120
581941=2512 120
583110=0612 120
583232=4912 120
583408=4912 000
583561=1512 100
584188=1904 120
584756=1911 120
585062=4912 799
587117=4912 120
587162=0712 120
587176=0712 120
588901=1908 120
589297=0907 120
601056=0001 000
640035=1908 120
641701=2012 120
676754=0307 101 *

*/

  static OrderedVector patches=OrderedVector.New(BinEntry.class); //each time server finds we got a card wrong we make a patch.

  public static void insertPatch(BinEntry patch){
    if(patch!=null){
      if(patch.high<=patch.low){// doing < makes defective instances livable.
        patch.high=0; //expedites compare
      }
      patches.insert(patch);//presently relies upon patches NOT being ranges.
    }
  }

  public static BinEntry Guess(MSRData card){
    return Guess(card.accountNumber.BinNumber());
  }

  public static BinEntry Guess(int cardno){
    //check patches first, they are repairs to internal table.
    //really need to merge the table into the patchlist. Requires a funky uniquer.
    for(int i=patches.length();i-->0;){
      BinEntry patch= (BinEntry)patches.itemAt(i);
      if(patch.contains(cardno)){
        return patch;
      }
    }

    for(int i=guesser.length;i-->0;){
      BinEntry possible= guesser[i];
      if(possible.contains(cardno)){
        return possible;
      }
    }
  //if we get here we know nothing about the bin...
    return unknown;
  }

  public String toSpam(){
    return issuer.FullName()+" "+act.Image()+ (expires?"":" never expires");
  }
  //////////////////////////////
  /**
   * testing card #'s
   *
   * +++ find some samples to use, put them in CODE, and the put this into a jUnit test suite!
   *
   *
   */
  public static final void main(String[] args) {
    if(args.length>0){
      MSRData mancard= new MSRData();
      StringBuffer track2=new StringBuffer(args[0]);
      if(args.length>1){
        track2.append('=');
        track2.append(args[1]);//expiration
        if(args.length>2){
          track2.append(args[2]);//service code
        }
      }
      mancard.setTrack(MSRData.T2,String.valueOf(track2));
      mancard.ParseTrack2();
      System.err.println(mancard.toSpam());//#in a main()
      BinEntry arf=BinEntry.Guess(mancard);
      System.err.println(arf!=null? arf.toSpam():"card not recognized");//#in a main()
    } else {
      System.err.println("Output analysis of card#");//#in a main()
      System.err.println("usage: cardnumber [expiration_as_yymm [servicecode]]");//#in a main()
    }
  }

}
//$Id: BinEntry.java,v 1.27 2004/02/03 19:53:55 mattm Exp $

