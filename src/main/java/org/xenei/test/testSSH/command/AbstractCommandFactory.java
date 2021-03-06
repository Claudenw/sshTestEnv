package org.xenei.test.testSSH.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.server.session.ServerSession;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public abstract class AbstractCommandFactory  {

	/**
	 * 
	 */
	protected final SSHTestingEnvironment sshTestingEnvironment;
	private final List<String> executedCommands = new ArrayList<>();

	/**
	 * @param sshTestingEnvironment
	 */
	public AbstractCommandFactory(SSHTestingEnvironment sshTestingEnvironment) {
		this.sshTestingEnvironment = sshTestingEnvironment;
	}

	/**
	 * Return true if this factory can handle the command
	 * @param command the comand to handle.
	 * @return true if this factory handles the command.
	 */
	public abstract boolean handles( String command );

	/**
	 * Create the command.
	 * @param command the command to create.
	 * @param closeAfterCommand true if the connection should be closed after the command.
	 * @return an AbstractTestCommand instance that handles the command.
	 */
	public abstract AbstractTestCommand createCommand(final String command, boolean closeAfterCommand);

	/**
	 * Clear any state held by this factory.
	 */
	public abstract void clearState(ServerSession session);
	
	/**
	 * Get the list of executed commands.
	 * @return
	 */
	public List<String> getExecutedCommands() {
		return executedCommands;
	}
	
	/**
	 * Clear the list of executed commands.
	 */
	public void clearExecutedCommands() {
		executedCommands.clear();
	}
	
}