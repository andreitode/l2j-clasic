package org.classiclude.commons.network.internal;

import java.nio.ByteBuffer;

import org.classiclude.commons.network.ResourcePool;
import org.classiclude.commons.network.WritableBuffer;

/**
 * An abstract base class for internal writable buffers.<br>
 * This class defines the common interface for various implementations of writable buffers within the network package.
 * @author JoeAlisson
 */
public abstract class InternalWritableBuffer extends WritableBuffer
{
	/**
	 * Gets the current buffer position.
	 * @return The current position within the buffer.
	 */
	public abstract int position();
	
	/**
	 * Sets the buffer position to a new value.
	 * @param pos The new position within the buffer.
	 */
	public abstract void position(int pos);
	
	/**
	 * Marks the end of the buffer's content.
	 */
	public abstract void mark();
	
	/**
	 * Converts the writable buffer into an array of ByteBuffers.
	 * @return An array of ByteBuffers containing the contents of the writable buffer.
	 */
	public abstract ByteBuffer[] toByteBuffers();
	
	/**
	 * Releases the resources used by the buffer.
	 */
	public abstract void releaseResources();
	
	/**
	 * Create a new Dynamic Buffer that increases as needed
	 * @param buffer the initial under layer buffer
	 * @param resourcePool the resource pool used to get new buffers when needed
	 * @return a new Dynamic Buffer
	 */
	public static InternalWritableBuffer dynamicOf(ByteBuffer buffer, ResourcePool resourcePool)
	{
		return new DynamicPacketBuffer(buffer, resourcePool);
	}
	
	/**
	 * Create a new Dynamic Buffer that increases as needed based on ArrayPacketBuffer
	 * @param buffer the base buffer
	 * @param resourcePool the resource pool used to get new buffers when needed
	 * @return a new Dynamic buffer
	 */
	public static InternalWritableBuffer dynamicOf(ArrayPacketBuffer buffer, ResourcePool resourcePool)
	{
		final DynamicPacketBuffer copy = new DynamicPacketBuffer(buffer.toByteBuffer(), resourcePool);
		copy.limit(buffer.limit());
		return copy;
	}
	
	/**
	 * Create a new buffer backed by array
	 * @param resourcePool the resource pool used to get new buffers
	 * @return a Buffer backed by array
	 */
	public static InternalWritableBuffer arrayBacked(ResourcePool resourcePool)
	{
		return new ArrayPacketBuffer(resourcePool.getSegmentSize(), resourcePool);
	}
}
