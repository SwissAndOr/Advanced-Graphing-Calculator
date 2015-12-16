import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

	public static JFrame window = new JFrame();
	public static final int MAX_THICKNESS = 20;

	/**
	 * The file separator character
	 */
	public static final String fs = System.getProperty("file.separator");

	public static final NumberFormat numbers = new DecimalFormat("0");

	private static JPanel relationPanel = new JPanel(new GridBagLayout());
	public static JList<Relation> relationList;
	private static JButton relationUp = new JButton("/\\");
	private static JButton relationDown = new JButton("\\/");

	private static JButton relationNew = new JButton("New");
	private static JButton relationDelete = new JButton("Delete");
	private static JButton relationRename = new JButton("Rename");

	private static String[] relationTypes = {"Function", "Parametric", "Scatterplot"};
	public static JComboBox<String> relationPropertiesType = new JComboBox<>(relationTypes);
	private static JPanel relationPropertiesPanel;

	private static JPanel windowPanel = new JPanel();

	private static JTabbedPane viewSettings = new JTabbedPane();

	private static JPanel minMax = new JPanel();
	private static JLabel xMinLabel = new JLabel("X Min");
	private static NumberTextField xMin = new NumberTextField(-5, 5);
	private static JLabel xMaxLabel = new JLabel("X Max");
	private static NumberTextField xMax = new NumberTextField(5, 5);
	private static JLabel yMinLabel = new JLabel("Y Min");
	private static NumberTextField yMin = new NumberTextField(-5, 5);
	private static JLabel yMaxLabel = new JLabel("Y Max");
	private static NumberTextField yMax = new NumberTextField(5, 5);

	private static JPanel xy = new JPanel();
	private static JLabel xLabel = new JLabel("X");
	private static NumberTextField xView = new NumberTextField(0, 5);
	private static JLabel widthLabel = new JLabel("Width");
	private static NumberTextField widthView = new NumberTextField(10, 5);
	private static JLabel yLabel = new JLabel("Y");
	private static NumberTextField yView = new NumberTextField(0, 5);
	private static JLabel heightLabel = new JLabel("Height");
	private static NumberTextField heightView = new NumberTextField(10, 5);

	private static final FocusListener windowSettingsFocusListener = new FocusAdapter() {

		public void focusLost(FocusEvent e) {
			if (e.getSource() == xMin || e.getSource() == xMax || e.getSource() == yMin || e.getSource() == yMax) {
				try {
					xView.setValue((Double.parseDouble(xMin.getText()) + Double.parseDouble(xMax.getText())) / 2);
				} catch (NumberFormatException exception) {}
				try {
					widthView.setValue(Double.parseDouble(xMax.getText()) - Double.parseDouble(xMin.getText()));
				} catch (NumberFormatException exception) {}
				try {
					yView.setValue((Double.parseDouble(yMin.getText()) + Double.parseDouble(yMax.getText())) / 2);
				} catch (NumberFormatException exception) {}
				try {
					heightView.setValue(Double.parseDouble(yMax.getText()) - Double.parseDouble(yMin.getText()));
				} catch (NumberFormatException exception) {}
			} else {
				try {
					xMin.setValue(Double.parseDouble(xView.getText()) - Double.parseDouble(widthView.getText()) / 2);
				} catch (NumberFormatException exception) {}
				try {
					xMax.setValue(Double.parseDouble(xView.getText()) + Double.parseDouble(widthView.getText()) / 2);
				} catch (NumberFormatException exception) {}
				try {
					yMin.setValue(Double.parseDouble(yView.getText()) - Double.parseDouble(heightView.getText()) / 2);
				} catch (NumberFormatException exception) {}
				try {
					yMax.setValue(Double.parseDouble(yView.getText()) + Double.parseDouble(heightView.getText()) / 2);
				} catch (NumberFormatException exception) {}
			}
		}
	};

	private static JCheckBox cartesianGrid = new JCheckBox("Cartesian Grid", true);
	private static JCheckBox polarGrid = new JCheckBox("Polar Grid", false);

	private static final ItemListener gridChangeListener = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getSource() == cartesianGrid) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					windowPanel.remove(gridLineIntervalXLabel);
					windowPanel.remove(gridLineIntervalX);
					windowPanel.remove(gridLineIntervalYLabel);
					windowPanel.remove(gridLineIntervalY);
				} else {
					windowPanel.add(gridLineIntervalXLabel, 3);
					windowPanel.add(gridLineIntervalX, 4);
					windowPanel.add(gridLineIntervalYLabel, 5);
					windowPanel.add(gridLineIntervalY, 6);
				}
			} else if (e.getSource() == polarGrid) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					windowPanel.remove(gridLineIntervalThetaLabel);
					windowPanel.remove(gridLineIntervalTheta);
					windowPanel.remove(gridLineIntervalRLabel);
					windowPanel.remove(gridLineIntervalR);
				} else {
					windowPanel.remove(axisX);
					windowPanel.remove(axisY);
					windowPanel.add(gridLineIntervalThetaLabel);
					windowPanel.add(gridLineIntervalTheta);
					windowPanel.add(gridLineIntervalRLabel);
					windowPanel.add(gridLineIntervalR);
					windowPanel.add(axisX);
					windowPanel.add(axisY);
				}
			}

			windowPanel.setPreferredSize(new Dimension(210, 160 + (cartesianGrid.isSelected() ? 50 : 0) + (polarGrid.isSelected() ? 50 : 0)));
			sidebar.revalidate();
			sidebar.repaint();
		}
	};

	private static JLabel gridLineIntervalXLabel = new JLabel("Grid Line Interval X");
	private static NumberTextField gridLineIntervalX = new NumberTextField(1, 8);
	private static JLabel gridLineIntervalYLabel = new JLabel("Grid Line Interval Y");
	private static NumberTextField gridLineIntervalY = new NumberTextField(1, 8);
	private static JLabel gridLineIntervalThetaLabel = new JLabel("Grid Line Interval \u03B8");
	private static NumberTextField gridLineIntervalTheta = new NumberTextField(.392699081698724139499745433568, 8);
	private static JLabel gridLineIntervalRLabel = new JLabel("Grid Line Interval R");
	private static NumberTextField gridLineIntervalR = new NumberTextField(1, 8);

	private static JCheckBox axisX = new JCheckBox("Axis X", true);
	private static JCheckBox axisY = new JCheckBox("Axis Y", true);

	public static JButton applyButton = new JButton("Apply");

	private static JPanel sidebar = new JPanel();

	private static JMenuBar menuBar = new JMenuBar();

	private static JMenu fileMenu = new JMenu("File");
	private static JMenu viewMenu = new JMenu("View");
	private static JMenu calculateMenu = new JMenu("Calculate");
	private static JMenu helpMenu = new JMenu("Help");

	private static JMenuItem newGraph = new JMenuItem("New Graph");
	private static JMenuItem openGraph = new JMenuItem("Open Graph...");
	private static JMenuItem saveGraph = new JMenuItem("Save Graph");
	private static JMenuItem saveGraphAs = new JMenuItem("Save Graph As...");
	private static JMenuItem saveAllGraphs = new JMenuItem("Save All");
	private static JMenuItem importRelations = new JMenuItem("Import Relations...");
	private static JMenuItem renameGraph = new JMenuItem("Rename Graph...");
	private static JMenuItem closeGraph = new JMenuItem("Close Graph");
	private static JMenuItem closeAllGraphs = new JMenuItem("Close All Graphs");
	private static JMenuItem newWorkspace = new JMenuItem("New Workspace");
	private static JMenuItem loadWorkspace = new JMenuItem("Load Workspace");
	private static JMenuItem saveWorkspace = new JMenuItem("Save Workspace");
	private static JMenuItem saveWorkspaceAs = new JMenuItem("Save Workspace As");
	private static JMenuItem importWorkspace = new JMenuItem("Import Workspace");
	private static JMenuItem exit = new JMenuItem("Exit");

	private static JMenuItem equalAxes = new JMenuItem("Equalize Axes");
	private static JMenuItem centerOrigin = new JMenuItem("Center Origin");
	private static JMenuItem defaultZoom = new JMenuItem("Default Window Settings");
	private static JMenuItem zoomIn = new JMenuItem("Zoom In");
	private static JMenuItem zoomOut = new JMenuItem("Zoom Out");
	private static JMenu fit = new JMenu("Fit...");
	private static JMenuItem fitAll = new JMenuItem("All Relations");
	private static JMenuItem trig = new JMenuItem("Trigonometry Window");

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
		numbers.setMaximumFractionDigits(15);

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

		importRelations.setMnemonic(KeyEvent.VK_I);
		importRelations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
		importRelations.addActionListener(menuListener);
		fileMenu.add(importRelations);

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

		viewMenu.setMnemonic(KeyEvent.VK_V);

		equalAxes.setMnemonic(KeyEvent.VK_E);
		equalAxes.addActionListener(menuListener);
		viewMenu.add(equalAxes);

		centerOrigin.setMnemonic(KeyEvent.VK_C);
		centerOrigin.addActionListener(menuListener);
		viewMenu.add(centerOrigin);

		defaultZoom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK));
		defaultZoom.setMnemonic(KeyEvent.VK_D);
		defaultZoom.addActionListener(menuListener);
		viewMenu.add(defaultZoom);

		zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK));
		zoomIn.setMnemonic(KeyEvent.VK_I);
		zoomIn.addActionListener(menuListener);
		viewMenu.add(zoomIn);

		zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));
		zoomOut.setMnemonic(KeyEvent.VK_O);
		zoomOut.setDisplayedMnemonicIndex(5);
		zoomOut.addActionListener(menuListener);
		viewMenu.add(zoomOut);

		fit.setMnemonic(KeyEvent.VK_F);
		fit.setAutoscrolls(true);
		fit.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				fit.removeAll();
				fit.add(fitAll);

				fit.addSeparator();
				
				for (Relation rel : GraphTabbedPane.pane.getSelectedGraph().relations) {
					fit.add(new JMenuItem(rel.getName()));
					// TODO Add action listener
				}
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {}
			
			@Override
			public void menuCanceled(MenuEvent e) {}
		});

		fitAll.setMnemonic(KeyEvent.VK_A);
		fitAll.addActionListener(menuListener);
		fit.add(fitAll);

		fit.addSeparator();

		viewMenu.add(fit);

		trig.setMnemonic(KeyEvent.VK_T);
		trig.addActionListener(menuListener);
		viewMenu.add(trig);

		menuBar.add(viewMenu);

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

		xMin.addFocusListener(windowSettingsFocusListener);
		xMax.addFocusListener(windowSettingsFocusListener);
		yMin.addFocusListener(windowSettingsFocusListener);
		yMax.addFocusListener(windowSettingsFocusListener);

		xView.setValue(0);
		xView.setColumns(5);
		widthView.setValue(20);
		widthView.setColumns(5);
		yView.setValue(0);
		yView.setColumns(5);
		heightView.setValue(20);
		heightView.setColumns(5);

		xView.addFocusListener(windowSettingsFocusListener);
		widthView.addFocusListener(windowSettingsFocusListener);
		yView.addFocusListener(windowSettingsFocusListener);
		heightView.addFocusListener(windowSettingsFocusListener);

		GraphTabbedPane.pane.addGraph(new Graph("Graph"));
		GraphTabbedPane.pane.getSelectedGraph().relations.add(new Function("Function"));

		relationList = new JList<>(GraphTabbedPane.pane.getSelectedGraph().relations);
		relationList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (!relationList.getValueIsAdjusting()) {
					if (relationList.getSelectedIndex() != -1 && relationPropertiesPanel != null) {
						sidebar.remove(4);
						relationPropertiesPanel = relationList.getSelectedValue().getPanel();
						sidebar.add(relationPropertiesPanel, 4);
						relationPropertiesPanel.add(relationPropertiesType, 0);
						relationPropertiesPanel.revalidate();
						relationPropertiesPanel.repaint();

						relationPropertiesType.setSelectedIndex(Arrays.asList(relationTypes).indexOf(relationList.getSelectedValue().getClass().getSimpleName()));

						if (relationList.getSelectedIndex() == 0)
							relationUp.setEnabled(false);
						else
							relationUp.setEnabled(true);

						if (relationList.getSelectedIndex() == GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1)
							relationDown.setEnabled(false);
						else
							relationDown.setEnabled(true);

						for (Component component : relationPropertiesPanel.getComponents()) {
							component.setEnabled(true);
						}
					} else {
						relationPropertiesPanel = new JPanel();

						relationUp.setEnabled(false);
						relationDown.setEnabled(false);
						for (Component component : relationPropertiesPanel.getComponents()) {
							component.setEnabled(false);
						}
					}
				}
			}
		});
		relationList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int index = e.getY() / (MAX_THICKNESS * 2 + 4);
				if (index < GraphTabbedPane.pane.getSelectedGraph().relations.size() && e.getX() > 160 && Math.abs(index * (MAX_THICKNESS * 2 + 4) + MAX_THICKNESS + 2 - e.getY()) < 9) {
					e.consume();
					GraphTabbedPane.pane.getSelectedGraph().relations.get(index).enabled ^= true;
					relationList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
				}
			}
		});
		relationList.setFixedCellWidth(178);
		relationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		relationList.setVisibleRowCount(4);
		relationList.setSelectedIndex(0);
		relationList.setCellRenderer(new RelationListCellRenderer());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		JScrollPane relListScrollPane = new JScrollPane(relationList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar bar = relListScrollPane.getVerticalScrollBar();
		bar.setPreferredSize(new Dimension(12, bar.getPreferredSize().height));
		relationPanel.add(relListScrollPane, c);

		relationUp.setMargin(new Insets(relationUp.getMargin().top, 4, relationUp.getMargin().bottom, 4));
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0.5;
		c.gridx = 1;
		relationUp.addActionListener(actionListeners);
		relationPanel.add(relationUp, c);

		relationDown.setMargin(new Insets(relationDown.getMargin().top, 4, relationDown.getMargin().bottom, 4));
		c.gridy = 1;
		relationDown.addActionListener(actionListeners);
		relationPanel.add(relationDown, c);
		sidebar.add(relationPanel);

		relationNew.setMargin(new Insets(relationNew.getMargin().top, 8, relationNew.getMargin().bottom, 8));
		relationNew.addActionListener(actionListeners);
		sidebar.add(relationNew);

		relationDelete.setMargin(new Insets(relationDelete.getMargin().top, 8, relationDelete.getMargin().bottom, 8));
		relationDelete.addActionListener(actionListeners);
		sidebar.add(relationDelete);

		relationRename.setMargin(new Insets(relationRename.getMargin().top, 8, relationRename.getMargin().bottom, 8));
		relationRename.addActionListener(actionListeners);
		sidebar.add(relationRename);

		relationPropertiesType.setPreferredSize(new Dimension(relationPropertiesType.getPreferredSize().width + 20, relationPropertiesType.getPreferredSize().height));

		relationPropertiesType.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int index = relationList.getSelectedIndex();

					switch ((String) relationPropertiesType.getSelectedItem()) {
						case "Function":
							GraphTabbedPane.pane.getSelectedGraph().relations.set(relationList.getSelectedIndex(), new Function(relationList.getSelectedValue()));
							break;
						case "Parametric":
							GraphTabbedPane.pane.getSelectedGraph().relations.set(relationList.getSelectedIndex(), new Parametric(relationList.getSelectedValue()));
							break;
						case "Scatterplot":
							GraphTabbedPane.pane.getSelectedGraph().relations.set(relationList.getSelectedIndex(), new Scatterplot(relationList.getSelectedValue()));
							break;
					}

					relationList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
					relationList.setVisibleRowCount(4);
					relationList.setSelectedIndex(index);
				}
			}
		});
		relationPropertiesPanel = relationList.getSelectedValue().getPanel();
		sidebar.add(relationPropertiesPanel);

		windowPanel.setPreferredSize(new Dimension(210, 210));

		minMax.setPreferredSize(new Dimension(205, 50));
		minMax.add(xMinLabel);
		minMax.add(xMin);
		minMax.add(xMaxLabel);
		minMax.add(xMax);
		minMax.add(yMinLabel);
		minMax.add(yMin);
		minMax.add(yMaxLabel);
		minMax.add(yMax);

		xy.setPreferredSize(new Dimension(205, 50));
		xy.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));
		xy.add(xLabel);
		xy.add(xView);
		xy.add(widthLabel);
		xy.add(widthView);
		xy.add(yLabel);
		xy.add(yView);
		xy.add(heightLabel);
		xy.add(heightView);

		viewSettings.addTab(" Min and Max  ", minMax);
		viewSettings.addTab("      X and Y      ", xy);
		windowPanel.add(viewSettings);

		cartesianGrid.addItemListener(gridChangeListener);
		polarGrid.addItemListener(gridChangeListener);

		windowPanel.add(cartesianGrid);
		windowPanel.add(polarGrid);

		windowPanel.add(gridLineIntervalXLabel);
		windowPanel.add(gridLineIntervalX);
		windowPanel.add(gridLineIntervalYLabel);
		windowPanel.add(gridLineIntervalY);

		windowPanel.add(axisX);
		windowPanel.add(axisY);

		sidebar.add(windowPanel);

		applyButton.addActionListener(actionListeners);
		sidebar.add(applyButton);

		sidebar.setPreferredSize(new Dimension(220, Integer.MAX_VALUE));
		window.add(sidebar, BorderLayout.LINE_START);

		window.add(GraphTabbedPane.pane, BorderLayout.CENTER);

		window.setVisible(true);
	}

	public static void refreshWindowSettings() {
		try {
			double newXMin = GraphTabbedPane.pane.getSelectedGraph().xMin;
			double newXMax = GraphTabbedPane.pane.getSelectedGraph().xMax;
			double newYMin = GraphTabbedPane.pane.getSelectedGraph().yMin;
			double newYMax = GraphTabbedPane.pane.getSelectedGraph().yMax;

			xMin.setValue(newXMin);
			xMax.setValue(newXMax);
			yMin.setValue(newYMin);
			yMax.setValue(newYMax);
			xView.setValue((newXMin + newXMax) / 2);
			widthView.setValue(newXMax - newXMin);
			yView.setValue((newYMin + newYMax) / 2);
			heightView.setValue(newYMax - newYMin);
			gridLineIntervalX.setValue(GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalX);
			gridLineIntervalY.setValue(GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalY);
			axisX.setSelected(GraphTabbedPane.pane.getSelectedGraph().axisX);
			axisY.setSelected(GraphTabbedPane.pane.getSelectedGraph().axisY);

			for (Component component : windowPanel.getComponents()) {
				component.setEnabled(true);
			}
			relationNew.setEnabled(true);
			relationDelete.setEnabled(true);
			relationRename.setEnabled(true);
			applyButton.setEnabled(true);
		} catch (IndexOutOfBoundsException e) {
			xMin.setText("");
			xMax.setText("");
			yMin.setText("");
			yMax.setText("");
			gridLineIntervalX.setText("");
			gridLineIntervalY.setText("");
			axisX.setSelected(true);
			axisY.setSelected(true);

			for (Component component : windowPanel.getComponents()) {
				component.setEnabled(false);
			}
			relationNew.setEnabled(false);
			relationDelete.setEnabled(false);
			relationRename.setEnabled(false);
			applyButton.setEnabled(false);
		}
	}

	protected static class MenuActionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == newGraph) {
				GraphTabbedPane.pane.addGraph(new Graph(GraphTabbedPane.pane.getNameForNewGraph()));
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
			} else if (e.getSource() == importRelations) {
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
						relationList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
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
			} else if (e.getSource() == equalAxes) {
				Dimension dim = GraphTabbedPane.pane.getGraphSize();
				Graph graph = GraphTabbedPane.pane.getSelectedGraph();
				double sx = (double) dim.width / (graph.xMax - graph.xMin);
				double sy = (double) dim.height / (graph.yMax - graph.yMin);

				if (sx > sy) {
					double inc = (double) dim.width / sy - (graph.xMax - graph.xMin);

					xMax.setValue(graph.xMax + inc / 2);
					xMin.setValue(graph.xMin - inc / 2);
					widthView.setValue(inc + graph.xMax - graph.xMin);
				} else {
					double inc = (double) dim.height / sx - (graph.yMax - graph.yMin);

					yMax.setValue(graph.yMax + inc / 2);
					yMin.setValue(graph.yMin - inc / 2);
					heightView.setValue(inc + graph.yMax - graph.yMin);
				}
				
				applyButton.doClick();
			} else if (e.getSource() == centerOrigin) {
				Graph graph = GraphTabbedPane.pane.getSelectedGraph();

				xView.setValue(0);
				yView.setValue(0);

				double mx = 0.5 * (graph.xMax - graph.xMin);
				double my = 0.5 * (graph.yMax - graph.yMin);

				xMin.setValue(-mx);
				xMax.setValue(mx);
				yMin.setValue(-my);
				yMax.setValue(my);
			} else if (e.getSource() == defaultZoom) {
				xMin.setValue(-5);
				xMax.setValue(5);
				yMin.setValue(-5);
				yMax.setValue(5);

				xView.setValue(0);
				yView.setValue(0);
				widthView.setValue(10);
				heightView.setValue(10);
			} else if (e.getSource() == zoomIn) {
				Graph graph = GraphTabbedPane.pane.getSelectedGraph();

				xMin.setValue(graph.xMin / 1.5);
				xMax.setValue(graph.xMax / 1.5);
				yMin.setValue(graph.yMin / 1.5);
				yMax.setValue(graph.yMax / 1.5);

				widthView.setValue((graph.xMax - graph.xMin) / 1.5);
				heightView.setValue((graph.yMax - graph.yMin) / 1.5);
				
				applyButton.doClick();
			} else if (e.getSource() == zoomOut) {
				Graph graph = GraphTabbedPane.pane.getSelectedGraph();

				xMin.setValue(graph.xMin * 1.5);
				xMax.setValue(graph.xMax * 1.5);
				yMin.setValue(graph.yMin * 1.5);
				yMax.setValue(graph.yMax * 1.5);

				widthView.setValue((graph.xMax - graph.xMin) * 1.5);
				heightView.setValue((graph.yMax - graph.yMin) * 1.5);
				
				applyButton.doClick();
			} else if (e.getSource() == fitAll) {
				// TODO
			} else if (e.getSource() == trig) {
				xMin.setValue(-2 * Math.PI);
				xMax.setValue(2 * Math.PI);
				yMin.setValue(-4);
				yMax.setValue(4);

				xView.setValue(0);
				yView.setValue(0);
				widthView.setValue(4 * Math.PI);
				heightView.setValue(8);
			} else if (e.getSource() == calculateY) {
				if (relationList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (relationList.getSelectedValue() == null || relationList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (relationList.getSelectedValue() instanceof Function) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is not a function.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String xValue = JOptionPane.showInputDialog(Main.window, "Please input an X value:", "Calculate Y", JOptionPane.PLAIN_MESSAGE);
					if (xValue != null && !xValue.isEmpty()) {
						Function function = (Function) relationList.getSelectedValue();
						JOptionPane.showMessageDialog(window, function.evaluate(Double.parseDouble(xValue)), "Calculate Y", JOptionPane.PLAIN_MESSAGE);
					}
				}
			} else if (e.getSource() == trace) {

			} else if (e.getSource() == minimum) {
				if (relationList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (relationList.getSelectedValue() == null || relationList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					showMultiInputDialog("Left Bound:", "Right Bound:", "Enter shit", "Shit");
				}
			} else if (e.getSource() == maximum) {
				if (relationList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (relationList.getSelectedValue() == null || relationList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {

				}
			} else if (e.getSource() == intersect) {

			} else if (e.getSource() == zeroes) {
				if (relationList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (relationList.getSelectedValue() == null || relationList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {

				}
			} else if (e.getSource() == derivative) {
				if (relationList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (relationList.getSelectedValue() == null || relationList.getSelectedValue().isInvalid()) {
					JOptionPane.showMessageDialog(Main.window, "The currently selected relation is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				} else {

				}
			} else if (e.getSource() == integral) {
				if (relationList.getSelectedIndex() < 0) {
					JOptionPane.showMessageDialog(Main.window, "There is no currently selected relation.", "Error", JOptionPane.ERROR_MESSAGE);
				} else if (relationList.getSelectedValue() == null || relationList.getSelectedValue().isInvalid()) {
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
			if (e.getSource() == relationUp) {
				int i = relationList.getSelectedIndex();
				if (i > 0) {
					relationList.setValueIsAdjusting(true);
					GraphTabbedPane.pane.getSelectedGraph().relations.add(i - 1, GraphTabbedPane.pane.getSelectedGraph().relations.get(i));
					GraphTabbedPane.pane.getSelectedGraph().relations.remove(i + 1);
					relationList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
					relationList.setSelectedIndex(i - 1);
					relationList.setValueIsAdjusting(false);

					if (relationList.getSelectedIndex() != 0)
						relationDown.setEnabled(true);
					else
						relationUp.setEnabled(false);
					
					relationList.ensureIndexIsVisible(relationList.getSelectedIndex());
				}
			} else if (e.getSource() == relationDown) {
				int i = relationList.getSelectedIndex();
				if (i < GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1) {
					relationList.setValueIsAdjusting(true);
					GraphTabbedPane.pane.getSelectedGraph().relations.add(i + 2, GraphTabbedPane.pane.getSelectedGraph().relations.get(i));
					GraphTabbedPane.pane.getSelectedGraph().relations.remove(i);
					relationList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
					relationList.setSelectedIndex(i + 1);
					relationList.setValueIsAdjusting(false);

					if (relationList.getSelectedIndex() != GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1)
						relationUp.setEnabled(true);
					else
						relationDown.setEnabled(false);
					
					relationList.ensureIndexIsVisible(relationList.getSelectedIndex());
				}
			} else if (e.getSource() == relationNew) {
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

				relationList.setValueIsAdjusting(true);
				GraphTabbedPane.pane.getSelectedGraph().relations.add(new Function("Untitled " + (high + 1)));
				relationList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
				relationList.setSelectedIndex(GraphTabbedPane.pane.getSelectedGraph().relations.size() - 1);
				relationList.setValueIsAdjusting(false);

				relationList.getListSelectionListeners()[0].valueChanged(null);
				
				relationList.ensureIndexIsVisible(relationList.getSelectedIndex());
			} else if (e.getSource() == relationDelete) {
				if (relationList.getSelectedIndex() >= 0 && JOptionPane.showConfirmDialog(window, "Are you sure you want to delete \"" + relationList.getSelectedValue().getName() + "\"?", "Delete Relation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					relationList.setValueIsAdjusting(true);
					int i = relationList.getSelectedIndex();
					GraphTabbedPane.pane.getSelectedGraph().relations.remove(i);
					relationList.setListData(GraphTabbedPane.pane.getSelectedGraph().relations);
					relationList.setSelectedIndex(GraphTabbedPane.pane.getSelectedGraph().relations.size() > 0 ? Math.max(i - 1, 0) : -1);
					relationList.setValueIsAdjusting(false);

					relationList.getListSelectionListeners()[0].valueChanged(null);
					
					relationList.ensureIndexIsVisible(relationList.getSelectedIndex());
				}
			} else if (e.getSource() == relationRename) {
				if (relationList.getSelectedIndex() >= 0) {
					String newRelationName = JOptionPane.showInputDialog(window, "Please input a new name for \"" + relationList.getSelectedValue().getName() + "\":", "Rename Relation", JOptionPane.PLAIN_MESSAGE);
					if (newRelationName != null && !newRelationName.isEmpty()) {
						newRelationName = newRelationName.replaceAll("\\<\\/?html\\>", "");
						relationList.getSelectedValue().setName(newRelationName);
						
						relationList.repaint();
					}
				}
			} else if (e.getSource() == applyButton) {
				gridLineIntervalX.setValue(Math.abs(Double.parseDouble(gridLineIntervalX.getText())));
				gridLineIntervalY.setValue(Math.abs(Double.parseDouble(gridLineIntervalY.getText())));

				relationList.repaint();

				for (Relation relation : GraphTabbedPane.pane.getSelectedGraph().relations)
					relation.applyValues();

				GraphTabbedPane.pane.getSelectedGraph().xMin = Double.parseDouble(xMin.getText());
				GraphTabbedPane.pane.getSelectedGraph().xMax = Double.parseDouble(xMax.getText());
				GraphTabbedPane.pane.getSelectedGraph().yMin = Double.parseDouble(yMin.getText());
				GraphTabbedPane.pane.getSelectedGraph().yMax = Double.parseDouble(yMax.getText());
				GraphTabbedPane.pane.getSelectedGraph().cartesian = cartesianGrid.isSelected();
				GraphTabbedPane.pane.getSelectedGraph().polar = polarGrid.isSelected();
				GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalX = Double.parseDouble(gridLineIntervalX.getText());
				GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalY = Double.parseDouble(gridLineIntervalY.getText());
				GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalTheta = Double.parseDouble(gridLineIntervalTheta.getText());
				GraphTabbedPane.pane.getSelectedGraph().gridLineIntervalR = Double.parseDouble(gridLineIntervalR.getText());
				GraphTabbedPane.pane.getSelectedGraph().axisX = axisX.isSelected();
				GraphTabbedPane.pane.getSelectedGraph().axisY = axisY.isSelected();

				GraphTabbedPane.pane.graphPane.repaint();
			}
		}
	}
}
