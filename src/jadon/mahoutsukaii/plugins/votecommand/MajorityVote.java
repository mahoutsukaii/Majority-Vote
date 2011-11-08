package jadon.mahoutsukaii.plugins.votecommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;

import ru.tehkode.permissions.bukkit.PermissionsEx;

@SuppressWarnings("deprecation")
public class MajorityVote extends JavaPlugin {

	Vote vote = new Vote(null, null, null, null);
	
	private final MajorityVotePlayerListener voteCommandPlayerListener = new MajorityVotePlayerListener(this);
	public Configuration properties = new Configuration(new File("plugins/MajorityVote/config.yml"));
	public String maindir = "plugins/MajorityVote/";
	
	public int defaultTime;
	public int minimumPercent;
	public int minimumPlayers;
	public int timeout;
	
	public List<String> allowedCommands;
	public ArrayList<CommandStuff> commandEssentials = new ArrayList<CommandStuff>();

	private Plugin permissionsEx;
	private Plugin groupManager;
	private Plugin permissions;
	
	public void getStuffs()
	{
		allowedCommands = properties.getKeys("commands");
		minimumPercent = properties.getNode("settings").getInt("minimumPercent", 50);
		minimumPlayers = properties.getNode("settings").getInt("minimumPlayers", 0);
		timeout = properties.getNode("settings").getInt("timeout", 120);
		
		for(String str : allowedCommands)
		{
			
			CommandStuff commandStuff = new CommandStuff(str, properties.getNode("commands").getNode(str).getString("command"), properties.getNode("commands").getNode(str).getString("description"));
			commandEssentials.add(commandStuff);
		}
		
	}

	public void onDisable()
	{
		System.out.print(this + " is now disabled!");
	}

	public void onEnable()
	{	
		new File(maindir).mkdir();
		createDefaultConfiguration("config.yml");

		properties.load();
		getStuffs();
		
		// permissions stuff
		permissionsEx = getServer().getPluginManager().getPlugin("PermissionsEx");
		groupManager = getServer().getPluginManager().getPlugin("GroupManager");
		permissions = getServer().getPluginManager().getPlugin("Permissions");

		
		
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Event.Type.PLAYER_MOVE, voteCommandPlayerListener, Priority.Normal, this);
		
