/*
 * This file is part of the ClassicLude project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.classiclude.gameserver.model.vip;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.classiclude.Config;
import org.classiclude.gameserver.data.xml.SkillData;
import org.classiclude.gameserver.data.xml.VipData;
import org.classiclude.gameserver.enums.ChatType;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.events.Containers;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerLoad;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.classiclude.gameserver.model.events.listeners.ConsumerEventListener;
import org.classiclude.gameserver.model.skill.Skill;
import org.classiclude.gameserver.network.serverpackets.CreatureSay;
import org.classiclude.gameserver.network.serverpackets.ExBRNewIconCashBtnWnd;
import org.classiclude.gameserver.network.serverpackets.vip.ReceiveVipInfo;

/**
 * @author Gabriel Costa Souza
 */
public class VipManager
{
	private static final byte VIP_MAX_TIER = (byte) Config.VIP_SYSTEM_MAX_TIER;
	
	private final ConsumerEventListener _vipLoginListener = new ConsumerEventListener(null, EventType.ON_PLAYER_LOGIN, (Consumer<OnPlayerLogin>) this::onVipLogin, this);
	
	protected VipManager()
	{
		if (!Config.VIP_SYSTEM_ENABLED)
		{
			return;
		}
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOAD, (Consumer<OnPlayerLoad>) this::onPlayerLoaded, this));
	}
	
	private void onPlayerLoaded(OnPlayerLoad event)
	{
		final Player player = event.getPlayer();
		player.setVipTier(getVipTier(player));
		if (player.getVipTier() > 0)
		{
			manageTier(player);
			player.addListener(_vipLoginListener);
		}
		else
		{
			player.sendPacket(new ReceiveVipInfo(player));
			player.sendPacket(new ExBRNewIconCashBtnWnd((byte) 0));
		}
	}
	
	/*
	 * private boolean canReceiveGift(Player player) { if (!Config.VIP_SYSTEM_ENABLED) { return false; } if (player.getVipTier() <= 0) { return false; } return player.getAccountVariables().getLong(AccountVariables.VIP_ITEM_BOUGHT, 0L) <= 0; }
	 */
	
	private void onVipLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		player.sendPacket(new ExBRNewIconCashBtnWnd((byte) 0));
		player.removeListener(_vipLoginListener);
		player.sendPacket(new ReceiveVipInfo(player));
	}
	
	public void manageTier(Player player)
	{
		if (!checkVipTierExpiration(player))
		{
			player.sendPacket(new ReceiveVipInfo(player));
		}
		
		if (player.getVipTier() > 1)
		{
			final int oldSkillId = VipData.getInstance().getSkillId((byte) (player.getVipTier() - 1));
			if (oldSkillId > 0)
			{
				final Skill oldSkill = SkillData.getInstance().getSkill(oldSkillId, 1);
				if (oldSkill != null)
				{
					player.removeSkill(oldSkill);
				}
			}
		}
		
		final int skillId = VipData.getInstance().getSkillId(player.getVipTier());
		if (skillId > 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(skillId, 1);
			if (skill != null)
			{
				player.addSkill(skill);
			}
		}
	}
	
	public byte getVipTier(Player player)
	{
		return getVipInfo(player).getTier();
	}
	
	public byte getVipTier(long points)
	{
		byte temp = getVipInfo(points).getTier();
		if (temp > VIP_MAX_TIER)
		{
			temp = VIP_MAX_TIER;
		}
		return temp;
	}
	
	private VipInfo getVipInfo(Player player)
	{
		return getVipInfo(player.getVipPoints());
	}
	
	public float getSilverCoinDropChance(Player player)
	{
		return getVipInfo(player).getSilverCoinChance();
	}
	
	public float getRustyCoinDropChance(Player player)
	{
		return getVipInfo(player).getGoldCoinChance();
	}
	
	private VipInfo getVipInfo(long points)
	{
		for (byte i = 0; i < VipData.getInstance().getVipTiers().size(); i++)
		{
			if (points < VipData.getInstance().getVipTiers().get(i).getPointsRequired())
			{
				byte temp = (byte) (i - 1);
				if (temp > VIP_MAX_TIER)
				{
					temp = VIP_MAX_TIER;
				}
				return VipData.getInstance().getVipTiers().get(temp);
			}
		}
		return VipData.getInstance().getVipTiers().get(VIP_MAX_TIER);
	}
	
	public long getPointsDepreciatedOnLevel(byte vipTier)
	{
		final VipInfo tier = VipData.getInstance().getVipTiers().get(vipTier);
		if (tier == null)
		{
			return 0;
		}
		return tier.getPointsDepreciated();
	}
	
	public long getPointsToLevel(byte vipTier)
	{
		final VipInfo tier = VipData.getInstance().getVipTiers().get(vipTier);
		if (tier == null)
		{
			return 0;
		}
		return tier.getPointsRequired();
	}
	
	public boolean checkVipTierExpiration(Player player)
	{
		final Instant now = Instant.now();
		long currentExpiration = player.getVipTierExpiration();
		
		if (now.isAfter(Instant.ofEpochMilli(currentExpiration)))
		{
			long pointsToDeduct = getPointsDepreciatedOnLevel(player.getVipTier());
			player.updateVipPoints(-pointsToDeduct);
			
			if (player.getVipPoints() > 0)
			{
				long newExpiration = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli();
				player.setVipTierExpiration(newExpiration);
				return true;
			}
			player.setVipTier((byte) 0);
			player.sendPacket(new ReceiveVipInfo(player));
			player.sendPacket(new CreatureSay(player, ChatType.WHISPER, "VIP System", "Your VIP status has expired. Thank you for collaborating with the server, if you wish you can activate it again with a new VIP scroll."));
			return false;
		}
		return true;
	}
	
	public static VipManager getInstance()
	{
		return Singleton.INSTANCE;
	}
	
	private static class Singleton
	{
		protected static final VipManager INSTANCE = new VipManager();
	}
}