package handlers.skillconditionhandlers;

import org.classiclude.gameserver.model.Party;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.Summon;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;

/**
 * @author UnAfraid
 */
public class TargetMyPartySkillCondition implements ISkillCondition
{
	private final boolean _includeMe;
	
	public TargetMyPartySkillCondition(StatSet params)
	{
		_includeMe = params.getBoolean("includeMe");
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if ((target == null) || !target.isPlayable())
		{
			return false;
		}
		
		final Party party = caster.getParty();
		if (target.isPlayer())
		{
			final Party targetParty = target.asPlayer().getParty();
			return ((party == null) ? (_includeMe && (caster == target)) : (_includeMe ? party == targetParty : (party == targetParty) && (caster != target)));
		}
		else if (target.isSummon())
		{
			final Summon summon = target.asSummon();
			final Player summonOwner = summon.getOwner();
			if (summonOwner != null)
			{
				final Party targetParty = summonOwner.getParty();
				return ((party == null) ? (_includeMe && (caster == summonOwner)) : (_includeMe ? party == targetParty : (party == targetParty) && (caster != summonOwner)));
			}
		}
		return false;
	}
}
