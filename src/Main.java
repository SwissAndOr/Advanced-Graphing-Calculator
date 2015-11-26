import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Main {
	public static JFrame window = new JFrame();

	private static NumberFormat numbers = NumberFormat.getNumberInstance();

	private static JPanel functionPanel = new JPanel(new GridBagLayout());
	public static JList<Function> functionList;
	private static JButton functionUp = new JButton("/\\");
	private static JButton functionDown = new JButton("\\/");

	private static JButton functionNew = new JButton("New");
	private static JButton functionDelete = new JButton("Delete");
	private static JButton functionRename = new JButton("Rename");

	private static JPanel functionPropertiesPanel = new JPanel(new FlowLayout());
	private static JTextField functionTextField = new JTextField(16);
	private static Color selectedColor = Color.BLUE;
	private static JLabel colorChooserLabel = new JLabel("Color Chooser");
	private static JButton colorChooserButton = new JButton();
	private static JLabel thicknessLabel = new JLabel("Line Thickness");
	private static JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 0, 15, 2);

	private static JLabel xMinLabel = new JLabel("X Min");
	private static JFormattedTextField xMin = new JFormattedTextField(numbers);
	private static JLabel xMaxLabel = new JLabel("X Max");
	private static JFormattedTextField xMax = new JFormattedTextField(numbers);
	private static JLabel yMinLabel = new JLabel("Y Min");
	private static JFormattedTextField yMin = new JFormattedTextField(numbers);
	private static JLabel yMaxLabel = new JLabel("Y Max");
	private static JFormattedTextField yMax = new JFormattedTextField(numbers);

	private static JLabel gridLineIntervalXLabel = new JLabel("Grid Line Interval X");
	private static JFormattedTextField gridLineIntervalX = new JFormattedTextField(numbers);
	private static JLabel gridLineIntervalYLabel = new JLabel("Grid Line Interval Y");
	private static JFormattedTextField gridLineIntervalY = new JFormattedTextField(numbers);

	private static JCheckBox axisX = new JCheckBox("Axis X", true);
	private static JCheckBox axisY = new JCheckBox("Axis Y", true);

	private static JButton applyButton = new JButton("Apply");

	private static JPanel sidebar = new JPanel();

	public static JProgressBar progressBar = new JProgressBar();

