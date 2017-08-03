package graphic;

import static graphic.AbstractGraphicCanvas2D.FONT;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public abstract class AbstractGraphicCanvas extends Canvas {

    private static final String EXTENSION_FILTER_FILES = "png files (*.png)"; 
    private static final String EXTENSION_FILTER_FILE = "*.png"; 

    protected static final int FONT_SIZE = 12;
    protected static final Font FONT = new Font("Courier New", FONT_SIZE);
    
    protected static final Text FIRST_AXIS = createText("1. axis");
    protected static final Text SECOND_AXIS = createText("2. axis");
    
    public AbstractGraphicCanvas() {
        super(500, 500);
    }

    public abstract void draw();
    
    protected static Text createText(String s) {
        Text text = new Text(s);
        text.setFont(FONT);
        return text;
    }
    
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
