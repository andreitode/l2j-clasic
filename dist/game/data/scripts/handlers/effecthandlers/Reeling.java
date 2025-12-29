/*
 * This file is part of the ClassicLude project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import org.classiclude.gameserver.data.xml.FishingRodsData;
import org.classiclude.gameserver.enums.ShotType;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.effects.EffectType;
import org.classiclude.gameserver.model.fishing.Fishing;
import org.classiclude.gameserver.model.fishing.FishingRod;
import org.classiclude.gameserver.model.item.Weapon;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.ActionFailed;

/**
 * Reeling effect implementation.
 * @author UnAfraid
 */
public class Reeling extends AbstractEffect
{
	private final double _power;
	
	public Reeling(StatSet params)
	{
		if (params.getString("power", null) == null)
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + ": effect without power!");
		}
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FISHING;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effector.isPlayer())
		{
			return;
		}
		
		final Player player = effector.asPlayer();
		final Fishing fish = player.getFishCombat();
		if (fish == null)
		{
			// Reeling skill is available only while fishing
			player.sendPacket(SystemMessageId.YOU_MAY_ONLY_USE_THE_REELING_SKILL_WHILE_YOU_ARE_FISHING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final Weapon weaponItem = player.getActiveWeaponItem();
		final Item weaponInst = effector.getActiveWeaponInstance();
		if ((weaponInst == null) || (weaponItem == null))
		{
			return;
		}
		int ss = 1;
		int pen = 0;
		if (effector.isChargedShot(ShotType.FISH_SOULSHOTS))
		{
			ss = 2;
		}
		final FishingRod fishingRod = FishingRodsData.getInstance().getFishingRod(weaponItem.getId());
		final double gradeBonus = fishingRod.getFishingRodLevel() * 0.1; // TODO: Check this formula (is guessed)
		double fishingExpertise = player.getStat().getFishingExpertise();
		int dmg = (int) ((fishingRod.getFishingRodDamage() + fishingExpertise + _power) * gradeBonus * ss);
		// Penalty 5% less damage dealt
		if (player.getSkillLevel(1315) <= (skill.getLevel() - 2)) // 1315 - Fish Expertise
		{
			player.sendPacket(SystemMessageId.DUE_TO_YOUR_REELING_AND_OR_PUMPING_SKILL_BEING_THREE_OR_MORE_LEVELS_HIGHER_THAN_YOUR_FISHING_SKILL_A_5PERCEN_DAMAGE_PENALTY_WILL_BE_APPLIED);
			pen = (int) (dmg * 0.05);
			dmg -= pen;
		}
		if (ss > 1)
		{
			effector.unchargeShot(ShotType.FISH_SOULSHOTS);
		}
		
		fish.useReeling(dmg, pen);
	}
}
