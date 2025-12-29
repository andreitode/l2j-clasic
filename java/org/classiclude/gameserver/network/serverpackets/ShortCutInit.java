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

import java.util.Collection;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.Shortcut;
import org.classiclude.gameserver.model.VariationInstance;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class ShortCutInit extends ServerPacket
{
	private final Player _player;
	private final Collection<Shortcut> _shortCuts;
	
	public ShortCutInit(Player player)
	{
		_player = player;
		_shortCuts = player.getAllShortCuts();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHORT_CUT_INIT.writeId(this, buffer);
		buffer.writeInt(_shortCuts.size());
		Player receiver = client.getPlayer();
		for (Shortcut sc : _shortCuts)
		{
			buffer.writeInt(sc.getType().ordinal());
			buffer.writeInt(sc.getSlot() + (sc.getPage() * 12));
			switch (sc.getType())
			{
				case ITEM:
				{
					buffer.writeInt(sc.getId());
					buffer.writeInt(1); // Enabled or not
					buffer.writeInt(sc.getSharedReuseGroup());
					buffer.writeInt(0);
					buffer.writeInt(0);
					
					final Item item = _player.getInventory().getItemByObjectId(sc.getId());
					if (item != null)
					{
						final VariationInstance augment = item.getAugmentation();
						if (augment != null)
						{
							buffer.writeInt(augment.getOption1Id());
							buffer.writeInt(augment.getOption2Id());
						}
						else
						{
							buffer.writeInt(0);
							buffer.writeInt(0);
						}
						if (receiver.isHideAppearanceEnabled())
						{
							int slot = sc.getSlot();
							Item item1 = _player.getInventory().getPaperdollItem(slot);
							buffer.writeInt(item1 != null ? item1.getTemplate().getId() : 0);
						}
						else
						{
							buffer.writeInt(item.getVisualId());
						}
					
					}
					else
					{
						buffer.writeInt(0);
						buffer.writeInt(0);
						buffer.writeInt(0);
					}
					break;
				}
				case SKILL:
				{
					buffer.writeInt(sc.getId());
					buffer.writeShort(sc.getLevel());
					buffer.writeShort(sc.getSubLevel());
					buffer.writeInt(sc.getSharedReuseGroup());
					buffer.writeByte(0); // C5
					buffer.writeInt(1); // C6
					break;
				}
				case ACTION:
				case MACRO:
				case RECIPE:
				case BOOKMARK:
				{
					buffer.writeInt(sc.getId());
					buffer.writeInt(1); // C6
				}
			}
		}
	}
}
