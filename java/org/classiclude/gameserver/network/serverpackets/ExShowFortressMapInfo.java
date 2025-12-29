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

import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.instancemanager.FortSiegeManager;
import org.classiclude.gameserver.model.FortSiegeSpawn;
import org.classiclude.gameserver.model.Spawn;
import org.classiclude.gameserver.model.siege.Fort;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * TODO: Rewrite!!!!!!
 * @author KenM
 */
public class ExShowFortressMapInfo extends ServerPacket
{
	private final Fort _fortress;
	
	public ExShowFortressMapInfo(Fort fortress)
	{
		_fortress = fortress;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_FORTRESS_MAP_INFO.writeId(this, buffer);
		buffer.writeInt(_fortress.getResidenceId());
		buffer.writeInt(_fortress.getSiege().isInProgress()); // fortress siege status
		buffer.writeInt(_fortress.getFortSize()); // barracks count
		final List<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortress.getResidenceId());
		if ((commanders != null) && !commanders.isEmpty() && _fortress.getSiege().isInProgress())
		{
			switch (commanders.size())
			{
				case 3:
				{
					for (FortSiegeSpawn spawn : commanders)
					{
						if (isSpawned(spawn.getId()))
						{
							buffer.writeInt(0);
						}
						else
						{
							buffer.writeInt(1);
						}
					}
					break;
				}
				case 4: // TODO: change 4 to 5 once control room supported
				{
					int count = 0;
					for (FortSiegeSpawn spawn : commanders)
					{
						count++;
						if (count == 4)
						{
							buffer.writeInt(1); // TODO: control room emulated
						}
						if (isSpawned(spawn.getId()))
						{
							buffer.writeInt(0);
						}
						else
						{
							buffer.writeInt(1);
						}
					}
					break;
				}
			}
		}
		else
		{
			for (int i = 0; i < _fortress.getFortSize(); i++)
			{
				buffer.writeInt(0);
			}
		}
	}
	
	/**
	 * @param npcId
	 * @return
	 */
	private boolean isSpawned(int npcId)
	{
		boolean ret = false;
		for (Spawn spawn : _fortress.getSiege().getCommanders())
		{
			if (spawn.getId() == npcId)
			{
				ret = true;
				break;
			}
		}
		return ret;
	}
}
