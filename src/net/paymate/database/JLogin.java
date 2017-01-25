package net.paymate.database;

/*
Download Instructions
http://examples.informix.com/doc/case_studies/connectivity/download_jlogin.html

Instructions for Downloading & Running JLogin

Follow these instructions for downloading the JLogin utility

If you have the Informix JDBC Driver installed on your computer:
1. Set your CLASSPATH environment variable so that it includes the full,
absolute pathname of jlogin.class and ifxjdbc.jar.
For example: set CLASSPATH D:\jlogin\ifxjdbc.jar;%CLASSPATH%
4. To run JLogin as a stand-alone application,
enter the following on the command line: java net.paymate.database.JLogin
5. If you run JLogin as an applet,
the archive containing the Informix JDBC Driver (ifxjdbc.jar)
must be available to the browser's class loader on the client host.
You can do this by following either of these two procedures:
Specify an ARCHIVE attribute in the APPLET tag to locate ifxjdbc.jar on the web server.
This will allow the driver to be downloaded over the network, as needed.
(a) Install ifxjdbc.jar on each client host and
(b) set the CLASSPATH environment variable on the client
to point to the location of ifxjdbc.jar before launching the browser.

Note: You can easily change the default properties that JLogin displays
by editing the JLogin.java source file and recompiling it.
*/


import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.sql.*;
import java.util.*;

public class JLogin extends Applet {

