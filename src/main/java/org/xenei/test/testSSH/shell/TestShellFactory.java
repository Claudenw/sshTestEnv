package org.xenei.test.testSSH.shell;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.SSHTestingEnvironment;
import org.xenei.test.testSSH.command.AbstractTestCommand;
import org.xenei.test.testSSH.command.TestCommandFactory;

import jline.console.ConsoleReader;

public class TestShellFactory implements Factory<Command> {
	
    private static final Logger LOG = LoggerFactory.getLogger( TestShellFactory.class );

    /**
	 * 
	 */
	private final TestCommandFactory testCommandFactory;
	private final String prompt;

	/**
	 * @param sshTestingEnvironment
	 */
	public TestShellFactory(TestCommandFactory testCommandFactory, String prompt) {
		this.testCommandFactory = testCommandFactory;
		this.prompt = prompt;
	}

	@Override
    public Command create() {
        return new ShellCommand(prompt);
    }

	public class ShellCommand implements Command, Runnable {
		
	    /**
		 * 
		 */
		//private final SSHTestingEnvironment sshTestingEnvironment;
		private final String prompt;
	

		/**
		 * @param sshTestingEnvironment
		 */
		ShellCommand(String prompt) {
			this.prompt = prompt;
		}

		private InputStream in;
	    protected OutputStream out;
	    protected OutputStream err;
	    protected ExitCallback callback;
	    private Thread thread;
	    private ConsoleReader reader;
	    private PrintWriter writer;

	    @Override
	    public void start(final Environment env) throws IOException {
	        thread = new Thread( this, ShellCommand.class.getName() );
	        thread.start();
	    }

	    @Override
	    public void destroy() throws Exception {
	        thread.interrupt();
	    }

	    @Override
	    public void setInputStream(final InputStream in) {
	        this.in = in;
	    }

	    @Override
	    public void setOutputStream(final OutputStream out) {
	        this.out = out;
	    }

	    @Override
	    public void setErrorStream(final OutputStream err) {
	        this.err = err;
	    }

	    @Override
	    public void setExitCallback(final ExitCallback callback) {
	        this.callback = callback;
	    }

	    @Override
	    public void run() {
	        try
	        {

	            reader = new ConsoleReader( in, new FilterOutputStream( out ) {
	                @Override
	                public void write(final int ii) throws IOException {
	                    super.write( ii );

	                    // workaround for MacOSX!! reset line after CR..
	                    if (SSHTestingEnvironment.IS_MAC_OSX && (ii == ConsoleReader.CR.toCharArray()[0]))
	                    {
	                        super.write( ConsoleReader.RESET_LINE );
	                    }
	                }
	            } );
	            reader.setPrompt( prompt );
	            writer = new PrintWriter( reader.getOutput() );

	            // output welcome banner on ssh session startup
	            writer.println( ShellCommand.class.getSimpleName() + " testing shell" );

	            writer.flush();

	            String line;
	            while ((line = reader.readLine()) != null)
	            {
	                out.write( (line + "\n").getBytes( StandardCharsets.UTF_8 ) );
	                handleUserInput( line.trim() );            
	            }
	            callback.onExit( 0 );
	        } catch (final InterruptedIOException ex)
	        {
	            callback.onExit( 1 );
	        } catch (final Exception ex)
	        {
	            LOG.error( "Error executing in Shell...", ex );
	            callback.onExit( 1 );
	        }
	    }

	    private void handleUserInput(final String line) throws InterruptedIOException {
	        final AbstractTestCommand command = testCommandFactory.createShellCommand( line );       
	        command.setErrorStream( err );
	        command.setOutputStream( out );
	        command.setInputStream( in );
	        command.setExitCallback( callback );
	        command.run();
	    }
	}
}