import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
			
//				Graphics2D g = image.createGraphics();
//				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//				g.setColor(relation.getColor());
//				g.setStroke(new BasicStroke(relation.getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//				g.drawLine(relation.getThickness() / 2, relation.getThickness() / 2, 24 - relation.getThickness() / 2, 24 - relation.getThickness() / 2);
//				g.dispose();

			cb.setSelected(rel.enabled);

			JLabel newLabel = new JLabel(label.getText(), rel.getIcon(), SwingConstants.LEFT);
			if (rel.isInvalid()) newLabel.setForeground(Color.RED);
			panel.add(newLabel);
		}

		cb.setBackground(new Color(0, 0, 0, 0));
		panel.add(cb, BorderLayout.EAST);

		return panel;
	}

}
