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
package handlers.itemhandlers;

import java.util.List;

import org.classiclude.gameserver.enums.ItemSkillType;
import org.classiclude.gameserver.enums.ShotType;
import org.classiclude.gameserver.handler.IItemHandler;
import org.classiclude.gameserver.model.actor.Playable;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.holders.ItemSkillHolder;
import org.classiclude.gameserver.model.item.Weapon;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.model.item.type.ActionType;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.MagicSkillUse;
import org.classiclude.gameserver.util.Broadcast;

public class BlessedSpiritShot implements IItemHandler
{
	// disable blessed on olympiad
	private static final int[] BLESSED_SPIRITSHOT_IDS =
	{
		3947, // D-grade Blessed Spirit Shot
		3948, // C-grade Blessed Spirit Shot
		3949, // B-grade Blessed Spirit Shot
		3950, // A-grade Blessed Spirit Shot
		3951, // S-grade Blessed Spirit Shot
		3952
	};
	
	// disable blessed on olympiad
	private boolean isBlessedSpiritShot(int itemId)
	{
		for (int id : BLESSED_SPIRITSHOT_IDS)
		{
			if (id == itemId)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = playable.asPlayer();
		final Item weaponInst = player.getActiveWeaponInstance();
		final Weapon weaponItem = player.getActiveWeaponItem();
		final List<ItemSkillHolder> skills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
		if (skills == null)
		{
			LOGGER.warning(getClass().getSimpleName() + ": is missing skills!");
			return false;
		}
		
		final int itemId = item.getId();
		
		// disable blessed on olympiad
		if (player.isInOlympiadMode() && isBlessedSpiritShot(itemId))
		{
			return false;
		}
		
		// Check if Blessed SpiritShot can be used
		if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
		{
			if (!player.getAutoSoulShot().contains(itemId))
			{
				player.sendPacket(SystemMessageId.YOU_MAY_NOT_USE_SPIRITSHOTS);
			}
			return false;
		}
		
		// Check if Blessed SpiritShot is already active (it can be charged over SpiritShot)
		if (player.isChargedShot(ShotType.BLESSED_SPIRITSHOTS))
		{
			return false;
		}
		
		// Check for correct grade
		final boolean gradeCheck = item.isEtcItem() && (item.getEtcItem().getDefaultAction() == ActionType.SPIRITSHOT) && (weaponInst.getTemplate().getCrystalTypePlus() == item.getTemplate().getCrystalTypePlus());
		if (!gradeCheck)
		{
			if (!player.getAutoSoulShot().contains(itemId))
			{
				player.sendPacket(SystemMessageId.YOUR_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE);
			}
			
			return false;
		}
		
		// Consume Blessed SpiritShot if player has enough of them
		if (!player.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (!player.disableAutoShot(itemId))
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOT_FOR_THAT);
			}
			return false;
		}
		
		// Charge Spirit shot
		player.chargeShot(ShotType.BLESSED_SPIRITSHOTS);
		
		// Send message to client
		if (!player.getAutoSoulShot().contains(item.getId()))
		{
			player.sendPacket(SystemMessageId.YOUR_SPIRITSHOT_HAS_BEEN_ENABLED);
		}
		
		// Visual effect change if player has equipped Sapphire level 3 or higher
		if (player.getActiveShappireJewel() != null)
		{
			Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, player.getActiveShappireJewel().getSkillId(), player.getActiveShappireJewel().getSkillLevel(), 0, 0), 600);
		}
		else
		{
			skills.forEach(holder -> Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, holder.getSkillId(), holder.getSkillLevel(), 0, 0), 600));
		}
		return true;
	}
}
