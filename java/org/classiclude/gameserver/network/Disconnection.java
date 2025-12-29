package org.classiclude.gameserver.network;

import java.util.logging.Logger;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.instancemanager.AntiFeedManager;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.serverpackets.ServerPacket;
import org.classiclude.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * @author NB4L1
 */
public class Disconnection
{
	private static final Logger LOGGER = Logger.getLogger(Disconnection.class.getName());
	
	public static GameClient getClient(GameClient client, Player player)
	{
		if (client != null)
		{
			return client;
		}
		
		if (player != null)
		{
			return player.getClient();
		}
		
		return null;
	}
	
	public static Player getPlayer(GameClient client, Player player)
	{
		if (player != null)
		{
			return player;
		}
		
		if (client != null)
		{
			return client.getPlayer();
		}
		
		return null;
	}
	
	private final GameClient _client;
	private final Player _player;
	
	private Disconnection(GameClient client)
	{
		this(client, null);
	}
	
	public static Disconnection of(GameClient client)
	{
		return new Disconnection(client);
	}
	
	private Disconnection(Player player)
	{
		this(null, player);
	}
	
	public static Disconnection of(Player player)
	{
		return new Disconnection(player);
	}
	
	private Disconnection(GameClient client, Player player)
	{
		_client = getClient(client, player);
		_player = getPlayer(client, player);
		
		// Stop player tasks.
		if (_player != null)
		{
			_player.stopAllTasks();
		}
		
		// Anti Feed
		AntiFeedManager.getInstance().onDisconnect(_client);
		
		if (_client != null)
		{
			_client.setPlayer(null);
		}
		
		if (_player != null)
		{
			_player.setClient(null);
		}
	}
	
	public static Disconnection of(GameClient client, Player player)
	{
		return new Disconnection(client, player);
	}
	
	public Disconnection storeMe()
	{
		try
		{
			if (_player != null)
			{
				_player.storeMe();
			}
		}
		catch (RuntimeException e)
		{
			LOGGER.warning(e.getMessage());
		}
		return this;
	}
	
	public Disconnection deleteMe()
	{
		try
		{
			if ((_player != null) && _player.isOnline())
			{
				_player.deleteMe();
			}
		}
		catch (RuntimeException e)
		{
			LOGGER.warning(e.getMessage());
		}
		return this;
	}
	
	public Disconnection close(ServerPacket packet)
	{
		if (_client != null)
		{
			_client.close(packet);
		}
		return this;
	}
	
	public void defaultSequence(ServerPacket packet)
	{
		defaultSequence();
		close(packet);
	}
	
	private void defaultSequence()
	{
		storeMe();
		deleteMe();
	}
	
	public void onDisconnection()
	{
		if (_player != null)
		{
			ThreadPool.schedule(this::defaultSequence, _player.canLogout() ? 0 : AttackStanceTaskManager.COMBAT_TIME);
		}
	}
}