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
package org.classiclude.gameserver.model.itemcontainer;

import org.classiclude.Config;
import org.classiclude.gameserver.enums.ItemLocation;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.clan.Clan;
import org.classiclude.gameserver.model.events.EventDispatcher;
import org.classiclude.gameserver.model.events.EventType;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerClanWHItemAdd;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerClanWHItemDestroy;
import org.classiclude.gameserver.model.events.impl.creature.player.OnPlayerClanWHItemTransfer;
import org.classiclude.gameserver.model.item.instance.Item;

public class ClanWarehouse extends Warehouse
{
	private final Clan _clan;
	
	public ClanWarehouse(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	public String getName()
	{
		return "ClanWarehouse";
	}
	
	@Override
	public int getOwnerId()
	{
		return _clan.getId();
	}
	
	@Override
	public Player getOwner()
	{
		return _clan.getLeader().getPlayer();
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}
	
	@Override
	public boolean validateCapacity(long slots)
	{
		return (_items.size() + slots) <= Config.WAREHOUSE_SLOTS_CLAN;
	}
	
	@Override
	public Item addItem(String process, int itemId, long count, Player actor, Object reference)
	{
		final Item item = super.addItem(process, itemId, count, actor, reference);
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_ADD, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemAdd(process, actor, item, this), item.getTemplate());
		}
		
		return item;
	}
	
	@Override
	public Item addItem(String process, Item item, Player actor, Object reference)
	{
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_ADD, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemAdd(process, actor, item, this), item.getTemplate());
		}
		
		return super.addItem(process, item, actor, reference);
	}
	
	@Override
	public Item destroyItem(String process, Item item, long count, Player actor, Object reference)
	{
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_DESTROY, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemDestroy(process, actor, item, count, this), item.getTemplate());
		}
		
		return super.destroyItem(process, item, count, actor, reference);
	}
	
	@Override
	public Item transferItem(String process, int objectId, long count, ItemContainer target, Player actor, Object reference)
	{
		final Item item = getItemByObjectId(objectId);
		
		// Notify to scripts
		if (EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_CLAN_WH_ITEM_TRANSFER, item.getTemplate()))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerClanWHItemTransfer(process, actor, item, count, target), item.getTemplate());
		}
		
		return super.transferItem(process, objectId, count, target, actor, reference);
	}
}
