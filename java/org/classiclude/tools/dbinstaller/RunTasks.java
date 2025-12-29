package org.classiclude.tools.dbinstaller;

import java.io.File;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.classiclude.tools.dbinstaller.util.mysql.DBDumper;
import org.classiclude.tools.dbinstaller.util.mysql.ScriptExecutor;

/**
 * @author mrTJO
 */
public class RunTasks extends Thread
{
	DBOutputInterface _frame;
	String _db;
	String _sqlDir;
	
	public RunTasks(DBOutputInterface frame, String db, String sqlDir)
	{
		_frame = frame;
		_db = db;
		_sqlDir = sqlDir;
	}
	
	@Override
	public void run()
	{
		new DBDumper(_frame, _db, _sqlDir);
		final ScriptExecutor exec = new ScriptExecutor(_frame);
		_frame.appendToProgressArea("Installing Database Content...");
		exec.execSqlBatch(new File(_sqlDir));
		_frame.appendToProgressArea("Database Installation Complete!");
		
		try
		{
			_frame.getConnection().close();
		}
		catch (SQLException e)
		{
			JOptionPane.showMessageDialog(null, "Cannot close MySQL Connection: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
		}
		
		_frame.showMessage("Done!", "Database Installation Complete!", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
}
