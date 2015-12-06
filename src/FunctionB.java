import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Stack;

@Deprecated
public class FunctionB {

	public FunctionType functionType = FunctionType.POLAR_FUNCTION;
	public String name;
	public String yString = "", xString = "";
	public int[][] points;
	private Stack<Object> yRpn, xRpn;
	public Color color;
	public int thickness = 2;
	public boolean enabled = true;
	
	public double tMin = 0, tMax = 7;

	public boolean invalid = true;
	public BufferedImage image;

	public FunctionB(String newName) {
		color = new Color((int) (Math.random() * 16777216));
		name = newName;
	}

	public double evaluateY(double var) {
		return Evaluator.evaluate(yRpn, var);
	}

	public double evaluateX(double var) {
		return Evaluator.evaluate(xRpn, var);
	}

	public void setYString(String yString) {
		this.yString = yString;

		try {
			this.yRpn = Evaluator.simplify(Evaluator.toRPN(this.yString));
			Evaluator.evaluate(yRpn, 0);
			this.invalid = false;
		} catch (Exception e) {
			this.invalid = true;
		}
	}

	public void setXString(String xString) {
		this.xString = xString;

		try {
			this.xRpn = Evaluator.simplify(Evaluator.toRPN(this.xString));
			Evaluator.evaluate(xRpn, 0);
			this.invalid = false;
		} catch (Exception e) {
			this.invalid = true;
		}
	}

	public void createImage() {
		Dimension dim = GraphTabbedPane.pane.getGraphSize();

		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);

		if (!this.invalid && this.enabled) {
			Graph graph = GraphTabbedPane.pane.getSelectedGraph();

			Graphics2D g = image.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(this.color);
			g.setStroke(new BasicStroke(this.thickness));

			if (this.functionType == FunctionType.CARTESIAN_FUNCTION) {
				double previousValue = dim.height - ((this.evaluateY(graph.xMin) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);
				for (double x = 0; x < dim.width; x++) {
					double currentValue = dim.height - ((this.evaluateY(((x + 1) / dim.width) * (graph.xMax - graph.xMin) + graph.xMin) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);

					// TODO: Make it so that it only graphs functions when absolutely needed, and improve binary search
					if (Double.isFinite(currentValue) && Double.isFinite(previousValue)) {
						g.drawLine((int) x, (int) previousValue, (int) x, (int) currentValue);
					} else if (Double.isFinite(currentValue) ^ Double.isFinite(previousValue)) {
						double currentGuess = 0, lastFiniteGuess = 0;
						boolean finiteOnRight = Double.isFinite(currentValue);

						// TODO: Check if these two values work when xMin > xMax and make them if not
						double min = (x / dim.width) * (graph.xMax - graph.xMin) + graph.xMin;
						double max = ((x + 1) / dim.width) * (graph.xMax - graph.xMin) + graph.xMin;

						for (int ii = 0; ii < 10; ii++) {
							double currentGuessX = (max - min) / 2 + min;
							currentGuess = dim.height - ((this.evaluateY(currentGuessX) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);

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
				}
			} else if (functionType == FunctionType.POLAR_FUNCTION) {
				double step = 0.000244140625 * (tMax - tMin);
				
				double previousValue = evaluateY(tMin), currentValue;
				double previousX = dim.width * ((Math.cos(tMin) * previousValue) - graph.xMin) / (graph.xMax - graph.xMin), currentX;
				double previousY = dim.height - ((Math.sin(tMin) * previousValue - graph.yMin) / (graph.yMax - graph.yMin) * dim.height), currentY;
				for (double t = tMin; t < tMax; t += step) {
					currentValue = evaluateY(t);
                    currentX = dim.width * ((Math.cos(t) * currentValue) - graph.xMin) / (graph.xMax - graph.xMin);
					currentY = dim.height - ((Math.sin(t) * currentValue - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);
					
					// TODO: Make it so that it only graphs functions when absolutely needed, and improve binary search
					if (Double.isFinite(currentValue) && Double.isFinite(previousValue)) {
						g.drawLine((int) previousX, (int) previousY, (int) currentX, (int) currentY);
					} else if (Double.isFinite(currentValue) ^ Double.isFinite(previousValue)) {
						double currentGuess = 0, lastFiniteGuess = 0;
						boolean finiteOnRight = Double.isFinite(currentValue);

						// TODO: Check if these two values work when xMin > xMax and make them if not
						double min = t;
						double max = t + step;

						double currentGuessT, lastFiniteGuessT = 0;
						for (int ii = 0; ii < 10; ii++) {
							currentGuessT = (max - min) / 2 + min;
							currentGuess = evaluateY(currentGuessT);

							if (Double.isFinite(currentGuess)) {
								lastFiniteGuess = currentGuess;
								lastFiniteGuessT = currentGuessT;

								if (finiteOnRight)
									max = currentGuessT;
								else
									min = currentGuessT;
							} else {
								if (finiteOnRight)
									min = currentGuessT;
								else
									max = currentGuessT;
							}
						}

						double lastFiniteX = dim.width * ((lastFiniteGuess * Math.cos(lastFiniteGuessT)) - graph.xMin) / (graph.xMax - graph.xMin);              
						double lastFiniteY = dim.height - ((lastFiniteGuess * Math.sin(lastFiniteGuessT) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);
						
						if (finiteOnRight) {
							g.drawLine((int) lastFiniteX, (int) lastFiniteY, (int) currentX, (int) currentY); 
						} else {
							g.drawLine((int) previousX, (int) previousY, (int) lastFiniteX, (int) lastFiniteY);
						}
					}
					previousValue = currentValue;
					previousX = currentX;
					previousY = currentY;
				}
			}
			
			g.dispose();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public static enum FunctionType {
		CARTESIAN_FUNCTION,
		CARTESIAN_PARAMETRIC,
		POLAR_FUNCTION,
		POLAR_PARAMETRIC,
		SCATTERPLOT;

		public String toString() {
			return this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
		}
	}
}
