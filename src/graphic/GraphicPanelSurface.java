package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

public class GraphicPanelSurface extends AbstractGraphicPanel3D {

    // Parameter für 3D-Graphen
    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varS, varT;
    private Expression[] exprs = new Expression[2];
    // Array, indem die Punkte am Graphen gespeichert sind
    private double[][][] surfaceGraph3D;
    private double[][][] surfaceGraph3DForGraphic;
    private boolean[][] surfaceGraph3DIsDefined;
    
    private final ArrayList<Color> colors = new ArrayList<>();

    private final Color color = new Color(170, 170, 70);

    private double minS, maxS, minT, maxT;

    private static final Color gridColorGridOnlyBright = Color.black;
    private static final Color gridColorGridOnlyDark = Color.green;

    public GraphicPanelSurface() {
        super();
    }

    public Expression[] getExpressions() {
        return this.exprs;
    }

    public ArrayList<Color> getColors() {
        if (this.colors.isEmpty()){
            this.colors.add(Color.BLUE);
        }
        return this.colors;
    }
    
    public Color getColor() {
        return this.color;
    }

    public void setExpressions(Expression[] exprs) {
        this.exprs = exprs;
    }

    public void setParameters(String varS, String varT, double bigRadius, double heightProjection, double angle,
            double verticalAngle) {
        this.varS = varS;
        this.varT = varT;
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
    private void computeScreenSizes(Expression exprS_0, Expression exprS_1, Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double s_0 = exprS_0.evaluate();
        double s_1 = exprS_1.evaluate();
        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        double globalMaxX = Double.NaN;
        double globalMaxY = Double.NaN;
        double globalMaxZ = Double.NaN;

        double x, y, z;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {

                Variable.setValue(this.varS, s_0 + i * (s_1 - s_0) / 100);
                Variable.setValue(this.varT, t_0 + j * (t_1 - t_0) / 100);
                try {
                    x = exprs[0].evaluate();
                    y = exprs[1].evaluate();
                    z = exprs[2].evaluate();
                } catch (EvaluationException e) {
                    x = Double.NaN;
                    y = Double.NaN;
                    z = Double.NaN;
                }

                if (!Double.isNaN(x) && !Double.isInfinite(x) && !Double.isNaN(y) && !Double.isInfinite(y)
                        && !Double.isNaN(z) && !Double.isInfinite(z)) {
                    if (Double.isNaN(globalMaxX)) {
                        globalMaxX = Math.abs(x);
                        globalMaxY = Math.abs(y);
                        globalMaxZ = Math.abs(z);
                    } else {
                        globalMaxX = Math.max(globalMaxX, Math.abs(x));
                        globalMaxY = Math.max(globalMaxY, Math.abs(y));
                        globalMaxZ = Math.max(globalMaxZ, Math.abs(z));
                    }
                }
            }
        }

        if (Double.isNaN(globalMaxX) || Double.isInfinite(globalMaxX) || Double.isNaN(globalMaxY) || Double.isInfinite(globalMaxY)
                || Double.isNaN(globalMaxZ) || Double.isInfinite(globalMaxZ)) {
            this.maxX = 1;
            this.maxY = 1;
            this.maxZ = 1;
        } else {
            this.maxX = globalMaxX;
            this.maxY = globalMaxY;
            this.maxZ = globalMaxZ;

            // Falls alle expr.get(i) konstant sind.
            if (this.maxX == 0) {
                this.maxX = 1;
            }
            if (this.maxY == 0) {
                this.maxY = 1;
            }
            if (this.maxZ == 0) {
                this.maxZ = 1;
            }

            // 30 % Rand lassen!
            this.maxX = this.maxX * 1.3;
            this.maxY = this.maxY * 1.3;
            this.maxZ = this.maxZ * 1.3;
        }

    }

