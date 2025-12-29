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
package org.classiclude.gameserver.model.votesystem;

import java.util.ArrayList;
import java.util.List;

import org.classiclude.gameserver.model.actor.Player;

/**
 * @author Tuccy
 */
public class VoteSite
{
	private int _siteOrdinal;
	private String _siteName;
	private String _url;
	private final List<RewardVote> _rewards = new ArrayList<>();
	
	public void setSiteOrdinal(int siteOrdinal)
	{
		_siteOrdinal = siteOrdinal;
	}
	
	public void setSiteName(String siteName)
	{
		_siteName = siteName;
	}
	
	public void setRewardList(List<RewardVote> rewards)
	{
		for (RewardVote r : rewards)
		{
			_rewards.add(r);
		}
	}
	
	public int getSiteOrdinal()
	{
		return _siteOrdinal;
	}
	
	public String getSiteName()
	{
		return _siteName;
	}
	
	public List<RewardVote> getRewardList()
	{
		return _rewards;
	}
	
	public void setUrl(String url)
	{
		_url = url;
	}
	
	public String getUrl(Player player)
	{
		String url = _url;
		if (url != null)
		{
			url = url.replace("%ip%", player.getIPAddress());
			url = url.replace("%char%", player.getName());
			url = url.replace("%uparam%", "&u");
		}
		return url;
	}
}