package org.xenei.test.testSSH.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.server.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class TestCommandFactory implements CommandFactory {

	static final Logger LOG = LoggerFactory.getLogger( TestCommandFactory.class );

	/**
	 * 
	 */
	private final SSHTestingEnvironment sshTestingEnvironment;

	/**
	 * @param sshTestingEnvironment
	 */
	public TestCommandFactory(SSHTestingEnvironment sshTestingEnvironment) {
		this.sshTestingEnvironment = sshTestingEnvironment;
	}

	private final List<String> executedCommands = new ArrayList<>();

    @Override
    public AbstractTestCommand createCommand(final String command) {
    	return createCommand( command, true );
    }

    public AbstractTestCommand createCommand(final String command, boolean closeAfterCommand) {
        LOG.debug( "Creating command: " + command );
        executedCommands.add( command );
        return new EchoCommand( this.sshTestingEnvironment, command, true, this.sshTestingEnvironment.isCloseAfterError() );
    }
    
    /**
     * create shell command.
     *
     * @param command
     *            command
     * @return
     */
    public AbstractTestCommand createShellCommand(final String command) {
        LOG.debug( "Creating shell command: " + command );
        executedCommands.add( command );
        return new EchoCommand( this.sshTestingEnvironment, command, false, this.sshTestingEnvironment.isCloseAfterError() );
    }

    public List<String> getExecutedCommands() {
        return executedCommands;
    }
}