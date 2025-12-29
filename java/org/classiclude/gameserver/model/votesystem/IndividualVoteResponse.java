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
public class IndividualVoteResponse
{
	private boolean _isVoted;
	private long _diffTime;
	private long _voteSiteTime;
	
	/*
	 * public IndividualVoteResponse() { }
	 */
	
	/**
	 * @param isVoted
	 * @param diffTime
	 * @param voteSiteTime
	 */
	public IndividualVoteResponse(boolean isVoted, long diffTime, long voteSiteTime)
	{
		_isVoted = isVoted;
		_diffTime = diffTime;
		_voteSiteTime = voteSiteTime;
	}
	
	public void setIsVoted(boolean isVoted)
	{
		_isVoted = isVoted;
	}
	
	public void setDiffTime(long diffTime)
	{
		_diffTime = diffTime;
	}
	
	public void setVoteSiteTime(long voteSiteTime)
	{
		_voteSiteTime = voteSiteTime;
	}
	
	public boolean getIsVoted()
	{
		return _isVoted;
	}
	
	public long getDiffTime()
	{
		return _diffTime;
	}
	
	public long getVoteSiteTime()
	{
		return _voteSiteTime;
	}
}