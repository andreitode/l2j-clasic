package org.classiclude.commons.network.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a pool of ByteBuffer objects for reuse, to avoid frequent allocation and deallocation.<br>
 * Buffers are managed in a concurrent queue with a maximum size limit.
 * @author JoeAlisson, Mobius
 */
public class BufferPool
{
	private final Queue<ByteBuffer> _buffers = new ConcurrentLinkedQueue<>();
	private final AtomicInteger _maxSize = new AtomicInteger();
	private final AtomicInteger _estimateSize = new AtomicInteger();
	private final int _bufferSize;
	
	/**
	 * Create a Buffer Pool
	 * @param maxSize the pool max size
	 * @param bufferSize the size of the buffers kept in Buffer Pool
	 */
	public BufferPool(int maxSize, int bufferSize)
	{
		_maxSize.set(maxSize);
		_bufferSize = bufferSize;
	}
	
	/**
	 * Initializes the buffer pool by pre-allocating a certain number of ByteBuffers.<br>
	 * The number of buffers allocated is determined by the specified factor and the maximum size of the pool.
	 * @param factor The factor used to determine the initial number of ByteBuffers to allocate.
	 */
	public void initialize(float factor)
	{
		final int maxSize = _maxSize.get();
		final int amount = (int) Math.min(maxSize, maxSize * factor);
		for (int i = 0; i < amount; i++)
		{
			_buffers.offer(ByteBuffer.allocateDirect(_bufferSize).order(ByteOrder.LITTLE_ENDIAN));
		}
		_estimateSize.set(amount);
	}
	
	/**
	 * Retrieves a ByteBuffer from the pool.<br>
	 * Returns a ByteBuffer if available, or null if the pool is empty.
	 * @return A ByteBuffer from the pool, or null if none are available.
	 */
	public ByteBuffer get()
	{
		final ByteBuffer buffer = _buffers.poll();
		if (buffer != null)
		{
			_estimateSize.decrementAndGet();
		}
		return buffer;
	}
	
	/**
	 * Attempts to recycle a ByteBuffer back into the pool.<br>
	 * If the pool size has not reached its maximum, the buffer is added back to the pool; otherwise, it is discarded.
	 * @param buffer The ByteBuffer to be recycled.
	 * @return true if the buffer was successfully recycled, false otherwise.
	 */
	public boolean recycle(ByteBuffer buffer)
	{
		final boolean recycle = _estimateSize.get() < _maxSize.get();
		if (recycle)
		{
			_buffers.offer(buffer.clear());
			_estimateSize.incrementAndGet();
		}
		return recycle;
	}
	
	/**
	 * Expands the buffer pool's capacity and allocates additional {@link ByteBuffer} instances.
	 * @param factor The factor used to determine the number of ByteBuffers to allocate.
	 * @param limit The size limit that, if not exceed, triggers the pool expansion.
	 */
	public synchronized void expandCapacity(float factor, int limit)
	{
		final int maxSize = _maxSize.get();
		if (maxSize > limit)
		{
			return;
		}
		
		if (factor > 0)
		{
			final int amount = (int) (maxSize * factor);
			for (int i = 0; i < amount; i++)
			{
				_buffers.offer(ByteBuffer.allocateDirect(_bufferSize).order(ByteOrder.LITTLE_ENDIAN));
			}
			_maxSize.set(maxSize + amount);
			_estimateSize.addAndGet(amount);
		}
		else
		{
			_maxSize.set(maxSize * 2);
		}
	}
	
	/**
	 * Returns the maximum size of the buffer pool.
	 * @return the maximum number of ByteBuffers that can be stored in the pool.
	 */
	public synchronized int getMaxSize()
	{
		return _maxSize.get();
	}
	
	/**
	 * @return {@code true} if this buffer pool size has reached its maximum capacity.
	 */
	public boolean isFull()
	{
		return _buffers.size() >= _maxSize.get();
	}
	
	/**
	 * @return {@code true} if this buffer pool is empty.
	 */
	public boolean isEmpty()
	{
		return _buffers.isEmpty();
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Pool {maxSize=");
		sb.append(_maxSize.get());
		sb.append(", bufferSize=");
		sb.append(_bufferSize);
		sb.append(", estimateUse=");
		sb.append(_estimateSize.get());
		sb.append('}');
		return sb.toString();
	}
}
