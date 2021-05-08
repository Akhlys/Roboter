package klaue.roboter.gui.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import layout.TableLayout;
import layout.TableLayoutConstants;

public class HotkeyDialog extends JDialog implements ActionListener, NativeKeyListener {
	private static final long serialVersionUID = -2159699374663609873L;

	JPanel pnlAll = new JPanel();
	
	JLabel lblHotkeyStartStop = new JLabel("Hotkey for Start/Stop:");
	JTextField txtHotkeyStartStop = new JTextField();
	int hotkeyStartStopCode, initialHotkeyStartStopCode;

	JLabel lblHotkeyMousePos = new JLabel("Hotkey for mouse position in dialog:");
	JTextField txtHotkeyMousePos = new JTextField();
	int hotkeyMousePosCode, initialHotkeyMousePosCode;
	
	JButton btnCancel = new JButton("Cancel");
	JButton btnSave = new JButton("Save");
	
	public HotkeyDialog(int prevStartStopHotkey, int prevMouseHotkey, Frame parent, boolean modal) {
		super(parent, modal);
		GlobalScreen.addNativeKeyListener(this);
		this.hotkeyStartStopCode = this.initialHotkeyStartStopCode = prevStartStopHotkey;
		this.hotkeyMousePosCode = this.initialHotkeyMousePosCode = prevMouseHotkey;
		
		this.setTitle("Change Hotkeys");
		//this.setAlwaysOnTop(true);
		this.setSize(390, 150);
		//setMinimumSize(new Dimension(300, 150));
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		
		this.pnlAll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		double size[][] =
            {{TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL}, // column widths
             {TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED}}; // row heights
		this.pnlAll.setLayout(new TableLayout(size));

		this.txtHotkeyStartStop.setText(NativeKeyEvent.getKeyText(this.hotkeyStartStopCode));
		this.txtHotkeyMousePos.setText(NativeKeyEvent.getKeyText(this.hotkeyMousePosCode));
		this.txtHotkeyStartStop.setPreferredSize(new Dimension(100, this.txtHotkeyStartStop.getPreferredSize().height));
		//this.txtHotkeyStartStop.addKeyListener(this);
		this.txtHotkeyMousePos.setPreferredSize(new Dimension(100, this.txtHotkeyStartStop.getPreferredSize().height));
		//this.txtHotkeyMousePos.addKeyListener(this);

		this.pnlAll.add(this.lblHotkeyStartStop, "0, 0");
		this.pnlAll.add(this.txtHotkeyStartStop, "2, 0");
		this.pnlAll.add(this.lblHotkeyMousePos, "0, 2");
		this.pnlAll.add(this.txtHotkeyMousePos, "2, 2");
		
		this.btnCancel.addActionListener(this);
		this.btnSave.addActionListener(this);
		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(this.btnCancel);
		pnlButtons.add(Box.createHorizontalStrut(10));
		pnlButtons.add(this.btnSave);
		
		this.pnlAll.add(pnlButtons, "0, 4, 3, 4");
		
		this.add(this.pnlAll);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btnCancel) {
			this.hotkeyStartStopCode = this.initialHotkeyStartStopCode;
			this.hotkeyMousePosCode = this.initialHotkeyMousePosCode;
			this.dispose();
		} else if (arg0.getSource() == this.btnSave) {
			if (this.hotkeyMousePosCode == this.hotkeyStartStopCode) {
				JOptionPane.showMessageDialog(null, "Cannot set both hotkeys to the same", "Hotkeys", JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.dispose();
		}
	}
	
	public int getHotkeyStartStopCode() {
		return this.hotkeyStartStopCode;
	}

	public int getHotkeyMousePosCode() {
		return this.hotkeyMousePosCode;
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent nke) {
		int key = nke.getKeyCode();
		// figure out the source. usually this would be done adding just a key listener to
		// the text input, but alas jnativehook uses its own key codes and there doesn't seem
		// any translation function
		//if (this.isFocusOwner()) { // is dialog actually in the forefront?
			if (this.txtHotkeyMousePos.isFocusOwner()) {
				this.hotkeyMousePosCode = key;
				this.txtHotkeyMousePos.setText(NativeKeyEvent.getKeyText(key));
			} else if (this.txtHotkeyStartStop.isFocusOwner()) {
				this.hotkeyStartStopCode = key;
				this.txtHotkeyStartStop.setText(NativeKeyEvent.getKeyText(key));
			}
		//}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {}
}