  public void init() 	{
    /**
     * If you run JLogin as an applet, make sure you change the initial values of
     * hostNameFLD and ipAddrFLD to the appropriate values of the machine that will
     * be hosting the web server that delivers the HTML page containing this applet.
     * Then recompile JLogin.java.
     *
     * Whether JLogin runs as an applet or a stand-alone application, make sure that
     * CLASSPATH on the client host points to the ifxjdbc.jar archive containing
     * the Informix JDBC driver.
     */

    //{{INIT_CONTROLS
    setLayout(null);
    setSize(487,662);
    setForeground(new Color(0));
    setBackground(new Color(4210752));
    titleLBL = new java.awt.Label("Ping Database Environment",Label.CENTER);
    titleLBL.setBounds(24,12,432,40);
    titleLBL.setFont(new Font("Serif", Font.BOLD, 21));
    titleLBL.setForeground(new Color(12632256));
    titleLBL.setBackground(new Color(4210752));
    add(titleLBL);
    jdbcProtocolLBL = new java.awt.Label("JDBC Protocol",Label.RIGHT);
    jdbcProtocolLBL.setBounds(24,60,96,24);
    jdbcProtocolLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    jdbcProtocolLBL.setForeground(new Color(12632256));
    add(jdbcProtocolLBL);
    jdbcProtocolFLD = new java.awt.TextField();
    jdbcProtocolFLD.setEditable(false);
    jdbcProtocolFLD.setText("jdbc");
    jdbcProtocolFLD.setBounds(132,60,108,32);
    jdbcProtocolFLD.setBackground(new Color(8421504));
    add(jdbcProtocolFLD);
    subprotocolLBL = new java.awt.Label("Subprotocol",Label.RIGHT);
    subprotocolLBL.setBounds(240,60,96,28);
    subprotocolLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    subprotocolLBL.setForeground(new Color(12632256));
    add(subprotocolLBL);
    subprotocolFLD = new java.awt.TextField();
    subprotocolFLD.setEditable(false);
    subprotocolFLD.setText("informix-sqli");
    subprotocolFLD.setBounds(348,60,108,32);
    subprotocolFLD.setBackground(new Color(8421504));
    add(subprotocolFLD);
    hostIdLBL = new java.awt.Label("Host",Label.RIGHT);
    hostIdLBL.setBounds(48,132,72,24);
    hostIdLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    hostIdLBL.setForeground(new Color(12632256));
    add(hostIdLBL);
    hostMachinePAN = new java.awt.Panel();
    hostMachinePAN.setLayout(null);
    hostMachinePAN.setBounds(120,108,348,84);
    add(hostMachinePAN);
    hostIdChoiceLBL = new java.awt.Label("Enter IP address or name of host machine:");
    hostIdChoiceLBL.setBounds(12,0,312,28);
    hostIdChoiceLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    hostIdChoiceLBL.setForeground(new Color(12632256));
    hostMachinePAN.add(hostIdChoiceLBL);
    hostIdGRP = new CheckboxGroup();
    hostNameBTN = new java.awt.Checkbox("Name", hostIdGRP, true);
    hostNameBTN.setBounds(12,24,100,24);
    hostNameBTN.setForeground(new Color(16777215));
    hostMachinePAN.add(hostNameBTN);
    hostNameFLD = new java.awt.TextField();
    hostNameFLD.setEditable(false);
    hostNameFLD.setText("byexample");
    hostNameFLD.setBounds(12,48,132,32);
    hostNameFLD.setBackground(new Color(8421504));
    hostMachinePAN.add(hostNameFLD);
    ipAddrBTN = new java.awt.Checkbox("IP Address", hostIdGRP, false);
    ipAddrBTN.setBounds(204,24,100,24);
    ipAddrBTN.setForeground(new Color(16777215));
    ipAddrBTN.setBackground(new Color(4210752));
    hostMachinePAN.add(ipAddrBTN);
    ipAddrFLD = new java.awt.TextField();
    ipAddrFLD.setEditable(false);
    ipAddrFLD.setText("158.58.10.244");
    ipAddrFLD.setBounds(204,48,132,32);
    ipAddrFLD.setBackground(new Color(8421504));
    hostMachinePAN.add(ipAddrFLD);
    portLBL = new java.awt.Label("TCP/IP Port",Label.RIGHT);
    portLBL.setBounds(36,204,84,24);
    portLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    portLBL.setForeground(new Color(12632256));
    add(portLBL);
    portFLD = new java.awt.TextField();
    portFLD.setText("1533");
    portFLD.setBounds(132,204,132,32);
    portFLD.setForeground(new Color(0));
    portFLD.setBackground(new Color(16777215));
    add(portFLD);
    userLBL = new java.awt.Label("User Name",Label.RIGHT);
    userLBL.setBounds(36,252,84,24);
    userLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    userLBL.setForeground(new Color(12632256));
    add(userLBL);
    userFLD = new java.awt.TextField();
    userFLD.setText("iusr");
    userFLD.setBounds(132,252,132,32);
    userFLD.setBackground(new Color(16777215));
    add(userFLD);
    passwdLBL = new java.awt.Label("Password",Label.RIGHT);
    passwdLBL.setBounds(36,300,84,20);
    passwdLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    passwdLBL.setForeground(new Color(12632256));
    add(passwdLBL);
    passwdFLD = new java.awt.TextField();
    passwdFLD.setEchoChar('*');
    passwdFLD.setText("aBc/123");
    passwdFLD.setBounds(132,300,132,32);
    passwdFLD.setBackground(new Color(16777215));
    add(passwdFLD);
    serverNameLBL = new java.awt.Label("Server Name",Label.RIGHT);
    serverNameLBL.setBounds(24,348,96,30);
    serverNameLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    serverNameLBL.setForeground(new Color(12632256));
    add(serverNameLBL);
    serverNameFLD = new java.awt.TextField();
    serverNameFLD.setText("beautynet_us914");
    serverNameFLD.setBounds(132,348,132,32);
    serverNameFLD.setBackground(new Color(16777215));
    add(serverNameFLD);
    databaseNameLBL = new java.awt.Label("Database",Label.RIGHT);
    databaseNameLBL.setBounds(48,408,72,26);
    databaseNameLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    databaseNameLBL.setForeground(new Color(12632256));
    add(databaseNameLBL);
    databaseNameFLD = new java.awt.TextField();
    databaseNameFLD.setText("stores7");
    databaseNameFLD.setBounds(132,408,132,32);
    databaseNameFLD.setBackground(new Color(16777215));
    add(databaseNameFLD);
    pingServerBTN = new java.awt.Button();
    pingServerBTN.setLabel("Ping DB server");
    pingServerBTN.setBounds(324,348,132,32);
    pingServerBTN.setFont(new Font("Dialog", Font.BOLD, 12));
    pingServerBTN.setForeground(new Color(12632256));
    pingServerBTN.setBackground(new Color(0));
    add(pingServerBTN);
    statusLBL = new java.awt.Label("Status:");
    statusLBL.setBounds(36,468,60,18);
    statusLBL.setFont(new Font("Dialog", Font.BOLD, 12));
    statusLBL.setForeground(new Color(12632256));
    add(statusLBL);
    statusLST = new java.awt.List(4);
    statusLST.add("In the above boxes, provide the parameters that you want to test,");
    statusLST.add("then click one of the \"Ping\" buttons.");
    statusLST.add("");
    add(statusLST);
    statusLST.setBounds(36,492,420,84);
    statusLST.setFont(new Font("Dialog", Font.PLAIN, 12));
    statusLST.setForeground(new Color(255));
    statusLST.setBackground(new Color(12632256));
    closeBTN = new java.awt.Button();
    closeBTN.setLabel("Close");
    closeBTN.setBounds(396,600,60,32);
    closeBTN.setFont(new Font("Dialog", Font.BOLD, 12));
    closeBTN.setForeground(new Color(12632256));
    closeBTN.setBackground(new Color(0));
    add(closeBTN);
    pingDatabaseBTN = new java.awt.Button();
    pingDatabaseBTN.setLabel("Ping database");
    pingDatabaseBTN.setBounds(324,408,132,32);
    pingDatabaseBTN.setFont(new Font("Dialog", Font.BOLD, 12));
    pingDatabaseBTN.setForeground(new Color(12632256));
    pingDatabaseBTN.setBackground(new Color(0));
    add(pingDatabaseBTN);
    //}}

    if (JLogin.objForm.equals( "application" )) 		{
      hostNameFLD.setBackground(new Color(16777215));
      ipAddrFLD.setBackground  (new Color(16777215));
      hostNameFLD.setEditable  (true);
      ipAddrFLD.setEditable    (true);
    }

    //{{REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    pingServerBTN.addActionListener(lSymAction);
    pingDatabaseBTN.addActionListener(lSymAction);
    statusLST.addActionListener(lSymAction);
    closeBTN.addActionListener(lSymAction);

    SymItem lSymItem = new SymItem();
    hostNameBTN.addItemListener(lSymItem);
    ipAddrBTN.addItemListener(lSymItem);

    SymText lSymText = new SymText();
    hostNameFLD.addTextListener(lSymText);
    ipAddrFLD.addTextListener(lSymText);
    //}}
  }

