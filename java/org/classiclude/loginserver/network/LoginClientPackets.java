package org.classiclude.loginserver.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.classiclude.loginserver.network.clientpackets.AuthGameGuard;
import org.classiclude.loginserver.network.clientpackets.LoginClientPacket;
import org.classiclude.loginserver.network.clientpackets.RequestAuthLogin;
import org.classiclude.loginserver.network.clientpackets.RequestCmdLogin;
import org.classiclude.loginserver.network.clientpackets.RequestPIAgreement;
import org.classiclude.loginserver.network.clientpackets.RequestPIAgreementCheck;
import org.classiclude.loginserver.network.clientpackets.RequestServerList;
import org.classiclude.loginserver.network.clientpackets.RequestServerLogin;

/**
 * @author Mobius
 */
public enum LoginClientPackets
{
	AUTH_GAME_GUARD(0x07, AuthGameGuard::new, ConnectionState.CONNECTED),
	REQUEST_AUTH_LOGIN(0x00, RequestAuthLogin::new, ConnectionState.AUTHED_GG),
	REQUEST_LOGIN(0x0B, RequestCmdLogin::new, ConnectionState.AUTHED_GG),
	REQUEST_SERVER_LOGIN(0x02, RequestServerLogin::new, ConnectionState.AUTHED_LOGIN),
	REQUEST_SERVER_LIST(0x05, RequestServerList::new, ConnectionState.AUTHED_LOGIN),
	REQUEST_PI_AGREEMENT_CHECK(0x0E, RequestPIAgreementCheck::new, ConnectionState.AUTHED_LOGIN),
	REQUEST_PI_AGREEMENT(0x0F, RequestPIAgreement::new, ConnectionState.AUTHED_LOGIN);
	
	public static final LoginClientPackets[] PACKET_ARRAY;
	static
	{
		final short maxPacketId = (short) Arrays.stream(values()).mapToInt(LoginClientPackets::getPacketId).max().orElse(0);
		PACKET_ARRAY = new LoginClientPackets[maxPacketId + 1];
		for (LoginClientPackets packet : values())
		{
			PACKET_ARRAY[packet.getPacketId()] = packet;
		}
	}
	
	private final short _packetId;
	private final Supplier<LoginClientPacket> _packetSupplier;
	private final Set<ConnectionState> _connectionStates;
	
	LoginClientPackets(int packetId, Supplier<LoginClientPacket> packetSupplier, ConnectionState... connectionStates)
	{
		// Packet id is an unsigned byte.
		if (packetId > 0xFF)
		{
			throw new IllegalArgumentException("Packet id must not be bigger than 0xFF");
		}
		
		_packetId = (short) packetId;
		_packetSupplier = packetSupplier != null ? packetSupplier : () -> null;
		_connectionStates = new HashSet<>(Arrays.asList(connectionStates));
	}
	
	public int getPacketId()
	{
		return _packetId;
	}
	
	public LoginClientPacket newPacket()
	{
		return _packetSupplier.get();
	}
	
	public Set<ConnectionState> getConnectionStates()
	{
		return _connectionStates;
	}
}
