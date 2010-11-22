package org.nebula.userData;

import java.util.ArrayList;

public class Groups {
	private static ArrayList<Group> groupList = new ArrayList<Group>();
	
	public static void add(Group group){
		groupList.add(group);
	}
	
	public static Group get(int i){
		return groupList.get(i);
	}
	
	public static Group set(int i, Group group){
		return groupList.set(i,group);
	}
	
	public static boolean contains(Group group){
		return groupList.contains(group);
	}
	
	public static boolean isEmpty(){
		return groupList.isEmpty();
	}
	
	public static int size(){
		return groupList.size();
	}
}
