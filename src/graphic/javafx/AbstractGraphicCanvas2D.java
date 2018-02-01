package graphic.javafx;

import abstractexpressions.matrixexpression.classes.Matrix;
import exceptions.EvaluationException;
import javafx.scene.input.MouseEvent;
import java.awt.Point;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lang.translator.Translator;

/**
 * Abstrakte Oberklasse aller Grafikklassen für zweidimensionale Grafiken.
 */
public abstract class AbstractGraphicCanvas2D extends AbstractGraphicCanvas {

    private static final String GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON = "GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON";
    private static final String GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON = "GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON";
    private static final String GR_Graphic2D_MOVE_MOUSE_WHEEL = "GR_Graphic2D_MOVE_MOUSE_WHEEL";
    
    /**
     * Variablenname für 2D-Graphen: varAbsc = Abszissenname.
     */
    protected String varAbsc = null;

    /**
     * Variablenname für 2D-Graphen: varOrd = Ordinatenname.
     */
    protected String varOrd = null;

    protected double axeCenterX, axeCenterY;
    protected double maxX, maxY;
    protected int expX, expY;

    protected boolean movable = false;

    protected static boolean pointsAreShowable = true;
    protected int mouseCoordinateX = -1;
    protected int mouseCoordinateY = -1;

    protected double[][] specialPoints;

    protected Point lastMousePosition;
    protected double zoomfactor, zoomfactorX, zoomfactorY;

