import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingWorker;

public class Graph {

	public String name = null;
	public Vector<Function> functions = new Vector<>();

	private boolean imageValid = false;
	private BufferedImage image;

	private Thread thread;
	private boolean cancelling;

	private int currentProgress;
	
	private final Runnable imageCalculator = new Runnable() {

		@Override
		public void run() {
			BarPainter bp = new BarPainter();
			
			Dimension dim = GraphTabbedPane.getGraphSize();
			BufferedImage newImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);

			List<Function> validFunctions = new ArrayList<>();

			int progressMax = 0;
			
			for (int i = functions.size() - 1; i >= 0; i--) {
				if (functions.get(i).string != null && !functions.get(i).string.isEmpty()) {
					validFunctions.add(functions.get(i));
					progressMax += dim.width;
				}
			}
			
			Main.progressBar.setMaximum(progressMax);

			Graphics2D g = newImage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			currentProgress = 0;
			
			for (Function func : validFunctions) {
				if (cancelling) break;

				g.setColor(func.color);
				g.setStroke(new BasicStroke(func.thickness));

				double previousValue = dim.height - ((func.evaluate(GraphTabbedPane.xMin) - GraphTabbedPane.yMin) / (GraphTabbedPane.yMax - GraphTabbedPane.yMin) * dim.height);
				for (double x = 0; x < dim.width || cancelling; x++) {
					// height - ((n - yMin) / (yMax - yMin) * height) (absolute to relative)
					// (n / width) * (xMax - xMin) + xMin (relative to absolute)

					double currentValue = dim.height - ((func.evaluate(((x + 1) / dim.width) * (GraphTabbedPane.xMax - GraphTabbedPane.xMin) + GraphTabbedPane.xMin) - GraphTabbedPane.yMin) / (GraphTabbedPane.yMax - GraphTabbedPane.yMin) * dim.height);

					// TODO: Make it so that it only graphs functions when absolutely needed, and improve binary search
					if (Double.isFinite(currentValue) && Double.isFinite(previousValue)) {
						g.drawLine((int) x, (int) previousValue, (int) x, (int) currentValue);
					} else if (Double.isFinite(currentValue) ^ Double.isFinite(previousValue)) {
						double currentGuess = 0, lastFiniteGuess = 0;
						boolean finiteOnRight = Double.isFinite(currentValue);

						// TODO: Check if these two values work when xMin > xMax and make them if not
						double min = (x / dim.width) * (GraphTabbedPane.xMax - GraphTabbedPane.xMin) + GraphTabbedPane.xMin;
						double max = ((x + 1) / dim.width) * (GraphTabbedPane.xMax - GraphTabbedPane.xMin) + GraphTabbedPane.xMin;

						for (int ii = 0; ii < 10 || cancelling; ii++) {
							double currentGuessX = (max - min) / 2 + min;
							currentGuess = dim.height - ((func.evaluate(currentGuessX) - GraphTabbedPane.yMin) / (GraphTabbedPane.yMax - GraphTabbedPane.yMin) * dim.height);

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

						g.drawLine((int) x, (int) (finiteOnRight ? lastFiniteGuess : previousValue), (int) x, (int) (finiteOnRight ? currentValue : lastFiniteGuess));
					}
					previousValue = currentValue;
					
					currentProgress++;
					try {
						bp.doInBackground();
					} catch (Exception e) {}
				}
			}

			g.dispose();

			if (!cancelling) {
				image = newImage;
				imageValid = true;
				GraphTabbedPane.pane.repaint();
			}
		}
	};

	public Graph() {}

	public Graph(String name) {
		this.name = name;
	}

	public void invalidate() {
		imageValid = false;
	}

	public void calculateImage() {
		cancelCalculation();

		thread = new Thread(imageCalculator);
		thread.start();
	}

	public void cancelCalculation() {
		if (thread != null && thread.isAlive()) {
			cancelling = true;
			try {
				thread.join();
			} catch (InterruptedException e) {}
		}
		cancelling = false;
	}

	public boolean isImageValid() {
		return imageValid;
	}

	public BufferedImage getImage() {
		return image;
	}
	
	private class BarPainter extends SwingWorker<Object, Object> {

		@Override
		protected Object doInBackground() throws Exception {
			Main.progressBar.setValue(currentProgress);
			Main.progressBar.repaint();

			return null;
		}

	}

}
