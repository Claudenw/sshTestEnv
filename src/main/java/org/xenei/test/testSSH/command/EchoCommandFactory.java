package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.session.ServerSession;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class EchoCommandFactory extends AbstractCommandFactory {

	private Set<String> commands = new HashSet<String>();
	
	public EchoCommandFactory(SSHTestingEnvironment sshTestingEnvironment) {
		super(sshTestingEnvironment);		
	}
	
	public EchoCommandFactory(SSHTestingEnvironment sshTestingEnvironment, Configuration config) {
		super(sshTestingEnvironment);
		Iterator<String> keys = config.getKeys();
		while ( keys.hasNext())
		{
			commands.add(keys.next());
		}
	}

	@Override
	public boolean handles(String command) {
		return commands.isEmpty() || commands.contains(command);
	}

	@Override
	public AbstractTestCommand createCommand(String command, boolean closeAfterCommand) {
		EchoCommand cmd = new EchoCommand( sshTestingEnvironment.getCommandFactory(), command );
		cmd.setCloseAfterCommand( closeAfterCommand );
		return cmd;
	}
	
	@Override
	public void clearState(ServerSession session) {
		// do nothing
	}

	private static class EchoCommand extends AbstractTestCommand implements Command, Runnable {
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
	    public EchoCommand(TestCommandFactory testCommandFactory, final String command) {
	    	super( testCommandFactory, command );        
	    }

	    @Override
		protected boolean handleCommand() throws IOException {
	        
	        out.write( command.getBytes( StandardCharsets.UTF_8 ) );
	        out.write( '\n' );

	        return true;
	    }

	}
}
