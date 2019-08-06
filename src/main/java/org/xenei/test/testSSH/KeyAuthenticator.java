package org.xenei.test.testSSH;

import java.security.PublicKey;

import org.apache.commons.configuration2.Configuration;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class KeyAuthenticator extends AbstractLoggingBean implements PublickeyAuthenticator {

    private boolean accepted;

    public KeyAuthenticator(Configuration cfg) {
        this.accepted = cfg.getBoolean("accepted");
    }

    public final boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(final boolean state) {
        accepted = state;
    }

    @Override
    public final boolean authenticate(final String username, final PublicKey key, final ServerSession session) {
        final boolean accepted = isAccepted();
        if (accepted)
        {
            handleAcceptance( username, key, session );
        } else
        {
            handleRejection( username, key, session );
        }
        return accepted;
    }

    protected void handleAcceptance(final String username, final PublicKey key, final ServerSession session) {
        // accepting without really checking is dangerous, thus the warning
        log.warn( "authenticate({}[{}][{}][{}]: accepted without checking", username, session,
                (key == null) /* don't care about the key */ ? "null" : key.getAlgorithm(),
                KeyUtils.getFingerPrint( key ) );
    }

    protected void handleRejection(final String username, final PublicKey key, final ServerSession session) {
        if (log.isDebugEnabled())
        {
            log.debug( "authenticate({}[{}][{}][{}]: rejected", username, session,
                    (key == null) /* don't care about the key */ ? "null" : key.getAlgorithm(),
                    KeyUtils.getFingerPrint( key ) );
        }
    }

}