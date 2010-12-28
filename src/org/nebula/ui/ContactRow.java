/*
 * author - prajwol, sujan
 */

package org.nebula.ui;

public class ContactRow {
	//private String status;
	private String userName;
	private boolean isChecked;

	public ContactRow(String userName) {
		this("Offline", userName, false);
	}

	public ContactRow(String status, String userName, boolean isChecked) {
		super();
		//this.status = status;
		this.userName = userName;
		this.isChecked = isChecked;
	}

	public String toString() {
		return this.userName;
	}

	/*public String getStatus() {
		return status;
	}*/

	/*public void setStatus(String status) {
		this.status = status;
	}*/

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
}
