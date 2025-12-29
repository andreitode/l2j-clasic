package org.classiclude.gameserver.network;

import java.util.logging.Logger;

import org.classiclude.commons.network.PacketHandler;
import org.classiclude.commons.network.ReadableBuffer;
import org.classiclude.commons.network.ReadablePacket;
import org.classiclude.commons.util.CommonUtil;
import org.classiclude.gameserver.network.clientpackets.RequestBookMarkSlotInfo;
import org.classiclude.gameserver.network.clientpackets.RequestChangeBookMarkSlot;
import org.classiclude.gameserver.network.clientpackets.RequestDeleteBookMarkSlot;
import org.classiclude.gameserver.network.clientpackets.RequestModifyBookMarkSlot;
import org.classiclude.gameserver.network.clientpackets.RequestSaveBookMarkSlot;
import org.classiclude.gameserver.network.clientpackets.RequestTeleportBookMark;

/**
 * @author Mobius
 */
public class GamePacketHandler implements PacketHandler<GameClient>
{
	private static final Logger LOGGER = Logger.getLogger(GamePacketHandler.class.getName());
	
	@Override
	public ReadablePacket<GameClient> handlePacket(ReadableBuffer buffer, GameClient client)
	{
		// Read packet id.
		final int packetId;
		try
		{
			packetId = Byte.toUnsignedInt(buffer.readByte());
		}
		catch (Exception e)
		{
			LOGGER.warning("PacketHandler: Problem receiving packet id from " + client);
			LOGGER.warning(CommonUtil.getStackTrace(e));
			client.closeNow();
			return null;
		}
		
		// Ex client packet.
		if (packetId == 0xD0)
		{
			// Check if packet id is within valid range.
			final int exPacketId = Short.toUnsignedInt(buffer.readShort());
			if ((exPacketId < 0) || (exPacketId >= ExClientPackets.PACKET_ARRAY.length))
			{
				return null;
			}
			
			// Find packet enum.
			final ExClientPackets packetEnum = ExClientPackets.PACKET_ARRAY[exPacketId];
			if (packetEnum == null)
			{
				return null;
			}
			
			// Check connection state.
			if (!packetEnum.getConnectionStates().contains(client.getConnectionState()))
			{
				return null;
			}
			
			// Ex bookmark packet.
			if (exPacketId == 0x4E)
			{
				final int subId = buffer.readInt();
				switch (subId)
				{
					case 0:
					{
						return new RequestBookMarkSlotInfo();
					}
					case 1:
					{
						return new RequestSaveBookMarkSlot();
					}
					case 2:
					{
						return new RequestModifyBookMarkSlot();
					}
					case 3:
					{
						return new RequestDeleteBookMarkSlot();
					}
					case 4:
					{
						return new RequestTeleportBookMark();
					}
					case 5:
					{
						return new RequestChangeBookMarkSlot();
					}
				}
				return null;
			}
			
			// Create new ClientPacket.
			return packetEnum.newPacket();
		}
		
		// Check if packet id is within valid range.
		if ((packetId < 0) || (packetId >= ClientPackets.PACKET_ARRAY.length))
		{
			return null;
		}
		
		// Find packet enum.
		final ClientPackets packetEnum = ClientPackets.PACKET_ARRAY[packetId];
		if (packetEnum == null)
		{
			return null;
		}
		
		// Check connection state.
		if (!packetEnum.getConnectionStates().contains(client.getConnectionState()))
		{
			return null;
		}
		
		// Create new ClientPacket.
		return packetEnum.newPacket();
	}
}
