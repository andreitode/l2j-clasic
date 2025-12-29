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
public class ExFishingStartCombat extends ServerPacket
{
	private final Player _player;
	private final int _time;
	private final int _hp;
	private final int _lureType;
	private final int _deceptiveMode;
	private final int _mode;
	
	public ExFishingStartCombat(Player player, int time, int hp, int mode, int lureType, int deceptiveMode)
	{
		_player = player;
		_time = time;
		_hp = hp;
		_mode = mode;
		_lureType = lureType;
		_deceptiveMode = deceptiveMode;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_FISHING_START_COMBAT.writeId(this, buffer);
		buffer.writeInt(_player.getObjectId());
		buffer.writeInt(_time);
		buffer.writeInt(_hp);
		buffer.writeByte(_mode); // mode: 0 = resting, 1 = fighting
		buffer.writeByte(_lureType); // 0 = newbie lure, 1 = normal lure, 2 = night lure
		buffer.writeByte(_deceptiveMode); // Fish Deceptive Mode: 0 = no, 1 = yes
	}
}