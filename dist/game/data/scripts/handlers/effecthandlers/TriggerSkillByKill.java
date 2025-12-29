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

import org.classiclude.commons.util.Rnd;
import org.classiclude.gameserver.enums.InstanceType;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.OnCreatureKilled;
import org.classiclude.gameserver.model.events.listeners.ConsumerEventListener;
import org.classiclude.gameserver.model.holders.SkillHolder;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.skill.SkillCaster;

/**
 * Trigger Skill By Kill effect implementation.
 * @author Sdw
 */
public class TriggerSkillByKill extends AbstractEffect
{
	private final int _chance;
	private final SkillHolder _skill;
	private final InstanceType _victimType;
	
	public TriggerSkillByKill(StatSet params)
	{
		_chance = params.getInt("chance", 100);
		_skill = new SkillHolder(params.getInt("skillId", 0), params.getInt("skillLevel", 0));
		_victimType = params.getEnum("victimType", InstanceType.class, InstanceType.Creature);
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.addListener(new ConsumerEventListener(effected, EventType.ON_CREATURE_KILLED, (OnCreatureKilled event) -> onCreatureKilled(event), this));
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.removeListenerIf(EventType.ON_CREATURE_KILLED, listener -> listener.getOwner() == this);
	}
	
	private void onCreatureKilled(OnCreatureKilled event)
	{
		if ((_chance == 0) || ((_skill.getSkillId() == 0) || (_skill.getSkillLevel() == 0)))
		{
			return;
		}
		
		if (Rnd.get(100) > _chance)
		{
			return;
		}
		
		if (!event.getTarget().getInstanceType().isType(_victimType))
		{
			return;
		}
		
		SkillCaster.triggerCast(event.getAttacker(), event.getAttacker(), _skill.getSkill());
	}
}
