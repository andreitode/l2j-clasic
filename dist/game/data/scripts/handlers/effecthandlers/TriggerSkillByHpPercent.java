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
package handlers.effecthandlers;

import org.classiclude.gameserver.data.xml.SkillData;
import org.classiclude.gameserver.enums.SkillFinishType;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.OnCreatureHpChange;
import org.classiclude.gameserver.model.events.listeners.ConsumerEventListener;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.skill.SkillCaster;

/**
 * @author Mobius
 */
public class TriggerSkillByHpPercent extends AbstractEffect
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _percentFrom;
	private final int _percentTo;
	
	public TriggerSkillByHpPercent(StatSet params)
	{
		_skillId = params.getInt("skillId", 0);
		_skillLevel = params.getInt("skillLevel", 1);
		_percentFrom = params.getInt("percentFrom", 0);
		_percentTo = params.getInt("percentTo", 100);
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.addListener(new ConsumerEventListener(effected, EventType.ON_CREATURE_HP_CHANGE, (OnCreatureHpChange event) -> onHpChange(event), this));
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.removeListenerIf(EventType.ON_CREATURE_HP_CHANGE, listener -> listener.getOwner() == this);
	}
	
	private void onHpChange(OnCreatureHpChange event)
	{
		final Creature creature = event.getCreature();
		final int hpPercent = creature.getCurrentHpPercent();
		if ((hpPercent >= _percentFrom) && (hpPercent <= _percentTo))
		{
			if (!creature.isAffectedBySkill(_skillId))
			{
				SkillCaster.triggerCast(creature, creature, SkillData.getInstance().getSkill(_skillId, _skillLevel));
			}
		}
		else
		{
			creature.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, _skillId);
		}
	}
}
