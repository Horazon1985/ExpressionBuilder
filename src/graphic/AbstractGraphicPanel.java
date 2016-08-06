package graphic;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class AbstractGraphicPanel extends JPanel implements Exportable {
    
    // Grafikexport.
    @Override
    public void export(String filePath) throws IOException {
        BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        paintComponent(g);
        ImageIO.write(bi, "PNG", new File(filePath));
    }
    
}