    /**
     * Berechnet die Gitterpunkte für den 3D-Graphen aus dem Ausdruck expr.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(Expression exprS_0, Expression exprS_1, Expression exprT_0, Expression exprT_1) throws EvaluationException {

        this.minS = exprS_0.evaluate();
        this.maxS = exprS_1.evaluate();
        this.minT = exprT_0.evaluate();
        this.maxT = exprT_1.evaluate();

        this.surfaceGraph3D = new double[101][101][3];
        this.surfaceGraph3DIsDefined = new boolean[101][101];

        for (int i = 0; i <= 100; i++) {
            for (int j = 0; j <= 100; j++) {
                Variable.setValue(this.varS, this.minS + (this.maxS - this.minS) * i / 100);
                Variable.setValue(this.varT, this.minT + (this.maxT - this.minT) * j / 100);
                try {
                    this.surfaceGraph3D[i][j][0] = this.exprs[0].evaluate();
                    this.surfaceGraph3D[i][j][1] = this.exprs[1].evaluate();
                    this.surfaceGraph3D[i][j][2] = this.exprs[2].evaluate();
                    this.surfaceGraph3DIsDefined[i][j] = true;
                } catch (EvaluationException e) {
                    this.surfaceGraph3D[i][j][0] = Double.NaN;
                    this.surfaceGraph3D[i][j][1] = Double.NaN;
                    this.surfaceGraph3D[i][j][2] = Double.NaN;
                    this.surfaceGraph3DIsDefined[i][j] = false;
                }
            }
        }

        //Zeichenbereich berechnen.
        computeScreenSizes(exprS_0, exprS_1, exprT_0, exprT_1);

    }

    /**
     * Macht die Lösung (eventuell) etwas gröber, damit sie gezeichnet werden
     * kann Voraussetzung: maxX, maxY sind bekannt!
     */
    private double[][][] convertGraphsToCoarserGraphs() {

        int numberOfIntervalsAlongS = Math.min(100, (int) (30 * (this.maxS - this.minS) / (this.maxS * this.zoomfactor)));
        int numberOfIntervalsAlongT = Math.min((int) (100 * (this.maxT - this.minT) / (2 * Math.PI)), (int) (100 / this.zoomfactor * (this.maxT - this.minT) / (2 * Math.PI)));

        double[][][] graph3DForGraphic = new double[numberOfIntervalsAlongS][numberOfIntervalsAlongT][3];
        boolean[][] coarserGraph3DIsDefined = new boolean[numberOfIntervalsAlongS][numberOfIntervalsAlongT];

        int currentIndexI, currentIndexJ;

        for (int i = 0; i < numberOfIntervalsAlongS; i++) {

            if (this.surfaceGraph3D.length <= numberOfIntervalsAlongS) {
                currentIndexI = i;
            } else {
                currentIndexI = (int) (i * ((double) this.surfaceGraph3D.length - 1) / (numberOfIntervalsAlongS - 1));
            }

            for (int j = 0; j < numberOfIntervalsAlongT; j++) {
                if (this.surfaceGraph3D[0].length <= numberOfIntervalsAlongT) {
                    currentIndexJ = j;
                } else {
                    currentIndexJ = (int) (j * ((double) this.surfaceGraph3D[0].length - 1) / (numberOfIntervalsAlongT - 1));
                }
                graph3DForGraphic[i][j][0] = this.surfaceGraph3D[currentIndexI][currentIndexJ][0];
                graph3DForGraphic[i][j][1] = this.surfaceGraph3D[currentIndexI][currentIndexJ][1];
                graph3DForGraphic[i][j][2] = this.surfaceGraph3D[currentIndexI][currentIndexJ][2];
                // Prüft, ob der Funktionswert this.graph3D[i][j][2] definiert ist.
                coarserGraph3DIsDefined[i][j] = !(Double.isNaN(this.surfaceGraph3D[currentIndexI][currentIndexJ][2]) || Double.isInfinite(this.surfaceGraph3D[currentIndexI][currentIndexJ][2]));
            }

        }

        return graph3DForGraphic;

    }

