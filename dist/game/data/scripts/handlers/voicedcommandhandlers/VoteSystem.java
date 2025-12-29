/*
 * This file is part of the ClassicLude project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import org.classiclude.Config;
import org.classiclude.gameserver.enums.VoteSite;
import org.classiclude.gameserver.handler.IVoicedCommandHandler;
import org.classiclude.gameserver.instancemanager.VoteManager;
import org.classiclude.gameserver.model.actor.Player;

public class VoteSystem implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		Config.CUSTOM_VOTE_COMMAND_CMD
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String params)
	{
		if (command.equalsIgnoreCase(Config.CUSTOM_VOTE_COMMAND_CMD))
		{
			if (!Config.CUSTOM_VOTE_ENABLE)
			{
				player.sendMessage("The rewards system has been disabled by your administrator");
				return false;
			}
			if (!Config.CUSTOM_VOTE_COMMAND_ENABLE)
			{
				player.sendMessage("Voting command reward is disabled");
				return false;
			}
			if (player.isJailed())
			{
				player.sendMessage("You can't use that function while incarcerated");
				return false;
			}
			
			for (VoteSite vs : VoteSite.values())
			{
				
				new Thread(() ->
				{
					VoteManager.getInatance().getReward(player, vs.ordinal());
				}).start();
			}
			
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}