/*
 * Copyright (c) 2013 classiclude
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.classiclude.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.classiclude.Config;
import org.classiclude.commons.util.IXmlReader;
import org.classiclude.gameserver.data.sql.CharInfoTable;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.templates.NpcTemplate;
import org.classiclude.gameserver.model.holders.FakePlayerHolder;

/**
 * @author Mobius
 */
public class FakePlayerData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(FakePlayerData.class.getName());
	
	private final Map<Integer, FakePlayerHolder> _fakePlayerInfos = new HashMap<>();
	private final Map<String, String> _fakePlayerNames = new HashMap<>();
	private final Map<String, Integer> _fakePlayerIds = new HashMap<>();
	private final Set<String> _talkableFakePlayerNames = new HashSet<>();
	
	protected FakePlayerData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		if (Config.FAKE_PLAYERS_ENABLED)
		{
			_fakePlayerInfos.clear();
			_fakePlayerNames.clear();
			_fakePlayerIds.clear();
			_talkableFakePlayerNames.clear();
			parseDatapackFile("data/FakePlayerVisualData.xml");
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _fakePlayerInfos.size() + " templates.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": Disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "fakePlayer", fakePlayerNode ->
		{
			final StatSet set = new StatSet(parseAttributes(fakePlayerNode));
			final int npcId = set.getInt("npcId");
			final NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			final String name = template.getName();
			if (CharInfoTable.getInstance().getIdByName(name) > 0)
			{
				LOGGER.info(getClass().getSimpleName() + ": Could not create fake player template " + npcId + ", player name already exists.");
			}
			else
			{
				_fakePlayerIds.put(name, npcId); // name - npcId
				_fakePlayerNames.put(name.toLowerCase(), name); // name to lowercase - name
				_fakePlayerInfos.put(npcId, new FakePlayerHolder(set));
				if (template.isFakePlayerTalkable())
				{
					_talkableFakePlayerNames.add(name.toLowerCase());
				}
			}
		}));
	}
	
	public int getNpcIdByName(String name)
	{
		return _fakePlayerIds.get(name);
	}
	
	public String getProperName(String name)
	{
		return _fakePlayerNames.get(name.toLowerCase());
	}
	
	public boolean isTalkable(String name)
	{
		return _talkableFakePlayerNames.contains(name.toLowerCase());
	}
	
	public FakePlayerHolder getInfo(int npcId)
	{
		return _fakePlayerInfos.get(npcId);
	}
	
	public static FakePlayerData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FakePlayerData INSTANCE = new FakePlayerData();
	}
}
