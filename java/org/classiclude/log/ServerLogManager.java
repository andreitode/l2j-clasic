package org.classiclude.log;

import java.util.logging.LogManager;

/**
 * Specialized {@link LogManager} class.<br>
 * Prevents log devices to close before shutdown sequence so the shutdown sequence can make logging.
 */
public class ServerLogManager extends LogManager
{
	@Override
	public void reset()
	{
		// do nothing
	}
	
	public void doReset()
	{
		super.reset();
	}
}
