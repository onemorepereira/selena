package com.pushnpray.projectpi;

import java.math.BigInteger;
import java.security.SecureRandom;

/* random comment; forcing commit-build; watching Nexus artifact push from Jenkins */
public class Tokens {

	/* create a runtime token to use throughout our logger */
	public static String getNewID() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}

}
