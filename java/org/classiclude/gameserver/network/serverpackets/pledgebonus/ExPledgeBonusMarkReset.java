package org.classiclude.gameserver.network.serverpackets.pledgebonus;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExPledgeBonusMarkReset extends ServerPacket
{
	public static ExPledgeBonusMarkReset STATIC_PACKET = new ExPledgeBonusMarkReset();
	
	private ExPledgeBonusMarkReset()
	{
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_BONUS_MARK_RESET.writeId(this, buffer);
	}
}
