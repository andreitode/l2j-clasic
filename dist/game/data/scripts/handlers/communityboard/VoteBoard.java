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
package handlers.communityboard;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.classiclude.Config;
import org.classiclude.gameserver.cache.HtmCache;
import org.classiclude.gameserver.data.xml.VoteSiteData;
import org.classiclude.gameserver.handler.CommunityBoardHandler;
import org.classiclude.gameserver.handler.IParseBoardHandler;
import org.classiclude.gameserver.instancemanager.VoteManager;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.votesystem.VoteSite;

/**
 * Home board.
 * @author Tuccy
 */
public class VoteBoard implements IParseBoardHandler
{
	// SQL Queries
	
	private static final String[] COMMANDS =
	{
		"_bbsvote",
		"_bbsvote_reward"
	};
	
	private static final Predicate<Player> KARMA_CHECK = player -> Config.COMMUNITYBOARD_KARMA_DISABLED && (player.getReputation() < 0);
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		return COMMANDS;
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		if (KARMA_CHECK.test(player))
		{
			player.sendMessage("Players with Karma cannot use the Community Board.");
			return false;
		}
		
		String returnHtml = null;
		if (command.equals("_bbsvote"))
		{
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/vote/main.html");
			returnHtml = returnHtml.replace("%breadCumb%", composeBreadCumb(command.toString()));
			returnHtml = returnHtml.replace("%everyXtime%", String.valueOf(Config.CUSTOM_VOTE_INTERVAL_TOPSITES));
			returnHtml = returnHtml.replace("%sites%", getVoteList(player, command));
		}
		else if (command.startsWith("_bbsvote_reward"))
		{
			for (Entry<Integer, VoteSite> site : VoteSiteData.getInstance().getSites().entrySet())
			{
				new Thread(() ->
				{
					VoteManager.getInatance().getReward(player, site.getValue().getSiteOrdinal());
				}).start();
			}
			parseCommunityBoardCommand(player.getLastCBPageBypass(), player);
			CommunityBoardHandler.getInstance().addBypass(player, "Vote", command);
			return true;
		}
		
		if (returnHtml != null)
		{
			returnHtml = returnHtml.replace("%navigation%", org.classiclude.gameserver.community.utils.CommunityBoard.getMenu(player));
			player.setLastCBPageBypass(command);
			CommunityBoardHandler.separateAndSend(returnHtml, player);
		}
		return false;
	}
	
	public static String getVoteList(Player player, String command)
	{
		String htmRowTemplate = "<td fixwidth=\"170\" align=\"center\" valign=\"top\">";
		htmRowTemplate += "<button action=\"URL  %url%\" value=\"%name%\" width=170 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
		htmRowTemplate += "</td>";
		
		ArrayList<String> parsetHtmlArr = new ArrayList<>();
		for (Entry<Integer, VoteSite> entrie : VoteSiteData.getInstance().getSites().entrySet())
		{
			if (entrie.getValue().getUrl(player) != null)
			{
				String row = htmRowTemplate;
				row = row.replace("%name%", entrie.getValue().getSiteName());
				row = row.replace("%url%", entrie.getValue().getUrl(player));
				parsetHtmlArr.add(row);
			}
		}
		
		if ((parsetHtmlArr.size() % 3) != 0)
		{
			parsetHtmlArr.add("<td fixwidth=\"170\" align=\"center\" valign=\"top\"></td>");
		}
		
		return org.classiclude.gameserver.community.utils.CommunityBoard.htmlRowControlTr(parsetHtmlArr, 3);
	}
	
	public static String composeBreadCumb(String command)
	{
		String breadCumb = "<a action=\"bypass _bbshome\">Home</a>";
		breadCumb += "&nbsp;>&nbsp;Custom Comunity Vote";
		
		return breadCumb;
	}
}