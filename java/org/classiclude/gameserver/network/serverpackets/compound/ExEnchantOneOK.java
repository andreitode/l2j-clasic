
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnchantOneOK extends ServerPacket
{
	public static final ExEnchantOneOK STATIC_PACKET = new ExEnchantOneOK();
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_ONE_OK.writeId(this, buffer);
	}
}
