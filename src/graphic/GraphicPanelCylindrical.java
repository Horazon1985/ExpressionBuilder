package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GraphicPanelCylindrical extends AbstractGraphicPanel3D {

    //Parameter für 3D-Graphen
    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varR, varPhi;
    private final ArrayList<Expression> exprs = new ArrayList<>();
    //Array, indem die Punkte am Graphen gespeichert sind
    private ArrayList<double[][][]> cylindricalGraphs3D = new ArrayList<>();
    /*
     "Vergröberte Version" von Graph3D (GRUND: beim herauszoomen dürfen die
     Plättchen am Graphen nicht so klein sein -> Graph muss etwas vergröbert
     werden).
     */
    private ArrayList<double[][][]> cylindricalGraphs3DForGraphic = new ArrayList<>();
    //Gibt an, ob der Funktionswert an der betreffenden Stelle definiert ist.
    private ArrayList<boolean[][]> cylindricalGraphs3DAreDefined = new ArrayList<>();
    // Grundfarben für die einzelnen Graphen.
    private final ArrayList<Color> colors = new ArrayList<>();
    // Fixe Grundfarben für die ersten Graphen. Danach werden die Farben per Zufall generiert.
    private final static Color[] fixedColors = {new Color(170, 170, 70), new Color(170, 70, 170), new Color(70, 170, 170)};

    private double minR, maxR, minPhi, maxPhi;
    
    private static final Color gridColorGridOnlyBright = Color.black;
    private static final Color gridColorGridOnlyDark = Color.green;

    public GraphicPanelCylindrical() {
        super();
    }

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public ArrayList<Color> getColors() {
        return this.colors;
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
        int numberOfColors = Math.max(this.exprs.size(), this.cylindricalGraphs3D.size());
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

    public void setParameters(String varR, String varPhi, double bigRadius, double heightProjection, double angle,
            double verticalAngle) {
        this.varR = varR;
        this.varPhi = varPhi;
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

        if (this.cylindricalGraphs3D.isEmpty()) {

            this.maxX = 1;
            this.maxY = 1;
            this.maxZ = 1;

        } else {

            double globalMaxX = 0, globalMaxY = 0, globalMaxZ = 0;

            for (int k = 0; k < this.cylindricalGraphs3D.size(); k++) {

                if (this.cylindricalGraphs3D.get(0).length == 0) {
                    continue;
                }

                for (int i = 0; i < this.cylindricalGraphs3D.get(k).length; i++) {
                    for (int j = 0; j < this.cylindricalGraphs3D.get(k)[0].length; j++) {
                        globalMaxX = Math.max(globalMaxX, Math.abs(this.cylindricalGraphs3D.get(k)[i][j][0]));
                        globalMaxY = Math.max(globalMaxY, Math.abs(this.cylindricalGraphs3D.get(k)[i][j][1]));
                        if (cylindricalGraphs3DAreDefined.get(k)[i][j]) {
                            globalMaxZ = Math.max(globalMaxZ, Math.abs(this.cylindricalGraphs3D.get(k)[i][j][2]));
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
     * Berechnet die Gitterpunkte für den 3D-Graphen aus dem Ausdruck expr.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(Expression exprR_0, Expression exprR_1, Expression exprPhi_0, Expression exprPhi_1) throws EvaluationException {

        this.minR = exprR_0.evaluate();
        this.maxR = exprR_1.evaluate();
        this.minPhi = exprPhi_0.evaluate();
        this.maxPhi = exprPhi_1.evaluate();

        this.cylindricalGraphs3D = new ArrayList<>();
        this.cylindricalGraphs3DAreDefined = new ArrayList<>();

        double currentR, currentPhi;
        double[][][] singleGraph;
        boolean[][] singleGraphIsDefined;
        /*
         Entlang r wird in 100 Intervalle unterteilt, entlang phi in 
         100 * (this.maxPhi - this.minPhi) / (2 * Math.PI) Intervalle.
         */
        int numberOfIntervalsAlongPhi = (int) (100 * (this.maxPhi - this.minPhi) / (2 * Math.PI));
        for (Expression expr : exprs) {

            singleGraph = new double[101][numberOfIntervalsAlongPhi + 1][3];
            singleGraphIsDefined = new boolean[101][numberOfIntervalsAlongPhi + 1];
            Variable.setValue(this.varR, this.minR);
            Variable.setValue(this.varPhi, this.minPhi);
            for (int i = 0; i <= 100; i++) {
                for (int j = 0; j <= numberOfIntervalsAlongPhi; j++) {
                    currentR = this.minR + (this.maxR - this.minR) * i / 100;
                    currentPhi = this.minPhi + (this.maxPhi - this.minPhi) * j / numberOfIntervalsAlongPhi;
                    singleGraph[i][j][0] = currentR * Math.cos(currentPhi);
                    singleGraph[i][j][1] = currentR * Math.sin(currentPhi);
                    Variable.setValue(this.varR, currentR);
                    Variable.setValue(this.varPhi, currentPhi);
                    try {
                        singleGraph[i][j][2] = expr.evaluate();
                        singleGraphIsDefined[i][j] = true;
                    } catch (EvaluationException e) {
                        singleGraph[i][j][2] = Double.NaN;
                        singleGraphIsDefined[i][j] = false;
                    }
                }
            }
            this.cylindricalGraphs3D.add(singleGraph);
            this.cylindricalGraphs3DAreDefined.add(singleGraphIsDefined);

        }

        //Zeichenbereich berechnen.
        computeMaxXMaxYMaxZ();

    }

    /**
     * Macht die Lösung (eventuell) etwas gröber, damit sie gezeichnet werden
     * kann Voraussetzung: maxX, maxY sind bekannt!
     */
    private ArrayList<double[][][]> convertGraphsToCoarserGraphs() {

        int numberOfIntervalsAlongR = Math.min(100, (int) (30 * (this.maxR - this.minR) / (this.maxR * zoomfactor)));
        // Zur Erinnerung: Einschränkung ist maxPhi - minPhi <= 10 * 2 * pi.
        int numberOfIntervalsAlongPhi = Math.min((int) (100 * (this.maxPhi - this.minPhi) / (2 * Math.PI)), (int) (100 / zoomfactor * (this.maxPhi - this.minPhi) / (2 * Math.PI)));

        ArrayList<double[][][]> graphsForGraphic = new ArrayList<>();

        double[][][] graph3DForGraphic;
        boolean[][] coarserGraph3DIsDefined;
        this.cylindricalGraphs3DAreDefined.clear();
        for (double[][][] graph3D : this.cylindricalGraphs3D) {

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
            this.cylindricalGraphs3DAreDefined.add(coarserGraph3DIsDefined);

        }

        return graphsForGraphic;

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

        switch (backgroundColorMode) {
            case BRIGHT:
                g2.setPaint(gridColorGridOnlyBright);
                break;
            case DARK:
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
    private void drawLevelsOnEast(Graphics g) {

        if (this.angle >= 0 && this.angle <= 180) {
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
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 270) {
            g.drawString("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("2. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
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

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 270 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
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

    private void drawLevelsOnWest(Graphics g) {

        if (this.angle >= 180 && this.angle <= 360) {
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
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (this.angle >= 90) {
            g.drawString("2. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("2. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("2. axis") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZ) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 90 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxY / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -this.maxZ;

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

    private void drawLevelsOnSouth(Graphics g) {

        if (this.angle <= 90 || this.angle >= 270) {
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
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 180) {
            g.drawString("1. axis", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("1. axis", borderPixels[0][0] - g.getFontMetrics().stringWidth("1. axis") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= this.maxZ) {
            lineLevel[0][0] = this.maxX;
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 180) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, this.expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle >= 180) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnNorth(Graphics g) {

        if (this.angle >= 90 && this.angle <= 270) {
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
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2]);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle <= 90) {
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
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle < 90 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, this.expX));
        i = -bound;

        while (i * Math.pow(10, this.expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle <= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsBottom(Graphics g) {

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
        int bound = (int) (this.maxX / Math.pow(10, this.expX));
        int i = -bound;

        while (i * Math.pow(10, this.expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        //horizontale y-Niveaulinien zeichnen
        bound = (int) (this.maxY / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

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

        if (this.cylindricalGraphs3DAreDefined.get(indexOfGraph3D)[i][j]) {
            averageHeightOfTangentSpace += this.cylindricalGraphs3DForGraphic.get(indexOfGraph3D)[i][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.cylindricalGraphs3DAreDefined.get(indexOfGraph3D)[i + 1][j]) {
            averageHeightOfTangentSpace += this.cylindricalGraphs3DForGraphic.get(indexOfGraph3D)[i + 1][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.cylindricalGraphs3DAreDefined.get(indexOfGraph3D)[i][j + 1]) {
            averageHeightOfTangentSpace += this.cylindricalGraphs3DForGraphic.get(indexOfGraph3D)[i][j + 1][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.cylindricalGraphs3DAreDefined.get(indexOfGraph3D)[i + 1][j + 1]) {
            averageHeightOfTangentSpace += this.cylindricalGraphs3DForGraphic.get(indexOfGraph3D)[i + 1][j + 1][2];
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
    private void drawGraphsFromCylindricalGraphs3DForGraphic(Graphics g, double minExpr, double maxExpr) {

        int numberOfIntervalsAlongAbsc = 0;
        int numberOfIntervalsAlongOrd = 0;

        // Anzahl der Intervalle für das Zeichnen ermitteln.
        for (double[][][] cylindricalgraph3DForGraphic : this.cylindricalGraphs3DForGraphic) {
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

        for (int k = 0; k < this.cylindricalGraphs3DForGraphic.size(); k++) {
            graphicalGraph = new int[numberOfIntervalsAlongAbsc + 1][numberOfIntervalsAlongOrd + 1][2];
            for (int i = 0; i < numberOfIntervalsAlongAbsc + 1; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd + 1; j++) {
                    graphicalGraph[i][j] = convertToPixel(this.cylindricalGraphs3DForGraphic.get(k)[i][j][0], this.cylindricalGraphs3DForGraphic.get(k)[i][j][1],
                            this.cylindricalGraphs3DForGraphic.get(k)[i][j][2]);
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

        if (this.angle <= 90) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.cylindricalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.cylindricalGraphs3DForGraphic.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][1],
                                graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][1],
                                g, c);

                    }

                }

            }

        } else if (this.angle <= 180) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.cylindricalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.cylindricalGraphs3DForGraphic.get(indices.get(k))[i][j][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][j][0], graphicalGraphs.get(indices.get(k))[i][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j][0], graphicalGraphs.get(indices.get(k))[i + 1][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j + 1][0], graphicalGraphs.get(indices.get(k))[i + 1][j + 1][1],
                                graphicalGraphs.get(indices.get(k))[i][j + 1][0], graphicalGraphs.get(indices.get(k))[i][j + 1][1],
                                g, c);

                    }

                }

            }

        } else if (this.angle <= 270) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.cylindricalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.cylindricalGraphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][2]);
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
                    for (int k = 0; k < this.cylindricalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.cylindricalGraphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][2]);
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
    private void drawCylindricalGraph3D(Graphics g) {

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
        if (this.cylindricalGraphs3D.isEmpty()) {
            return;
        }
        computeExpXExpYExpZ();
        this.cylindricalGraphs3DForGraphic = convertGraphsToCoarserGraphs();

        /*
         Ermittelt den kleinsten und den größten Funktionswert Notwendig, um
         das Farbspektrum im 3D-Graphen zu berechnen!
         */
        double minExpr = Double.NaN;
        double maxExpr = Double.NaN;

        // Zunächst wird geprüft, ob mindestens ein Graph IRGENDWO definiert ist
        boolean graphIsSomewhereDefined = false;
        for (int k = 0; k < this.cylindricalGraphs3DAreDefined.size(); k++) {
            for (int i = 0; i < this.cylindricalGraphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.cylindricalGraphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.cylindricalGraphs3DAreDefined.get(k)[i][j]) {
                        graphIsSomewhereDefined = true;
                        minExpr = this.cylindricalGraphs3DForGraphic.get(k)[i][j][2];
                        maxExpr = this.cylindricalGraphs3DForGraphic.get(k)[i][j][2];
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

        for (int k = 0; k < this.cylindricalGraphs3DForGraphic.size(); k++) {
            for (int i = 0; i < this.cylindricalGraphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.cylindricalGraphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.cylindricalGraphs3DAreDefined.get(k)[i][j]) {
                        minExpr = Math.min(minExpr, this.cylindricalGraphs3DForGraphic.get(k)[i][j][2]);
                        maxExpr = Math.max(maxExpr, this.cylindricalGraphs3DForGraphic.get(k)[i][j][2]);
                    }
                }
            }
        }

        drawLevelsOnEast(g);
        drawLevelsOnSouth(g);
        drawLevelsOnWest(g);
        drawLevelsOnNorth(g);
        drawLevelsBottom(g);
        drawGraphsFromCylindricalGraphs3DForGraphic(g, minExpr, maxExpr);

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
        drawCylindricalGraph3D(g);
    }

}
