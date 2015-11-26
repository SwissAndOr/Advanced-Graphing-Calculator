import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Vector;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class Graph extends JComponent {
	public String name = null;
	public Vector<Function> functions = new Vector<>();
	public double xMin = -5, xMax = 5, yMin = -5, yMax = 5;
	public double gridLineIntervalX = 1, gridLineIntervalY = 1;
	public boolean axisX = true, axisY = true;

    public Graph() {}
    public Graph(String newName, double newXMin, double newXMax, double newYMin, double newYMax, double newGridLineIntervalX, double newGridLineIntervalY, boolean newAxisX, boolean newAxisY) {
    	name = newName;
    	xMin = newXMin; xMax = newXMax; yMin = newYMin; yMax = newYMax;
    	gridLineIntervalX = newGridLineIntervalX; gridLineIntervalY = newGridLineIntervalY;
    	axisX = newAxisX; axisY = newAxisY;
    }
    
	public void paint(Graphics g) {
		if (!(g instanceof Graphics2D)) return;
		Graphics2D gg = (Graphics2D) g;
		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		gg.setColor(Color.WHITE);
		gg.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		int width = this.getWidth();
		int height = this.getHeight();

		gg.setColor(Color.LIGHT_GRAY);
		if (gridLineIntervalX > 0) {
			for (double x = gridLineIntervalX * Math.floor((xMax - xMin > 0 ? xMin : xMax) / gridLineIntervalX); x < width; x += gridLineIntervalX) {
				gg.drawLine((int) ((x - xMin) / (xMax - xMin) * width), 0, (int) ((x - xMin) / (xMax - xMin) * width), height);
			}
		}
		if (gridLineIntervalY > 0) {
			for (double y = gridLineIntervalY * Math.floor((yMax - yMin > 0 ? yMin : yMax) / gridLineIntervalY); y < height; y += gridLineIntervalY) {
				gg.drawLine(0, (int) (height - (y - yMin) / (yMax - yMin) * height), width, (int) (height - (y - yMin) / (yMax - yMin) * height));
			}
		}
		
		gg.setColor(Color.BLACK);
		int zeroX = (int) ((double) -xMin / (xMax - xMin) * width );
		int zeroY = (int) (height - ((double) -yMin / (yMax - yMin) * height));
		if (axisX)
			gg.fillRect(0, zeroY - 1, width, 3);  // Draw horizontal 0 line
		if (axisY)
			gg.fillRect(zeroX - 1, 0, 3, height); // Draw vertical 0 line
		
		for (int i = functions.size() - 1; i >= 0; i--) {
			if (functions.get(i).string != null && !functions.get(i).string.isEmpty()) {
				gg.setColor(functions.get(i).color);
				gg.setStroke(new BasicStroke(functions.get(i).thickness));
				double previousValue = height - ((functions.get(i).evaluate(xMin) - yMin) / (yMax - yMin) * height);
				for (double x = 0; x < width; x++) {
					// height - ((n - yMin) / (yMax - yMin) * height) (absolute to relative)
					// (n / width) * (xMax - xMin) + xMin             (relative to absolute)
					
					double currentValue = height - ((functions.get(i).evaluate(((x + 1) / width) * (xMax - xMin) + xMin) - yMin) / (yMax - yMin) * height);
					// TODO: Make it so that it only graphs functions when absolutely needed, and improve binary search
					if (Double.isFinite(currentValue) && Double.isFinite(previousValue)) {
						gg.drawLine((int) x, (int) previousValue,(int) x, (int) currentValue);
					} else if (Double.isFinite(currentValue) ^ Double.isFinite(previousValue)) {
						double currentGuess = 0, lastFiniteGuess = 0;
						boolean finiteOnRight = Double.isFinite(currentValue);
						double min = (x / width) * (xMax - xMin) + xMin;       // TODO: Check if these two values work when xMin > xMax and make them if not
						double max = ((x + 1) / width) * (xMax - xMin) + xMin;
						
						for (int ii = 0; ii < 10; ii++) {
							double currentGuessX = (max - min) / 2 + min;
							currentGuess = height - ((functions.get(i).evaluate(currentGuessX) - yMin) / (yMax - yMin) * height);
							System.out.println(currentGuessX);
							
							if (Double.isFinite(currentGuess)) {
								lastFiniteGuess = currentGuess;
								if (finiteOnRight)
									max = currentGuessX;
								else
									min = currentGuessX;
							} else {
								if (finiteOnRight)
									min = currentGuessX;
								else
									max = currentGuessX;
							}
						}
						
						
						gg.drawLine((int) x, (int) (finiteOnRight ? lastFiniteGuess : previousValue), (int) x, (int) (finiteOnRight ? currentValue : lastFiniteGuess));
					}
					previousValue = currentValue;
				}
			}
		}
	}
}
