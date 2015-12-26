package graphic;

import exceptions.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Variable;
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
import translator.Translator;

public class GraphicPanel3D extends JPanel implements Runnable, Exportable {

    //Parameter für 3D-Graphen
    //Boolsche Variable, die angibt, ob der Graph gerade rotiert oder nicht.
    private boolean isRotating;
    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varAbsc, varOrd;
    private final ArrayList<Expression> exprs = new ArrayList<>();
    //Array, indem die Punkte am Graphen gespeichert sind
    private ArrayList<double[][][]> graphs3D = new ArrayList<>();
    /*
     "Vergröberte Version" von Graph3D (GRUND: beim herauszoomen dürfen die
     Plättchen am Graphen nicht so klein sein -> Graph muss etwas vergröbert
     werden).
     */
    private ArrayList<double[][][]> graphs3DForGraphic = new ArrayList<>();
    //Gibt an, ob der Funktionswert an der betreffenden Stelle definiert ist.
    private ArrayList<boolean[][]> graphs3DAreDefined = new ArrayList<>();
    // Grundfarben für die einzelnen Graphen.
    private final ArrayList<Color> colors = new ArrayList<>();
    // Fixe Grundfarben für die ersten Graphen. Danach werden die Farben per Zufall generiert.
    final static Color[] fixedColors = {new Color(170, 170, 70), new Color(170, 70, 170), new Color(70, 170, 170)};

    private double zoomfactor;
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

