package klaue.roboter.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import klaue.roboter.AutoActionsPerformer;
import klaue.roboter.Version;
import klaue.roboter.actions.ActionsPacket;
import klaue.roboter.actions.AutoAction;
import klaue.roboter.gui.dialog.AddDialog;
import klaue.roboter.gui.dialog.HotkeyDialog;
import layout.TableLayout;
import layout.TableLayoutConstants;

public class RoboterGui extends JFrame implements ActionListener, ListSelectionListener, MouseListener {
	private static final long serialVersionUID = 7310993201912901144L;
	
	JButton btnSave = new JButton("Save");
	JButton btnLoad = new JButton("Load");
	JButton btnAdd = new JButton("Add to List");
	JButton btnRemove = new JButton("Remove from List");
	JButton btnStartStop = null; // new JButton("Start/Stop"); // has hotkey added if avail.
	JButton btnUp = new JButton("\u25B2"); // black up-pointing triangle, ▲
	JButton btnDown = new JButton("\u25BC"); // black down-pointing triangle, ▼
	JButton btnHotkeySettings = new JButton("Change Hotkeys");
	
	JLabel lblPreDelay = new JLabel("Pre-Delay:");
	JTextField txtPreDelay = new JTextField("0");
	JLabel lblMs = new JLabel("ms, ");
	
	JLabel lblRepetitions = new JLabel("Repetitions:");
	JTextField txtRepetitions = new JTextField("0");
	
	JCheckBox chkReturnMouse = new JCheckBox("Return mouse");
	
	ActionsTableModel tableModel = new ActionsTableModel();
	JTable itemTable = new JTable(this.tableModel);
	
	JPanel pnlAll = new JPanel();
	
	File saveDir = null;
	FileNameExtensionFilter fileExtensionFilter = new FileNameExtensionFilter("Roboter files (*.rbt)", "rbt");
	AutoActionsPerformer autoActionsPerformer = null;
	
	int hotkeyClick;
	int hotkeyMousePos;
	boolean pauseHotkeyHandling = false;
	
