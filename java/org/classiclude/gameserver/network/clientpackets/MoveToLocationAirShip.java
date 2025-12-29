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
package org.classiclude.gameserver.network.clientpackets;

import org.classiclude.gameserver.ai.CtrlIntention;
import org.classiclude.gameserver.instancemanager.AirShipManager;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.VehiclePathPoint;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.instance.AirShip;
import org.classiclude.gameserver.network.SystemMessageId;

public class MoveToLocationAirShip extends ClientPacket
{
	public static final int MIN_Z = -895;
	public static final int MAX_Z = 6105;
	public static final int STEP = 300;
	
	private int _command;
	private int _param1;
	private int _param2 = 0;
	
	@Override
	protected void readImpl()
	{
		_command = readInt();
		_param1 = readInt();
		if (remaining() > 0)
		{
			_param2 = readInt();
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.isInAirShip())
		{
			return;
		}
		
		final AirShip ship = player.getAirShip();
		if (!ship.isCaptain(player))
		{
			return;
		}
		
		int z = ship.getZ();
		
		switch (_command)
		{
			case 0:
			{
				if (!ship.canBeControlled())
				{
					return;
				}
				if (_param1 < World.GRACIA_MAX_X)
				{
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_param1, _param2, z));
				}
				break;
			}
			case 1:
			{
				if (!ship.canBeControlled())
				{
					return;
				}
				ship.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			}
			case 2:
			{
				if (!ship.canBeControlled())
				{
					return;
				}
				if (z < World.GRACIA_MAX_Z)
				{
					z = Math.min(z + STEP, World.GRACIA_MAX_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(ship.getX(), ship.getY(), z));
				}
				break;
			}
			case 3:
			{
				if (!ship.canBeControlled())
				{
					return;
				}
				if (z > World.GRACIA_MIN_Z)
				{
					z = Math.max(z - STEP, World.GRACIA_MIN_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(ship.getX(), ship.getY(), z));
				}
				break;
			}
			case 4:
			{
				if (!ship.isInDock() || ship.isMoving())
				{
					return;
				}
				
				final VehiclePathPoint[] dst = AirShipManager.getInstance().getTeleportDestination(ship.getDockId(), _param1);
				if (dst == null)
				{
					return;
				}
				
				// Consume fuel, if needed
				final int fuelConsumption = AirShipManager.getInstance().getFuelConsumption(ship.getDockId(), _param1);
				if (fuelConsumption > 0)
				{
					if (fuelConsumption > ship.getFuel())
					{
						player.sendPacket(SystemMessageId.YOUR_AIRSHIP_CANNOT_TELEPORT_BECAUSE_DUE_TO_LOW_FUEL);
						return;
					}
					ship.setFuel(ship.getFuel() - fuelConsumption);
				}
				
				ship.executePath(dst);
				break;
			}
		}
	}
}
