import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Stack;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class Parametric extends Relation {

	public Parametric(String name) {
		setName(name);
	}

	private JTextField xTextField = new JTextField(16);
	private JTextField yTextField = new JTextField(16);
	private JLabel colorChooserLabel = new JLabel("Color Chooser");
	private JButton colorChooserButton = new JButton();
	private Color selectedColor = Color.BLUE;
	private JLabel thicknessLabel = new JLabel("Line Thickness");
	private JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 0, 15, 2);
	private JLabel tMinLabel = new JLabel("t Min");
	private JFormattedTextField tMinTextField = new JFormattedTextField(Main.numbers);
	private JLabel tMaxLabel = new JLabel("t Max");
	private JFormattedTextField tMaxTextField = new JFormattedTextField(Main.numbers);

	private double tMin = 0, tMax = 6.28318530717958623199592693709;

	private Stack<Object> xRpn, yRpn;
	private String xEquation, yEquation;
	public Color color;
	public int thickness = 2;

	@Override
	public void createImage() {

	}

	@Override
	public void applyValues() {

	}

	@Override
	public Icon getIcon() {
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Polygon(new int[] {thickness / 2, 31 - thickness / 2, 31 - thickness / 2}, new int[] {15, thickness / 2, 31 - thickness / 2}, 3));
		
		g.dispose();

		return new ImageIcon(image);
	}

	@Override
	public String writeJSON() {
		return String.format("\"type\":\"parametric\",\"polar\":%b,\"name\":\"%s\",\"xEquation\":\"%s\",\"yEquation\":\"%s\",\"color\":%d,\"thickness\":%d,\"tMin\":%f,\"tMax\":%f,\"enabled\":%b", isPolar(), getName().replaceAll("[\"\\\\]", "\\\\$0"), xEquation.replaceAll("[\"\\\\]", "\\\\$0"), yEquation.replaceAll("[\"\\\\]", "\\\\$0"), color.getRGB(), thickness, tMin, tMax, enabled);
	}

}
