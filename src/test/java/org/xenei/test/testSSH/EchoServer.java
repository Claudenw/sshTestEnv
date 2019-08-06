package org.xenei.test.testSSH;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.junit.Test;
import org.xenei.test.testSSH.command.AbstractTestCommand;
import org.xenei.test.testSSH.command.EchoCommandFactory;

public class EchoServer {
	
	@Test
	public void testEcho() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		Configuration cfg = new PropertiesConfiguration();
		cfg.addProperty("commandFactory.1.class", EchoCommandFactory.class.getName());
		
		SSHTestingEnvironment env = new SSHTestingEnvironment(cfg, 222);
		AbstractTestCommand cmd = env.getCommandFactory().createCommand("hello");
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		cmd.setErrorStream( err );
		cmd.setOutputStream( out );
		cmd.run();
		assertEquals( "Wrote to error", 0, err.toByteArray().length);
		assertEquals( "hello\n", out.toString());
	}

	@Test
	public void testDefinedEcho() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		Configuration cfg = new PropertiesConfiguration();
		cfg.addProperty("commandFactory.1.class", EchoCommandFactory.class.getName());
		cfg.addProperty("commandFactory.1.config.one", null);

		
		SSHTestingEnvironment env = new SSHTestingEnvironment(cfg, 222);
		AbstractTestCommand cmd = env.getCommandFactory().createCommand("one");
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		cmd.setErrorStream( err );
		cmd.setOutputStream( out );
		cmd.run();
		assertEquals( "Wrote to error", 0, err.toByteArray().length);
		assertEquals( "one\n", out.toString());
	}
	
	@Test
	public void testUndefinedEcho() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		Configuration cfg = new PropertiesConfiguration();
		cfg.addProperty("commandFactory.1.class", EchoCommandFactory.class.getName());
		cfg.addProperty("commandFactory.1.config.one", "");
				
		SSHTestingEnvironment env = new SSHTestingEnvironment(cfg, 222);
		AbstractTestCommand cmd = env.getCommandFactory().createCommand("two");
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		cmd.setErrorStream( err );
		cmd.setOutputStream( out );
		cmd.run();
		assertEquals( "Unknown command: two\n", err.toString());
		assertEquals( "Wrote to out", 0, out.toByteArray().length);
	}
}
