package klaue.roboter.actions;

import java.awt.Point;

public class KeyAction extends AutoAction {
	private static final long serialVersionUID = 2267473433169278576L;

	public KeyAction(int key) {
		super(EventType.KEY, key);
	}

	@Override
	public String getDescription() {
		return "A key press";
	}

	@Override
	public Point getMousePosition() {
		return new Point(0, 0);
	}
	
	@Override
	public void setMousePosition(Point mousePosition) {
		return; // do nothing
	}
}
