package org.classiclude.gameserver.network.serverpackets.primeshop;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.enums.ExBrProductReplyType;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Gnacik, UnAfraid
 */
public class ExBRBuyProduct extends ServerPacket
{
	private final int _reply;
	
	public ExBRBuyProduct(ExBrProductReplyType type)
	{
		_reply = type.getId();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_BUY_PRODUCT.writeId(this, buffer);
		buffer.writeInt(_reply);
	}
}
