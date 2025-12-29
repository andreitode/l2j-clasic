package org.classiclude.gameserver.network.loginserverpackets.login;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;

public class ChangePasswordResponse extends BaseReadablePacket
{
	public ChangePasswordResponse(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		// boolean isSuccessful = readByte() > 0;
		final String character = readString();
		final String msgToSend = readString();
		final Player player = World.getInstance().getPlayer(character);
		if (player != null)
		{
			player.sendMessage(msgToSend);
		}
	}
}