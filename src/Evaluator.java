import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Evaluator {

	private Evaluator() {}

	private static final Pattern numbers = Pattern.compile("^([\\+\\-]?)([0-9\\.]+(?:E[\\-\\+]?[0-9]+)?|[xte]|pi)");
	private static final Pattern ops = Pattern.compile("^(?:\\^|~|\\*|\\/|%|\\+|-|>>|<<|>>>|&&|\\^\\^|\\|\\|)");
	private static final Pattern functions = Pattern.compile("^(?:abs|acos|asin|atan|ceil|cos|cosh|cot|csc|deg|floor|fpart|ln|log|max|min|rad|round|rt|sec|sin|sinh|sqrt|tan|tanh)");

	private static final Map<String, Integer> opPrec = createOpPrec();

	private static final List<String> arg2 = Arrays.asList("&&", "*", "-", "<<", ">>", ">>>", "/", "log", "max", "min", "%", "+", "rt", "^", "^^", "||");

	public static Stack<Object> toRPN(String infix) {
		Stack<Object> out = new Stack<>();
		Stack<String> oper = new Stack<>();

		String expr = infix.replaceAll("\\s", "");

		boolean lastFunction = true;

		while (!expr.isEmpty()) {
			Matcher fm = functions.matcher(expr);

			if (fm.find()) {
				String func = fm.group();
				oper.push(func);

				expr = expr.replaceFirst("\\Q" + fm.group() + "\\E", "");

				lastFunction = true;
				continue;
			}

			if (expr.startsWith(",")) {
				while (!oper.peek().equals("(")) {
					out.push(oper.pop());
				}

				expr = expr.substring(1);

				lastFunction = true;
				continue;
			}

			Matcher om = ops.matcher(expr);
			Matcher nm = numbers.matcher(expr);

			if (om.find()) {
				String op = om.group();

				if (lastFunction && nm.find()) {
					out.push(getValueForString(nm.group()));
					expr = expr.replaceFirst("\\Q" + nm.group() + "\\E", "");

					lastFunction = false;
					continue;
				} else if (lastFunction && (op.equals("-") || op.equals("+"))) {
					expr = "0" + expr;
					continue;
				}

				int prec = opPrec.get(op);

				if (op.equals("~") || op.equals("^")) prec -= 1;

				while (!oper.isEmpty() && opPrec.get(oper.peek()) <= prec) {
					out.push(oper.pop());
				}

				oper.add(op);
				expr = expr.replaceFirst("\\Q" + op + "\\E", "");

				lastFunction = true;
				continue;
			}

			if (nm.find()) {
				if (!lastFunction) oper.push("*");
				out.push(getValueForString(nm.group()));
				expr = expr.replaceFirst("\\Q" + nm.group() + "\\E", "");

				lastFunction = false;
				continue;
			}

			if (expr.startsWith("(")) {
				if (!lastFunction) oper.push("*");
				oper.push("(");
				expr = expr.substring(1);

				lastFunction = true;
				continue;
			}

			if (expr.startsWith(")")) {
				try {
					while (!oper.peek().equals("(")) {
						out.push(oper.pop());
					}
				} catch (EmptyStackException e) {
					throw new IllegalArgumentException("Unbalanced Parentheses");
				}

				oper.pop();

				if (!oper.isEmpty()) {
					Matcher fm2 = functions.matcher(oper.peek());

					if (fm2.find()) out.push(oper.pop());
				}

				expr = expr.substring(1);

				lastFunction = false;
				continue;
			}
			
			throw new IllegalArgumentException("Unknown token in expression (" + infix + ")");
		}

		while (!oper.isEmpty()) {
			out.push(oper.lastElement());
			oper.remove(oper.size() - 1);
		}

		return out;
	}

	protected static Stack<Object> simplify(Stack<Object> rpn) {
		Stack<Object> ret = new Stack<>(), vals = new Stack<>();
		vals.addAll(rpn);

		while (!vals.isEmpty()) {
			Object token = vals.remove(0);

			if (token instanceof Number || (token instanceof CharSequence && "xt".contains((CharSequence) token))) {
				ret.push(token);
			} else if (token instanceof String) {
				int numArgs = arg2.contains(token) ? 2 : 1;

				boolean canEval = true;

				for (int i = 1; i <= numArgs; i++) {
					if (!(ret.elementAt(ret.size() - i) instanceof Number)) {
						canEval = false;
						break;
					}
				}

				if (!canEval) {
					ret.push(token);
					continue;
				}

				Double d;
				if (numArgs == 2) {
					d = valueOf((String) token, ((Number) ret.pop()).doubleValue(), ((Number) ret.pop()).doubleValue());
				} else {
					d = valueOf((String) token, ((Number) ret.pop()).doubleValue());
				}

				if (Double.isFinite(d)) {
					ret.push(d);
				} else {
					Stack<Object> NaNRet = new Stack<>();
					NaNRet.push(Double.NaN);
					return NaNRet;
				}
			}
		}

		return ret;
	}

	public static double valueOf(String func, double... vals) {
		try {
			switch (func) {
				case "abs":
					return Math.abs(vals[0]);
				case "acos":
					return Math.acos(vals[0]);
				case "asin":
					return Math.asin(vals[0]);
				case "atan":
					return Math.atan(vals[0]);
				case "ceil":
					return Math.ceil(vals[0]);
				case "cos":
					return Math.cos(vals[0]);
				case "cosh":
					return Math.cosh(vals[0]);
				case "cot":
					return 1.0 / Math.tan(vals[0]);
				case "csc":
					return 1.0 / Math.sin(vals[0]);
				case "deg":
					return Math.toDegrees(vals[0]);
				case "floor":
					return Math.floor(vals[0]);
				case "fpart":
					return vals[0] - Math.floor(vals[0]);
				case "ln":
					return Math.log(vals[0]);
				case "rad":
					return Math.toRadians(vals[0]);
				case "round":
					return Math.round(vals[0]);
				case "sec":
					return 1.0 / Math.cos(vals[0]);
				case "sin":
					return Math.sin(vals[0]);
				case "sinh":
					return Math.sinh(vals[0]);
				case "sqrt":
					return Math.sqrt(vals[0]);
				case "tan":
					return Math.tan(vals[0]);
				case "tanh":
					return Math.tanh(vals[0]);
				case "~":
					return ~((long) vals[0]);
				case "&&":
					return (long) vals[1] & (long) vals[0];
				case "*":
					return vals[1] * vals[0];
				case "-":
					return vals[1] - vals[0];
				case "<<":
					return (long) vals[1] << (long) vals[0];
				case ">>":
					return (long) vals[1] >> (long) vals[0];
				case ">>>":
					return (long) vals[1] >>> (long) vals[0];
				case "/":
					return vals[1] / vals[0];
				case "log":
					return Math.log(vals[1]) / Math.log(vals[0]);
				case "max":
					return Math.max(vals[1], vals[0]);
				case "min":
					return Math.min(vals[1], vals[0]);
				case "%":
					return vals[1] % vals[0];
				case "+":
					return vals[1] + vals[0];
				case "rt":
					return Math.pow(vals[1], 1.0 / vals[0]);
				case "^":
					return Math.pow(vals[1], vals[0]);
				case "^^":
					return (long) vals[1] ^ (long) vals[0];
				case "||":
					return (long) vals[1] | (long) vals[0];
				default:
					return Double.NaN;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Too few arguments to function " + func);
		}
	}

	private static Object getValueForString(String string) {
		if (string == null || string.isEmpty()) return 1.0;

		String sign = string.charAt(0) == '-' ? "-" : "";
		String str = string.replaceAll("^[\\+\\-]?", "");

		if ("xt".indexOf(str) != -1) {
			return sign + str;
		} else {
			int sgn = sign.isEmpty() ? 1 : -1;

			if (str.equals("pi")) {
				return sgn * Math.PI;
			} else if (str.equals("e")) {
				return sgn * Math.E;
			} else {
				try {
					return Double.parseDouble(string);
				} catch (NumberFormatException e) {
					return sgn;
				}
			}
		}
	}

	private static final Map<String, Integer> createOpPrec() {
		HashMap<String, Integer> ret = new HashMap<>();

		ret.put("(", 32767);
		ret.put("^", 1);
		ret.put("~", 2);
		ret.put("*", 3);
		ret.put("/", 3);
		ret.put("%", 3);
		ret.put("+", 4);
		ret.put("-", 4);
		ret.put(">>", 5);
		ret.put("<<", 5);
		ret.put(">>>", 5);
		ret.put("&&", 6);
		ret.put("^^", 7);
		ret.put("||", 8);

		return ret;
	}

	public static double evaluate(Stack<Object> rpn, double var) {
		Stack<Double> ret = new Stack<>();
		Stack<Object> vals = new Stack<>();
		vals.addAll(rpn);

		while (!vals.isEmpty()) {
			Object token = vals.remove(0);

			if (token instanceof Number) {
				ret.push(((Number) token).doubleValue());
			} else if (token instanceof String) {
				String tk = (String) token;

				if ("xt".contains(tk)) {
					ret.push(var);

					continue;
				}

				int numArgs = arg2.contains(token) ? 2 : 1;

				Double d;
				if (numArgs == 2) {
					d = valueOf(tk, ((Number) ret.pop()).doubleValue(), ((Number) ret.pop()).doubleValue());
				} else {
					d = valueOf(tk, ((Number) ret.pop()).doubleValue());
				}

				if (Double.isFinite(d)) {
					ret.push(d);
				} else {
					return Double.NaN;
				}
			}
		}

		if (ret.size() > 1) throw new IllegalArgumentException("Could not parse function");

		return ret.peek();
	}

}
