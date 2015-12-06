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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

	public Vector<Graph> graphs = new Vector<>();
	private int selectedGraph;
	protected Path currentSaveLocation = null;

	private JPanel tabPanel = new JPanel(new GridBagLayout());
	private JPanel tabButtonPanel = new JPanel(new GridBagLayout());
	private JScrollPane buttonScrollPane = new JScrollPane(tabButtonPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

	private Vector<JPanel> tabButtons = new Vector<>();

	private JPopupMenu tabPopup = new JPopupMenu();
	private JMenuItem rename = new JMenuItem("Rename");

	private JButton addButton = new JButton("+");

	public GraphPane graphPane = new GraphPane();

	private final TabHandler handle = new TabHandler();

	private JPanel space = new JPanel();

	public static GraphTabbedPane pane = new GraphTabbedPane();

	public GraphTabbedPane() {
		setLayout(new BorderLayout());

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

			@Override
			public void actionPerformed(ActionEvent e) {
				addGraph(new Graph(getNameForNewGraph(), -5, 5, -5, 5, 1, 1, true, true));
			}

		});
		c.gridx = 1;
		c.weightx = 0;
		tabPanel.add(addButton, c);

		add(tabPanel, BorderLayout.PAGE_START);

		add(graphPane);
	}

	public boolean save() {
		if (currentSaveLocation == null)
			return false;

		try {
			Files.write(currentSaveLocation, JSON.writeWorkspace().getBytes());
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public void save(Path path) {
		currentSaveLocation = path;
		save();
	}

	private static final Pattern p = Pattern.compile("Untitled ([0-9]+)");

	protected String getNameForNewGraph() {
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

		return "Untitled " + (high + 1);
	}

	/**
	 * <ul>
	 * <li><b><i>addGraph</i></b><br>
	 * <br>
	 * {@code public void addGraph(Graph graph)}<br>
	 * <br>
	 * Adds a new graph to this {@link GraphTabbedPane}. This method should be used for most purposes, instead of directly using
	 * {@link graphs}{@code .addElement()}, because this revalidates the pane as well.<br>
	 * @param graph The graph to add
	 *        </ul>
	 */
	public void addGraph(Graph graph) {
		graphs.addElement(graph);

		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

		JButton button = new JButton(graph.name);
		button.setOpaque(false);
		button.setBackground(new Color(0, 0, 0, 0));
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

		buttonScrollPane.validate();
		buttonScrollPane.getHorizontalScrollBar().setValue(buttonScrollPane.getHorizontalScrollBar().getMaximum());
		buttonScrollPane.repaint();

		graphPane.repaint();
		Main.refreshWindowSettings();
	}

	public void removeAtIndex(int index) {
		graphs.remove(index);
		tabButtons.remove(index);

		setSelectedIndex(selectedGraph >= tabButtons.size() ? selectedGraph - 1 : selectedGraph);

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

		graphPane.repaint();
		Main.refreshWindowSettings();
	}

	public void removeGraph(Graph graph) {
		removeAtIndex(graphs.indexOf(graph));
	}

	public void renameGraphAtIndex(int index, String name) {
		graphs.get(index).name = name;
		((JButton) tabButtons.get(index).getComponent(0)).setText(name);
		tabButtons.get(index).repaint();
	}

	public int getSelectedIndex() {
		return selectedGraph;
	}

	public void setSelectedIndex(int index) {
		for (int i = 0; i < tabButtons.size(); i++) {
			tabButtons.get(i).setBackground(index != i ? new Color(238, 238, 238) : new Color(200, 221, 242));
		}

		selectedGraph = index;

		if (Main.relationList != null) Main.relationList.setListData(selectedGraph >= 0 ? graphs.get(selectedGraph).relations : new Vector<Relation>());
	}

	public Graph getSelectedGraph() {
		return graphs.get(selectedGraph);
	}

	public Dimension getGraphSize() {
		return graphPane.getSize();
	}

	protected class GraphPane extends JComponent {

		@Override
		protected void paintComponent(Graphics gg) { // TODO: Possibly further optimize graphing
			if (!(gg instanceof Graphics2D) || graphs.size() <= 0) return;

			Graph currentGraph = graphs.get(selectedGraph);
			Graphics2D g = (Graphics2D) gg;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			int width = this.getWidth();
			int height = this.getHeight();

			g.setColor(Color.LIGHT_GRAY);
			if (currentGraph.gridLineIntervalX > 0) {
				for (double x = currentGraph.gridLineIntervalX * Math.floor((currentGraph.xMax - currentGraph.xMin > 0 ? currentGraph.xMin : currentGraph.xMax) / currentGraph.gridLineIntervalX); x < width; x += currentGraph.gridLineIntervalX) {
					g.drawLine((int) ((x - currentGraph.xMin) / (currentGraph.xMax - currentGraph.xMin) * width), 0, (int) ((x - currentGraph.xMin) / (currentGraph.xMax - currentGraph.xMin) * width), height);
				}
			}

			if (currentGraph.gridLineIntervalY > 0) {
				for (double y = currentGraph.gridLineIntervalY * Math.floor((currentGraph.yMax - currentGraph.yMin > 0 ? currentGraph.yMin : currentGraph.yMax) / currentGraph.gridLineIntervalY); y < height; y += currentGraph.gridLineIntervalY) {
					g.drawLine(0, (int) (height - (y - currentGraph.yMin) / (currentGraph.yMax - currentGraph.yMin) * height), width, (int) (height - (y - currentGraph.yMin) / (currentGraph.yMax - currentGraph.yMin) * height));
				}
			}

			g.setColor(Color.BLACK);
			int zeroX = (int) ((double) -currentGraph.xMin / (currentGraph.xMax - currentGraph.xMin) * width);
			int zeroY = (int) (height - ((double) -currentGraph.yMin / (currentGraph.yMax - currentGraph.yMin) * height));
			if (currentGraph.axisX)
				g.fillRect(0, zeroY - 1, width, 3); // Draw horizontal 0 line
			if (currentGraph.axisY)
				g.fillRect(zeroX - 1, 0, 3, height); // Draw vertical 0 line

			for (Relation relation : currentGraph.relations) {
				relation.createImage();
				g.drawImage(relation.getImage(), 0, 0, null);
			}
		}

	}

	protected class TabHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == rename) {
				int i = tabButtons.indexOf(tabPopup.getInvoker().getParent());

				String newGraphName = JOptionPane.showInputDialog(Main.window, "Please input a new name for \"" + graphs.get(i).name + "\":", "Rename Graph", JOptionPane.PLAIN_MESSAGE);
				if (newGraphName != null && !newGraphName.isEmpty()) {
					renameGraphAtIndex(i, newGraphName);
				}
			} else {
				int i = tabButtons.indexOf(((Component) e.getSource()).getParent());

				if (e.getActionCommand().equals("\u00D7")) {
					if (i >= 0 && JOptionPane.showConfirmDialog(Main.window, "Are you sure you want do delete \"" + graphs.get(i).name + "\"?", "Delete graph", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
						removeAtIndex(i);
				} else {
					setSelectedIndex(i);
					graphPane.repaint();
					Main.refreshWindowSettings();
				}
			}
		}
	}
}
