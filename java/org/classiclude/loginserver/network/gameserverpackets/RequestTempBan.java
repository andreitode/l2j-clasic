package org.classiclude.loginserver.network.gameserverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.classiclude.commons.database.DatabaseFactory;
import org.classiclude.commons.network.base.BaseReadablePacket;
import org.classiclude.loginserver.LoginController;

/**
 * @author mrTJO
 */
public class RequestTempBan extends BaseReadablePacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestTempBan.class.getName());
	
	private final String _accountName;
	private final String _ip;
	long _banTime;
	
	public RequestTempBan(byte[] decrypt)
	{
		super(decrypt);
		readByte(); // Packet id, it is already processed.
		
		_accountName = readString();
		_ip = readString();
		_banTime = readLong();
		final boolean haveReason = readByte() != 0;
		if (haveReason)
		{
			readString(); // _banReason
		}
		banUser();
	}
	
	private void banUser()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO account_data VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?"))
		{
			ps.setString(1, _accountName);
			ps.setString(2, "ban_temp");
			ps.setString(3, Long.toString(_banTime));
			ps.setString(4, Long.toString(_banTime));
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		LoginController.getInstance().addBanForAddress(_ip, _banTime);
	}
}
