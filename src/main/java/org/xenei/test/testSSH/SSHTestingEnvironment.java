package org.xenei.test.testSSH;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Security;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.command.TestCommandFactory;
import org.xenei.test.testSSH.shell.TestShellFactory;


public class SSHTestingEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger( SSHTestingEnvironment.class );
    public static final boolean IS_MAC_OSX = System.getProperty( "os.name" ).startsWith( "Mac OS X" );

    private final TestShellFactory testShellFactory;
    private final TestCommandFactory testCommandFactory;
    private final PasswordAuthenticator pwdAuthenticator;
    private final PublickeyAuthenticator keyAuthenticator;
    
    private final int port;
    private boolean closeAfterError = false;
    private SshServer sshd;


    private static Options getOptions() {
    	Options options = new Options();
    	options.addRequiredOption( "c", "cfg", true, "Server Configuration file");
    	options.addOption( "p", "port", true, "Port number (if not set random port is used)");
    	options.addOption( "h", "help", false, "this help");
    	return options;
    }
    
    private static void doHelp( ) {
    	String header = String.format( "%s - help", SSHTestingEnvironment.class );
    	String footer = "\n";
    	 
    	HelpFormatter formatter = new HelpFormatter();
    	formatter.printHelp("myapp", header, getOptions(), footer, true);
    }

    /**
     * Run the test environment from the command line. Runs until enter is pressed on the keyboard.
     *
     * @param args
     *            ignored.
     * @throws IOException
     *             if keyboard read fails.
     * @throws ParseException 
     * @throws ConfigurationException 
     * @throws RuntimeException 
     * @throws ClassNotFoundException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static void main(final String[] args) throws IOException, ParseException, ConfigurationException, ClassNotFoundException, RuntimeException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // add at runtime the Bouncy Castle Provider
        // the provider is available only for this application
        Security.addProvider( new BouncyCastleProvider() );
        // BC is the ID for the Bouncy Castle provider;
        if (Security.getProvider( "BC" ) == null)
        {
            System.out.println( "Bouncy Castle provider is NOT available" );
        } else
        {
            System.out.println( "Bouncy Castle provider is available" );
        }
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        
        try {
        	cmdLine = parser.parse( getOptions(), args );
        } catch (ParseException e)
        {
        	System.err.println( e.getMessage() );
        	doHelp();
        	throw e;
        }
        
        if (cmdLine.hasOption("h"))
        {
        	doHelp();
        	System.exit(1);
        }
        
        Integer port = null;
        if (cmdLine.hasOption("p"))
        {
        	port = Integer.valueOf(cmdLine.getOptionValue("p"));
        }
        
        Configurations configs = new Configurations();
        File propertiesFile = new File(cmdLine.getOptionValue( "c" ));
        Configuration serverCfg = configs.properties(propertiesFile);
             		        		    
        final SSHTestingEnvironment env = port == null? new SSHTestingEnvironment( serverCfg ) : new SSHTestingEnvironment( serverCfg, port );
            
        System.out.println( String.format( "Port: %s", env.getPort() ) );
        env.setupServer();
        System.out.println( "Press Enter to quit" );
        try
        {
            System.in.read();
        } catch (final Exception ex)
        {
            ex.printStackTrace( System.err );
        }
        System.out.println( "Stopped" );
    }

    public SSHTestingEnvironment(Configuration serverCfg) throws RuntimeException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        this( serverCfg, PortFinder.findFreePort() );
    }

    /**
     * Create a SSHTestingEnvironment.
     *
     * @param port
     *            port to listen on
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public SSHTestingEnvironment(Configuration serverCfg, final int port) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        this.port = port;
        if (serverCfg.containsKey("authenticator.class"))
        {
	        Class<?> clazz = Class.forName( serverCfg.getString( "authenticator.class") );
	        Configuration cfg = serverCfg.subset("authenticator.config" );
	        Object authenticator = clazz.getConstructor( Configuration.class ).newInstance( cfg );
	        if (authenticator instanceof UserAuthenticator)
	        {
	        	pwdAuthenticator = (UserAuthenticator) authenticator;
	        	keyAuthenticator = null;
	        }
	        else if (authenticator instanceof KeyAuthenticator)
	        {
	        	pwdAuthenticator = null;
	        	keyAuthenticator = (KeyAuthenticator) authenticator;
	        }
	        else {
	        	throw new IllegalArgumentException( String.format("Class %s does not extend UserAuthenticator, or KeyAuthenticator",clazz ));
	        }
        }
        else {
        	LOG.info( "Creating password authenticator that accepts any id/pwd");
        	pwdAuthenticator = new PasswordAuthenticator() {

				@Override
				public boolean authenticate(String username, String password, ServerSession session)
						throws PasswordChangeRequiredException {
					return true;
				}};
			keyAuthenticator = null;
        }
        testCommandFactory = new TestCommandFactory( this, serverCfg.subset("commandFactory") );
        testShellFactory =  new TestShellFactory( testCommandFactory, serverCfg.getString( "prompt", "TS>"));
    }
    

    /**
     * Shut down SSHTestingEnvironment.
     *
     * @throws IOException
     *             on fail
     */
    public void shutDown() throws IOException {
        if (getSshd() != null)
        {
            getSshd().stop( true );
        }
        sshd = null;
    }

    public int getPort() {
        return port;
    }

    public TestCommandFactory getCommandFactory() {
        return testCommandFactory;
    }

    public void setCloseAfterError(final boolean state) {
        closeAfterError = state;
    }

    /**
     * set up server.
     *
     * @param pwd
     *            use password authenticator
     * @throws IOException
     *             because reasons
     */
    public void setupServer() throws IOException {
        sshd = SshServer.setUpDefaultServer();
        getSshd().setPort( port );
        getSshd().setKeyPairProvider( new SimpleGeneratorHostKeyProvider() );
        if ( pwdAuthenticator != null)
		{
            getSshd().setPasswordAuthenticator( pwdAuthenticator );
        } else
        {
            getSshd().setPublickeyAuthenticator( keyAuthenticator );
        }
        getSshd().setCommandFactory( testCommandFactory );
        getSshd().setShellFactory( testShellFactory );
        getSshd().start();
    }

    public boolean isClosed() {
        return getSshd().isClosed();
    }

	public SshServer getSshd() {
		return sshd;
	}

	public boolean isCloseAfterError() {
		return closeAfterError;
	}

}