  //{{DECLARE_CONTROLS
  java.awt.Label titleLBL;
  java.awt.Label jdbcProtocolLBL;
  java.awt.TextField jdbcProtocolFLD;
  java.awt.Label subprotocolLBL;
  java.awt.TextField subprotocolFLD;
  java.awt.Label hostIdLBL;
  java.awt.Panel hostMachinePAN;
  java.awt.Label hostIdChoiceLBL;
  java.awt.Checkbox hostNameBTN;
  CheckboxGroup hostIdGRP;
  java.awt.TextField hostNameFLD;
  java.awt.Checkbox ipAddrBTN;
  java.awt.TextField ipAddrFLD;
  java.awt.Label portLBL;
  java.awt.TextField portFLD;
  java.awt.Label userLBL;
  java.awt.TextField userFLD;
  java.awt.Label passwdLBL;
  java.awt.TextField passwdFLD;
  java.awt.Label serverNameLBL;
  java.awt.TextField serverNameFLD;
  java.awt.Label databaseNameLBL;
  java.awt.TextField databaseNameFLD;
  java.awt.Button pingServerBTN;
  java.awt.Label statusLBL;
  java.awt.List statusLST;
  java.awt.Button closeBTN;
  java.awt.Button pingDatabaseBTN;
  //}}

  String hostID = "byexample";
  static String objForm = "applet";


  class SymAction implements java.awt.event.ActionListener 	{
    public void actionPerformed(java.awt.event.ActionEvent event) 		{
      Object object = event.getSource();
      if (object == pingServerBTN) {
        pingServerBTN_ActionPerformed(event);
      } else if (object == statusLST) {
        statusLST_DblClicked(event);
      } else if (object == closeBTN) {
        clostBTN_ActionPerformed(event);
      } else if (object == pingDatabaseBTN) {
        pingDatabaseBTN_ActionPerformed(event);
      }
    }
  }


  void pingServerBTN_ActionPerformed(java.awt.event.ActionEvent event) {
    ping("server");
  }


  void pingDatabaseBTN_ActionPerformed(java.awt.event.ActionEvent event) {
    ping("database");
  }


