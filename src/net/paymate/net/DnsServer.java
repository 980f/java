package net.paymate.net;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
//import com.sun.jndi.dns.DnsContextFactory;

/**
 * A DNS lookup service.
 */
public class DnsServer {
  DirContext ctx;
  public DnsServer(List dnsServers) throws NamingException {
    Hashtable env = new Hashtable();
    env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
    Iterator it = dnsServers.iterator();
    String dnsUrl = "";
    int c = 0;
    while (it.hasNext()) {
      String s = (String)it.next();
      if (c != 0) {
        dnsUrl += " ";
      }
      dnsUrl += "dns://" + s + "/";
      c++;
    }
    env.put("java.naming.provider.url", dnsUrl);
    ctx = new InitialDirContext(env);
  }

  public List lookup(String name) throws NamingException {
    return lookup(name, "A");
  }
  public List lookup(String name, String type) throws NamingException {
    List rtn = new LinkedList();
    Attributes attrs = ctx.getAttributes(name, new String[]{type});
    Enumeration ennum = attrs.getAll();
    while (ennum.hasMoreElements()) {
      Attribute at = (Attribute)ennum.nextElement();
      Enumeration enum2 = at.getAll();
      while (enum2.hasMoreElements()) {
        String s = (String)enum2.nextElement();
        rtn.add(s);
      }
    }
    return rtn;
  }

  public List lookupMX(String name) throws NamingException {
    List recs = lookup(name, "MX");
    TreeMap sorter = new TreeMap();
    Iterator it = recs.iterator();
    while (it.hasNext()) {
      String s = (String)it.next();
      StringTokenizer st = new StringTokenizer(s, " ");
      Integer num = new Integer(st.nextToken());
      String value = st.nextToken();
      sorter.put(num, value);
    }
    it = sorter.keySet().iterator();
    List rtn = new LinkedList();
    while (it.hasNext()) {
      String machine = (String)sorter.get(it.next());
      rtn.add(machine);
    }
    return rtn;
  }

  public static void main(String[] args) throws NamingException {
    List servers = new LinkedList();
    servers.add("dns1.act.health.gov.au");
    servers.add("dns2.act.health.gov.au");
    DnsServer srv = new DnsServer(servers);
    List recs = srv.lookupMX("act.gov.au");
    System.out.println(recs);
  }
}
