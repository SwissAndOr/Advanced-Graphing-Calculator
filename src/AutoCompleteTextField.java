import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class AutoCompleteTextField extends JTextField {

	private static final long serialVersionUID = -2682515412846453090L;

	private static final Pattern pattern = Pattern.compile("([a-z]+)$");

	private TreeSet<String> options;// = new TreeSet<>(Arrays.asList((Evaluator.functions.toString().replaceAll("\\^\\(\\?\\:|\\)", "") + "|e|pi|t|x").split("\\|")));
	
	private Vector<String> endings;
	private String func;

	private List<String> vars;
	
	private JList<String> stringList;
	private JScrollPane scrollPane;

	public AutoCompleteTextField() {
		this("", 0);
	}

	public AutoCompleteTextField(int columns) {
		this("", columns);
	}

	public AutoCompleteTextField(String text) {
		this(text, 0);
	}
	
	public AutoCompleteTextField(int columns, String... vars) {
		this("", columns, vars);
	}

	public AutoCompleteTextField(String text, String... vars) {
		this(text, 0, vars);
	}

	public AutoCompleteTextField(String text, int columns, String... vars) {
		super(text, columns);

		setVars(vars);
		
		options = createFunctionList();
		
		stringList = new JList<>();
		stringList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		stringList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					addText();
					setPopupShowing(false);
				}
			}
		});

		scrollPane = new JScrollPane(stringList);

		Listeners l = new Listeners();

		this.addFocusListener(l);
		this.getDocument().addDocumentListener(l);
		this.addKeyListener(l);
		this.addCaretListener(l);
	}
	
	private TreeSet<String> createFunctionList() {
		TreeSet<String> ret = new TreeSet<>();
		
		for (String s : Evaluator.functions.toString().replaceAll("\\Q^(?:\\E|\\)", "").split("\\|")) {
			ret.add(s);
		}
		
		ret.addAll(vars);
		
		return ret;
	}
	
	public void setVars(String... vars) {
		this.vars = new ArrayList<>(Arrays.asList(vars));
		this.vars.add("e");
		this.vars.add("pi");
		
		this.options = createFunctionList();
	}

	private class Listeners extends KeyAdapter implements FocusListener, DocumentListener, CaretListener {

		private boolean shouldProcessCaretEvent = true;

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case 38: // UP
					if (popupShowing) {
						int newI = stringList.getSelectedIndex() - 1;
						if (newI == -1) newI = endings.size() - 1;

						stringList.setSelectedIndex(newI);
						stringList.ensureIndexIsVisible(newI);
					}
					break;
				case 40: // DOWN
					if (popupShowing) {
						int newI = stringList.getSelectedIndex() + 1;
						if (newI == endings.size()) newI = 0;

						stringList.setSelectedIndex(newI);
						stringList.ensureIndexIsVisible(newI);
					}
					break;
				case 10: // ENTER
					if (popupShowing && !stringList.isSelectionEmpty())
						addText();
				case 27: // ESC
					setPopupShowing(false);
					break;
				case 32: // SPACE
					if (e.getModifiers() != KeyEvent.CTRL_MASK) break;
					
					Matcher m = pattern.matcher(getText().toLowerCase().substring(0, getCaretPosition()));
					recalculateList(m.find() ? m.group(1) : "");
					break;
				default:
					break;
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {}

		@Override
		public void insertUpdate(DocumentEvent e) {
			shouldProcessCaretEvent = false;

			if (popup != null) setPopupShowing(false);

			Matcher m = pattern.matcher(getText().toLowerCase().substring(0, getCaretPosition() + 1));
			if (!m.find()) return;

			recalculateList(m.group(1));
		}

		@Override
		public void removeUpdate(DocumentEvent e) {}

		@Override
		public void focusGained(FocusEvent e) {}

		@Override
		public void focusLost(FocusEvent e) {
			setPopupShowing(false);
		}

		@Override
		public void caretUpdate(CaretEvent e) {
			if (!shouldProcessCaretEvent) {
				shouldProcessCaretEvent = true;
				return;
			}
			setPopupShowing(false);
		}

	}

	private boolean popupShowing = false;
	private Popup popup;

	private void setPopupShowing(boolean showing) {
		if (showing == popupShowing || popup == null) return;
		
		if (showing) {
			popup.show();
		} else {
			popup.hide();
			popup = null;
		}

		this.popupShowing = showing;
	}

	private void addText() {
		try {
			String s = stringList.getSelectedValue();
			this.getDocument().insertString(this.getCaretPosition(), s.replaceFirst("\\Q" + func + "\\E", ""), null);
			if (!vars.contains(s)) {
				this.getDocument().insertString(this.getCaretPosition(), "()", null);
				this.setCaretPosition(this.getCaretPosition() - 1);
			}
		} catch (BadLocationException e) {}
	}

	private void recalculateList(String func) {
		this.func = func;
		
		if (options == null) return;
		
		String s = options.ceiling(func);

		if (!s.startsWith(func)) return;

		endings = new Vector<>();
		Iterator<String> iter = options.iterator();
		while (!iter.next().equals(s)) {}

		endings.addElement(s);
		while (iter.hasNext()) {
			String str = iter.next();
			if (!str.startsWith(func)) break;
			endings.addElement(str);
		}

		stringList.setListData(endings);
		stringList.setVisibleRowCount(Math.min(8, endings.size()));
		stringList.setFixedCellWidth(50);

		Rectangle r;
		try {
			r = this.modelToView(this.getCaretPosition());
		} catch (BadLocationException exception) {
			return;
		}

		Point p = this.getLocationOnScreen();
		popup = PopupFactory.getSharedInstance().getPopup(this, scrollPane, p.x + r.x, p.y + r.height + this.getBorder().getBorderInsets(this).bottom);
		setPopupShowing(true);
	}

}
