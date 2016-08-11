package graphic;

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
 * Abstrakte Oberklasse aller Grafikklassen für dreidimensionale Grafiken.
 */
public abstract class AbstractGraphicPanel3D extends AbstractGraphicPanel implements Runnable {

    /**
     * Boolsche Variable, die angibt, ob der Graph gerade rotiert oder nicht.
     */
    protected boolean isRotating;

    /**
     * Großer Radius für die Grundellipse.
     */
    protected double bigRadius;
    /**
     * Kleiner Radius für die Grundellipse.
     */
    protected double smallRadius;

    protected double height, heightProjection;
    /**
     * Horizontaler Winkel des Graphen gegen den Uhrzeigersinn (er wird
     * inkrementiert, wenn der Graph im Uhrzeigersinn rotiert).
     */
    protected double angle;
    /**
     * Der Winkel, unter dem man die dritte Achse sieht. Ist der Wert man gleich
     * 0, so schaut man seitlich auf den Graphen. Ist der Wert 90, so schaut man
     * von oben auf den Graphen.
     */
    protected double verticalAngle;

    /**
     * Der boolsche Wert von isAngleMeant ist genau dann true, wenn der aktuelle
     * Winkel, welcher durch das Auslösen eines MouseEvent verändert wird, angle
     * ist. Ansonsten ist es verticalAngle.
     */
    protected boolean isAngleMeant = true;
    protected Point lastMousePosition;

    protected double zoomfactor;

    protected double axeCenterX, axeCenterY;
    protected double maxX, maxY, maxZ;
    protected double maxXOrigin, maxYOrigin, maxZOrigin;
    protected int expX, expY, expZ;

    protected static PresentationMode presentationMode = PresentationMode.WHOLE_GRAPH;
    protected static BackgroundColorMode backgroundColorMode = BackgroundColorMode.BRIGHT;

    public enum BackgroundColorMode {

        BRIGHT, DARK;

    }

    public enum PresentationMode {

        WHOLE_GRAPH, GRID_ONLY;

    }

    public static BackgroundColorMode getBackgroundColorMode() {
        return backgroundColorMode;
    }

    public static void setBackgroundColorMode(BackgroundColorMode bgColorMode) {
        backgroundColorMode = bgColorMode;
    }

    public static PresentationMode getPresentationMode() {
        return presentationMode;
    }

    public static void setPresentationMode(PresentationMode prMode) {
        presentationMode = prMode;
    }

    public boolean getIsRotating() {
        return this.isRotating;
    }

    public void setIsRotating(boolean isRotating) {
        this.isRotating = isRotating;
    }

