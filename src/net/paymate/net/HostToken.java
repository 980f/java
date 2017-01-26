package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/HostToken.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 * @warn: these must be coordinated with pmclibs/TcpServerConfig.cpp
 */

public interface HostToken {
  String IPSpecKey   ="ip";
  String portKey     ="port";//separate param for slurm
  String creationTimeoutKey  ="creationTimeout";//slurm connectTimeout renamed on second release to this
  String nickNameKey = "nickName";
  String retriesKey  = "retries";//originally slurm only
}
//$Id: HostToken.java,v 1.2 2004/02/06 20:30:17 andyh Exp $
