package jadon.mahoutsukaii.plugins.votecommand;



import java.util.ArrayList;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Vote {
	
	MajorityVote plugin;
	
	private boolean running;
	private long initTime;
	private CommandSender startPlayer;
	private String target;
	private String command;
	private int numberOfYes;
	private ArrayList<String> votedPlayers = new ArrayList<String>();
	
	public Vote(CommandSender player, String command, String target, MajorityVote instance)
	{
		this.initTime = new Date().getTime();
		this.startPlayer = player;
		this.plugin = instance;
		this.command = command;
		this.target = target;
		this.running = false;
		this.votedPlayers.clear();
		this.numberOfYes = 0;
		

	}

	public boolean isRunning()
	{
		return running;
	}
	
	public long getStartTime()
	{
		return initTime;
	}
	
	public String getStartingPlayer()
	{
		return startPlayer.getName();
	}
	
	public void closeVote()
	{
		running = false;
	}
	
	public void addVoteYes(CommandSender player)
	{
		if(votedPlayers.contains(player.getName()))
		{
			player.sendMessage(ChatColor.RED + "You have already voted!");
			return;
		}
		player.sendMessage(ChatColor.LIGHT_PURPLE + "Your demand has been taken into consideration.");
		votedPlayers.add(player.getName());
		System.out.print("[MajorityVote] " + player.getName() + " voted yes.");
		numberOfYes++;
		checkVictory();
		
	}
	
	public void checkVictory()
	{
		// percentage. Is it initialised?
		//System.out.print(numberOfYes);
		//System.out.print(plugin.minimumPlayers);
		if(numberOfYes / plugin.getServer().getOnlinePlayers().length * 100 >= plugin.minimumPercent)
		{

			if(numberOfYes >= plugin.minimumPlayers)
			{
				plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Vote passed!");
				executeCommand();
			}
		}


	}
	
	public void checkLoss()
	{
		if(initTime + plugin.timeout * 1000 < new Date().getTime())
		{
			timeout();
		}
	}
	
	public void timeout()
	{
		plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "The vote failed! "+ChatColor.RED + numberOfYes +"/" + plugin.getServer().getOnlinePlayers().length);
		
		stop();
	}
	public void addVoteNo(CommandSender player)
	{
		if(votedPlayers.contains(player.getName()))
		{
			player.sendMessage(ChatColor.RED + "You have already voted!");
			return;
		}
		player.sendMessage(ChatColor.LIGHT_PURPLE + "Your demand has been taken into consideration.");
		System.out.print("[MajorityVote] " + player.getName() + " voted no.");
		votedPlayers.add(player.getName());
	}
	public void executeCommand()
	{
		stop();
		String command = this.command;
		int index = plugin.allowedCommands.indexOf(command);
		 String args = plugin.commandEssentials.get(index).getArgs();
		 
		 args = args.replace("%arg%", target);
		 
		 command = args;
		 
		System.out.print(command);
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
	
		
		
	}
	
	public String getVote()
	{
		return this.command + " "+ this.target;
	}
	public void start()
	{
		this.running = true;
	
		//executeCommand();
	}
	
	public void stop()
	{
		this.running = false;
	}
	
	public void terminate(CommandSender sender)
	{
		plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "The vote was terminated by " + ChatColor.YELLOW + sender.getName() + "!");
		stop();
	}

}
