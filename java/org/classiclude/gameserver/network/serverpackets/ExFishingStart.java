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

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author -Wooden-
 */
public class ExFishingStart extends ServerPacket
{
	private final Player _player;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _fishType;
	private final boolean _isNightLure;
	
	public ExFishingStart(Player player, int fishType, int x, int y, int z, boolean isNightLure)
	{
		_player = player;
		_fishType = fishType;
		_x = x;
		_y = y;
		_z = z;
		_isNightLure = isNightLure;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FISHING_START.writeId(this, buffer);
		buffer.writeInt(_player.getObjectId());
		buffer.writeInt(_fishType); // fish type
		buffer.writeInt(_x); // x position
		buffer.writeInt(_y); // y position
		buffer.writeInt(_z); // z position
		buffer.writeByte(_isNightLure); // night lure
		buffer.writeByte(0); // show fish rank result button
	}
}