  void ping(String target) {
    String targetName    = null;
    String datasourceURL = getDatabaseURL();      // Get JDBC URL.

    if ( target.equals("server") ) {
      targetName = serverNameFLD.getText();
    } else if ( target.equals("database") ) {
      targetName    = databaseNameFLD.getText();
      datasourceURL = datasourceURL.concat( "/" + targetName.trim() );
    }

    statusLST.add("JDBC URL to ping " + target.trim() + " '" + targetName.trim() + "' is:");

    statusLST.add("   " + datasourceURL + ":" + "INFORMIXSERVER=" + serverNameFLD.getText()
        + ";" + "user=" + userFLD.getText() + ";" + "password=" + passwdFLD.getText());

    statusLST.add("Connecting to " + target + "...");

    String driverClassName = getDriverClassName();  // Get JDBC driver class name.
    Properties properties  = getProperties( false );// Get other parameters the connection requires.

    try	{ // This ensures that the driver has been loaded and registered.
      DriverManager.registerDriver( (Driver) Class.forName(driverClassName).newInstance() );
    } catch (ClassNotFoundException exception) {
      String msg = "Configuration error. Class " + driverClassName + " not found.";
      statusLST.add(msg);
      throw new RuntimeException(msg);
    } catch( InstantiationException exception ) {
      statusLST.add("Failed to create a driver instance." + exception);
    } catch( IllegalAccessException exception ) {
      statusLST.add("Failed to register the driver." + exception);
    } catch( SQLException exception ) {
      statusLST.add("SQL error code = " + exception.getErrorCode());
      statusLST.add("SQL error message: " + exception.getMessage());
      statusLST.add("Failed to connect. " + "SQLException: " + exception);
    }

    Connection newConnection;
    try {
      newConnection = DriverManager.getConnection(datasourceURL, properties);
    } catch (SQLException exception) {
      String msg = "Error occurred while connecting to " + target + ": " + exception.getMessage();
      statusLST.add(msg);
      throw new RuntimeException(msg);
    }

    statusLST.add("   ...connected.");
    statusLST.add("Disconnecting from " + target + "...");

    try {
      newConnection.close();
    } catch (SQLException exception) {
      String msg = "Error occurred while disconnecting from " + target + ": " + exception.getMessage();
      statusLST.add(msg);
      throw new RuntimeException(msg);
    }
    statusLST.add("   ...disconnected.");
  }


  // Identify the data source URL.
  public String getDatabaseURL() {
    String dataSourceURL = jdbcProtocolFLD.getText() + ":"   + subprotocolFLD.getText() + "://" + hostID + ":" + portFLD.getText();
    return dataSourceURL;
    //returns something like this -- "jdbc:informix-sqli://158.58.10.244:1533"
  }

  public String getDriverClassName() {
    return "com.informix.jdbc.IfxDriver";
  }


  // getProperties() identifies other parameters the server or database requires.
  public Properties getProperties( boolean database ) {
    Properties properties = new Properties();
    properties.put( /*"iusr"*/ "user", userFLD.getText());
    properties.put( /*"aBc/123"*/ "password", passwdFLD.getText());
    properties.put( /*"beautynet_us914"*/ "INFORMIXSERVER", serverNameFLD.getText());
    if ( database == true ) {
      properties.put( /*"stores7"*/ "database", databaseNameFLD.getText());
    }
    return properties;
  }


  void statusLST_DblClicked(java.awt.event.ActionEvent event) {
    // Clear the List
    statusLST.removeAll();
  }


  class SymItem implements java.awt.event.ItemListener {
    public void itemStateChanged(java.awt.event.ItemEvent event) {
      Object object = event.getSource();
      if (object == hostNameBTN) {
        hostNameBTN_ItemStateChanged(event);
      } else if (object == ipAddrBTN) {
        ipAddrBTN_ItemStateChanged(event);
      }
    }
  }

  void hostNameBTN_ItemStateChanged(java.awt.event.ItemEvent event) {
    if ( event.getStateChange() == ItemEvent.DESELECTED ) {
      return;
    } else {
      hostID = hostNameFLD.getText();
    }
  }

  void ipAddrBTN_ItemStateChanged(java.awt.event.ItemEvent event) {
    if ( event.getStateChange() == ItemEvent.DESELECTED ) {
      return;
    } else {
      hostID = ipAddrFLD.getText();
    }
  }


  void clostBTN_ActionPerformed(java.awt.event.ActionEvent event) {
    // Exit...in case this program runs as a stand-alone application
    System.exit(0);
  }


  static final void setObjForm( String form ) {
    JLogin.objForm = form;
  }


  class SymText implements java.awt.event.TextListener {
    public void textValueChanged(java.awt.event.TextEvent event) {
      Object object = event.getSource();
      if (object == hostNameFLD) {
        hostNameFLD_TextValueChanged(event);
      } else if (object == ipAddrFLD) {
        ipAddrFLD_TextValueChanged(event);
      }
    }
  }

  void hostNameFLD_TextValueChanged(java.awt.event.TextEvent event) {
    hostID = hostNameFLD.getText();
    hostIdGRP.setSelectedCheckbox( hostNameBTN );
  }

  void ipAddrFLD_TextValueChanged(java.awt.event.TextEvent event) {
    hostID = ipAddrFLD.getText();
    hostIdGRP.setSelectedCheckbox( ipAddrBTN );
  }


    //This "main" method is required for this applet to run as a standalone program.
  public static final void main ( String args[] ) {
    JLogin JLoginApplet = new JLogin ();
    Frame fm = new Frame( "Database Environment Ping" );
    fm.addNotify ();
    fm.add ( JLoginApplet );

    JLogin.setObjForm( "application" );
    JLoginApplet.init();
    fm.pack();
    fm.show();

    JLoginApplet.start();
  }
}
