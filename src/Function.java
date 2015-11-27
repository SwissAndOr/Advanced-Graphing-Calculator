import java.awt.Color;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Function {

	public String name;
	public String string = "";
	public Color color = Color.BLUE;
	public int thickness = 2;
	public boolean enabled = true;

	private static ScriptEngineManager mgr = new ScriptEngineManager();
	private static ScriptEngine engine = mgr.getEngineByName("JavaScript");

	public Function(String newName) {
		name = newName;
	}

	public double evaluate(double x) {
		try {
			return ((Number) engine.eval(string.replace("x", "(" + Double.toString(x) + ")"))).doubleValue();
		} catch (ScriptException e) {
			// TODO: The error message below is displayed once for every horizontal pixel. Fix it.
			// JOptionPane.showMessageDialog(Main.window, "Something's wrong with the function", "Error", JOptionPane.ERROR_MESSAGE);
//			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
