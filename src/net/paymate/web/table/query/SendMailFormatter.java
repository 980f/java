package net.paymate.web.table.query;

import net.paymate.web.table.ArrayTableGen;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/table/query/SendMailFormatter.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.web.table.*;
import net.paymate.web.color.*;
import net.paymate.util.*;
import org.apache.ecs.*;
import javax.mail.Message;

public class SendMailFormatter
    extends ArrayTableGen {

  protected static final ErrorLogStream dbg = ErrorLogStream.getForClass(
      SendMailFormatter.class);

  protected static final HeaderDef defaultHeaders[] = {
      new HeaderDef(AlignType.RIGHT, "#"),
      new HeaderDef(AlignType.LEFT, "Time"),
      new HeaderDef(AlignType.LEFT, "Envelope"),
      new HeaderDef(AlignType.LEFT, "Content"),
  };

  public SendMailFormatter(String title, ColorScheme colors, HeaderDef headers[],
                           Message [ ] mailings, LocalTimeFormat ltf) {
    super(title, colors, null, headers, null);
    data = new String[mailings.length][4];
    for (int i = 0; i < data.length; i++) {
      Message mailing = mailings[i];
      if (mailing != null) {
        data[i][0] = String.valueOf(i);
        data[i][1] = "";
        try {
          data[i][1] = ltf.format(UTC.New(mailing.getSentDate().getTime()));
        } catch (Exception e) {
          // stub
        }
        try {
          data[i][2] = mailing.getSubject();
        } catch (Exception e) {
          // stub
        }
        data[i][3] = "";
        try {
          data[i][3] = mailing.getContent().toString();
        } catch (Exception e) {
          // stub
        }
      } else {
        data[i][0] = data[i][1] = "[not found]";
      }
    }
  }

  public static final Element output(String title, ColorScheme colors,
                                     HeaderDef headers[], Message [ ] mailings,
                                     LocalTimeFormat ltf) {
    return new SendMailFormatter(title, colors, headers, mailings, ltf);
  }

  public static final Element output(String title, ColorScheme colors, Message [ ] mailings,
                                     LocalTimeFormat ltf) {
    return output(title, colors, null, mailings, ltf);
  }

  public HeaderDef[] fabricateHeaders() {
    return defaultHeaders;
  }
}
