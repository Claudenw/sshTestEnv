package org.xenei.test.testSSH;

import org.apache.sshd.server.session.ServerSession;

/**
 * Handle prompt changes tracking by managing values in the session.
 *
 */
public class PromptHandler {
	
	private ServerSession session;

	/**
	 * Construcor.
	 * @param session The session to manage the prompt in.
	 */
	public PromptHandler( ServerSession session ) {
		this.session = session;
	}
	
	/**
	 * Set the prompt.
	 * @param prompt the prompt that will be displayed.
	 */
	public void setPrompt( String prompt ) {
		session.getProperties().put("_prompt", prompt );
		session.getProperties().put("_prompt_full", Boolean.TRUE);
	}
	
	/**
	 * Appends text to the prompt
	 * @param text the text to append to the prompt.
	 */
	public void setText( String text ) {
		session.getProperties().put("_prompt", text );
		session.getProperties().put("_prompt_full", Boolean.FALSE);		
	}
	
	/**
	 * Get the prompt text.
	 * @return the prompt text or null if it is not set.
	 */
	public String getPromptText()
	{
		return (String)session.getProperties().get("_prompt");
	}
	
	/**
	 * get the full prompt flag.
	 * @return true if the promptText is the full text, false if it to be appended to the text.
	 */
	public boolean isFullPrompt() {
		Boolean b = (Boolean) session.getProperties().get("_prompt_full");
		return b == null ? false : b.booleanValue();
	}
	
	/**
	 * Clear the prompt settings.
	 */
	public void clear() {
		session.getProperties().remove("_prompt" );
		session.getProperties().remove("_prompt_full");
	}

}
