# Overview

The sshTestEnv is an SSH server.  It runs on a random (unassigned) port, or you can specify a port with `-p`.  For complete options use `-h`.

You must provide a configuration file `-c`.


# Configuration

## Common options

All Configurations have the following options

 * authenticator.class - the class name of the authenticator implementation.  If not specified any user/name will be accepted
 * authenticator.config - the configuration options for the authenticator class.  subvalues depend on implementation.
 * prompt - the prompt for the server.
 * commandFactory.n.class= the nth command factory class name.
 * commandFactory.n.config=the configuration for the the nth command factory.
 
When a command is executed each commandFactory is checked in order to see if it handles the command.  CommandFactories are lexically ordered by "n".

### Authenticator Implementations

#### org.xenei.test.testSSH.KeyAuthenticator

Does not really authenticate, just accepts keys and either accepts or rejects them all.

configuration options: 
 * accepted=true|false  if not true then all keys are rejected.

#### org.xenei.test.testSSH.UserAuthenticator

Does not really authenticate, just accepts id and password.  Will accept if the id matches the id in the config.

configuration options: 
 * id=<userid>   The user ID that is valid.  No password check performed.

CommandFactory Implementations:

#### org.xenei.test.testSSH.command.EchoCommandFactory

Simply echos the command to output.

If no factory specific configurations are provided all commands are accepted and echoed.

If the configuration is provided then any option is the command to echo, any commands not in the configuration are not handled.

example:

    commandFactory.1.class=org.xenei.test.testSSH.command.EchoCommandFactory
    commandFactory.1.config.hello=
    commandFactory.1.config.goodbye=

the above configuration will echo the commands "hello" and "goodbye".  All others will be ignored.


#### org.xenei.test.testSSH.command.StaticCommandFactory

Provides static responses for commands

Processes the commands specified in the configuration (like echo) but instead of echoing the command returns the value

example:

    commandFactory.1.class=org.xenei.test.testSSH.command.StaticCommandFactory
    commandFactory.1.config.hello=hola
    commandFactory.1.config.goodbye=adios

the above configuration will respond with "hola" to the command "hello" and "adios" to the command "goodbye".  All others will be ignored.


#### org.xenei.test.testSSH.command.HelpParserCommandFactory

Parses a tree structure as a list of help commands.  Only processes commands that end with a single "?" as the last argument.

configuration options:
 * empty_list:  the characters to send if there are no more options to display
 * tree: the prefix for the nodes in the tree of commands

example:

    commandFactory.1.class=org.xenei.test.testSSH.command.HelpParserCommandFactory
    commandFactory.1.config.empty_list=<CR>
    commandFactory.1.config.tree.root.one.two=The second
    commandFactory.1.config.tree.root.one.potato=One Potato
    commandFactory.1.config.tree.root.one.potato.two=Two Potato
    commandFactory.1.config.tree.root.one.potato.thee=Three Potato
    commandFactory.1.config.tree.root.one.potato.four=Four
    commandFactory.1.config.tree.root.one.potato.hot=Hot Potato
    commandFactory.1.config.tree.root.one.rocket-ship=One rocket ship
    commandFactory.1.config.tree.root.one.more=One more
    commandFactory.1.config.tree.root.one.more.four=One more for the road

the command "?" will return
 
    root

the command "root ?" will return
 
    one

the command "root one ?" will return

    two     The Second
    potato    One Potato
    rocket-ship   One rocket ship
    more   One more

the command "root one two ?" will return

    <CR>

note the "<CR>" is the literal "<CR>" specified in the empty_list property noted above
