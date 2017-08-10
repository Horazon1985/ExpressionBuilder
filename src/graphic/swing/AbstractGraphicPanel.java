package graphic.swing;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public abstract class AbstractGraphicPanel extends JPanel {

    /**
     * Exportiert die gegebene Grafik als eine PNG-Datei und legt sie auf dem
     * gegebenen Pfad filePath ab.
     *
     * @throws IOException
     */
    public void export(String filePath) throws IOException {
        BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        paintComponent(g);
        ImageIO.write(bi, "PNG", new File(filePath));
    }

}
