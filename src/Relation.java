import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JPanel;

public abstract class Relation {
	
	private String name = null;
	public boolean enabled = true;
	private boolean invalid = true;
	private boolean polar;
	private BufferedImage image;
	private JPanel panel;

	public abstract void applyValues();

	public abstract void createImage();

	public abstract Icon getIcon();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public boolean isPolar() {
		return polar;
	}

	public void setPolar(boolean polar) {
		this.polar = polar;
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	@Override
	public String toString() {
		return getName();
	}

	public abstract String writeJSON();
}
