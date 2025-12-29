package org.classiclude.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.classiclude.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public abstract class Task
{
	protected final Logger LOGGER = Logger.getLogger(getClass().getName());
	
	public void initializate()
	{
	}
	
	public ScheduledFuture<?> launchSpecial(ExecutedTask instance)
	{
		return null;
	}
	
	public abstract String getName();
	
	public abstract void onTimeElapsed(ExecutedTask task);
	
	public void onDestroy()
	{
	}
}
