import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class Function extends Relation {
	
	private JCheckBox polarCB = new JCheckBox("Polar", false);
	private boolean xEquals = true;
	private JButton equals = new JButton("x = ");
	private JTextField functionTextField = new JTextField(12);
	private JLabel colorChooserLabel = new JLabel("Color Chooser");
	private JButton colorChooserButton = new JButton();
	private Color selectedColor = Color.BLUE;
	private JLabel thicknessLabel = new JLabel("Line Thickness");
	private JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 0, Main.MAX_THICKNESS, 2);
	private JLabel tMinLabel = new JLabel("\u03B8 Min");
	private JFormattedTextField tMinTextField = new JFormattedTextField(Main.numbers);
	private JLabel tMaxLabel = new JLabel("\u03B8 Max");
	private JFormattedTextField tMaxTextField = new JFormattedTextField(Main.numbers);

	private double tMin = 0, tMax = 2 * Math.PI;

	private Stack<Object> rpn;
	private String function;
	public Color color;
	public int thickness = 2;

	public double getTMin() {
		return tMin;
	}

	public void setTMin(double tMin) {
		this.tMin = tMin;
	}

	public double getTMax() {
		return tMax;
	}

	public void setTMax(double tMax) {
		this.tMax = tMax;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
		try {
			rpn = Evaluator.simplify(Evaluator.toRPN(function));
			Evaluator.evaluate(rpn, 0);
			setInvalid(false);
		} catch (Exception e) {
			setInvalid(true);
		}
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getThickness() {
		return thickness;
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	@Override
	public void setPolar(boolean polar) {
		if (!polar && isPolar()) {
			getPanel().setPreferredSize(new Dimension(190, 150));
			getPanel().remove(tMinLabel);
			getPanel().remove(tMinTextField);
			getPanel().remove(tMaxLabel);
			getPanel().remove(tMaxTextField);
			getPanel().revalidate();
			getPanel().repaint();
			Main.relationList.repaint();
			super.setPolar(polar);
		} else if (polar && !isPolar()) {
			getPanel().setPreferredSize(new Dimension(190, 170));
			getPanel().add(tMinLabel);
			getPanel().add(tMinTextField);
			getPanel().add(tMaxLabel);
			getPanel().add(tMaxTextField);
			getPanel().revalidate();
			getPanel().repaint();
			Main.relationList.repaint();
			super.setPolar(polar);
		}
	}
	
	public Function(String name, boolean polar) {
		setName(name);
		setColor(new Color((int) (Math.random() * 16777216)));
		selectedColor = getColor();
		setPolar(polar);

		tMinTextField.setColumns(4);
		tMinTextField.setText(Double.toString(tMin));
		tMaxTextField.setColumns(4);
		tMaxTextField.setText(Double.toString(tMax));
		
		setPanel(new JPanel());
		getPanel().setPreferredSize(new Dimension(190, 150));
		getPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
		getPanel().add(Main.relationPropertiesType);
		polarCB.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent e) {
				setPolar(polarCB.isSelected());
			}
		});
		getPanel().add(polarCB);
		equals.setMargin(new Insets(equals.getMargin().top - 3, 3, equals.getMargin().bottom - 3, 3));
		getPanel().add(equals);
		getPanel().add(functionTextField);
		getPanel().add(colorChooserLabel);
		colorChooserButton.setPreferredSize(new Dimension(85, 20));
		colorChooserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedColor = JColorChooser.showDialog(null, "Color Chooser", getColor());
				if (selectedColor == null)
					selectedColor = getColor();
				else
					colorChooserButton.setBackground(new Color(selectedColor.getRGB() & 16777215));
			}
		});
		colorChooserButton.setBackground(selectedColor);
		getPanel().add(colorChooserButton);
		getPanel().add(thicknessLabel);
		thicknessSlider.setPaintTicks(true);
		thicknessSlider.setMinorTickSpacing(1);
		thicknessSlider.setPreferredSize(new Dimension(180, thicknessSlider.getPreferredSize().height));
		getPanel().add(thicknessSlider);
	}
	
	public Function(String name) {
		this(name, false);
	}
	
	public double evaluate(double x) {
		return Evaluator.evaluate(rpn, x);
	}

	@Override
	public void createImage() {
		Dimension dim = GraphTabbedPane.pane.getGraphSize();

		setImage(new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB));

		if (!isInvalid() && isEnabled()) {
			Graph graph = GraphTabbedPane.pane.getSelectedGraph();

			Graphics2D g = getImage().createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(this.color);
			g.setStroke(new BasicStroke(this.thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			if (!isPolar()) {
				double previousValue = dim.height - ((evaluate(graph.xMin) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);
				for (double x = 0; x < dim.width; x++) {
					double currentValue = dim.height - ((evaluate(((x + 1) / dim.width) * (graph.xMax - graph.xMin) + graph.xMin) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);

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
							currentGuess = dim.height - ((evaluate(currentGuessX) - graph.yMin) / (graph.yMax - graph.yMin) * dim.height);

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
			} else {
				final double step = 0.000244140625 * (tMax - tMin);

				double previousValue = evaluate(tMin), currentValue;
				double previousX = dim.width * ((Math.cos(tMin) * previousValue) - graph.xMin) / (graph.xMax - graph.xMin), currentX;
				double previousY = dim.height - ((Math.sin(tMin) * previousValue - graph.yMin) / (graph.yMax - graph.yMin) * dim.height), currentY;
				for (double t = tMin; t < tMax; t += step) {
					currentValue = evaluate(t);
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
							currentGuess = evaluate(currentGuessT);

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
	public void applyValues() {
		setFunction(functionTextField.getText());
		color = selectedColor;
		thickness = thicknessSlider.getValue();
		tMin = Double.parseDouble(tMinTextField.getText());
		tMax = Double.parseDouble(tMaxTextField.getText());
	}

	@Override
	public Icon getIcon() {
		BufferedImage image = new BufferedImage(Main.MAX_THICKNESS * 2 + 2, Main.MAX_THICKNESS * 2 + 2, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (isPolar())
			g.drawOval(thickness / 2, thickness / 2, Main.MAX_THICKNESS * 2 + 1 - thickness, Main.MAX_THICKNESS * 2 + 1 - thickness);
		else
			g.drawLine(thickness / 2, thickness / 2, Main.MAX_THICKNESS * 2 + 1 - thickness / 2, Main.MAX_THICKNESS * 2 + 1 - thickness / 2);

		g.dispose();

		return new ImageIcon(image);
	}

	@Override
	public String writeJSON() {
		return String.format("{\"type\":\"function\",\"polar\":%b,\"name\":\"%s\",\"function\":\"%s\",\"color\":%d,\"thickness\":%d,\"tMin\":%f,\"tMax\":%f,\"enabled\":%b}", isPolar(), getName().replaceAll("[\"\\\\]", "\\\\$0"), function.replaceAll("[\"\\\\]", "\\\\$0"), color.getRGB(), thickness, tMin, tMax, enabled);
	}

}
