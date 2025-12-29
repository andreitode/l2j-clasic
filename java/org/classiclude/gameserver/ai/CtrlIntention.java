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
package org.classiclude.gameserver.ai;

/**
 * Enumeration of generic intentions of an NPC/PC, an intention may require several steps to be completed.
 */
public enum CtrlIntention
{
	/** Do nothing, disconnect AI of NPC if no players around */
	AI_INTENTION_IDLE,
	
	/** Alerted state without goal : scan attackable targets, random walk, etc */
	AI_INTENTION_ACTIVE,
	
	/** Rest (sit until attacked) */
	AI_INTENTION_REST,
	
	/** Attack target (cast combat magic, go to target, combat), may be ignored, if target is locked on another character or a peaceful zone and so on. */
	AI_INTENTION_ATTACK,
	
	/** Cast a spell, depending on the spell - may start or stop attacking */
	AI_INTENTION_CAST,
	
	/** Just move to another location */
	AI_INTENTION_MOVE_TO,
	
	/** Like move, but check target's movement and follow it */
	AI_INTENTION_FOLLOW,
	
	/** PickUp and item, (got to item, pickup it, become idle */
	AI_INTENTION_PICK_UP,
	
	/** Move to target, then interact */
	AI_INTENTION_INTERACT;
}
