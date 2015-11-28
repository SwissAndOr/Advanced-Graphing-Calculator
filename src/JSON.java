import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JSON {

	private JSON() {}

/*	public static String writeWithSpace(Object o) {
		String ret = "";
		
		if (o == null) {
			ret += "null";
		} else if (o instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) o;
			
			ret += "{\n";
			
			for (Object key : map.keySet()) {
				ret += String.format("\t\"%s\": %s,\n", key, writeWithSpace(map.get(key)).replaceAll("(?m)^", "\t").trim());
			}
			
			ret += "}";
		} else if (o instanceof Iterable<?>) {
			Iterable<?> i = (Iterable<?>) o;
			Iterator<?> iter = i.iterator();
			
			ret += "[\n";
			
			while (iter.hasNext()) {
				ret += "\t" + writeWithSpace(iter.next()).replaceAll("(?m)^", "\t").trim() + ",\n";
			}
			
			ret += "]";
		} else if (o instanceof BigDecimal) {
			ret += ((BigDecimal) o).toString();
		} else if (o instanceof BigInteger) {
			ret += ((BigInteger) o).toString();
		} else if (o instanceof Number) {
			ret += Main.numbers.format(((Number) o).doubleValue());
		} else if (o instanceof Boolean) {
			ret += String.format("%s", o);
		} else {
			ret += String.format("\"%s\"", o);
		}
		
		return ret;
	}
	*/
	
	/**
	 * <ul>
	 * <li><b><i>write</i></b><br>
	 * <br>
	 * {@code public static String write()}<br>
	 * <br>
	 * @return The current workspace, as a string
	 *         </ul>
	 */
	public static String writeWorkspace() {
		String ret = "{graphs:[";// String.format("{\"selectedGraph\":%d", GraphTabbedPane.pane.GraphTabbedPane.pane.getSelectedIndex()
		
		for (Graph graph : GraphTabbedPane.pane.graphs) {
			ret += writeGraph(graph) + ",";
		}

		return ret.replaceAll("\\,$", "") + "]}";
	}

	/**
	 * <ul>
	 * <li><b><i>write</i></b><br>
	 * <br>
	 * {@code public static String write(Graph graph)}<br>
	 * <br>
	 * @param graph The graph to write
	 * @return The given Graph as a string
	 *         </ul>
	 */
	public static String writeGraph(Graph graph) {
		String ret = String.format("{\"name\":\"%s\",\"xMin\":%f,\"xMax\":%f,\"yMin\":%f,\"yMax\":%f,\"gridLineIntervalX\":%f,\"gridLineIntervalY\":%f,\"axisX\":%b,\"axisY\":%b,\"functions\":[", graph.name.replaceAll("[\"\\\\]", "\\\\$0"), graph.xMin, graph.xMax, graph.yMin, graph.yMax, graph.gridLineIntervalX, graph.gridLineIntervalY, graph.axisX, graph.axisY);

		for (Function function : graph.functions) {
			ret += writeFunction(function) + ",";
		}

		ret.replaceAll("\\,$", "");
		
		return ret.replaceAll("\\,$", "") + "]}";
	}

	/**
	 * <ul>
	 * <li><b><i>write</i></b><br>
	 * <br>
	 * {@code protected static String write(Function function)}<br>
	 * <br>
	 * @param function The function to write
	 * @return The given function as a string
	 *         </ul>
	 */
	protected static String writeFunction(Function function) {
		return String.format("{\"name\":\"%s\",\"string\":\"%s\",\"color\":%d,\"thickness\":%d,\"enabled\":%b}", function.name.replaceAll("[\"\\\\]", "\\\\$0"), function.string.replaceAll("[\"\\\\]", "\\\\$0"), function.color.getRGB(), function.thickness, function.enabled);
	}

	public static boolean parsePane(String string) {
		Map<?, ?> pane = (Map<?, ?>) parse(string).get(0);

		int selectedGraph = pane.get("selectedgraph") instanceof Number ? ((Number) pane.get("selectedgraph")).intValue() : 0;

		Vector<Graph> graphs = new Vector<>();
		try {
			List<?> graphArray = (List<?>) pane.get("graphs");
			for (Object obj : graphArray) {
				if (!(obj instanceof Map<?, ?>)) continue;

				graphs.addElement(parseGraph((Map<?, ?>) obj));
			}
		} catch (ClassCastException | NullPointerException e) {}

//		while (!GraphTabbedPane.graphs.isEmpty())
//			GraphTabbedPane.removeAtIndex(0);

		Main.window.remove(GraphTabbedPane.pane);
		
		GraphTabbedPane.pane = new GraphTabbedPane();
		
		for (Graph graph : graphs) {
			if (graph != null) GraphTabbedPane.pane.addGraph(graph);
		}

		GraphTabbedPane.pane.setSelectedIndex(selectedGraph);
		
		Main.window.add(GraphTabbedPane.pane, BorderLayout.CENTER);
		GraphTabbedPane.pane.revalidate();
		GraphTabbedPane.pane.repaint();
		
		return true;
	}

	public static Graph parseGraph(String string) {
		// If the string parameter is changed to a path
		// String string = Files.readAllBytes(path);

		return parseGraph((Map<?, ?>) parse(string).get(0));
	}

	public static Graph parseGraph(Map<?, ?> graph) {
		String name = graph.get("name") instanceof String ? (String) graph.get("name") : "";

		double xMin = graph.get("xmin") instanceof Number ? ((Number) graph.get("xmin")).doubleValue() : -5;
		double xMax = graph.get("xmax") instanceof Number ? ((Number) graph.get("xmax")).doubleValue() : 5;
		double yMin = graph.get("ymin") instanceof Number ? ((Number) graph.get("ymin")).doubleValue() : -5;
		double yMax = graph.get("ymax") instanceof Number ? ((Number) graph.get("ymax")).doubleValue() : 5;
		double gridLineIntervalX = graph.get("gridlineintervalx") instanceof Number ? ((Number) graph.get("gridlineintervalx")).doubleValue() : 1;
		double gridLineIntervalY = graph.get("gridlineintervaly") instanceof Number ? ((Number) graph.get("gridlineintervaly")).doubleValue() : 1;
		boolean axisX = graph.get("axisx") instanceof Boolean ? (Boolean) graph.get("axisx") : true;
		boolean axisY = graph.get("axisy") instanceof Boolean ? (Boolean) graph.get("axisy") : true;

		Vector<Function> functions = new Vector<>();
		try {
			List<?> functionArray = (List<?>) graph.get("functions");
			for (Object obj : functionArray) {
				if (!(obj instanceof Map<?, ?>)) continue;

				functions.addElement(parseFunction((Map<?, ?>) obj));
			}
		} catch (ClassCastException | NullPointerException e) {}

		Graph ret = new Graph(name, xMin, xMax, yMin, yMax, gridLineIntervalX, gridLineIntervalY, axisX, axisY);
		ret.functions.addAll(functions);
		return ret;
	}

	protected static Function parseFunction(Map<?, ?> map) {
		// TODO Rename from untitiled graph to appropriate name?
		String name = map.get("name") instanceof String ? (String) map.get("name") : "Untitled Graph";
		String string = map.get("string") instanceof String ? (String) map.get("string") : "";
		Color color = map.get("color") instanceof Number ? new Color(((Number) map.get("color")).intValue()) : Color.BLUE;
		int thickness = map.get("thickness") instanceof Number ? ((Number) map.get("thickness")).intValue() : 2;
		boolean enabled = map.get("enabled") instanceof Boolean ? (Boolean) map.get("enabled") : true;

		Function ret = new Function(name);
		ret.color = color;
		ret.enabled = enabled;
		ret.string = string;
		ret.thickness = thickness;
		return ret;
	}

	public static ArrayList<Object> parse(String string) {
		HashMap<String, Object> braceObjs = new HashMap<>();

		String s = string;// .replaceAll("[@\\s]+", "");

		int lastAddress = (int) (System.currentTimeMillis() % 32768);
		
		Pattern quotes = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");//Pattern.compile("\"([^\"]*)\"");// Pattern.compile("\"((?:\\\"|[^\"])*)\"");
		Matcher m = quotes.matcher(s);
		while (m.find()) {
			String v = m.group(1);
			String k = "@" + lastAddress++;//System.identityHashCode(v);

			braceObjs.put(k, v.replaceAll("\\\\(.)", "$1"));
			s = s.replace(m.group(), k);

			m = quotes.matcher(s);
		}

		s = s.replaceAll("\\s+", "");

		Pattern braces = Pattern.compile("\\[[^\\[\\]\\{\\}]*\\]|\\{[^\\[\\]\\{\\}]*?\\}");

		m = braces.matcher(s);
		while (m.find()) {
			String g = m.group();
			if (g.charAt(0) == '[') {
				String[] terms = g.split("[\\[\\]\\,]");
				ArrayList<Object> termList = new ArrayList<>(terms.length);
				for (String term : terms) {
					if (term.isEmpty()) continue;

					if (term.startsWith("\"")) {
						termList.add(term.substring(1, term.length() - 1));
					} else if (term.startsWith("@")) {
						termList.add(braceObjs.get(term));
					} else if (term.equals("true")) {
						termList.add(true);
					} else if (term.equals("false")) {
						termList.add(false);
					} else {
						try {
							termList.add(Double.parseDouble(term));
						} catch (NumberFormatException e) {
							termList.add(null);
						}
					}
				}

//				int h = System.identityHashCode(termList);
				String k = "@" + lastAddress++;

				s = s.replace(g, k);
				if (s.equals(k)) return termList;
				braceObjs.put(k, termList);
			} else {
				String[] terms = g.split("[\\{\\}\\,]");
				Map<String, Object> termList = new HashMap<>();
				for (String term : terms) {
					if (term.isEmpty()) continue;

					String[] kvs = term.split(":");
					String k = (kvs[0].startsWith("@") ? (String) braceObjs.get(kvs[0]) : kvs[0].replace("\"", "")).toLowerCase();
					if (kvs[1].startsWith("\"")) {
						termList.put(k, kvs[1].substring(1, kvs[1].length() - 1));
					} else if (kvs[1].startsWith("@")) {
						termList.put(k, braceObjs.get(kvs[1]));
					} else if (kvs[1].equals("true")) {
						termList.put(k, true);
					} else if (kvs[1].equals("false")) {
						termList.put(k, false);
					} else {
						try {
							termList.put(k, Double.parseDouble(kvs[1]));
						} catch (NumberFormatException e) {
							termList.put(k, null);
						}
					}
				}

//				int h = System.identityHashCode(termList);
				String k = "@" + lastAddress++;

				s = s.replace(g, k);
				if (s.equals(k)) {
					ArrayList<Object> ret = new ArrayList<>(1);
					ret.add(termList);
					return ret;
				}
				braceObjs.put(k, termList);
			}

			m = braces.matcher(s);
		}

		return null;
	}

}
