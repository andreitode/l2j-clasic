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
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.classiclude.gameserver.util.Util;
import org.classiclude.gameserver.util.MathUtil;
import org.classiclude.gameserver.model.actor.instance.SchemeBuffer;

import org.classiclude.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import org.classiclude.gameserver.network.serverpackets.ExShowVariationMakeWindow;

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
		Config.COMMUNITYBOARD_ENABLE_AUGMENT ? "_bbsaugment" : null,
	};

	private static final int PAGE_LIMIT = 6;


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
    	String[] args = null;
        String baseCommand = command;

        // Split commands that use parameters (;)
        if (command.contains(";")) {
            args = command.split(";");
            baseCommand = args[0];
        }
	    player.sendMessage(command);
        player.sendMessage(baseCommand);
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

        if (baseCommand.equals("_bbshome"))
		{
			player.sendMessage("aici intra in primul if, makes sense");
			final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
			CommunityBoardHandler.getInstance().addBypass(player, "Home", command);
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/" + customPath + "home.html");
			if (!Config.CUSTOM_CB_ENABLED)
			{
				returnHtml = returnHtml.replace("%fav_count%", Integer.toString(getFavoriteCount(player)));
				returnHtml = returnHtml.replace("%region_count%", Integer.toString(getRegionCount(player)));
				returnHtml = returnHtml.replace("%clan_count%", Integer.toString(ClanTable.getInstance().getClanCount()));
			}
		} else if (baseCommand.equals("_bbstop")) {
            player.sendMessage("aici intra in if-ul de la bbstop ca sa schimbe navigatia");
            final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
            final String path = command.replace("_bbstop;", "");

            if ((path.length() > 0) && path.endsWith(".html"))
            {
                returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/" + customPath + path);
            }
		} else if (baseCommand.equals("_bbsmultisell")) {
            player.sendMessage("aici intra in multisell, asta e practic merchant-ul");

            final String fullBypass = command.replace("_bbsmultisell;", "");
            final String[] buypassOptions = fullBypass.split(",");
            final int multisellId = Integer.parseInt(buypassOptions[0]);
            final String page = buypassOptions[1];
            returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
            MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		} else if (baseCommand.equals("_bbssell")) {
            player.sendMessage("aici intra in sell, tab-ul de sell de la merchant");

            final String page = command.replace("_bbssell;", "");
            returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			player.sendPacket(new ExBuySellList(player, false));
		} else if (baseCommand.equals("_bbsteleport")) {
              player.sendMessage("intra in gatekeeper");

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
        } else if (baseCommand.equals("_bbsbuffscheme")) {
            CommunityBoardHandler.separateAndSend(getBuffsSchemes(player), player);
        } else if (baseCommand.equals("_bbsbuffschemecreate")) {

            player.sendMessage("line 257 a intrat aici");
            try
            {
                final String schemeName = command.replace("_bbsbuffschemecreate; ", "");
                if (schemeName.length() > 14)
                {
                    player.sendMessage("Scheme's name must contain up to 14 chars.");
                    return false;
                }
                // Simple hack to use spaces, dots, commas, minus, plus, exclamations or question marks.
                if (!Util.isAlphaNumeric(schemeName.replace(" ", "").replace(".", "").replace(",", "").replace("-", "").replace("+", "").replace("!", "").replace("?", "")))
                {
                    player.sendMessage("Please use plain alphanumeric characters.");
                    return false;
                }

                final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
                if (schemes != null)
                {
                    if (schemes.size() == Config.BUFFER_MAX_SCHEMES)
                    {
                        player.sendMessage("Maximum schemes amount is already reached.");
                        return false;
                    }

                    if (schemes.containsKey(schemeName))
                    {
                        player.sendMessage("The scheme name already exists.");
                        return false;
                    }
                }

                SchemeBufferTable.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<>());
            }
            catch (Exception e)
            {
                player.sendMessage("Scheme's name must contain up to 14 chars.");
            }
            CommunityBoardHandler.separateAndSend(getBuffsSchemes(player), player);
        } else if (baseCommand.equals("_bbsbuffsedit")) {
            final String sentParams = command.replace("_bbsbuffsedit;", "");

            player.sendMessage("here comes edit button");
			final String[] params = sentParams.split(";");

            player.sendMessage(String.valueOf(params[0]));
            player.sendMessage(String.valueOf(params[1]));
            player.sendMessage(String.valueOf(params[2]));

            CommunityBoardHandler.separateAndSend(handleEdit(player, String.valueOf(params[0]), String.valueOf(params[1]), Integer.parseInt(params[2])), player);
        } else if (baseCommand.equals("_bbsbuffskilledit")) {
            final String sentParams = command.replace("_bbsbuffskilledit;", "");
            player.sendMessage("here comes edit skill");
        	final String[] params = sentParams.split(";");

        	player.sendMessage(String.valueOf(params[0]));
            player.sendMessage(String.valueOf(params[1]));
            player.sendMessage(String.valueOf(params[2]));


            CommunityBoardHandler.separateAndSend(
            handleEditScheme(player,
            String.valueOf(params[0]),
            String.valueOf(params[1]),
            Integer.parseInt(params[3]),
            String.valueOf(params[2]),
            Integer.parseInt(params[4])
            ),
            player);
        } else if (baseCommand.equals("_bbsbuffschemedelete")) {
            try
            {
                final String schemeName = command.replace("_bbsbuffschemedelete;", "");
                final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
                if ((schemes != null) && schemes.containsKey(schemeName))
                {
                    schemes.remove(schemeName);
                }

            }
            catch (Exception e)
            {
                player.sendMessage("This scheme name is invalid.");
            }
            CommunityBoardHandler.separateAndSend(getBuffsSchemes(player), player);
        } else if (baseCommand.equals("_bbsheal")) {
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
        } else if (baseCommand.equals("_bbsbuffsgive")) {
            player.sendMessage(String.valueOf(command));
            final String sentParams = command.replace("_bbsbuffsgive;", "");

            player.sendMessage("here comes edit button");
			final String[] params = sentParams.split(";");
			player.sendMessage(String.valueOf(params[0]));
			player.sendMessage(String.valueOf(params[1]));
			player.sendMessage(String.valueOf(params[2]));
            CommunityBoardHandler.separateAndSend(
            handleBuffsGive(player,
            String.valueOf(params[0]),
            Integer.parseInt(params[1]),
            String.valueOf(params[2])
            ), player);
        } else if (baseCommand.equals("_bbsbuffsclean")) {
            CommunityBoardHandler.separateAndSend(handleCleanup(player), player);
        } else if (baseCommand.equals("_bbspremium")) {
            player.sendMessage("premium entry");
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
        } else if (baseCommand.equals("_bbsaugment")) {
            player.sendMessage("augment page");
            final String option = command.replace("_bbsaugment;", "");
            player.sendMessage(String.valueOf(option));

            if (option.equals("1")) {
                player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
            } else {
                player.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
            }
            returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/augment/main.html");
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

    private static String handleEdit(Player player, String groupType, String schemeName, int page)
    {
        String returnHtml = null;
        final String navigation = org.classiclude.gameserver.community.utils.CommunityBoard.getMenu(player);
		final List<Integer> schemeSkills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);


        returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/buffer/edit.html");
		returnHtml = returnHtml.replace("%schemename%", schemeName);
		returnHtml = returnHtml.replace("%count%", getCountOf(schemeSkills, false) + " / " + player.getStat().getMaxBuffCount() + " buffs, " + getCountOf(schemeSkills, true) + " / " + Config.DANCES_MAX_AMOUNT + " dances/songs");
		returnHtml = returnHtml.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page));
        returnHtml = returnHtml.replace("%typesframe%", getTypesFrame(groupType, schemeName));

        returnHtml = returnHtml.replace("%navigation%", navigation);

        return returnHtml;
    }



    private static String handleBuffsGive(Player player, String schemeName, int cost, String buffSummons)
    {
        player.sendMessage("line 379 logs");
        player.sendMessage(schemeName);
        player.sendMessage(buffSummons);
        if (buffSummons.equals("pet") && (player.getPet() == null) && !player.hasServitors())
        {
            player.sendMessage("You don't have a pet.");
        }
        else if (
        (cost == 0) ||
        ((Config.BUFFER_ITEM_ID == 57) &&
         player.reduceAdena("Community Board Buffer", cost, player, true)) ||
         ((Config.BUFFER_ITEM_ID != 57) &&
         player.destroyItemByItemId("Community Board Buffer", Config.BUFFER_ITEM_ID, cost, player, true)))
        {
            for (int skillId : SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName))
            {
                final Skill skill = SkillData.getInstance().getSkill(skillId, SchemeBufferTable.getInstance().getAvailableBuff(skillId).getLevel());
                if (buffSummons.equals("pet"))
                {
                    if (player.getPet() != null)
                    {
                        skill.applyEffects(player, player.getPet());
                    }
                    player.getServitors().values().forEach(servitor -> skill.applyEffects(player, servitor));
                }
                else
                {
                    skill.applyEffects(player, player);
                }
            }
        }

        return getBuffsSchemes(player);
    }

    /**
     * @param player : The player to make checks on.
     * @param groupType : The group of skills to select.
     * @param schemeName : The scheme to make check.
     * @param pageValue The page.
     * @return a String representing skills available to selection for a given groupType.
     */
    private static String getGroupSkillList(Player player, String groupType, String schemeName, int pageValue)
    {
        // Retrieve the entire skills list based on group type.
        List<Integer> skills = SchemeBufferTable.getInstance().getSkillsIdsByType(groupType);
        if (skills.isEmpty())
        {
            return "That group doesn't contain any skills.";
        }

        // Calculate page number.
        final int max = MathUtil.countPagesNumber(skills.size(), PAGE_LIMIT);
        int page = pageValue;
        if (page > max)
        {
            page = max;
        }

        // Cut skills list up to page number.
        skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size()));

        final List<Integer> schemeSkills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
        final StringBuilder sb = new StringBuilder(skills.size() * 150);
        int row = 0;
        for (int skillId : skills)
        {
            sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));

            final Skill skill = SkillData.getInstance().getSkill(skillId, 1);
            if (schemeSkills.contains(skillId))
            {
                sb.append("<td height=40 width=60><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button value=\" \" action=\"bypass _bbsbuffskilledit;remove;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
            }
            else
            {
                sb.append("<td height=40 width=60><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button value=\" \" action=\"bypass _bbsbuffskilledit;add;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
            }

            sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
            row++;
        }

        // Build page footer.
        sb.append("<br><img src=\"L2UI.SquareGray\" width=400 height=1><table width=\"100%\" bgcolor=000000><tr>");
        if (page > 1)
        {
            sb.append("<td align=left width=70><a action=\"bypass _bbsbuffsedit;" + groupType + ";" + schemeName + ";" + (page - 1) + "\"><font color=\"b3a382\">Previous</font></a></td>");
        }
        else
        {
            sb.append("<td align=left width=70>Previous</td>");
        }

        sb.append("<td align=center width=100>Page " + page + "</td>");
        if (page < max)
        {
            sb.append("<td align=right width=70><a action=\"bypass _bbsbuffsedit;" + groupType + ";" + schemeName + ";" + (page + 1) + "\"><font color=\"b3a382\">Next</font></a></td>");
        }
        else
        {
            sb.append("<td align=right width=70>Next</td>");
        }

        sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=400 height=1>");
        return sb.toString();
    }

    /**
     * @param groupType : The group of skills to select.
     * @param schemeName : The scheme to make check.
     * @return a string representing all groupTypes available. The group currently on selection isn't linkable.
     */
    private static String getTypesFrame(String groupType, String schemeName)
    {
        final StringBuilder sb = new StringBuilder(500);
        sb.append("<table>");

        int count = 0;
        for (String type : SchemeBufferTable.getInstance().getSkillTypes())
        {
            if (count == 0)
            {
                sb.append("<tr>");
            }

            if (groupType.equalsIgnoreCase(type))
            {
                sb.append("<td><button value=" + type + "  width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
            }
            else
            {
                sb.append("<td><button value=" + type + " action=\"bypass _bbsbuffsedit;" + type + ";" + schemeName + ";1\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
            }

            count++;
            if (count == 4)
            {
                sb.append("</tr>");
                count = 0;
            }
        }

        if (!sb.toString().endsWith("</tr>"))
        {
            sb.append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }

    private static int getCountOf(List<Integer> skills, boolean dances)
	{
		int count = 0;
		for (int skillId : skills)
		{
			if (SkillData.getInstance().getSkill(skillId, 1).isDance() == dances)
			{
				count++;
			}
		}
		return count;
	}

    private static String handleCleanup(Player player)
    {
        String returnHtml = null;
        final String navigation = org.classiclude.gameserver.community.utils.CommunityBoard.getMenu(player);
        player.stopAllEffects();

        final Summon summon = player.getPet();
        if (summon != null)
        {
            summon.stopAllEffects();
        }
        player.getServitors().values().forEach(servitor -> servitor.stopAllEffects());

        returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/buffer/main.html");
        returnHtml = returnHtml.replace("%navigation%", navigation);
        return returnHtml;
    }

    private static String handleEditScheme(Player player, String direction, String groupType, int skillId, String schemeName, int page)
    {
        final List<Integer> skills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
        if (direction.equals("add") && !schemeName.equalsIgnoreCase("none"))
        {
            final Skill skill = SkillData.getInstance().getSkill(skillId, SkillData.getInstance().getMaxLevel(skillId));
            if (skill.isDance())
            {
                if (getCountOf(skills, true) < Config.DANCES_MAX_AMOUNT)
                {
                    skills.add(skillId);
                }
                else
                {
                    player.sendMessage("This scheme has reached the maximum amount of dances/songs.");
                }
            }
            else
            {
                if (getCountOf(skills, false) < player.getStat().getMaxBuffCount())
                {
                    skills.add(skillId);
                }
                else
                {
                    player.sendMessage("This scheme has reached the maximum amount of buffs.");
                }
            }
        }
        else
        {
            skills.remove(Integer.valueOf(skillId));
        }

        return handleEdit(player, groupType, schemeName, page);
    }

    private static String getBuffsSchemes(Player player)
    {
    	String returnHtml = null;
        final StringBuilder sb = new StringBuilder(200);
        final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
        final String navigation = org.classiclude.gameserver.community.utils.CommunityBoard.getMenu(player);
        if ((schemes == null) || schemes.isEmpty())
        {
            sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
        }
        else
        {
            player.sendMessage("line 244");

            for (Entry<String, List<Integer>> scheme : schemes.entrySet())
            {
                final int count = scheme.getValue().size();
                final int cost = getFee(scheme.getValue());
                final String costText = (cost > 0) ? " - cost: " + NumberFormat.getInstance(Locale.ENGLISH).format(cost) : "";

                sb.append("<table width=280 cellpadding=0 cellspacing=0>");
                sb.append("<tr><td height=10></td></tr>");
                sb.append("<tr><td align=center>");
                sb.append("<table cellpadding=0 cellspacing=0><tr><td height=8></td></tr></table>");
                sb.append("<table cellpadding=0 cellspacing=0><tr><td fixwidth=202 align=left><font color=\"e5d0a5\">" + scheme.getKey() + costText + "</font></td></tr></table>");
                sb.append("<table><tr>");
                sb.append("<td fixwidth=2></td>");
                sb.append("<td fixwidth=22 align=left><a action=\"bypass _bbsbuffsgive;" + scheme.getKey() + ";" + cost + ";none\"><font color=\"b3a382\">Use</font></a></td>");
                sb.append("<td fixwidth=3>|</td>");
                sb.append("<td fixwidth=57 align=left><a action=\"bypass _bbsbuffsgive;" + scheme.getKey() + ";" + cost + ";pet\"><font color=\"b3a382\">Use on Pet</font></a></td>");
                sb.append("<td fixwidth=3>|</td>");
                sb.append("<td fixwidth=23 align=left><a action=\"bypass _bbsbuffsedit;Buffs;" + scheme.getKey() + ";1\"><font color=\"b3a382\">Edit</font></a></td>");
                sb.append("<td fixwidth=3>|</td>");
                sb.append("<td fixwidth=34 align=left><a action=\"bypass _bbsbuffschemedelete;" + scheme.getKey() + "\"><font color=\"b3a382\">Delete</font></a></td>");
                sb.append("<td fixwidth=35></td>");
                sb.append("</tr></table></td>");
                sb.append("<td align=center>");
                sb.append("<table cellpadding=0 cellspacing=0><tr><td height=17></td></tr></table>");
                sb.append("<table cellpadding=0 cellspacing=0><tr><td fixwidth=60 align=center>" + count + " <font color=\"LEVEL\">Skill(s)</font></td></tr></table>");
                sb.append("</td></tr>");
                sb.append("<tr><td height=18></td></tr>");
                sb.append("</table>");
                sb.append("<center><br><img src=\"l2ui.squaregray\" width=\"300\" height=\"1\" /></center><br>");
            }
        }
        returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/buffer/scheme.html");
        returnHtml = returnHtml.replace("%schemes%", sb.toString());
        returnHtml = returnHtml.replace("%max_schemes%", String.valueOf(Config.BUFFER_MAX_SCHEMES));
        returnHtml = returnHtml.replace("%navigation%", navigation);
        return returnHtml;
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
	 * @param list : A list of skill ids.
	 * @return a global fee for all skills contained in list.
	 */
	private static int getFee(List<Integer> list)
	{
		if (Config.BUFFER_STATIC_BUFF_COST > 0)
		{
			return list.size() * Config.BUFFER_STATIC_BUFF_COST;
		}

		int fee = 0;
		for (int sk : list)
		{
			fee += SchemeBufferTable.getInstance().getAvailableBuff(sk).getPrice();
		}

		return fee;
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
