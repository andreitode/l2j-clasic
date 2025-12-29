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
package org.classiclude.gameserver.network.serverpackets;

import org.classiclude.Config;
import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author UnAfraid
 */
public class ExWorldChatCnt extends ServerPacket
{
	private final int _points;
	
	public ExWorldChatCnt(Player player)
	{
		_points = (player.getLevel() < Config.WORLD_CHAT_MIN_LEVEL) || (Config.VIP_SYSTEM_ENABLED && (player.getVipTier() <= 0)) ? 0 : Math.max(player.getWorldChatPoints() - player.getWorldChatUsed(), 0);
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_CHAT_CNT.writeId(this, buffer);
		buffer.writeInt(_points);
	}
}
