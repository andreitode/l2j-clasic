package org.classiclude.gameserver.network.loginserverpackets.game;

import java.util.List;

import org.classiclude.commons.network.base.BaseWritablePacket;

/**
 * @author -Wooden-
 */
public class PlayerInGame extends BaseWritablePacket
{
	public PlayerInGame(String player)
	{
		writeByte(0x02);
		writeShort(1);
		writeString(player);
	}
	
	public PlayerInGame(List<String> players)
	{
		writeByte(0x02);
		writeShort(players.size());
		for (String player : players)
		{
			writeString(player);
		}
	}
}