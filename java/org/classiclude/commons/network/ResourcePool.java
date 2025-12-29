package org.classiclude.commons.network;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.classiclude.commons.network.internal.BufferPool;

/**
 * Manages pools of ByteBuffers for efficient resource allocation and reuse.<br>
 * This class maintains a collection of buffer pools, each optimized for different buffer sizes, to reduce the overhead of buffer allocation.
 * @author JoeAlisson, Mobius
 */
public class ResourcePool
{
	// private static final Logger LOGGER = Logger.getLogger(ResourcePool.class.getName());
	
	private final NavigableMap<Integer, BufferPool> _bufferPools = new TreeMap<>();
	private boolean _autoExpandCapacity = true;
	private boolean _initBufferPools = false;
	private float _initBufferPoolFactor = 0;
	private int _bufferSegmentSize = 64;
	
	public ResourcePool()
	{
	}
	
	public ByteBuffer getHeaderBuffer()
	{
		return getSizedBuffer(ConnectionConfig.HEADER_SIZE);
	}
	
	public ByteBuffer getSegmentBuffer()
	{
		return getSizedBuffer(_bufferSegmentSize);
	}
	
	public ByteBuffer getBuffer(int size)
	{
		return getSizedBuffer(determineBufferSize(size));
	}
	
	public ByteBuffer recycleAndGetNew(ByteBuffer buffer, int newSize)
	{
		final int bufferSize = determineBufferSize(newSize);
		if (buffer != null)
		{
			if (buffer.clear().limit() == bufferSize)
			{
				return buffer.limit(newSize);
			}
			
			recycleBuffer(buffer);
		}
		
		return getSizedBuffer(bufferSize).limit(newSize);
	}
	
	private ByteBuffer getSizedBuffer(int size)
	{
		ByteBuffer buffer = null;
		BufferPool pool = null;
		
		final Entry<Integer, BufferPool> entry = _bufferPools.ceilingEntry(size);
		if (entry != null)
		{
			pool = entry.getValue();
			if (pool != null)
			{
				if (_autoExpandCapacity)
				{
					if (_initBufferPools)
					{
						if (pool.isEmpty())
						{
							// LOGGER.info("ResourcePool: Buffer pool for size " + size + " is empty. Expanding capacity by a factor of " + _initBufferPoolFactor + ".");
							pool.expandCapacity(_initBufferPoolFactor, pool.getMaxSize());
						}
					}
					else if (pool.isFull())
					{
						// LOGGER.info("ResourcePool: Buffer pool for size " + entry.getKey() + " is full. Doubling the capacity from " + pool.getMaxSize() + " to " + (pool.getMaxSize() * 2) + ".");
						pool.expandCapacity(_initBufferPoolFactor, pool.getMaxSize());
					}
				}
				
				buffer = pool.get();
			}
		}
		
		if (buffer == null)
		{
			if (pool == null)
			{
				// LOGGER.warning("ResourcePool: There is no buffer pool handling buffer size " + size + ". Creating a new pool.");
				pool = new BufferPool(10, size);
				if (_initBufferPools)
				{
					pool.initialize(_initBufferPoolFactor);
				}
				_bufferPools.put(size, pool);
			}
			
			buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
		}
		
		return buffer;
	}
	
	private int determineBufferSize(int size)
	{
		final Entry<Integer, BufferPool> entry = _bufferPools.ceilingEntry(size);
		if (entry != null)
		{
			return entry.getKey();
		}
		
		// LOGGER.warning("ResourcePool: There is no buffer pool handling buffer size " + size + ". Creating a new pool.");
		final BufferPool pool = new BufferPool(10, size);
		if (_initBufferPools)
		{
			pool.initialize(_initBufferPoolFactor);
		}
		_bufferPools.put(size, pool);
		
		return size;
	}
	
	public void recycleBuffer(ByteBuffer buffer)
	{
		if (buffer != null)
		{
			final BufferPool pool = _bufferPools.get(buffer.capacity());
			if ((pool == null) || !pool.recycle(buffer))
			{
				// LOGGER.warning("ResourcePool: Buffer was not recycled " + buffer + " in pool " + pool);
			}
		}
	}
	
	public int getSegmentSize()
	{
		return _bufferSegmentSize;
	}
	
	public void addBufferPool(int bufferSize, BufferPool bufferPool)
	{
		_bufferPools.putIfAbsent(bufferSize, bufferPool);
	}
	
	public int bufferPoolSize()
	{
		return _bufferPools.size();
	}
	
	public void initializeBuffers(boolean autoExpandCapacity, float initBufferPoolFactor)
	{
		_autoExpandCapacity = autoExpandCapacity;
		_initBufferPoolFactor = initBufferPoolFactor;
		_initBufferPools = initBufferPoolFactor > 0;
		if (_initBufferPools)
		{
			_bufferPools.values().forEach(pool -> pool.initialize(initBufferPoolFactor));
		}
	}
	
	public void setBufferSegmentSize(int size)
	{
		_bufferSegmentSize = size;
	}
	
	public String stats()
	{
		final StringBuilder sb = new StringBuilder();
		for (BufferPool pool : _bufferPools.values())
		{
			sb.append(pool.toString());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
