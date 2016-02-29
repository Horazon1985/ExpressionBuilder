package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import lang.translator.Translator;

public class GraphicPanelSpherical extends JPanel implements Runnable, Exportable {

    //Parameter für 3D-Graphen
    //Boolsche Variable, die angibt, ob der Graph gerade rotiert oder nicht.
    private boolean isRotating;
    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varPhi, varTau;
    private final ArrayList<Expression> exprs = new ArrayList<>();
    //Array, indem die Punkte am Graphen gespeichert sind
    private ArrayList<double[][][]> sphericalGraphs3D = new ArrayList<>();
    /*
     "Vergröberte Version" von Graph3D (GRUND: beim herauszoomen dürfen die
     Plättchen am Graphen nicht so klein sein -> Graph muss etwas vergröbert
     werden).
     */
    private ArrayList<double[][][]> sphericalGraphs3DForGraphic = new ArrayList<>();
    //Gibt an, ob der Funktionswert an der betreffenden Stelle definiert ist.
    private ArrayList<boolean[][]> sphericalGraphs3DAreDefined = new ArrayList<>();
    // Grundfarben für die einzelnen Graphen.
    private final ArrayList<Color> colors = new ArrayList<>();
    // Fixe Grundfarben für die ersten Graphen. Danach werden die Farben per Zufall generiert.
    private final static Color[] fixedColors = {new Color(170, 170, 70), new Color(170, 70, 170), new Color(70, 170, 170)};

    private double zoomfactor;
    private double minPhi, maxPhi, minTau, maxTau;
    private double maxX, maxY, maxZ;
    private int expX, expY, expZ;

    //Radien für die Grundellipse
    private double bigRadius, smallRadius;
    private double height, heightProjection;
    /*
     Neigungswinkel des Graphen: angle = horizontaler Winkel (er wird
     inkrementiert, wenn der Graph im Uhrzeigersinn rotiert) verticalAngle =
     Winkel, unter dem man die dritte Achse sieht. 0 = man schaut seitlich auf
     den Graphen, 90 = man schaut von oben auf den Graphen.
     */
    private double angle, verticalAngle;
    /*
     Die boolsche Variable isAngleMeant ist true <-> der aktuelle Winkel
     (welcher durch das Auslösen eines MouseEvent verändert wird) ist angle.
     Ansonsten verticalAngle.
     */
    private boolean isAngleMeant = true;
    private Point lastMousePosition;

    private PresentationMode presentationMode = PresentationMode.WHOLE_GRAPH;
    private BackgroundColorMode backgroundColorMode = BackgroundColorMode.BRIGHT;

    private static final Color backgroundColorBright = Color.white;
    private static final Color backgroundColorDark = Color.black;
    private static final Color gridColorWholeGraphBright = Color.black;
    private static final Color gridColorWholeGraphDark = Color.green;
    private static final Color gridColorGridOnlyBright = Color.black;
    private static final Color gridColorGridOnlyDark = Color.green;

    public enum BackgroundColorMode {

        BRIGHT, DARK;

    }

    public enum PresentationMode {

        WHOLE_GRAPH, GRID_ONLY;

    }

