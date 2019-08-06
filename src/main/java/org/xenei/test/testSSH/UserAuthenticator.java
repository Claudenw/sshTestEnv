package org.xenei.test.testSSH;

import org.apache.commons.configuration2.Configuration;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

/**
 * A simple authenticator that accepts any password for the user.
 * The user is defined in the configuration under the key "id".
 *
 */
public class UserAuthenticator implements PasswordAuthenticator {

    private String id;

    /**
     * Constructor.
     * @param cfg the configuration file.
     */
    public UserAuthenticator(Configuration cfg) {
    	this.id = cfg.getString( "id" );
   
    }

    @Override
    public boolean authenticate(final String username, final String password, final ServerSession session)
            throws PasswordChangeRequiredException {
        return username.equals( id );
    }

    /**
     * Change the user id
     * @param id the ID to set it to.
     */
    public void setId(final String id) {
        this.id = id;
    }
}