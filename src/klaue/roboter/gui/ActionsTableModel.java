package klaue.roboter.gui;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import klaue.roboter.actions.AutoAction;
import klaue.roboter.actions.EventType;

public class ActionsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -8547814588061509694L;
	
	private String[] columnNames = {"Description", "Button", "Position", "Delay (ms)", "Delay \u25CF\u25CB (ms)"};
	ArrayList<AutoAction> actions = new ArrayList<>();

	public ArrayList<AutoAction> getActions() {
		return this.actions;
	}
	
	public void setActions(ArrayList<AutoAction> actions) {
		this.actions = actions;
		fireTableDataChanged();
	}
	
	public void addAction(AutoAction aa) {
		this.actions.add(aa);
		fireTableDataChanged();
	}
	
	public void setActionAt(AutoAction aa, int index) {
		this.actions.set(index, aa);
		fireTableDataChanged();
	}
	
	public void removeActionAt(int index) {
		this.actions.remove(index);
		fireTableDataChanged();
	}
	
	public void moveUp(int index) {
		switchActions(index, index - 1);
	}
	
	public void moveDown(int index) {
		switchActions(index, index + 1);
	}
	
	private void switchActions(int start, int target) {
		if (start < 0 || start > this.actions.size() - 1) {
			throw new ArrayIndexOutOfBoundsException(start);
		}
		if (target < 0 || target > this.actions.size() - 1) {
			throw new ArrayIndexOutOfBoundsException(target);
		}
		AutoAction aa1 = this.actions.get(start);
		AutoAction aa2 = this.actions.get(target);
		this.actions.set(start, aa2);
		this.actions.set(target, aa1);
		fireTableDataChanged();
	}
	
	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return this.actions.size();
	}
	
	@Override
	public String getColumnName(int col) {
		return this.columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		AutoAction aa = this.actions.get(row);
		switch(col) {
			case 0:
				return aa.getDescription();
			case 1:
				if (aa.getType() == EventType.KEY) {
					return KeyEvent.getKeyText(aa.getKey());
				} else if (aa.getType() == EventType.MOUSE) {
					switch(aa.getKey()) {
						default:
						case InputEvent.BUTTON1_MASK:
							return "Left";
						case InputEvent.BUTTON2_MASK:
							return "Middle";
						case InputEvent.BUTTON3_MASK:
							return "Right";
					}
				}
				return null;
			case 2:
				if (aa.getType() == EventType.KEY) return "";
				if (aa.getType() == EventType.MOUSE) {
					Point p = aa.getMousePosition();
					if (p.getX() == 0 && p.getY() == 0) {
						return "At mouse position";
					}
					return ((int)p.getX()) + " x " + ((int)p.getY());
				}
				return null;
			case 3:
				return aa.getDelay();
			case 4:
				return aa.getDelayDownUp();
			default:
				return null;
		}
	}

}