    protected final static Color[] FIXED_COLORS = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.RED, Color.PINK};
    protected final static int MOUSE_DISTANCE_FOR_SHOWING_POINT = 10;

    public AbstractGraphicCanvas2D() {
        super();
        addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseCoordinateX = (int) event.getX();
                mouseCoordinateY = (int) event.getY();
                draw();
            }
        });
    }

    public AbstractGraphicCanvas2D(final double maxZoomfactor, final double minZoomfactor) {

        this();

        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lastMousePosition = new Point((int) event.getX(), (int) event.getY());
                movable = event.getButton() == MouseButton.PRIMARY;
            }
        });

        setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (movable) {
                    axeCenterX += (lastMousePosition.x - event.getX()) * maxX / 250;
                    axeCenterY += (-lastMousePosition.y + event.getY()) * maxY / 250;
                    lastMousePosition = new Point((int) event.getX(), (int) event.getY());
                } else {
                    if (lastMousePosition.x - event.getX() >= 0 && zoomfactorX < maxZoomfactor
                            || lastMousePosition.x - event.getX() <= 0 && zoomfactorX > minZoomfactor) {
                        maxX = maxX * Math.pow(1.02, lastMousePosition.x - event.getX());
                        zoomfactorX = zoomfactorX * Math.pow(1.02, lastMousePosition.x - event.getX());
                    }
                    if (lastMousePosition.y - event.getY() >= 0 && zoomfactorY < maxZoomfactor
                            || lastMousePosition.y - event.getY() <= 0 && zoomfactorY > minZoomfactor) {
                        maxY = maxY * Math.pow(1.02, lastMousePosition.y - event.getY());
                        zoomfactorY = zoomfactorY * Math.pow(1.02, lastMousePosition.y - event.getY());
                    }
                    lastMousePosition = new Point((int) event.getX(), (int) event.getY());
                }
                draw();
            }
        });

        setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                // Der Zoomfaktor darf höchstens maxZoomfactor sein (und mindestens minZoomfactor)
                if (event.getDeltaY() <= 0 && zoomfactor < maxZoomfactor
                        || event.getDeltaY() >= 0 && zoomfactor > minZoomfactor) {
                    maxX *= Math.pow(1.1, -event.getDeltaY() / 40);
                    maxY *= Math.pow(1.1, -event.getDeltaY() / 40);
                    zoomfactor *= Math.pow(1.1, -event.getDeltaY() / 40);
                    computeExpXExpY();
                }
                draw();
            }
        });

    }

    public static boolean getPointsAreShowable() {
        return pointsAreShowable;
    }

    public static void setPointsAreShowable(boolean showable) {
        pointsAreShowable = showable;
    }

    public void setSpecialPoints(double[][] specialPoints) {
        this.specialPoints = specialPoints;
    }

    /**
     * Setzt die speziellen Punkte mittels Auswertung der Matrizenausdrücke
     * specialPoints.<br>
     * VORAUSSETZUNG: specialPoints sind allesamt (2x1)-Matrizen.
     *
     * @throws EvaluationException
     */
    public void setSpecialPoints(Matrix[] specialPoints) throws EvaluationException {
        this.specialPoints = new double[specialPoints.length][2];
        for (int i = 0; i < specialPoints.length; i++) {
            this.specialPoints[i][0] = specialPoints[i].getEntry(0, 0).evaluate();
            this.specialPoints[i][1] = specialPoints[i].getEntry(1, 0).evaluate();
        }
    }

    /**
     * Liefert eine List mit Bedienungsanweisungen für zweidimensionale
     * Grafiken.
     */
    public static List<String> getInstructions() {
        List<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage(GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON));
        instructions.add(Translator.translateOutputMessage(GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON));
        instructions.add(Translator.translateOutputMessage(GR_Graphic2D_MOVE_MOUSE_WHEEL));
        return instructions;
    }

    /**
     * Erzeugt eine neue Zufallsfarbe.
     */
    protected Color generateColor() {
        return Color.rgb((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
    }

    protected int[] getMouseCoordinates() {
        return new int[]{this.mouseCoordinateX, this.mouseCoordinateY};
    }

    /**
     * Berechnet die Attribute expX bzw. expY, die größten Exponenten für eine
     * Zehnerpotenz, die kleiner oder gleich maxX bzw. maxY sind. VORAUSSETZUNG:
     * der Graph ist bereits initialisiert (und mit Funktionswerten gefüllt).
     * Die Attribute maxX und maxY sind initialisiert.
     */
    protected void computeExpXExpY() {

        /*
         Markierungen an den Achsen anbringen; exp_x = max. Exponent einer
         10er-Potenz, die kleiner als der größte x-Wert ist exp_y = min.
         Exponent einer 10er-Potenz, die kleiner als der größte y-Wert ist
         */
        this.expX = 0;
        this.expY = 0;

        if (this.maxX >= 1) {
            while (this.maxX / Math.pow(10, this.expX) >= 1) {
                this.expX++;
            }
            this.expX--;
        } else {
            while (this.maxX / Math.pow(10, this.expX) < 1) {
                this.expX--;
            }
        }

        if (this.maxY >= 1) {
            while (this.maxY / Math.pow(10, this.expY) >= 1) {
                this.expY++;
            }
            this.expY--;
        } else {
            while (this.maxY / Math.pow(10, this.expY) < 1) {
                this.expY--;
            }
        }

    }

    /**
     * Berechnet aus Punktkoordinaten (x, y) Koordinaten (x', y') für die
     * graphische Darstellung.<br>
     * VORAUSSETZUNG: maxX und maxY sind initialisiert und nicht 0.
     */
    protected int[] convertToPixel(double x, double y) {
        return new int[]{convertToPixelX(x), convertToPixelY(y)};
    }

    protected int convertToPixelX(double x) {
        return 250 + (int) Math.round(250 * (x - this.axeCenterX) / this.maxX);
    }

    protected int convertToPixelY(double y) {
        return 250 - (int) Math.round(250 * (y - this.axeCenterY) / this.maxY);
    }

    /**
     * Berechnet aus Koordinaten (x', y') für die graphische Darstellung die
     * echten euklidischen Koordinaten (x, y).<br>
     * VORAUSSETZUNG: maxX und maxY sind initialisiert und nicht 0.
     */
    protected double[] convertToEuclideanCoordinates(int x, int y) {
        return new double[]{convertToEuclideanCoordinateX(x), convertToEuclideanCoordinateY(y)};
    }

    protected double convertToEuclideanCoordinateX(int x) {
        return ((x - 250) * this.maxX) / 250 + this.axeCenterX;
    }

    protected double convertToEuclideanCoordinateY(int y) {
        return ((250 - y) * this.maxY) / 250 + this.axeCenterY;
    }

    /**
     * Berechnet den euklidischen Abstand zweier Punkte (in Pixeln).
     */
    protected int computeDistanceOfPixels(int[] pixelStart, int[] pixelEnd) {
        return (int) Math.sqrt(Math.pow(pixelStart[0] - pixelEnd[0], 2) + Math.pow(pixelStart[1] - pixelEnd[1], 2));
    }

    /**
     * Gibt den Achseneintrag m*10^(-k) zurück.
     */
    protected BigDecimal roundAxisEntries(int m, int k) {
        if (k >= 0) {
            return new BigDecimal(m).multiply(BigDecimal.TEN.pow(k));
        }
        return new BigDecimal(m).divide(BigDecimal.TEN.pow(-k));
    }

    /**
     * Zeichnet die Achsen und die grauen Niveaulinien. Die horizontale Achse
     * wird mit varAbsc beschriftet, die vertikale Achse mit varOrd, falls
     * varOrd nicht null ist. Ist varOrd == null, so wird die vertikale Achse
     * mit f(varAbsc) beschriftet. f steht hierbei stellvertretend für eine
     * allgemeine Funktion.
     */
    protected void drawAxesAndLines(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);

        int linePosition;
        int k = (int) (this.axeCenterX * Math.pow(10, -this.expX));

        // x-Niveaulinien zeichnen
        linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];

        while (linePosition <= 500) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
                gc.strokeLine(linePosition, 0, linePosition, 500);

                if ((250 * this.axeCenterY / this.maxY - 3 <= 248) && (250 * this.axeCenterY / this.maxY - 3 >= -230)) {
                    gc.strokeText(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
                } else if (250 * this.axeCenterY / this.maxY - 3 > 248) {
                    gc.strokeText(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 495);
                } else {
                    gc.strokeText(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 20);
                }

            }

            k++;

        }

        k = (int) (this.axeCenterX * Math.pow(10, -this.expX)) - 1;
        linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];

        while (linePosition >= 0) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
                gc.strokeLine(linePosition, 0, linePosition, 500);

                if ((250 * this.axeCenterY / this.maxY - 3 <= 248) && (250 * this.axeCenterY / this.maxY - 3 >= -230)) {
                    gc.strokeText(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
                } else if (250 * this.axeCenterY / this.maxY - 3 > 248) {
                    gc.strokeText(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 495);
                } else {
                    gc.strokeText(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 20);
                }

            }

            k--;

        }

        // y-Niveaulinien zeichnen
        k = (int) (this.axeCenterY * Math.pow(10, -this.expY));
        linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];

        while (linePosition >= 0) {
            linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
            gc.strokeLine(0, linePosition, 500, linePosition);

            if ((250 * this.axeCenterX / this.maxX - 3 >= -225) && (250 * this.axeCenterX / this.maxX - 3 <= 245)) {
                gc.strokeText(String.valueOf(roundAxisEntries(k, expY)), 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, linePosition - 3);
            } else if (250 * this.axeCenterX / this.maxX - 3 >= -225) {
                gc.strokeText(String.valueOf(roundAxisEntries(k, this.expY)), 5, linePosition - 3);
            } else {
                gc.strokeText(String.valueOf(roundAxisEntries(k, this.expY)), 475, linePosition - 3);
            }

            k++;

        }

        k = (int) (this.axeCenterY * Math.pow(10, -this.expY)) - 1;
        linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];

        while (linePosition <= 500) {
            linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
            gc.strokeLine(0, linePosition, 500, linePosition);

            if ((250 * this.axeCenterX / this.maxX - 3 >= -225) && (250 * this.axeCenterX / this.maxX - 3 <= 245)) {
                gc.strokeText(String.valueOf(roundAxisEntries(k, this.expY)), 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, linePosition - 3);
            } else if (250 * this.axeCenterX / this.maxX - 3 >= -225) {
                gc.strokeText(String.valueOf(roundAxisEntries(k, this.expY)), 5, linePosition - 3);
            } else {
                gc.strokeText(String.valueOf(roundAxisEntries(k, this.expY)), 475, linePosition - 3);
            }

            k--;

        }

        // Achsen inkl. Achsenbezeichnungen eintragen
        // Achsen
        gc.setStroke(Color.BLACK);
        gc.strokeLine(0, 250 + (int) (250 * this.axeCenterY / this.maxY), 500, 250 + (int) (250 * this.axeCenterY / this.maxY));
        gc.strokeLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX), 500);
        // Achsenpfeile
        gc.strokeLine(500, 250 + (int) (250 * this.axeCenterY / this.maxY), 494, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
        gc.strokeLine(500, 250 + (int) (250 * this.axeCenterY / this.maxY), 494, 250 + (int) (250 * this.axeCenterY / this.maxY) + 3);
        gc.strokeLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, 6);
        gc.strokeLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX) - 3, 6);
        /*
         Achsenbeschriftung. WICHTIG: In der Prozedur strokeText werden die
         Achsenbeschriftung derart eingetragen, dass (1) Die Beschriftung der
         Variablen var innerhalb des Bildschirms liegt (5 px) (2) Die
         Beschriftung f(var) links von der vertikalen Achse liegt (5 px)
         Hierzu müssen die Pixellängen der gezeichneten Strings ausgerechnet
         werden (mittels gc.getFontMetrics().stringWidth()).
         */
        double lengthTextFirstAxis = FIRST_AXIS.getLayoutBounds().getWidth();
        double lengthTextSecondAxis = SECOND_AXIS.getLayoutBounds().getWidth();

        if (this.varAbsc == null) {
            gc.strokeText(FIRST_AXIS.getText(), 500 - 5 - lengthTextFirstAxis, 250 + (int) (250 * this.axeCenterY / this.maxY) + 15);
        } else {
            gc.strokeText(this.varAbsc, 500 - 5 - createText(varAbsc).getLayoutBounds().getWidth(), 250 + (int) (250 * this.axeCenterY / this.maxY) + 15);
        }
        if (this.varOrd == null) {
            gc.strokeText(SECOND_AXIS.getText(), 250 - (int) (250 * this.axeCenterX / this.maxX) - 5 - lengthTextSecondAxis, 20);
        } else {
            gc.strokeText(this.varOrd, 250 - (int) (250 * this.axeCenterX / this.maxX) - 5 - createText(varOrd).getLayoutBounds().getWidth(), 20);
        }
    }

    /**
     * Zeichnet rote Punkte an gegebenen Stellen specialPoints (etwa Markierung
     * von Nullstellen etc).
     */
    protected void drawSpecialPoints(GraphicsContext gc, double[][] specialPoints) {
        gc.setFill(Color.RED);
        if (specialPoints == null) {
            return;
        }
        int[][] specialPointsCoordinates = new int[specialPoints.length][2];
        for (int i = 0; i < specialPoints.length; i++) {
            specialPointsCoordinates[i] = convertToPixel(specialPoints[i][0], specialPoints[i][1]);
            gc.fillOval(specialPointsCoordinates[i][0] - 3, specialPointsCoordinates[i][1] - 3, 7, 7);
        }
    }

    /**
     * Zeichnet einen roten Punkt an der Stelle (x, y), wobei x und y die
     * Pixelkoordinaten des Punktes sind.
     */
    protected void drawCirclePoint(GraphicsContext gc, int x, int y, boolean printCoordinates) {
        gc.setStroke(Color.RED);
        gc.setFill(Color.RED);
        gc.fillOval(x - 3, y - 3, 7, 7);
        if (!printCoordinates) {
            return;
        }
        gc.setStroke(Color.BLACK);
        Text coordText = createText("(" + roundCoordinate(convertToEuclideanCoordinateX(x), 2) + ", "
                + roundCoordinate(convertToEuclideanCoordinateY(y), 2) + ")");
        double length = coordText.getLayoutBounds().getWidth();
        if (x >= length) {
            if (y >= 20) {
                gc.strokeText(coordText.getText(), x - length, y - 5);
            } else {
                gc.strokeText(coordText.getText(), x - length, y + 20);
            }
        } else if (y >= 20) {
            gc.strokeText(coordText.getText(), x, y - 5);
        } else {
            gc.strokeText(coordText.getText(), x, y + 20);
        }
    }

    /**
     * Zeichnet einen Vektorpfeil für den Vektor [a; b] an der Stelle (x, y),
     * wobei x und y die Pixelkoordinaten des Punktes sind.
     */
    protected void drawVectorLine(GraphicsContext gc, int x, int y, int endPointVectorX, int endPointVectorY, boolean printCoordinates) {
        gc.setStroke(Color.BLACK);
        Text coordText = createText("(" + roundCoordinate(convertToEuclideanCoordinateX(endPointVectorX), 2) + ", "
                + roundCoordinate(convertToEuclideanCoordinateY(endPointVectorY), 2) + ")");
        double length = coordText.getLayoutBounds().getWidth();
        gc.strokeLine(x, y, endPointVectorX, endPointVectorY);
        if (endPointVectorX >= length) {
            if (endPointVectorY >= 20) {
                gc.strokeText(coordText.getText(), endPointVectorX - length, endPointVectorY - 5);
            } else {
                gc.strokeText(coordText.getText(), endPointVectorX - length, endPointVectorY + 20);
            }
        } else if (endPointVectorY >= 20) {
            gc.strokeText(coordText.getText(), endPointVectorX, endPointVectorY - 5);
        } else {
            gc.strokeText(coordText.getText(), endPointVectorX, endPointVectorY + 20);
        }
    }

    protected void drawVectorArrow(GraphicsContext gc, int x, int y, int length, double angleOfArrow) {
        if (angleOfArrow == -1) {
            return;
        }
        double angleForLeftArrowPart = angleOfArrow - Math.PI / 4, angleForRightArrowPart = angleOfArrow + Math.PI / 4;
        gc.strokeLine(x, y, x - (int) (length * Math.cos(angleForLeftArrowPart)), y - (int) (length * Math.sin(angleForLeftArrowPart)));
        gc.strokeLine(x, y, x - (int) (length * Math.cos(angleForRightArrowPart)), y - (int) (length * Math.sin(angleForRightArrowPart)));
    }

    private static BigDecimal roundCoordinate(double value, int digits) {
        return BigDecimal.valueOf(value).multiply(BigDecimal.TEN.pow(digits)).setScale(
                0, BigDecimal.ROUND_HALF_UP).divide(BigDecimal.TEN.pow(digits));
    }

    protected abstract void drawMousePointOnGraph();

    @Override
    public void draw() {
        /*
        Folgende "Vorarbeit" benötigt jede 2D-Grafikart: Weißen Hintergrund
        zeichnen, Markierungen an den Achsen berechnen und Niveaulinien 
        und Achsen zeichnen. 
         */
        // Weißen Hintergrund zeichnen.
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());
        // Markierungen an den Achsen berechnen.
        computeExpXExpY();
        // Niveaulinien und Achsen zeichnen
        drawAxesAndLines(gc);
    }

}
