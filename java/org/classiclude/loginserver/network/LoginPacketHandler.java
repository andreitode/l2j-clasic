package org.classiclude.loginserver.network;

import java.util.logging.Logger;

import org.classiclude.commons.network.PacketHandler;
import org.classiclude.commons.network.ReadableBuffer;
import org.classiclude.commons.network.ReadablePacket;
import org.classiclude.commons.util.CommonUtil;
import org.classiclude.loginserver.enums.LoginFailReason;

/**
 * @author Mobius
 */
public class LoginPacketHandler implements PacketHandler<LoginClient>
{
	private static final Logger LOGGER = Logger.getLogger(LoginPacketHandler.class.getName());
	
	@Override
	public ReadablePacket<LoginClient> handlePacket(ReadableBuffer buffer, LoginClient client)
	{
		// Read packet id.
		final int packetId;
		try
		{
			packetId = Byte.toUnsignedInt(buffer.readByte());
		}
		catch (Exception e)
		{
			LOGGER.warning("LoginPacketHandler: Problem receiving packet id from " + client);
			LOGGER.warning(CommonUtil.getStackTrace(e));
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return null;
		}
		
		// Check if packet id is within valid range.
		if ((packetId < 0) || (packetId >= LoginClientPackets.PACKET_ARRAY.length))
		{
			return null;
		}
		
		// Find packet enum.
		final LoginClientPackets packetEnum = LoginClientPackets.PACKET_ARRAY[packetId];
		if (packetEnum == null)
		{
			return null;
		}
		
		// Check connection state.
		if (!packetEnum.getConnectionStates().contains(client.getConnectionState()))
		{
			return null;
		}
		
		// Create new LoginClientPacket.
		return packetEnum.newPacket();
	}
}
