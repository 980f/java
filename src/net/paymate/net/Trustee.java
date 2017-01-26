package net.paymate.net;
/**
* Title:        $Source: /cvs/src/net/paymate/net/Trustee.java,v $
* Description:
* Copyright:    2000-2002 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Trustee.java,v 1.31 2003/07/27 05:35:12 mattm Exp $
* @todo: use free memory or somesuch as part of seed.
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

public class Trustee {
  static final protected Tracer dbg=new Tracer(Trustee.class);

  static final public com.sun.net.ssl.SSLContext makeContext(){
    return makeContext(new KeyStoreAxess());
  }

  static final public com.sun.net.ssl.SSLContext makeContext(KeyStoreAxess keyStore){
    com.sun.net.ssl.SSLContext context=null;

    com.sun.net.ssl.X509TrustManager    the_trust ;
    X509Certificate[]   trusted   ;
    com.sun.net.ssl.TrustManager[]      trusts    ;
    com.sun.net.ssl.X509KeyManager      X509km    ;
    X509Certificate     cert[]    ;
    com.sun.net.ssl.KeyManager []       kma       ;
    com.sun.net.ssl.TrustManagerFactory trust_man ;
    KeyStore            ks        ;
    com.sun.net.ssl.KeyManagerFactory   key_man   ;

    dbg.Enter("makeContext");
    try {

      dbg.mark("add a provider");
      Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
      dbg.logArray(dbg.VERBOSE, "Providers", java.security.Security.getProviders());

      dbg.mark("Get a context");
      context=com.sun.net.ssl.SSLContext.getInstance("SSL");//java.security.NoSuchAlgorithmException

      dbg.mark("Get TM Factory");
      trust_man = com.sun.net.ssl.TrustManagerFactory.getInstance("SunX509");//java.security.NoSuchAlgorithmException

      dbg.mark("Getting Keystore");
      ks = KeyStore.getInstance("JKS");//java.security.KeyStoreException
      dbg.VERBOSE("Keystore type is " + ks.getType());

      dbg.mark("Loading Keystore [file: " + keyStore.FileName() + ", pw: " + keyStore.Password() + "]");
      ks.load(new FileInputStream(keyStore.FileName()), keyStore.Password());// see below
      //java.io.FileNotFoundException,java.security.cert.CertificateException

      dbg.mark("Initializing the TrustManagerFactory");
      trust_man.init(ks);//java.security.KeyStoreException

      dbg.mark("Get a SunX509 Key Manager factory");
      key_man = com.sun.net.ssl.KeyManagerFactory.getInstance("SunX509");//java.security.NoSuchAlgorithmException
      dbg.VERBOSE("KeyManager algorithm is " + key_man.getAlgorithm());

      dbg.mark(" Initialize the Key Manager factory.");
      key_man.init(ks, keyStore.Password());//java.security.UnrecoverableKeyException
      kma = key_man.getKeyManagers();

      dbg.mark("Get all the trusts for this factory");
      trusts = trust_man.getTrustManagers();
      dbg.VERBOSE("Number of trusts is: " + trusts.length);
      the_trust = (com.sun.net.ssl.X509TrustManager)trusts[0];
      trusted = the_trust.getAcceptedIssuers();

      dbg.mark("Initialize the session context");
      for (int i=0; i<kma.length; i++){
        X509km = (com.sun.net.ssl.X509KeyManager) kma[i];
        cert = X509km.getCertificateChain(keyStore.Alias());//???always an emtpy string, what is it used for??
      }
      //net.paymate.util.timer.StopWatch sw = new net.paymate.util.timer.StopWatch(); // ---
      SecureRandom faster=SecureRandom.getInstance("SHA1PRNG");//java.security.NoSuchAlgorithmException
      faster.setSeed(0xF018CB7EF28A391FL*DateX.utcNow());//
      context.init(kma, trusts, faster);//java.security.KeyManagementException
      //sw.Stop(); // ---
      //dbg.Message("context.init(kma, trusts, ...); took " + DateX.millisToSecsPlus(sw.millis())  + " seconds."); // ---
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
//$Id: Trustee.java,v 1.31 2003/07/27 05:35:12 mattm Exp $