//	@Deprecated
//	private static JTabbedPane graphTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

	public static void main(String[] a) {
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setMinimumSize(new Dimension(300, 450));
		window.setSize(677, 500);
		window.setTitle("Advanced Graphing Calculator");

		xMin.setValue(-5);
		xMin.setColumns(4);
		xMax.setValue(5);
		xMax.setColumns(4);
		yMin.setValue(-5);
		yMin.setColumns(4);
		yMax.setValue(5);
		yMax.setColumns(4);

		GraphTabbedPane.addGraph(new Graph("Graph", -5, 5, -5, 5, 1, 1, true, true));
		GraphTabbedPane.getSelectedGraph().functions.add(new Function("Function"));

		functionList = new JList<>(GraphTabbedPane.getSelectedGraph().functions);
		functionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!functionList.getValueIsAdjusting()) {
					if (functionList.getSelectedIndex() != -1) {
						functionTextField.setText(GraphTabbedPane.getSelectedGraph().functions.get(functionList.getSelectedIndex()).string);
						selectedColor = GraphTabbedPane.getSelectedGraph().functions.get(functionList.getSelectedIndex()).color;
						colorChooserButton.setBackground(new Color(selectedColor.getRGB() & 16777215));
						thicknessSlider.setValue(GraphTabbedPane.getSelectedGraph().functions.get(functionList.getSelectedIndex()).thickness);

						if (functionList.getSelectedIndex() == 0)
							functionUp.setEnabled(false);
						else
							functionUp.setEnabled(true);

						if (functionList.getSelectedIndex() == GraphTabbedPane.getSelectedGraph().functions.size() - 1)
							functionDown.setEnabled(false);
						else
							functionDown.setEnabled(true);

						for (Component component : functionPropertiesPanel.getComponents()) {
							component.setEnabled(true);
						}
					} else {
						functionTextField.setText(null);
						colorChooserButton.setBackground(null);
						thicknessSlider.setValue(2);

						functionUp.setEnabled(false);
						functionDown.setEnabled(false);
						for (Component component : functionPropertiesPanel.getComponents()) {
							component.setEnabled(false);
						}
					}
				}
			}
		});
		functionList.setFixedCellWidth(160);
		functionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		functionList.setVisibleRowCount(4);
		functionList.setSelectedIndex(0);
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		functionPanel.add(new JScrollPane(functionList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);
		functionUp.setMargin(new Insets(functionUp.getMargin().top, 5, functionUp.getMargin().bottom, 5));
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0.5;
		c.gridx = 1;
		functionUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = functionList.getSelectedIndex();
				if (i > 0) {
					functionList.setValueIsAdjusting(true);
					GraphTabbedPane.getSelectedGraph().functions.add(i - 1, GraphTabbedPane.getSelectedGraph().functions.get(i));
					GraphTabbedPane.getSelectedGraph().functions.remove(i + 1);
					functionList.setListData(GraphTabbedPane.getSelectedGraph().functions);
					functionList.setSelectedIndex(i - 1);
					functionList.setValueIsAdjusting(false);

					if (functionList.getSelectedIndex() != 0)
						functionDown.setEnabled(true);
					else
						functionUp.setEnabled(false);
				}
			}
		});
		functionPanel.add(functionUp, c);
		functionDown.setMargin(new Insets(functionDown.getMargin().top, 5, functionDown.getMargin().bottom, 5));
		c.gridy = 1;
		functionDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = functionList.getSelectedIndex();
				if (i < GraphTabbedPane.getSelectedGraph().functions.size() - 1) {
					functionList.setValueIsAdjusting(true);
					GraphTabbedPane.getSelectedGraph().functions.add(i + 2, GraphTabbedPane.getSelectedGraph().functions.get(i));
					GraphTabbedPane.getSelectedGraph().functions.remove(i);
					functionList.setListData(GraphTabbedPane.getSelectedGraph().functions);
					functionList.setSelectedIndex(i + 1);
					functionList.setValueIsAdjusting(false);

					if (functionList.getSelectedIndex() != GraphTabbedPane.getSelectedGraph().functions.size() - 1)
						functionUp.setEnabled(true);
					else
						functionDown.setEnabled(false);
				}
			}
		});
		functionPanel.add(functionDown, c);
		sidebar.add(functionPanel);

		functionNew.setMargin(new Insets(functionNew.getMargin().top, 8, functionNew.getMargin().bottom, 8));
		functionNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newFunctionName = JOptionPane.showInputDialog(window, "Please input the new function's name:", "New Function", JOptionPane.PLAIN_MESSAGE);
				if (newFunctionName != null && !newFunctionName.isEmpty()) {
					functionList.setValueIsAdjusting(true);
					GraphTabbedPane.getSelectedGraph().functions.add(new Function(newFunctionName));
					functionList.setListData(GraphTabbedPane.getSelectedGraph().functions);
					functionList.setSelectedIndex(GraphTabbedPane.getSelectedGraph().functions.size() - 1);
					functionList.setValueIsAdjusting(false);

					functionList.getListSelectionListeners()[0].valueChanged(null);
				}
			}
		});
		sidebar.add(functionNew);
		functionDelete.setMargin(new Insets(functionDelete.getMargin().top, 8, functionDelete.getMargin().bottom, 8));
		functionDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (functionList.getSelectedIndex() >= 0 && JOptionPane.showConfirmDialog(window, "Are you sure you want to delete this function?", "Delete Function", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					functionList.setValueIsAdjusting(true);
					int i = functionList.getSelectedIndex();
					GraphTabbedPane.getSelectedGraph().functions.remove(i);
					functionList.setListData(GraphTabbedPane.getSelectedGraph().functions);
					functionList.setSelectedIndex(GraphTabbedPane.getSelectedGraph().functions.size() > 0 ? Math.max(i - 1, 0) : -1);
					functionList.setValueIsAdjusting(false);

					functionList.getListSelectionListeners()[0].valueChanged(null);
				}
			}
		});
		sidebar.add(functionDelete);
		functionRename.setMargin(new Insets(functionRename.getMargin().top, 8, functionRename.getMargin().bottom, 8));
		functionRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (functionList.getSelectedIndex() >= 0) {
					String newFunctionName = JOptionPane.showInputDialog(window, "Please input a new name for \"" + functionList.getSelectedValue().name + "\":", "Rename Function", JOptionPane.PLAIN_MESSAGE);
					if (newFunctionName != null && !newFunctionName.isEmpty()) {
						functionList.getSelectedValue().name = newFunctionName;
						functionList.repaint();
					}
				}
			}
		});
		sidebar.add(functionRename);

		functionPropertiesPanel.setPreferredSize(new Dimension(190, 120));
		functionPropertiesPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		functionPropertiesPanel.add(functionTextField);
		functionPropertiesPanel.add(colorChooserLabel);
		colorChooserButton.setPreferredSize(new Dimension(85, 20));
		colorChooserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedColor = JColorChooser.showDialog(null, "Color Chooser", GraphTabbedPane.getSelectedGraph().functions.get(functionList.getSelectedIndex()).color);
				if (selectedColor == null)
					selectedColor = GraphTabbedPane.getSelectedGraph().functions.get(functionList.getSelectedIndex()).color;
				else
					colorChooserButton.setBackground(new Color(selectedColor.getRGB() & 16777215));
			}
		});
		colorChooserButton.setBackground(selectedColor);
		functionPropertiesPanel.add(colorChooserButton);
		functionPropertiesPanel.add(thicknessLabel);
		thicknessSlider.setPaintTicks(true);
		thicknessSlider.setMinorTickSpacing(1);
		thicknessSlider.setPreferredSize(new Dimension(180, thicknessSlider.getPreferredSize().height));
		functionPropertiesPanel.add(thicknessSlider);
		sidebar.add(functionPropertiesPanel);

		sidebar.add(xMinLabel);
		sidebar.add(xMin);
		sidebar.add(xMaxLabel);
		sidebar.add(xMax);
		sidebar.add(yMinLabel);
		sidebar.add(yMin);
		sidebar.add(yMaxLabel);
		sidebar.add(yMax);

		gridLineIntervalX.setValue(1);
		gridLineIntervalX.setColumns(6);
		gridLineIntervalY.setValue(1);
		gridLineIntervalY.setColumns(6);

		sidebar.add(gridLineIntervalXLabel);
		sidebar.add(gridLineIntervalX);
		sidebar.add(gridLineIntervalYLabel);
		sidebar.add(gridLineIntervalY);

		sidebar.add(axisX);
		sidebar.add(axisY);

		applyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				gridLineIntervalX.setText(Double.toString(Math.abs(Double.parseDouble(gridLineIntervalX.getText()))));
				gridLineIntervalY.setText(Double.toString(Math.abs(Double.parseDouble(gridLineIntervalY.getText()))));

				if (functionList.getSelectedIndex() >= 0) {
					Function currentFunc = GraphTabbedPane.getSelectedGraph().functions.get(functionList.getSelectedIndex());
					currentFunc.string = functionTextField.getText();
					currentFunc.color = selectedColor;
					currentFunc.thickness = thicknessSlider.getValue();
					
					if (!currentFunc.string.equals(functionTextField.getText()) || !currentFunc.color.equals(selectedColor) || currentFunc.thickness != thicknessSlider.getValue()) {
						GraphTabbedPane.getSelectedGraph().invalidate();
					}
				}

				GraphTabbedPane.getSelectedGraph().xMin = Double.parseDouble(xMin.getText());
				GraphTabbedPane.getSelectedGraph().xMax = Double.parseDouble(xMax.getText());
				GraphTabbedPane.getSelectedGraph().yMin = Double.parseDouble(yMin.getText());
				GraphTabbedPane.getSelectedGraph().yMax = Double.parseDouble(yMax.getText());
				GraphTabbedPane.getSelectedGraph().gridLineIntervalX = Double.parseDouble(gridLineIntervalX.getText());
				GraphTabbedPane.getSelectedGraph().gridLineIntervalY = Double.parseDouble(gridLineIntervalY.getText());
				GraphTabbedPane.getSelectedGraph().axisX = axisX.isSelected();
				GraphTabbedPane.getSelectedGraph().axisY = axisY.isSelected();

				GraphTabbedPane.getSelectedGraph().invalidate();
				GraphTabbedPane.graphPane.repaint();
			}
		});
		sidebar.add(applyButton);

		sidebar.setPreferredSize(new Dimension(200, Integer.MAX_VALUE));
		window.add(sidebar, BorderLayout.LINE_START);

		window.add(GraphTabbedPane.pane, BorderLayout.CENTER);

		progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 8));
		window.add(progressBar, BorderLayout.PAGE_END);

		window.setVisible(true);
	}
	
	public static void refreshWindowSettings() {
		xMin.setText(Double.toString(GraphTabbedPane.getSelectedGraph().xMin));
		xMax.setText(Double.toString(GraphTabbedPane.getSelectedGraph().xMax));
		yMin.setText(Double.toString(GraphTabbedPane.getSelectedGraph().yMin));
		yMax.setText(Double.toString(GraphTabbedPane.getSelectedGraph().yMax));
		gridLineIntervalX.setText(Double.toString(GraphTabbedPane.getSelectedGraph().gridLineIntervalX));
		gridLineIntervalY.setText(Double.toString(GraphTabbedPane.getSelectedGraph().gridLineIntervalY));
		axisX.setSelected(GraphTabbedPane.getSelectedGraph().axisX);
		axisY.setSelected(GraphTabbedPane.getSelectedGraph().axisY);
	}
}
