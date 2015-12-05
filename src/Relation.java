import java.awt.image.BufferedImage;

public abstract class Relation {
	private String name = null;
	public String getString() { return name; }
	public void setString(String newName) { name = newName; }
	public boolean enabled = true;

	private boolean invalid = true;
	public boolean isInvalid() { return invalid; }
	public void setInvalid(boolean newInvalid) { invalid = newInvalid; }

	private BufferedImage image;
	public BufferedImage getImage() { return image; }
	public void setImage(BufferedImage newImage) { image = newImage; }
	
	public abstract void createImage();
	
	@Override
	public String toString() {
		return name;
	}
}