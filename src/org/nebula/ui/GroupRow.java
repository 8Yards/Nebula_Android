/*
 * author - prajwol
 */

package org.nebula.ui;

public class GroupRow {
	private String name;
	private boolean isChecked;

	public GroupRow(String name) {
		this(name, false);
	}

	public GroupRow(String name, boolean isChecked) {
		super();
		this.name = name;
		this.isChecked = isChecked;
	}

	public String toString() {
		return this.name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
}
