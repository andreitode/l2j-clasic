package org.classiclude.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.gameserver.taskmanager.Task;
import org.classiclude.gameserver.taskmanager.TaskManager;
import org.classiclude.gameserver.taskmanager.TaskManager.ExecutedTask;
import org.classiclude.gameserver.taskmanager.TaskTypes;

/**
 * @author Layane
 */
public class TaskRecom extends Task
{
	private static final String NAME = "recommendations";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, time_left=?, rec_have=0 WHERE rec_have <=  20"))
			{
				ps.setInt(1, 20); // Rec left = 20
				ps.setInt(2, 3600000); // Timer = 1 hour
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, time_left=?, rec_have=GREATEST(rec_have-20,0) WHERE rec_have > 20"))
			{
				ps.setInt(1, 20); // Rec left = 20
				ps.setInt(2, 3600000); // Timer = 1 hour
				ps.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.severe(getClass().getSimpleName() + ": Could not reset Recommendations System: " + e);
		}
		
		LOGGER.info("Recommendations System reseted.");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}