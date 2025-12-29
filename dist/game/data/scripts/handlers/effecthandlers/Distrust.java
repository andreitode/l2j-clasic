package handlers.effecthandlers;

import java.util.List;

import org.classiclude.commons.util.Rnd;
import org.classiclude.gameserver.ai.AttackableAI;
import org.classiclude.gameserver.ai.CtrlIntention;
import org.classiclude.gameserver.ai.DistrustAI;
import org.classiclude.gameserver.model.StatSet;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Attackable;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.instance.Chest;
import org.classiclude.gameserver.model.actor.instance.Monster;
import org.classiclude.gameserver.model.effects.AbstractEffect;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.SystemMessageId;

public class Distrust extends AbstractEffect
{
	public Distrust(StatSet params)
	{
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!(effected instanceof Monster) || (effected instanceof Chest) || effected.isRaid() || effected.isRaidMinion() || effected.isDead())
		{
			effector.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final Monster targetMonster = effected.asMonster();
		
		final List<Monster> targets = World.getInstance().getVisibleObjectsInRange(targetMonster, Monster.class, 1100, m -> (m != targetMonster) && !(m instanceof Chest) && m.isAttackable() && !m.isDead() && !m.isRaid() && !m.isRaidMinion());
		
		if (targets.isEmpty())
		{
			return;
		}
		
		final Monster newTarget = targets.get(Rnd.get(targets.size()));
		if ((newTarget == null) || (newTarget == effected))
		{
			return;
		}
		
		Attackable targetMob = effected.asAttackable();
		targetMob.setAI(new DistrustAI(targetMob, newTarget));
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		Attackable mob = effected.asAttackable();
		if ((mob == null) || mob.isDead())
		{
			return;
		}
		mob.setAI(new AttackableAI(mob));
		mob.setTarget(null);
		mob.setWalking();
		mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
}
