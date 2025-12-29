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
package handlers.bypasshandlers;

import org.classiclude.Config;
import org.classiclude.gameserver.enums.CategoryType;
import org.classiclude.gameserver.handler.IBypassHandler;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Npc;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.Summon;
import org.classiclude.gameserver.model.holders.SkillHolder;
import org.classiclude.gameserver.model.skill.SkillCaster;

public class SupportMagic implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"supportmagicservitor",
		"supportmagic"
	};
	
	// Buffs
	private static final SkillHolder HASTE_1 = new SkillHolder(4327, 1);
	private static final SkillHolder HASTE_2 = new SkillHolder(5632, 1);
	private static final SkillHolder CUBIC = new SkillHolder(4338, 1);
	private static final SkillHolder[] SUMMON_BUFFS =
	{
		new SkillHolder(4322, 1), // Wind Walk
		new SkillHolder(4323, 1), // Shield
		new SkillHolder(5637, 1), // Magic Barrier
		new SkillHolder(4324, 1), // Bless the Body
		new SkillHolder(4325, 1), // Vampiric Rage
		new SkillHolder(4326, 1), // Regeneration
		new SkillHolder(4328, 1), // Bless the Soul
		new SkillHolder(4329, 1), // Acumen
		new SkillHolder(4330, 1), // Concentration
		new SkillHolder(4331, 1), // Empower
	};
	
	// Levels
	private static final int LOWEST_LEVEL = 6;
	private static final int CUBIC_LOWEST = 16;
	private static final int CUBIC_HIGHEST = 36;
	private static final int HASTE_LEVEL_2 = Config.MAX_NEWBIE_BUFF_LEVEL + 1; // disabled
	
	// BUff level restrictions
	
	private static final int WIND_WALK_MIN = 6;
	private static final int WIND_WALK_MAX = 39;
	private static final int SHIELD_MIN = 11;
	private static final int SHIELD_MAX = 39;
	private static final int BLESS_BODY_MIN = 12;
	private static final int BLESS_BODY_MAX = 30;
	private static final int BLESS_SOUL_MIN = 12;
	private static final int BLESS_SOUL_MAX = 30;
	private static final int VAMPIRIC_RAGE_MIN = 13;
	private static final int VAMPIRIC_RAGE_MAX = 38;
	private static final int ACUMEN_MIN = 13;
	private static final int ACUMEN_MAX = 38;
	private static final int REGENERATION_MIN = 14;
	private static final int REGENERATION_MAX = 38;
	private static final int CONCENTRATION_MIN = 14;
	private static final int CONCENTRATION_MAX = 38;
	private static final int HASTE_MIN = 15;
	private static final int HASTE_MAX = 37;
	private static final int EMPOWER_MIN = 15;
	private static final int EMPOWER_MAX = 37;
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (!target.isNpc() || player.isCursedWeaponEquipped())
		{
			return false;
		}
		
		if (command.equalsIgnoreCase(COMMANDS[0]))
		{
			makeSupportMagic(player, target.asNpc(), true);
		}
		else if (command.equalsIgnoreCase(COMMANDS[1]))
		{
			makeSupportMagic(player, target.asNpc(), false);
		}
		return true;
	}
	
	private void makeSupportMagic(Player player, Npc npc, boolean isSummon)
	{
		final int level = player.getLevel();
		if (isSummon && !player.hasServitors())
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicNoSummon.htm");
			return;
		}
		else if (level < LOWEST_LEVEL)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicLowLevel.htm");
			return;
		}
		else if (level > Config.MAX_NEWBIE_BUFF_LEVEL)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicHighLevel.htm");
			return;
		}
		else if (player.getClassId().level() == 3)
		{
			player.sendMessage("Only adventurers who have not completed their 3rd class transfer may receive these buffs."); // Custom message
			return;
		}
		
		if (isSummon)
		{
			for (Summon s : player.getServitors().values())
			{
				npc.setTarget(s);
				applySummonBuffs(npc, s, level);
			}
		}
		else
		{
			npc.setTarget(player);
			if (player.isInCategory(CategoryType.BEGINNER_MAGE))
			{
				applyMageBuffs(npc, player, level);
			}
			else
			{
				applyFighterBuffs(npc, player, level);
			}
		}
	}
	
	private void applyMageBuffs(Npc npc, Player player, int level)
	{
		if ((level >= WIND_WALK_MIN) && (level <= WIND_WALK_MAX)) // Wind Walk
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4322, 1).getSkill());
		}
		if ((level >= SHIELD_MIN) && (level <= SHIELD_MAX)) // Shield
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4323, 1).getSkill());
		}
		if ((level >= BLESS_SOUL_MIN) && (level <= BLESS_SOUL_MAX)) // Bless Soul
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4328, 1).getSkill());
		}
		if ((level >= ACUMEN_MIN) && (level <= ACUMEN_MAX)) // Acumen
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4329, 1).getSkill());
		}
		if ((level >= CONCENTRATION_MIN) && (level <= CONCENTRATION_MAX)) // Concentration
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4330, 1).getSkill());
		}
		if ((level >= EMPOWER_MIN) && (level <= EMPOWER_MAX)) // Empower
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4331, 1).getSkill());
		}
		if ((level >= CUBIC_LOWEST) && (level <= CUBIC_HIGHEST))
		{
			SkillCaster.triggerCast(npc, player, CUBIC.getSkill()); // Cubic
		}
	}
	
	private void applyFighterBuffs(Npc npc, Player player, int level)
	{
		if ((level >= WIND_WALK_MIN) && (level <= WIND_WALK_MAX))
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4322, 1).getSkill()); // Wind Walk
		}
		if ((level >= SHIELD_MIN) && (level <= SHIELD_MAX))
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4323, 1).getSkill()); // Shield
		}
		if ((level >= BLESS_BODY_MIN) && (level <= BLESS_BODY_MAX))
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4324, 1).getSkill()); // Bless the Body
		}
		if ((level >= VAMPIRIC_RAGE_MIN) && (level <= VAMPIRIC_RAGE_MAX))
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4325, 1).getSkill()); // Vampiric Rage
		}
		if ((level >= REGENERATION_MIN) && (level <= REGENERATION_MAX))
		{
			SkillCaster.triggerCast(npc, player, new SkillHolder(4326, 1).getSkill()); // Regeneration
		}
		if ((level >= CUBIC_LOWEST) && (level <= CUBIC_HIGHEST))
		{
			SkillCaster.triggerCast(npc, player, CUBIC.getSkill()); // Cubic
		}
		
		applyHaste(npc, player, level);
	}
	
	private void applySummonBuffs(Npc npc, Summon summon, int level)
	{
		for (SkillHolder skill : SUMMON_BUFFS)
		{
			SkillCaster.triggerCast(npc, summon, skill.getSkill());
		}
		
		applyHaste(npc, summon, level);
	}
	
	private void applyHaste(Npc npc, Creature target, int level)
	{
		if ((level >= HASTE_MIN) && (level <= HASTE_MAX))
		{
			if (level >= HASTE_LEVEL_2)
			{
				SkillCaster.triggerCast(npc, target, HASTE_2.getSkill());
			}
			else
			{
				SkillCaster.triggerCast(npc, target, HASTE_1.getSkill());
			}
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
