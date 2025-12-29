package org.classiclude.commons.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.classiclude.Config;
import org.classiclude.commons.enums.ServerMode;

/**
 * @author Mobius
 */
public class DatabaseBackup
{
	public static void performBackup()
	{
		// Delete old files.
		if (Config.BACKUP_DAYS > 0)
		{
			final long cut = LocalDateTime.now().minusDays(Config.BACKUP_DAYS).toEpochSecond(ZoneOffset.UTC);
			final Path path = Paths.get(Config.BACKUP_PATH);
			try
			{
				Files.list(path).filter(n ->
				{
					try
					{
						return Files.getLastModifiedTime(n).to(TimeUnit.SECONDS) < cut;
					}
					catch (Exception ex)
					{
						return false;
					}
				}).forEach(n ->
				{
					try
					{
						Files.delete(n);
					}
					catch (Exception ex)
					{
						// Ignore.
					}
				});
			}
			catch (Exception e)
			{
				// Ignore.
			}
		}
		
		// Dump to file.
		final String mysqldumpPath = System.getProperty("os.name").toLowerCase().contains("win") ? Config.MYSQL_BIN_PATH : "";
		try
		{
			// Java 17
			// final Process process = Runtime.getRuntime().exec(mysqldumpPath + "mysqldump -u " + Config.DATABASE_LOGIN + (Config.DATABASE_PASSWORD.trim().isEmpty() ? "" : " -p" + Config.DATABASE_PASSWORD) + " "
			// + Config.DATABASE_URL.replace("jdbc:mysql://", "").replaceAll(".*\\/|\\?.*", "") + " -r " + Config.BACKUP_PATH + (Config.SERVER_MODE == ServerMode.GAME ? "game" : "login") + new SimpleDateFormat("_yyyy_MM_dd_HH_mm'.sql'").format(new Date()));
			// Java 18
			final String backupFileName = Config.BACKUP_PATH + (Config.SERVER_MODE == ServerMode.GAME ? "game" : "login") + new SimpleDateFormat("_yyyy_MM_dd_HH_mm'.sql'").format(new Date());
			final String databaseName = Config.DATABASE_URL.replace("jdbc:mysql://", "").replaceAll(".*\\/|\\?.*", "");
			final String[] command =
			{
				mysqldumpPath + "mysqldump",
				"-u",
				Config.DATABASE_LOGIN,
				Config.DATABASE_PASSWORD.trim().isEmpty() ? "" : "-p" + Config.DATABASE_PASSWORD,
				databaseName,
				"-r",
				backupFileName
			};
			
			final Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
		}
		catch (Exception e)
		{
			// Ignore.
		}
	}
}
