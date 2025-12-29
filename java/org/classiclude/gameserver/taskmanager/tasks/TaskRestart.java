package org.classiclude.gameserver.taskmanager.tasks;

import org.classiclude.gameserver.Shutdown;
import org.classiclude.gameserver.taskmanager.Task;
import org.classiclude.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public class TaskRestart extends Task
{
	private static final String NAME = "restart";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		final Shutdown handler = new Shutdown(Integer.parseInt(task.getParams()[2]), true);
		handler.start();
	}
}
