
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnchantTwoFail extends ServerPacket
{
	public static final ExEnchantTwoFail STATIC_PACKET = new ExEnchantTwoFail();
	
	private ExEnchantTwoFail()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_TWO_FAIL.writeId(this, buffer);
	}
}
