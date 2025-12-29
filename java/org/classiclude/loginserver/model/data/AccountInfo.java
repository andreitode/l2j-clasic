package org.classiclude.loginserver.model.data;

import java.util.Objects;

/**
 * @author HorridoJoho
 */
public class AccountInfo
{
	private final String _login;
	private final String _passHash;
	private final int _accessLevel;
	private final int _lastServer;
	
	public AccountInfo(String login, String passHash, int accessLevel, int lastServer)
	{
		Objects.requireNonNull(login, "login");
		Objects.requireNonNull(passHash, "passHash");
		if (login.isEmpty())
		{
			throw new IllegalArgumentException("login");
		}
		if (passHash.isEmpty())
		{
			throw new IllegalArgumentException("passHash");
		}
		
		_login = login.toLowerCase();
		_passHash = passHash;
		_accessLevel = accessLevel;
		_lastServer = lastServer;
	}
	
	public boolean checkPassHash(String passHash)
	{
		return _passHash.equals(passHash);
	}
	
	public String getLogin()
	{
		return _login;
	}
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public int getLastServer()
	{
		return _lastServer;
	}
}
