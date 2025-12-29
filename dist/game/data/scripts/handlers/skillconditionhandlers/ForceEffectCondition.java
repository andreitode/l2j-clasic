package handlers.skillconditionhandlers;

import java.util.ArrayList;
import java.util.List;

import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.skill.BuffInfo;
import org.classiclude.gameserver.model.skill.ISkillCondition;
import org.classiclude.gameserver.model.skill.Skill;

public class ForceEffectCondition implements ISkillCondition
{
	private static class ForceRequirement
	{
		final String type;
		final int minLevel;
		
		ForceRequirement(String type, int minLevel)
		{
			this.type = type;
			this.minLevel = minLevel;
		}
	}
	
	private static final int SPELL_FORCE_ID = 5105;
	private static final int BATTLE_FORCE_ID = 5104;
	
	private final List<ForceRequirement> _requiredForces = new ArrayList<>();
	
	public ForceEffectCondition(StatSet params)
	{
		List<String> forces = params.getList("force", String.class);
		if ((forces == null) || forces.isEmpty())
		{
			String single = params.getString("force", null);
			if (single != null)
			{
				forces = List.of(single);
			}
		}
		
		if ((forces == null) || forces.isEmpty())
		{
			throw new IllegalArgumentException("Missing or invalid 'force' in XML.");
		}
		
		for (String force : forces)
		{
			String[] split = force.split(",");
			if (split.length != 2)
			{
				throw new IllegalArgumentException("Invalid force format: " + force);
			}
			
			String type = split[0].trim().toLowerCase();
			int level;
			try
			{
				level = Integer.parseInt(split[1].trim());
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Invalid level in force: " + split[1]);
			}
			
			if (!type.equals("spell") && !type.equals("battle"))
			{
				throw new IllegalArgumentException("Unknown force type: " + type);
			}
			
			_requiredForces.add(new ForceRequirement(type, level));
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
		
		for (ForceRequirement req : _requiredForces)
		{
			int skillId;
			switch (req.type)
			{
				case "spell":
					skillId = SPELL_FORCE_ID;
					break;
				case "battle":
					skillId = BATTLE_FORCE_ID;
					break;
				default:
					skillId = -1;
					break;
			}
			
			final BuffInfo info = player.getEffectList().getBuffInfoBySkillId(skillId);
			if ((info == null) || (info.getSkill().getLevel() < req.minLevel))
			{
				player.sendMessage("You need " + req.type + " force at Lv." + req.minLevel + " or higher.");
				return false;
			}
		}
		
		return true;
	}
}
