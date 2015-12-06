import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

	public static JFrame window = new JFrame();

	/**
	 * The file separator character
	 */
	public static final String fs = System.getProperty("file.separator");

	public static final NumberFormat numbers = new DecimalFormat("0");

	private static JPanel relationPanel = new JPanel(new GridBagLayout());
	public static JList<Relation> functionList;
	private static JButton functionUp = new JButton("/\\");
	private static JButton functionDown = new JButton("\\/");

	private static JButton functionNew = new JButton("New");
	private static JButton functionDelete = new JButton("Delete");
	private static JButton functionRename = new JButton("Rename");

	private static JPanel relationPropertiesBox = new JPanel();
	private static JPanel relationPropertiesPanel;

	private static JPanel windowPanel = new JPanel();
	private static JLabel xMinLabel = new JLabel("X Min");
	private static JFormattedTextField xMin = new JFormattedTextField(numbers);
	private static JLabel xMaxLabel = new JLabel("X Max");
	private static JFormattedTextField xMax = new JFormattedTextField(numbers);
	private static JLabel yMinLabel = new JLabel("Y Min");
	private static JFormattedTextField yMin = new JFormattedTextField(numbers);
	private static JLabel yMaxLabel = new JLabel("Y Max");
	private static JFormattedTextField yMax = new JFormattedTextField(numbers);

	private static final MouseListener formattedListener = new MouseAdapter() {

		public void mousePressed(MouseEvent e) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					JTextField tf = (JTextField) e.getSource();
					int offset = tf.viewToModel(e.getPoint());
					tf.setCaretPosition(offset);
				}
			});
		}
	};

	private static JLabel gridLineIntervalXLabel = new JLabel("Grid Line Interval X");
	private static JFormattedTextField gridLineIntervalX = new JFormattedTextField(numbers);
	private static JLabel gridLineIntervalYLabel = new JLabel("Grid Line Interval Y");
	private static JFormattedTextField gridLineIntervalY = new JFormattedTextField(numbers);

	private static JCheckBox axisX = new JCheckBox("Axis X", true);
	private static JCheckBox axisY = new JCheckBox("Axis Y", true);

	public static JButton applyButton = new JButton("Apply");

	private static JPanel sidebar = new JPanel();

	private static JMenuBar menuBar = new JMenuBar();
	private static JMenu fileMenu = new JMenu("File");
	private static JMenu calculateMenu = new JMenu("Calculate");
	private static JMenu helpMenu = new JMenu("Help");
	private static JMenuItem newGraph = new JMenuItem("New Graph");
	private static JMenuItem openGraph = new JMenuItem("Open Graph...");
	private static JMenuItem saveGraph = new JMenuItem("Save Graph");
	private static JMenuItem saveGraphAs = new JMenuItem("Save Graph As...");
	private static JMenuItem saveAllGraphs = new JMenuItem("Save All");
	private static JMenuItem importFunctions = new JMenuItem("Import Functions...");
	private static JMenuItem renameGraph = new JMenuItem("Rename Graph...");
	private static JMenuItem closeGraph = new JMenuItem("Close Graph");
	private static JMenuItem closeAllGraphs = new JMenuItem("Close All Graphs");
	private static JMenuItem newWorkspace = new JMenuItem("New Workspace");
	private static JMenuItem loadWorkspace = new JMenuItem("Load Workspace");
	private static JMenuItem saveWorkspace = new JMenuItem("Save Workspace");
	private static JMenuItem saveWorkspaceAs = new JMenuItem("Save Workspace As");
	private static JMenuItem importWorkspace = new JMenuItem("Import Workspace");
	private static JMenuItem exit = new JMenuItem("Exit");
	private static JMenuItem calculateY = new JMenuItem("Calculate Y...");
	private static JMenuItem trace = new JMenuItem("Trace Function");
	private static JMenuItem minimum = new JMenuItem("Find Minimum...");
	private static JMenuItem maximum = new JMenuItem("Find Maximum...");
	private static JMenuItem intersect = new JMenuItem("Find Intersection...");
	private static JMenuItem zeroes = new JMenuItem("Find Zeroes...");
	private static JMenuItem derivative = new JMenuItem("Find Derivative...");
	private static JMenuItem integral = new JMenuItem("Find Integral...");
	private static JMenuItem help = new JMenuItem("Help");
	private static JMenuItem about = new JMenuItem("About");

	private static final FileNameExtensionFilter graphFilter = new FileNameExtensionFilter("Graph", "graph");
	private static final FileNameExtensionFilter[] imageFilters = {
			new FileNameExtensionFilter("PNG (*.png)", "png"),
			new FileNameExtensionFilter("GIF (*.gif)", "gif"),
			new FileNameExtensionFilter("JPEG (*.jpg;*.jpeg;*.jpe;*.jfif)", "jpg", "jpeg", "jpe", "jfif"),
			new FileNameExtensionFilter("Bitmap (*.bmp;*.dib)", "bmp", "dib")
	};
	private static final FileNameExtensionFilter workspaceFilter = new FileNameExtensionFilter("Workspace", "wksp");

	private static final MenuActionHandler menuListener = new MenuActionHandler();
	private static JFileChooser graphOpen = new JFileChooser(System.getProperty("user.dir"));
	private static JFileChooser graphSave = new JFileChooser(System.getProperty("user.dir"));
	private static JFileChooser workspaceOpen = new JFileChooser(System.getProperty("user.dir"));
	private static JFileChooser workspaceSave = new JFileChooser(System.getProperty("user.dir"));

	private static final Pattern p = Pattern.compile("Untitled ([0-9]+)");
	private static final ActionListeners actionListeners = new ActionListeners();

	public static void main(String[] a) {
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setMinimumSize(new Dimension(400, 530));
		window.setSize(677, 530);
		window.setTitle("Advanced Graphing Calculator");

		graphSave.setAcceptAllFileFilterUsed(false);
		workspaceSave.setAcceptAllFileFilterUsed(false);

		graphOpen.setFileFilter(graphFilter);
		graphSave.addChoosableFileFilter(graphFilter);
		for (FileNameExtensionFilter filter : imageFilters) {
			graphSave.addChoosableFileFilter(filter);
		}
		workspaceOpen.setFileFilter(workspaceFilter);
		workspaceSave.setFileFilter(workspaceFilter);

		// This can be set to something else if 340 is too many. Do not go over 340. Do not build the seventh row.
		numbers.setMaximumFractionDigits(340);

		fileMenu.setMnemonic(KeyEvent.VK_F);

		newGraph.setMnemonic(KeyEvent.VK_N);
		newGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		newGraph.addActionListener(menuListener);
		fileMenu.add(newGraph);

		openGraph.setMnemonic(KeyEvent.VK_O);
		openGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		openGraph.addActionListener(menuListener);
		fileMenu.add(openGraph);

		saveGraph.setMnemonic(KeyEvent.VK_S);
		saveGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		saveGraph.addActionListener(menuListener);
		fileMenu.add(saveGraph);

		saveGraphAs.setMnemonic(KeyEvent.VK_A);
		saveGraphAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		saveGraphAs.addActionListener(menuListener);
		fileMenu.add(saveGraphAs);

		saveAllGraphs.addActionListener(menuListener);
		fileMenu.add(saveAllGraphs);

		importFunctions.setMnemonic(KeyEvent.VK_I);
		importFunctions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
		importFunctions.addActionListener(menuListener);
		fileMenu.add(importFunctions);

		renameGraph.setMnemonic(KeyEvent.VK_R);
		renameGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
		renameGraph.addActionListener(menuListener);
		fileMenu.add(renameGraph);

		closeGraph.setMnemonic(KeyEvent.VK_C);
		closeGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
		closeGraph.addActionListener(menuListener);
		fileMenu.add(closeGraph);

		closeAllGraphs.setMnemonic(KeyEvent.VK_L);
		closeAllGraphs.addActionListener(menuListener);
		fileMenu.add(closeAllGraphs);

		fileMenu.add(new JSeparator());

		newWorkspace.setMnemonic(KeyEvent.VK_N);
		newWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		newWorkspace.addActionListener(menuListener);
		fileMenu.add(newWorkspace);

		loadWorkspace.setMnemonic(KeyEvent.VK_P);
		loadWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		loadWorkspace.addActionListener(menuListener);
		fileMenu.add(loadWorkspace);

		saveWorkspace.setMnemonic(KeyEvent.VK_V);
		saveWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		saveWorkspace.addActionListener(menuListener);
		fileMenu.add(saveWorkspace);

		saveWorkspaceAs.setMnemonic(KeyEvent.VK_W);
		saveWorkspaceAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		saveWorkspaceAs.addActionListener(menuListener);
		fileMenu.add(saveWorkspaceAs);

		importWorkspace.setMnemonic(KeyEvent.VK_M);
		importWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		importWorkspace.addActionListener(menuListener);
		fileMenu.add(importWorkspace);

		fileMenu.add(new JSeparator());

		exit.setMnemonic(KeyEvent.VK_E);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
		exit.addActionListener(menuListener);
		fileMenu.add(exit);

		menuBar.add(fileMenu);

		calculateMenu.setMnemonic(KeyEvent.VK_C);

		calculateY.setMnemonic(KeyEvent.VK_C);
		calculateY.addActionListener(menuListener);
		calculateMenu.add(calculateY);

		trace.setMnemonic(KeyEvent.VK_T);
		trace.addActionListener(menuListener);
		calculateMenu.add(trace);

		minimum.setMnemonic(KeyEvent.VK_M);
		minimum.addActionListener(menuListener);
		calculateMenu.add(minimum);

		maximum.setMnemonic(KeyEvent.VK_X);
		maximum.addActionListener(menuListener);
		calculateMenu.add(maximum);

		intersect.setMnemonic(KeyEvent.VK_I);
		intersect.setDisplayedMnemonicIndex(5);
		intersect.addActionListener(menuListener);
		calculateMenu.add(intersect);

		zeroes.setMnemonic(KeyEvent.VK_Z);
		zeroes.addActionListener(menuListener);
		calculateMenu.add(zeroes);

		derivative.setMnemonic(KeyEvent.VK_D);
		derivative.setDisplayedMnemonicIndex(5);
		derivative.addActionListener(menuListener);
		calculateMenu.add(derivative);

		integral.setMnemonic(KeyEvent.VK_N);
		integral.setDisplayedMnemonicIndex(6);
		integral.addActionListener(menuListener);
		calculateMenu.add(integral);

		menuBar.add(calculateMenu);

		helpMenu.setMnemonic(KeyEvent.VK_H);

		help.setMnemonic(KeyEvent.VK_H);
		help.addActionListener(menuListener);
		helpMenu.add(help);

		helpMenu.add(new JSeparator());

		about.setMnemonic(KeyEvent.VK_A);
		about.addActionListener(menuListener);
		helpMenu.add(about);

		menuBar.add(helpMenu);

		window.setJMenuBar(menuBar);

		xMin.setValue(-5);
		xMin.setColumns(4);
		xMax.setValue(5);
		xMax.setColumns(4);
		yMin.setValue(-5);
		yMin.setColumns(4);
		yMax.setValue(5);
		yMax.setColumns(4);

		xMin.addMouseListener(formattedListener);
		xMin.addMouseListener(formattedListener);
		xMax.addMouseListener(formattedListener);
		xMax.addMouseListener(formattedListener);
		yMin.addMouseListener(formattedListener);
		yMin.addMouseListener(formattedListener);
		yMax.addMouseListener(formattedListener);
		yMax.addMouseListener(formattedListener);

		GraphTabbedPane.pane.addGraph(new Graph("Graph", -5, 5, -5, 5, 1, 1, true, true));
		GraphTabbedPane.pane.getSelectedGraph().relations.add(new Function("Function"));

		functionList = new JList<>(GraphTabbedPane.pane.getSelectedGraph().relations);
		functionList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (!functionList.getValueIsAdjusting()) {
					if (functionList.getSelectedIndex() != -1) {
						
//						functionTextField.setText(GraphTabbedPane.pane.getSelectedGraph().relations.get(functionList.getSelectedIndex()).getFunction());
//						selectedColor = GraphTabbedPane.pane.getSelectedGraph().relations.get(functionList.getSelectedIndex()).getColor();
//						colorChooserButton.setBackground(new Color(selectedColor.getRGB() & 16777215));
//						thicknessSlider.setValue(GraphTabbedPane.pane.getSelectedGraph().relations.get(functionList.getSelectedIndex()).getThickness());

						relationPropertiesPanel = functionList.getSelectedValue().getPanel();
						
						if (functionList.getSelectedIndex() == 0)
							functionUp.setEnabled(false);
						else
							functionUp.setEnabled(true);

						if (functionList.getSelectedIndex() == GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1)
							functionDown.setEnabled(false);
						else
							functionDown.setEnabled(true);

						for (Component component : relationPropertiesPanel.getComponents()) {
							component.setEnabled(true);
						}
					} else {
						relationPropertiesPanel = new JPanel();

						functionUp.setEnabled(false);
						functionDown.setEnabled(false);
						for (Component component : relationPropertiesPanel.getComponents()) {
							component.setEnabled(false);
						}
					}
				}
			}
		});
		functionList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getX() > 140) {
					e.consume();
					GraphTabbedPane.pane.getSelectedGraph().relations.get(e.getY() / 30).enabled ^= true;
					functionList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
				}
			}
		});
		functionList.setFixedCellWidth(160);
		functionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		functionList.setVisibleRowCount(4);
		functionList.setSelectedIndex(0);
		functionList.setCellRenderer(new FunctionListCellRenderer());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		relationPanel.add(new JScrollPane(functionList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);

		functionUp.setMargin(new Insets(functionUp.getMargin().top, 5, functionUp.getMargin().bottom, 5));
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0.5;
		c.gridx = 1;
		functionUp.addActionListener(actionListeners);
		relationPanel.add(functionUp, c);

		functionDown.setMargin(new Insets(functionDown.getMargin().top, 5, functionDown.getMargin().bottom, 5));
		c.gridy = 1;
		functionDown.addActionListener(actionListeners);
		relationPanel.add(functionDown, c);
		sidebar.add(relationPanel);

		functionNew.setMargin(new Insets(functionNew.getMargin().top, 8, functionNew.getMargin().bottom, 8));
		functionNew.addActionListener(actionListeners);
		sidebar.add(functionNew);

		functionDelete.setMargin(new Insets(functionDelete.getMargin().top, 8, functionDelete.getMargin().bottom, 8));
		functionDelete.addActionListener(actionListeners);
		sidebar.add(functionDelete);

		functionRename.setMargin(new Insets(functionRename.getMargin().top, 8, functionRename.getMargin().bottom, 8));
		functionRename.addActionListener(actionListeners);
		sidebar.add(functionRename);

		relationPropertiesBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		relationPropertiesPanel = functionList.getSelectedValue().getPanel();
		relationPropertiesBox.add(relationPropertiesPanel);
		sidebar.add(relationPropertiesBox);

		windowPanel.setPreferredSize(new Dimension(190, 140));
		windowPanel.add(xMinLabel);
		windowPanel.add(xMin);
		windowPanel.add(xMaxLabel);
		windowPanel.add(xMax);
		windowPanel.add(yMinLabel);
		windowPanel.add(yMin);
		windowPanel.add(yMaxLabel);
		windowPanel.add(yMax);

		gridLineIntervalX.setValue(1);
		gridLineIntervalX.setColumns(6);
		gridLineIntervalY.setValue(1);
		gridLineIntervalY.setColumns(6);

		windowPanel.add(gridLineIntervalXLabel);
		windowPanel.add(gridLineIntervalX);
		windowPanel.add(gridLineIntervalYLabel);
		windowPanel.add(gridLineIntervalY);

		windowPanel.add(axisX);
		windowPanel.add(axisY);

		sidebar.add(windowPanel);

		applyButton.addActionListener(actionListeners);
		sidebar.add(applyButton);

		sidebar.setPreferredSize(new Dimension(200, Integer.MAX_VALUE));
		window.add(sidebar, BorderLayout.LINE_START);

		window.add(GraphTabbedPane.pane, BorderLayout.CENTER);

		window.setVisible(true);
	}

	public static void refreshWindowSettings() {
		try {
			xMin.setText(numbers.format(GraphTabbedPane.pane.getSelectedGraph().xMin));
			xMax.setText(numbers.format(GraphTabbedPane.pane.getSelectedGraph().xMax));
			yMin.setText(numbers.format(GraphTabbedPane.pane.getSelectedGraph().yMin));
			yMax.setText(numbers.format(GraphTabbedPane.pane.getSelectedGraph().yMax));
			gridLineIntervalX.setText(numbers.format(GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalX));
			gridLineIntervalY.setText(numbers.format(GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalY));
			axisX.setSelected(GraphTabbedPane.pane.getSelectedGraph().axisX);
			axisY.setSelected(GraphTabbedPane.pane.getSelectedGraph().axisY);

			for (Component component : windowPanel.getComponents()) {
				component.setEnabled(true);
			}
			functionNew.setEnabled(true);
			functionDelete.setEnabled(true);
			functionRename.setEnabled(true);
			applyButton.setEnabled(true);
		} catch (IndexOutOfBoundsException e) {
			xMin.setText(null);
			xMax.setText(null);
			yMin.setText(null);
			yMax.setText(null);
			gridLineIntervalX.setText(null);
			gridLineIntervalY.setText(null);
			axisX.setSelected(true);
			axisY.setSelected(true);

			for (Component component : windowPanel.getComponents()) {
				component.setEnabled(false);
			}
			functionNew.setEnabled(false);
			functionDelete.setEnabled(false);
			functionRename.setEnabled(false);
			applyButton.setEnabled(false);
		}
	}

	protected static class MenuActionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == newGraph) {
				GraphTabbedPane.pane.addGraph(new Graph(GraphTabbedPane.pane.getNameForNewGraph(), -5, 5, -5, 5, 1, 1, true, true));
			} else if (e.getSource() == openGraph) {
				File dir = graphOpen.getCurrentDirectory();
				graphOpen.setSelectedFile(new File(""));
				graphOpen.setCurrentDirectory(dir);

				if (graphOpen.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
					Graph graph = null;
					try {
						graph = JSON.parseGraph(new String(Files.readAllBytes(graphOpen.getSelectedFile().toPath())));
					} catch (IOException exception) {}

					if (graph == null) {
						JOptionPane.showMessageDialog(window, "<html>" + graphOpen.getSelectedFile().getAbsolutePath() + "<br>Could not read this file.<br>This is not a valid graph file, or its format is not currently supported.</html>", "Error Reading File", JOptionPane.ERROR_MESSAGE);
					} else {
						graph.currentSaveLocation = graphOpen.getSelectedFile().toPath();
						GraphTabbedPane.pane.addGraph(graph);
					}
				}
			} else if ((e.getSource() == saveGraph && !GraphTabbedPane.pane.getSelectedGraph().save()) || e.getSource() == saveGraphAs) {
				graphSave.resetChoosableFileFilters();
				graphSave.addChoosableFileFilter(graphFilter);
				for (FileNameExtensionFilter filter : imageFilters) {
					graphSave.addChoosableFileFilter(filter);
				}

				Graph graph = GraphTabbedPane.pane.getSelectedGraph();
				
				File cur = graphSave.getCurrentDirectory();
				graphSave.setSelectedFile(graph.currentSaveLocation == null ? new File(cur.getAbsolutePath() + fs + graph.name + "." + ((FileNameExtensionFilter) graphSave.getFileFilter()).getExtensions()[0]) : graph.currentSaveLocation.toFile());
				if (graphSave.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
					File file = graphSave.getSelectedFile();

					if (!graphSave.getFileFilter().accept(file)) {
						file = new File(file.getAbsolutePath() + "." + ((FileNameExtensionFilter) graphSave.getFileFilter()).getExtensions()[0]);
					}

					graph.save(file.toPath(), (FileNameExtensionFilter) graphSave.getFileFilter());
				}
			} else if (e.getSource() == saveAllGraphs) {
				graphSave.resetChoosableFileFilters();
				graphSave.addChoosableFileFilter(graphFilter);

				File cur = graphSave.getCurrentDirectory();
				for (Graph graph : GraphTabbedPane.pane.graphs) {
					if (!graph.save()) {
						graphSave.setSelectedFile(new File(cur.getAbsolutePath() + fs + graph.name + "." + ((FileNameExtensionFilter) graphSave.getFileFilter()).getExtensions()[0]));
						if (graphSave.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
							File file = graphSave.getSelectedFile();

							if (!graphSave.getFileFilter().accept(file)) {
								file = new File(file.getAbsolutePath() + ".graph");
							}

							GraphTabbedPane.pane.getSelectedGraph().save(file.toPath(), (FileNameExtensionFilter) graphSave.getFileFilter());
						}
					}
				}
			} else if (e.getSource() == importFunctions) {
				File dir = graphOpen.getCurrentDirectory();
				graphOpen.setSelectedFile(new File(""));
				graphOpen.setCurrentDirectory(dir);

				if (graphOpen.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
					Graph graph = null;
					try {
						graph = JSON.parseGraph(new String(Files.readAllBytes(graphOpen.getSelectedFile().toPath())));
					} catch (IOException exception) {}

					if (graph == null) {
						JOptionPane.showMessageDialog(window, "<html>" + graphOpen.getSelectedFile().getAbsolutePath() + "<br>Could not read this file.<br>This is not a valid graph file, or its format is not currently supported.</html>", "Error Reading File", JOptionPane.ERROR_MESSAGE);
					} else {
						GraphTabbedPane.pane.getSelectedGraph().relations.addAll(graph.relations);
						functionList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
						GraphTabbedPane.pane.graphPane.repaint();
					}
				}
			} else if (e.getSource() == renameGraph) {
				String newGraphName = JOptionPane.showInputDialog(Main.window, "Please input a new name for \"" + GraphTabbedPane.pane.getSelectedGraph().name + "\":", "Rename Graph", JOptionPane.PLAIN_MESSAGE);
				if (newGraphName != null && !newGraphName.isEmpty()) {
					GraphTabbedPane.pane.renameGraphAtIndex(GraphTabbedPane.pane.getSelectedIndex(), newGraphName);
				}
			} else if (e.getSource() == closeGraph) {
				if (JOptionPane.showConfirmDialog(Main.window, "Are you sure you want to close \"" + GraphTabbedPane.pane.getSelectedGraph().name + "\"?", "Close graph", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
					GraphTabbedPane.pane.removeAtIndex(GraphTabbedPane.pane.getSelectedIndex());
			} else if (e.getSource() == closeAllGraphs) {
				if (JOptionPane.showConfirmDialog(Main.window, "Are you sure you want do close all graphs?", "Close All Graphs", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					while (!GraphTabbedPane.pane.graphs.isEmpty()) {
						GraphTabbedPane.pane.removeAtIndex(0);
					}
				}
			} else if (e.getSource() == newWorkspace) {
				// TODO Prompt to save, then close all graphs and reset workspace.
			} else if (e.getSource() == loadWorkspace) {
				File dir = workspaceOpen.getCurrentDirectory();
				workspaceOpen.setSelectedFile(new File(""));
				workspaceOpen.setCurrentDirectory(dir);

				if (workspaceOpen.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
					try {
						window.remove(GraphTabbedPane.pane);

						GraphTabbedPane.pane = JSON.parsePane(new String(Files.readAllBytes(workspaceOpen.getSelectedFile().toPath())));

						window.add(GraphTabbedPane.pane, BorderLayout.CENTER);
						GraphTabbedPane.pane.revalidate();
						GraphTabbedPane.pane.repaint();
					} catch (IOException exception) {
						JOptionPane.showMessageDialog(window, "<html>" + workspaceOpen.getSelectedFile().getAbsolutePath() + "<br>Could not read this file.<br>This is not a valid graph file, or its format is not currently supported.</html>", "Error Reading File", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else if ((e.getSource() == saveWorkspace && !GraphTabbedPane.pane.save()) || e.getSource() == saveWorkspaceAs) {
				if (GraphTabbedPane.pane.currentSaveLocation == null) {
					File dir = workspaceOpen.getCurrentDirectory();
					workspaceOpen.setSelectedFile(new File(""));
					workspaceOpen.setCurrentDirectory(dir);
				} else {
					workspaceSave.setSelectedFile(GraphTabbedPane.pane.currentSaveLocation.toFile());
				}

				if (workspaceSave.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
					File file = workspaceSave.getSelectedFile();

					if (!workspaceFilter.accept(file)) {
						file = new File(file.getPath() + ".wksp");
					}

					GraphTabbedPane.pane.save(file.toPath());
				}
			} else if (e.getSource() == importWorkspace) {
				File dir = workspaceOpen.getCurrentDirectory();
				workspaceOpen.setSelectedFile(new File(""));
				workspaceOpen.setCurrentDirectory(dir);

				if (workspaceOpen.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
					GraphTabbedPane pane = null;
					try {
						pane = JSON.parsePane(new String(Files.readAllBytes(workspaceOpen.getSelectedFile().toPath())));
					} catch (IOException exception) {}

					if (pane == null) {
						JOptionPane.showMessageDialog(window, "<html>" + workspaceOpen.getSelectedFile().getAbsolutePath() + "<br>Could not read this file.<br>This is not a valid graph file, or its format is not currently supported.</html>", "Error Reading File", JOptionPane.ERROR_MESSAGE);
					} else {
						for (Graph graph : pane.graphs)
							GraphTabbedPane.pane.addGraph(graph);
					}
				}
			} else if (e.getSource() == exit) {
				// TODO Do this only if there are unsaved changes
				if (JOptionPane.showConfirmDialog(window, "Are you sure you want to quit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			} else if (e.getSource() == calculateY) {
				if (functionList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (functionList.getSelectedValue() == null || functionList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (functionList.getSelectedValue() instanceof Function) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is not a function.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String xValue = JOptionPane.showInputDialog(Main.window, "Please input an X value:", "Calculate Y", JOptionPane.PLAIN_MESSAGE);
					if (xValue != null && !xValue.isEmpty()) {
						Function function = (Function) functionList.getSelectedValue();
						JOptionPane.showMessageDialog(window, function.evaluate(Double.parseDouble(xValue)), "Calculate Y", JOptionPane.PLAIN_MESSAGE);
					}
				}
			} else if (e.getSource() == trace) {

			} else if (e.getSource() == minimum) {
				if (functionList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (functionList.getSelectedValue() == null || functionList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					showMultiInputDialog("Left Bound:", "Right Bound:", "Enter shit", "Shit");
				}
			} else if (e.getSource() == maximum) {
				if (functionList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (functionList.getSelectedValue() == null || functionList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {

				}
			} else if (e.getSource() == intersect) {

			} else if (e.getSource() == zeroes) {
				if (functionList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (functionList.getSelectedValue() == null || functionList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {

				}
			} else if (e.getSource() == derivative) {
				if (functionList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (functionList.getSelectedValue() == null || functionList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {

				}
			} else if (e.getSource() == integral) {
				if (functionList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (functionList.getSelectedValue() == null || functionList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {

				}
			} else if (e.getSource() == help) {
				// TODO Help
				JOptionPane.showMessageDialog(window, "<html><span style=\"width:500px\">Unfortunately, there is no help at this time.<br>Try contacting SwissAndOr at <a href=\"mailto:2000matu@gmail.com\">2000matu@gmail.com</a> for help.</span></html>", "Help", JOptionPane.PLAIN_MESSAGE);
			} else if (e.getSource() == about) {
				// TODO Use something better than JOptionPane
				JOptionPane.showMessageDialog(window, "<html><span style=\"width:500px\">Advanced Graphing Calculator v. Indev 0.0<br><br>Copyright (c) 2015 SwissAndOr and ricky3350<br><br>An advanced, feature rich graphing program created in Java using Swing<br>and other default libraries. Still in the process of being developed.</span></html>", "Advanced Graphing Calculator v. Indev 0.0", JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

	public static double[] showMultiInputDialog(String leftLabel, String rightLabel, String message, String title) {
		double[] inputs = new double[2];
		JLabel label = new JLabel(message);
		JTextField leftField = new JTextField(10);
		JTextField rightField = new JTextField(10);

		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(new JLabel(leftLabel));
		panel.add(leftField);
		panel.add(new JLabel(rightLabel));
		panel.add(rightField);

		int result = JOptionPane.showConfirmDialog(Main.window, panel, title, JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			inputs[0] = Double.parseDouble(leftField.getText());
			inputs[1] = Double.parseDouble(rightField.getText());
			return inputs;
		} else {
			return null;
		}
	}

	public static class ActionListeners implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == functionUp) {
				int i = functionList.getSelectedIndex();
				if (i > 0) {
					functionList.setValueIsAdjusting(true);
					GraphTabbedPane.pane.getSelectedGraph().relations.add(i - 1, GraphTabbedPane.pane.getSelectedGraph().relations.get(i));
					GraphTabbedPane.pane.getSelectedGraph().relations.remove(i + 1);
					functionList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
					functionList.setSelectedIndex(i - 1);
					functionList.setValueIsAdjusting(false);

					if (functionList.getSelectedIndex() != 0)
						functionDown.setEnabled(true);
					else
						functionUp.setEnabled(false);
				}
			} else if (e.getSource() == functionDown) {
				int i = functionList.getSelectedIndex();
				if (i < GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1) {
					functionList.setValueIsAdjusting(true);
					GraphTabbedPane.pane.getSelectedGraph().relations.add(i + 2, GraphTabbedPane.pane.getSelectedGraph().relations.get(i));
					GraphTabbedPane.pane.getSelectedGraph().relations.remove(i);
					functionList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
					functionList.setSelectedIndex(i + 1);
					functionList.setValueIsAdjusting(false);

					if (functionList.getSelectedIndex() != GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1)
						functionUp.setEnabled(true);
					else
						functionDown.setEnabled(false);
				}
			} else if (e.getSource() == functionNew) {
				int high = 0;

				for (Relation relation : GraphTabbedPane.pane.getSelectedGraph().relations) {
					Matcher m = p.matcher(relation.getName());
					if (m.find()) {
						try {
							int i = Integer.parseInt(m.group(1));
							if (i > high) high = i;
						} catch (NumberFormatException exception) {}
					}
				}

				functionList.setValueIsAdjusting(true);
				GraphTabbedPane.pane.getSelectedGraph().relations.add(new Function("Untitled " + (high + 1)));
				functionList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
				functionList.setSelectedIndex(GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1);
				functionList.setValueIsAdjusting(false);

				functionList.getListSelectionListeners()[0].valueChanged(null);
			} else if (e.getSource() == functionDelete) {
				if (functionList.getSelectedIndex() >= 0 && JOptionPane.showConfirmDialog(window, "Are you sure you want to delete \"" + functionList.getSelectedValue().getName() + "\"?", "Delete Function", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					functionList.setValueIsAdjusting(true);
					int i = functionList.getSelectedIndex();
					GraphTabbedPane.pane.getSelectedGraph().relations.remove(i);
					functionList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
					functionList.setSelectedIndex(GraphTabbedPane.pane.getSelectedGraph().relations.size() > 0 ? Math.max(i - 1, 0) : -1);
					functionList.setValueIsAdjusting(false);

					functionList.getListSelectionListeners()[0].valueChanged(null);
				}
			} else if (e.getSource() == functionRename) {
				if (functionList.getSelectedIndex() >= 0) {
					String newRelationName = JOptionPane.showInputDialog(window, "Please input a new name for \"" + functionList.getSelectedValue().getName() + "\":", "Rename Function", JOptionPane.PLAIN_MESSAGE);
					if (newRelationName != null && !newRelationName.isEmpty()) {
						newRelationName = newRelationName.replaceAll("\\<\\/?html\\>", "");
						functionList.getSelectedValue().setName(newRelationName);
						functionList.repaint();
					}
				}
			} else if (e.getSource() == applyButton) {
				gridLineIntervalX.setText(numbers.format(Math.abs(Double.parseDouble(gridLineIntervalX.getText()))));
				gridLineIntervalY.setText(numbers.format(Math.abs(Double.parseDouble(gridLineIntervalY.getText()))));

				functionList.repaint();

				for (Relation relation : GraphTabbedPane.pane.getSelectedGraph().relations)
					relation.applyValues();

				GraphTabbedPane.pane.getSelectedGraph().xMin = Double.parseDouble(xMin.getText());
				GraphTabbedPane.pane.getSelectedGraph().xMax = Double.parseDouble(xMax.getText());
				GraphTabbedPane.pane.getSelectedGraph().yMin = Double.parseDouble(yMin.getText());
				GraphTabbedPane.pane.getSelectedGraph().yMax = Double.parseDouble(yMax.getText());
				GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalX = Double.parseDouble(gridLineIntervalX.getText());
				GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalY = Double.parseDouble(gridLineIntervalY.getText());
				GraphTabbedPane.pane.getSelectedGraph().axisX = axisX.isSelected();
				GraphTabbedPane.pane.getSelectedGraph().axisY = axisY.isSelected();

				GraphTabbedPane.pane.graphPane.repaint();
			}
		}
	}
}
