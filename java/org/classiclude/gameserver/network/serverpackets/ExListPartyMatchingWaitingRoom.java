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
package org.classiclude.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.ClassId;
import org.classiclude.gameserver.instancemanager.InstanceManager;
import org.classiclude.gameserver.instancemanager.MatchingRoomManager;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.instancezone.Instance;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class ExListPartyMatchingWaitingRoom extends ServerPacket
{
	private static final int NUM_PER_PAGE = 64;
	
	private final int _size;
	private final List<Player> _players = new LinkedList<>();
	
	public ExListPartyMatchingWaitingRoom(int page, int minLevel, int maxLevel, List<ClassId> classIds, String query)
	{
		final List<Player> players = MatchingRoomManager.getInstance().getPlayerInWaitingList(minLevel, maxLevel, classIds, query);
		_size = players.size();
		final int startIndex = (page - 1) * NUM_PER_PAGE;
		int chunkSize = _size - startIndex;
		if (chunkSize > NUM_PER_PAGE)
		{
			chunkSize = NUM_PER_PAGE;
		}
		for (int i = startIndex; i < (startIndex + chunkSize); i++)
		{
			_players.add(players.get(i));
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_LIST_PARTY_MATCHING_WAITING_ROOM.writeId(this, buffer);
		buffer.writeInt(_size);
		buffer.writeInt(_players.size());
		for (Player player : _players)
		{
			buffer.writeString(player.getName());
			buffer.writeInt(player.getClassId().getId());
			buffer.writeInt(player.getLevel());
			final Instance instance = InstanceManager.getInstance().getPlayerInstance(player, false);
			buffer.writeInt((instance != null) && (instance.getTemplateId() >= 0) ? instance.getTemplateId() : -1);
			final Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(player);
			buffer.writeInt(instanceTimes.size());
			for (Entry<Integer, Long> entry : instanceTimes.entrySet())
			{
				final long instanceTime = TimeUnit.MILLISECONDS.toSeconds(entry.getValue() - System.currentTimeMillis());
				buffer.writeInt(entry.getKey());
				buffer.writeInt((int) instanceTime);
			}
		}
	}
}
