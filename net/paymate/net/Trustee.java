package net.paymate.net;
/**
* Title:        Trustee
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Trustee.java,v 1.23 2001/07/19 01:06:51 mattm Exp $
*/

import net.paymate.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;
import javax.net.*;
import java.security.*;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.*;
import com.sun.net.ssl.*;

public class Trustee {
  static final protected Tracer dbg=new Tracer(Trustee.class.getName());

  static final public SSLContext makeContext(KeyStoreAccess keyStore){
    SSLContext context=null;

    X509TrustManager    the_trust ;
    X509Certificate[]   trusted   ;
    TrustManager[]      trusts    ;
    X509KeyManager      X509km    ;
    X509Certificate     cert[]    ;
    KeyManager []       kma       ;
    TrustManagerFactory trust_man ;
    KeyStore            ks        ;
    KeyManagerFactory   key_man   ;

    dbg.Enter("makeContext");
    try {

      dbg.mark("add a provider");
      Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
      dbg.logArray(dbg.VERBOSE, "Providers", java.security.Security.getProviders());

      dbg.mark("Get a context");
      context=SSLContext.getInstance("SSL");//java.security.NoSuchAlgorithmException

      dbg.mark("Get TM Factory");
      trust_man = TrustManagerFactory.getInstance("SunX509");//java.security.NoSuchAlgorithmException

      dbg.mark("Getting Keystore");
      ks = KeyStore.getInstance("JKS");//java.security.KeyStoreException
      dbg.VERBOSE("Keystore type is " + ks.getType());

      dbg.mark("Loading Keystore [file: " + keyStore.FileName() + ", pw: " + keyStore.Password() + "]");
      ks.load(new FileInputStream(keyStore.FileName()), keyStore.Password());// see below
      //java.io.FileNotFoundException,java.security.cert.CertificateException

      dbg.mark("Initializing the TrustManagerFactory");
      trust_man.init(ks);//java.security.KeyStoreException

      dbg.mark("Get a SunX509 Key Manager factory");
      key_man = KeyManagerFactory.getInstance("SunX509");//java.security.NoSuchAlgorithmException
      dbg.VERBOSE("KeyManager algorithm is " + key_man.getAlgorithm());

      dbg.mark(" Initialize the Key Manager factory.");
      key_man.init(ks, keyStore.Password());//java.security.UnrecoverableKeyException
      kma = key_man.getKeyManagers();

      dbg.mark("Get all the trusts for this factory");
      trusts = trust_man.getTrustManagers();
      dbg.VERBOSE("Number of trusts is: " + trusts.length);
      the_trust = (X509TrustManager)trusts[0];
      trusted = the_trust.getAcceptedIssuers();

      dbg.mark("Initialize the session context");
      for (int i=0; i<kma.length; i++){
        X509km = (X509KeyManager) kma[i];
        cert = X509km.getCertificateChain(keyStore.Alias);
      }
      //net.paymate.util.timer.StopWatch sw = new net.paymate.util.timer.StopWatch(); // ---
      SecureRandom faster=SecureRandom.getInstance("SHA1PRNG");//java.security.NoSuchAlgorithmException
      faster.setSeed(0xF018CB7EF28A391FL*Safe.utcNow());//
      context.init(kma, trusts, faster);//java.security.KeyManagementException
      //sw.Stop(); // ---
      //dbg.Message("context.init(kma, trusts, ...); took " + net.paymate.util.Safe.millisToSecsPlus(sw.millis())  + " seconds."); // ---
    }
    //+++ possible distinct catches are commented on in the code above.
    catch (Exception caught){
      //      dbg.ERROR("At milestone "+milestone+" - "+caught);
      dbg.Caught(caught);
    }
    finally {
      dbg.Exit();
      return context;
    }
  }



}
//$Id: Trustee.java,v 1.23 2001/07/19 01:06:51 mattm Exp $
