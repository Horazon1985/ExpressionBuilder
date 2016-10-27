package graphic;

import abstractexpressions.matrixexpression.classes.Matrix;
import exceptions.EvaluationException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import lang.translator.Translator;

/**
 * Abstrakte Oberklasse aller Grafikklassen für zweidimensionale Grafiken.
 */
public abstract class AbstractGraphicPanel2D extends AbstractGraphicPanel {

    /**
     * Variablenname für 2D-Graphen: varAbsc = Abszissenname.
     */
    protected String varAbsc = null;

    /**
     * Variablenname für 2D-Graphen: varOrd = Ordinatenname.
     */
    protected String varOrd = null;

    final static Color[] FIXED_COLORS = {Color.blue, Color.green, Color.orange, Color.red, Color.PINK};

    protected double axeCenterX, axeCenterY;
    protected double maxX, maxY;
    protected int expX, expY;

    protected boolean movable = false;

    protected boolean pointsAreShowable = true;
    protected int mouseCoordinateX = -1;
    protected int mouseCoordinateY = -1;

    protected double[][] specialPoints;

    protected Point lastMousePosition;
    protected double zoomfactor, zoomfactorX, zoomfactorY;

    public AbstractGraphicPanel2D() {
    }

    public AbstractGraphicPanel2D(final double maxZoomfactor, final double minZoomfactor) {

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastMousePosition = e.getPoint();
                    movable = true;
                } else {
                    lastMousePosition = e.getPoint();
                    movable = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (movable) {
                    axeCenterX += (lastMousePosition.x - e.getPoint().x) * maxX / 250;
                    axeCenterY += (-lastMousePosition.y + e.getPoint().y) * maxY / 250;
                    lastMousePosition = e.getPoint();
                    repaint();
                } else {
                    if (lastMousePosition.x - e.getPoint().x >= 0 && zoomfactorX < maxZoomfactor
                            || lastMousePosition.x - e.getPoint().x <= 0 && zoomfactorX > minZoomfactor) {
                        maxX = maxX * Math.pow(1.02, lastMousePosition.x - e.getPoint().x);
                        zoomfactorX = zoomfactorX * Math.pow(1.02, lastMousePosition.x - e.getPoint().x);
                    }
                    if (lastMousePosition.y - e.getPoint().y >= 0 && zoomfactorY < maxZoomfactor
                            || lastMousePosition.y - e.getPoint().y <= 0 && zoomfactorY > minZoomfactor) {
                        maxY = maxY * Math.pow(1.02, lastMousePosition.y - e.getPoint().y);
                        zoomfactorY = zoomfactorY * Math.pow(1.02, lastMousePosition.y - e.getPoint().y);
                    }
                    lastMousePosition = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseCoordinateX = e.getPoint().x;
                mouseCoordinateY = e.getPoint().y;
                repaint();
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Der Zoomfaktor darf höchstens maxZoomfactor sein (und mindestens minZoomfactor)
                if ((e.getWheelRotation() >= 0 && zoomfactor < maxZoomfactor)
                        || (e.getWheelRotation() <= 0 && zoomfactor > minZoomfactor)) {
                    maxX *= Math.pow(1.1, e.getWheelRotation());
                    maxY *= Math.pow(1.1, e.getWheelRotation());
                    zoomfactor *= Math.pow(1.1, e.getWheelRotation());
                    computeExpXExpY();
                    repaint();
                }

                repaint();
            }
        });
    }

    public boolean getPointsAreShowable() {
        return this.pointsAreShowable;
    }

    public void setPointsAreShowable(boolean pointsAreShowable) {
        this.pointsAreShowable = pointsAreShowable;
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
     * Liefert eine ArrayList mit Bedienungsanweisungen für zweidimensionale
     * Grafiken.
     */
    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    /**
     * Erzeugt eine neue Zufallsfarbe.
     */
    protected Color generateColor() {
        return new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
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
    protected void drawAxesAndLines(Graphics g) {
        g.setColor(Color.lightGray);

        int linePosition;
        int k = (int) (this.axeCenterX * Math.pow(10, -this.expX));

        // x-Niveaulinien zeichnen
        linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];

        while (linePosition <= 500) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
                g.drawLine(linePosition, 0, linePosition, 500);

                if ((250 * this.axeCenterY / this.maxY - 3 <= 248) && (250 * this.axeCenterY / this.maxY - 3 >= -230)) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
                } else if (250 * this.axeCenterY / this.maxY - 3 > 248) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 495);
                } else {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 20);
                }

            }

            k++;

        }

        k = (int) (this.axeCenterX * Math.pow(10, -this.expX)) - 1;
        linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];

        while (linePosition >= 0) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
                g.drawLine(linePosition, 0, linePosition, 500);

                if ((250 * this.axeCenterY / this.maxY - 3 <= 248) && (250 * this.axeCenterY / this.maxY - 3 >= -230)) {
                    g.drawString(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
                } else if (250 * this.axeCenterY / this.maxY - 3 > 248) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 495);
                } else {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 20);
                }

            }

            k--;

        }

        // y-Niveaulinien zeichnen
        k = (int) (this.axeCenterY * Math.pow(10, -this.expY));
        linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];

        while (linePosition >= 0) {
            linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
            g.drawLine(0, linePosition, 500, linePosition);

            if ((250 * this.axeCenterX / this.maxX - 3 >= -225) && (250 * this.axeCenterX / this.maxX - 3 <= 245)) {
                g.drawString(String.valueOf(roundAxisEntries(k, expY)), 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, linePosition - 3);
            } else if (250 * this.axeCenterX / this.maxX - 3 >= -225) {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 5, linePosition - 3);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 475, linePosition - 3);
            }

            k++;

        }

        k = (int) (this.axeCenterY * Math.pow(10, -this.expY)) - 1;
        linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];

        while (linePosition <= 500) {
            linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
            g.drawLine(0, linePosition, 500, linePosition);

            if ((250 * this.axeCenterX / this.maxX - 3 >= -225) && (250 * this.axeCenterX / this.maxX - 3 <= 245)) {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, linePosition - 3);
            } else if (250 * this.axeCenterX / this.maxX - 3 >= -225) {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 5, linePosition - 3);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 475, linePosition - 3);
            }

            k--;

        }

        // Achsen inkl. Achsenbezeichnungen eintragen
        // Achsen
        g.setColor(Color.black);
        g.drawLine(0, 250 + (int) (250 * this.axeCenterY / this.maxY), 500, 250 + (int) (250 * this.axeCenterY / this.maxY));
        g.drawLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX), 500);
        // Achsenpfeile
        g.drawLine(500, 250 + (int) (250 * this.axeCenterY / this.maxY), 494, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
        g.drawLine(500, 250 + (int) (250 * this.axeCenterY / this.maxY), 494, 250 + (int) (250 * this.axeCenterY / this.maxY) + 3);
        g.drawLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, 6);
        g.drawLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX) - 3, 6);
        /*
         Achsenbeschriftung. WICHTIG: In der Prozedur drawString werden die
         Achsenbeschriftung derart eingetragen, dass (1) Die Beschriftung der
         Variablen var innerhalb des Bildschirms liegt (5 px) (2) Die
         Beschriftung f(var) links von der vertikalen Achse liegt (5 px)
         Hierzu müssen die Pixellängen der gezeichneten Strings ausgerechnet
         werden (mittels g.getFontMetrics().stringWidth()).
         */
        if (this.varAbsc == null) {
            g.drawString("1. axis", 500 - 5 - g.getFontMetrics().stringWidth("1. axis"), 250 + (int) (250 * this.axeCenterY / this.maxY) + 15);
        } else {
            g.drawString(this.varAbsc, 500 - 5 - g.getFontMetrics().stringWidth(this.varAbsc), 250 + (int) (250 * this.axeCenterY / this.maxY) + 15);
        }
        if (this.varOrd == null) {
            g.drawString("2. axis", 250 - (int) (250 * this.axeCenterX / this.maxX) - 5 - g.getFontMetrics().stringWidth("2. axis"), 20);
        } else {
            g.drawString(this.varOrd, 250 - (int) (250 * this.axeCenterX / this.maxX) - 5 - g.getFontMetrics().stringWidth(this.varOrd), 20);
        }
    }

    /**
     * Zeichnet rote Punkte an gegebenen Stellen specialPoints (etwa Markierung
     * von Nullstellen etc).
     */
    protected void drawSpecialPoints(Graphics g, double[][] specialPoints) {
        g.setColor(Color.red);
        if (specialPoints == null) {
            return;
        }
        int[][] specialPointsCoordinates = new int[specialPoints.length][2];
        for (int i = 0; i < specialPoints.length; i++) {
            specialPointsCoordinates[i] = convertToPixel(specialPoints[i][0], specialPoints[i][1]);
            g.fillOval(specialPointsCoordinates[i][0] - 3, specialPointsCoordinates[i][1] - 3, 7, 7);
        }
    }

    /**
     * Zeichnet einen roten Punkt an der Stelle (x, y), wobei x und y die
     * Pixelkoordinaten des Punktes sind.
     */
    protected void drawCirclePoint(Graphics g, int x, int y, boolean printCoordinates) {
        g.setColor(Color.red);
        g.fillOval(x - 3, y - 3, 7, 7);
        if (!printCoordinates) {
            return;
        }
        g.setColor(Color.black);
        String coordinates = "(" + roundCoordinate(convertToEuclideanCoordinateX(x), 2) + ", "
                + roundCoordinate(convertToEuclideanCoordinateY(y), 2) + ")";
        int length = g.getFontMetrics().stringWidth(coordinates);
        if (x >= length) {
            if (y >= 20) {
                g.drawString(coordinates, x - length, y - 5);
            } else {
                g.drawString(coordinates, x - length, y + 20);
            }
        } else if (y >= 20) {
            g.drawString(coordinates, x, y - 5);
        } else {
            g.drawString(coordinates, x, y + 20);
        }
    }

    /**
     * Zeichnet einen Vektorpfeil für den Vektor [a; b] an der Stelle (x, y),
     * wobei x und y die Pixelkoordinaten des Punktes sind.
     */
    protected void drawVectorLine(Graphics g, int x, int y, int endPointVectorX, int endPointVectorY, boolean printCoordinates) {
        g.setColor(Color.black);
        String coordinates = "(" + roundCoordinate(convertToEuclideanCoordinateX(endPointVectorX), 2) + ", "
                + roundCoordinate(convertToEuclideanCoordinateY(endPointVectorY), 2) + ")";
        int length = g.getFontMetrics().stringWidth(coordinates);
        g.drawLine(x, y, endPointVectorX, endPointVectorY);
        if (endPointVectorX >= length) {
            if (endPointVectorY >= 20) {
                g.drawString(coordinates, endPointVectorX - length, endPointVectorY - 5);
            } else {
                g.drawString(coordinates, endPointVectorX - length, endPointVectorY + 20);
            }
        } else if (endPointVectorY >= 20) {
            g.drawString(coordinates, endPointVectorX, endPointVectorY - 5);
        } else {
            g.drawString(coordinates, endPointVectorX, endPointVectorY + 20);
        }
    }

    protected void drawVectorArrow(Graphics g, int x, int y, int length, double angleOfArrow) {
        if (angleOfArrow == -1) {
            return;
        }
        double angleForLeftArrowPart = angleOfArrow - Math.PI / 4, angleForRightArrowPart = angleOfArrow + Math.PI / 4;
        g.drawLine(x, y, x - (int) (length * Math.cos(angleForLeftArrowPart)), y - (int) (length * Math.sin(angleForLeftArrowPart)));
        g.drawLine(x, y, x - (int) (length * Math.cos(angleForRightArrowPart)), y - (int) (length * Math.sin(angleForRightArrowPart)));
    }
    
    private static BigDecimal roundCoordinate(double value, int digits) {
        return BigDecimal.valueOf(value).multiply(BigDecimal.TEN.pow(digits)).setScale(
                0, BigDecimal.ROUND_HALF_UP).divide(BigDecimal.TEN.pow(digits));
    }

    protected abstract void drawMousePointOnGraph(Graphics g);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        /*
        Folgende "Vorarbeit" benötigt jede 2D-Grafikart: Weißen Hintergrund
        zeichnen, Markierungen an den Achsen berechnen und Niveaulinien 
        und Achsen zeichnen. 
         */
        // Weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);
        // Markierungen an den Achsen berechnen.
        computeExpXExpY();
        // Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g);
    }

}
