package org.classiclude.gameserver.network.serverpackets.vip;

import java.util.Collection;

import org.classiclude.Config;
import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.xml.PrimeShopData;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.primeshop.PrimeShopGroup;
import org.classiclude.gameserver.model.primeshop.PrimeShopItem;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

public class ReceiveVipProductList extends ServerPacket
{
	private final Player _player;
	
	public ReceiveVipProductList(Player player)
	{
		_player = player;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (!Config.VIP_SYSTEM_ENABLED)
		{
			return;
		}
		
		final Collection<PrimeShopGroup> products = PrimeShopData.getInstance().getPrimeItems().values();
		final PrimeShopGroup gift = PrimeShopData.getInstance().getVipGiftOfTier(_player.getVipTier());
		ServerPackets.RECIVE_VIP_PRODUCT_LIST.writeId(this, buffer);
		buffer.writeLong(_player.getAdena());
		buffer.writeLong(_player.getGoldCoin()); // Gold Coin Amount
		buffer.writeLong(_player.getSilverCoin()); // Silver Coin Amount
		buffer.writeByte(1); // Show Reward tab
		if (gift != null)
		{
			buffer.writeInt(products.size() + 1);
			writeProduct(gift, buffer);
		}
		else
		{
			buffer.writeInt(products.size());
		}
		for (PrimeShopGroup product : products)
		{
			writeProduct(product, buffer);
		}
	}
	
	private void writeProduct(PrimeShopGroup product, WritableBuffer buffer)
	{
		buffer.writeInt(product.getBrId());
		buffer.writeByte(product.getCat());
		buffer.writeByte(product.getPaymentType());
		buffer.writeInt(product.getPrice()); // L2 Coin | Gold Coin seems to use the same field based on payment type
		buffer.writeInt(product.getSilverCoin());
		buffer.writeByte(product.getPanelType()); // NEW - 6; HOT - 5 ... Unk
		buffer.writeByte(product.getVipTier());
		buffer.writeByte(10);
		buffer.writeByte(product.getItems().size());
		for (PrimeShopItem item : product.getItems())
		{
			buffer.writeInt(item.getId());
			buffer.writeInt((int) item.getCount());
		}
	}
}
