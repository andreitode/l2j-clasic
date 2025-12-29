package org.classiclude.gameserver.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NpcEnchantSkillTeach
{
	private static final NpcEnchantSkillTeach INSTANCE = new NpcEnchantSkillTeach();
	private final Map<Integer, Set<Integer>> NpcEnchantSkillTeach;
	
	private NpcEnchantSkillTeach()
	{
		NpcEnchantSkillTeach = new HashMap<>();
		initializeMap();
	}
	
	public static NpcEnchantSkillTeach getInstance()
	{
		return INSTANCE;
	}
	
	private void initializeMap()
	{
		addNpcEnchantSkillTeach(30850, 88);
		addNpcEnchantSkillTeach(30850, 89);
		addNpcEnchantSkillTeach(30850, 90);
		addNpcEnchantSkillTeach(30850, 91);
		addNpcEnchantSkillTeach(30850, 93);
		addNpcEnchantSkillTeach(30850, 92);
		addNpcEnchantSkillTeach(30852, 99);
		addNpcEnchantSkillTeach(30852, 100);
		addNpcEnchantSkillTeach(30852, 101);
		addNpcEnchantSkillTeach(30852, 102);
		addNpcEnchantSkillTeach(30188, 94);
		addNpcEnchantSkillTeach(30188, 95);
		addNpcEnchantSkillTeach(30188, 96);
		addNpcEnchantSkillTeach(30188, 97);
		addNpcEnchantSkillTeach(30188, 98);
		addNpcEnchantSkillTeach(30680, 103);
		addNpcEnchantSkillTeach(30680, 104);
		addNpcEnchantSkillTeach(30680, 105);
		addNpcEnchantSkillTeach(30715, 94);
		addNpcEnchantSkillTeach(30715, 95);
		addNpcEnchantSkillTeach(30715, 96);
		addNpcEnchantSkillTeach(30715, 97);
		addNpcEnchantSkillTeach(30715, 98);
		addNpcEnchantSkillTeach(30718, 103);
		addNpcEnchantSkillTeach(30718, 104);
		addNpcEnchantSkillTeach(30718, 105);
		addNpcEnchantSkillTeach(30721, 110);
		addNpcEnchantSkillTeach(30721, 111);
		addNpcEnchantSkillTeach(30721, 112);
		addNpcEnchantSkillTeach(31580, 106);
		addNpcEnchantSkillTeach(31580, 107);
		addNpcEnchantSkillTeach(31580, 108);
		addNpcEnchantSkillTeach(31580, 109);
		addNpcEnchantSkillTeach(31581, 110);
		addNpcEnchantSkillTeach(31581, 111);
		addNpcEnchantSkillTeach(31581, 112);
		addNpcEnchantSkillTeach(31582, 106);
		addNpcEnchantSkillTeach(31582, 107);
		addNpcEnchantSkillTeach(31582, 108);
		addNpcEnchantSkillTeach(31582, 109);
		addNpcEnchantSkillTeach(30835, 110);
		addNpcEnchantSkillTeach(30835, 111);
		addNpcEnchantSkillTeach(30835, 112);
		addNpcEnchantSkillTeach(30691, 88);
		addNpcEnchantSkillTeach(30691, 89);
		addNpcEnchantSkillTeach(30691, 90);
		addNpcEnchantSkillTeach(30691, 91);
		addNpcEnchantSkillTeach(30691, 93);
		addNpcEnchantSkillTeach(30691, 92);
		addNpcEnchantSkillTeach(30692, 99);
		addNpcEnchantSkillTeach(30692, 100);
		addNpcEnchantSkillTeach(30692, 101);
		addNpcEnchantSkillTeach(30692, 102);
		addNpcEnchantSkillTeach(31327, 114);
		addNpcEnchantSkillTeach(31327, 113);
		addNpcEnchantSkillTeach(31337, 116);
		addNpcEnchantSkillTeach(31337, 115);
		addNpcEnchantSkillTeach(31316, 118);
		addNpcEnchantSkillTeach(31311, 117);
		addNpcEnchantSkillTeach(31312, 117);
		addNpcEnchantSkillTeach(31313, 117);
		addNpcEnchantSkillTeach(31315, 117);
		
	}
	
	private void addNpcEnchantSkillTeach(int npcId, int classId)
	{
		NpcEnchantSkillTeach.computeIfAbsent(npcId, k -> new HashSet<>()).add(classId);
	}
	
	public boolean canTeach(int npcId, int classId)
	{
		return NpcEnchantSkillTeach.containsKey(npcId) && NpcEnchantSkillTeach.get(npcId).contains(classId);
	}
}