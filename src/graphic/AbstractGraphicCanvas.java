package graphic;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public abstract class AbstractGraphicCanvas extends Canvas {

    private static final String EXTENSION_FILTER_FILES = "png files (*.png)"; 
    private static final String EXTENSION_FILTER_FILE = "*.png"; 
    
    public AbstractGraphicCanvas() {
        super(500, 500);
    }

    public abstract void draw();
    
    /**
     * Exportiert die gegebene Grafik als eine PNG-Datei und legt sie auf dem
     * gegebenen Pfad filePath ab.
     *
     * @throws IOException
     */
    public void export(Stage parentStage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        // Extension-Filter setzen
        FileChooser.ExtensionFilter extFilter
                = new FileChooser.ExtensionFilter(EXTENSION_FILTER_FILES, EXTENSION_FILTER_FILE);
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(parentStage);
        WritableImage writableImage = new WritableImage((int) getWidth(), (int) getHeight());
        snapshot(null, writableImage);
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
        ImageIO.write(renderedImage, "png", file);
    }

}
