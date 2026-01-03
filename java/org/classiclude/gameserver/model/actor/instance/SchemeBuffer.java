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
package org.classiclude.gameserver.model.actor.instance;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.classiclude.Config;
import org.classiclude.gameserver.data.SchemeBufferTable;
import org.classiclude.gameserver.data.xml.SkillData;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.Summon;
import org.classiclude.gameserver.model.actor.templates.NpcTemplate;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.serverpackets.NpcHtmlMessage;
import org.classiclude.gameserver.util.MathUtil;
import org.classiclude.gameserver.util.Util;

public class SchemeBuffer extends Npc
{
	private static final int PAGE_LIMIT = 6;

	public SchemeBuffer(NpcTemplate template)
	{
		super(template);
	}

	@Override
	public void onBypassFeedback(Player player, String commandValue)
	{
		// Simple hack to use createscheme bypass with a space.
		final String command = commandValue.replace("createscheme ", "createscheme;");

		final StringTokenizer st = new StringTokenizer(command, ";");
		final String currentCommand = st.nextToken();
		if (currentCommand.startsWith("menu"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), 0, player));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("cleanup"))
		{
			player.stopAllEffects();

			final Summon summon = player.getPet();
			if (summon != null)
			{
				summon.stopAllEffects();
			}
			player.getServitors().values().forEach(servitor -> servitor.stopAllEffects());

			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), 0, player));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("heal"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());

			final Summon summon = player.getPet();
			if (summon != null)
			{
				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
			}
			player.getServitors().values().forEach(servitor -> servitor.setCurrentHpMp(servitor.getMaxHp(), servitor.getMaxMp()));

			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), 0, player));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("support"))
		{
			showSchemeBuffsWindow(player);
		}
        else if (currentCommand.startsWith("invidualbuffs"))
        {
 		    showIndividualBuffsWindow(player);
        }
		else if (currentCommand.startsWith("givebuffs"))
		{
			final String schemeName = st.nextToken();
			final int cost = Integer.parseInt(st.nextToken());
			final boolean buffSummons = st.hasMoreTokens() && st.nextToken().equalsIgnoreCase("pet");
			if (buffSummons && (player.getPet() == null) && !player.hasServitors())
			{
				player.sendMessage("You don't have a pet.");
			}
			else if ((cost == 0) || ((Config.BUFFER_ITEM_ID == 57) && player.reduceAdena("NPC Buffer", cost, this, true)) || ((Config.BUFFER_ITEM_ID != 57) && player.destroyItemByItemId("NPC Buffer", Config.BUFFER_ITEM_ID, cost, player, true)))
			{
				for (int skillId : SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName))
				{
					final Skill skill = SkillData.getInstance().getSkill(skillId, SchemeBufferTable.getInstance().getAvailableBuff(skillId).getLevel());
					if (buffSummons)
					{
						if (player.getPet() != null)
						{
							skill.applyEffects(this, player.getPet());
						}
						player.getServitors().values().forEach(servitor -> skill.applyEffects(this, servitor));
					}
					else
					{
						skill.applyEffects(this, player);
					}
				}
			}
		}
		else if (currentCommand.startsWith("editschemes"))
		{
			showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
		}

		else if (currentCommand.startsWith("skill"))
		{
			final String groupType = st.nextToken();
			final String schemeName = st.nextToken();
			final int skillId = Integer.parseInt(st.nextToken());
			final int page = Integer.parseInt(st.nextToken());
			final List<Integer> skills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
			if (currentCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none"))
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
			else if (currentCommand.startsWith("skillunselect"))
			{
				skills.remove(Integer.valueOf(skillId));
			}

			showEditSchemeWindow(player, groupType, schemeName, page);
		}
		else if (currentCommand.startsWith("createscheme"))
		{
			try
			{
				final String schemeName = st.nextToken().trim();
				if (schemeName.length() > 14)
				{
					player.sendMessage("Scheme's name must contain up to 14 chars.");
					return;
				}
				// Simple hack to use spaces, dots, commas, minus, plus, exclamations or question marks.
				if (!Util.isAlphaNumeric(schemeName.replace(" ", "").replace(".", "").replace(",", "").replace("-", "").replace("+", "").replace("!", "").replace("?", "")))
				{
					player.sendMessage("Please use plain alphanumeric characters.");
					return;
				}

				final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
				if (schemes != null)
				{
					if (schemes.size() == Config.BUFFER_MAX_SCHEMES)
					{
						player.sendMessage("Maximum schemes amount is already reached.");
						return;
					}

					if (schemes.containsKey(schemeName))
					{
						player.sendMessage("The scheme name already exists.");
						return;
					}
				}

				SchemeBufferTable.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<>());
				showSchemeBuffsWindow(player);
			}
			catch (Exception e)
			{
				player.sendMessage("Scheme's name must contain up to 14 chars.");
			}
		}
		else if (currentCommand.startsWith("deletescheme"))
		{
			try
			{
				final String schemeName = st.nextToken();
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
			showSchemeBuffsWindow(player);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String filename = "";
		if (value == 0)
		{
			filename = Integer.toString(npcId);
		}
		else
		{
			filename = npcId + "-" + value;
		}
		return "data/html/mods/SchemeBuffer/" + filename + ".htm";
	}

    private void showIndividualBuffsWindow(Player player)
    {
        final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    	html.setFile(player, getHtmlPath(getId(), 3, player));
//     	html.replace("%schemes%", sb.toString());
//    		html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
//    		html.replace("%objectId%", getObjectId());
   		player.sendPacket(html);
    }


	/**
	 * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targetType parameter {player, pet}
	 * @param player : The player to make checks on.
	 */
	private void showSchemeBuffsWindow(Player player)
	{
		final StringBuilder sb = new StringBuilder(200);
		final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
		if ((schemes == null) || schemes.isEmpty())
		{
			sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
		}
		else
		{
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
				sb.append("<td fixwidth=22 align=left><a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + "\"><font color=\"b3a382\">Use</font></a></td>");
				sb.append("<td fixwidth=3>|</td>");
				sb.append("<td fixwidth=57 align=left><a action=\"bypass -h npc_%objectId%_givebuffs;" + scheme.getKey() + ";" + cost + ";pet\"><font color=\"b3a382\">Use on Pet</font></a></td>");
				sb.append("<td fixwidth=3>|</td>");
				sb.append("<td fixwidth=23 align=left><a action=\"bypass -h npc_%objectId%_editschemes;Buffs;" + scheme.getKey() + ";1\"><font color=\"b3a382\">Edit</font></a></td>");
				sb.append("<td fixwidth=3>|</td>");
				sb.append("<td fixwidth=34 align=left><a action=\"bypass -h npc_%objectId%_deletescheme;" + scheme.getKey() + "\"><font color=\"b3a382\">Delete</font></a></td>");
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

		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player, getHtmlPath(getId(), 1, player));
		html.replace("%schemes%", sb.toString());
		html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}

	/**
	 * This sends an html packet to player with Edit Scheme Menu info. This allows player to edit each created scheme (add/delete skills)
	 * @param player : The player to make checks on.
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @param page The page.
	 */
	private void showEditSchemeWindow(Player player, String groupType, String schemeName, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		final List<Integer> schemeSkills = SchemeBufferTable.getInstance().getScheme(player.getObjectId(), schemeName);
		html.setFile(player, getHtmlPath(getId(), 2, player));
		html.replace("%schemename%", schemeName);
		html.replace("%count%", getCountOf(schemeSkills, false) + " / " + player.getStat().getMaxBuffCount() + " buffs, " + getCountOf(schemeSkills, true) + " / " + Config.DANCES_MAX_AMOUNT + " dances/songs");
		html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
		html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page));
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}

	/**
	 * @param player : The player to make checks on.
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @param pageValue The page.
	 * @return a String representing skills available to selection for a given groupType.
	 */
	private String getGroupSkillList(Player player, String groupType, String schemeName, int pageValue)
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
				sb.append("<td height=40 width=40><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button value=\" \" action=\"bypass npc_%objectId%_skillunselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
			}
			else
			{
				sb.append("<td height=40 width=40><img src=\"" + skill.getIcon() + "\" width=32 height=32></td><td width=190>" + skill.getName() + "<br1><font color=\"B09878\">" + SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription() + "</font></td><td><button value=\" \" action=\"bypass npc_%objectId%_skillselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			}

			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}

		// Build page footer.
		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		if (page > 1)
		{
			sb.append("<td align=left width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page - 1) + "\"><font color=\"b3a382\">Previous</font></a></td>");
		}
		else
		{
			sb.append("<td align=left width=70>Previous</td>");
		}

		sb.append("<td align=center width=100>Page " + page + "</td>");
		if (page < max)
		{
			sb.append("<td align=right width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page + 1) + "\"><font color=\"b3a382\">Next</font></a></td>");
		}
		else
		{
			sb.append("<td align=right width=70>Next</td>");
		}

		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
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
                sb.append("<td><button value=" + type + " action=\"bypass npc_%objectId%_editschemes;" + type + ";" + schemeName + ";1\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
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
}