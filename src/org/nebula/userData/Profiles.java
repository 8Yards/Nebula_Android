package org.nebula.userData;

import java.util.ArrayList;

public class Profiles {
	private ArrayList<Profile> profileList = new ArrayList<Profile>();
	
	public void add(Profile profile){
		profileList.add(profile);
	}
	
	public Profile get(int i){
		return profileList.get(i);
	}
	
	public ArrayList<Profile> returnProfiles(){
		return profileList;
	}
	
	public Profile set(int i, Profile profile){
		return profileList.set(i,profile);
	}
	
	public boolean contains(Profile profile){
		return profileList.contains(profileList);
	}
	
	public boolean isEmpty(){
		return profileList.isEmpty();
	}
	
	public int size(){
		return profileList.size();
	}
}
