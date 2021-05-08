package klaue.roboter.gui.dialog;

import javax.swing.JPanel;

import klaue.roboter.actions.AutoAction;
import klaue.roboter.actions.EventType;

public abstract class EventPanel extends JPanel {
	private static final long serialVersionUID = -1585163025848041194L;
	public abstract AutoAction getAutoAction();
	public abstract void setAutoAction(AutoAction a);
	public abstract EventType getType();
}
