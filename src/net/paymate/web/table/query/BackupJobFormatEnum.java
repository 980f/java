// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/BackupJobFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class BackupJobFormatEnum extends TrueEnum {
  public final static int NumberCol   =0;
  public final static int StartTimeCol=1;
  public final static int TableNameCol=2;
  public final static int StartedByCol=3;
  public final static int PriorityCol =4;
  public final static int SleepCol    =5;
  public final static int StatusCol   =6;
  public final static int DurationCol =7;
  public final static int WidthCol    =8;
  public final static int RowCountCol =9;
  public final static int BytesCol    =10;
  public final static int FileSizeCol =11;
  public final static int PercentCol  =12;
  public final static int FilenameCol =13;
  public final static int MessageCol  =14;

  public int numValues(){ return 15; }
  private static final TextList myText = TrueEnum.nameVector(BackupJobFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final BackupJobFormatEnum Prop=new BackupJobFormatEnum();
  public BackupJobFormatEnum(){
    super();
  }
  public BackupJobFormatEnum(int rawValue){
    super(rawValue);
  }
  public BackupJobFormatEnum(String textValue){
    super(textValue);
  }
  public BackupJobFormatEnum(BackupJobFormatEnum rhs){
    this(rhs.Value());
  }
  public BackupJobFormatEnum setto(BackupJobFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
