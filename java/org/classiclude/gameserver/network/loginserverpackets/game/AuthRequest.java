package org.classiclude.gameserver.network.loginserverpackets.game;

import java.util.List;

import org.classiclude.commons.network.base.BaseWritablePacket;

public class AuthRequest extends BaseWritablePacket
{
	public AuthRequest(int id, boolean acceptAlternate, byte[] hexid, int port, boolean reserveHost, int maxplayer, List<String> subnets, List<String> hosts)
	{
		writeByte(0x01);
		writeByte(id);
		writeByte(acceptAlternate ? 0x01 : 0x00);
		writeByte(reserveHost ? 0x01 : 0x00);
		writeShort(port);
		writeInt(maxplayer);
		writeInt(hexid.length);
		writeBytes(hexid);
		writeInt(subnets.size());
		for (int i = 0; i < subnets.size(); i++)
		{
			writeString(subnets.get(i));
			writeString(hosts.get(i));
		}
	}
}