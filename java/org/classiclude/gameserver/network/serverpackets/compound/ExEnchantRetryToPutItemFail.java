
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Sdw
 */
public class ExEnchantRetryToPutItemFail extends ServerPacket
{
	public static final ExEnchantRetryToPutItemFail STATIC_PACKET = new ExEnchantRetryToPutItemFail();
	
	private ExEnchantRetryToPutItemFail()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_RETRY_TO_PUT_ITEM_FAIL.writeId(this, buffer);
	}
}