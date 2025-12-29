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
package org.classiclude.gameserver.network.clientpackets;

import org.classiclude.gameserver.enums.PrivateStoreType;
import org.classiclude.gameserver.instancemanager.RecipeManager;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.util.Util;

public class RequestRecipeShopMakeItem extends ClientPacket
{
	private int _id;
	private int _recipeId;
	@SuppressWarnings("unused")
	private long _unknown;
	
	@Override
	protected void readImpl()
	{
		_id = readInt();
		_recipeId = readInt();
		_unknown = readLong();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().canManufacture())
		{
			return;
		}
		
		final Player manufacturer = World.getInstance().getPlayer(_id);
		if (manufacturer == null)
		{
			return;
		}
		
		if (manufacturer.getInstanceWorld() != player.getInstanceWorld())
		{
			return;
		}
		
		if (player.isInStoreMode())
		{
			player.sendMessage("You cannot create items while trading.");
			return;
		}
		if (manufacturer.getPrivateStoreType() != PrivateStoreType.MANUFACTURE)
		{
			// player.sendMessage("You cannot create items while trading.");
			return;
		}
		
		if (player.isCrafting() || manufacturer.isCrafting())
		{
			player.sendMessage("You are currently in Craft Mode.");
			return;
		}
		if (Util.checkIfInRange(150, player, manufacturer, true))
		{
			RecipeManager.getInstance().requestManufactureItem(manufacturer, _recipeId, player);
		}
	}
}
