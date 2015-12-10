import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class NumberTextField extends JTextField {

	private static final long serialVersionUID = -7092435382692447120L;
	private static final FocusListener focusListener = new FocusAdapter() {

		public void focusLost(FocusEvent e) {
			NumberTextField tf = (NumberTextField) e.getSource();
			try {
				tf.setValue(Evaluator.evaluate(Evaluator.toRPN(tf.getText()), Double.NaN));
			} catch (Exception exception) {
				tf.setText(tf.lastText);
			}
			tf.setCaretPosition(0);
		};

	};

	private String lastText;

	public NumberTextField() {
		this("", 0);
	}

	public NumberTextField(int columns) {
		this("", columns);
	}

	public NumberTextField(String text) {
		this(text, 0);
	}

	public NumberTextField(String text, int columns) {
		super(text, columns);
		
		double value = 0;
		try {
			value = Evaluator.evaluate(Evaluator.toRPN(text), Double.NaN);
		} catch (Exception e) {}
		
		lastText = "";
		
		this.setValue(value);

		this.addFocusListener(focusListener);
		
		this.setCaretPosition(0);
	}

	public NumberTextField(double value, int columns) {
		super(Double.isFinite(value) ? "" : Main.numbers.format(value), columns);

		lastText = super.getText();
		
		this.setValue(value);

		this.addFocusListener(focusListener);
		
		this.setCaretPosition(0);
	}

	@Override
	public void setText(String text) {
		double value = 0;
		try {
			value = Evaluator.evaluate(Evaluator.toRPN(text), Double.NaN);
		} catch (Exception e) {
			super.setText(lastText);
			return;
		}
		this.setValue(value);
	}

	public void setValue(double d) {
		if (!Double.isFinite(d)) {
			super.setText(lastText);
			return;
		}
		super.setText(Main.numbers.format(d));
		lastText = getText();
	}

}
