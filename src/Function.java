import java.awt.Color;
import java.util.Stack;

public class Function {

	public String name;
	public String string = "";
	private Stack<Object> rpn;
	public Color color = Color.BLUE;
	public int thickness = 2;
	public boolean enabled = true;

//	private static ScriptEngineManager mgr = new ScriptEngineManager();
//	private static ScriptEngine engine = mgr.getEngineByName("JavaScript");

	public Function(String newName) {
		name = newName;
	}
	
//	public double evaluate(double x) {
//		try {
//			return ((Number) engine.eval(string.replace("x", "(" + Double.toString(x) + ")"))).doubleValue();
//		} catch (ScriptException e) {
//			// TODO: The error message below is displayed once for every horizontal pixel. Fix it.
//			// JOptionPane.showMessageDialog(Main.window, "Something's wrong with the function", "Error", JOptionPane.ERROR_MESSAGE);
////			e.printStackTrace();
//			return 0;
//		}
//	}
	
	public double evaluate(double x) {
		return Evaluator.evaluate(rpn, x);
	}
	
	public void setString(String string) {
		this.string = string;
		this.rpn = Evaluator.simplify(Evaluator.toRPN(this.string));
	}

	@Override
	public String toString() {
		return name;
	}
}