    public GraphicPanelSpherical() {
        isRotating = false;
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

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public ArrayList<Color> getColors() {
        return this.colors;
    }

    public boolean getIsRotating() {
        return this.isRotating;
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    public BackgroundColorMode getBackgroundColorMode() {
        return this.backgroundColorMode;
    }

    public void setBackgroundColorMode(BackgroundColorMode backgroundColorMode) {
        this.backgroundColorMode = backgroundColorMode;
    }

    public PresentationMode getPresentationMode() {
        return this.presentationMode;
    }

    public void setPresentationMode(PresentationMode presentationMode) {
        this.presentationMode = presentationMode;
    }

    public void setExpressions(ArrayList<Expression> exprs) {
        this.exprs.clear();
        this.exprs.addAll(exprs);
        setColors();
    }

    public void setExpressions(Expression... exprs) {
        this.exprs.clear();
        this.exprs.addAll(Arrays.asList(exprs));
        setColors();
    }

    public void addExpression(Expression expr) {
        this.exprs.add(expr);
        setColors();
    }

    private void setColors() {
        int numberOfColors = Math.max(this.exprs.size(), this.sphericalGraphs3D.size());
        this.colors.clear();
        for (int i = this.colors.size(); i < numberOfColors; i++) {
            if (i < fixedColors.length) {
                this.colors.add(fixedColors[i]);
            } else {
                this.colors.add(generateColor());
            }
        }
    }

    private Color generateColor() {
        return new Color((int) (70 + 100 * Math.random()), (int) (100 * Math.random()), (int) (70 + 100 * Math.random()));
    }

    public void setIsRotating(boolean isRotating) {
        this.isRotating = isRotating;
    }

    public void setParameters(String varR, String varPhi, double bigRadius, double heightProjection, double angle,
            double verticalAngle) {
        this.varPhi = varR;
        this.varTau = varPhi;
        this.heightProjection = heightProjection;
        this.bigRadius = bigRadius;
        this.smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
        this.height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);
        this.angle = angle;
        this.verticalAngle = verticalAngle;
        this.zoomfactor = 1;
    }

    /**
     * Voraussetzung: graphs sind bereits alle initialisiert (bzw. mit
     * Funktionswerten gefüllt).
     */
    private void computeMaxXMaxYMaxZ() {

        if (this.sphericalGraphs3D.isEmpty()) {

            this.maxX = 1;
            this.maxY = 1;
            this.maxZ = 1;

        } else {

            double globalMaxX = 0, globalMaxY = 0, globalMaxZ = 0;

            for (int k = 0; k < this.sphericalGraphs3D.size(); k++) {

                if (this.sphericalGraphs3D.get(0).length == 0) {
                    continue;
                }

                for (int i = 0; i < this.sphericalGraphs3D.get(k).length; i++) {
                    for (int j = 0; j < this.sphericalGraphs3D.get(k)[0].length; j++) {
                        globalMaxX = Math.max(globalMaxX, Math.abs(this.sphericalGraphs3D.get(k)[i][j][0]));
                        globalMaxY = Math.max(globalMaxY, Math.abs(this.sphericalGraphs3D.get(k)[i][j][1]));
                        if (sphericalGraphs3DAreDefined.get(k)[i][j]) {
                            globalMaxZ = Math.max(globalMaxZ, Math.abs(this.sphericalGraphs3D.get(k)[i][j][2]));
                        }
                    }
                }

                this.maxX = globalMaxX;
                this.maxY = globalMaxY;
                this.maxZ = globalMaxZ;
                // 30 % Rand auf jeder der Achsen lassen!
                this.maxX = this.maxX * 1.3;
                this.maxY = this.maxY * 1.3;
                this.maxZ = this.maxZ * 1.3;

            }

        }

        if (this.maxX == 0) {
            this.maxX = 1;
        }
        if (this.maxY == 0) {
            this.maxY = 1;
        }
        if (this.maxZ == 0) {
            this.maxZ = 1;
        }

    }

    /**
     * Voraussetzung: graph3D ist bereits initialisiert (bzw. mit
     * Funktionswerten gefüllt), maxX, maxY, maxZ sind bekannt/initialisiert.
     */
    private void computeExpXExpYExpZ() {

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
     * Berechnet die Gitterpunkte für den 3D-Graphen aus dem Ausdruck expr.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(Expression exprR_0, Expression exprR_1, Expression exprPhi_0, Expression exprPhi_1) throws EvaluationException {

        this.minPhi = exprR_0.evaluate();
        this.maxPhi = exprR_1.evaluate();
        this.minTau = exprPhi_0.evaluate();
        this.maxTau = exprPhi_1.evaluate();

        this.sphericalGraphs3D = new ArrayList<>();
        this.sphericalGraphs3DAreDefined = new ArrayList<>();

        double currentPhi, currentTau, currentR;
        double[][][] singleGraph;
        boolean[][] singleGraphIsDefined;
        /*
         Entlang r wird in 100 Intervalle unterteilt, entlang phi in 
         100 * (this.maxPhi - this.minPhi) / (2 * Math.PI) Intervalle.
         */
        int numberOfIntervalsAlongPhi = (int) (100 * (this.maxTau - this.minTau) / (2 * Math.PI));
        for (Expression expr : exprs) {

            singleGraph = new double[101][numberOfIntervalsAlongPhi + 1][3];
            singleGraphIsDefined = new boolean[101][numberOfIntervalsAlongPhi + 1];
            Variable.setValue(this.varPhi, this.minPhi);
            Variable.setValue(this.varTau, this.minTau);
            for (int i = 0; i <= 100; i++) {
                for (int j = 0; j <= numberOfIntervalsAlongPhi; j++) {
                    currentPhi = this.minPhi + (this.maxPhi - this.minPhi) * i / 100;
                    currentTau = this.minTau + (this.maxTau - this.minTau) * j / numberOfIntervalsAlongPhi;
                    Variable.setValue(this.varPhi, currentPhi);
                    Variable.setValue(this.varTau, currentTau);
                    try {
                        currentR = expr.evaluate();
                        singleGraph[i][j][0] = currentR * Math.sin(currentTau) * Math.cos(currentPhi);
                        singleGraph[i][j][1] = currentR * Math.sin(currentTau) * Math.sin(currentPhi);
                        singleGraph[i][j][2] = currentR * Math.cos(currentTau);
                        singleGraphIsDefined[i][j] = true;
                    } catch (EvaluationException e) {
                        singleGraph[i][j][0] = Double.NaN;
                        singleGraph[i][j][1] = Double.NaN;
                        singleGraph[i][j][2] = Double.NaN;
                        singleGraphIsDefined[i][j] = false;
                    }
                }
            }
            this.sphericalGraphs3D.add(singleGraph);
            this.sphericalGraphs3DAreDefined.add(singleGraphIsDefined);

        }

        //Zeichenbereich berechnen.
        computeMaxXMaxYMaxZ();

    }

    //Berechnet aus dem Winkel "angle" den Winkel, welcher in der graphischen Darstellung auftaucht
    private double getGraphicalAngle(double bigRadius, double smallRadius, double angle) {
        //Vorausgesetzt: 0 <= real_angle < 360
        if ((angle == 0) || (angle == 90) || (angle == 180) || (angle == 270)) {
            return angle;
        } else if (angle < 90) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI;
        } else if (angle < 180) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        } else if (angle < 270) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        } else {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 360;
        }
    }

    /**
     * Macht die Lösung (eventuell) etwas gröber, damit sie gezeichnet werden
     * kann Voraussetzung: maxX, maxY sind bekannt!
     */
    private ArrayList<double[][][]> convertGraphsToCoarserGraphs() {

        int numberOfIntervalsAlongR = Math.min(100, (int) (30 * (this.maxPhi - this.minPhi) / (this.maxPhi * zoomfactor)));
        // Zur Erinnerung: Einschränkung ist maxPhi - minPhi <= 10 * 2 * pi.
        int numberOfIntervalsAlongPhi = Math.min((int) (100 * (this.maxTau - this.minTau) / (2 * Math.PI)), (int) (100 / zoomfactor * (this.maxTau - this.minTau) / (2 * Math.PI)));

        ArrayList<double[][][]> graphsForGraphic = new ArrayList<>();

        double[][][] graph3DForGraphic;
        boolean[][] coarserGraph3DIsDefined;
        this.sphericalGraphs3DAreDefined.clear();
        for (double[][][] graph3D : this.sphericalGraphs3D) {

            graph3DForGraphic = new double[numberOfIntervalsAlongR][numberOfIntervalsAlongPhi][3];
            coarserGraph3DIsDefined = new boolean[numberOfIntervalsAlongR][numberOfIntervalsAlongPhi];

            int currentIndexI, currentIndexJ;

            for (int i = 0; i < numberOfIntervalsAlongR; i++) {

                if (graph3D.length <= numberOfIntervalsAlongR) {
                    currentIndexI = i;
                } else {
                    currentIndexI = (int) (i * ((double) graph3D.length - 1) / (numberOfIntervalsAlongR - 1));
                }

                for (int j = 0; j < numberOfIntervalsAlongPhi; j++) {
                    if (graph3D[0].length <= numberOfIntervalsAlongPhi) {
                        currentIndexJ = j;
                    } else {
                        currentIndexJ = (int) (j * ((double) graph3D[0].length - 1) / (numberOfIntervalsAlongPhi - 1));
                    }
                    try {
                        graph3DForGraphic[i][j][0] = graph3D[currentIndexI][currentIndexJ][0];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        double x = 1;
                    }
                    graph3DForGraphic[i][j][1] = graph3D[currentIndexI][currentIndexJ][1];
                    graph3DForGraphic[i][j][2] = graph3D[currentIndexI][currentIndexJ][2];
                    // Prüft, ob der Funktionswert this.graph3D[i][j][2] definiert ist.
                    coarserGraph3DIsDefined[i][j] = !(Double.isNaN(graph3D[currentIndexI][currentIndexJ][2]) || Double.isInfinite(graph3D[currentIndexI][currentIndexJ][2]));
                }

            }

            graphsForGraphic.add(graph3DForGraphic);
            this.sphericalGraphs3DAreDefined.add(coarserGraph3DIsDefined);

        }

        return graphsForGraphic;

    }

    //Berechnet aus Punktkoordinaten (x, y, z) Koordinaten (x', y') für die graphische Darstellung
    private int[] convertToPixel(double x, double y, double z, double bigRadius, double smallRadius, double height, double angle) {

        double angleAbsc = getGraphicalAngle(bigRadius, smallRadius, angle);
        double angleOrd;
        if (angle < 90) {
            angleOrd = getGraphicalAngle(bigRadius, smallRadius, angle + 270);
        } else {
            angleOrd = getGraphicalAngle(bigRadius, smallRadius, angle - 90);
        }

        //pixels sind die Pixelkoordinaten für die Graphische Darstellung von (x, y, z)
        int[] pixel = new int[2];

        //Berechnung von pixels[0]
        double x_1, x_2;

        if (angleAbsc == 0) {
            x_1 = bigRadius * x / maxX;
            x_2 = 0;
        } else if (angleAbsc == 90) {
            x_1 = 0;
            x_2 = bigRadius * y / maxY;
        } else if (angleAbsc == 180) {
            x_1 = -bigRadius * x / maxX;
            x_2 = 0;
        } else if (angleAbsc == 270) {
            x_1 = 0;
            x_2 = -bigRadius * y / maxY;
        } else if (angleAbsc < 90) {
            x_1 = (x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = (y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        } else if (angleAbsc < 180) {
            x_1 = -(x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = (y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        } else if (angleAbsc < 270) {
            x_1 = -(x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = -(y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        } else {
            x_1 = (x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = -(y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        }

        pixel[0] = (int) (250 + x_1 + x_2);

        //Berechnung von pixels[1]
        double y_1, y_2, y_3;

        if (angleAbsc == 0) {
            y_1 = 0;
            y_2 = -smallRadius * y / maxY;
        } else if (angleAbsc == 90) {
            y_1 = smallRadius * x / maxX;
            y_2 = 0;
        } else if (angleAbsc == 180) {
            y_1 = 0;
            y_2 = smallRadius * y / maxY;
        } else if (angleAbsc == 270) {
            y_1 = -smallRadius * x / maxX;
            y_2 = 0;
        } else {
            y_1 = x_1 * Math.tan(angleAbsc * Math.PI / 180);
            y_2 = x_2 * Math.tan(angleOrd * Math.PI / 180);
        }

        //maximaler Funktionswert (also max_z) soll h Pixel betragen
        y_3 = -height * z / maxZ;
        pixel[1] = (int) (250 + y_1 + y_2 + y_3);

        return pixel;

    }

    /**
     * Zeichnet ein (tangentiales) rechteckiges Plättchen des 3D-Graphen
     */
    private void drawInfinitesimalTangentSpace(int x_1, int y_1, int x_2, int y_2,
            int x_3, int y_3, int x_4, int y_4, Graphics g, Color c) {

        GeneralPath tangent = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
        tangent.moveTo(x_1, y_1);
        tangent.lineTo(x_2, y_2);
        tangent.lineTo(x_3, y_3);
        tangent.lineTo(x_4, y_4);
        tangent.closePath();
        Graphics2D g2 = (Graphics2D) g;

//        if (presentationMode.equals(PresentationMode.WHOLE_GRAPH)) {
//            g2.setPaint(c);
//            g2.fill(tangent);
//        }
        switch (backgroundColorMode) {
            case BRIGHT:
//                switch (presentationMode) {
//                    case WHOLE_GRAPH:
//                        g2.setPaint(gridColorWholeGraphBright);
//                        break;
//                    case GRID_ONLY:
//                        g2.setPaint(gridColorNetOnlyBright);
//                        break;
//                }
                g2.setPaint(gridColorGridOnlyBright);
                break;
            case DARK:
//                switch (presentationMode) {
//                    case WHOLE_GRAPH:
//                        g2.setPaint(gridColorWholeGraphDark);
//                        break;
//                    case GRID_ONLY:
//                        g2.setPaint(gridColorGridOnlyDark);
//                        break;
//                }
                g2.setPaint(gridColorGridOnlyDark);
                break;
        }

        g2.draw(tangent);

    }

    /**
     * Berechnet den Achseneintrag m*10^(-k) ohne den Double-typischen
     * Nachkommastellenfehler.
     */
    private BigDecimal roundAxisEntries(int m, int k) {
        if (k >= 0) {
            return new BigDecimal(m).multiply(BigDecimal.TEN.pow(k));
        }
        return new BigDecimal(m).divide(BigDecimal.TEN.pow(-k));
    }

    /**
     * Die folgenden vier Prozeduren zeichnen Niveaulinien am Rand des Graphen.
     * Voraussetzung: maxX, maxY, maxZ, bigRadius, smallRadius, height, angle
     * sind bekannt/initialisiert.
     */
    private void drawLevelsOnEast(Graphics g, double angle) {

        if ((angle >= 0) && (angle <= 180)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxX;
        border[0][1] = this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = this.maxX;
        border[1][1] = -this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = this.maxX;
        border[2][1] = -this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = this.maxX;
        border[3][1] = this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 270) {
            g.drawString("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("2. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZ) {
            lineLevel[0][0] = this.maxX;
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 270) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxY / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= this.maxY) {
            lineLevel[0][0] = this.maxX;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 270) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnWest(Graphics g, double angle) {

        if ((angle >= 180) && (angle <= 360)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -this.maxX;
        border[0][1] = -this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = -this.maxX;
        border[1][1] = this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = -this.maxX;
        border[2][1] = this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = -this.maxX;
        border[3][1] = -this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 90) {
            g.drawString("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("2. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= this.maxZ) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 90) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxY / Math.pow(10, expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = i * Math.pow(10, expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnSouth(Graphics g, double angle) {

        if ((angle <= 90) || (angle >= 270)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxX;
        border[0][1] = -this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = -this.maxX;
        border[1][1] = -this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = -this.maxX;
        border[2][1] = -this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = this.maxX;
        border[3][1] = -this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 180) {
            g.drawString("1. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("1. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("1. axis") - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= this.maxZ) {
            lineLevel[0][0] = this.maxX;
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 180) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        //Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 180) {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnNorth(Graphics g, double angle) {

        if ((angle >= 90) && (angle <= 270)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -this.maxX;
        border[0][1] = this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = this.maxX;
        border[1][1] = this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = this.maxX;
        border[2][1] = this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = -this.maxX;
        border[3][1] = this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle <= 90) {
            g.drawString("1. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("1. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("1. axis") - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= this.maxZ) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle < 90) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle <= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsBottom(Graphics g, double angle) {

        //Zunächst den Rahmen auf dem Boden zeichnen
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxX;
        border[0][1] = this.maxY;
        border[0][2] = -this.maxZ;
        border[1][0] = -this.maxX;
        border[1][1] = this.maxY;
        border[1][2] = -this.maxZ;
        border[2][0] = -this.maxX;
        border[2][1] = -this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = this.maxX;
        border[3][1] = -this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        //horizontale x-Niveaulinien zeichnen
        int bound = (int) (this.maxX / Math.pow(10, this.expX));
        int i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        //horizontale y-Niveaulinien zeichnen
        bound = (int) (this.maxY / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = i * Math.pow(10, expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

    }

    private Color computeColor(Color groundColor, double minExpr, double maxExpr, double height) {

        Color c;
        int red, green, blue;

        int r = groundColor.getRed();
        int g = groundColor.getGreen();
        int b = groundColor.getBlue();

        if (minExpr == maxExpr) {
            red = r;
            green = g;
            blue = b;
        } else {
            red = r - (int) (60 * Math.sin(this.angle / 180 * Math.PI));
            green = g + (int) ((255 - g) * (height - minExpr) / (maxExpr - minExpr));
            blue = b + (int) (60 * Math.sin(this.angle / 180 * Math.PI));
        }

        c = new Color(red, green, blue);
        return c;

    }

    /**
     * Berechnet die Höhe des Schwerpunktes eines Tangentialplättchens, falls es
     * definiert ist.
     */
    private double computeAverageHeightOfInfinitesimalTangentSpace(int indexOfGraph3D, int i, int j) {

        double averageHeightOfTangentSpace = 0;
        int numberOfDefinedBorderPoints = 0;

        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i][j]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i + 1][j]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i + 1][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i][j + 1]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i][j + 1][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i + 1][j + 1]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i + 1][j + 1][2];
            numberOfDefinedBorderPoints++;
        }

        if (numberOfDefinedBorderPoints == 4) {
            return averageHeightOfTangentSpace / numberOfDefinedBorderPoints;
        }
        return Double.NaN;

    }

    /**
     * Zeichnet den ganzen 3D-Graphen bei Übergabe der Pixelkoordinaten (mit
     * Achsen)
     */
    private void drawGraphsFromCylindricalGraphs3DForGraphic(Graphics g, double minExpr, double maxExpr,
            double bigRadius, double smallRadius, double height, double angle) {

        int numberOfIntervalsAlongAbsc = 0;
        int numberOfIntervalsAlongOrd = 0;

        // Anzahl der Intervalle für das Zeichnen ermitteln.
        for (double[][][] cylindricalgraph3DForGraphic : this.sphericalGraphs3DForGraphic) {
            if (cylindricalgraph3DForGraphic.length > 0) {
                numberOfIntervalsAlongAbsc = cylindricalgraph3DForGraphic.length - 1;
                numberOfIntervalsAlongOrd = cylindricalgraph3DForGraphic[0].length - 1;
                break;
            }
        }

        // Dann können keine Graphen gezeichnet werden.
        if (numberOfIntervalsAlongAbsc == 0 || numberOfIntervalsAlongOrd == 0) {
            return;
        }

        //Koordinaten der einzelnen Graphen in graphische Koordinaten umwandeln
        ArrayList<int[][][]> graphicalGraphs = new ArrayList<>();
        int[][][] graphicalGraph;

        for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
            graphicalGraph = new int[numberOfIntervalsAlongAbsc + 1][numberOfIntervalsAlongOrd + 1][2];
            for (int i = 0; i < numberOfIntervalsAlongAbsc + 1; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd + 1; j++) {
                    graphicalGraph[i][j] = convertToPixel(this.sphericalGraphs3DForGraphic.get(k)[i][j][0], this.sphericalGraphs3DForGraphic.get(k)[i][j][1],
                            this.sphericalGraphs3DForGraphic.get(k)[i][j][2], bigRadius, smallRadius, height, angle);
                }
            }
            graphicalGraphs.add(graphicalGraph);
        }

        /*
         Jetzt wird der Graph, abhängig vom Winkel angle, gezeichnet. Es muss
         deshalb nach dem Winkel unterschieden werden, da der Graph stets "von
         hinten nach vorne" gezeichnet werden soll. Voraussetzung: 0 <= angle
         < 360
         */
        HashMap<Integer, Double> heightsOfCentersOfInfinitesimalTangentSpaces = new HashMap<>();
        Double heightOfCentersOfInfinitesimalTangentSpace;
        ArrayList<Integer> indices;

        if (angle <= 90) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.sphericalGraphs3DForGraphic.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][1],
                                graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][1],
                                g, c);

                    }

                }

            }

        } else if (angle <= 180) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.sphericalGraphs3DForGraphic.get(indices.get(k))[i][j][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][j][0], graphicalGraphs.get(indices.get(k))[i][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j][0], graphicalGraphs.get(indices.get(k))[i + 1][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j + 1][0], graphicalGraphs.get(indices.get(k))[i + 1][j + 1][1],
                                graphicalGraphs.get(indices.get(k))[i][j + 1][0], graphicalGraphs.get(indices.get(k))[i][j + 1][1],
                                g, c);

                    }

                }

            }

        } else if (angle <= 270) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.sphericalGraphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][1],
                                g, c);

                    }

                }

            }

        } else {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.sphericalGraphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][1],
                                g, c);

                    }

                }

            }

        }

    }

    /**
     * Hilfsmethode. Gibt eine Liste von Indizes zurück, gemäß welcher die
     * Double-Werte im HashMap im Parameter aufsteigend sortiert sind.
     */
    private static ArrayList<Integer> getIndicesForAscendingSorting(HashMap<Integer, Double> centersOfInfinitesimalTangentSpaces) {

        HashMap<Integer, Double> copyOfCenters = new HashMap<>();
        ArrayList<Integer> sortedCenters = new ArrayList<>();

        // Manuell kopieren. clone() traue ich nicht!
        for (Integer i : centersOfInfinitesimalTangentSpaces.keySet()) {
            copyOfCenters.put(i, centersOfInfinitesimalTangentSpaces.get(i));
        }

        int indexWithLeastElement;
        double minimalValue;
        while (!copyOfCenters.isEmpty()) {

            indexWithLeastElement = -1;
            minimalValue = Double.POSITIVE_INFINITY;
            for (Integer i : copyOfCenters.keySet()) {
                if (indexWithLeastElement == -1) {
                    indexWithLeastElement = i;
                    minimalValue = copyOfCenters.get(i);
                } else if (copyOfCenters.get(i) < minimalValue) {
                    indexWithLeastElement = i;
                    minimalValue = copyOfCenters.get(i);
                }
            }

            sortedCenters.add(indexWithLeastElement);
            copyOfCenters.remove(indexWithLeastElement);

        }

        return sortedCenters;

    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Graphen.
     */
    private void drawCylindricalGraph3D(Graphics g, double angle) {

        //Zunächst Hintergrund zeichnen.
        switch (backgroundColorMode) {
            case BRIGHT:
                g.setColor(Color.white);
                break;
            case DARK:
                g.setColor(Color.black);
                break;
        }
        g.fillRect(0, 0, 500, 500);

        /*
         Falls kein echter Graph vorhanden ist, dann nur den weißen
         Hintergrund zeichnen und beenden. GRUND: Zu Beginn wird sofort
         repaint() aufgerufen, welches wiederum paint() aufruft. Dann sind
         aber varAbsc und varOrd nicht initialisiert und es gibt eine
         Exception. Dies wird hiermit verhindert.
         */
        if (this.sphericalGraphs3D.isEmpty()) {
            return;
        }
        computeExpXExpYExpZ();
        this.sphericalGraphs3DForGraphic = convertGraphsToCoarserGraphs();

        /*
         Ermittelt den kleinsten und den größten Funktionswert Notwendig, um
         das Farbspektrum im 3D-Graphen zu berechnen!
         */
        double minExpr = Double.NaN;
        double maxExpr = Double.NaN;

        // Zunächst wird geprüft, ob mindestens ein Graph IRGENDWO definiert ist
        boolean graphIsSomewhereDefined = false;
        for (int k = 0; k < this.sphericalGraphs3DAreDefined.size(); k++) {
            for (int i = 0; i < this.sphericalGraphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.sphericalGraphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.sphericalGraphs3DAreDefined.get(k)[i][j]) {
                        graphIsSomewhereDefined = true;
                        minExpr = this.sphericalGraphs3DForGraphic.get(k)[i][j][2];
                        maxExpr = this.sphericalGraphs3DForGraphic.get(k)[i][j][2];
                        break;
                    }
                }
                if (graphIsSomewhereDefined) {
                    break;
                }
            }
            if (graphIsSomewhereDefined) {
                break;
            }
        }

        // Falls kein Graph definiert (irgendwo) ist, Defaultwerte für Maße setzen.
        if (!graphIsSomewhereDefined) {
            minExpr = 0;
            maxExpr = 1;
        }

        for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
            for (int i = 0; i < this.sphericalGraphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.sphericalGraphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.sphericalGraphs3DAreDefined.get(k)[i][j]) {
                        minExpr = Math.min(minExpr, this.sphericalGraphs3DForGraphic.get(k)[i][j][2]);
                        maxExpr = Math.max(maxExpr, this.sphericalGraphs3DForGraphic.get(k)[i][j][2]);
                    }
                }
            }
        }

        drawLevelsOnEast(g, angle);
        drawLevelsOnSouth(g, angle);
        drawLevelsOnWest(g, angle);
        drawLevelsOnNorth(g, angle);
        drawLevelsBottom(g, angle);
        drawGraphsFromCylindricalGraphs3DForGraphic(g, minExpr, maxExpr, bigRadius, smallRadius, height, angle);

    }

    public void drawCylindricalGraphs3D(Expression r_0, Expression r_1, Expression phi_0, Expression phi_1, Expression... exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(r_0, r_1, phi_0, phi_1);
        drawCylindricalGraphs3D();
    }

    public void drawCylindricalGraphs3D(Expression r_0, Expression r_1, Expression phi_0, Expression phi_1, ArrayList<Expression> exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(r_0, r_1, phi_0, phi_1);
        drawCylindricalGraphs3D();
    }

    private void drawCylindricalGraphs3D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCylindricalGraph3D(g, angle);
    }

    @Override
    public void run() {
        while (isRotating) {

            angle = angle + 1;
            if (angle >= 360) {
                angle = angle - 360;
            }
            if (angle < 0) {
                angle = angle + 360;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            repaint();

        }
    }

    // Grafikexport.
    @Override
    public void export(String filePath) throws IOException {
        BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        paintComponent(g);
        ImageIO.write(bi, "PNG", new File(filePath));
    }

}
