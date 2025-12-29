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
package org.classiclude.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.classiclude.commons.util.IXmlReader;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.votesystem.RewardVote;
import org.classiclude.gameserver.model.votesystem.VoteSite;

/**
 * @author l2.topgameserver.net
 */
public class VoteSiteData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(VoteSiteData.class.getName());
	private final Map<Integer, VoteSite> _voteSites = new HashMap<>();
	
	protected VoteSiteData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		parseDatapackFile("data/VoteSystem.xml");
		LOGGER.info("Loaded " + _voteSites.size() + " reward sites");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("votesite".equalsIgnoreCase(d.getNodeName()))
					{
						final VoteSite votesite = new VoteSite();
						votesite.setSiteName(parseString(d.getAttributes(), "name"));
						votesite.setSiteOrdinal(parseInteger(d.getAttributes(), "ordinal"));
						votesite.setUrl(parseString(d.getAttributes(), "url"));
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							for (Node cde = cd.getFirstChild(); cde != null; cde = cde.getNextSibling())
							{
								if ("item".equalsIgnoreCase(cde.getNodeName()))
								{
									votesite.getRewardList().add(new RewardVote(new StatSet(parseAttributes(cde))));
									_voteSites.put(votesite.getSiteOrdinal(), votesite);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public String getSiteName(int ordinal)
	{
		return _voteSites.get(ordinal).getSiteName();
	}
	
	public Collection<RewardVote> getRewards(int ordinal)
	{
		return _voteSites.get(ordinal).getRewardList();
	}
	
	public Map<Integer, VoteSite> getSites()
	{
		return _voteSites;
	}
	
	public static final VoteSiteData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final VoteSiteData INSTANCE = new VoteSiteData();
	}
}