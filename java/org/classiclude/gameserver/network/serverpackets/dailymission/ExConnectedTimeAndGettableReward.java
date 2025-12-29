package org.classiclude.gameserver.network.serverpackets.dailymission;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.gameserver.data.xml.DailyMissionData;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Ren
 */
public class ExConnectedTimeAndGettableReward extends ServerPacket
{
	private final int _oneDayRewardAvailableCount;
	
	public ExConnectedTimeAndGettableReward(Player player)
	{
		_oneDayRewardAvailableCount = (int) DailyMissionData.getInstance().getDailyMissionData(player).stream().filter(d -> d.getStatus(player) == 1).count();
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (!DailyMissionData.getInstance().isAvailable())
		{
			return;
		}
		
		ServerPackets.EX_CONNECTED_TIME_AND_GETTABLE_REWARD.writeId(this, buffer);
		buffer.writeInt(0);
		buffer.writeInt(_oneDayRewardAvailableCount);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
