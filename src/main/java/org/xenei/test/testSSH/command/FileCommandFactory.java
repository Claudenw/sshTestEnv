package org.xenei.test.testSSH.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.session.ServerSession;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class FileCommandFactory extends AbstractCommandFactory {

	private Configuration cfg;
	
	public FileCommandFactory(SSHTestingEnvironment sshTestingEnvironment) {
		super(sshTestingEnvironment);		
	}
	
	public FileCommandFactory(SSHTestingEnvironment sshTestingEnvironment, Configuration config) {
		super(sshTestingEnvironment);
		this.cfg = config;
	}

	@Override
	public boolean handles(String command) {
		return cfg.containsKey(command);
	}

	@Override
	public AbstractTestCommand createCommand(String command, boolean closeAfterCommand) {
		FileCommand cmd = new FileCommand( sshTestingEnvironment.getCommandFactory(), command );
		cmd.setCloseAfterCommand( closeAfterCommand );
		return cmd;
	}
	
	@Override
	public void clearState(ServerSession session) {
		// do nothing
	}

	private class FileCommand extends AbstractTestCommand implements Command, Runnable {
		private File file;
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
	    public FileCommand(TestCommandFactory testCommandFactory, final String command) {
	    	super( testCommandFactory, command ); 
	    	file = new File( cfg.getString( command ));
	    	
	    }

	    @Override
		protected boolean handleCommand() throws IOException {
	        if (file.exists())
	        {
	        	try (InputStream in = new FileInputStream( file ))
	        	{
	        		IOUtils.copyLarge( in, out);
	        	}
	        	out.write( '\n' );;
		        return true;
	        }
	        throw new IOException( "Unable to read file "+file.getAbsolutePath());
	    }

	}
}
