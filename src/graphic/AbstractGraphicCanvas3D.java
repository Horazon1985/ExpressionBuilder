package graphic;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lang.translator.Translator;

/**
 * Abstrakte Oberklasse aller Grafikklassen für dreidimensionale Grafiken.
 */
public abstract class AbstractGraphicCanvas3D extends AbstractGraphicCanvas implements Runnable {

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

    protected double axeCenterX, axeCenterY, axeCenterZ;
    protected double axeCenterXOrigin, axeCenterYOrigin, axeCenterZOrigin;
    protected double minX, minY, minZ;
    protected double maxX, maxY, maxZ;
    protected double minXOrigin, minYOrigin, minZOrigin;
    protected double maxXOrigin, maxYOrigin, maxZOrigin;
    protected int expX, expY, expZ;

    protected static PresentationMode presentationMode = PresentationMode.WHOLE_GRAPH;
    protected static BackgroundColorMode backgroundColorMode = BackgroundColorMode.BRIGHT;

    protected static final Color gridColorWholeGraphBright = Color.BLACK;
    protected static final Color gridColorWholeGraphDark = Color.GREEN;
    protected static final Color gridColorGridOnlyBright = Color.BLACK;
    protected static final Color gridColorGridOnlyDark = Color.GRAY;

    protected ArrayList<TangentPolygon>[][] abstractGraph3D;
    /**
     * Standardwert für die Anzahl der Unterteilungen des zu zeichnenden
     * Bereichs der x,y-Ebene entlang x und y. Wird mit Veränderung des
     * Zoomfaktors angepasst.
     */
    protected int intervals = 50;

    public static class TangentPolygon implements Comparable<TangentPolygon> {

        private ArrayList<double[]> points = new ArrayList<>();

        public ArrayList<double[]> getPoints() {
            return points;
        }

        public void setPoints(ArrayList<double[]> points) {
            this.points = points;
        }

        public void setPoints(double[]... points) {
            this.points.addAll(Arrays.asList(points));
        }

        public void addPoint(double[] point) {
            this.points.add(point);
        }

        public double getCenterX() {
            double center = 0;
            if (!this.points.isEmpty()) {
                for (double[] point : this.points) {
                    center += point[0];
                }
                center = center / this.points.size();
            }
            return center;
        }

        public double getCenterY() {
            double center = 0;
            if (!this.points.isEmpty()) {
                for (double[] point : this.points) {
                    center += point[1];
                }
                center = center / this.points.size();
            }
            return center;
        }

        public double getCenterZ() {
            double center = 0;
            if (!this.points.isEmpty()) {
                for (double[] point : this.points) {
                    center += point[2];
                }
                center = center / this.points.size();
            }
            return center;
        }

        @Override
        public int compareTo(TangentPolygon polygon) {
            double centerZ = getCenterZ();
            double centerZPolygon = polygon.getCenterZ();
            if (centerZ < centerZPolygon) {
                return 1;
            }
            if (centerZ > centerZPolygon) {
                return -1;
            }
            return 0;
        }

        @Override
        public String toString() {
            String polygon = "[";
            for (int i = 0; i < this.points.size(); i++) {
                polygon += "(";
                for (int j = 0; j < this.points.get(i).length; j++) {
                    polygon += this.points.get(i)[j];
                    if (j < this.points.get(i).length - 1) {
                        polygon += ", ";
                    }
                }
                polygon += ")";
                if (i < this.points.size() - 1) {
                    polygon += ", ";
                }
            }
            return polygon + "]";
        }

    }

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

    public AbstractGraphicCanvas3D() {
        this.isRotating = false;
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    isAngleMeant = true;
                    lastMousePosition = new Point((int) event.getX(),(int) event.getY());
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    isAngleMeant = false;
                    lastMousePosition = new Point((int) event.getX(),(int) event.getY());
                }
            }
        });

