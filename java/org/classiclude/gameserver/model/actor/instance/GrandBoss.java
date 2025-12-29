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
package org.classiclude.gameserver.model.actor.instance;

import org.classiclude.gameserver.enums.InstanceType;
import org.classiclude.gameserver.model.Party;
import org.classiclude.gameserver.model.actor.Creature;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.actor.templates.NpcTemplate;
import org.classiclude.gameserver.model.olympiad.Hero;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages all Grand Bosses.
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public class GrandBoss extends Monster
{
	private boolean _useRaidCurse = true;
	
	/**
	 * Creates a grand boss.
	 * @param template the grand boss NPC template
	 */
	public GrandBoss(NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.GrandBoss);
		setIsRaid(true);
		setLethalable(false);
	}
	
	@Override
	public void onSpawn()
	{
		setRandomWalking(false);
		super.onSpawn();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (killer != null)
		{
			final Player player = killer.asPlayer();
			if (player != null)
			{
				broadcastPacket(new SystemMessage(SystemMessageId.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL));
				
				final Party party = player.getParty();
				if (party != null)
				{
					for (Player member : party.getMembers())
					{
						if (member.isNoble())
						{
							Hero.getInstance().setRBkilled(member.getObjectId(), getId());
						}
					}
				}
				else
				{
					if (player.isNoble())
					{
						Hero.getInstance().setRBkilled(player.getObjectId(), getId());
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean useVitalityRate()
	{
		return false;
	}
	
	public void setUseRaidCurse(boolean value)
	{
		_useRaidCurse = value;
	}
	
	@Override
	public boolean giveRaidCurse()
	{
		return _useRaidCurse;
	}
}