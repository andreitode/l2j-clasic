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
package org.classiclude.gameserver.model.conditions;

import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.item.ItemTemplate;
import org.classiclude.gameserver.model.skill.BuffInfo;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * The Class ConditionTargetActiveEffectId.
 */
public class ConditionTargetActiveEffectId extends Condition
{
	private final int _effectId;
	private final int _effectLvl;
	
	/**
	 * Instantiates a new condition target active effect id.
	 * @param effectId the effect id
	 */
	public ConditionTargetActiveEffectId(int effectId)
	{
		_effectId = effectId;
		_effectLvl = -1;
	}
	
	/**
	 * Instantiates a new condition target active effect id.
	 * @param effectId the effect id
	 * @param effectLevel the effect level
	 */
	public ConditionTargetActiveEffectId(int effectId, int effectLevel)
	{
		_effectId = effectId;
		_effectLvl = effectLevel;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		final BuffInfo info = effected.getEffectList().getBuffInfoBySkillId(_effectId);
		return (info != null) && ((_effectLvl == -1) || (_effectLvl <= info.getSkill().getLevel()));
	}
}