//        addMouseMotionListener(new MouseMotionListener() {
//
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                if (isAngleMeant) {
//                    angle += (lastMousePosition.x - e.getPoint().x) * 0.5;
//
//                    if (angle >= 360) {
//                        angle = angle - 360;
//                    }
//                    if (angle < 0) {
//                        angle = angle + 360;
//                    }
//
//                    lastMousePosition = e.getPoint();
//                    repaint();
//                } else {
//                    verticalAngle -= (lastMousePosition.y - e.getPoint().y) * 0.3;
//
//                    if (verticalAngle >= 90) {
//                        verticalAngle = 90;
//                    }
//                    if (verticalAngle < 1) {
//                        verticalAngle = 1;
//                    }
//
//                    smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
//                    height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);
//
//                    lastMousePosition = e.getPoint();
//                    repaint();
//                }
//            }
//
//            @Override
//            public void mouseMoved(MouseEvent e) {
//            }
//        });
//
//        addMouseWheelListener(new MouseWheelListener() {
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                // Der Zoomfaktor darf höchstens 10 sein (und mindestens 0.1)
//                if (e.getWheelRotation() >= 0 && zoomfactor < 10
//                        || e.getWheelRotation() <= 0 && zoomfactor > 0.1) {
//                    minX *= Math.pow(1.1, e.getWheelRotation());
//                    minY *= Math.pow(1.1, e.getWheelRotation());
//                    minZ *= Math.pow(1.1, e.getWheelRotation());
//                    maxX *= Math.pow(1.1, e.getWheelRotation());
//                    maxY *= Math.pow(1.1, e.getWheelRotation());
//                    maxZ *= Math.pow(1.1, e.getWheelRotation());
//                    axeCenterX *= Math.pow(1.1, e.getWheelRotation());
//                    axeCenterY *= Math.pow(1.1, e.getWheelRotation());
//                    axeCenterZ *= Math.pow(1.1, e.getWheelRotation());
//                    zoomfactor *= Math.pow(1.1, e.getWheelRotation());
//
//                    intervals = (int) (50 * zoomfactor);
//
//                    if (intervals > 50) {
//                        intervals = 50;
//                    }
//                    if (intervals < 2) {
//                        intervals = 2;
//                    }
//
//                    repaint();
//                }
//            }
//        });

    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.
     */
    protected void computeScreenSizes(ArrayList<double[][][]> graphs, boolean paddingForX, boolean paddingForY, boolean paddingForZ) {

        if (graphs.isEmpty()) {

            this.minX = -1;
            this.minY = -1;
            this.minZ = -1;
            this.maxX = 1;
            this.maxY = 1;
            this.maxZ = 1;

        } else {

            boolean firstDefinedPointAlongXFound;
            boolean firstDefinedPointAlongYFound;
            boolean firstDefinedPointAlongZFound;

            for (int k = 0; k < graphs.size(); k++) {

                firstDefinedPointAlongXFound = false;
                firstDefinedPointAlongYFound = false;
                firstDefinedPointAlongZFound = false;

                for (int i = 0; i < graphs.get(k).length; i++) {
                    for (int j = 0; j < graphs.get(k)[0].length; j++) {
                        if (!Double.isNaN(graphs.get(k)[i][j][0]) || !Double.isInfinite(graphs.get(k)[i][j][0])) {
                            if (!firstDefinedPointAlongXFound) {
                                this.minX = graphs.get(k)[i][j][0];
                                this.maxX = graphs.get(k)[i][j][0];
                                firstDefinedPointAlongXFound = true;
                            } else {
                                this.minX = Math.min(this.minX, graphs.get(k)[i][j][0]);
                                this.maxX = Math.max(this.maxX, graphs.get(k)[i][j][0]);
                            }
                        }
                        if (!Double.isNaN(graphs.get(k)[i][j][1]) || !Double.isInfinite(graphs.get(k)[i][j][1])) {
                            if (!firstDefinedPointAlongYFound) {
                                this.minY = graphs.get(k)[i][j][1];
                                this.maxY = graphs.get(k)[i][j][1];
                                firstDefinedPointAlongYFound = true;
                            } else {
                                this.minY = Math.min(this.minY, graphs.get(k)[i][j][1]);
                                this.maxY = Math.max(this.maxY, graphs.get(k)[i][j][1]);
                            }
                        }
                        if (!Double.isNaN(graphs.get(k)[i][j][2]) || !Double.isInfinite(graphs.get(k)[i][j][2])) {
                            if (!firstDefinedPointAlongZFound) {
                                this.minZ = graphs.get(k)[i][j][2];
                                this.maxZ = graphs.get(k)[i][j][2];
                                firstDefinedPointAlongZFound = true;
                            } else {
                                this.minZ = Math.min(this.minZ, graphs.get(k)[i][j][2]);
                                this.maxZ = Math.max(this.maxZ, graphs.get(k)[i][j][2]);
                            }
                        }
                    }
                }

            }

            // 30 % Rand auf jeder der Achsen lassen!
            if (paddingForX) {
                this.maxX = this.maxX + 0.3 * (this.maxX - this.minX);
                this.minX = this.minX - 0.3 * (this.maxX - this.minX);
            }
            if (paddingForY) {
                this.maxY = this.maxY + 0.3 * (this.maxY - this.minY);
                this.minY = this.minY - 0.3 * (this.maxY - this.minY);
            }
            if (paddingForZ) {
                this.maxZ = this.maxZ + 0.3 * (this.maxZ - this.minZ);
                this.minZ = this.minZ - 0.3 * (this.maxZ - this.minZ);
            }

        }

        if (this.minX == this.maxX) {
            this.maxX = this.maxX + 1;
            this.minX = this.minX - 1;
        }
        if (this.minY == this.maxY) {
            this.maxY = this.maxY + 1;
            this.minY = this.minY - 1;
        }
        if (this.minZ == this.maxZ) {
            this.maxZ = this.maxZ + 1;
            this.minZ = this.minZ - 1;
        }

        this.minXOrigin = this.minX;
        this.minYOrigin = this.minY;
        this.minZOrigin = this.minZ;
        this.maxXOrigin = this.maxX;
        this.maxYOrigin = this.maxY;
        this.maxZOrigin = this.maxZ;

        this.axeCenterX = (this.minX + this.maxX) / 2;
        this.axeCenterY = (this.minY + this.maxY) / 2;
        this.axeCenterZ = (this.minZ + this.maxZ) / 2;
        this.axeCenterXOrigin = this.axeCenterX;
        this.axeCenterYOrigin = this.axeCenterY;
        this.axeCenterZOrigin = this.axeCenterZ;

    }

    /**
     * Berechnet die Maße Darstellungsbereichs eines einzelnen Graphen.
     */
    protected void computeScreenSizes(double[][][] graph, boolean paddingForX, boolean paddingForY, boolean paddingForZ) {
        ArrayList<double[][][]> graphs = new ArrayList<>();
        graphs.add(graph);
        computeScreenSizes(graphs, paddingForX, paddingForY, paddingForZ);
    }

    /**
     * Berechnet die Attribute expX bzw. expY bzw. expZ, die größten Exponenten
     * für eine Zehnerpotenz, die kleiner oder gleich maxX bzw. maxY bzw. maxZ
     * sind.<br>
     * VORAUSSETZUNG: der Graph ist bereits initialisiert (und mit
     * Funktionswerten gefüllt). Die Attribute maxX, maxY, maxZ, ... sind
     * initialisiert.
     */
    protected void computeExpXExpYExpZ() {
        this.expX = getSuitableExponent((Math.max(Math.abs(this.maxXOrigin), Math.abs(this.minXOrigin))) / this.zoomfactor);
        this.expY = getSuitableExponent((Math.max(Math.abs(this.maxYOrigin), Math.abs(this.minYOrigin))) / this.zoomfactor);
        this.expZ = getSuitableExponent((Math.max(Math.abs(this.maxZOrigin), Math.abs(this.minZOrigin))) / this.zoomfactor);
    }

    private static int getSuitableExponent(double a) {
        if (a == 0) {
            return 0;
        }
        int exponent = 0;
        if (a >= 1) {
            while (a / Math.pow(10, exponent) >= 1) {
                exponent++;
            }
            exponent--;
        } else {
            while (a / Math.pow(10, exponent) < 1) {
                exponent--;
            }
        }
        return exponent;
    }

    /**
     * Berechnet die Farbe eines kleinen Graphenplättchens in Abhängigkeit von
     * seiner Position auf der z-Achse.
     */
    protected Color computeColor(Color groundColor, double minZ, double maxZ, double height) {

        int red, green, blue;

        int r = (int) (255 * groundColor.getRed());
        int g = (int) (255 * groundColor.getGreen());
        int b = (int) (255 * groundColor.getBlue());

        if (minZ == maxZ) {
            red = r;
            green = g;
            blue = b;
        } else {
            red = Math.min(255, r - (int) (60 * Math.sin(this.angle / 180 * Math.PI)));
            green = Math.min(255, g + (int) ((255 - g) * (height - minZ) / (maxZ - minZ)));
            blue = Math.min(255, b + (int) (60 * Math.sin(this.angle / 180 * Math.PI)));
        }
        if (r < 0) {
            r = 0;
        }
        if (g < 0) {
            g = 0;
        }
        if (b < 0) {
            b = 0;
        }

        return Color.rgb(red, green, blue);

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
            x_1 = 2 * this.bigRadius * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin);
            x_2 = 0;
        } else if (angleAbsc == 90) {
            x_1 = 0;
            x_2 = 2 * this.bigRadius * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin);
        } else if (angleAbsc == 180) {
            x_1 = -2 * this.bigRadius * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin);
            x_2 = 0;
        } else if (angleAbsc == 270) {
            x_1 = 0;
            x_2 = -2 * this.bigRadius * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin);
        } else if (angleAbsc < 90) {
            x_1 = (2 * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = (2 * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else if (angleAbsc < 180) {
            x_1 = -(2 * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = (2 * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else if (angleAbsc < 270) {
            x_1 = -(2 * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = -(2 * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else {
            x_1 = (2 * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = -(2 * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin)) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        }

        pixel[0] = (int) (250 + this.zoomfactor * (x_1 + x_2));

        // Berechnung von pixel[1]
        double y_1, y_2, y_3;

        if (angleAbsc == 0) {
            y_1 = 0;
            y_2 = -2 * this.smallRadius * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin);
        } else if (angleAbsc == 90) {
            y_1 = 2 * this.smallRadius * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin);
            y_2 = 0;
        } else if (angleAbsc == 180) {
            y_1 = 0;
            y_2 = 2 * this.smallRadius * (y - this.axeCenterYOrigin) / (this.maxYOrigin - this.minYOrigin);
        } else if (angleAbsc == 270) {
            y_1 = -2 * this.smallRadius * (x - this.axeCenterXOrigin) / (this.maxXOrigin - this.minXOrigin);
            y_2 = 0;
        } else {
            y_1 = x_1 * Math.tan(angleAbsc * Math.PI / 180);
            y_2 = x_2 * Math.tan(angleOrd * Math.PI / 180);
        }

        /* 
        Maximaler Funktionswert (also maxZ) soll h Pixel betragen. Deshalb die 
        folgende Skalierung.
         */
        y_3 = -this.height * (z - this.axeCenterZOrigin) / (this.maxZOrigin - this.axeCenterZOrigin);

        pixel[1] = (int) (250 + this.zoomfactor * (y_1 + y_2 + y_3));

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
     * Zeichnet ein (tangentiales) viereckiges Plättchen des 3D-Graphen.
     */
    protected void drawInfinitesimalTangentSpace(TangentPolygon p, GraphicsContext gc, Color c) {

        GeneralPath tangent = new GeneralPath(GeneralPath.WIND_EVEN_ODD, p.getPoints().size());
        if (p.getPoints().isEmpty()) {
            return;
        }

        int[] pixel = convertToPixel(p.getPoints().get(0)[0], p.getPoints().get(0)[1], p.getPoints().get(0)[2]);
        tangent.moveTo(pixel[0], pixel[1]);
        for (int k = 1; k < p.getPoints().size(); k++) {
            pixel = convertToPixel(p.getPoints().get(k)[0], p.getPoints().get(k)[1], p.getPoints().get(k)[2]);
            tangent.lineTo(pixel[0], pixel[1]);
        }
        tangent.closePath();
        Graphics2D g2 = (Graphics2D) g;

        if (presentationMode.equals(PresentationMode.WHOLE_GRAPH)) {
            g2.setPaint(c);
            g2.fill(tangent);
        }

        switch (backgroundColorMode) {
            case BRIGHT:
                switch (presentationMode) {
                    case WHOLE_GRAPH:
                        g2.setPaint(gridColorWholeGraphBright);
                        break;
                    case GRID_ONLY:
                        g2.setPaint(gridColorGridOnlyBright);
                        break;
                }
                break;
            case DARK:
                switch (presentationMode) {
                    case WHOLE_GRAPH:
                        g2.setPaint(gridColorWholeGraphDark);
                        break;
                    case GRID_ONLY:
                        g2.setPaint(gridColorGridOnlyDark);
                        break;
                }
                break;
        }

        g2.draw(tangent);

    }

    /**
     * Zeichnet Niveaulinien am östlichen Rand des Graphen.<br>
     * VORAUSSETZUNG: maxX, maxY, maxZ, ..., bigRadius, smallRadius, height,
     * angle sind initialisiert.
     */
    protected void drawLevelsOnEast(GraphicsContext gc, String varAbsc, String varOrd, String varAppl) {

        if (this.angle >= 0 && this.angle <= 180) {
            return;
        }

        gc.setStroke(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxXOrigin;
        border[0][1] = this.maxYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = this.maxXOrigin;
        border[1][1] = this.minYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = this.maxXOrigin;
        border[2][1] = this.minYOrigin;
        border[2][2] = this.minZOrigin;
        border[3][0] = this.maxXOrigin;
        border[3][1] = this.maxYOrigin;
        border[3][2] = this.minZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        gc.strokeLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        gc.strokeLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        gc.strokeLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        gc.strokeLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 270) {
            if (varOrd == null) {
                gc.strokeText("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                gc.strokeText(varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varOrd == null) {
            gc.strokeText("2. axis", borderPixels[0][0] - gc.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        } else {
            gc.strokeText(varOrd, borderPixels[0][0] - gc.getFontMetrics().stringWidth(varOrd) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.minZOrigin / Math.pow(10, this.expZ));
        int i = bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = this.maxXOrigin;
            lineLevel[0][1] = this.maxYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = this.minYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 270) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.minYOrigin / Math.pow(10, this.expY));
        i = bound;

        while (i * Math.pow(10, this.expY) <= this.maxYOrigin) {
            lineLevel[0][0] = this.maxXOrigin;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = this.minZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle >= 270) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - gc.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
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
    protected void drawLevelsOnWest(GraphicsContext gc, String varAbsc, String varOrd, String varAppl) {

        if (this.angle >= 180 && this.angle <= 360) {
            return;
        }

        gc.setStroke(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.minXOrigin;
        border[0][1] = this.minYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = this.minXOrigin;
        border[1][1] = this.maxYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = this.minXOrigin;
        border[2][1] = this.maxYOrigin;
        border[2][2] = this.minZOrigin;
        border[3][0] = this.minXOrigin;
        border[3][1] = this.minYOrigin;
        border[3][2] = this.minZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        gc.strokeLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        gc.strokeLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        gc.strokeLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        gc.strokeLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 90) {
            if (varOrd == null) {
                gc.strokeText("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                gc.strokeText(varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varOrd == null) {
            gc.strokeText("2. axis", borderPixels[0][0] - gc.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        } else {
            gc.strokeText(varOrd, borderPixels[0][0] - gc.getFontMetrics().stringWidth(varOrd) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.minZOrigin / Math.pow(10, this.expZ));
        int i = bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = this.minXOrigin;
            lineLevel[0][1] = this.minYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.minXOrigin;
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 90) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.minYOrigin / Math.pow(10, this.expY));
        i = bound;

        while (i * Math.pow(10, this.expY) <= this.maxYOrigin) {
            lineLevel[0][0] = this.minXOrigin;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = this.minXOrigin;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = this.minZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle >= 90) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - gc.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
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
    protected void drawLevelsOnSouth(GraphicsContext gc, String varAbsc, String varOrd, String varAppl) {

        if (this.angle <= 90 || this.angle >= 270) {
            return;
        }

        gc.setStroke(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxXOrigin;
        border[0][1] = this.minYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = this.minXOrigin;
        border[1][1] = this.minYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = this.minXOrigin;
        border[2][1] = this.minYOrigin;
        border[2][2] = this.minZOrigin;
        border[3][0] = this.maxXOrigin;
        border[3][1] = this.minYOrigin;
        border[3][2] = this.minZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        gc.strokeLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        gc.strokeLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        gc.strokeLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        gc.strokeLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 180) {
            if (varAbsc == null) {
                gc.strokeText("1. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                gc.strokeText(varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varAbsc == null) {
            gc.strokeText("1. axis", borderPixels[0][0] - gc.getFontMetrics().stringWidth("1. axis") - 10, borderPixels[0][1] + 15);
        } else {
            gc.strokeText(varAbsc, borderPixels[0][0] - gc.getFontMetrics().stringWidth(varAbsc) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.minZOrigin / Math.pow(10, this.expZ));
        int i = bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = this.maxXOrigin;
            lineLevel[0][1] = this.minYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.minXOrigin;
            lineLevel[1][1] = this.minYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 180) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.minXOrigin / Math.pow(10, this.expX));
        i = bound;

        while (i * Math.pow(10, this.expX) <= this.maxXOrigin) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = this.minYOrigin;
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = this.minYOrigin;
            lineLevel[1][2] = this.minZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle >= 180) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0]
                        - gc.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expX))) - 5,
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
    protected void drawLevelsOnNorth(GraphicsContext gc, String varAbsc, String varOrd, String varAppl) {

        if (this.angle >= 90 && this.angle <= 270) {
            return;
        }

        gc.setStroke(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.minXOrigin;
        border[0][1] = this.maxYOrigin;
        border[0][2] = this.maxZOrigin;
        border[1][0] = this.maxXOrigin;
        border[1][1] = this.maxYOrigin;
        border[1][2] = this.maxZOrigin;
        border[2][0] = this.maxXOrigin;
        border[2][1] = this.maxYOrigin;
        border[2][2] = this.minZOrigin;
        border[3][0] = this.minXOrigin;
        border[3][1] = this.maxYOrigin;
        border[3][2] = this.minZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        gc.strokeLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        gc.strokeLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        gc.strokeLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        gc.strokeLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle <= 90) {
            if (varAbsc == null) {
                gc.strokeText("1. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            } else {
                gc.strokeText(varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
            }
        } else if (varAbsc == null) {
            gc.strokeText("1. axis", borderPixels[0][0] - gc.getFontMetrics().stringWidth("1. axis") - 10, borderPixels[0][1] + 15);
        } else {
            gc.strokeText(varAbsc, borderPixels[0][0] - gc.getFontMetrics().stringWidth(varAbsc) - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.minZOrigin / Math.pow(10, this.expZ));
        int i = bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZOrigin) {
            lineLevel[0][0] = this.minXOrigin;
            lineLevel[0][1] = this.maxYOrigin;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle < 90) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.minXOrigin / Math.pow(10, this.expX));
        i = bound;

        while (i * Math.pow(10, this.expX) <= this.maxXOrigin) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = this.maxYOrigin;
            lineLevel[0][2] = this.maxZOrigin;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = this.minZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle <= 90) {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                gc.strokeText(String.valueOf(roundAxisEntries(i, this.expX)), lineLevelPixels[0][0]
                        - gc.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expX))) - 5,
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
    protected void drawLevelsBottom(GraphicsContext gc) {

        // Zunächst den Rahmen auf dem Boden zeichnen
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxXOrigin;
        border[0][1] = this.maxYOrigin;
        border[0][2] = this.minZOrigin;
        border[1][0] = this.minXOrigin;
        border[1][1] = this.maxYOrigin;
        border[1][2] = this.minZOrigin;
        border[2][0] = this.minXOrigin;
        border[2][1] = this.minYOrigin;
        border[2][2] = this.minZOrigin;
        border[3][0] = this.maxXOrigin;
        border[3][1] = this.minYOrigin;
        border[3][2] = this.minZOrigin;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        gc.strokeLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        gc.strokeLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        gc.strokeLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        gc.strokeLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        // Horizontale x-Niveaulinien zeichnen
        int bound = (int) (this.minXOrigin / Math.pow(10, this.expX));
        int i = bound;

        while (i * Math.pow(10, this.expX) <= this.maxXOrigin) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = this.minYOrigin;
            lineLevel[0][2] = this.minZOrigin;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = this.maxYOrigin;
            lineLevel[1][2] = this.minZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        // Horizontale y-Niveaulinien zeichnen
        bound = (int) (this.minYOrigin / Math.pow(10, this.expY));
        i = bound;

        while (i * Math.pow(10, this.expY) <= this.maxYOrigin) {
            lineLevel[0][0] = this.minXOrigin;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.minZOrigin;
            lineLevel[1][0] = this.maxXOrigin;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = this.minZOrigin;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            gc.strokeLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

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
            draw();

        }
    }

    @Override
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        // Zunächst Hintergrund zeichnen.
        switch (backgroundColorMode) {
            case BRIGHT:
                gc.setFill(Color.WHITE);
                break;
            case DARK:
                gc.setFill(Color.BLACK);
                break;
        }
        gc.fillRect(0, 0, 500, 500);
        // Markierungen an den Achsen berechnen.
        computeExpXExpYExpZ();
    }

}
