/*
 * Copyright (c) 2013 classiclude
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.classiclude.gameserver.model;

import java.util.Objects;

import org.classiclude.commons.util.Point2D;
import org.classiclude.gameserver.model.interfaces.ILocational;
import org.classiclude.gameserver.model.interfaces.IPositionable;

/**
 * A datatype used to retain a 3D (x/y/z/heading) point. It got the capability to be set and cleaned.
 */
public class Location extends Point2D implements IPositionable
{
	protected volatile int _z;
	protected volatile int _heading;
	
	public Location(int x, int y, int z)
	{
		super(x, y);
		_z = z;
		_heading = 0;
	}
	
	public Location(int x, int y, int z, int heading)
	{
		super(x, y);
		_z = z;
		_heading = heading;
	}
	
	public Location(WorldObject obj)
	{
		this(obj.getX(), obj.getY(), obj.getZ(), obj.getHeading());
	}
	
	public Location(StatSet set)
	{
		super(set.getInt("x", 0), set.getInt("y", 0));
		_z = set.getInt("z", 0);
		_heading = set.getInt("heading", 0);
	}
	
	/**
	 * Get the x coordinate.
	 * @return the x coordinate
	 */
	@Override
	public int getX()
	{
		return _x;
	}
	
	/**
	 * Get the y coordinate.
	 * @return the y coordinate
	 */
	@Override
	public int getY()
	{
		return _y;
	}
	
	/**
	 * Get the z coordinate.
	 * @return the z coordinate
	 */
	@Override
	public int getZ()
	{
		return _z;
	}
	
	/**
	 * Set the x, y, z coordinates.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	@Override
	public void setXYZ(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	/**
	 * Set the x, y, z coordinates.
	 * @param loc The location.
	 */
	@Override
	public void setXYZ(ILocational loc)
	{
		setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Get the heading.
	 * @return the heading
	 */
	@Override
	public int getHeading()
	{
		return _heading;
	}
	
	/**
	 * Set the heading.
	 * @param heading the heading
	 */
	@Override
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	@Override
	public IPositionable getLocation()
	{
		return this;
	}
	
	@Override
	public void setLocation(Location loc)
	{
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
		_heading = loc.getHeading();
	}
	
	@Override
	public void clean()
	{
		super.clean();
		_z = 0;
	}
	
	@Override
	public Location clone()
	{
		return new Location(_x, _y, _z);
	}
	
	@Override
	public int hashCode()
	{
		return (31 * super.hashCode()) + Objects.hash(_z);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Location)
		{
			final Location loc = (Location) obj;
			return (getX() == loc.getX()) && (getY() == loc.getY()) && (getZ() == loc.getZ()) && (getHeading() == loc.getHeading());
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + "] X: " + _x + " Y: " + _y + " Z: " + _z + " Heading: " + _heading;
	}
}
