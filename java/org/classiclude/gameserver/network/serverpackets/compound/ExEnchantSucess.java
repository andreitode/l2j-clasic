
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnchantSucess extends ServerPacket
{
	private final int _itemId;
	
	public ExEnchantSucess(int itemId)
	{
		_itemId = itemId;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_SUCESS.writeId(this, buffer);
		buffer.writeInt(_itemId);
	}
}
