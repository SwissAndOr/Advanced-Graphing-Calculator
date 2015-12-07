import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

public class Scatterplot extends Relation {

	private JCheckBox polarCB = new JCheckBox("Polar  ", false);
	private JTable pointTable = new JTable(new Object[][] {{"", ""}}, new String[] {"x", "y"});
	private List<Double> xTableData = new ArrayList<Double>();
	private List<Double> yTableData = new ArrayList<Double>();
	private JLabel colorChooserLabel = new JLabel("Color Chooser");
	private JButton colorChooserButton = new JButton();
	private Color selectedColor = Color.BLUE;
	private JLabel thicknessLabel = new JLabel("Line Thickness");
	private JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 0, Main.MAX_THICKNESS, 2);
	// Point shape?

	// TODO Regressions
	private Double[][] points = new Double[2][0];

	public Scatterplot(String name, boolean polar) {
		setColor(new Color((int) (Math.random() * 16777216)));
		selectedColor = getColor();
		setName(name);
		setPolar(polar);

		setPanel(new JPanel());
		getPanel().setPreferredSize(new Dimension(210, 220));
		getPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		polarCB.setSelected(polar);
		getPanel().add(polarCB);
		polarCB.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				setPolar(polarCB.isSelected());
			}
		});

		pointTable.setModel(new AbstractTableModel() {

			private static final long serialVersionUID = -3606060112359139704L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (rowIndex == xTableData.size())
					return "";
				else if (columnIndex == 0)
					return xTableData.get(rowIndex);
				else
					return yTableData.get(rowIndex);
			}

			@Override
			public int getRowCount() {
				return xTableData.size() + 1;
			}

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				List<Double> colData = columnIndex == 0 ? xTableData : yTableData;

				if (aValue instanceof Number) {
					if (getValueAt(rowIndex, columnIndex) instanceof String) {
						xTableData.add(0.0);
						yTableData.add(0.0);
					}
					colData.set(rowIndex, ((Number) aValue).doubleValue());
				} else if (aValue == null || aValue instanceof String) {
					String str = (String) aValue;
					if (str != null && !str.isEmpty()) {
						try {
							Stack<Object> rpn = Evaluator.simplify(Evaluator.toRPN(str));
							if (rpn.size() > 1 || !(rpn.peek() instanceof Number)) return;

							if (getValueAt(rowIndex, columnIndex) instanceof String) {
								xTableData.add(0.0);
								yTableData.add(0.0);
							}
							colData.set(rowIndex, ((Number) rpn.peek()).doubleValue());
						} catch (Exception e) {}
					}
				}

				pointTable.revalidate();
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		});
		pointTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = -7633725032237649623L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (comp instanceof JLabel) {
					JLabel label = (JLabel) comp;

					try {
						double d = Double.parseDouble(label.getText());
						label.setText(Main.numbers.format(d));
						label.setToolTipText(Main.numbers.format(d));
					} catch (NumberFormatException e) {}
				}
				return comp;
			}

		});
		pointTable.getTableHeader().setReorderingAllowed(false);
		pointTable.getTableHeader().setResizingAllowed(false);
		JScrollPane scrollPane = new JScrollPane(pointTable);
		scrollPane.setPreferredSize(new Dimension(200, 100));
		getPanel().add(scrollPane);

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
	}

	public Scatterplot(String name) {
		this(name, false);
	}
	
	public Scatterplot(Relation relation) {
		this(relation.getName(), relation.isPolar());
		setColor(relation.getColor());
		selectedColor = getColor();
		colorChooserButton.setBackground(selectedColor);
		setThickness(relation.getThickness());
	}

	@Override
	public void setPolar(boolean polar) {
		if (getPanel() == null) {
			super.setPolar(polar);
			return;
		}
		
		if (!polar && isPolar()) {
			TableColumnModel tcm = pointTable.getTableHeader().getColumnModel();
			tcm.getColumn(0).setHeaderValue("x");
			tcm.getColumn(1).setHeaderValue("y");
			getPanel().revalidate();
			getPanel().repaint();
			Main.relationList.repaint();
			super.setPolar(polar);
		} else if (polar && !isPolar()) {
			TableColumnModel tcm = pointTable.getTableHeader().getColumnModel();
			tcm.getColumn(0).setHeaderValue("\u03B8");
			tcm.getColumn(1).setHeaderValue("r");
			getPanel().revalidate();
			getPanel().repaint();
			Main.relationList.repaint();
			super.setPolar(polar);
		}
	}

	@Override
	public boolean isInvalid() {
		return false;
	}

	@Override
	public void createImage() {
		Dimension dim = GraphTabbedPane.pane.getGraphSize();

		setImage(new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB));

		if (isEnabled()) {
			Graph graph = GraphTabbedPane.pane.getSelectedGraph();

			Graphics2D g = getImage().createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(this.getColor());
			g.setStroke(new BasicStroke(this.getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			
			for (int i = 0; i < points[0].length; i++) {
				double x = dim.width * ((isPolar() ? (Math.cos(points[0][i]) * points[1][i]) : points[0][i]) - graph.xMin) / (graph.xMax - graph.xMin);
				double y = dim.height - (dim.height * ((isPolar() ? (Math.sin(points[0][i]) * points[1][i]) : points[1][i]) - graph.yMin) / (graph.yMax - graph.yMin));
				
				g.draw(new Polygon(new int[] {(int) x}, new int[] {(int) y}, 1));
			}
			
			g.dispose();
		}
	}

	@Override
	public void applyValues() {
		points = new Double[2][xTableData.size()];
		xTableData.toArray(points[0]);
		yTableData.toArray(points[1]);
		setColor(selectedColor);
		setThickness(thicknessSlider.getValue());
	}

	@Override
	public Icon getIcon() {
		BufferedImage image = new BufferedImage(Main.MAX_THICKNESS * 2 + 2, Main.MAX_THICKNESS * 2 + 2, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getColor());
		g.setStroke(new BasicStroke(getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Polygon(new int[] {Main.MAX_THICKNESS / 2}, new int[] {5 * Main.MAX_THICKNESS / 4}, 1));
		g.draw(new Polygon(new int[] {3 * Main.MAX_THICKNESS / 2}, new int[] {3 * Main.MAX_THICKNESS / 4}, 1));

		g.dispose();

		return new ImageIcon(image);
	}

	@Override
	public String writeJSON() {
		return String.format("{\"type\":\"scatterplot\",\"polar\":%b,\"name\":\"%s\",\"points\":{\"x\":%s,\"y\":%s},\"color\":%d,\"thickness\":%d,\"enabled\":%b}", isPolar(), getName().replaceAll("[\"\\\\]", "\\\\$0"), Arrays.toString(points[0]), Arrays.toString(points[1]), getColor().getRGB(), getThickness(), enabled);
	}

}
