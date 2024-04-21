package klaue.roboter;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import klaue.roboter.actions.ActionsPacket;
import klaue.roboter.actions.AutoAction;
import klaue.roboter.actions.EventType;

public class AutoActionsPerformer implements Runnable {
	ActionsPacket actionsPacket = null;
	boolean stopped = true;
	boolean abort = false;
	private static boolean isMac = false;
	static {
		String OS = System.getProperty("os.name", "generic").toLowerCase();
		isMac = OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0;
	}

	Robot robot = new Robot();
	
	public AutoActionsPerformer(ActionsPacket packet) throws AWTException {
		if (packet == null) return;
		this.actionsPacket = packet;
	}
	
	@Override
	public void run() {
		this.stopped = false;
		
		sleepFor(this.actionsPacket.preDelay);
		
		if (this.actionsPacket.repetitions > 0) {
			for (int i = 0; i < this.actionsPacket.repetitions; ++i) {
				oneLoop(i == this.actionsPacket.repetitions - 1);
				if (this.abort) break;
			}
		} else {
			// endless repetition
			while(true) {
				oneLoop(false);
				if (this.abort) break;
			}
		}
		this.stopped = true;
		this.abort = false;
	}
	
	private void oneLoop(boolean lastLoop) {
		for (int j = 0; j < this.actionsPacket.getList().size(); ++j) {
			AutoAction ao = this.actionsPacket.getList().get(j);
			if (this.abort) break;
			
			Point position = null;
			Point prePosition = null;
			boolean moveMouse = false;
			if (ao.getType() == EventType.MOUSE) {
				position = ao.getMousePosition();
				if (position.x != 0 || position.y != 0) {
					moveMouse = true;
					if (this.actionsPacket.returnMouse) {
						prePosition = MouseInfo.getPointerInfo().getLocation();
					}
				} else if (isMac) {
					// mac has a problem with "clicking where the mouse is" because Roboters internal clicking location
					// is not updated after the first click so each mousePress() lands on that location
					position = MouseInfo.getPointerInfo().getLocation();
					moveMouse = true;
				}
			}
			
			if (moveMouse) {
				this.robot.mouseMove(position.x, position.y);
			}
			
			if (ao.getType() == EventType.MOUSE) {
				this.robot.mousePress(ao.getKey());
				this.robot.mouseRelease(ao.getKey());
			} else {
				this.robot.keyPress(ao.getKey());
				this.robot.keyRelease(ao.getKey());
			}
			
			if (prePosition != null) { // only not null if should be returned
				this.robot.mouseMove(prePosition.x, prePosition.y);
			}

			// don't sleep for the very last one
			if (j != this.actionsPacket.getList().size() - 1 || !lastLoop) {
				sleepFor(ao.getDelay());
			}
		}
	}
	
	/**
	 * sleep for a max of delay in increments of 100 ms to catch abort
	 * @param delay
	 */
	private void sleepFor(int delay) {
		int rest = (delay > 100) ? delay % 100 : delay;
		int fullParts = (delay > 100) ? delay / 100 : 0;
		
		int[] parts = new int[fullParts + (rest > 0 ? 1 : 0)];
		for (int i = 0; i < fullParts; ++i) {
			parts[i] = 100;
		}
		if (rest > 0) parts[fullParts] = rest;
		
		for (int part : parts) {
			if (this.abort) break;
			try {
				Thread.sleep(part);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isRunning() {
		if (this.abort) return false; // fake it till you make it
		return !this.stopped;
	}
	
	public void abort() {
		this.abort = true;
	}

}
