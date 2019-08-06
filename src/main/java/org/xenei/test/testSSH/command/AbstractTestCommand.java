package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public abstract class AbstractTestCommand implements Command, Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger( AbstractTestCommand.class );
	
    /**
	 * 
	 */
	protected final SSHTestingEnvironment sshTestingEnvironment;
	protected final boolean closeAfterError;
    protected final boolean closeAfterCommand;
    protected final String command;
    private InputStream in;
    protected OutputStream out;
    protected OutputStream err;

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
    public AbstractTestCommand(SSHTestingEnvironment sshTestingEnvironment, final String command, final boolean closeAfterCommand, final boolean closeAfterError) {
        this.sshTestingEnvironment = sshTestingEnvironment;
		this.command = ValidateUtils.checkNotNullAndNotEmpty( command, "No command" );
        this.closeAfterCommand = closeAfterCommand;
        this.closeAfterError = closeAfterError;
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
            	AbstractTestCommand subCommand = sshTestingEnvironment.getCommandFactory().createCommand( cmds[i], ((i + 1) == cmds.length) );
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