import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class RelationListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -5112234230892455719L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(label.getBorder());
		panel.setBackground(label.getBackground());

		JCheckBox cb = new JCheckBox();
		
		if (value instanceof Relation) {
			Relation rel = (Relation) value;
			
			cb.setSelected(rel.enabled);

			JLabel newLabel = new JLabel(rel.getIcon());
			panel.add(newLabel, BorderLayout.LINE_START);
			
			JTextArea textArea = new JTextArea(label.getText());
			textArea.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setOpaque(false);
			textArea.setBackground(new Color(0, 0, 0, 0));
			
			FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
			
			int lines = 1 + fm.stringWidth(label.getText()) / (list.getFixedCellWidth() - 20 - Main.MAX_THICKNESS * 2 + 2);
			
			textArea.setMargin(new Insets(Math.max(0, Main.MAX_THICKNESS - lines * fm.getHeight() / 2), 1, 0, 1));
			
			if (rel.isInvalid()) textArea.setForeground(Color.RED);
			
			panel.add(textArea);
		}

		cb.setBackground(new Color(0, 0, 0, 0));
		cb.setMargin(new Insets(0, 0, 0, 0));
		panel.add(cb, BorderLayout.LINE_END);

		return panel;
	}

}
