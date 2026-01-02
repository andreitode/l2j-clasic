/*
 * This file is part of the L2J Coliseum project.
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

package org.classiclude.gameserver.community.utils;

import java.util.ArrayList;

import org.classiclude.Config;
import org.classiclude.gameserver.cache.HtmCache;
import org.classiclude.gameserver.model.actor.Player;

public final class CommunityBoard
{
	
	public static String getMenu(Player player)
	{
		final String navigation = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/navigation.html");
		ArrayList<String> buttons = new ArrayList<>();
		String links = "";
		String links_template = "<tr>" + "<td><button value=\"%text%\" action=\"%action%\" width=200 height=30 back=\"%iconBack%\" fore=\"%iconFore%\"></td>" + "</tr>";
		
		String btn = links_template;
		
		if (Config.COMMUNITYBOARD_ENABLE_BUFFS)
        {
        	btn = links_template;

        	btn = btn.replace("%text%", "Buffer");
        	btn = btn.replace("%iconBack%", "L2UI_CT1.HtmlWnd_DF_Level_Down");
        	btn = btn.replace("%iconFore%", "L2UI_CT1.HtmlWnd_DF_Level");
            btn = btn.replace("%action%", "bypass _bbstop;buffer/main.html");
        	buttons.add(btn);
        }

        if (Config.COMMUNITYBOARD_ENABLE_MULTISELLS)
        {
        	btn = links_template;

        	btn = btn.replace("%text%", "Merchant");
        	btn = btn.replace("%iconBack%", "L2UI_CT1.OlympiadWnd_DF_BuyEquip_Down");
        	btn = btn.replace("%iconFore%", "L2UI_CT1.OlympiadWnd_DF_BuyEquip");
            btn = btn.replace("%action%", "bypass _bbstop;merchant/main.html");
        	buttons.add(btn);
        }

        if (Config.COMMUNITYBOARD_ENABLE_TELEPORTS)
        {
        	btn = links_template;

        	btn = btn.replace("%text%", "Gatekeeper");
        	btn = btn.replace("%iconBack%", "L2UI_CT1.HtmlWnd_DF_Campaign_Down");
        	btn = btn.replace("%iconFore%", "L2UI_CT1.HtmlWnd_DF_Campaign");
            btn = btn.replace("%action%", "bypass _bbstop;gatekeeper/main.html");
        	buttons.add(btn);
        }

        btn = links_template;

        btn = btn.replace("%text%", "Drop Search");
        btn = btn.replace("%iconBack%", "L2UI_CT1.HtmlWnd_DF_Area_Down");
        btn = btn.replace("%iconFore%", "L2UI_CT1.HtmlWnd_DF_Area");
        btn = btn.replace("%action%", "bypass _bbstop;dropsearch/main.html");
        buttons.add(btn);

        if (Config.COMMUNITYBOARD_ENABLE_VOTE)
        {
        	btn = links_template;

        	btn = btn.replace("%text%", "Vote");
        	btn = btn.replace("%iconBack%", "L2UI_CT1.OlympiadWnd_DF_Reward_Down");
        	btn = btn.replace("%iconFore%", "L2UI_CT1.OlympiadWnd_DF_Reward");
            btn = btn.replace("%action%", "bypass _bbsvote");
        	buttons.add(btn);
        }
		for (String button : buttons)
		{
			links += button;
		}
		return navigation.replace("%navLinks%", links);
	}
	
	public static String htmlRowControlTr(ArrayList<String> htmParsedRowArr, int limit)
	{
		String htmContent = "<tr>";
		if (htmParsedRowArr.size() > 0)
		{
			int x = 0;
			for (String entry : htmParsedRowArr)
			{
				x++;
				htmContent += entry;
				if (((x % limit) == 0) || (htmParsedRowArr.size() == x))
				{
					htmContent += "</tr>";
					if (htmParsedRowArr.size() > x)
					{
						htmContent += "<tr>";
					}
				}
			}
		}
		else
		{
			htmContent = "";
		}
		
		return htmContent;
	}
	
	public static String htmlRowControlTrTable(ArrayList<String> htmParsedRowArr, int limit)
	{
		String htmContent = "<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\"><tr>";
		if (htmParsedRowArr.size() > 0)
		{
			int x = 0;
			for (String entry : htmParsedRowArr)
			{
				
				x++;
				htmContent += entry;
				if (((x % limit) == 0) || (htmParsedRowArr.size() == x))
				{
					htmContent += "</tr></table><br><br>";
					if (htmParsedRowArr.size() > x)
					{
						htmContent += "<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\"><tr>";
					}
				}
			}
		}
		else
		{
			htmContent += "</tr></table><br><br>";
		}
		return htmContent;
	}
}