    /**
     * Zeichnet ein (tangentiales) rechteckiges Plättchen des 3D-Graphen
     */
    private void drawInfinitesimalTangentSpace(int x_1, int y_1, int x_2, int y_2,
            int x_3, int y_3, int x_4, int y_4, Graphics g) {

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
     * Zeichnet den ganzen 3D-Graphen bei Übergabe der Pixelkoordinaten (mit
     * Achsen)
     */
    private void drawSurfaceFromSurfaceForGraphic(Graphics g, double minExpr, double maxExpr) {

        if (this.surfaceGraph3DForGraphic.length == 0) {
            return;
        }

        // Anzahl der Intervalle für das Zeichnen ermitteln.
        int numberOfIntervalsAlongAbsc = surfaceGraph3DForGraphic.length - 1;
        int numberOfIntervalsAlongOrd = surfaceGraph3DForGraphic[0].length - 1;

        // Dann können keine Graphen gezeichnet werden.
        if (numberOfIntervalsAlongAbsc == 0 || numberOfIntervalsAlongOrd == 0) {
            return;
        }

        // Koordinaten der einzelnen Graphen in graphische Koordinaten umwandeln
        int[][][] graphicalGraph = new int[numberOfIntervalsAlongAbsc + 1][numberOfIntervalsAlongOrd + 1][2];
        for (int i = 0; i < numberOfIntervalsAlongAbsc + 1; i++) {
            for (int j = 0; j < numberOfIntervalsAlongOrd + 1; j++) {
                graphicalGraph[i][j] = convertToPixel(this.surfaceGraph3DForGraphic[i][j][0], this.surfaceGraph3DForGraphic[i][j][1],
                        this.surfaceGraph3DForGraphic[i][j][2]);
            }
        }

        for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {
            for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {
                drawInfinitesimalTangentSpace(graphicalGraph[i][j][0], graphicalGraph[i][j][1],
                        graphicalGraph[i + 1][j][0], graphicalGraph[i + 1][j][1],
                        graphicalGraph[i + 1][j + 1][0], graphicalGraph[i + 1][j + 1][1],
                        graphicalGraph[i][j + 1][0], graphicalGraph[i][j + 1][1],
                        g);
            }
        }

    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Graphen.
     */
    private void drawSurface(Graphics g) {

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

        /*
         Falls kein echter Graph vorhanden ist, dann nur den weißen
         Hintergrund zeichnen und beenden. GRUND: Zu Beginn wird sofort
         repaint() aufgerufen, welches wiederum paint() aufruft. Dann sind
         aber varAbsc und varOrd nicht initialisiert und es gibt eine
         Exception. Dies wird hiermit verhindert.
         */
        if (this.surfaceGraph3D.length == 0) {
            return;
        }
        computeExpXExpYExpZ();
        this.surfaceGraph3DForGraphic = convertGraphsToCoarserGraphs();

        /*
         Ermittelt den kleinsten und den größten Funktionswert Notwendig, um
         das Farbspektrum im 3D-Graphen zu berechnen!
         */
        double minExpr = Double.NaN;
        double maxExpr = Double.NaN;

        // Zunächst wird geprüft, ob mindestens ein Graph IRGENDWO definiert ist
        for (int i = 0; i < this.surfaceGraph3DForGraphic.length; i++) {
            for (int j = 0; j < this.surfaceGraph3DForGraphic[0].length; j++) {
                if (this.surfaceGraph3DIsDefined[i][j]) {
                    minExpr = this.surfaceGraph3DForGraphic[i][j][2];
                    maxExpr = this.surfaceGraph3DForGraphic[i][j][2];
                    break;
                }
            }
        }

        for (int i = 0; i < this.surfaceGraph3DForGraphic.length; i++) {
            for (int j = 0; j < this.surfaceGraph3DForGraphic[0].length; j++) {
                if (this.surfaceGraph3DIsDefined[i][j]) {
                    minExpr = Math.min(minExpr, this.surfaceGraph3DForGraphic[i][j][2]);
                    maxExpr = Math.max(maxExpr, this.surfaceGraph3DForGraphic[i][j][2]);
                }
            }
        }

        drawLevelsOnEast(g, null, null, null);
        drawLevelsOnSouth(g, null, null, null);
        drawLevelsOnWest(g, null, null, null);
        drawLevelsOnNorth(g, null, null, null);
        drawLevelsBottom(g);
        drawSurfaceFromSurfaceForGraphic(g, minExpr, maxExpr);

    }

    public void drawSurface(Expression s_0, Expression s_1, Expression t_0, Expression t_1, Expression[] exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(s_0, s_1, t_0, t_1);
        drawSurface();
    }

    private void drawSurface() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawSurface(g);
    }

}
