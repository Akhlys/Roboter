package klaue.roboter.gui.dialog;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;

import klaue.roboter.actions.AutoAction;
import klaue.roboter.actions.EventType;
import klaue.roboter.gui.NumberOnlyFilter;
import layout.TableLayout;
import layout.TableLayoutConstants;

public class AddDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -2159699374663609873L;
	
	AutoAction autoAction = null;
	
	JPanel pnlAll = new JPanel();
	JPanel pnlEvent = new JPanel();
	CardLayout cards = new CardLayout();
	
	JLabel lblType = new JLabel("Type:");
	JComboBox<EventType> typeBox = new JComboBox<>(EventType.values());

	JLabel lblDelay = new JLabel("Delay after:");
	JTextField txtDelay = new JTextField();
	JLabel lblMs = new JLabel("ms");
	
	JButton btnCancel = new JButton("Cancel");
	JButton btnSave = new JButton("Save");
	
	public AddDialog(Frame parent, boolean modal) {
		this(parent, modal, null);
	}
	
	public AddDialog(Frame parent, boolean modal, AutoAction actionToEdit) {
		super(parent, modal);
		addTooltips();
		
		this.setTitle("Add an event");
		//this.setAlwaysOnTop(true);
		this.setSize(450, 300);
		setMinimumSize(new Dimension(420, 250));
		this.setLocationRelativeTo(parent);

		this.pnlAll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		double size[][] =
            {{TableLayoutConstants.FILL}, // column widths
             {TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, 10, TableLayoutConstants.PREFERRED}}; // row heights
		this.pnlAll.setLayout(new TableLayout(size));
		
		JPanel pnlType = new JPanel();
		pnlType.setLayout(new BoxLayout(pnlType, BoxLayout.X_AXIS));
		pnlType.add(this.lblType);
		pnlType.add(Box.createHorizontalStrut(10));
		
		this.typeBox.setMaximumSize(this.typeBox.getPreferredSize()); // for width
		if (actionToEdit != null) {
			this.typeBox.setSelectedItem(actionToEdit.getType());
		}
		this.typeBox.addActionListener(this);
		pnlType.add(this.typeBox);
		pnlType.add(Box.createHorizontalGlue());
		pnlType.setMaximumSize(new Dimension(pnlType.getMaximumSize().width, pnlType.getPreferredSize().height));
		
		this.pnlAll.add(pnlType, "0, 0");
		
		this.pnlEvent.setLayout(this.cards);
		// EventPanel with wrong Autoaction type or if it's null will exit safely
		this.pnlEvent.add(new KeyPanel(actionToEdit), EventType.KEY.toString());
		this.pnlEvent.add(new MousePanel(actionToEdit), EventType.MOUSE.toString());
		this.cards.show(this.pnlEvent, this.typeBox.getSelectedItem().toString());
		this.pnlAll.add(this.pnlEvent, "0, 2");
		
		JPanel pnlDelay = new JPanel();
		pnlDelay.setLayout(new BoxLayout(pnlDelay, BoxLayout.X_AXIS));
		pnlDelay.add(this.lblDelay);
		pnlDelay.add(Box.createHorizontalStrut(10));
		this.txtDelay.setMaximumSize(new Dimension(70, this.txtDelay.getPreferredSize().height));
		this.txtDelay.setPreferredSize(this.txtDelay.getMaximumSize());
		((AbstractDocument)this.txtDelay.getDocument()).setDocumentFilter(new NumberOnlyFilter());
		if (actionToEdit != null) {
			this.txtDelay.setText(Integer.toString(actionToEdit.getDelay()));
		}
		pnlDelay.add(this.txtDelay);
		pnlDelay.add(Box.createHorizontalStrut(5));
		pnlDelay.add(this.lblMs);
		pnlDelay.add(Box.createHorizontalGlue());
		
		this.pnlAll.add(pnlDelay, "0, 4");
		
		this.btnCancel.addActionListener(this);
		this.btnSave.addActionListener(this);
		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
		pnlButtons.add(Box.createHorizontalGlue());
		pnlButtons.add(this.btnCancel);
		pnlButtons.add(Box.createHorizontalStrut(10));
		pnlButtons.add(this.btnSave);
		
		this.pnlAll.add(pnlButtons, "0, 6");
		
		this.add(this.pnlAll);
		this.setVisible(true);
	}
	
	private void addTooltips() {
		String type = "The type of the event to add";
		this.lblType.setToolTipText(type);
		this.typeBox.setToolTipText(type);
		
		String delay = "How many milliseconds to wait after the previous event or each round if this one is the only one";
		this.lblDelay.setToolTipText(delay);
		this.txtDelay.setToolTipText(delay);
		this.lblMs.setToolTipText(delay);
		
		this.btnCancel.setToolTipText("Abort adding of the event");
		this.btnSave.setToolTipText("Add the event");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == this.btnCancel) {
			this.dispose();
		} else if (arg0.getSource() == this.typeBox) {
			this.cards.show(this.pnlEvent, this.typeBox.getSelectedItem().toString());
		} else if (arg0.getSource() == this.btnSave) {
			EventType currentEvent = (EventType)this.typeBox.getSelectedItem();
			EventPanel pnlToSave = null;
			for (Component comp : this.pnlEvent.getComponents() ) {
				if (!(comp instanceof EventPanel)) continue;
				if (((EventPanel)comp).getType() == currentEvent) {
					pnlToSave = (EventPanel)comp; 
					break;
				}
			}
			
			if (pnlToSave == null) {
				JOptionPane.showMessageDialog(this, "Some error happened o.o try again", "What", JOptionPane.ERROR_MESSAGE);
				this.dispose();
			}
			
			this.autoAction = pnlToSave.getAutoAction();
			if (this.autoAction == null || this.txtDelay.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Not all required fields are set", "Cannot Save", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			this.autoAction.setDelay(Integer.parseInt(this.txtDelay.getText())); // txtDelay can only contain numbers
			this.dispose();
		}
	}

	public AutoAction getAutoAction() {
		return this.autoAction;
	}
}
