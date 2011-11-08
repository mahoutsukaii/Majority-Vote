package jadon.mahoutsukaii.plugins.votecommand;


import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;


public class MajorityVotePlayerListener extends PlayerListener {
	
	MajorityVote plugin;
	
	public MajorityVotePlayerListener(MajorityVote instance)
	{
		this.plugin = instance;
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		if(plugin.vote.isRunning())
		{
			plugin.vote.checkLoss();
		}
	}

	
}
