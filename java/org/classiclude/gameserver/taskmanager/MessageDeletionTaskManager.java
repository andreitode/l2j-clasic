package org.classiclude.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.classiclude.commons.threads.ThreadPool;
import org.classiclude.gameserver.instancemanager.MailManager;
import org.classiclude.gameserver.model.Message;
import org.classiclude.gameserver.model.World;
import org.classiclude.gameserver.model.actor.Player;
import org.classiclude.gameserver.network.SystemMessageId;
import org.classiclude.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Mobius
 */
public class MessageDeletionTaskManager implements Runnable
{
	private static final Map<Integer, Long> PENDING_MESSAGES = new ConcurrentHashMap<>();
	private static boolean _working = false;
	
	protected MessageDeletionTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 10000, 10000);
	}
	
	@Override
	public void run()
	{
		if (_working)
		{
			return;
		}
		_working = true;
		
		if (!PENDING_MESSAGES.isEmpty())
		{
			final long currentTime = System.currentTimeMillis();
			final Iterator<Entry<Integer, Long>> iterator = PENDING_MESSAGES.entrySet().iterator();
			Entry<Integer, Long> entry;
			Integer messageId;
			Message message;
			
			while (iterator.hasNext())
			{
				entry = iterator.next();
				if (currentTime > entry.getValue())
				{
					messageId = entry.getKey();
					message = MailManager.getInstance().getMessage(messageId);
					if (message == null)
					{
						iterator.remove();
						continue;
					}
					
					if (message.hasAttachments())
					{
						final Player sender = World.getInstance().getPlayer(message.getSenderId());
						final Player receiver = World.getInstance().getPlayer(message.getReceiverId());
						if (sender != null)
						{
							message.getAttachments().returnToWh(sender.getWarehouse());
							sender.sendPacket(SystemMessageId.THE_MAIL_WAS_RETURNED_DUE_TO_THE_EXCEEDED_WAITING_TIME);
						}
						else if (message.getSenderId() == -1) // Action House / Custom Mail
						{
							message.getAttachments().returnToWh(receiver.getWarehouse());
						}
						else
						{
							message.getAttachments().returnToWh(null);
						}
						message.getAttachments().deleteMe();
						message.removeAttachments();
						
						if (receiver != null)
						{
							receiver.sendPacket(new SystemMessage(SystemMessageId.THE_MAIL_WAS_RETURNED_DUE_TO_THE_EXCEEDED_WAITING_TIME));
						}
					}
					
					MailManager.getInstance().deleteMessageInDb(messageId);
					iterator.remove();
				}
			}
		}
		
		_working = false;
	}
	
	public void add(int msgId, long deletionTime)
	{
		PENDING_MESSAGES.put(msgId, deletionTime);
	}
	
	public static MessageDeletionTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MessageDeletionTaskManager INSTANCE = new MessageDeletionTaskManager();
	}
}
