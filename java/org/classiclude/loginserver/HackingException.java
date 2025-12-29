package org.classiclude.loginserver;

/**
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:30:09 $
 */
public class HackingException extends Exception
{
	private final String _ip;
	private final int _connects;
	
	public HackingException(String ip, int connects)
	{
		_ip = ip;
		_connects = connects;
	}
	
	public String getIP()
	{
		return _ip;
	}
	
	public int getConnects()
	{
		return _connects;
	}
}
