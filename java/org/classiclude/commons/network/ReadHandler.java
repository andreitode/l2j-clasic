package org.classiclude.commons.network;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

/**
 * Handles the completion of read operations for network clients.<br>
 * This class implements {@link CompletionHandler} to process the data read from the client, converting it into packets and executing them.
 * @param <T> The type of Client associated with this read handler.
 * @author JoeAlisson
 */
public class ReadHandler<T extends Client<Connection<T>>> implements CompletionHandler<Integer, T>
{
	// private static final Logger LOGGER = Logger.getLogger(ReadHandler.class.getName());
	
	private final PacketHandler<T> _packetHandler;
	private final PacketExecutor<T> _executor;
	
	public ReadHandler(PacketHandler<T> packetHandler, PacketExecutor<T> executor)
	{
		_packetHandler = packetHandler;
		_executor = executor;
	}
	
	@Override
	public void completed(Integer bytesRead, T client)
	{
		if (!client.isConnected())
		{
			return;
		}
		
		// LOGGER.info("Reading " + bytesRead + " from " + client);
		if (bytesRead < 0)
		{
			client.disconnect();
			return;
		}
		
		if (bytesRead < client.getExpectedReadSize())
		{
			client.resumeRead(bytesRead);
			return;
		}
		
		if (client.isReadingPayload())
		{
			handlePayload(client);
		}
		else
		{
			handleHeader(client);
		}
	}
	
	private void handleHeader(T client)
	{
		final ByteBuffer buffer = client.getConnection().getReadingBuffer();
		if (buffer == null)
		{
			client.disconnect();
			return;
		}
		buffer.flip();
		final int dataSize = Short.toUnsignedInt(buffer.getShort()) - ConnectionConfig.HEADER_SIZE;
		if (dataSize > 0)
		{
			client.readPayload(dataSize);
		}
		else
		{
			client.read();
		}
	}
	
	private void handlePayload(T client)
	{
		final ByteBuffer buffer = client.getConnection().getReadingBuffer();
		if (buffer == null)
		{
			client.disconnect();
			return;
		}
		buffer.flip();
		parseAndExecutePacket(client, buffer);
		client.isReading.set(false);
		if (client.canReadNextPacket())
		{
			client.read();
		}
	}
	
	private void parseAndExecutePacket(T client, ByteBuffer incomingBuffer)
	{
		// LOGGER.info("Trying to parse data");
		try
		{
			final ReadableBuffer buffer = ReadableBuffer.of(incomingBuffer);
			final boolean decrypted = client.decrypt(buffer, 0, buffer.remaining());
			if (decrypted)
			{
				final ReadablePacket<T> packet = _packetHandler.handlePacket(buffer, client);
				// LOGGER.info("Data parsed to packet " + packet);
				if (packet != null)
				{
					packet.init(client, buffer);
					execute(packet);
				}
			}
		}
		catch (Exception e)
		{
			failed(e, client);
		}
	}
	
	private void execute(ReadablePacket<T> packet)
	{
		if (packet.read())
		{
			// LOGGER.info("packet " + packet + " was read from client " + packet.client);
			_executor.execute(packet);
		}
	}
	
	@Override
	public void failed(Throwable e, T client)
	{
		// LOGGER.log(Level.INFO, "Failed to read from client", e);
		client.disconnect();
	}
}
