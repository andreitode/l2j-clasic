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
package org.classiclude.gameserver.model.actor;

import org.classiclude.gameserver.ai.CtrlEvent;
import org.classiclude.gameserver.enums.InstanceType;
import org.classiclude.gameserver.instancemanager.ZoneManager;
import org.classiclude.gameserver.model.WorldObject;
import org.classiclude.gameserver.model.actor.stat.PlayableStat;
import org.classiclude.gameserver.model.actor.status.PlayableStatus;
import org.classiclude.gameserver.model.actor.templates.CreatureTemplate;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.effects.EffectFlag;
import org.classiclude.gameserver.model.events.EventDispatcher;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.OnCreatureDeath;
import org.classiclude.gameserver.model.events.returns.TerminateReturn;
import org.classiclude.gameserver.model.instancezone.Instance;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.quest.QuestState;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.serverpackets.EtcStatusUpdate;

/**
 * This class represents all Playable characters in the world.<br>
 * Playable:
 * <ul>
 * <li>Player</li>
 * <li>Summon</li>
 * </ul>
 */
public abstract class Playable extends Creature
{
	private Creature _lockedTarget = null;
	private Player transferDmgTo = null;
	
	/**
	 * Constructor of Playable.<br>
	 * <br>
	 * <b><u>Actions</u>:</b>
	 * <ul>
	 * <li>Call the Creature constructor to create an empty _skills slot and link copy basic Calculator set to this Playable</li>
	 * </ul>
	 * @param objectId the object id
	 * @param template The CreatureTemplate to apply to the Playable
	 */
	public Playable(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.Playable);
		setInvul(false);
	}
	
	public Playable(CreatureTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.Playable);
		setInvul(false);
	}
	
	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (EventDispatcher.getInstance().hasListener(EventType.ON_CREATURE_DEATH, this))
		{
			final TerminateReturn returnBack = EventDispatcher.getInstance().notifyEvent(new OnCreatureDeath(killer, this), this, TerminateReturn.class);
			if ((returnBack != null) && returnBack.terminate())
			{
				return false;
			}
		}
		
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
			{
				return false;
			}
			// now reset currentHp to zero
			setCurrentHp(0);
			setDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Abort casting after target has been cancelled.
		abortAttack();
		abortCast();
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		boolean deleteBuffs = true;
		if (isNoblesseBlessedAffected())
		{
			stopEffects(EffectFlag.NOBLESS_BLESSING);
			deleteBuffs = false;
		}
		if (isResurrectSpecialAffected())
		{
			stopEffects(EffectFlag.RESURRECTION_SPECIAL);
			deleteBuffs = false;
		}
		
		final Player player = asPlayer();
		if (isPlayer())
		{
			if (player.hasCharmOfCourage())
			{
				if (player.isInSiege())
				{
					player.reviveRequest(player, false, 0, 0, 0, 0);
				}
				player.setCharmOfCourage(false);
				player.sendPacket(new EtcStatusUpdate(player));
			}
		}
		
		if (deleteBuffs)
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		broadcastStatusUpdate();
		
		ZoneManager.getInstance().getRegion(this).onDeath(this);
		
		// Notify Quest of Playable's death
		if (!player.isNotifyQuestOfDeathEmpty())
		{
			for (QuestState qs : player.getNotifyQuestOfDeath())
			{
				qs.getQuest().notifyDeath((killer == null ? this : killer), this, qs);
			}
		}
		
		// Notify instance
		if (isPlayer())
		{
			final Instance instance = getInstanceWorld();
			if (instance != null)
			{
				instance.onDeath(player);
			}
		}
		
		if (killer != null)
		{
			final Player killerPlayer = killer.asPlayer();
			if (killerPlayer != null)
			{
				killerPlayer.onPlayerKill(this);
			}
		}
		
		// Notify Creature AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		return true;
	}
	
	public boolean checkIfPvP(Player target)
	{
		final Player player = asPlayer();
		if ((player == null) //
			|| (target == null) //
			|| (player == target) //
			|| (target.getReputation() < 0) //
			|| (target.getPvpFlag() > 0))
		{
			return true;
		}
		else if (player.isInParty() && player.getParty().containsPlayer(target))
		{
			return false;
		}
		
		final Clan playerClan = player.getClan();
		final Clan targetClan = target.getClan();
		if ((playerClan != null) && (targetClan != null) && !player.isAcademyMember() && !target.isAcademyMember())
		{
			if (playerClan.isAtWarWith(targetClan.getId()))
			{
				if (targetClan.isAtWarWith(playerClan.getId()))
				{
					return true;
				}
			}
		}
		return false;
		
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean canBeAttacked()
	{
		return true;
	}
	
	// Support for Noblesse Blessing skill, where buffs are retained after resurrect
	public boolean isNoblesseBlessedAffected()
	{
		return isAffected(EffectFlag.NOBLESS_BLESSING);
	}
	
	/**
	 * @return {@code true} if char can resurrect by himself, {@code false} otherwise
	 */
	public boolean isResurrectSpecialAffected()
	{
		return isAffected(EffectFlag.RESURRECTION_SPECIAL);
	}
	
	/**
	 * @return {@code true} if the Silent Moving mode is active, {@code false} otherwise
	 */
	public boolean isSilentMovingAffected()
	{
		return isAffected(EffectFlag.SILENT_MOVE);
	}
	
	/**
	 * For Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you.
	 * @return
	 */
	public boolean isProtectionBlessingAffected()
	{
		return isAffected(EffectFlag.PROTECTION_BLESSING);
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		getEffectList().updateEffectIcons(partyOnly);
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget != null;
	}
	
	public Creature getLockedTarget()
	{
		return _lockedTarget;
	}
	
	public void setLockedTarget(Creature creature)
	{
		_lockedTarget = creature;
	}
	
	public void setTransferDamageTo(Player val)
	{
		transferDmgTo = val;
	}
	
	public Player getTransferingDamageTo()
	{
		return transferDmgTo;
	}
	
	public abstract void doPickupItem(WorldObject object);
	
	public abstract boolean useMagic(Skill skill, Item item, boolean forceUse, boolean dontMove);
	
	public abstract void storeMe();
	
	public abstract void storeEffect(boolean storeEffects);
	
	public abstract void restoreEffects();
	
	public boolean isOnEvent()
	{
		return false;
	}
	
	@Override
	public boolean isPlayable()
	{
		return true;
	}
	
	@Override
	public Playable asPlayable()
	{
		return this;
	}
}
