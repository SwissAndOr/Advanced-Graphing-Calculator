import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Graph {

	public String name = null;
	public Vector<Relation> relations = new Vector<>();

	public double xMin = -5, xMax = 5, yMin = -5, yMax = 5;
	public double gridLineIntervalX = 1, gridLineIntervalY = 1, gridLineIntervalTheta = .392699081698724139499745433568, gridLineIntervalR = 1;
	public boolean axisX = true, axisY = true, cartesian = true, polar = false;

	protected Path currentSaveLocation = null;

	public Graph() {}

	public Graph(String name) {
		this.name = name;
	}

	public Graph(String name, double xMin, double xMax, double yMin, double yMax, double gridLineIntervalX, double gridLineIntervalY, double gridLineIntervalR, double gridLineIntervalTheta, boolean axisX, boolean axisY, boolean cartesian, boolean polar) {
		this.name = name;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.gridLineIntervalX = gridLineIntervalX;
		this.gridLineIntervalY = gridLineIntervalY;
		this.gridLineIntervalR = gridLineIntervalR;
		this.gridLineIntervalTheta = gridLineIntervalTheta;
		this.axisX = axisX;
		this.axisY = axisY;
		this.cartesian = cartesian;
		this.polar = polar;
	}

	public Graph(Map<?, ?> graph) {
		this.name = graph.get("name") instanceof String ? (String) graph.get("name") : "Untitled Graph";
		this.xMin = graph.get("xmin") instanceof Number ? ((Number) graph.get("xmin")).doubleValue() : -5;
		this.xMax = graph.get("xmax") instanceof Number ? ((Number) graph.get("xmax")).doubleValue() : 5;
		this.yMin = graph.get("ymin") instanceof Number ? ((Number) graph.get("ymin")).doubleValue() : -5;
		this.yMax = graph.get("ymax") instanceof Number ? ((Number) graph.get("ymax")).doubleValue() : 5;
		this.gridLineIntervalX = graph.get("gridlineintervalx") instanceof Number ? ((Number) graph.get("gridlineintervalx")).doubleValue() : 1;
		this.gridLineIntervalY = graph.get("gridlineintervaly") instanceof Number ? ((Number) graph.get("gridlineintervaly")).doubleValue() : 1;
		this.gridLineIntervalR = graph.get("gridlineintervalr") instanceof Number ? ((Number) graph.get("gridlineintervalr")).doubleValue() : 1;
		this.gridLineIntervalTheta = graph.get("gridlineintervaltheta") instanceof Number ? ((Number) graph.get("gridlineintervaltheta")).doubleValue() : Math.PI * 2;
		this.axisX = graph.get("axisx") instanceof Boolean ? (Boolean) graph.get("axisx") : true;
		this.axisY = graph.get("axisy") instanceof Boolean ? (Boolean) graph.get("axisy") : true;
		this.cartesian = graph.get("cartesian") instanceof Boolean ? (Boolean) graph.get("cartesian") : true;
		this.polar = graph.get("polar") instanceof Boolean ? (Boolean) graph.get("polar") : false;

		Vector<Relation> relations = new Vector<>();
		try {
			List<?> relationArray = (List<?>) graph.get("relations");
			for (Object obj : relationArray) {
				if (!(obj instanceof Map<?, ?>)) continue;

				relations.addElement(JSON.parseRelation((Map<?, ?>) obj));
			}
		} catch (ClassCastException | NullPointerException e) {}
	}

	/**
	 * <ul>
	 * <li><b><i>save</i></b><br>
	 * <br>
	 * {@code private boolean save()}<br>
	 * <br>
	 * Attempts to save this graph to its currentSaveLocation (which is set when calling {@link Graph#save(Path)}).<br>
	 * @return Whether the graph saved successfully.
	 *         </ul>
	 */
	public boolean save() {
		if (currentSaveLocation == null)
			return false;

		try {
			Files.write(currentSaveLocation, JSON.writeGraph(this).getBytes());
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public void save(Path path, FileNameExtensionFilter format) {
		if (format.getExtensions()[0].equals("graph")) {
			currentSaveLocation = path;
			save();
		} else {
			Dimension dim = GraphTabbedPane.pane.getGraphSize();
			BufferedImage img = new BufferedImage(dim.width, dim.height, format.getDescription().startsWith("Bit") ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);

			GraphTabbedPane.pane.graphPane.paint(img.createGraphics());

			try {
				ImageIO.write(img, format.getExtensions()[0], path.toFile());
			} catch (IOException e) {}
		}
	}

}
