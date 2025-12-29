package org.classiclude.gameserver.network.serverpackets.training;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Sdw
 */
public class ExTrainingZone_Leaving extends ServerPacket
{
	public static final ExTrainingZone_Leaving STATIC_PACKET = new ExTrainingZone_Leaving();
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TRAINING_ZONE_LEAVING.writeId(this, buffer);
	}
}
