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

    /**
     * Variablenname für 3D-Graphen: varS = Name des ersten Parameters.
     */
    private String varS;
    /**
     * Variablenname für 3D-Graphen: varS = Name des zweiten Parameters.
     */
    private String varT;
    /**
     * exprs[0], exprs[1] und exprs[2] sind die Komponenten in der
     * Flächendarstellung.
     */
    private Expression[] expr = new Expression[3];
    private double[][][] surfaceGraph3D;
    private double[][][] surfaceGraph3DForGraphic;
    private boolean[][] surfaceGraph3DIsDefined;

    private final ArrayList<Color> colors = new ArrayList<>();

    private final Color color = new Color(170, 170, 70);

    private double minS, maxS, minT, maxT;

    public GraphicPanelSurface() {
        super();
    }

    public Expression[] getExpressions() {
        return this.expr;
    }

    public ArrayList<Color> getColors() {
        if (this.colors.isEmpty()) {
            this.colors.add(Color.BLUE);
        }
        return this.colors;
    }

    public Color getColor() {
        return this.color;
    }

    public void setExpressions(Expression[] exprs) {
        this.expr = exprs;
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
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: expr, varS und varT sind bereits initialisiert.
     */
    private void computeScreenSizes() {
        super.computeScreenSizes(this.surfaceGraph3D, true, true, true);
    }

    /**
     * Berechnet die Gitterpunkte für den 3D-Graphen aus den Ausdrücken in expr.
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
                    this.surfaceGraph3D[i][j][0] = this.expr[0].evaluate();
                    this.surfaceGraph3D[i][j][1] = this.expr[1].evaluate();
                    this.surfaceGraph3D[i][j][2] = this.expr[2].evaluate();
                    this.surfaceGraph3DIsDefined[i][j] = true;
                } catch (EvaluationException e) {
                    this.surfaceGraph3D[i][j][0] = Double.NaN;
                    this.surfaceGraph3D[i][j][1] = Double.NaN;
                    this.surfaceGraph3D[i][j][2] = Double.NaN;
                    this.surfaceGraph3DIsDefined[i][j] = false;
                }
            }
        }

        // Zeichenbereich berechnen.
        computeScreenSizes();

    }

    /**
     * Gibt (eventuell) einen etwas gröberen Graphen zurück, damit dieser
     * gezeichnet werden können.<br>
     * VORAUSSETZUNG: minS, maxS, minT und maxT sind initialisiert.
     */
    private double[][][] convertGraphToCoarserGraph() {

        int numberOfIntervals = (int) (50 * this.zoomfactor);

        if (numberOfIntervals > 50) {
            numberOfIntervals = 50;
        }
        if (numberOfIntervals < 2) {
            numberOfIntervals = 2;
        }
        
        double[][][] graph3DForGraphic = new double[numberOfIntervals][numberOfIntervals][3];
        boolean[][] coarserGraph3DIsDefined = new boolean[numberOfIntervals][numberOfIntervals];

        int currentIndexI, currentIndexJ;

        for (int i = 0; i < numberOfIntervals; i++) {

            if (this.surfaceGraph3D.length <= numberOfIntervals) {
                currentIndexI = i;
            } else {
                currentIndexI = (int) (i * ((double) this.surfaceGraph3D.length - 1) / (numberOfIntervals - 1));
            }

            for (int j = 0; j < numberOfIntervals; j++) {
                if (this.surfaceGraph3D[0].length <= numberOfIntervals) {
                    currentIndexJ = j;
                } else {
                    currentIndexJ = (int) (j * ((double) this.surfaceGraph3D[0].length - 1) / (numberOfIntervals - 1));
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
     * Zeichnet ein (tangentiales) viereckiges Plättchen des 3D-Graphen.
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

    /**
     * Zeichnet die parametrisierte Fläche.
     */
    private void drawSurfaceFromSurfaceForGraphic(Graphics g) {

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
        this.surfaceGraph3DForGraphic = convertGraphToCoarserGraph();

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
        drawSurfaceFromSurfaceForGraphic(g);

    }

    /**
     * Öffentliche Hauptmethode zum Zeichnen einer parametrisierten Fläche.
     */
    public void drawSurface(Expression s_0, Expression s_1, Expression t_0, Expression t_1, Expression[] exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(s_0, s_1, t_0, t_1);
        drawSurface();
    }

    /**
     * Hauptmethode zum Zeichnen einer parametrisierten Fläche.
     */
    private void drawSurface() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawSurface(g);
    }

}
