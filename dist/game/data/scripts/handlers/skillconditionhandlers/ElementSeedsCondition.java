package handlers.skillconditionhandlers;

import java.util.ArrayList;
import java.util.List;

import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.model.skill.enums.Element;

public class ElementSeedsCondition implements ISkillCondition
{
	private static class ElementSeedRequirement
	{
		final Element element;
		final int amount;
		
		ElementSeedRequirement(Element element, int amount)
		{
			this.element = element;
			this.amount = amount;
		}
	}
	
	private final List<ElementSeedRequirement> _requiredSeeds = new ArrayList<>();
	
	public ElementSeedsCondition(StatSet params)
	{
		List<String> seeds = params.getList("elementalSeed", String.class);
		if ((seeds == null) || seeds.isEmpty())
		{
			String singleSeed = params.getString("elementalSeed", null);
			if (singleSeed != null)
			{
				seeds = List.of(singleSeed);
			}
		}
		
		if ((seeds == null) || seeds.isEmpty())
		{
			throw new IllegalArgumentException("Missing or invalid 'elementalSeed' in XML.");
		}
		
		for (String seedData : seeds)
		{
			String[] split = seedData.split(",");
			if (split.length != 2)
			{
				throw new IllegalArgumentException("Invalid elementalSeed format: " + seedData);
			}
			
			Element element;
			try
			{
				element = Element.valueOf(split[0].trim().toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Unknown element: " + split[0].trim());
			}
			
			int amount;
			try
			{
				amount = Integer.parseInt(split[1].trim());
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Invalid amount in elementalSeed: " + split[1].trim());
			}
			
			_requiredSeeds.add(new ElementSeedRequirement(element, amount));
		}
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		if (!caster.isPlayer())
		{
			return false;
		}
		
		final Player player = caster.asPlayer();
		for (ElementSeedRequirement seedReq : _requiredSeeds)
		{
			if (player.getElementSeedCount(seedReq.element) < seedReq.amount)
			{
				return false;
			}
		}
		return true;
	}
}
