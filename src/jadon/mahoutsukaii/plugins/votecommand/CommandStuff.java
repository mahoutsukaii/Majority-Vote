package jadon.mahoutsukaii.plugins.votecommand;


public class CommandStuff {
	
	private String commandName;
	private String args;
	private String description;
	
	public CommandStuff(String commandName, String args, String description)
	{
		this.commandName = commandName;
		this.args = args;
		this.description = description;
	}
	
	public String getCommandName()
	{
		return commandName;
	}
	public String getArgs()
	{
		return args;
	}
	
	public String getDescription()
	{
		return description;
	}


}
