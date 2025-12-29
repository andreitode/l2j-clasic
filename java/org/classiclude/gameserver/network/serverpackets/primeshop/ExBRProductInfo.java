package org.classiclude.gameserver.network.serverpackets.primeshop;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.primeshop.PrimeShopGroup;
import org.classiclude.gameserver.model.primeshop.PrimeShopItem;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Gnacik
 */
public class ExBRProductInfo extends ServerPacket
{
	private final PrimeShopGroup _item;
	private final int _charPoints;
	private final long _charAdena;
	private final long _charCoins;
	
	public ExBRProductInfo(PrimeShopGroup item, Player player)
	{
		_item = item;
		_charPoints = player.getPrimePoints();
		_charAdena = player.getAdena();
		_charCoins = player.getInventory().getInventoryItemCount(23805, -1);
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_PRODUCT_INFO.writeId(this, buffer);
		buffer.writeInt(_item.getBrId());
		buffer.writeInt(_item.getPrice());
		buffer.writeInt(_item.getItems().size());
		for (PrimeShopItem item : _item.getItems())
		{
			buffer.writeInt(item.getId());
			buffer.writeInt((int) item.getCount());
			buffer.writeInt(item.getWeight());
			buffer.writeInt(item.isTradable());
		}
		buffer.writeLong(_charAdena);
		buffer.writeLong(_charPoints);
		buffer.writeLong(_charCoins); // Hero coins
	}
}
