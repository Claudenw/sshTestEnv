package org.xenei.test.testSSH.shell;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.xenei.test.testSSH.SSHTestingEnvironment;

public class TestShellFactory implements Factory<Command> {
    /**
	 * 
	 */
	private final SSHTestingEnvironment sshTestingEnvironment;
	private final String prompt;

	/**
	 * @param sshTestingEnvironment
	 */
	public TestShellFactory(SSHTestingEnvironment sshTestingEnvironment, String prompt) {
		this.sshTestingEnvironment = sshTestingEnvironment;
		this.prompt = prompt;
	}

	@Override
    public Command create() {
        return new ShellCommand(this.sshTestingEnvironment, prompt);
    }

}