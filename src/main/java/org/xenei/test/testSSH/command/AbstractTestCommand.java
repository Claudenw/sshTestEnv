package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestCommand implements Command, Runnable, SessionAware {
	
	private static final Logger LOG = LoggerFactory.getLogger( AbstractTestCommand.class );
	
    /**
	 * 
	 */
	protected final TestCommandFactory testCommandFactory;
	protected boolean closeAfterError;
    protected boolean closeAfterCommand;
    protected final String command;
    private InputStream in;
    protected OutputStream out;
    protected OutputStream err;
    protected ServerSession session;

    /**
     * Test Command.
     *
     * @param command
     *            you know, for kids
     * @param closeAfterCommand
     *            close after command
     * @param closeAfterError
     *            close after error
     * @param sshTestingEnvironment TODO
     */
    public AbstractTestCommand(TestCommandFactory testCommandFactory, final String command) {
        this.testCommandFactory = testCommandFactory;
		//this.command = ValidateUtils.checkNotNullAndNotEmpty( command, "No command" );
        this.command = command;
        this.closeAfterCommand = false;
        this.closeAfterError = false;
    }
    

    @Override
	public void setSession(ServerSession session) {
		this.session = session;
	}


	public final void setCloseAfterError(boolean closeAfterError) {
		this.closeAfterError = closeAfterError;
	}


	public final void setCloseAfterCommand(boolean closeAfterCommand) {
		this.closeAfterCommand = closeAfterCommand;
	}
	
	protected Map<String,Object> getContext() {
		@SuppressWarnings("unchecked")
		Map<String,Object> ctxt = (Map<String, Object>) (session.getProperties().get( this.getClass().getName()));
		if (ctxt == null)
		{
			ctxt = new HashMap<String,Object>();
			session.getProperties().put( this.getClass().getName(), ctxt );
		}
		return ctxt;
	}
	
	protected void clearContext() {
		session.getProperties().remove( this.getClass().getName());
		session.getProperties().remove( "_prompt" );
	}

	public final String getCommand() {
        return command;
    }

    @Override
    public final void setInputStream(final InputStream in) {
        this.in = in;
    }

    @Override
    public final void setOutputStream(final OutputStream out) {
        this.out = out;
    }

    @Override
    public final void setErrorStream(final OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(final ExitCallback callback) {
    }

    /**
     * Return true if the command succeeded, false if there was an error.
     * @return true on success.
     * @throws IOException
     */
    abstract protected boolean handleCommand() throws IOException;
    
    @Override
    public final void start(final Environment env) {
        new Thread( this ).start();
    }

    @Override
    public void run() {

        ValidateUtils.checkNotNull( err, "No error stream" );
        ValidateUtils.checkNotNull( out, "No error stream" );

        final String[] cmds = command.split( "\n" );
        if (cmds.length == 1)
        {
            try
            {
                LOG.debug( "Executing " + command );
                if (!handleCommand())
                {
                    handleErrCmd();
                    if (closeAfterError)
                    {
                        IOUtils.closeQuietly( out );
                        IOUtils.closeQuietly( err );
                    }
                }
                if (closeAfterCommand)
                {
                    IOUtils.closeQuietly( err );
                    IOUtils.closeQuietly( out );
                }
            } catch (final IOException ex)
            {
                throw new RuntimeException( ex );
            }
        } else
        {
            LOG.debug( "Executing " + Arrays.toString( cmds ) );
            for (int i = 0; i < cmds.length; i++)
            {
            	AbstractTestCommand subCommand = testCommandFactory.createCommand( cmds[i], ((i + 1) == cmds.length) );
            	subCommand.setSession(session);
            	subCommand.setInputStream( in );
                subCommand.setErrorStream( err );
                subCommand.setOutputStream( out );
                LOG.debug( "Executing " + subCommand );
                subCommand.run();
            }
        }
        LOG.debug( "Finished " + command );
    }

    protected void handleErrCmd() throws IOException {                
    }

    @Override
    public void destroy() {
        // ignored
    }

    @Override
    public int hashCode() {
        return Objects.hashCode( getCommand() );
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AbstractTestCommand other = (AbstractTestCommand) obj;

        return Objects.equals( getCommand(), other.getCommand() );
    }

}