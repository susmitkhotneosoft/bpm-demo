package com.neosoft.beans;

public class UserBean {
	
	private String name;
	
	private long age;
	
	private boolean isUserAdmin;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getAge() {
		return age;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public boolean isUserAdmin() {
		return isUserAdmin;
	}

	public void setUserAdmin(boolean isUserAdmin) {
		this.isUserAdmin = isUserAdmin;
	}
	
}
