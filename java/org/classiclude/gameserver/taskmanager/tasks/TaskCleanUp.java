package org.classiclude.gameserver.taskmanager.tasks;

import org.classiclude.gameserver.taskmanager.Task;
import org.classiclude.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Tempy
 */
public class TaskCleanUp extends Task
{
	private static final String NAME = "clean_up";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		// Removed with Java 18.
		// System.runFinalization();
		
		System.gc();
	}
}
