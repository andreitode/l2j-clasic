package org.classiclude.gameserver.network.serverpackets.vip;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.classiclude.commons.network.WritableBuffer;
import org.classiclude.Config;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.model.vip.VipManager;
import org.classiclude.gameserver.network.GameClient;
import org.classiclude.gameserver.network.ServerPackets;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;

public class ReceiveVipInfo extends ServerPacket
{
	private final Player _player;
	
	public ReceiveVipInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		if (!Config.VIP_SYSTEM_ENABLED)
		{
			return;
		}
		
		final VipManager vipManager = VipManager.getInstance();
		final byte vipTier = _player.getVipTier();
		final int vipDuration = (int) ChronoUnit.SECONDS.between(Instant.now(), Instant.ofEpochMilli(_player.getVipTierExpiration()));
		
		ServerPackets.RECIVE_VIP_INFO.writeId(this, buffer);
		buffer.writeByte(vipTier);
		buffer.writeLong(_player.getVipPoints());
		buffer.writeInt(vipDuration);
		buffer.writeLong(vipManager.getPointsToLevel((byte) (vipTier + 1)));
		buffer.writeLong(vipManager.getPointsDepreciatedOnLevel(vipTier));
		buffer.writeByte(vipTier);
		buffer.writeLong(vipManager.getPointsToLevel(vipTier));
	}
}