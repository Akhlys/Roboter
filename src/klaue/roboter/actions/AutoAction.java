package klaue.roboter.actions;

import java.awt.Point;
import java.io.Serializable;


public abstract class AutoAction implements Serializable {
	private static final long serialVersionUID = -4577387308909228537L;
	
	EventType type = null;
	Point mousePosition = new Point(0, 0);
	int key = 0; // can be keycode of normal key or mouse button code
	int delay = 0;
	
	/**
	 * 
	 * @param type the type of event
	 * @param key the keycode of normal key (KeyEvent.xxx) or of the mouse button (InputEvent.xxx)
	 */
	public AutoAction(EventType type, int key) {
		this.type = type;
		this.key = key;
	}

	public Point getMousePosition() {
		return this.mousePosition;
	}

	public void setMousePosition(Point mousePosition) {
		this.mousePosition = mousePosition;
	}

	public int getDelay() {
		return this.delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public EventType getType() {
		return this.type;
	}

	public int getKey() {
		return this.key;
	}
	
	public abstract String getDescription();
}
