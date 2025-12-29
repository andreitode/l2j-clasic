
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Sdw
 */
public class ExEnchantRetryToPutItemOk extends ServerPacket
{
	public static final ExEnchantRetryToPutItemOk STATIC_PACKET = new ExEnchantRetryToPutItemOk();
	
	private ExEnchantRetryToPutItemOk()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_RETRY_TO_PUT_ITEM_OK.writeId(this, buffer);
	}
}