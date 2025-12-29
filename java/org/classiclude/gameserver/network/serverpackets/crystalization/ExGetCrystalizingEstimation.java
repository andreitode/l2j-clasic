package org.classiclude.gameserver.network.serverpackets.crystalization;

import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.holders.ItemChanceHolder;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExGetCrystalizingEstimation extends ServerPacket
{
	private final List<ItemChanceHolder> _items;
	
	public ExGetCrystalizingEstimation(List<ItemChanceHolder> items)
	{
		_items = items;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_GET_CRYSTALIZING_ESTIMATION.writeId(this, buffer);
		buffer.writeInt(_items.size());
		for (ItemChanceHolder holder : _items)
		{
			buffer.writeInt(holder.getId());
			buffer.writeLong(holder.getCount());
			buffer.writeDouble(holder.getChance());
		}
	}
}
