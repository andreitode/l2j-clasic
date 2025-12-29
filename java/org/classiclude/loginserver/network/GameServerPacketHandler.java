package org.classiclude.loginserver.network;

import java.util.logging.Logger;

import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.GameServerThread;
import org.classiclude.loginserver.network.gameserverpackets.BlowFishKey;
import org.classiclude.loginserver.network.gameserverpackets.ChangeAccessLevel;
import org.classiclude.loginserver.network.gameserverpackets.ChangePassword;
import org.classiclude.loginserver.network.gameserverpackets.GameServerAuth;
import org.classiclude.loginserver.network.gameserverpackets.PlayerAuthRequest;
import org.classiclude.loginserver.network.gameserverpackets.PlayerInGame;
import org.classiclude.loginserver.network.gameserverpackets.PlayerLogout;
import org.classiclude.loginserver.network.gameserverpackets.PlayerTracert;
import org.classiclude.loginserver.network.gameserverpackets.ReplyCharacters;
import org.classiclude.loginserver.network.gameserverpackets.RequestTempBan;
import org.classiclude.loginserver.network.gameserverpackets.ServerStatus;
import org.classiclude.loginserver.network.loginserverpackets.LoginServerFail;

/**
 * @author mrTJO
 */
public class GameServerPacketHandler
{
	protected static final Logger LOGGER = Logger.getLogger(GameServerPacketHandler.class.getName());
	
	public enum GameServerState
	{
		CONNECTED,
		BF_CONNECTED,
		AUTHED
	}
	
	public static BaseReadablePacket handlePacket(byte[] data, GameServerThread server)
	{
		BaseReadablePacket msg = null;
		final int opcode = data[0] & 0xff;
		final GameServerState state = server.getLoginConnectionState();
		switch (state)
		{
			case CONNECTED:
			{
				switch (opcode)
				{
					case 0x00:
					{
						msg = new BlowFishKey(data, server);
						break;
					}
					default:
					{
						LOGGER.warning("Unknown Opcode (" + Integer.toHexString(opcode).toUpperCase() + ") in state " + state.name() + " from GameServer, closing connection.");
						server.forceClose(LoginServerFail.NOT_AUTHED);
						break;
					}
				}
				break;
			}
			case BF_CONNECTED:
			{
				switch (opcode)
				{
					case 0x01:
					{
						msg = new GameServerAuth(data, server);
						break;
					}
					default:
					{
						LOGGER.warning("Unknown Opcode (" + Integer.toHexString(opcode).toUpperCase() + ") in state " + state.name() + " from GameServer, closing connection.");
						server.forceClose(LoginServerFail.NOT_AUTHED);
						break;
					}
				}
				break;
			}
			case AUTHED:
			{
				switch (opcode)
				{
					case 0x02:
					{
						msg = new PlayerInGame(data, server);
						break;
					}
					case 0x03:
					{
						msg = new PlayerLogout(data, server);
						break;
					}
					case 0x04:
					{
						msg = new ChangeAccessLevel(data, server);
						break;
					}
					case 0x05:
					{
						msg = new PlayerAuthRequest(data, server);
						break;
					}
					case 0x06:
					{
						msg = new ServerStatus(data, server);
						break;
					}
					case 0x07:
					{
						msg = new PlayerTracert(data);
						break;
					}
					case 0x08:
					{
						msg = new ReplyCharacters(data, server);
						break;
					}
					case 0x09:
					{
						// msg = new RequestSendMail(data);
						break;
					}
					case 0x0A:
					{
						msg = new RequestTempBan(data);
						break;
					}
					case 0x0B:
					{
						new ChangePassword(data);
						break;
					}
					default:
					{
						LOGGER.warning("Unknown Opcode (" + Integer.toHexString(opcode).toUpperCase() + ") in state " + state.name() + " from GameServer, closing connection.");
						server.forceClose(LoginServerFail.NOT_AUTHED);
						break;
					}
				}
				break;
			}
		}
		return msg;
	}
}
