package org.classiclude.gameserver.network.serverpackets.primeshop;

import java.util.Collection;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.primeshop.PrimeShopGroup;
import org.classiclude.gameserver.model.primeshop.PrimeShopItem;
import org.classiclude.gameserver.model.variables.AccountVariables;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExBRProductList extends ServerPacket
{
	private final Player _player;
	private final int _type;
	private final Collection<PrimeShopGroup> _primeList;
	
	public ExBRProductList(Player player, int type, Collection<PrimeShopGroup> items)
	{
		_player = player;
		_type = type;
		_primeList = items;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_PRODUCT_LIST.writeId(this, buffer);
		buffer.writeLong(_player.getAdena()); // Adena
		buffer.writeLong(0); // Hero coins
		buffer.writeByte(_type); // Type 0 - Home, 1 - History, 2 - Favorites
		buffer.writeInt(_primeList.size());
		for (PrimeShopGroup brItem : _primeList)
		{
			buffer.writeInt(brItem.getBrId());
			buffer.writeByte(brItem.getCat());
			buffer.writeByte(brItem.getPaymentType()); // Payment Type: 0 - Prime Points, 1 - Adena, 2 - Hero Coins
			buffer.writeInt(brItem.getPrice());
			buffer.writeByte(brItem.getPanelType()); // Item Panel Type: 0 - None, 1 - Event, 2 - Sale, 3 - New, 4 - Best
			buffer.writeInt(brItem.getRecommended()); // Recommended: (bit flags) 1 - Top, 2 - Left, 4 - Right
			buffer.writeInt(brItem.getStartSale());
			buffer.writeInt(brItem.getEndSale());
			buffer.writeByte(brItem.getDaysOfWeek());
			buffer.writeByte(brItem.getStartHour());
			buffer.writeByte(brItem.getStartMinute());
			buffer.writeByte(brItem.getStopHour());
			buffer.writeByte(brItem.getStopMinute());
			
			// Daily account limit.
			if ((brItem.getAccountDailyLimit() > 0) && (_player.getAccountVariables().getInt(AccountVariables.PRIME_SHOP_PRODUCT_DAILY_COUNT + brItem.getBrId(), 0) >= brItem.getAccountDailyLimit()))
			{
				buffer.writeInt(brItem.getAccountDailyLimit());
				buffer.writeInt(brItem.getAccountDailyLimit());
			}
			// General account limit.
			else if ((brItem.getAccountBuyLimit() > 0) && (_player.getAccountVariables().getInt(AccountVariables.PRIME_SHOP_PRODUCT_COUNT + brItem.getBrId(), 0) >= brItem.getAccountBuyLimit()))
			{
				buffer.writeInt(brItem.getAccountBuyLimit());
				buffer.writeInt(brItem.getAccountBuyLimit());
			}
			else
			{
				buffer.writeInt(brItem.getStock());
				buffer.writeInt(brItem.getTotal());
			}
			
			buffer.writeByte(brItem.getSalePercent());
			buffer.writeByte(brItem.getMinLevel());
			buffer.writeByte(brItem.getMaxLevel());
			buffer.writeInt(brItem.getMinBirthday());
			buffer.writeInt(brItem.getMaxBirthday());
			
			// Daily account limit.
			if (brItem.getAccountDailyLimit() > 0)
			{
				buffer.writeInt(1); // Days
				buffer.writeInt(brItem.getAccountDailyLimit()); // Amount
			}
			// General account limit.
			else if (brItem.getAccountBuyLimit() > 0)
			{
				buffer.writeInt(-1); // Days
				buffer.writeInt(brItem.getAccountBuyLimit()); // Amount
			}
			else
			{
				buffer.writeInt(0); // Days
				buffer.writeInt(0); // Amount
			}
			
			buffer.writeByte(brItem.getItems().size());
			for (PrimeShopItem item : brItem.getItems())
			{
				buffer.writeInt(item.getId());
				buffer.writeInt((int) item.getCount());
				buffer.writeInt(item.getWeight());
				buffer.writeInt(item.isTradable());
			}
		}
	}
}