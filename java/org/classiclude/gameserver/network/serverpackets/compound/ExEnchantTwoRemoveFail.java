
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnchantTwoRemoveFail extends ServerPacket
{
	public static final ExEnchantTwoRemoveFail STATIC_PACKET = new ExEnchantTwoRemoveFail();
	
	private ExEnchantTwoRemoveFail()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_TWO_REMOVE_FAIL.writeId(this, buffer);
	}
}
