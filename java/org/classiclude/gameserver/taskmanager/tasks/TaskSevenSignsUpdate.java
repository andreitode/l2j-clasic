package org.classiclude.gameserver.taskmanager.tasks;

import org.classiclude.gameserver.model.sevensigns.SevenSigns;
import org.classiclude.gameserver.model.sevensigns.SevenSignsFestival;
import org.classiclude.gameserver.taskmanager.Task;
import org.classiclude.gameserver.taskmanager.TaskManager;
import org.classiclude.gameserver.taskmanager.TaskManager.ExecutedTask;
import org.classiclude.gameserver.taskmanager.TaskTypes;

/**
 * Updates all data for the Seven Signs and Festival of Darkness engines, when time is elapsed.
 * @author Tempy
 */
public class TaskSevenSignsUpdate extends Task
{
	private static final String NAME = "seven_signs_update";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try
		{
			SevenSigns.getInstance().saveSevenSignsStatus();
			if (!SevenSigns.getInstance().isSealValidationPeriod())
			{
				SevenSignsFestival.getInstance().saveFestivalData(false);
			}
			LOGGER.info("SevenSigns: Data updated successfully.");
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": SevenSigns: Failed to save Seven Signs configuration: " + e.getMessage());
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "1800000", "");
	}
}
