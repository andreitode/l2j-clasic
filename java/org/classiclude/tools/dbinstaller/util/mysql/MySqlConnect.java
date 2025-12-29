package org.classiclude.tools.dbinstaller.util.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Formatter;

import javax.swing.JOptionPane;

/**
 * @author mrTJO
 */
public class MySqlConnect
{
	Connection con = null;
	
	public MySqlConnect(String host, String port, String user, String password, String db, boolean console) throws Exception
	{
		try (Formatter form = new Formatter())
		{
			Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
			final String formattedText = form.format("jdbc:mysql://%1$s:%2$s", host, port).toString();
			con = DriverManager.getConnection(formattedText, user, password);
			
			try (Statement s = con.createStatement())
			{
				s.execute("CREATE DATABASE IF NOT EXISTS `" + db + "`");
				s.execute("USE `" + db + "`");
			}
		}
		catch (SQLException e)
		{
			if (console)
			{
				e.printStackTrace();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "MySQL Error: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (InstantiationException e)
		{
			if (console)
			{
				e.printStackTrace();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Instantiation Exception: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (IllegalAccessException e)
		{
			if (console)
			{
				e.printStackTrace();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Illegal Access: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (ClassNotFoundException e)
		{
			if (console)
			{
				e.printStackTrace();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Cannot find MySQL Connector: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public Connection getConnection()
	{
		return con;
	}
	
	public Statement getStatement()
	{
		try
		{
			return con.createStatement();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println("Statement Null");
			return null;
		}
	}
}
