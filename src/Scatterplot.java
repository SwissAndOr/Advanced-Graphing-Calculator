import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;

public class Scatterplot extends Relation {

	private JCheckBox polarCB = new JCheckBox("Polar", false);
	private JTable pointTable = new JTable();
	private JLabel colorChooserLabel = new JLabel("Color Chooser");
	private JButton colorChooserButton = new JButton();
	private Color selectedColor = Color.BLUE;
	private JLabel thicknessLabel = new JLabel("Line Thickness");
	private JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 0, 15, 2);
	// Point shape?
	
	// TODO Regressions
	private int[][] points;
	public Color color;
	public int thickness = 2;
	
	public Scatterplot(String name, boolean polar) {
		setName(name);
		setPolar(polar);
		
		setPanel(new JPanel());
		getPanel().setPreferredSize(new Dimension(190, 140));
		getPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
		getPanel().add(polarCB);
	}
	
	public Scatterplot(String name) {
		this(name, false);
	}
	
	@Override
	public void createImage() {
		
	}

	@Override
	public void applyValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Icon getIcon() {
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Polygon(new int[] {8}, new int[] {12}, 1));
		g.draw(new Polygon(new int[] {24}, new int[] {20}, 1));

		g.dispose();

		return new ImageIcon(image);
	}

	@Override
	public String writeJSON() {
		return String.format("{\"type\":\"scatterplot\",\"polar\":%b,\"name\":\"%s\",\"points\":{\"x\":%s,\"y\":%s},\"color\":%d,\"thickness\":%d,\"enabled\":%b}", isPolar(), getName().replaceAll("[\"\\\\]", "\\\\$0"), Arrays.toString(points[0]), Arrays.toString(points[1]), color.getRGB(), thickness, enabled);
	}
	
}