		System.out.print(this + " is now enabled!");
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(command.getName().toLowerCase().equals("vote"))
		{
			return doVoteStuff(sender, args);
		}
		return false;
	}


	public boolean doVoteStuff(CommandSender sender, String[] args)
	{
		boolean auth = false;
		Player player = null;
		if(sender instanceof Player)
			player = (Player)sender;



		if(vote.isRunning()) //vote already running
		{
			if(args.length < 1)
			{
				return voteHelp(sender);
			}

			if(args[0].toLowerCase().equals("yes") | args[0].toLowerCase().equals("no") | args[0].toLowerCase().equals("stop"))
			{
				if(player != null)
				{
					if(getPermission(player, "majorityvote.input"))
					{
						auth = true;
					}
				}
				else auth = true;
			}
			else 
			{
				return voteHelp(sender);
			}

			if(auth)
				return playerVote(sender, args[0].toLowerCase());
			else 
			{
				sender.sendMessage(ChatColor.RED + "You are not allowed to vote.");
				return true;
			}
		}

		else //start new vote!
		{
			if(player != null)
			{
				if(getPermission(player, "majorityvote.startvote"))
				{
					auth = true;
				}
			}
			else auth = true;

			if(auth)
				return startVote(sender, args);
			else
			{
				sender.sendMessage(ChatColor.RED + "You are not allowed to cast a vote.");
				return true;
			}
		}
	}
	
	public boolean voteHelp(CommandSender sender)
	{
		if(vote.isRunning())
		{
			boolean auth = false;
			sender.sendMessage(ChatColor.GREEN + "A vote is currently in progress to " + ChatColor.YELLOW + vote.getVote() +  "! ");
			sender.sendMessage(ChatColor.YELLOW +  "/vote yes");
			sender.sendMessage(ChatColor.YELLOW +  "/vote no");
			if(sender instanceof Player)
			{
				if(sender.getName().equals(vote.getStartingPlayer()) | getPermission((Player)sender, "majorityvote.stop")) auth = true;

			}
			else auth = true;
			if(auth) sender.sendMessage(ChatColor.RED + "/vote stop");
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + "A vote is not in progress!");
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type " + ChatColor.YELLOW + "/vote [command] {player}" + ChatColor.LIGHT_PURPLE + " to start a vote!");

			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Allowed commands:");

			for(String str : allowedCommands)
			{
				int index = allowedCommands.indexOf(str);

				sender.sendMessage(ChatColor.GOLD + str + ": " + ChatColor.YELLOW + commandEssentials.get(index).getDescription());
			}
		}
		return true;
	}

	public boolean playerVote(CommandSender player, String vote)
	{
		if(vote.equals("stop"))
		{
			if(this.vote.getStartingPlayer().equals(player.getName()) | getPermission((Player)player,"majorityvote.stop"))
			{
				this.vote.terminate(player);
				return true;
			}
		}
		if(vote.equals("yes"))
			this.vote.addVoteYes(player);
		else
			this.vote.addVoteNo(player);
		

		


		return true;
	}

	public boolean startVote(CommandSender sender, String[] args)
	{
		if(args.length < 1)
		{
			return voteHelp(sender);
		}
		String command = args[0];
		if(!allowedCommands.contains(command))
		{
			return voteHelp(sender);
		}
		String victim;
		if(args.length < 2)
		{
			victim = "";
			int index = allowedCommands.indexOf(command);
			if(commandEssentials.get(index).getArgs().contains("%arg%"))
			{
				sender.sendMessage(ChatColor.RED + "This command requires an argument!");
				return true;
			}
		}
		else
			victim  = args[1];

		//if(getPermission(getServer().getPlayer(expandName(args[1])),"majorityvote.immune"))
		//{
		//	sender.sendMessage(ChatColor.RED + args[1] + " is not online!");
		//	return true;
		//}


		this.vote = new Vote(sender, command, victim, this);

		vote.start();
		getServer().broadcastMessage(ChatColor.YELLOW + sender.getName() + ChatColor.LIGHT_PURPLE +  " has started a vote to " +ChatColor.YELLOW + command + " " + victim);
		getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Vote by typing " +ChatColor.YELLOW +   " /vote yes"  + ChatColor.LIGHT_PURPLE + " or " +ChatColor.YELLOW +  "/vote no");
		vote.addVoteYes(sender);
		return true;
	}

	@SuppressWarnings("static-access")
	public boolean getPermission(Player player, String node)
	{
		if(player.hasPermission(node))
		{
			return true;
		}

		//PermissionsEx
		if(permissionsEx!=null)
			if( ((PermissionsEx) permissionsEx).getPermissionManager().has(player, node)) return true;

		if(permissions!=null)
			if(((Permissions) permissions).getHandler().has(player, node)) return true;

		if(groupManager!=null)
			if ( ((GroupManager) groupManager).getWorldsHolder().getWorldPermissions(player).has(player, node)) return true;

		return false;
	}

	public String expandName(String Name) {
		int m = 0;
		String Result = "";
		for (int n = 0; n < getServer().getOnlinePlayers().length; n++) {
			String str = getServer().getOnlinePlayers()[n].getName();
			if (str.matches("(?i).*" + Name + ".*")) {
				m++;
				Result = str;
				if(m==2) {
					return null;
				}
			}
			if (str.equalsIgnoreCase(Name))
				return str;
		}
		if (m == 1)
			return Result;
		if (m > 1) {
			return null;
		}
		if (m < 1) {
			return Name;
		}
		return Name;
	}
	protected void createDefaultConfiguration(String name) {
		File actual = new File(getDataFolder(), name);
		if (!actual.exists()) {

			InputStream input =
					this.getClass().getResourceAsStream("/defaults/" + name);
			if (input != null) {
				FileOutputStream output = null;

				try {
					output = new FileOutputStream(actual);
					byte[] buf = new byte[8192];
					int length = 0;
					while ((length = input.read(buf)) > 0) {
						output.write(buf, 0, length);
					}

					System.out.println(getDescription().getName()
							+ ": Default configuration file written: " + name);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException e) {}

					try {
						if (output != null)
							output.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	
	public String combineSplit(int startIndex, String[] string, String seperator) {
		StringBuilder builder = new StringBuilder();

		for (int i = startIndex; i < string.length; i++) {
			builder.append(string[i]);
			builder.append(seperator);
		}

		builder.deleteCharAt(builder.length() - seperator.length()); // remove
		return builder.toString();
	}

}
