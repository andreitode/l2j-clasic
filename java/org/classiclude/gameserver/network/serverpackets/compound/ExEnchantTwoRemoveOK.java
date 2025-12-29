
package org.classiclude.gameserver.network.serverpackets.compound;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnchantTwoRemoveOK extends ServerPacket
{
	public static final ExEnchantTwoRemoveOK STATIC_PACKET = new ExEnchantTwoRemoveOK();
	
	private ExEnchantTwoRemoveOK()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ENCHANT_TWO_REMOVE_OK.writeId(this, buffer);
	}
}
