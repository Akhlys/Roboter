package klaue.roboter.actions;

import java.io.Serializable;
import java.util.ArrayList;


public class ActionsPacket  implements Serializable {
	private static final long serialVersionUID = -9144774536849184537L;
	
	public ArrayList<AutoAction> list = new ArrayList<>();
	public int preDelay = 0;
	public int repetitions = 0;
	public boolean returnMouse = false;
	
	public ArrayList<AutoAction> getList() {
		return this.list;
	}
	public void setList(ArrayList<AutoAction> list) {
		this.list = list;
	}
	public int getPreDelay() {
		return this.preDelay;
	}
	public void setPreDelay(int preDelay) {
		this.preDelay = preDelay;
	}
	public int getRepetitions() {
		return this.repetitions;
	}
	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}
	public boolean isReturnMouse() {
		return this.returnMouse;
	}
	public void setReturnMouse(boolean returnMouse) {
		this.returnMouse = returnMouse;
	}
}
