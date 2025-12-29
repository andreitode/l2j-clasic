
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnchantOneRemoveFail extends ServerPacket
{
	public static final ExEnchantOneRemoveFail STATIC_PACKET = new ExEnchantOneRemoveFail();
	
	private ExEnchantOneRemoveFail()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_ONE_REMOVE_FAIL.writeId(this, buffer);
	}
}