    public GraphicPanel3D() {
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

    public ArrayList<Expression> getExpression() {
        return this.exprs;
    }

    public boolean getIsRotating() {
        return this.isRotating;
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateExceptionMessage("GR_Graphic3D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateExceptionMessage("GR_Graphic3D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateExceptionMessage("GR_Graphic3D_MOVE_MOUSE_WHEEL"));
        return instructions;
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
        int numberOfColors = Math.max(this.exprs.size(), this.graphs3D.size());
        this.colors.clear();
        for (int i = this.colors.size(); i < numberOfColors; i++) {
            if (i < GraphicPanel2D.fixedColors.length) {
                this.colors.add(GraphicPanel3D.fixedColors[i]);
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

    public void setParameters(String varAbsc, String varOrd, double bigRadius, double heightProjection, double angle,
            double verticalAngle) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
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

        if (this.graphs3D.isEmpty()) {

            this.maxX = 1;
            this.maxY = 1;
            this.maxZ = 1;

        } else {

            double globalMaxX = 0, globalMaxY = 0, globalMaxZ = 0;

            for (int k = 0; k < this.graphs3D.size(); k++) {

                if (this.graphs3D.get(0).length == 0) {
                    continue;
                }
                globalMaxX = Math.max(globalMaxX, Math.max(Math.abs(this.graphs3D.get(0)[0][0][0]), Math.abs(this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0])));
                globalMaxY = Math.max(globalMaxY, Math.max(Math.abs(this.graphs3D.get(0)[0][0][1]), Math.abs(this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1])));

                for (int i = 0; i <= this.graphs3D.get(k).length - 1; i++) {
                    for (int j = 0; j <= this.graphs3D.get(k)[0].length - 1; j++) {
                        if (graphs3DAreDefined.get(k)[i][j]) {
                            globalMaxZ = Math.max(globalMaxZ, Math.abs(this.graphs3D.get(k)[i][j][2]));
                        }
                    }
                }
                this.maxX = globalMaxX;
                this.maxY = globalMaxY;
                this.maxZ = globalMaxZ;
                // 30 % Rand auf der z-Achse lassen!
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
    private void expressionToGraph(Expression exprX_0, Expression exprX_1, Expression exprY_0, Expression exprY_1) throws EvaluationException {

        double x_0 = exprX_0.evaluate();
        double x_1 = exprX_1.evaluate();
        double y_0 = exprY_0.evaluate();
        double y_1 = exprY_1.evaluate();

        if (x_0 >= x_1) {
            throw new EvaluationException(Translator.translateExceptionMessage("MCC_FIRST_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOT3D"));
        }
        if (y_0 >= y_1) {
            throw new EvaluationException(Translator.translateExceptionMessage("MCC_SECOND_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOT3D"));
        }

        this.graphs3D = new ArrayList<>();
        this.graphs3DAreDefined = new ArrayList<>();

        double[][][] singleGraph;
        boolean[][] singleGraphIsDefined;
        for (Expression expr : exprs) {

            singleGraph = new double[101][101][3];
            singleGraphIsDefined = new boolean[101][101];
            Variable.setValue(this.varAbsc, x_0);
            Variable.setValue(this.varOrd, y_0);
            for (int i = 0; i <= 100; i++) {
                for (int j = 0; j <= 100; j++) {
                    singleGraph[i][j][0] = x_0 + (x_1 - x_0) * i / 100;
                    singleGraph[i][j][1] = y_0 + (y_1 - y_0) * j / 100;
                    Variable.setValue(this.varAbsc, x_0 + (x_1 - x_0) * i / 100);
                    Variable.setValue(this.varOrd, y_0 + (y_1 - y_0) * j / 100);
                    try {
                        singleGraph[i][j][2] = expr.evaluate();
                        singleGraphIsDefined[i][j] = true;
                    } catch (EvaluationException e) {
                        singleGraph[i][j][2] = Double.NaN;
                        singleGraphIsDefined[i][j] = false;
                    }
                }
            }
            this.graphs3D.add(singleGraph);
            this.graphs3DAreDefined.add(singleGraphIsDefined);

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

        int numberOfIntervalsAlongAbsc = graphs3D.get(0).length;
        int numberOfIntervalsAlongOrd = graphs3D.get(0)[0].length;

        if (numberOfIntervalsAlongAbsc > 1.35 * 20 * (graphs3D.get(0)[numberOfIntervalsAlongAbsc - 1][0][0] - graphs3D.get(0)[0][0][0]) / maxX) {
            numberOfIntervalsAlongAbsc = (int) (1.35 * 20 * (graphs3D.get(0)[numberOfIntervalsAlongAbsc - 1][0][0] - graphs3D.get(0)[0][0][0]) / maxX);
            if (numberOfIntervalsAlongAbsc < 1) {
                numberOfIntervalsAlongAbsc = 1;
            }
        }
        if (numberOfIntervalsAlongOrd > 1.35 * 20 * (graphs3D.get(0)[0][numberOfIntervalsAlongOrd - 1][1] - graphs3D.get(0)[0][0][1]) / maxY) {
            numberOfIntervalsAlongOrd = (int) (1.35 * 20 * (graphs3D.get(0)[0][numberOfIntervalsAlongOrd - 1][1] - graphs3D.get(0)[0][0][1]) / maxY);
            if (numberOfIntervalsAlongOrd < 1) {
                numberOfIntervalsAlongOrd = 1;
            }
        }

        ArrayList<double[][][]> graphsForGraphic = new ArrayList<>();

        double[][][] graph3DForGraphic;
        boolean[][] coarserGraph3DIsDefined;
        this.graphs3DAreDefined.clear();
        for (double[][][] graph3D : this.graphs3D) {

            graph3DForGraphic = new double[numberOfIntervalsAlongAbsc][numberOfIntervalsAlongOrd][3];
            coarserGraph3DIsDefined = new boolean[numberOfIntervalsAlongAbsc][numberOfIntervalsAlongOrd];

            int currentIndexI, currentIndexJ;

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                if (graph3D.length <= numberOfIntervalsAlongAbsc) {
                    currentIndexI = i;
                } else {
                    currentIndexI = (int) (i * ((double) graph3D.length - 1) / (numberOfIntervalsAlongAbsc - 1));
                }

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {
                    if (graph3D[0].length <= numberOfIntervalsAlongOrd) {
                        currentIndexJ = j;
                    } else {
                        currentIndexJ = (int) (j * ((double) graph3D[0].length - 1) / (numberOfIntervalsAlongOrd - 1));
                    }
                    graph3DForGraphic[i][j][0] = graph3D[currentIndexI][currentIndexJ][0];
                    graph3DForGraphic[i][j][1] = graph3D[currentIndexI][currentIndexJ][1];
                    graph3DForGraphic[i][j][2] = graph3D[currentIndexI][currentIndexJ][2];
                    // Prüft, ob der Funktionswert this.graph3D[i][j][2] definiert ist.
                    coarserGraph3DIsDefined[i][j] = !(Double.isNaN(graph3D[currentIndexI][currentIndexJ][2]) || Double.isInfinite(graph3D[currentIndexI][currentIndexJ][2]));
                }

            }

            graphsForGraphic.add(graph3DForGraphic);
            this.graphs3DAreDefined.add(coarserGraph3DIsDefined);

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
        g2.setPaint(c);
        g2.fill(tangent);
        g2.setPaint(Color.black);
        g2.draw(tangent);

    }

    /**
     * Zeichnet ein (tangentiales) rechteckiges Plättchen des 3D-Graphen
     */
    private void drawInfinitesimalTriangleTangentSpace(int x_1, int y_1, int x_2, int y_2,
            int x_3, int y_3, Graphics g, Color c) {

        GeneralPath tangent = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
        tangent.moveTo(x_1, y_1);
        tangent.lineTo(x_2, y_2);
        tangent.lineTo(x_3, y_3);
        tangent.closePath();
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(c);
        g2.fill(tangent);
        g2.setPaint(Color.black);
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
    private void drawLevelsOnEast(Graphics g, double minExpr, double maxExpr, double bigRadius,
            double smallRadius, double height, double angle) {

        if ((angle >= 0) && (angle <= 180)) {
            return;
        }

        double maxValueAbsc = Math.max(Math.abs(this.graphs3D.get(0)[0][0][0]), Math.abs(this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0]));
        double maxValueOrd = Math.max(Math.abs(this.graphs3D.get(0)[0][0][1]), Math.abs(this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1]));
        double maxValueAppl = Math.max(Math.abs(minExpr), Math.abs(maxExpr));

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = maxValueAbsc;
        border[0][1] = maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = maxValueAbsc;
        border[1][1] = -maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = maxValueAbsc;
        border[2][1] = -maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = maxValueAbsc;
        border[3][1] = maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], bigRadius, smallRadius, height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 270) {
            g.drawString(varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(varOrd) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= maxValueAppl) {
            lineLevel[0][0] = maxValueAbsc;
            lineLevel[0][1] = maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = -maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 270) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (maxValueOrd / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= maxValueOrd) {
            lineLevel[0][0] = maxValueAbsc;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

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

    private void drawLevelsOnWest(Graphics g, double minExpr, double maxExpr, double bigRadius,
            double smallRadius, double height, double angle) {

        if ((angle >= 180) && (angle <= 360)) {
            return;
        }

        double maxValueAbsc = Math.max(Math.abs(this.graphs3D.get(0)[0][0][0]), Math.abs(this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0]));
        double maxValueOrd = Math.max(Math.abs(this.graphs3D.get(0)[0][0][1]), Math.abs(this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1]));
        double maxValueAppl = Math.max(Math.abs(minExpr), Math.abs(maxExpr));

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -maxValueAbsc;
        border[0][1] = -maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = -maxValueAbsc;
        border[1][1] = maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = -maxValueAbsc;
        border[2][1] = maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = -maxValueAbsc;
        border[3][1] = -maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], bigRadius, smallRadius, height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 90) {
            g.drawString(varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(varOrd) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= maxValueAppl) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = -maxValueAbsc;
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 90) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (maxValueOrd / Math.pow(10, expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= maxValueOrd) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = -maxValueAbsc;
            lineLevel[1][1] = i * Math.pow(10, expY);
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

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

    private void drawLevelsOnSouth(Graphics g, double f_min, double f_max, double R, double r, double h, double angle) {

        if ((angle <= 90) || (angle >= 270)) {
            return;
        }

        double maxValueAbsc = Math.max(Math.abs(this.graphs3D.get(0)[0][0][0]), Math.abs(this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0]));
        double maxValueOrd = Math.max(Math.abs(this.graphs3D.get(0)[0][0][1]), Math.abs(this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1]));
        double maxValueAppl = Math.max(Math.abs(f_min), Math.abs(f_max));

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = maxValueAbsc;
        border[0][1] = -maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = -maxValueAbsc;
        border[1][1] = -maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = -maxValueAbsc;
        border[2][1] = -maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = maxValueAbsc;
        border[3][1] = -maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], R, r, h, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 180) {
            g.drawString(varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(varAbsc) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= maxValueAppl) {
            lineLevel[0][0] = maxValueAbsc;
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = -maxValueAbsc;
            lineLevel[1][1] = -maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], R, r, h, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], R, r, h, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 180) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        //Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (maxValueAbsc / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= maxValueAbsc) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = -maxValueOrd;
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], R, r, h, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], R, r, h, angle);

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

    private void drawLevelsOnNorth(Graphics g, double f_min, double f_max, double R, double r, double h, double angle) {

        if ((angle >= 90) && (angle <= 270)) {
            return;
        }

        double maxValueAbsc = Math.max(Math.abs(this.graphs3D.get(0)[0][0][0]), Math.abs(this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0]));
        double maxValueOrd = Math.max(Math.abs(this.graphs3D.get(0)[0][0][1]), Math.abs(this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1]));
        double maxValueAppl = Math.max(Math.abs(f_min), Math.abs(f_max));

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -maxValueAbsc;
        border[0][1] = maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = maxValueAbsc;
        border[1][1] = maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = maxValueAbsc;
        border[2][1] = maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = -maxValueAbsc;
        border[3][1] = maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], R, r, h, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle <= 90) {
            g.drawString(varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(varAbsc) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= maxValueAppl) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], R, r, h, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], R, r, h, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle < 90) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (maxValueAbsc / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= maxValueAbsc) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = maxValueOrd;
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], R, r, h, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], R, r, h, angle);

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

    private void drawLevelsBottom(Graphics g, double f_min, double f_max, double R, double r, double h, double angle) {

        double maxValueAbsc = Math.max(Math.abs(this.graphs3D.get(0)[0][0][0]), Math.abs(this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0]));
        double maxValueOrd = Math.max(Math.abs(this.graphs3D.get(0)[0][0][1]), Math.abs(this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1]));
        double maxValueAppl = Math.max(Math.abs(f_min), Math.abs(f_max));

        //Zunächst den Rahmen auf dem Boden zeichnen
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = maxValueAbsc;
        border[0][1] = maxValueOrd;
        border[0][2] = -maxValueAppl;
        border[1][0] = -maxValueAbsc;
        border[1][1] = maxValueOrd;
        border[1][2] = -maxValueAppl;
        border[2][0] = -maxValueAbsc;
        border[2][1] = -maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = maxValueAbsc;
        border[3][1] = -maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], R, r, h, angle);
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
        int bound = (int) (maxValueAbsc / Math.pow(10, this.expX));
        int i = -bound;

        while (i * Math.pow(10, expX) <= maxValueAbsc) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = -maxValueAppl;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], R, r, h, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], R, r, h, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        //horizontale y-Niveaulinien zeichnen
        bound = (int) (maxValueOrd / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= maxValueOrd) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = -maxValueAppl;
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = i * Math.pow(10, expY);
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], R, r, h, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], R, r, h, angle);

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

        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i][j]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i + 1][j]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i + 1][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i][j + 1]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i][j + 1][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i + 1][j + 1]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i + 1][j + 1][2];
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
    private void drawGraphsFromGraphs3DForGraphic(Graphics g, double minExpr, double maxExpr,
            double bigRadius, double smallRadius, double height, double angle) {

        int numberOfIntervalsAlongAbsc = 0;
        int numberOfIntervalsAlongOrd = 0;

        // Anzahl der Intervalle für das Zeichnen ermitteln.
        for (double[][][] graph3DForGraphic : this.graphs3DForGraphic) {
            if (graph3DForGraphic.length > 0) {
                numberOfIntervalsAlongAbsc = graph3DForGraphic.length - 1;
                numberOfIntervalsAlongOrd = graph3DForGraphic[0].length - 1;
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

        for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
            graphicalGraph = new int[numberOfIntervalsAlongAbsc + 1][numberOfIntervalsAlongOrd + 1][2];
            for (int i = 0; i < numberOfIntervalsAlongAbsc + 1; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd + 1; j++) {
                    graphicalGraph[i][j] = convertToPixel(this.graphs3DForGraphic.get(k)[i][j][0], this.graphs3DForGraphic.get(k)[i][j][1],
                            this.graphs3DForGraphic.get(k)[i][j][2], bigRadius, smallRadius, height, angle);
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
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][2]);
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
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[i][j][2]);
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
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][2]);
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
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][2]);
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
                } else {
                    if (copyOfCenters.get(i) < minimalValue) {
                        indexWithLeastElement = i;
                        minimalValue = copyOfCenters.get(i);
                    }
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
    private void drawGraph3D(Graphics g, double angle) {

        //Zunächst weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        /*
         Falls kein echter Graph vorhanden ist, dann nur den weißen
         Hintergrund zeichnen und beenden. GRUND: Zu Beginn wird sofort
         repaint() aufgerufen, welches wiederum paint() aufruft. Dann sind
         aber varAbsc und varOrd nicht initialisiert und es gibt eine
         Exception. Dies wird hiermit verhindert.
         */
        if (this.graphs3D.isEmpty()) {
            return;
        }
        computeExpXExpYExpZ();
        this.graphs3DForGraphic = convertGraphsToCoarserGraphs();

        /*
         Ermittelt den kleinsten und den größten Funktionswert Notwendig, um
         das Farbspektrum im 3D-Graphen zu berechnen!
         */
        double minExpr = Double.NaN;
        double maxExpr = Double.NaN;

        // Zunächst wird geprüft, ob mindestens ein Graph IRGENDWO definiert ist
        boolean graphIsSomewhereDefined = false;
        for (int k = 0; k < this.graphs3DAreDefined.size(); k++) {
            for (int i = 0; i < this.graphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.graphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.graphs3DAreDefined.get(k)[i][j]) {
                        graphIsSomewhereDefined = true;
                        minExpr = this.graphs3DForGraphic.get(k)[i][j][2];
                        maxExpr = this.graphs3DForGraphic.get(k)[i][j][2];
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

        for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
            for (int i = 0; i < this.graphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.graphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.graphs3DAreDefined.get(k)[i][j]) {
                        minExpr = Math.min(minExpr, this.graphs3DForGraphic.get(k)[i][j][2]);
                        maxExpr = Math.max(maxExpr, this.graphs3DForGraphic.get(k)[i][j][2]);
                    }
                }
            }
        }

        drawLevelsOnEast(g, minExpr, maxExpr, bigRadius, smallRadius, height, angle);
        drawLevelsOnSouth(g, minExpr, maxExpr, bigRadius, smallRadius, height, angle);
        drawLevelsOnWest(g, minExpr, maxExpr, bigRadius, smallRadius, height, angle);
        drawLevelsOnNorth(g, minExpr, maxExpr, bigRadius, smallRadius, height, angle);
        drawLevelsBottom(g, minExpr, maxExpr, bigRadius, smallRadius, height, angle);
        drawGraphsFromGraphs3DForGraphic(g, minExpr, maxExpr, bigRadius, smallRadius, height, angle);

    }

    public void drawGraphs3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, Expression... exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(x_0, x_1, x_0, x_1);
        drawGraphs3D();
    }
    
    public void drawGraphs3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, ArrayList<Expression> exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(x_0, x_1, x_0, x_1);
        drawGraphs3D();
    }
    
    private void drawGraphs3D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGraph3D(g, angle);
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
