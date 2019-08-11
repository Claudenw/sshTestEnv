package org.xenei.test.testSSH.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class ConfigCommandFactory extends AbstractCommandFactory implements CommandFactory {

	static final Logger LOG = LoggerFactory.getLogger(ConfigCommandFactory.class);

	private final Configuration cfg;

	/**
	 * @param sshTestingEnvironment
	 */
	public ConfigCommandFactory(SSHTestingEnvironment sshTestingEnvironment, Configuration config) {
		super( sshTestingEnvironment);
		this.cfg = config;
	}

	private final List<String> executedCommands = new ArrayList<>();

	
	private AbstractTestCommand instantiate(Configuration cfg, String command)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> clazz = Class.forName(cfg.getString("class"));
		if (AbstractTestCommand.class.isAssignableFrom(clazz)) {			
			try {
				Constructor<?> cnst = clazz.getConstructor(TestCommandFactory.class, Configuration.class,
						String.class);
				return (AbstractTestCommand) cnst.newInstance(sshTestingEnvironment.getCommandFactory(), cfg.subset("config"), command);
			} catch (NoSuchMethodException expected) {
				Constructor<?> cnst = clazz.getConstructor(TestCommandFactory.class, String.class);
				return (AbstractTestCommand) cnst.newInstance(sshTestingEnvironment.getCommandFactory(), command);
			}
		}
		throw new IllegalArgumentException(String.format("%s is not an instance of %s", clazz, AbstractTestCommand.class));
	}
	
	@Override
	public boolean handles( String command )
	{
		return ! cfg.subset(command).isEmpty();
	}

	@Override
	public AbstractTestCommand createCommand(final String command) {
		return createCommand(command, true);
	}

	@Override
	public AbstractTestCommand createCommand(final String command, boolean closeAfterCommand) {
		LOG.debug("Creating command: " + command);
		Configuration cmdCfg = cfg.subset(command);
		AbstractTestCommand cmd;
		try {
			cmd = this.instantiate(cmdCfg, command);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Unable to create command: " + command, e);
		}
		executedCommands.add(command);
		cmd.setCloseAfterError(closeAfterCommand);
		cmd.setCloseAfterError(this.sshTestingEnvironment.isCloseAfterError());
		return cmd;
	}

	/**
	 * create shell command.
	 *
	 * @param command command
	 * @return
	 */
	public AbstractTestCommand createShellCommand(final String command) {
		LOG.debug("Creating shell command: " + command);
		executedCommands.add(command);
		return createCommand( command, false );
	}

	/**
	 * Get the list of executed commands.
	 * @return
	 */
	@Override
	public List<String> getExecutedCommands() {
		return executedCommands;
	}
	
	/**
	 * Clear the list of executed commands.
	 */
	@Override
	public void clearExecutedCommands() {
		executedCommands.clear();
	}

	@Override
	public void clearState(ServerSession session) {
		// do nothing
	}
}