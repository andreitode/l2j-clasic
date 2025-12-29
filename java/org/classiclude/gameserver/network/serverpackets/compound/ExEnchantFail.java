
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnchantFail extends ServerPacket
{
	public static final ExEnchantFail STATIC_PACKET = new ExEnchantFail(0, 0);
	
	private final int _itemOne;
	private final int _itemTwo;
	
	public ExEnchantFail(int itemOne, int itemTwo)
	{
		_itemOne = itemOne;
		_itemTwo = itemTwo;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_FAIL.writeId(this, buffer);
		buffer.writeInt(_itemOne);
		buffer.writeInt(_itemTwo);
	}
}
