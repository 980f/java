package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/RufClient.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import net.paymate.util.*;
import net.paymate.io.*;
import net.paymate.lang.*;
import net.paymate.text.*;
import net.paymate.util.compress.tar.*;
import net.paymate.*;

import java.net.*;
import java.io.*;

public class RufClient {
  MultiHomedSocketFactory rufhosts;
  File chroot; //used for debug
  ErrorLogStream dbg;
  String id;
  TextList filenames;

  public RufClient(EasyCursor ezc) {
    dbg = ErrorLogStream.getForClass(RufClient.class);
    rufhosts = new MultiHomedSocketFactory();
    id = ezc.getString("applName");
    chroot = new File(ezc.getString("chroot", "/tmp")); //@todo put real default in here after debugged!
    ezc.push("server");
    rufhosts.Initialize(ezc, MultiHomedHost.class);
  }

  private boolean assureDirExists(File destFile) { //Move to FileX
    try {
      if(!destFile.exists()) {
        dbg.WARNING("attempting to make directory:"+ destFile.getPath());
        if(!destFile.mkdirs()) {
          dbg.ERROR("error making directory path:" + destFile.getPath());
          return false;
        }
      }
      dbg.WARNING("assured existence of:" + destFile.getPath());
      return true;

    } catch(Exception ex) {
      dbg.Caught("while assuring directory exists", ex);
      return false;
    }
  }

  private boolean extractEntry(File destDir, TarEntry entry, TarInputStream tarIn) throws IOException {
    try {
      File destFile = new File(destDir, entry.getName());
      String destName=destFile.getPath();
      if(entry.isDirectory()) {
        dbg.WARNING("directory entry:"+destName);
        return assureDirExists(destFile);
      } else {
        if(assureDirExists(new File(destFile.getParent()))) {
          dbg.WARNING("writing file:" + destName);
          FileOutputStream out = new FileOutputStream(destFile);
          tarIn.copyEntryContents(out);
          dbg.WARNING("wrote file:" + destName);
          out.close();
          filenames.add(destName);
//we'd like to set permissions here, but java didn't abstract that... one of the advantages of the C version.
          return true;
        } else {
          return false;
        }
      }
    } catch(Exception ex) {
      dbg.Caught("while extracting an entry", ex);
      return false;
    }
  }

  private boolean tarxvf(byte[] tarfile) {
    dbg.WARNING("untarring to " + chroot.getPath());
    ByteArrayInputStream raw=new ByteArrayInputStream(tarfile);
    TarInputStream tarfin = new TarInputStream(raw);
    tarfin.setDebug(true);
    TarEntry onefile;
    int bugcount=0;
    try {
      dbg.WARNING("starting with "+tarfin.available()+" bytes, raw:"+raw.available());
      while( (onefile = tarfin.getNextEntry()) != null) {
        dbg.WARNING("before extract have "+tarfin.available()+" bytes, raw:"+raw.available());
        extractEntry(chroot, onefile, tarfin);
        dbg.WARNING("still have "+tarfin.available()+" bytes, raw:"+raw.available());
        dbg.WARNING("Entries processed:"+ ++bugcount);
      }
      return true;
    } catch(Exception ex) {
      dbg.Caught("while getting next entry ", ex);
      return ex instanceof InvalidHeaderException; //ignoring these until we figure out why they happen.
    }
  }

  private boolean legacyRuf(InputStream fromhost, OutputStream tohost, String id) throws Exception {
    //@todo: see if there are enough ACK's below
    String urp = "0000000053\n" + Fstring.fill(id, 12, ' ') + "FEDBCA9801234567890123456789012345678912\nACKACKACKACKACKACKACKACKACK";
    dbg.WARNING("Sending login as " + id);
    tohost.write(urp.getBytes());
    //wait for "NOK"
    dbg.WARNING("waiting for OK.");
    MiniLexer nokker = MiniLexer.lookerFor("NOK");
    for(int token = 0; (token = fromhost.read()) >= 0; ) {
      if(nokker.match( (char) token)) {
        break;
      }
    }
    int tarlen = 0;
    dbg.WARNING("waiting for size.");

    for(int digits = 10; digits-- > 0; ) { //server sends exactly 10 decimal digits.
      int token = fromhost.read();
      if(token >= 0) {
        tarlen *= 10;
        tarlen += token - 48;
      } else {
        dbg.WARNING("Server bailed on me.");

        return false;
      }
    }
    if(tarlen > 0) {
      dbg.WARNING("getting tarfile:" + tarlen);
      byte[] tarfile = new byte[tarlen];
      //now we have to wait for all the bytes to come in, more than one mtu is required.
      int got = 0;
      while(got < tarlen) { //for as many attempts as it takes
        got += fromhost.read(tarfile, got, tarfile.length - got);
      }
      //we now have a tarimage in an array of bytes, how do we unpack it?
      return tarxvf(tarfile); //fail if we don't get a whole tar image
    } else {
      dbg.WARNING("nothing to receive.");
      return true;
    }
  }

  private boolean run1(MultiHomedHost server) {
    if(server != null) {
      Socket sock = null;
      InputStream reader = null;
      OutputStream writer = null;
      try {
        sock = server.open(server.creationTimeoutMs, true);
        if(sock!=null){
          reader = sock.getInputStream();
          writer = sock.getOutputStream();
          return legacyRuf(reader, writer, id);
        }
      } catch(Exception ex) {
        dbg.Caught(ex);
        return false;
      } finally {
        IOX.Close(reader);
        SocketX.Close(sock);
      }
    }
    return false;
  }

  public TextList run() {
    MultiHomedHost server;
    int trials = 5; ///may go forever once it works.
    while(trials-- > 0) {
      server = rufhosts.currentHost();
      if(filenames==null){
        filenames=new TextList();
      } else {
        filenames.clear();
      }
      if(run1(server)) {
        break;
      } else {
        rufhosts.thisFailed(server);
      }
    }
    return filenames;
  }

////retain this 'main' in client for manual system upgrades during development.
  public static void main(String[] args) {
    /*Main app=*/Main.cli(RufClient.class, args);
    RufClient ruffer = new RufClient(EasyCursor.FromDisk("ruf.properties").push("ruf"));
//override logcontrol.properties:
    LogSwitch.SetAll(LogSwitch.WARNING);
//local server:

    ruffer.run();
  }

}
//$Id: RufClient.java,v 1.4 2004/03/02 22:42:42 andyh Exp $