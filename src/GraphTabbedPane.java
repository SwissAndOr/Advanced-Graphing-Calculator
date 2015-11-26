import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class GraphTabbedPane extends JPanel {

	public static double xMin = -5, xMax = 5, yMin = -5, yMax = 5;
	public static double gridLineIntervalX = 1, gridLineIntervalY = 1;
	public static boolean axisX = true, axisY = true;

	public static Vector<Graph> graphs = new Vector<>();
	private static int selectedGraph;

	private static JPanel tabPanel = new JPanel(new GridBagLayout());
	private static JPanel tabButtonPanel = new JPanel(new GridBagLayout());
	private static JScrollPane buttonScrollPane = new JScrollPane(tabButtonPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

	private static Vector<JPanel> tabButtons = new Vector<>();

	private static JPopupMenu tabPopup = new JPopupMenu();
	private static JMenuItem rename = new JMenuItem("Rename");

	private static JButton addButton = new JButton("+");

	public static GraphPane graphPane = new GraphPane();

	private static final TabHandler handle = new TabHandler();

	private static JPanel space = new JPanel();

	public static GraphTabbedPane pane = tabPane();

	private static GraphTabbedPane tabPane() {
		GraphTabbedPane pane = new GraphTabbedPane();

		pane.setLayout(new BorderLayout());

		tabPopup.add(rename);
		rename.addActionListener(handle);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 1;

		space.setPreferredSize(new Dimension(0, 20));
		tabButtonPanel.add(space, c);

		c.weightx = 1;
		// This can be set to 0, 0 to remove the scroll bar entirely and let the user scroll with the mouse wheel only 
		buttonScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(buttonScrollPane.getPreferredSize().width, 5));
		buttonScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		tabPanel.add(buttonScrollPane, c);

		addButton.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
		addButton.setMargin(new Insets(-10, 6, -10, 6));
		addButton.setBackground(new Color(238, 238, 238));
		addButton.setFocusPainted(false);
		addButton.addActionListener(new ActionListener() {

			private Pattern p = Pattern.compile("Untitled ([0-9]+)");

			@Override
			public void actionPerformed(ActionEvent e) {
				int high = 0;

				for (JPanel panel : tabButtons) {
					Matcher m = p.matcher(((JButton) panel.getComponent(0)).getText());
					if (m.find()) {
						try {
							int i = Integer.parseInt(m.group(1));
							if (i > high) high = i;
						} catch (NumberFormatException exception) {}
					}
				}

				addGraph(new Graph("Untitled " + (high + 1)));
			}

		});
		c.gridx = 1;
		c.weightx = 0;
		tabPanel.add(addButton, c);

		pane.add(tabPanel, BorderLayout.PAGE_START);

		pane.add(graphPane);

		pane.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				for (Graph g : graphs) {
					g.invalidate();
				}
			}
		});

		return pane;
	}

	private GraphTabbedPane() {}

	/**
	 * <ul>
	 * <li><b><i>addGraph</i></b><br>
	 * <br>
	 * {@code public static void addGraph(Graph graph)}<br>
	 * <br>
	 * Adds a new graph to this {@link GraphTabbedPane}. This method should be used for most purposes, instead of directly using
	 * {@link graphs}{@code .addElement()}, because this revalidates the pane as well.<br>
	 * @param graph The graph to add
	 *        </ul>
	 */
	public static void addGraph(Graph graph) {
		graphs.addElement(graph);

		GridBagConstraints c = new GridBagConstraints();

//		for (JPanel panel : tabButtons) {
//			panel.setBackground(new Color(238, 238, 238));
//		}

		JPanel panel = new JPanel(new GridBagLayout());
//		panel.setBackground(new Color(200, 221, 242));
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

		JButton button = new JButton(graph.name);
		button.setOpaque(false);
		button.setBackground(new Color(0, 0, 0, 0));
//		button.setBackground(new Color(238, 238, 238));
//		button.setBackground(new Color(200, 221, 242));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setMargin(new Insets(0, 5, 0, 0));
		button.addActionListener(handle);
		button.setComponentPopupMenu(tabPopup);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		panel.add(button, c);

		JButton del = new JButton("\u00D7");
		del.setBorderPainted(false);
		del.setFocusPainted(false);
		del.setOpaque(false);
		del.setBackground(new Color(0, 0, 0, 0));
		del.setMargin(new Insets(0, 0, 0, 2));
		del.addActionListener(handle);

		c.gridx = 1;
		c.weightx = 0;
		panel.add(del, c);

		c.weightx = 0.5;
		c.gridx = graphs.size();
		tabButtonPanel.add(panel, c);
		tabButtons.addElement(panel);

		setSelectedIndex(graphs.size() - 1);

//		System.out.println(tabButtonPanel.getPreferredSize().width);
//		tabButtonPanel.setPreferredSize(new Dimension(tabButtonPanel.getPreferredSize().width + button.getPreferredSize().width, tabButtonPanel.getPreferredSize().height));

		buttonScrollPane.validate();
		buttonScrollPane.getHorizontalScrollBar().setValue(buttonScrollPane.getHorizontalScrollBar().getMaximum());
		buttonScrollPane.repaint();
	}

	public static void removeAtIndex(int index) {
		graphs.remove(index);
		tabButtons.remove(index);

		if (selectedGraph >= tabButtons.size()) selectedGraph--;

		tabButtonPanel.removeAll();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 1;

		tabButtonPanel.add(space, c);

		c.weightx = 0.5;
		c.gridx = 1;

		for (int i = 0; i < tabButtons.size(); i++) {
			tabButtonPanel.add(tabButtons.get(i), c);
			tabButtons.get(i).setBackground(selectedGraph != i ? new Color(238, 238, 238) : new Color(200, 221, 242));
			c.gridx++;
		}

		tabButtonPanel.revalidate();
		tabButtonPanel.repaint();
	}

	public static void removeGraph(Graph graph) {
		removeAtIndex(graphs.indexOf(graph));
	}

	public static int getSelectedIndex() {
		return selectedGraph;
	}

	public static void setSelectedIndex(int index) {
		for (int i = 0; i < tabButtons.size(); i++) {
			tabButtons.get(i).setBackground(index != i ? new Color(238, 238, 238) : new Color(200, 221, 242));
		}

		selectedGraph = index;

		if (Main.functionList != null) Main.functionList.setListData(graphs.get(selectedGraph).functions);
	}

	public static Graph getSelectedGraph() {
		return graphs.get(selectedGraph);
	}

	public static Dimension getGraphSize() {
		return graphPane.getSize();
	}

	protected static class GraphPane extends JComponent {

		@Override
		protected void paintComponent(Graphics gg) {
			if (!(gg instanceof Graphics2D)) return;

			Graphics2D g = (Graphics2D) gg;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			int width = this.getWidth();
			int height = this.getHeight();

			g.setColor(Color.LIGHT_GRAY);
			if (gridLineIntervalX > 0) {
				for (double x = gridLineIntervalX * Math.floor((xMax - xMin > 0 ? xMin : xMax) / gridLineIntervalX); x < width; x += gridLineIntervalX) {
					g.drawLine((int) ((x - xMin) / (xMax - xMin) * width), 0, (int) ((x - xMin) / (xMax - xMin) * width), height);
				}
			}

			if (gridLineIntervalY > 0) {
				for (double y = gridLineIntervalY * Math.floor((yMax - yMin > 0 ? yMin : yMax) / gridLineIntervalY); y < height; y += gridLineIntervalY) {
					g.drawLine(0, (int) (height - (y - yMin) / (yMax - yMin) * height), width, (int) (height - (y - yMin) / (yMax - yMin) * height));
				}
			}

			g.setColor(Color.BLACK);
			int zeroX = (int) ((double) -xMin / (xMax - xMin) * width);
			int zeroY = (int) (height - ((double) -yMin / (yMax - yMin) * height));
			if (axisX)
				g.fillRect(0, zeroY - 1, width, 3); // Draw horizontal 0 line
			if (axisY)
				g.fillRect(zeroX - 1, 0, 3, height); // Draw vertical 0 line

			Graph currentGraph = graphs.get(selectedGraph);

			if (currentGraph.isImageValid()) {
				g.drawImage(currentGraph.getImage(), 0, 0, null);
			} else {
				currentGraph.calculateImage();
			}
		}

	}

	protected static class TabHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == rename) {
				int i = tabButtons.indexOf(tabPopup.getInvoker().getParent());
				
				String newFunctionName = JOptionPane.showInputDialog(Main.window, "Please input a new name for \"" + graphs.get(i).name + "\":", "Rename Graph", JOptionPane.PLAIN_MESSAGE);
				if (newFunctionName != null && !newFunctionName.isEmpty()) {
					graphs.get(i).name = newFunctionName;
					((JButton) tabButtons.get(i).getComponent(0)).setText(newFunctionName);
					tabButtons.get(i).repaint();
				}
			} else {
				int i = tabButtons.indexOf(((Component) e.getSource()).getParent());
				
				if (e.getActionCommand().equals("\u00D7")) {
					if (i >= 0 && JOptionPane.showConfirmDialog(Main.window, "Are you sure you want do delete this graph?", "Delete graph", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
						removeAtIndex(i);
				} else {
					setSelectedIndex(i);
				}
			}
		}

	}
}
