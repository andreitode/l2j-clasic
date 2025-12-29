package handlers.bypasshandlers;

import org.classiclude.commons.util.Rnd;
import org.classiclude.gameserver.data.xml.EnchantSkillGroupsData;
import org.classiclude.gameserver.data.xml.SkillData;
import org.classiclude.gameserver.data.xml.SkillRoutes;
import org.classiclude.gameserver.enums.SkillEnchantType;
import org.classiclude.gameserver.handler.IBypassHandler;
import org.classiclude.gameserver.model.NpcEnchantSkillTeach;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.instance.Folk;
import org.classiclude.gameserver.model.holders.EnchantSkillHolder;
import org.classiclude.gameserver.model.holders.ItemHolder;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import org.classiclude.gameserver.network.serverpackets.ExEnchantSkillList;
import org.classiclude.gameserver.network.serverpackets.ExEnchantSkillResult;
import org.classiclude.gameserver.network.serverpackets.NpcHtmlMessage;
import org.classiclude.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Naker
 */
public class EnchantSkillList implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"EnchantSkillList",
		"showEnchantPage",
		"showSkillDetails",
		"enchantSkill"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (!(target instanceof Folk))
		{
			return false;
		}
		
		if (command.contains("showEnchantPage"))
		{
			try
			{
				String[] args = command.trim().split("\\s+");
				
				int page = 0;
				
				if (args.length > 1)
				{
					String pageString = args[args.length - 1].replaceAll("[^0-9]", "");
					
					try
					{
						page = Integer.parseInt(pageString);
					}
					catch (NumberFormatException e)
					{
						System.out.println("Error: Can't convert '" + pageString + "' to number.");
					}
				}
				
				ExEnchantSkillList esl = new ExEnchantSkillList(player, page);
				esl.showHtml(player, (Npc) target);
			}
			catch (Exception e)
			{
				System.out.println("Error processing page number from bypass");
				e.printStackTrace();
			}
			return true;
		}
		else if (command.startsWith("showSkillDetails"))
		{
			showSkillDetails(player, (Npc) target, command);
			return true;
		}
		else if (command.startsWith("enchantSkill"))
		{
			enchantSkill(player, (Npc) target, command);
			return true;
		}
		int npcId = ((Npc) target).getId();
		int playerClassId = player.getClassId().getId();
		if (NpcEnchantSkillTeach.getInstance().canTeach(npcId, playerClassId))
		{
			((Folk) target).showEnchantSkillList(player);
			return true;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
		html.setHtml("<html><body>I cannot teach you any skills.<br>You must find your current class teachers.</body></html>");
		player.sendPacket(html);
		return true;
	}
	
	private int getSubLevel(int subLevel)
	{
		if (subLevel == 0)
		{
			subLevel = 1;
			return subLevel;
		}
		return subLevel;
	}
	
	private void showSkillDetails(Player player, Npc npc, String command)
	{
		String[] args = command.split(" ");
		int skillId = Integer.parseInt(args[1]);
		int routeId = Integer.parseInt(args[2]);
		
		Skill skill = player.getKnownSkill(skillId);
		if (skill == null)
		{
			player.sendMessage("Error");
			return;
		}
		
		int subLevel = skill.getSubLevel();
		String enchantLevel = getEnchantLevel(subLevel);
		
		EnchantSkillHolder enchantSkillHolder = EnchantSkillGroupsData.getInstance().getEnchantSkillHolder(getSubLevel(subLevel) % 1000);
		if (enchantSkillHolder == null)
		{
			player.sendMessage("No info found for this skill");
			return;
		}
		
		String routeName = getRouteName(skillId, routeId);
		String routeDescription = getRouteDescription(skillId, routeId);
		
		String requiredItem = "<td align=left><img width=32 height=32 src=\"icon.etc_codex_of_giant_i00\"></td><td align=left>&nbsp;Secret Book of Giants X 1</td>";
		
		long currentSP = player.getSp();
		long remainingExp = player.getExp();
		long requiredExp = player.getStat().getExpForLevel(player.getLevel());
		long currentEXP = (remainingExp - requiredExp);
		long chance = enchantSkillHolder.getLevelChance(SkillEnchantType.NORMAL, player.getLevel());
		long requiredEXP = enchantSkillHolder.getRequiredExp(SkillEnchantType.NORMAL);
		long requiredSP = enchantSkillHolder.getSp(SkillEnchantType.NORMAL);
		
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(player, "data/html/trainer/EnchantSkillDetails.htm");
		html.replace("%objectId%", npc.getObjectId());
		html.replace("%skillId%", skillId);
		html.replace("%skillIcon%", skill.getIcon());
		html.replace("%skillName%", skill.getName());
		html.replace("%enchantLevel%", enchantLevel);
		html.replace("%routeName%", routeName);
		html.replace("%currentExp%", currentEXP);
		html.replace("%currentSp%", currentSP);
		html.replace("%chance%", chance);
		html.replace("%requiredExp%", requiredEXP);
		html.replace("%requiredSp%", requiredSP);
		html.replace("%skillenchantdescription%", routeDescription);
		if (subLevel == 0)
		{
			
			html.replace("%requiredItem%", requiredItem);
		}
		else
		{
			html.replace("%requiredItem%", " ");
		}
		player.sendPacket(html);
	}
	
	private String getEnchantLevel(int subLevel)
	{
		if ((subLevel > 1000) && (subLevel < 2000))
		{
			int encat = ((subLevel + 1) - 1000);
			return "+" + encat;
		}
		if (subLevel > 2000)
		{
			int encat = ((subLevel + 1) - 2000);
			return "+" + encat;
		}
		return "+1";
	}
	
	private String getRouteName(int skillId, int routeId)
	{
		return SkillRoutes.getInstance().getRouteName(skillId, routeId);
	}
	
	private String getRouteDescription(int skillId, int routeId)
	{
		return SkillRoutes.getInstance().getRouteDescription(skillId, routeId);
	}
	
	private void enchantSkill(Player player, Npc npc, String command)
	{
		if (!player.isAllowedToEnchantSkills())
		{
			return;
		}
		
		if (player.isSellingBuffs())
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			return;
		}
		
		if (player.isInStoreMode())
		{
			return;
		}
		
		String[] args = command.split(" ");
		int skillId = Integer.parseInt(args[1]);
		String routeName = args[2];
		
		Skill skill = player.getKnownSkill(skillId);
		if (skill == null)
		{
			return;
		}
		
		int subLevel = skill.getSubLevel();
		int routeId = SkillRoutes.getInstance().getRouteId(skillId, routeName);
		
		SkillEnchantType type = SkillEnchantType.NORMAL;
		
		EnchantSkillHolder enchantSkillHolder = EnchantSkillGroupsData.getInstance().getEnchantSkillHolder(getSubLevel(subLevel) % 1000);
		if (enchantSkillHolder == null)
		{
			return;
		}
		
		for (ItemHolder holder : enchantSkillHolder.getRequiredItems(type))
		{
			if (skill.getSubLevel() <= 1001)
			{
				if (player.getInventory().getInventoryItemCount(holder.getId(), 0) < holder.getCount())
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
			}
		}
		
		long requiredExp = enchantSkillHolder.getRequiredExp(type);
		if (player.getExp() < requiredExp)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_XP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		long remainingExp = player.getExp() - requiredExp;
		
		if (remainingExp < player.getStat().getExpForLevel(player.getLevel()))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_XP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		long requiredSp = enchantSkillHolder.getSp(type);
		if (player.getSp() < requiredSp)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		if ((skill.getSubLevel() + 1) >= 1002)
		{
			for (ItemHolder holder : enchantSkillHolder.getRequiredItems(type))
			{
				if (!player.destroyItemByItemId("Skill enchanting", 57, holder.getCount(), player, true))
				{
					return;
				}
			}
		}
		else
		{
			for (ItemHolder holder : enchantSkillHolder.getRequiredItems(type))
			{
				if (!player.destroyItemByItemId("Skill enchanting", holder.getId(), holder.getCount(), player, true))
				{
					return;
				}
			}
		}
		
		player.getStat().removeExpAndSp(requiredExp, requiredSp, false);
		
		int successChance = enchantSkillHolder.getLevelChance(type, player.getLevel());
		
		if (successChance <= 0)
		{
			return;
		}
		
		if (Rnd.get(100) <= successChance)
		{
			int newSubLevel = (subLevel == 0) ? (routeId == 1001 ? 1001 : 2001) : (subLevel + 1);
			
			Skill enchantedSkill = SkillData.getInstance().getSkill(skillId, skill.getLevel(), newSubLevel);
			final long reuse = player.getSkillRemainingReuseTime(skill.getReuseHashCode());
			if (reuse > 0)
			{
				player.addTimeStamp(enchantedSkill, reuse);
			}
			
			player.addSkill(enchantedSkill, true);
			
			player.sendPacket(new SystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED).addSkillName(skillId));
			player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_TRUE);
		}
		else
		{
			final int newSubLevel = 0;
			Skill enchantedSkill = SkillData.getInstance().getSkill(skillId, skill.getLevel(), newSubLevel);
			player.addSkill(enchantedSkill, true);
			
			player.sendPacket(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
			player.sendPacket(ExEnchantSkillResult.STATIC_PACKET_FALSE);
		}
		
		player.broadcastUserInfo();
		player.sendSkillList();
		
		skill = player.getKnownSkill(skillId);
		// player.sendPacket(new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), skill.getSubLevel(), skill.getSubLevel()));
		player.sendPacket(new ExEnchantSkillInfoDetail(SkillEnchantType.NORMAL, skill.getId(), skill.getLevel(), Math.min(skill.getSubLevel() + 1, EnchantSkillGroupsData.MAX_ENCHANT_LEVEL), player));
		player.updateShortCuts(skill.getId(), skill.getLevel(), skill.getSubLevel());
		((Folk) npc).showEnchantSkillList(player);
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
	
}