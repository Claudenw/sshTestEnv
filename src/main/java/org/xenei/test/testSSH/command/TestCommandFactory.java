package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.commons.configuration2.Configuration;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class TestCommandFactory implements CommandFactory {
	
	private List<AbstractCommandFactory> factories;
	
	private Stack<AbstractCommandFactory> factoryStack;

	public TestCommandFactory(SSHTestingEnvironment sshTestingEnvironment, Configuration cfg) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		factories = new ArrayList<AbstractCommandFactory>();
		factoryStack = new Stack<AbstractCommandFactory>();
		Iterator<String> iter = cfg.getKeys();
		Set<String> seen = new TreeSet<String>();
		while (iter.hasNext() ) {
			seen.add( iter.next().split( "\\.")[0] );
		}
		
		for (String key : seen )
		{
			factories.add( instantiate( sshTestingEnvironment, cfg.subset(key)));
		}
	}
	
	public void push(AbstractCommandFactory factory)
	{
		factoryStack.push( factory );
	}
	
	private static AbstractCommandFactory instantiate(SSHTestingEnvironment sshTestingEnvironment, Configuration cfg)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> clazz = Class.forName(cfg.getString("class"));
		if (AbstractCommandFactory.class.isAssignableFrom(clazz)) {			
				Constructor<?> cnst = clazz.getConstructor(SSHTestingEnvironment.class, Configuration.class);
				return (AbstractCommandFactory) cnst.newInstance(sshTestingEnvironment, cfg.subset("config"));
		}
		throw new IllegalArgumentException(String.format("%s is not an instance of %s", clazz, AbstractCommandFactory.class));
	}
	
	public void clearState(ServerSession session) {
		factories.stream().forEach( f -> f.clearState(session) );
		factoryStack.clear();
	}
	
	@Override
	public AbstractTestCommand createCommand(String command) {
		return createCommand( command, false );
	}

	public AbstractTestCommand createCommand(String command, boolean closeAfterCommand) {
		if (!factoryStack.isEmpty())
		{
			return factoryStack.pop().createCommand(command, closeAfterCommand);
		}
		for (AbstractCommandFactory factory : factories)
		{
			if (factory.handles( command ))
			{
				return factory.createCommand(command, closeAfterCommand);
			}
		}
		// unknown command
		return new AbstractTestCommand(null, command) {

			@Override
			protected boolean handleCommand() throws IOException {
				String cmd = ValidateUtils.checkNotNullAndNotEmpty(command, "No command");
		        String errorMessage = String.format("Unknown command: [%s]", cmd );
				ValidateUtils.checkNotNull(err, "No error stream");
		        try {
		            err.write(errorMessage.getBytes(StandardCharsets.UTF_8));
		            err.write('\n');
		        } finally {
		            err.flush();
		        }
		        return false;
			}
		};
	}
}
