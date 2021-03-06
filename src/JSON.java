import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JSON {

	private JSON() {}

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
		String ret = "{graphs:[";// String.format("{\"selectedGraph\":%d",
									// GraphTabbedPane.pane.GraphTabbedPane.pane.getSelectedIndex()

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
		String ret = String.format("{\"name\":\"%s\",\"xMin\":%f,\"xMax\":%f,\"yMin\":%f,\"yMax\":%f,\"gridLineIntervalX\":%f,\"gridLineIntervalY\":%f,\"gridLineIntervalR\":%f,\"gridLineIntervalTheta\":%f,\"axisX\":%b,\"axisY\":%b,\"cartesian\":%b,\"polar\":%b,\"functions\":[",
				graph.name.replaceAll("[\"\\\\]", "\\\\$0"), graph.xMin, graph.xMax, graph.yMin, graph.yMax, graph.gridLineIntervalX, graph.gridLineIntervalY, graph.gridLineIntervalR, graph.gridLineIntervalTheta, graph.axisX, graph.axisY, graph.cartesian, graph.polar);

		for (Relation relation : graph.relations) {
			ret += relation.writeJSON() + ",";
		}

		ret.replaceAll("\\,$", "");

		return ret.replaceAll("\\,$", "") + "]}";
	}

	public static GraphTabbedPane parsePane(String string) {
		Map<?, ?> pane = (Map<?, ?>) parse(string).get(0);
		GraphTabbedPane GTPane = new GraphTabbedPane();

		int selectedGraph = pane.get("selectedgraph") instanceof Number ? ((Number) pane.get("selectedgraph")).intValue() : 0;

		Vector<Graph> graphs = new Vector<>();
		try {
			List<?> graphArray = (List<?>) pane.get("graphs");
			for (Object obj : graphArray) {
				if (!(obj instanceof Map<?, ?>)) continue;

				graphs.addElement(parseGraph((Map<?, ?>) obj));
			}
		} catch (ClassCastException | NullPointerException e) {}

		for (Graph graph : graphs) {
			if (graph != null) GTPane.addGraph(graph);
		}

		GTPane.setSelectedIndex(selectedGraph);

		return GTPane;
	}

	public static Graph parseGraph(String string) {
		return parseGraph((Map<?, ?>) parse(string).get(0));
	}

	public static Graph parseGraph(Map<?, ?> graph) {
		return new Graph(graph);
	}

	protected static Relation parseRelation(Map<?, ?> map) {
		String typeName = (String) map.get("type");

		if (typeName == null) return null;

		switch (typeName) {
			case "function":
				return new Function(map);
			case "parametric":
				return new Parametric(map);
			case "scatterplot":
				return new Scatterplot(map);
			default:
				return null;
		}
	}

	public static ArrayList<Object> parse(String string) {
		HashMap<String, Object> braceObjs = new HashMap<>();

		String s = string;

		int lastAddress = (int) (System.currentTimeMillis() % 32768);

		Pattern quotes = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
		Matcher m = quotes.matcher(s);
		while (m.find()) {
			String v = m.group(1);
			String k = "@" + lastAddress++;

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
