package org.classiclude.gameserver.util;

import java.util.logging.Logger;

import org.classiclude.Config;
import org.classiclude.commons.util.Rnd;
import org.classiclude.gameserver.data.sql.OfflineTraderTable;
import org.classiclude.gameserver.instancemanager.AntiFeedManager;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.Summon;
import org.classiclude.gameserver.model.olympiad.OlympiadManager;
import org.classiclude.gameserver.model.zone.ZoneId;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.serverpackets.ServerClose;

/**
 * @author lord_rex
 */
public class OfflineTradeUtil
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	private OfflineTradeUtil()
	{
		// utility class
	}
	
	/**
	 * Check whether player is able to enter offline mode.
	 * @param player the player to be check.
	 * @return {@code true} if the player is allowed to remain as off-line shop.
	 */
	private static boolean offlineMode(Player player)
	{
		if ((player == null) || player.isInOlympiadMode() || player.isRegisteredOnEvent() || player.isJailed() || (player.getVehicle() != null))
		{
			return false;
		}
		
		boolean canSetShop = false;
		switch (player.getPrivateStoreType())
		{
			case SELL:
			case PACKAGE_SELL:
			case BUY:
			{
				canSetShop = Config.OFFLINE_TRADE_ENABLE;
				break;
			}
			case MANUFACTURE:
			{
				canSetShop = Config.OFFLINE_TRADE_ENABLE;
				break;
			}
			default:
			{
				canSetShop = Config.OFFLINE_CRAFT_ENABLE && player.isCrafting();
				break;
			}
		}
		
		if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE))
		{
			canSetShop = false;
		}
		
		// Check whether client is null or player is already in offline mode.
		final GameClient client = player.getClient();
		if ((client == null) || client.isDetached())
		{
			return false;
		}
		
		return canSetShop;
	}
	
	/**
	 * Manages the disconnection process of offline traders.
	 * @param player
	 * @return {@code true} when player entered offline mode, otherwise {@code false}
	 */
	public static boolean enteredOfflineMode(Player player)
	{
		if (!offlineMode(player))
		{
			return false;
		}
		
		World.OFFLINE_TRADE_COUNT++;
		
		final GameClient client = player.getClient();
		client.close(ServerClose.STATIC_PACKET);
		if (!Config.DUALBOX_COUNT_OFFLINE_TRADERS)
		{
			AntiFeedManager.getInstance().onDisconnect(client);
		}
		client.setDetached(true);
		
		player.leaveParty();
		OlympiadManager.getInstance().unRegisterNoble(player);
		
		// If the Player has Pet, unsummon it
		Summon pet = player.getPet();
		if (pet != null)
		{
			pet.setRestoreSummon(true);
			pet.unSummon(player);
			pet = player.getPet();
			
			// Dead pet wasn't unsummoned, broadcast npcinfo changes (pet will be without owner name - means owner offline)
			if (pet != null)
			{
				pet.broadcastNpcInfo(0);
			}
		}
		
		player.getServitors().values().forEach(s ->
		{
			s.setRestoreSummon(true);
			s.unSummon(player);
		});
		
		if (Config.OFFLINE_SET_NAME_COLOR)
		{
			player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
			player.broadcastUserInfo();
		}
		
		if (player.getOfflineStartTime() == 0)
		{
			player.setOfflineStartTime(System.currentTimeMillis());
		}
		
		// Store trade on exit, if realtime saving is enabled.
		if (Config.STORE_OFFLINE_TRADE_IN_REALTIME)
		{
			OfflineTraderTable.getInstance().onTransaction(player, false, true);
		}
		
		player.storeMe();
		LOGGER_ACCOUNTING.info("Entering offline mode, " + client);
		
		if (!Config.OFFLINE_ABNORMAL_EFFECTS.isEmpty())
		{
			player.getEffectList().startAbnormalVisualEffect(Config.OFFLINE_ABNORMAL_EFFECTS.get(Rnd.get(Config.OFFLINE_ABNORMAL_EFFECTS.size())));
		}
		
		return true;
	}
}
