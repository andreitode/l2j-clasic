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
package org.classiclude.gameserver.network.serverpackets.commission;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.ItemInfo;
import org.classiclude.gameserver.model.commission.CommissionItem;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author NosBit
 */
public class ExResponseCommissionBuyItem extends ServerPacket
{
	public static final ExResponseCommissionBuyItem FAILED = new ExResponseCommissionBuyItem(null);
	
	private final CommissionItem _commissionItem;
	
	public ExResponseCommissionBuyItem(CommissionItem commissionItem)
	{
		_commissionItem = commissionItem;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_COMMISSION_BUY_ITEM.writeId(this, buffer);
		buffer.writeInt(_commissionItem != null);
		if (_commissionItem != null)
		{
			final ItemInfo itemInfo = _commissionItem.getItemInfo();
			buffer.writeInt(itemInfo.getEnchantLevel());
			buffer.writeInt(itemInfo.getItem().getId());
			buffer.writeLong(itemInfo.getCount());
		}
	}
}
