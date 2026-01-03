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
package handlers.communityboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.classiclude.Config;
import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.cache.HtmCache;
import org.classiclude.gameserver.data.sql.ClanTable;
import org.classiclude.gameserver.data.xml.BuyListData;
import org.classiclude.gameserver.data.xml.ExperienceData;
import org.classiclude.gameserver.data.xml.MultisellData;
import org.classiclude.gameserver.data.xml.SkillData;
import org.classiclude.gameserver.handler.CommunityBoardHandler;
import org.classiclude.gameserver.handler.IParseBoardHandler;
import org.classiclude.gameserver.instancemanager.PcCafePointsManager;
import org.classiclude.gameserver.instancemanager.PremiumManager;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.Summon;
import org.classiclude.gameserver.model.actor.instance.Pet;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.zone.ZoneId;
import org.classiclude.gameserver.network.serverpackets.BuyList;
import org.classiclude.gameserver.network.serverpackets.ExBuySellList;
import org.classiclude.gameserver.network.serverpackets.MagicSkillUse;
import org.classiclude.gameserver.network.serverpackets.ShowBoard;


import org.classiclude.gameserver.data.SchemeBufferTable;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Home board.
 * @author Zoey76, Mobius
 */
public class HomeBoard implements IParseBoardHandler
{
	// SQL Queries
	private static final String COUNT_FAVORITES = "SELECT COUNT(*) AS favorites FROM `bbs_favorites` WHERE `playerId`=?";
	private static final String NAVIGATION_PATH = "data/html/CommunityBoard/Custom/navigation.html";
	
	private static final String[] COMMANDS =
	{
		"_bbshome",
		"_bbstop",
	};
	
	private static final String[] CUSTOM_COMMANDS =
	{
		Config.PREMIUM_SYSTEM_ENABLED && Config.COMMUNITY_PREMIUM_SYSTEM_ENABLED ? "_bbspremium" : null,
		Config.COMMUNITYBOARD_ENABLE_MULTISELLS ? "_bbsexcmultisell" : null,
		Config.COMMUNITYBOARD_ENABLE_MULTISELLS ? "_bbsmultisell" : null,
		Config.COMMUNITYBOARD_ENABLE_MULTISELLS ? "_bbssell" : null,
		Config.COMMUNITYBOARD_ENABLE_TELEPORTS ? "_bbsteleport" : null,
		Config.COMMUNITYBOARD_ENABLE_BUFFS ? "_bbsbuff" : null,
		Config.COMMUNITYBOARD_ENABLE_HEAL ? "_bbsheal" : null,
		Config.COMMUNITYBOARD_ENABLE_DELEVEL ? "_bbsdelevel" : null
	};
	
	private static final BiPredicate<String, Player> COMBAT_CHECK = (command, player) ->
	{
		boolean commandCheck = false;
		for (String c : CUSTOM_COMMANDS)
		{
			if ((c != null) && command.startsWith(c))
			{
				commandCheck = true;
				break;
			}
		}
		return commandCheck && (player.isCastingNow() || player.isInCombat() || player.isInDuel() || player.isInOlympiadMode() || player.isInsideZone(ZoneId.SIEGE) || player.isInsideZone(ZoneId.PVP) || (player.getPvpFlag() > 0) || player.isAlikeDead() || player.isOnEvent() || player.isInStoreMode());
	};
	
	private static final Predicate<Player> KARMA_CHECK = player -> Config.COMMUNITYBOARD_KARMA_DISABLED && (player.getReputation() < 0);

