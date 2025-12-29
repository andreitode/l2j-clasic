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
package org.classiclude.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.classiclude.Config;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.commons.util.IXmlReader;
import org.classiclude.commons.util.Rnd;
import org.classiclude.gameserver.data.SpawnTable;
import org.classiclude.gameserver.data.xml.FakePlayerData;
import org.classiclude.gameserver.enums.ChatType;
import org.classiclude.gameserver.geoengine.GeoEngine;
import org.classiclude.gameserver.model.Spawn;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.holders.FakePlayerChatHolder;
import org.classiclude.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Mobius
 */
public class FakePlayerChatManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(FakePlayerChatManager.class.getName());
	final List<FakePlayerChatHolder> MESSAGES = new ArrayList<>();
	private static final int MIN_DELAY = 5000;
	private static final int MAX_DELAY = 15000;
	
	protected FakePlayerChatManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		if (Config.FAKE_PLAYERS_ENABLED && Config.FAKE_PLAYER_CHAT)
		{
			MESSAGES.clear();
			parseDatapackFile("data/FakePlayerChatData.xml");
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + MESSAGES.size() + " chat templates.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": Disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "fakePlayerChat", fakePlayerChatNode ->
		{
			final StatSet set = new StatSet(parseAttributes(fakePlayerChatNode));
			MESSAGES.add(new FakePlayerChatHolder(set.getString("fpcName"), set.getString("searchMethod"), set.getString("searchText"), set.getString("answers")));
		}));
	}
	
	public void manageChat(Player player, String fpcName, String message)
	{
		ThreadPool.schedule(() -> manageResponce(player, fpcName, message), Rnd.get(MIN_DELAY, MAX_DELAY));
	}
	
	public void manageChat(Player player, String fpcName, String message, int minDelay, int maxDelay)
	{
		ThreadPool.schedule(() -> manageResponce(player, fpcName, message), Rnd.get(minDelay, maxDelay));
	}
	
	private void manageResponce(Player player, String fpcName, String message)
	{
		if (player == null)
		{
			return;
		}
		
		final String text = message.toLowerCase();
		
		// tricky question
		if (text.contains("can you see me"))
		{
			final Spawn spawn = SpawnTable.getInstance().getAnySpawn(FakePlayerData.getInstance().getNpcIdByName(fpcName));
			if (spawn != null)
			{
				final Npc npc = spawn.getLastSpawn();
				if (npc != null)
				{
					if (npc.calculateDistance2D(player) < 3000)
					{
						if (GeoEngine.getInstance().canSeeTarget(npc, player) && !player.isInvisible())
						{
							sendChat(player, fpcName, Rnd.nextBoolean() ? "i am not blind" : Rnd.nextBoolean() ? "of course i can" : "yes");
						}
						else
						{
							sendChat(player, fpcName, Rnd.nextBoolean() ? "i know you are around" : Rnd.nextBoolean() ? "not at the moment :P" : "no, where are you?");
						}
					}
					else
					{
						sendChat(player, fpcName, Rnd.nextBoolean() ? "nope, can't see you" : Rnd.nextBoolean() ? "nope" : "no");
					}
					return;
				}
			}
		}
		
		for (FakePlayerChatHolder chatHolder : MESSAGES)
		{
			if (!chatHolder.getFpcName().equals(fpcName) && !chatHolder.getFpcName().equals("ALL"))
			{
				continue;
			}
			
			switch (chatHolder.getSearchMethod())
			{
				case "EQUALS":
				{
					if (text.equals(chatHolder.getSearchText().get(0)))
					{
						sendChat(player, fpcName, chatHolder.getAnswers().get(Rnd.get(chatHolder.getAnswers().size())));
					}
					break;
				}
				case "STARTS_WITH":
				{
					if (text.startsWith(chatHolder.getSearchText().get(0)))
					{
						sendChat(player, fpcName, chatHolder.getAnswers().get(Rnd.get(chatHolder.getAnswers().size())));
					}
					break;
				}
				case "CONTAINS":
				{
					boolean allFound = true;
					for (String word : chatHolder.getSearchText())
					{
						if (!text.contains(word))
						{
							allFound = false;
						}
					}
					if (allFound)
					{
						sendChat(player, fpcName, chatHolder.getAnswers().get(Rnd.get(chatHolder.getAnswers().size())));
					}
					break;
				}
			}
		}
	}
	
	public void sendChat(Player player, String fpcName, String message)
	{
		final Spawn spawn = SpawnTable.getInstance().getAnySpawn(FakePlayerData.getInstance().getNpcIdByName(fpcName));
		if (spawn != null)
		{
			final Npc npc = spawn.getLastSpawn();
			if (npc != null)
			{
				player.sendPacket(new CreatureSay(npc, ChatType.WHISPER, fpcName, message));
			}
		}
	}
	
	public static FakePlayerChatManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FakePlayerChatManager INSTANCE = new FakePlayerChatManager();
	}
}
