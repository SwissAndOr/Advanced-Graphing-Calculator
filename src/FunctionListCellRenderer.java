import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class FunctionListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -5112234230892455719L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(label.getBorder());
		panel.setBackground(label.getBackground());

		JCheckBox cb = new JCheckBox();

		BufferedImage image = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB);
		if (value instanceof Function) {
			Function func = (Function) value;
			Graphics2D g = image.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(func.color);
			g.setStroke(new BasicStroke(func.thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawLine(func.thickness / 2, func.thickness / 2, 24 - func.thickness / 2, 24 - func.thickness / 2);
			g.dispose();

			cb.setSelected(func.enabled);
		}
		panel.add(new JLabel(label.getText(), new ImageIcon(image), SwingConstants.LEFT));

		cb.setBackground(new Color(0, 0, 0, 0));
		panel.add(cb, BorderLayout.EAST);

		return panel;
	}

}
