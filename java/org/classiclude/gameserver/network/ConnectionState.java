package org.classiclude.gameserver.network;

/**
 * @author KenM
 */
public enum ConnectionState
{
	CONNECTED,
	DISCONNECTED,
	CLOSING,
	AUTHENTICATED,
	ENTERING,
	IN_GAME;
}
