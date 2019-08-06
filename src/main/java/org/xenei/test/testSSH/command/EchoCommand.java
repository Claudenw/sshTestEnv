package org.xenei.test.testSSH.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class EchoCommand extends AbstractTestCommand implements Command, Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger( EchoCommand.class );	


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
    public EchoCommand(SSHTestingEnvironment sshTestingEnvironment, final String command, final boolean closeAfterCommand, final boolean closeAfterError) {
    	super( sshTestingEnvironment, command, closeAfterCommand, closeAfterError );        
    }

    protected boolean handleCommand() throws IOException {
        
        out.write( String.format( "Command: %s", command ).getBytes( StandardCharsets.UTF_8 ) );
        out.write( '\n' );

        return true;
    }

}