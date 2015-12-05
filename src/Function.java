import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Stack;

public class Function {

	public String name;
	public String string = "";
	private Stack<Object> rpn;
	public Color color = Color.BLUE;
	public int thickness = 2;
	public boolean enabled = true;

	public boolean invalid = true;
	public BufferedImage image;

	public Function(String newName) {
		name = newName;
	}

	public double evaluate(double x) {
		return Evaluator.evaluate(rpn, x);
	}

	public void setString(String string) {
		this.string = string;
		try {
			this.rpn = Evaluator.simplify(Evaluator.toRPN(this.string));
			Evaluator.evaluate(rpn, 0);
			this.invalid = false;
		} catch (Exception e) {
			this.invalid = true;
		}
	}

	public void createImage() {
		Dimension dim = GraphTabbedPane.pane.getGraphSize();

		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);

		if (!this.invalid && this.enabled && this.string != null && !this.string.isEmpty()) {
			Graph graph = GraphTabbedPane.pane.getSelectedGraph();
			
			Graphics2D g = image.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(this.color);
			g.setStroke(new BasicStroke(this.thickness));

			double previousValue = dim.height - ((this.evaluate(graph.xMin) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);
			for (double x = 0; x < dim.width; x++) {
				double currentValue = dim.height - ((this.evaluate(((x + 1) / dim.width) * (graph.xMax - graph.xMin) + graph.xMin) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);

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
						currentGuess = dim.height - ((this.evaluate(currentGuessX) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);

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

			g.dispose();
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
