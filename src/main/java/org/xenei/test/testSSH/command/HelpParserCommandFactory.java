package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.session.ServerSession;
import org.xenei.test.testSSH.PromptHandler;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class HelpParserCommandFactory extends AbstractCommandFactory {

	private Configuration config;
	
	@Override
	public void clearState(ServerSession session) {
		session.getProperties().remove(HelpCommand.class.getName());
	}
	
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

		private String getKey( ) {
			// the command prefix is stored in the context.
	    	Map<String,Object> context = getContext();	    	
	    	String pfx = StringUtils.defaultString((String)context.get("commandPrefix"));
	    	if (command.length() > 0)
	    	{
		    	if (pfx.length()>0) {
		    		pfx = String.format( "%s %s",  pfx, command);
		    	}
		    	else {
		    		pfx = command;
		    	}
	    	}
    		context.put( "commandPrefix", pfx );
    		
    		PromptHandler handler = new PromptHandler( session );
    		if (pfx.length() > 0)
    		{
    			handler.setText( pfx+" " );
    			return "tree."+pfx.replace( ' ', '.' );
    		}
    		
	    	return "tree";
	    	
		}
		
		
	    @Override
		protected boolean handleCommand() throws IOException {
	    	if (command == null) {
	    		return false;
	    	}
	    	
	    	String key = getKey();
	    	Configuration cfg = config.subset( key );
	    	Iterator<String> iter = cfg.getKeys();
	    	if (iter.hasNext()) {
		    	Set<String> subCmds = new HashSet<String>();
				while (iter.hasNext()) {
					String innerKey = iter.next().split("\\.")[0];
					if (innerKey.length() > 0 && subCmds.add(innerKey)) {
						String txt = cfg.getString( innerKey );
						if (txt == null)
						{
							out.write( String.format( "%s\n", innerKey ).getBytes( StandardCharsets.UTF_8 ) );
						} else {
							out.write( String.format( "%s\t%s\n", innerKey, txt ).getBytes( StandardCharsets.UTF_8 ) );
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
