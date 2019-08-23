package org.xenei.test.testSSH.shell;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.test.testSSH.PromptHandler;
import org.xenei.test.testSSH.command.AbstractTestCommand;
import org.xenei.test.testSSH.command.TestCommandFactory;

import jline.console.ConsoleReader;
import jline.console.UserInterruptException;

public class TestShellFactory implements Factory<Command> {

	private static final Logger LOG = LoggerFactory.getLogger(TestShellFactory.class);

	/**
	 * 
	 */
	private final TestCommandFactory testCommandFactory;
	private final String defaultPrompt;


	/**
	 * @param sshTestingEnvironment
	 */
	public TestShellFactory(TestCommandFactory testCommandFactory, String prompt) {
		this.testCommandFactory = testCommandFactory;
		this.defaultPrompt = prompt;
	}

	@Override
	public Command create() {
		return new ShellCommand(defaultPrompt);
	}

	public class ShellCommand implements Command, Runnable, SessionAware {

		/**
		 * 
		 */
		private final String defaultPrompt;
		private ServerSession session;
		private PromptHandler promptHandler;

		/**
		 * @param sshTestingEnvironment
		 */
		ShellCommand(String prompt) {
			this.defaultPrompt = prompt;
		}

		private InputStream in;
		protected OutputStream out;
		protected OutputStream err;
		protected ExitCallback callback;
		private Thread thread;
		private ConsoleReader reader;

		@Override
		public void start(final Environment env) throws IOException {
			thread = new Thread(this, ShellCommand.class.getName());
			thread.start();
		}

		@Override
		public void destroy() throws Exception {
			thread.interrupt();
		}

		@Override
		public void setInputStream(final InputStream in) {
			this.in = in;
		}

		@Override
		public void setOutputStream(final OutputStream out) {
			this.out = out;
		}

		@Override
		public void setErrorStream(final OutputStream err) {
			this.err = err;
		}

		@Override
		public void setExitCallback(final ExitCallback callback) {
			this.callback = callback;
		}

		@Override
		public void run() {
			try {

				reader = new ConsoleReader(in, new FilterOutputStream(out) {
					@Override
					public void write(final int ii) throws IOException {
						super.write(ii);

						// workaround for MacOSX!! reset line after CR..
						if (ii == ConsoleReader.CR.toCharArray()[0]) {
							super.write(ConsoleReader.RESET_LINE);
						}
					}
				});
				// reader = new ConsoleReader( in, out );
				reader.setPrompt(defaultPrompt);

				// output welcome banner on ssh session startup
				reader.getOutput().write(ShellCommand.class.getSimpleName() + " testing shell");
				reader.getOutput().write("\n");
				reader.getOutput().flush();

				/**
				 * Set whether user interrupts (ctrl-C) are handled by having JLine throw
				 * {@link UserInterruptException} from {@link #readLine}. Otherwise, the JVM
				 * will handle {@code SIGINT} as normal, which usually causes it to exit. The
				 * default is {@code false}.
				 *
				 * @since 2.10
				 */
				reader.setHandleUserInterrupt(true);

				String line;
				String myPrompt = defaultPrompt;
				String promptPrefix = defaultPrompt;
				while (true) {
					try {
						String promptText = promptHandler.getPromptText();
						if ( promptText != null) {
							if (promptHandler.isFullPrompt()) {
								promptPrefix = promptText;
								myPrompt = promptPrefix;
							} else {
								myPrompt = String.format( "%s %s", promptPrefix, promptText);
							}
						} else {
							myPrompt = promptPrefix;
						}
						if ((line = reader.readLine(myPrompt)) == null) {
							break;
						}
						handleUserInput(line.trim());
					} catch (UserInterruptException exception) {
						clearState();
						promptPrefix = defaultPrompt;						
					}
				}
				callback.onExit(0);
			} catch (final InterruptedIOException ex) {
				callback.onExit(1);
			} catch (final Exception ex) {
				LOG.error("Error executing in Shell...", ex);
				callback.onExit(1);
			}
		}

		private void clearState() {
			testCommandFactory.clearState(session);
			promptHandler.clear();
		}

		private void handleUserInput(final String line) throws InterruptedIOException {
			final OutputStream myOut = new WriterOutputStream(reader.getOutput(), StandardCharsets.UTF_8);
			final AbstractTestCommand command = testCommandFactory.createCommand(line, false);
			command.setSession(session);
			command.setErrorStream(err);
			command.setOutputStream(myOut);
			command.setInputStream(reader.getInput());
			command.setExitCallback(callback);
			command.setReader( reader );
			command.run();
			try {
				myOut.write('\r');
				myOut.flush();
			} catch (IOException ignore) {
				LOG.debug("Error flusing output", ignore);
			}

			
		}

		@Override
		public void setSession(ServerSession session) {
			this.session = session;
			this.promptHandler = new PromptHandler(session);
		}
	}
}