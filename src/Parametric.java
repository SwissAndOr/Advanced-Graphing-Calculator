import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class Parametric extends Relation {

	private JCheckBox polarCB = new JCheckBox("Polar  ", false);
	private JLabel xLabel = new JLabel(" x = ");
	private JTextField xTextField = new JTextField(14);
	private JLabel yLabel = new JLabel(" y = ");
	private JTextField yTextField = new JTextField(14);
	private JLabel colorChooserLabel = new JLabel("Color Chooser");
	private JButton colorChooserButton = new JButton();
	private Color selectedColor = Color.BLUE;
	private JLabel thicknessLabel = new JLabel("Line Thickness");
	private JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 0, Main.MAX_THICKNESS, 2);
	private JLabel tMinLabel = new JLabel("t Min");
	private NumberTextField tMinTextField = new NumberTextField(0, 5);
	private JLabel tMaxLabel = new JLabel("t Max");
	private NumberTextField tMaxTextField = new NumberTextField(6.28318530717958623199592693709, 5);

	private double tMin = 0, tMax = 6.28318530717958623199592693709;

	private Stack<Object> xRpn, yRpn;
	private String xEquation, yEquation;

	public Parametric(String name, boolean polar) {
		setName(name);
		setColor(new Color((int) (Math.random() * 16777216)));
		selectedColor = getColor();
		setPolar(polar);

		setPanel(new JPanel());
		getPanel().setPreferredSize(new Dimension(210, 190));
		getPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
		getPanel().add(Main.relationPropertiesType);

		polarCB.setSelected(polar);
		polarCB.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				setPolar(polarCB.isSelected());
			}
		});
		getPanel().add(polarCB);
		getPanel().add(xLabel);
		getPanel().add(xTextField);
		getPanel().add(yLabel);
		getPanel().add(yTextField);
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
		thicknessSlider.setPreferredSize(new Dimension(200, thicknessSlider.getPreferredSize().height));
		getPanel().add(thicknessSlider);

		getPanel().add(tMinLabel);
		getPanel().add(tMinTextField);
		getPanel().add(tMaxLabel);
		getPanel().add(tMaxTextField);
	}

	public Parametric(String name) {
		this(name, false);
	}

	public Parametric(Relation relation) {
		this(relation.getName(), relation.isPolar());
		setColor(relation.getColor());
		selectedColor = getColor();
		colorChooserButton.setBackground(selectedColor);
		setThickness(relation.getThickness());
	}

	public Parametric(Map<?, ?> map) {
		setName(map.get("name") == null ? "Untitled Parametric" : map.get("name").toString());
		setXEquation(map.get("xequation") == null ? "" : map.get("xequation").toString());
		setYEquation(map.get("yequation") == null ? "" : map.get("yequation").toString());
		setPolar(map.get("polar") instanceof Boolean ? (Boolean) map.get("polar") : false);
		polarCB.setSelected(isPolar());
		setColor(new Color(map.get("color") instanceof Number ? ((Number) map.get("color")).intValue() : (int) (Math.random() * 16777216)));
		selectedColor = getColor();
		setThickness(map.get("thickness") instanceof Number ? ((Number) map.get("thickness")).intValue() : 2);
		thicknessSlider.setValue(getThickness());
		this.tMin = map.get("tmin") instanceof Number ? ((Number) map.get("tmin")).doubleValue() : 0;
		tMinTextField.setValue(this.tMin);
		this.tMax = map.get("tmax") instanceof Number ? ((Number) map.get("tmax")).doubleValue() : Math.PI * 2;
		tMaxTextField.setValue(this.tMax);
		setEnabled(map.get("enabled") instanceof Boolean ? (Boolean) map.get("enabled") : true);
	}

	@Override
	public void setPolar(boolean polar) {
		if (getPanel() == null) {
			super.setPolar(polar);
			return;
		}

		if (polar && !isPolar()) {
			xLabel.setText(" \u03B8 = ");
			yLabel.setText(" r = ");
			getPanel().revalidate();
			getPanel().repaint();
			Main.relationList.repaint();
			super.setPolar(polar);
		} else if (!polar && isPolar()) {
			xLabel.setText(" x = ");
			yLabel.setText(" y = ");
			getPanel().revalidate();
			getPanel().repaint();
			Main.relationList.repaint();
			super.setPolar(polar);
		}
	}

	public void setXEquation(String xEquation) {
		this.xEquation = xEquation;
		try {
			xRpn = Evaluator.simplify(Evaluator.toRPN(this.xEquation));
			Evaluator.evaluate(xRpn, 0);
			setInvalid(false);
		} catch (Exception e) {
			setInvalid(true);
		}
	}

	public void setYEquation(String yEquation) {
		this.yEquation = yEquation;
		try {
			yRpn = Evaluator.simplify(Evaluator.toRPN(this.yEquation));
			Evaluator.evaluate(yRpn, 0);
			setInvalid(false);
		} catch (Exception e) {
			setInvalid(true);
		}
	}

	/**
	 * <ul>
	 * <li><b><i>evaluate</i></b><br>
	 * <br>
	 * {@code public double[] evaluate(double t)}<br>
	 * <br>
	 * Evaluates the current function at a given t value.<br>
	 * @param t The t value to evaluate for
	 * @return a <code>double</code> array, containing the x/theta and y/r values respectively.
	 *         </ul>
	 */
	public double[] evaluate(double t) {
		return new double[] {Evaluator.evaluate(xRpn, t), Evaluator.evaluate(yRpn, t)};
	}

	@Override
	public void createImage() {
		Dimension dim = GraphTabbedPane.pane.getGraphSize();

		setImage(new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB));

		if (!isInvalid() && isEnabled()) {
			Graph graph = GraphTabbedPane.pane.getSelectedGraph();
			Graphics2D g = getImage().createGraphics();

			g.setColor(getColor());
			g.setStroke(new BasicStroke(getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			double[] previousValues = evaluate(tMin), currentValues;
			double previousX, previousY, currentX, currentY;
			previousX = dim.width * ((isPolar() ? (Math.cos(previousValues[0]) * previousValues[1]) : previousValues[0]) - graph.xMin) / (graph.xMax - graph.xMin);
			previousY = dim.height - (dim.height * ((isPolar() ? (Math.sin(previousValues[0]) * previousValues[1]) : previousValues[1]) - graph.yMin) / (graph.yMax - graph.yMin));

			final double step = 0.000244140625 * (tMax - tMin);

			for (double t = tMin; t < tMax; t += step) {
				currentValues = evaluate(t);
				currentX = dim.width * ((isPolar() ? (Math.cos(currentValues[0]) * currentValues[1]) : currentValues[0]) - graph.xMin) / (graph.xMax - graph.xMin);
				currentY = dim.height - (dim.height * ((isPolar() ? (Math.sin(currentValues[0]) * currentValues[1]) : currentValues[1]) - graph.yMin) / (graph.yMax - graph.yMin));

				if (Double.isFinite(currentX) && Double.isFinite(currentY) && Double.isFinite(previousX) && Double.isFinite(previousY)) {
					g.drawLine((int) previousX, (int) previousY, (int) currentX, (int) currentY);
				} else {
					// TODO Binary search for point of infinity
				}

				previousValues = new double[] {currentValues[0], currentValues[1]};
				previousX = currentX;
				previousY = currentY;
			}
			g.dispose();
		}
	}

	@Override
	public void applyValues() {
		setXEquation(xTextField.getText());
		setYEquation(yTextField.getText());
		setColor(selectedColor);
		setThickness(thicknessSlider.getValue());
		tMin = Double.parseDouble(tMinTextField.getText());
		tMax = Double.parseDouble(tMaxTextField.getText());
	}

	@Override
	public Icon getIcon() {
		BufferedImage image = new BufferedImage(Main.MAX_THICKNESS * 2 + 2, Main.MAX_THICKNESS * 2 + 2, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getColor());
		g.setStroke(new BasicStroke(getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Polygon(new int[] {Main.MAX_THICKNESS, getThickness() / 2, Main.MAX_THICKNESS * 2 + 1 - getThickness() / 2}, new int[] {Main.MAX_THICKNESS * 2 + 1 - getThickness() / 2, getThickness() / 2, getThickness() / 2}, 3));

		g.dispose();

		return new ImageIcon(image);
	}

	@Override
	public String writeJSON() {
		return String.format("\"type\":\"parametric\",\"polar\":%b,\"name\":\"%s\",\"xEquation\":\"%s\",\"yEquation\":\"%s\",\"color\":%d,\"thickness\":%d,\"tMin\":%f,\"tMax\":%f,\"enabled\":%b", isPolar(), getName().replaceAll("[\"\\\\]", "\\\\$0"), xEquation.replaceAll("[\"\\\\]", "\\\\$0"), yEquation.replaceAll("[\"\\\\]", "\\\\$0"), getColor().getRGB(), getThickness(), tMin, tMax, enabled);
	}

}