	@Override
	public String[] getCommunityBoardCommands()
	{
		final List<String> commands = new ArrayList<>();
		commands.addAll(Arrays.asList(COMMANDS));
		commands.addAll(Arrays.asList(CUSTOM_COMMANDS));
		return commands.stream().filter(Objects::nonNull).toArray(String[]::new);
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		// Old custom conditions check move to here
		if (Config.COMMUNITYBOARD_COMBAT_DISABLED && COMBAT_CHECK.test(command, player))
		{
			player.sendMessage("You can't use the Community Board right now.");
			return false;
		}
		
		if (KARMA_CHECK.test(player))
		{
			player.sendMessage("Players with Karma cannot use the Community Board.");
			return false;
		}
		
		if (Config.COMMUNITYBOARD_PEACE_ONLY && !player.isInsideZone(ZoneId.PEACE))
		{
			player.sendMessage("Community Board cannot be used out of peace zone.");
			return false;
		}
		
		String returnHtml = null;
		final String navigation = org.classiclude.gameserver.community.utils.CommunityBoard.getMenu(player);
		if (command.equals("_bbshome") || command.equals("_bbstop"))
		{
			final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
			CommunityBoardHandler.getInstance().addBypass(player, "Home", command);
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/" + customPath + "home.html");
			if (!Config.CUSTOM_CB_ENABLED)
			{
				returnHtml = returnHtml.replace("%fav_count%", Integer.toString(getFavoriteCount(player)));
				returnHtml = returnHtml.replace("%region_count%", Integer.toString(getRegionCount(player)));
				returnHtml = returnHtml.replace("%clan_count%", Integer.toString(ClanTable.getInstance().getClanCount()));
			}
		}
		else if (command.startsWith("_bbstop;"))
		{
			final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
			final String path = command.replace("_bbstop;", "");
			if ((path.length() > 0) && path.endsWith(".html"))
			{
				returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/" + customPath + path);
			}
		}
		else if (command.startsWith("_bbsmultisell"))
		{
			final String fullBypass = command.replace("_bbsmultisell;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int multisellId = Integer.parseInt(buypassOptions[0]);
			final String page = buypassOptions[1];
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		}
		else if (command.startsWith("_bbsexcmultisell"))
		{
			final String fullBypass = command.replace("_bbsexcmultisell;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int multisellId = Integer.parseInt(buypassOptions[0]);
			final String page = buypassOptions[1];
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, true);
		}
		else if (command.startsWith("_bbssell"))
		{
			final String page = command.replace("_bbssell;", "");
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			player.sendPacket(new BuyList(BuyListData.getInstance().getBuyList(423), player, 0));
			player.sendPacket(new ExBuySellList(player, false));
		}
		else if (command.startsWith("_bbsteleport"))
		{
			final String teleBuypass = command.replace("_bbsteleport;", "");
			if (player.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < Config.COMMUNITYBOARD_TELEPORT_PRICE)
			{
				player.sendMessage("Not enough currency!");
			}
			else if (Config.COMMUNITY_AVAILABLE_TELEPORTS.get(teleBuypass) != null)
			{
				player.disableAllSkills();
				player.sendPacket(new ShowBoard());
				player.destroyItemByItemId("CB_Teleport", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_TELEPORT_PRICE, player, true);
				player.setInstanceById(0);
				player.teleToLocation(Config.COMMUNITY_AVAILABLE_TELEPORTS.get(teleBuypass), 0);
				ThreadPool.schedule(player::enableAllSkills, 3000);
			}
		}
		else if (command.startsWith("_bbsbuff"))
		{
		    showSchemeBuffsWindow(player);
// 			final String fullBypass = command.replace("_bbsbuff;", "");
// 			final String[] buypassOptions = fullBypass.split(";");
// 			final int buffCount = buypassOptions.length - 1;
// 			final String page = buypassOptions[buffCount];
// 			if (player.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < (Config.COMMUNITYBOARD_BUFF_PRICE * buffCount))
// 			{
// 				player.sendMessage("Not enough currency!");
// 			}
// 			else
// 			{
// 				player.destroyItemByItemId("CB_Buff", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_BUFF_PRICE * buffCount, player, true);
// 				final Pet pet = player.getPet();
// 				final List<Creature> targets = new ArrayList<>(4);
// 				targets.add(player);
// 				if (pet != null)
// 				{
// 					targets.add(pet);
// 				}
//
// 				player.getServitors().values().forEach(targets::add);
//
// 				for (int i = 0; i < buffCount; i++)
// 				{
// 					final Skill skill = SkillData.getInstance().getSkill(Integer.parseInt(buypassOptions[i].split(",")[0]), Integer.parseInt(buypassOptions[i].split(",")[1]));
// 					if (!Config.COMMUNITY_AVAILABLE_BUFFS.contains(skill.getId()))
// 					{
// 						continue;
// 					}
// 					for (Creature target : targets)
// 					{
// 						if (skill.isSharedWithSummon() || target.isPlayer())
// 						{
// 							skill.applyEffects(player, target);
// 							if (Config.COMMUNITYBOARD_CAST_ANIMATIONS)
// 							{
// 								player.sendPacket(new MagicSkillUse(player, target, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
// 								// not recommend broadcast
// 								// player.broadcastPacket(new MagicSkillUse(player, target, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
// 							}
// 						}
// 					}
// 				}
// 			}
			
// 			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
		}
		else if (command.startsWith("_bbsheal"))
		{
			final String page = command.replace("_bbsheal;", "");
			if (player.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < (Config.COMMUNITYBOARD_HEAL_PRICE))
			{
				player.sendMessage("Not enough currency!");
			}
			else
			{
				player.destroyItemByItemId("CB_Heal", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_HEAL_PRICE, player, true);
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				if (player.hasPet())
				{
					player.getPet().setCurrentHp(player.getPet().getMaxHp());
					player.getPet().setCurrentMp(player.getPet().getMaxMp());
					player.getPet().setCurrentCp(player.getPet().getMaxCp());
				}
				for (Summon summon : player.getServitors().values())
				{
					summon.setCurrentHp(summon.getMaxHp());
					summon.setCurrentMp(summon.getMaxMp());
					summon.setCurrentCp(summon.getMaxCp());
				}
				player.updateUserInfo();
				player.sendMessage("You used heal!");
			}
			
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
		}
		else if (command.equals("_bbsdelevel"))
		{
			if (player.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < Config.COMMUNITYBOARD_DELEVEL_PRICE)
			{
				player.sendMessage("Not enough currency!");
			}
			else if (player.getLevel() == 1)
			{
				player.sendMessage("You are at minimum level!");
			}
			else
			{
				player.destroyItemByItemId("CB_Delevel", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_DELEVEL_PRICE, player, true);
				final int newLevel = player.getLevel() - 1;
				player.setExp(ExperienceData.getInstance().getExpForLevel(newLevel));
				player.getStat().setLevel((byte) newLevel);
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				player.broadcastUserInfo();
				player.checkPlayerSkills(); // Adjust skills according to new level.
				returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/delevel/complete.html");
			}
		}
		else if (command.startsWith("_bbspremium"))
		{
			final String fullBypass = command.replace("_bbspremium;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int premiumDays = Integer.parseInt(buypassOptions[0]);
			if ((premiumDays < 1) || (premiumDays > 30) || (player.getInventory().getInventoryItemCount(Config.COMMUNITY_PREMIUM_COIN_ID, -1) < (Config.COMMUNITY_PREMIUM_PRICE_PER_DAY * premiumDays)))
			{
				player.sendMessage("Not enough currency!");
			}
			else
			{
				player.destroyItemByItemId("CB_Premium", Config.COMMUNITY_PREMIUM_COIN_ID, Config.COMMUNITY_PREMIUM_PRICE_PER_DAY * premiumDays, player, true);
				PremiumManager.getInstance().addPremiumTime(player.getAccountName(), premiumDays, TimeUnit.DAYS);
				player.sendMessage("Your account will now have premium status until " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(PremiumManager.getInstance().getPremiumExpiration(player.getAccountName())) + ".");
				if (Config.PC_CAFE_RETAIL_LIKE)
				{
					PcCafePointsManager.getInstance().run(player);
				}
				returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/premium/thankyou.html");
			}
		}
		
		if (returnHtml != null)
		{
			if (Config.CUSTOM_CB_ENABLED)
			{
				returnHtml = returnHtml.replace("%navigation%", navigation);
			}
			CommunityBoardHandler.separateAndSend(returnHtml, player);
		}
		return false;
	}
	/** scheme buffer part here */

	/**
    	 * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targetType parameter {player, pet}
    	 * @param player : The player to make checks on.
    	 */
    	private void showSchemeBuffsWindow(Player player)
    	{
//     		final StringBuilder sb = new StringBuilder(200);
//     		final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
//     		if ((schemes == null) || schemes.isEmpty())
//     		{
//     			sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
//     		}
//     		else
//     		{
//     			for (Entry<String, List<Integer>> scheme : schemes.entrySet())
//     			{
//                     final int count = scheme.getValue().size();
//     				final int cost = org.classiclude.gameserver.model.actor.instance.SchemeBuffer.getFee(scheme.getValue());
//     				final String costText = (cost > 0) ? " - cost: " + NumberFormat.getInstance(Locale.ENGLISH).format(cost) : "";
//
//     				sb.append("<table width=280 cellpadding=0 cellspacing=0>");
//     				sb.append("<tr><td height=10></td></tr>");
//     				sb.append("<tr><td align=center>");
//     				sb.append("<table cellpadding=0 cellspacing=0><tr><td height=8></td></tr></table>");
//     				sb.append("<table cellpadding=0 cellspacing=0><tr><td fixwidth=202 align=left><font color=\"e5d0a5\">" + scheme.getKey() + costText + "</font></td></tr></table>");
//     				sb.append("<table><tr>");
//     				sb.append("<td fixwidth=2></td>");
//     				sb.append("<td fixwidth=22 align=left><a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + "\"><font color=\"b3a382\">Use</font></a></td>");
//     				sb.append("<td fixwidth=3>|</td>");
//     				sb.append("<td fixwidth=57 align=left><a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + ";pet\"><font color=\"b3a382\">Use on Pet</font></a></td>");
//     				sb.append("<td fixwidth=3>|</td>");
//     				sb.append("<td fixwidth=23 align=left><a action=\"bypass -h npc_%objectId%_editschemes;Buffs;" + scheme.getKey() + ";1\"><font color=\"b3a382\">Edit</font></a></td>");
//     				sb.append("<td fixwidth=3>|</td>");
//     				sb.append("<td fixwidth=34 align=left><a action=\"bypass -h npc_%objectId%_deletescheme;" + scheme.getKey() + "\"><font color=\"b3a382\">Delete</font></a></td>");
//     				sb.append("<td fixwidth=35></td>");
//     				sb.append("</tr></table></td>");
//     				sb.append("<td align=center>");
//     				sb.append("<table cellpadding=0 cellspacing=0><tr><td height=17></td></tr></table>");
//     				sb.append("<table cellpadding=0 cellspacing=0><tr><td fixwidth=60 align=center>" + count + " <font color=\"LEVEL\">Skill(s)</font></td></tr></table>");
//     				sb.append("</td></tr>");
//     				sb.append("<tr><td height=18></td></tr>");
//     				sb.append("</table>");
//                     sb.append("<center><br><img src=\"l2ui.squaregray\" width=\"300\" height=\"1\" /></center><br>");
//     			}
//     		}

			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/scheme.html");

//     		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
//     		html.setFile(player, getHtmlPath(getId(), 1, player));
//     		html.replace("%schemes%", sb.toString());
//     		html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
//     		html.replace("%objectId%", getObjectId());
//     		player.sendPacket(html);
    	}





	/**
	 * Gets the Favorite links for the given player.
	 * @param player the player
	 * @return the favorite links count
	 */
	private static int getFavoriteCount(Player player)
	{
		int count = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(COUNT_FAVORITES))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					count = rs.getInt("favorites");
				}
			}
		}
		catch (Exception e)
		{
			LOG.warning(FavoriteBoard.class.getSimpleName() + ": Coudn't load favorites count for " + player);
		}
		return count;
	}
	
	/**
	 * Gets the registered regions count for the given player.
	 * @param player the player
	 * @return the registered regions count
	 */
	private static int getRegionCount(Player player)
	{
		return 0; // TODO: Implement.
	}
}
