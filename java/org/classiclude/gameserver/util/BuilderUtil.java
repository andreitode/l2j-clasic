package org.classiclude.gameserver.util;

import org.classiclude.Config;
import org.classiclude.gameserver.data.xml.SendMessageLocalisationData;
import org.classiclude.gameserver.enums.ChatType;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.serverpackets.CreatureSay;
import org.classiclude.gameserver.network.serverpackets.ExUserInfoAbnormalVisualEffect;

/**
 * @author lord_rex
 */
public class BuilderUtil
{
	private BuilderUtil()
	{
		// utility class
	}
	
	/**
	 * Sends builder system message to the player.
	 * @param player
	 * @param message
	 */
	public static void sendSysMessage(Player player, String message)
	{
		if (Config.GM_STARTUP_BUILDER_HIDE)
		{
			player.sendPacket(new CreatureSay(null, ChatType.GENERAL, "SYS", SendMessageLocalisationData.getLocalisation(player, message)));
		}
		else
		{
			player.sendMessage(message);
		}
	}
	
	/**
	 * Sends builder html message to the player.
	 * @param player
	 * @param message
	 */
	public static void sendHtmlMessage(Player player, String message)
	{
		player.sendPacket(new CreatureSay(null, ChatType.GENERAL, "HTML", message));
	}
	
	/**
	 * Changes player's hiding state.
	 * @param player
	 * @param hide
	 * @return {@code true} if hide state was changed, otherwise {@code false}
	 */
	public static boolean setHiding(Player player, boolean hide)
	{
		if (player.hasEnteredWorld())
		{
			if (player.isInvisible() && hide)
			{
				// already hiding
				return false;
			}
			
			if (!player.isInvisible() && !hide)
			{
				// already visible
				return false;
			}
		}
		
		player.setSilenceMode(hide);
		player.setInvul(hide);
		player.setInvisible(hide);
		
		player.broadcastUserInfo();
		player.sendPacket(new ExUserInfoAbnormalVisualEffect(player));
		return true;
	}
}
