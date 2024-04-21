package klaue.roboter.gui.dialog;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import klaue.roboter.actions.AutoAction;
import klaue.roboter.actions.EventType;
import klaue.roboter.actions.KeyAction;
import layout.TableLayout;
import layout.TableLayoutConstants;

public class KeyPanel extends EventPanel implements KeyListener {
	private static final long serialVersionUID = -7549641734238066523L;
	
	Integer key = null;
	
	JLabel lblKey = new JLabel("Key to press:");
	JTextField txtKey = new JTextField();
	
	public KeyPanel(AutoAction aa) {
		this();
		setAutoAction(aa);
	}
	
	public KeyPanel() {
		double size[][] =
            {{TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED}, // column widths
             {TableLayoutConstants.PREFERRED}}; // row heights
		this.setLayout(new TableLayout(size));
		
		this.add(this.lblKey, "0, 0");
		this.txtKey.setPreferredSize(new Dimension(100, this.txtKey.getPreferredSize().height));
		this.txtKey.addKeyListener(this);
		this.add(this.txtKey, "2, 0");
	}
	
	
	@Override
	public AutoAction getAutoAction() {
		if (this.key == null) return null;
		return new KeyAction(this.key);
	}
	
	@Override
	public void setAutoAction(AutoAction aa) {
		if (aa == null || aa.getType() != EventType.KEY) return;
		setKeyAction((KeyAction)aa);
	}
	
	public void setKeyAction(KeyAction ka) {
		if (ka == null) return;
		this.key = ka.getKey();
		this.txtKey.setText(KeyEvent.getKeyText(this.key));
	}


	@Override
	public EventType getType() {
		return EventType.KEY;
	}


	@Override
	public void keyPressed(KeyEvent arg0) {
		this.key = arg0.getKeyCode();
		this.txtKey.setText(KeyEvent.getKeyText(this.key));
	}


	@Override
	public void keyReleased(KeyEvent arg0) {
		this.key = arg0.getKeyCode();
		this.txtKey.setText(KeyEvent.getKeyText(this.key));
	}


	@Override
	public void keyTyped(KeyEvent arg0) {
		arg0.consume();
	}

}
