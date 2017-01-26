package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthSubmitResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

// +++ @@@ %%% Needs to NOT extend AuthResponse.  They don't have enough in common.
// +++ @@@ %%% Instead, have them both extend ANOTHER class which shares the common ResponseCode and other classes.

import net.paymate.data.AuthResponse;

public class AuthSubmitResponse extends AuthResponse {

  public AuthSubmitResponse() {
  }

}