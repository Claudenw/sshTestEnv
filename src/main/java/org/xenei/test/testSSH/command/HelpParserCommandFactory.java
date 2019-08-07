package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.sshd.server.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class HelpParserCommandFactory extends AbstractCommandFactory {

	private Configuration config;
	
	public HelpParserCommandFactory(SSHTestingEnvironment sshTestingEnvironment, Configuration config) {
		super(sshTestingEnvironment);
		this.config = config;
	}
	
	private String parseKey(String command) {
		String[] parts = command.trim().split( "[\\s]+" );
		if ( "?".contentEquals(parts[parts.length-1]))
		{
			String key = String.join(".", parts);			
			return  (parts.length > 1) ? key.substring(0, key.length()-2 ) : "";
		}
		return null;
	}

	@Override
	public boolean handles(String command) {
		String key = parseKey(command);
		if (key != null)
		{
			return (key.length()==0) ? true : config.subset(key).isEmpty();
		}
		return false;
	}

	@Override
	public AbstractTestCommand createCommand(String command, boolean closeAfterCommand) {
		HelpCommand cmd = new HelpCommand( sshTestingEnvironment.getCommandFactory(), parseKey(command) );
		cmd.setCloseAfterCommand( closeAfterCommand );
		return cmd;
	}

	
	private class HelpCommand extends AbstractTestCommand implements Command, Runnable {	

	   
	    public HelpCommand(TestCommandFactory testCommandFactory, final String command) {
	    	super( testCommandFactory, command );        
	    }
	    
		private void writeEmptyList() throws IOException {
			String txt = config.getString( "empty_list", "" );
			out.write( txt.getBytes( StandardCharsets.UTF_8 ));
			out.write( '\n');		
		}

	    protected boolean handleCommand() throws IOException {
	    	if (command == null) {
	    		return false;
	    	}
	    	Configuration cfg = config.subset( command.length()>0?"tree."+command:"tree");
	    	Iterator<String> iter = cfg.getKeys();
	    	if (iter.hasNext()) {
		    	Set<String> subCmds = new HashSet<String>();
				while (iter.hasNext()) {
					String key = iter.next().split("\\.")[0];
					if (key.length() > 0 && subCmds.add(key)) {
						String txt = cfg.getString( key );
						if (txt == null)
						{
							out.write( String.format( "%s\n", key ).getBytes( StandardCharsets.UTF_8 ) );
						} else {
							out.write( String.format( "%s\t%s\n", key, txt ).getBytes( StandardCharsets.UTF_8 ) );
						}
					}
					if (subCmds.isEmpty())
					{
						writeEmptyList();
					}
				}
	    	} else {
	    		writeEmptyList();
	    	}
	        return true;
	    }

	}
}
