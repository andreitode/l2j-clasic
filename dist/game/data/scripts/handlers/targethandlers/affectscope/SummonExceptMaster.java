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
package handlers.targethandlers.affectscope;

import java.util.function.Consumer;

import org.classiclude.gameserver.handler.AffectObjectHandler;
import org.classiclude.gameserver.handler.IAffectObjectHandler;
import org.classiclude.gameserver.handler.IAffectScopeHandler;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.skill.targets.AffectScope;
import org.classiclude.gameserver.util.Util;

/**
 * @author Nik, Mobius
 */
public class SummonExceptMaster implements IAffectScopeHandler
{
	@Override
	public void forEachAffected(Creature creature, WorldObject target, Skill skill, Consumer<? super WorldObject> action)
	{
		final IAffectObjectHandler affectObject = AffectObjectHandler.getInstance().getHandler(skill.getAffectObject());
		final int affectRange = skill.getAffectRange();
		final int affectLimit = skill.getAffectLimit();
		
		if (target.isPlayable())
		{
			int count = 0;
			final int limit = (affectLimit > 0) ? affectLimit : Integer.MAX_VALUE;
			for (Creature c : target.asPlayer().getServitorsAndPets())
			{
				if (c.isDead())
				{
					continue;
				}
				
				if ((affectRange > 0) && !Util.checkIfInRange(affectRange, c, target, true))
				{
					continue;
				}
				
				if ((affectObject != null) && !affectObject.checkAffectedObject(creature, c))
				{
					continue;
				}
				
				count++;
				action.accept(c);
				
				if (count >= limit)
				{
					break;
				}
			}
		}
	}
	
	@Override
	public Enum<AffectScope> getAffectScopeType()
	{
		return AffectScope.SUMMON_EXCEPT_MASTER;
	}
}