	public RoboterGui() {
		// Set the event dispatcher to a swing safe executor service.
		GlobalScreen.setEventDispatcher(new SwingDispatchService());
		
		Preferences prefs = Preferences.userRoot().node("klaue/roboter");
		this.hotkeyClick = prefs.getInt("hotkey_click", NativeKeyEvent.VC_F6);
		this.hotkeyMousePos =  prefs.getInt("hotkey_mousepos", NativeKeyEvent.VC_F7);
		
		this.setTitle("Roboter v. " + Version.version + " by Klaue");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		boolean hotkeysAvailable = false;
		try {
			GlobalScreen.registerNativeHook();
			hotkeysAvailable = true;
		} catch (NativeHookException e) {
			e.printStackTrace();
			hotkeysAvailable = false;
			// continue w/o hotkeys, UnifiedHotkeyProvider.INSTANCE.areHotkeysAvailable() will not neccessarily be false,
			// possible that hotkeys are available but those particular ones couldn't be registered (for example on linux when started twice)
		}
		
		if (!hotkeysAvailable) {
			JOptionPane.showMessageDialog(null, "Could not add all Hotkeys, will start without", "Hotkeys", JOptionPane.ERROR_MESSAGE);
			this.btnHotkeySettings.setEnabled(false);
		}
		
		if (!hotkeysAvailable) {
			this.btnStartStop = new JButton("Start/Stop");
		} else {
			this.btnStartStop = new JButton("Start/Stop (" + NativeKeyEvent.getKeyText(this.hotkeyClick) + ")");
			// add hotkey
			GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
				@Override
				public void nativeKeyPressed(NativeKeyEvent nke) {
					if (nke.getKeyCode() == RoboterGui.this.hotkeyClick && !RoboterGui.this.pauseHotkeyHandling) {
						RoboterGui.this.btnStartStop.doClick();
					}
				}
				@Override
				public void nativeKeyReleased(NativeKeyEvent arg0) {}
				@Override
				public void nativeKeyTyped(NativeKeyEvent arg0) {}
				
			});
		}
		
		this.setSize(600, 300);
		setMinimumSize(new Dimension(430,260));
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		
		this.pnlAll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		addToolTips();
		
		// numbers only
		((AbstractDocument)this.txtPreDelay.getDocument()).setDocumentFilter(new NumberOnlyFilter());
		((AbstractDocument)this.txtRepetitions.getDocument()).setDocumentFilter(new NumberOnlyFilter());
		
		// set default button states
		this.btnSave.setEnabled(false);
		this.btnRemove.setEnabled(false);
		this.btnUp.setEnabled(false);
		this.btnDown.setEnabled(false);
		this.btnStartStop.setEnabled(false);
		
		// add button actions
		this.btnSave.addActionListener(this);
		this.btnLoad.addActionListener(this);
		this.btnAdd.addActionListener(this);
		this.btnRemove.addActionListener(this);
		this.btnUp.addActionListener(this);
		this.btnDown.addActionListener(this);
		this.btnStartStop.addActionListener(this);
		this.btnHotkeySettings.addActionListener(this);
		
		placeGuiElements();
		
		this.add(this.pnlAll);
		this.setVisible(true);
	}
	
	private void addToolTips() {
		this.btnSave.setToolTipText("Save settings to file");
		this.btnLoad.setToolTipText("Load settings from file");
		this.btnAdd.setToolTipText("Add new Entry to List");
		this.btnRemove.setToolTipText("Remove selected Item from List");
		
		if (!GlobalScreen.isNativeHookRegistered()) {
			this.btnStartStop.setToolTipText("Start/Stop clicking (don't forget pre delay)");
		} else {
			this.btnStartStop.setToolTipText("Start/Stop clicking (Hotkey: " + NativeKeyEvent.getKeyText(this.hotkeyClick) + ")");
		}
		this.btnUp.setToolTipText("Move item up in list");
		this.btnDown.setToolTipText("Move item down in list");
		
		String preDelay = "Delay before the first click in milliseconds";
		if (!GlobalScreen.isNativeHookRegistered()) preDelay += ". Can be left empty due to hotkey.";
		this.lblPreDelay.setToolTipText(preDelay);
		this.txtPreDelay.setToolTipText(preDelay);
		this.lblMs.setToolTipText(preDelay);
		
		String reps = "How often the whole list should be repeated, 0 for endless (can be prematurely stopped by pressing Start/Stop";
		reps += !GlobalScreen.isNativeHookRegistered() ? " or hotkey)" : ")";
		this.lblRepetitions.setToolTipText(reps);
		this.txtRepetitions.setToolTipText(reps);
		
		this.chkReturnMouse.setToolTipText("<html>After each auto-click, returns the mouse to the position it was before clicking.</html>");
		
		this.btnHotkeySettings.setToolTipText("Change hotkeys");
	}
	
	private void placeGuiElements() {
		// get table layout dimensions
		double size[][] =
            {{TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED}, // column widths
             {TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.FILL, 10, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED}}; // row heights
		
		this.pnlAll.setLayout (new TableLayout(size));
		
		this.pnlAll.add(this.btnLoad,   "0, 0");
		this.pnlAll.add(this.btnSave,   "2, 0");
		this.pnlAll.add(this.btnAdd,    "0, 2");
		this.pnlAll.add(this.btnRemove, "2, 2");
		this.pnlAll.add(this.btnHotkeySettings, "4, 0");

		this.itemTable.getSelectionModel().addListSelectionListener(this);
		this.itemTable.addMouseListener(this);
		this.itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.pnlAll.add(new JScrollPane(this.itemTable), "0, 4, 4, 8");
		
		this.pnlAll.add(this.btnUp,   "6, 4");
		this.pnlAll.add(this.btnDown, "6, 6");
		
		// new panel for lower components as that looks better
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
		this.txtPreDelay.setMaximumSize(new Dimension(70, this.txtPreDelay.getPreferredSize().height));
		this.txtPreDelay.setPreferredSize(this.txtPreDelay.getMaximumSize());
		this.txtRepetitions.setMaximumSize(new Dimension(50, this.txtRepetitions.getPreferredSize().height));
		this.txtRepetitions.setPreferredSize(this.txtRepetitions.getMaximumSize());
		pnl.add(this.lblPreDelay);
		pnl.add(Box.createHorizontalStrut(10));
		pnl.add(this.txtPreDelay);
		pnl.add(Box.createHorizontalStrut(5));
		pnl.add(this.lblMs);
		pnl.add(this.lblRepetitions);
		pnl.add(Box.createHorizontalStrut(10));
		pnl.add(this.txtRepetitions);
		pnl.add(Box.createHorizontalGlue());
		pnl.add(this.chkReturnMouse);
		this.pnlAll.add(pnl, "0, 10, 5, 10");
		
		this.pnlAll.add(this.btnStartStop, "0, 12, 6, 12");
	}
	
	public ActionsPacket getActionsPacket() {
		ActionsPacket ap = new ActionsPacket();
		ap.list = this.tableModel.getActions();
		if (this.txtPreDelay.getText().trim().isEmpty()) {
			ap.preDelay = -1;
		} else {
			ap.preDelay = Integer.parseInt(this.txtPreDelay.getText().trim());
		}
		if (this.txtRepetitions.getText().trim().isEmpty()) {
			ap.repetitions = 0;
		} else {
			ap.repetitions = Integer.parseInt(this.txtRepetitions.getText().trim());
		}
		ap.returnMouse = this.chkReturnMouse.isSelected();
		return ap;
	}
	
	public void setActionsPacket(ActionsPacket ap) {
		if (ap.preDelay >= 0) {
			this.txtPreDelay.setText(Integer.toString(ap.preDelay));
		}
		if (ap.repetitions > 0) {
			this.txtRepetitions.setText(Integer.toString(ap.repetitions));
		}
		this.tableModel.setActions(ap.getList());
		
		this.btnSave.setEnabled(ap.getList().size() > 0);
		this.btnStartStop.setEnabled(ap.getList().size() > 0);
		
		if (ap.getList().size() > 0) {
			this.itemTable.setRowSelectionInterval(0, 0);
		}
		this.chkReturnMouse.setSelected(ap.isReturnMouse());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btnAdd) {
			AddDialog dialog = new AddDialog(this, true);
			AutoAction aa = dialog.getAutoAction();
			if (aa != null) {
				this.tableModel.addAction(aa);
				
				// doesn't hurt if already enabled
				this.btnSave.setEnabled(true);
				this.btnStartStop.setEnabled(true);
				
				this.itemTable.setRowSelectionInterval(this.tableModel.getRowCount() - 1, this.tableModel.getRowCount() - 1);
			}
		} else if (e.getSource() == this.btnUp || e.getSource() == this.btnDown) {
			int selectedRow = this.itemTable.getSelectedRow();
			int maxRow = this.tableModel.getRowCount() - 1;
			if (selectedRow == -1 || maxRow == 0) return;
			if (e.getSource() == this.btnUp) {
				this.tableModel.moveUp(selectedRow);
				this.itemTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
			} else {
				this.tableModel.moveDown(selectedRow);
				this.itemTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
			}
		} else if(e.getSource() == this.btnRemove) {
			int selectedRow = this.itemTable.getSelectedRow();
			if (selectedRow == -1) return;
			this.tableModel.removeActionAt(selectedRow);
			if (selectedRow - 1 >= 0) {
				this.itemTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
			} else if(this.tableModel.getRowCount() > 0) {
				this.itemTable.setRowSelectionInterval(0, 0);
			}
			
			if (this.tableModel.getRowCount() == 0) {
				this.btnSave.setEnabled(false);
				this.btnStartStop.setEnabled(false);
			}
		} else if (e.getSource() == this.btnSave) {
			JFileChooser saveChooser = new JFileChooser(this.saveDir);
			saveChooser.setDialogTitle("Saving settings");
			saveChooser.setFileFilter(this.fileExtensionFilter);
			
			int result = saveChooser.showSaveDialog(this);
			if (result != JFileChooser.APPROVE_OPTION) return;
			
			File f = saveChooser.getSelectedFile();
			if (f == null) return;
			if (!f.getName().toLowerCase().endsWith(".rbt")) f = new File(f.getAbsolutePath() + ".rbt");
			
			if (f.exists()) {
				int reply = JOptionPane.showConfirmDialog(this, "File exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.NO_OPTION) return;
				f.delete();
			}
			
			this.saveDir = f.getParentFile();
			
			try (FileOutputStream fileOut = new FileOutputStream(f);
					ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
				objOut.writeObject(getActionsPacket());
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Could not save", "Save", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == this.btnLoad) {
			JFileChooser loadChooser = new JFileChooser(this.saveDir);
			loadChooser.setDialogTitle("Loading settings");
			loadChooser.setFileFilter(this.fileExtensionFilter);
			
			int result = loadChooser.showOpenDialog(this);
			if (result != JFileChooser.APPROVE_OPTION) return;
			
			File f = loadChooser.getSelectedFile();
			
			this.saveDir = f.getParentFile();
			
			try (FileInputStream fileIn = new FileInputStream(f);
					ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
				setActionsPacket((ActionsPacket)objIn.readObject());
			} catch (IOException | ClassNotFoundException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Could not load", "Load", JOptionPane.ERROR_MESSAGE);
			}
		} else if(e.getSource() == this.btnStartStop) {
			if (this.autoActionsPerformer != null && this.autoActionsPerformer.isRunning()) {
				this.autoActionsPerformer.abort();
				this.btnStartStop.setBackground(this.btnAdd.getBackground());
			} else {
				if (this.txtPreDelay.getText().isEmpty() || this.txtRepetitions.getText().isEmpty()
						|| this.tableModel.getActions().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Not all values filled", "Fill fields", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ActionsPacket aap = getActionsPacket();
				try {
					this.autoActionsPerformer = new AutoActionsPerformer(aap);
					this.btnStartStop.setBackground(Color.GREEN);
					
					final Thread worker = new Thread(this.autoActionsPerformer);
					worker.start();
					
					// color resetter
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								worker.join();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							RoboterGui.this.btnStartStop.setBackground(RoboterGui.this.btnAdd.getBackground());
						}
					}).start();
				} catch (AWTException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Could not start autoclicker", "Meh", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getSource() == this.btnHotkeySettings) {
			if (!GlobalScreen.isNativeHookRegistered()) return;
			this.pauseHotkeyHandling = true;
			HotkeyDialog dialog = new HotkeyDialog(this.hotkeyClick, this.hotkeyMousePos, this, true);
			int newClickHotkey = dialog.getHotkeyStartStopCode();
			int newMouseHotkey = dialog.getHotkeyMousePosCode();

			Preferences prefs = Preferences.userRoot().node("klaue/roboter");
			if (newClickHotkey != this.hotkeyClick) {
				this.hotkeyClick = newClickHotkey;
				prefs.putInt("hotkey_click", newClickHotkey);
				this.btnStartStop.setText("Start/Stop (" + NativeKeyEvent.getKeyText(newClickHotkey) + ")");
			}
			if (newMouseHotkey != this.hotkeyMousePos) {
				this.hotkeyMousePos = newMouseHotkey;
				prefs.putInt("hotkey_mousepos", newMouseHotkey);
			}
			this.pauseHotkeyHandling = false;
			
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse) {
		int selectedRow = this.itemTable.getSelectedRow();
		this.btnRemove.setEnabled(selectedRow != -1);
		
		int maxRow = this.tableModel.getRowCount() - 1;
		if (maxRow == 0 || selectedRow == -1) {
			this.btnUp.setEnabled(false);
			this.btnDown.setEnabled(false);
		} else {
			this.btnUp.setEnabled(selectedRow != 0);
			this.btnDown.setEnabled(selectedRow != maxRow);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getSource() == this.itemTable) {
			Point p = arg0.getPoint();
			int row = this.itemTable.rowAtPoint(p);
			if (arg0.getClickCount() == 2 && row != -1) {
				AutoAction a = this.tableModel.getActions().get(row);
				AddDialog dialog = new AddDialog(this, true, a);
				a = dialog.getAutoAction();
				if (a != null) {
					this.tableModel.setActionAt(a, row);
					this.itemTable.setRowSelectionInterval(row, row);
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
	

	
	public static void main(String[] args) {
		final Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.SEVERE);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new RoboterGui();
			}
		});
	}
}
