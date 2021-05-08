package klaue.roboter.actions;

import java.awt.Point;

public class MouseAction extends AutoAction {
	private static final long serialVersionUID = 5603943243425418893L;

	public MouseAction(int key, Point pos) {
		super(EventType.MOUSE, key);
		setMousePosition(pos);
	}

	@Override
	public String getDescription() {
		return "A mouse click";
	}

}
