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
package org.classiclude.gameserver.network.serverpackets;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.InventorySlot;
import org.classiclude.gameserver.model.VariationInstance;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.itemcontainer.PlayerInventory;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class ExUserInfoEquipSlot extends AbstractMaskPacket<InventorySlot>
{
	private final Player _player;
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	public ExUserInfoEquipSlot(Player player)
	{
		this(player, true);
	}
	
	public ExUserInfoEquipSlot(Player player, boolean addAll)
	{
		_player = player;
		if (addAll)
		{
			addComponentType(InventorySlot.values());
		}
	}
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_USER_INFO_EQUIP_SLOT.writeId(this, buffer);
		buffer.writeInt(_player.getObjectId());
		buffer.writeShort(InventorySlot.values().length);
		buffer.writeBytes(_masks);
		final PlayerInventory inventory = _player.getInventory();
		for (InventorySlot slot : InventorySlot.values())
		{
			if (containsMask(slot))
			{
				final VariationInstance augment = inventory.getPaperdollAugmentation(slot.getSlot());
				buffer.writeShort(22); // 10 + 4 * 3
				buffer.writeInt(inventory.getPaperdollObjectId(slot.getSlot()));
				buffer.writeInt(inventory.getPaperdollItemId(slot.getSlot()));
				buffer.writeInt(augment != null ? augment.getOption1Id() : 0);
				buffer.writeInt(augment != null ? augment.getOption2Id() : 0);
				buffer.writeInt(inventory.getPaperdollItemVisualId(slot.getSlot()));
			}
		}
	}
}