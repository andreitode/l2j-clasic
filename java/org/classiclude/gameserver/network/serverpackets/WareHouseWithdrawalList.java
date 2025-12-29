package org.classiclude.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.item.instance.Item;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.PacketLogger;
import org.classiclude.gameserver.network.ServerPackets;

/**
 * @author Mobius
 */
public class WareHouseWithdrawalList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 1;
	
	private Player _player;
	private long _playerAdena;
	private final int _invSize;
	private Collection<Item> _items;
	private final List<Integer> _itemsStackable = new ArrayList<>();
	/**
	 * <ul>
	 * <li>0x01-Private Warehouse</li>
	 * <li>0x02-Clan Warehouse</li>
	 * <li>0x03-Castle Warehouse</li>
	 * <li>0x04-Warehouse</li>
	 * </ul>
	 */
	private int _whType;
	
	public WareHouseWithdrawalList(Player player, int type)
	{
		_player = player;
		_whType = type;
		_playerAdena = _player.getAdena();
		_invSize = player.getInventory().getSize();
		if (_player.getActiveWarehouse() == null)
		{
			PacketLogger.warning("error while sending withdraw request to: " + _player.getName());
			return;
		}
		_items = _player.getActiveWarehouse().getItems();
		for (Item item : _items)
		{
			if (item.isStackable())
			{
				_itemsStackable.add(item.getDisplayId());
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.WAREHOUSE_WITHDRAW_LIST.writeId(this, buffer);
		buffer.writeShort(_whType);
		buffer.writeLong(_playerAdena);
		buffer.writeShort(_items.size());
		buffer.writeShort(_itemsStackable.size());
		for (int itemId : _itemsStackable)
		{
			buffer.writeInt(itemId);
		}
		buffer.writeInt(_invSize);
		for (Item item : _items)
		{
			writeItem(item, buffer);
			buffer.writeInt(item.getObjectId());
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
