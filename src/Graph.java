import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;

public class Graph {

	public String name = null;
	public Vector<Function> functions = new Vector<>();

	public double xMin = -5, xMax = 5, yMin = -5, yMax = 5;
	public double gridLineIntervalX = 1, gridLineIntervalY = 1;
	public boolean axisX = true, axisY = true;

	private Path currentSaveLocation = null;

	public Graph() {}

	public Graph(String newName, double newXMin, double newXMax, double newYMin, double newYMax, double newGridLineIntervalX, double newGridLineIntervalY, boolean newAxisX, boolean newAxisY) {
		name = newName;
		xMin = newXMin;
		xMax = newXMax;
		yMin = newYMin;
		yMax = newYMax;
		gridLineIntervalX = newGridLineIntervalX;
		gridLineIntervalY = newGridLineIntervalY;
		axisX = newAxisX;
		axisY = newAxisY;
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

	public void save(Path path) {
		currentSaveLocation = path;
		save();
	}

}
