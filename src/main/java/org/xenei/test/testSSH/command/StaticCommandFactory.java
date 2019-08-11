package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.configuration2.Configuration;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.session.ServerSession;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class StaticCommandFactory extends AbstractCommandFactory {

	private Configuration config;
	
	public StaticCommandFactory(SSHTestingEnvironment sshTestingEnvironment, Configuration config) {
		super(sshTestingEnvironment);
		this.config = config;
	}

	@Override
	public boolean handles(String command) {
		return config.containsKey(command);
	}

	@Override
	public AbstractTestCommand createCommand(String command, boolean closeAfterCommand) {
		StaticCommand cmd = new StaticCommand( sshTestingEnvironment.getCommandFactory(), command );
		cmd.setCloseAfterCommand( closeAfterCommand );
		return cmd;
	}

	@Override
	public void clearState(ServerSession session) {
		// do nothing
	}
	
	private class StaticCommand extends AbstractTestCommand implements Command, Runnable {	

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
	    public StaticCommand(TestCommandFactory testCommandFactory, final String command) {
	    	super( testCommandFactory, command );        
	    }

	    @Override
		protected boolean handleCommand() throws IOException {
	        
	        out.write( config.getString(command).getBytes( StandardCharsets.UTF_8 ) );
	        out.write( '\n' );
	        return true;
	    }

	}
}
