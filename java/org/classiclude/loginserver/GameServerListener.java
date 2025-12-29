package org.classiclude.loginserver;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.Config;

/**
 * @author KenM
 */
public class GameServerListener extends FloodProtectedListener
{
	private static Collection<GameServerThread> _gameServers = ConcurrentHashMap.newKeySet();
	
	public GameServerListener() throws IOException
	{
		super(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
		setName(getClass().getSimpleName());
	}
	
	@Override
	public void addClient(Socket s)
	{
		_gameServers.add(new GameServerThread(s));
	}
	
	public void removeGameServer(GameServerThread gst)
	{
		_gameServers.remove(gst);
	}
}
