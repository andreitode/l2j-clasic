package handlers.skillconditionhandlers;

import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.skill.enums.Element;

public class ElementSeedTotalCondition implements ISkillCondition
{
	private final int _totalRequired;
	
	public ElementSeedTotalCondition(StatSet params)
	{
		_totalRequired = params.getInt("elementalSeeds");
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if (!caster.isPlayer())
		{
			return false;
		}
		
		Player player = caster.asPlayer();
		int totalSeeds = 0;
		for (Element element : Element.values())
		{
			totalSeeds += player.getElementSeedCount(element);
		}
		return totalSeeds >= _totalRequired;
	}
}
