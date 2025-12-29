package org.classiclude.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.classiclude.gameserver.data.xml.EnchantSkillGroupsData;
import org.classiclude.gameserver.data.xml.SkillRoutes;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * @author Naker
 */
public class ExEnchantSkillList
{
	private static final int SKILLS_PER_PAGE = 8;
	private final int _page;
	private final List<SkillEnchantEntry> _skills = new ArrayList<>();
	
	public ExEnchantSkillList(Player player, int page)
	{
		this._page = page;
		loadEnchantableSkills(player);
		
	}
	
	private void loadEnchantableSkills(Player player)
	{
		_skills.clear();
		
		for (Skill skill : player.getAllSkills())
		{
			int skillId = skill.getId();
			int skillLevel = skill.getLevel();
			int subLevel = skill.getSubLevel();
			int currentRoute = getCurrentRoute(subLevel);
			
			Set<Integer> routes = EnchantSkillGroupsData.getInstance().getRouteForSkill(skillId, skillLevel);
			if ((routes != null) && !routes.isEmpty())
			{
				for (Integer route : routes)
				{
					if ((subLevel == 0) || (currentRoute == route))
					{
						_skills.add(new SkillEnchantEntry(skillId, skillLevel, subLevel, route));
					}
				}
			}
		}
	}
	
	private int getCurrentRoute(int subLevel)
	{
		if ((subLevel > 1000) && (subLevel < 2000))
		{
			return 1001;
		}
		else if (subLevel >= 2000)
		{
			return 2001;
		}
		return 0;
	}
	
	public void showHtml(Player player, Npc npc)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(player, "data/html/trainer/ExEnchantSkillList.htm");
		html.replace("%objectId%", npc.getObjectId());
		html.replace("%skill_enchant_list%", getSkillEnchantListHtml(player));
		html.replace("%paging%", getPagingHtml(player, npc));
		player.sendPacket(html);
	}
	
	private static class SkillEnchantEntry
	{
		private final int _skillId;
		private final int _subLevel;
		private final int _routeId;
		
		public SkillEnchantEntry(int skillId, int skillLevel, int subLevel, int routeId)
		{
			_skillId = skillId;
			_subLevel = subLevel;
			_routeId = routeId;
		}
		
		public int getSkillId()
		{
			return _skillId;
		}
		
		public int getRouteId()
		{
			return _routeId;
		}
		
		public String getSkillName(Player player)
		{
			Skill skill = player.getKnownSkill(_skillId);
			return (skill != null) ? skill.getName() : "Desconocido";
		}
		
		public String getSkillIcon(Player player)
		{
			Skill skill = player.getKnownSkill(_skillId);
			return (skill != null) ? skill.getIcon() : "icon.skill0000";
		}
		
		private String getRouteName(int skillId, int routeId)
		{
			return SkillRoutes.getInstance().getRouteName(skillId, routeId);
		}
		
		public String getEnchantRouteName(Player player)
		{
			Skill skill = player.getKnownSkill(_skillId);
			if (skill != null)
			{
				return getRouteName(_skillId, _routeId);
			}
			return "";
		}
		
		public String getEnchantlvl(Player player)
		{
			Skill skill = player.getKnownSkill(_skillId);
			if (skill != null)
			{
				int base = _subLevel;
				if ((base > 1000) && (base < 2000))
				{
					int encat = ((base + 1) - 1000);
					return "+" + encat;
				}
				if (base > 2000)
				{
					int encat = ((base + 1) - 2000);
					return "+" + encat;
				}
				return "+1";
			}
			return "+1";
		}
	}
	
	private String getSkillEnchantListHtml(Player player)
	{
		StringBuilder sb = new StringBuilder();
		int startIndex = _page * SKILLS_PER_PAGE;
		int endIndex = Math.min(startIndex + SKILLS_PER_PAGE, _skills.size());
		
		if (_skills.isEmpty())
		{
			sb.append("<tr><td>No tienes habilidades encantables.</td></tr>");
		}
		else
		{
			sb.append("<table border=0 cellspacing=0 cellpadding=0 width=292 height=316>");
			for (int i = startIndex; i < endIndex; i++)
			{
				SkillEnchantEntry entry = _skills.get(i);
				sb.append("<tr>");
				sb.append("<td align=center>");
				sb.append("<a action=\"bypass -h npc_").append(player.getTargetId()).append("_showSkillDetails ").append(entry.getSkillId()).append(" ").append(entry.getRouteId()).append("\">"); // Incluir routeId en el bypass
				sb.append("<img src=\"").append(entry.getSkillIcon(player)).append("\" width=32 height=32 style=\"border: 1px solid white;\">");
				sb.append("</a>");
				sb.append("</td>");
				sb.append("<td width=200>");
				sb.append("<a action=\"bypass -h npc_").append(player.getTargetId()).append("_showSkillDetails ").append(entry.getSkillId()).append(" ").append(entry.getRouteId()).append("\">"); // Incluir routeId en el bypass
				sb.append(entry.getSkillName(player));
				sb.append("<br1><font color=\"b09979\">").append(entry.getEnchantlvl(player)).append("</font>&nbsp;");
				sb.append("<font color=\"b09979\">").append(entry.getEnchantRouteName(player)).append("</font>");
				sb.append("</a>");
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
		}
		return sb.toString();
	}
	
	private String getPagingHtml(Player player, Npc npc)
	{
		StringBuilder sb = new StringBuilder();
		int totalPages = (_skills.size() / SKILLS_PER_PAGE) + ((_skills.size() % SKILLS_PER_PAGE) > 0 ? 1 : 0);
		
		if (totalPages > 1)
		{
			sb.append("<center><table><tr>");
			for (int i = 0; i < totalPages; i++)
			{
				String bypassCommand = "bypass -h EnchantSkillList showEnchantPage " + i;
				
				if (i == _page)
				{
					sb.append("<td align=center width=30><font color=\"LEVEL\">[").append(i + 1).append("]</font></td>");
				}
				else
				{
					sb.append("<td align=center width=30><a action=\"").append(bypassCommand).append("\">[").append(i + 1).append("]</a></td>");
				}
			}
			sb.append("</tr></table></center>");
		}
		return sb.toString();
	}
}