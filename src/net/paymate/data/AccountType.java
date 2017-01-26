package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/AccountType.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

public class AccountType extends PayType {

  public final static AccountType Credit=new AccountType(PayType.Credit);
  public final static AccountType Debit= new AccountType(PayType.Debit);
  public final static AccountType GiftCard=   new AccountType(PayType.GiftCard);
  public final static AccountType Unknown= new AccountType(PayType.Unknown);

  private AccountType(int acti) {
    super(acti);
  }

  public static final AccountType fromPayType(PayType pt) {
    int val = pt.Value();
    switch(val) {
      case PayType.Credit: {
        return Credit;
      }
      case PayType.Debit: {
        return Debit;
      }
      case PayType.GiftCard: {
        return GiftCard;
      }
      case PayType.Unknown:
      default: {
        return Unknown;
      }
    }
  }
}
//$Id: AccountType.java,v 1.6 2003/10/25 20:34:18 mattm Exp $
