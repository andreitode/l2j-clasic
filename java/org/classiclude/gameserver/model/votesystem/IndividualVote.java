/*
 * This file is part of the ClassicLude project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.classiclude.gameserver.model.votesystem;

/**
 * @author Tuccy
 */
public class IndividualVote
{
	private String _voterIp;
	private long _diffTime;
	private long _votingTimeSite;
	private int _voteSite;
	private boolean _alreadyRewarded;
	
	public IndividualVote(String voterIp, long diffTime, long votingTimeSite, int voteSite, boolean alreadyRewarded)
	{
		_voterIp = voterIp;
		_diffTime = diffTime;
		_votingTimeSite = votingTimeSite;
		_voteSite = voteSite;
		_alreadyRewarded = alreadyRewarded;
	}
	
	public IndividualVote(String voterIp, long votingTimeSite, int voteSite, boolean alreadyRewarded)
	{
		_voterIp = voterIp;
		_votingTimeSite = votingTimeSite;
		_voteSite = voteSite;
		_alreadyRewarded = alreadyRewarded;
	}
	
	public void setVoterIp(String voterIp)
	{
		_voterIp = voterIp;
	}
	
	public void setDiffTime(long diffTime)
	{
		_diffTime = diffTime;
	}
	
	public void setVotingTimeSite(long votingTimeSite)
	{
		_votingTimeSite = votingTimeSite;
	}
	
	public void setVoteSite(int voteSite)
	{
		_voteSite = voteSite;
	}
	
	public void setAlreadyRewarded(boolean alreadyRewarded)
	{
		_alreadyRewarded = alreadyRewarded;
	}
	
	public String getVoterIp()
	{
		return _voterIp;
	}
	
	public long getDiffTime()
	{
		return _diffTime;
	}
	
	public long getVotingTimeSite()
	{
		return _votingTimeSite;
	}
	
	public int getVoteSite()
	{
		return _voteSite;
	}
	
	public boolean getAlreadyRewarded()
	{
		return _alreadyRewarded;
	}
	
}