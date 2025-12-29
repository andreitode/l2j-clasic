package org.classiclude.commons.network;

import java.nio.channels.CompletionHandler;

/**
 * Handles the completion of write operations for network clients.<br>
 * This class implements {@link CompletionHandler} to process the results of data writing to the client, ensuring proper handling of the write operation's conclusion.
 * @param <T> The type of Client associated with this write handler.
 * @author JoeAlisson
 */
class WriteHandler<T extends Client<Connection<T>>> implements CompletionHandler<Long, T>
{
	// private static final Logger LOGGER = Logger.getLogger(WriteHandler.class.getName());
	
	@Override
	public void completed(Long result, T client)
	{
		// Client probably disconnected.
		if (client == null)
		{
			return;
		}
		
		if (result < 0)
		{
			// LOGGER.warning("Couldn't send data to client " + client);
			if (client.isConnected())
			{
				client.disconnect();
			}
			return;
		}
		
		if ((result < client.getDataSentSize()) && (result > 0))
		{
			// LOGGER.info("Still " + result + " data to send. Trying to send");
			client.resumeSend(result);
		}
		else
		{
			client.finishWriting();
		}
	}
	
	@Override
	public void failed(Throwable e, T client)
	{
		// if (!(e instanceof IOException))
		// {
		// LOGGER.log(Level.WARNING, e.getMessage(), e);
		// }
		client.disconnect();
	}
}
