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
import org.classiclude.gameserver.model.actor.Creature;
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
	public void showChatWindow(Player player)
	{
		showPrimeWindow(player);
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
			showPrimeWindow(player);
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

			showPrimeWindow(player);
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

			showPrimeWindow(player);
		}
		else if (currentCommand.startsWith("support"))
		{
			showPrimeWindow(player);
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
			showPrimeWindow(player);
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
				showPrimeWindow(player);
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
			showPrimeWindow(player);
		}
		else if (currentCommand.startsWith("solobuffmenu"))
		{
			final String[] params = currentCommand.split(" ");
			String page = params[1];
			int p = Integer.valueOf(page);
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), p, player));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (currentCommand.startsWith("bbsadbuff"))
		{
			String[] parts = currentCommand.split(" ");

			if (parts.length < 3)
			{
				player.sendMessage("Invalid buff request.");
				return;
			}

			int p = Integer.parseInt(parts[2]);

			String[] skillData = parts[1].split(",");
			int skillId = Integer.parseInt(skillData[0]);
			int skillLevel = Integer.parseInt(skillData[1]);

			List<Creature> targets = new ArrayList<>();
			targets.add(player);

			if (player.getPet() != null)
			{
				targets.add(player.getPet());
			}

			player.getServitors().values().forEach(targets::add);
			Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
			for (Creature target : targets)
			{
				if (skill.isSharedWithSummon() || target.isPlayer())
				{
					skill.applyEffects(player, target);
				}
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player, getHtmlPath(getId(), p, player));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
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

	private void showPrimeWindow(Player player)
	{
		final Map<String, List<Integer>> schemes = SchemeBufferTable.getInstance().getPlayerSchemes(player.getObjectId());
		final StringBuilder sb = new StringBuilder(200);

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

				sb.append("<table width=280 cellpadding=0 cellspacing=0 background=\"l2tartarus_protojah.bg2\">");
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
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player, getHtmlPath(getId(), 0, player));
		html.replace("%schemes%", sb.toString());
		html.replace("%objectId%", getObjectId());
		html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);

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
		html.replace("%countbuff%", getCountOf(schemeSkills, false) + " / " + (player.getStat().getMaxBuffCount() - 1));
		html.replace("%countdance%", getCountOf(schemeSkills, true) + " / " + (Config.DANCES_MAX_AMOUNT - 1));
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
		sb.append("<table cellpadding=0 cellspacing=0><tr><td fixwidth=6></td><td align=center>");
		sb.append("<table width=284 cellpadding=0 cellspacing=0  background=\"l2tartarus_protojah.bg2\"><tr><td height=10></td></tr>");

		for (int skillId : skills)
		{
			final Skill skill = SkillData.getInstance().getSkill(skillId, 1);
			final String icon = skill.getIcon();
			final String name = skill.getName();
			final String desc = SchemeBufferTable.getInstance().getAvailableBuff(skillId).getDescription();

			sb.append("<tr><td align=center>");
			sb.append("<table cellpadding=0 cellspacing=0><tr><td height=8></td></tr></table>");
			sb.append("<table cellpadding=0 cellspacing=0>");
			sb.append("<tr><td fixwidth=5></td>");
			sb.append("<td fixwidth=38 align=center><img src=\"" + icon + "\" width=32 height=32></td>");
			sb.append("</tr></table></td>");
			sb.append("<td align=center>");
			sb.append("<table cellpadding=0 cellspacing=0><tr><td height=13></td></tr></table>");
			sb.append("<table cellpadding=0 cellspacing=0><tr>");
			sb.append("<td fixwidth=220 align=left>" + name + "</td></tr></table>");
			sb.append("<table cellpadding=0 cellspacing=0><tr>");
			sb.append("<td fixwidth=220 align=left><font color=\"b3a382\">" + desc + "</font></td>");
			sb.append("</tr></table></td>");
			sb.append("<td align=center>");
			sb.append("<table cellpadding=0 cellspacing=0><tr><td height=12></td></tr></table>");
			sb.append("<table cellpadding=0 cellspacing=0>");
			sb.append("<tr><td fixwidth=5></td>");

			if (schemeSkills.contains(skillId))
			{
				// BOTÓN MENOS (ELIMINAR)
				sb.append("<td fixwidth=32 align=center>" + "<button value=\" \" action=\"bypass npc_%objectId%_skillunselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=21 back=\"l2tartarus_protojah.BTN_Minus_down\" fore=\"l2tartarus_protojah.BTN_Minus\">" + "</td>");
			}
			else
			{
				// BOTÓN MÁS (AÑADIR)
				sb.append("<td fixwidth=32 align=center>" + "<button value=\" \" action=\"bypass npc_%objectId%_skillselect;" + groupType + ";" + schemeName + ";" + skillId + ";" + page + "\" width=32 height=21 back=\"l2tartarus_protojah.BTN_Plus_down\" fore=\"l2tartarus_protojah.BTN_Plus\">" + "</td>");
			}
			sb.append("</tr></table></td></tr><tr><td height=18></td></tr>");
		}
		sb.append("</table></td></tr></table>");

		// Build page footer. ---------
		sb.append("<table cellpadding=0 cellspacing=0><tr><td height=20></td></tr></table>");
		sb.append("<table cellpadding=0 cellspacing=0 width=290><tr><td fixwidth=10></td>");
		if (page > 1)
		{
			sb.append("<td fixwidth=96 align=center><button value=\"Previous\" action=\"bypass npc_" + getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page - 1) + "\" width=70 height=23 back=\"l2tartarus_protojah.btn_down\" fore=\"l2tartarus_protojah.btn\"></td>>");
		}
		else
		{
			sb.append("<td fixwidth=96 align=center><button value=\"Previous\" action=\"#\" width=70 height=23 back=\"l2tartarus_protojah.btn_down\" fore=\"l2tartarus_protojah.btn\"></td>");
		}

		sb.append("<td fixwidth=96 align=center>Page " + page + "</td>");
		if (page < max)
		{
			sb.append("<td fixwidth=96 align=center><button value=\"Next\" action=\"bypass npc_" + getObjectId() + "_editschemes;" + groupType + ";" + schemeName + ";" + (page + 1) + "\"width=70 height=23 back=\"l2tartarus_protojah.btn_down\" fore=\"l2tartarus_protojah.btn\"></td>");
		}
		else
		{
			sb.append("<td fixwidth=96 align=center><button value=\"Next\" action=\"#\" width=70 height=23 back=\"l2tartarus_protojah.btn_down\" fore=\"l2tartarus_protojah.btn\"></td>");
		}
		sb.append("<td fixwidth=96 align=center><button value=\"Back\" action=\"bypass npc_" + getObjectId() + "_support;\" width=70 height=23 back=\"l2tartarus_protojah.btn_down\" fore=\"l2tartarus_protojah.btn\"></td>");
		sb.append("</tr></table>");
		return sb.toString();
	}

	/**
	 * @param groupType : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @return a string representing all groupTypes available. The group currently on selection isn't linkable.
	 */
	private static String getTypesFrame(String groupType, String schemeName)
	{
		final String[] types = new String[]
		{
			"Buffs",
			"Resist",
			"Dances",
			"Songs"
		};
		final StringBuilder sb = new StringBuilder(500);
		sb.append("<table cellpadding=0 cellspacing=0 width=290><tr>");
		sb.append("<td fixwidth=10></td>");

		for (String t : types)
		{
			if (t.equalsIgnoreCase(groupType))
			{
				sb.append("<td fixwidth=96 align=center><button value=\"" + t + "\" action=\"#\" width=70 height=23 back=\"l2tartarus_protojah.btn_down\" fore=\"l2tartarus_protojah.btn_over\"></td>");
			}
			else
			{
				sb.append("<td fixwidth=96 align=center><button value=\"" + t + "\" action=\"bypass -h npc_%objectId%_editschemes;" + t + ";" + schemeName + ";1\" width=70 height=23 back=\"l2tartarus_protojah.btn_down\" fore=\"l2tartarus_protojah.btn\"></td>");
			}
		}

		sb.append("</tr></table>");
		
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