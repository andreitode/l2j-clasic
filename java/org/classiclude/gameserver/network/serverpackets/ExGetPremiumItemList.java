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

import java.util.Map;
import java.util.Map.Entry;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.PremiumItem;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class ExGetPremiumItemList extends ServerPacket
{
	private final Player _player;
	private final Map<Integer, PremiumItem> _map;
	
	public ExGetPremiumItemList(Player player)
	{
		_player = player;
		_map = _player.getPremiumItemList();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GET_PREMIUM_ITEM_LIST.writeId(this, buffer);
		buffer.writeInt(_map.size());
		for (Entry<Integer, PremiumItem> entry : _map.entrySet())
		{
			final PremiumItem item = entry.getValue();
			buffer.writeLong(entry.getKey());
			buffer.writeInt(item.getItemId());
			buffer.writeLong(item.getCount());
			buffer.writeInt(0); // ?
			buffer.writeString(item.getSender());
		}
	}
}
