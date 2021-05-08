package klaue.roboter.actions;

/**
 * the type of the autoclicker event
 * @author klaue
 *
 */
public enum EventType {
	MOUSE("Mouse"), KEY("Key");
	String name = null;
	private EventType(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return this.name;
	}
}
