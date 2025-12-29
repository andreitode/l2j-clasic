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
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Maktakien
 */
public class VehicleCheckLocation extends ServerPacket
{
	private final Creature _boat;
	
	public VehicleCheckLocation(Creature boat)
	{
		_boat = boat;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.VEHICLE_CHECK_LOCATION.writeId(this, buffer);
		buffer.writeInt(_boat.getObjectId());
		buffer.writeInt(_boat.getX());
		buffer.writeInt(_boat.getY());
		buffer.writeInt(_boat.getZ());
		buffer.writeInt(_boat.getHeading());
	}
}
