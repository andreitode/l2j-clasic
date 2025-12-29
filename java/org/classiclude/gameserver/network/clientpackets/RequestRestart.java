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

import java.util.logging.Logger;

import org.classiclude.Config;
import org.classiclude.gameserver.enums.TeleportWhereType;
import org.classiclude.gameserver.instancemanager.MapRegionManager;
import org.classiclude.gameserver.model.Location;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.instancezone.Instance;
import org.classiclude.gameserver.model.olympiad.OlympiadManager;
import org.classiclude.gameserver.model.variables.PlayerVariables;
import org.classiclude.gameserver.network.ConnectionState;
import org.classiclude.gameserver.network.Disconnection;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.serverpackets.ActionFailed;
import org.classiclude.gameserver.network.serverpackets.CharSelectionInfo;
import org.classiclude.gameserver.network.serverpackets.RestartResponse;
import org.classiclude.gameserver.util.OfflineTradeUtil;

/**
 * @author Mobius
 */
public class RequestRestart extends ClientPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.canLogout())
		{
			player.sendPacket(RestartResponse.FALSE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Remove player from boss zone.
		player.removeFromBossZone();
		
		// Unregister from olympiad.
		if (OlympiadManager.getInstance().isRegistered(player))
		{
			OlympiadManager.getInstance().unRegisterNoble(player);
		}
		
		final Instance world = player.getInstanceWorld();
		if (world != null)
		{
			if (Config.RESTORE_PLAYER_INSTANCE)
			{
				player.getVariables().set(PlayerVariables.INSTANCE_RESTORE, world.getId());
			}
			else
			{
				Location location = world.getExitLocation(player);
				if (location == null)
				{
					location = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
				}
				player.getVariables().set(PlayerVariables.RESTORE_LOCATION, location.getX() + ";" + location.getY() + ";" + location.getZ());
			}
			player.setInstance(null);
		}
		
		final GameClient client = getClient();
		LOGGER_ACCOUNTING.info("Logged out, " + client);
		
		if (!OfflineTradeUtil.enteredOfflineMode(player))
		{
			Disconnection.of(client, player).storeMe().deleteMe();
		}
		
		// Return the client to the authenticated status.
		client.setConnectionState(ConnectionState.AUTHENTICATED);
		
		client.sendPacket(RestartResponse.TRUE);
		
		// Send character list.
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.sendPacket(new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1));
		client.setCharSelection(cl.getCharInfo());
	}
}
