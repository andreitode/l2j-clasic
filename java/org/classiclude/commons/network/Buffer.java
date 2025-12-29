package org.classiclude.commons.network;

/**
 * Represents a buffer for reading and writing different data types.<br>
 * This interface provides methods to read and write primitive data types<br>
 * like byte, short, and int at specific indices within the buffer.<br>
 * Additionally, methods to get and set the buffer's limit are provided.<br>
 * @author JoeAlisson
 */
public interface Buffer
{
	/**
	 * Reads a byte value from the buffer at the specified index.
	 * @param index The index from where the byte should be read.
	 * @return The byte value at the specified index.
	 */
	byte readByte(int index);
	
	/**
	 * Writes a byte value to the buffer at the specified index.
	 * @param index The index at which the byte value should be written.
	 * @param value The byte value to be written.
	 */
	void writeByte(int index, byte value);
	
	/**
	 * Reads a short value (16-bit integer) from the buffer at the specified index.
	 * @param index The index from where the short should be read.
	 * @return The short value at the specified index.
	 */
	short readShort(int index);
	
	/**
	 * Writes a short value (16-bit integer) to the buffer at the specified index.
	 * @param index The index at which the short value should be written.
	 * @param value The short value to be written.
	 */
	void writeShort(int index, short value);
	
	/**
	 * Reads an int value (32-bit integer) from the buffer at the specified index.
	 * @param index The index from where the int should be read.
	 * @return The int value at the specified index.
	 */
	int readInt(int index);
	
	/**
	 * Writes an int value (32-bit integer) to the buffer at the specified index.
	 * @param index The index at which the int value should be written.
	 * @param value The int value to be written.
	 */
	void writeInt(int index, int value);
	
	/**
	 * Retrieves the current limit of the buffer.
	 * @return The buffer's current limit.
	 */
	int limit();
	
	/**
	 * Sets a new limit for the buffer.
	 * @param newLimit The new limit to be set for the buffer.
	 */
	void limit(int newLimit);
}