    public AbstractGraphicPanel3D() {
        this.isRotating = false;
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isAngleMeant = true;
                    lastMousePosition = e.getPoint();
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isAngleMeant = false;
                    lastMousePosition = e.getPoint();
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
                if (isAngleMeant) {
                    angle += (lastMousePosition.x - e.getPoint().x) * 0.5;

                    if (angle >= 360) {
                        angle = angle - 360;
                    }
                    if (angle < 0) {
                        angle = angle + 360;
                    }

                    lastMousePosition = e.getPoint();
                    repaint();
                } else {
                    verticalAngle -= (lastMousePosition.y - e.getPoint().y) * 0.3;

                    if (verticalAngle >= 90) {
                        verticalAngle = 90;
                    }
                    if (verticalAngle < 1) {
                        verticalAngle = 1;
                    }

                    smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
                    height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);

                    lastMousePosition = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Der Zoomfaktor darf höchstens 10 sein (und mindestens 0.1)
                if (((e.getWheelRotation() >= 0) && (zoomfactor < 10))
                        || ((e.getWheelRotation() <= 0) && (zoomfactor > 0.1))) {
                    maxX *= Math.pow(1.1, e.getWheelRotation());
                    maxY *= Math.pow(1.1, e.getWheelRotation());
                    maxZ *= Math.pow(1.1, e.getWheelRotation());
                    zoomfactor *= Math.pow(1.1, e.getWheelRotation());
                    repaint();
                }
            }
        });

    }

    /**
     * Berechnet die Attribute expX bzw. expY bzw. expZ, die größten Exponenten
     * für eine Zehnerpotenz, die kleiner oder gleich maxX bzw. maxY bzw. maxZ
     * sind. VORAUSSETZUNG: der Graph ist bereits initialisiert (und mit
     * Funktionswerten gefüllt). Die Attribute maxX, maxY und maxZ sind
     * initialisiert.
     */
    protected void computeExpXExpYExpZ() {

        this.expX = 0;
        this.expY = 0;
        this.expZ = 0;

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

        if (this.maxZ >= 1) {
            while (this.maxZ / Math.pow(10, this.expZ) >= 1) {
                this.expZ++;
            }
            this.expZ--;
        } else {
            while (this.maxZ / Math.pow(10, this.expZ) < 1) {
                this.expZ--;
            }
        }

    }

    /**
     * Berechnet aus Punktkoordinaten (x, y, z) Koordinaten (x', y') für die
     * graphische Darstellung.
     */
    protected int[] convertToPixel(double x, double y, double z) {

        double angleAbsc = getGraphicalAngle(this.bigRadius, this.smallRadius, this.angle);
        double angleOrd;
        if (this.angle < 90) {
            angleOrd = getGraphicalAngle(this.bigRadius, this.smallRadius, this.angle + 270);
        } else {
            angleOrd = getGraphicalAngle(this.bigRadius, this.smallRadius, this.angle - 90);
        }

        // pixel sind die Pixelkoordinaten für die Graphische Darstellung von (x, y, z)
        int[] pixel = new int[2];

        // Berechnung von pixels[0]
        double x_1, x_2;

        if (angleAbsc == 0) {
            x_1 = this.bigRadius * x / this.maxX;
            x_2 = 0;
        } else if (angleAbsc == 90) {
            x_1 = 0;
            x_2 = this.bigRadius * y / this.maxY;
        } else if (angleAbsc == 180) {
            x_1 = -this.bigRadius * x / this.maxX;
            x_2 = 0;
        } else if (angleAbsc == 270) {
            x_1 = 0;
            x_2 = -this.bigRadius * y / this.maxY;
        } else if (angleAbsc < 90) {
            x_1 = (x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = (y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else if (angleAbsc < 180) {
            x_1 = -(x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = (y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else if (angleAbsc < 270) {
            x_1 = -(x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = -(y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else {
            x_1 = (x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = -(y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        }

        pixel[0] = (int) (250 + x_1 + x_2);

        // Berechnung von pixel[1]
        double y_1, y_2, y_3;

        if (angleAbsc == 0) {
            y_1 = 0;
            y_2 = -this.smallRadius * y / this.maxY;
        } else if (angleAbsc == 90) {
            y_1 = this.smallRadius * x / this.maxX;
            y_2 = 0;
        } else if (angleAbsc == 180) {
            y_1 = 0;
            y_2 = this.smallRadius * y / this.maxY;
        } else if (angleAbsc == 270) {
            y_1 = -this.smallRadius * x / this.maxX;
            y_2 = 0;
        } else {
            y_1 = x_1 * Math.tan(angleAbsc * Math.PI / 180);
            y_2 = x_2 * Math.tan(angleOrd * Math.PI / 180);
        }

        /* 
        Maximaler Funktionswert (also maxZ) soll h Pixel betragen. Deshalb die 
        folgende Skalierung.
         */
        y_3 = -this.height * z / this.maxZ;
        pixel[1] = (int) (250 + y_1 + y_2 + y_3);

        return pixel;

    }

    /**
     * Berechnet aus dem Winkelattribut angle den Winkel, welcher in der
     * graphischen Darstellung auftaucht, und gibt diesen zurück.
     */
    private double getGraphicalAngle(double bigRadius, double smallRadius, double angle) {
        // Voraussetzung: 0 <= angle < 360
        if ((angle == 0) || (angle == 90) || (angle == 180) || (angle == 270)) {
            return angle;
        } else if (angle < 90) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI;
        } else if (angle < 180) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        } else if (angle < 270) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        }
        return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 360;
    }

    /**
     * Zeichnet Niveaulinien am östlichen Rand des Graphen.<br>
     * VORAUSSETZUNG: maxX, maxY, maxZ, bigRadius, smallRadius, height, angle
     * sind initialisiert.
     */
    protected void drawLevelsOnEast(Graphics g, String varAbsc, String varOrd, String varAppl) {

        if (this.angle >= 0 && this.angle <= 180) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxXOrigin;
        border[0][1] = this.maxYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = this.maxXOrigin;
        border[1][1] = -this.maxYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = this.maxXOrigin;
        border[2][1] = -this.maxYOrigin;
        border[2][2] = -this.maxZOrigin;
        border[3][0] = this.maxXOrigin;
        border[3][1] = this.maxYOrigin;
        border[3][2] = -this.maxZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 270) {
            if (varOrd == null) {
                g.drawString("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                g.drawString(varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varOrd == null) {
            g.drawString("2. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        } else {
            g.drawString(varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(varOrd) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZOrigin / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = this.maxXOrigin;
            lineLevel[0][1] = this.maxYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = -this.maxYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 270 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZOrigin) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxYOrigin / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= this.maxYOrigin) {
            lineLevel[0][0] = this.maxXOrigin;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -this.maxZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle >= 270) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    /**
     * Zeichnet Niveaulinien am westlichen Rand des Graphen.<br>
     * VORAUSSETZUNG: maxX, maxY, maxZ, bigRadius, smallRadius, height, angle
     * sind initialisiert.
     */
    protected void drawLevelsOnWest(Graphics g, String varAbsc, String varOrd, String varAppl) {

        if (this.angle >= 180 && this.angle <= 360) {
            return;
        }

        System.out.println("maxValueAbsc = " + this.maxX + ", " + "maxValueOrd = " + this.maxY + ", " + "maxValueAppl = " + this.maxZ);
        
        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -this.maxXOrigin;
        border[0][1] = -this.maxYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = -this.maxXOrigin;
        border[1][1] = this.maxYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = -this.maxXOrigin;
        border[2][1] = this.maxYOrigin;
        border[2][2] = -this.maxZOrigin;
        border[3][0] = -this.maxXOrigin;
        border[3][1] = -this.maxYOrigin;
        border[3][2] = -this.maxZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 90) {
            if (varOrd == null) {
                g.drawString("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                g.drawString(varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varOrd == null) {
            g.drawString("2. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        } else {
            g.drawString(varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(varOrd) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZOrigin / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = -this.maxXOrigin;
            lineLevel[0][1] = -this.maxYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = -this.maxXOrigin;
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 90 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZOrigin) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxYOrigin / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= this.maxYOrigin) {
            lineLevel[0][0] = -this.maxXOrigin;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = -this.maxXOrigin;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -this.maxZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle >= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    /**
     * Zeichnet Niveaulinien am südlichen Rand des Graphen.<br>
     * VORAUSSETZUNG: maxX, maxY, maxZ, bigRadius, smallRadius, height, angle
     * sind initialisiert.
     */
    protected void drawLevelsOnSouth(Graphics g, String varAbsc, String varOrd, String varAppl) {

        if (this.angle <= 90 || this.angle >= 270) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxXOrigin;
        border[0][1] = -this.maxYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = -this.maxXOrigin;
        border[1][1] = -this.maxYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = -this.maxXOrigin;
        border[2][1] = -this.maxYOrigin;
        border[2][2] = -this.maxZOrigin;
        border[3][0] = this.maxXOrigin;
        border[3][1] = -this.maxYOrigin;
        border[3][2] = -this.maxZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 180) {
            if (varAbsc == null) {
                g.drawString("1. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                g.drawString(varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varAbsc == null) {
            g.drawString("1. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("1. axis") - 10, borderPixels[0][1] + 15);
        } else {
            g.drawString(varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(varAbsc) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZOrigin / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = this.maxXOrigin;
            lineLevel[0][1] = -this.maxYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = -this.maxXOrigin;
            lineLevel[1][1] = -this.maxYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 180 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZOrigin) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxXOrigin / Math.pow(10, this.expX));
        i = -bound;

        while (i * Math.pow(10, this.expX) <= this.maxXOrigin) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = -this.maxYOrigin;
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = -this.maxYOrigin;
            lineLevel[1][2] = -this.maxZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle >= 180) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expX))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    /**
     * Zeichnet Niveaulinien am nördlichen Rand des Graphen.<br>
     * VORAUSSETZUNG: maxX, maxY, maxZ, bigRadius, smallRadius, height, angle
     * sind initialisiert.
     */
    protected void drawLevelsOnNorth(Graphics g, String varAbsc, String varOrd, String varAppl) {

        if (this.angle >= 90 && this.angle <= 270) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -this.maxXOrigin;
        border[0][1] = this.maxYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = this.maxXOrigin;
        border[1][1] = this.maxYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = this.maxXOrigin;
        border[2][1] = this.maxYOrigin;
        border[2][2] = -this.maxZOrigin;
        border[3][0] = -this.maxXOrigin;
        border[3][1] = this.maxYOrigin;
        border[3][2] = -this.maxZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle <= 90) {
            if (varAbsc == null) {
                g.drawString("1. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                g.drawString(varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varAbsc == null) {
            g.drawString("1. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("1. axis") - 10, borderPixels[0][1] + 15);
        } else {
            g.drawString(varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(varAbsc) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZOrigin / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = -this.maxXOrigin;
            lineLevel[0][1] = this.maxYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle < 90 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZOrigin) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxXOrigin / Math.pow(10, this.expX));
        i = -bound;

        while (i * Math.pow(10, this.expX) <= this.maxXOrigin) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = this.maxYOrigin;
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = -this.maxZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle <= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expX))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    /**
     * Zeichnet Niveaulinien am Boden des Graphen.<br>
     * VORAUSSETZUNG: maxX, maxY, maxZ, bigRadius, smallRadius, height, angle
     * sind initialisiert.
     */
    protected void drawLevelsBottom(Graphics g) {

        // Zunächst den Rahmen auf dem Boden zeichnen
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxXOrigin;
        border[0][1] = this.maxYOrigin;
        border[0][2] = -this.maxZOrigin;
        border[1][0] = -this.maxXOrigin;
        border[1][1] = this.maxYOrigin;
        border[1][2] = -this.maxZOrigin;
        border[2][0] = -this.maxXOrigin;
        border[2][1] = -this.maxYOrigin;
        border[2][2] = -this.maxZOrigin;
        border[3][0] = this.maxXOrigin;
        border[3][1] = -this.maxYOrigin;
        border[3][2] = -this.maxZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        // Horizontale x-Niveaulinien zeichnen
        int bound = (int) (this.maxXOrigin / Math.pow(10, this.expX));
        int i = -bound;

        while (i * Math.pow(10, this.expX) <= this.maxXOrigin) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = -this.maxYOrigin;
            lineLevel[0][2] = -this.maxZOrigin;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = -this.maxZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        // Horizontale y-Niveaulinien zeichnen
        bound = (int) (this.maxYOrigin / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= this.maxYOrigin) {
            lineLevel[0][0] = -this.maxXOrigin;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = -this.maxZOrigin;
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -this.maxZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

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
     * Liefert eine ArrayList mit Bedienungsanweisungen für dreidimensionale
     * Grafiken.
     */
    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    @Override
    public void run() {
        while (this.isRotating) {

            this.angle = this.angle + 1;
            if (this.angle >= 360) {
                this.angle = this.angle - 360;
            }
            if (this.angle < 0) {
                this.angle = this.angle + 360;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            repaint();

        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Zunächst Hintergrund zeichnen.
        switch (backgroundColorMode) {
            case BRIGHT:
                g.setColor(Color.white);
                break;
            case DARK:
                g.setColor(Color.black);
                break;
        }
        g.fillRect(0, 0, 500, 500);
        // Markierungen an den Achsen berechnen.
        computeExpXExpYExpZ();
    }
    
}