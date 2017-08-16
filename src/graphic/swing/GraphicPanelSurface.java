package graphic.swing;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

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
            this.colors.add(this.color);
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
    private void convertGraphToCoarserGraph() {

        int numberOfIntervals = (int) (50 * this.zoomfactor);

        if (numberOfIntervals > 50) {
            numberOfIntervals = 50;
        }
        if (numberOfIntervals < 2) {
            numberOfIntervals = 2;
        }

        this.surfaceGraph3DForGraphic = new double[numberOfIntervals + 1][numberOfIntervals + 1][3];
        boolean[][] coarserGraph3DIsDefined = new boolean[numberOfIntervals + 1][numberOfIntervals + 1];

        int currentIndexI, currentIndexJ;

        for (int i = 0; i <= numberOfIntervals; i++) {

            if (this.surfaceGraph3D.length <= numberOfIntervals) {
                currentIndexI = this.surfaceGraph3D.length - 1;
            } else {
                currentIndexI = (int) (i * ((double) this.surfaceGraph3D.length - 1) / numberOfIntervals);
            }

            for (int j = 0; j <= numberOfIntervals; j++) {
                if (this.surfaceGraph3D[0].length <= numberOfIntervals) {
                    currentIndexJ = this.surfaceGraph3D[0].length - 1;
                } else {
                    currentIndexJ = (int) (j * ((double) this.surfaceGraph3D[0].length - 1) / numberOfIntervals);
                }
                this.surfaceGraph3DForGraphic[i][j][0] = this.surfaceGraph3D[currentIndexI][currentIndexJ][0];
                this.surfaceGraph3DForGraphic[i][j][1] = this.surfaceGraph3D[currentIndexI][currentIndexJ][1];
                this.surfaceGraph3DForGraphic[i][j][2] = this.surfaceGraph3D[currentIndexI][currentIndexJ][2];
                // Prüft, ob der Funktionswert this.graph3D[i][j][2] definiert ist.
                coarserGraph3DIsDefined[i][j] = !(Double.isNaN(this.surfaceGraph3D[currentIndexI][currentIndexJ][2]) || Double.isInfinite(this.surfaceGraph3D[currentIndexI][currentIndexJ][2]));
            }

        }

        this.abstractGraph3D = (ArrayList<TangentPolygon>[][]) Array.newInstance(new ArrayList<TangentPolygon>().getClass(), numberOfIntervals, numberOfIntervals);
        TangentPolygon polygon;
        int indexI, indexJ;
        double centerX, centerY;
        for (int i = 0; i < numberOfIntervals; i++) {
            for (int j = 0; j < numberOfIntervals; j++) {

                polygon = new TangentPolygon();
                if (coarserGraph3DIsDefined[i][j]) {
                    polygon.addPoint(this.surfaceGraph3DForGraphic[i][j]);
                }
                if (coarserGraph3DIsDefined[i + 1][j]) {
                    polygon.addPoint(this.surfaceGraph3DForGraphic[i + 1][j]);
                }
                if (coarserGraph3DIsDefined[i + 1][j + 1]) {
                    polygon.addPoint(this.surfaceGraph3DForGraphic[i + 1][j + 1]);
                }
                if (coarserGraph3DIsDefined[i][j + 1]) {
                    polygon.addPoint(this.surfaceGraph3DForGraphic[i][j + 1]);
                }
                centerX = polygon.getCenterX();
                centerY = polygon.getCenterY();
                indexI = (int) (numberOfIntervals * (centerX - this.minXOrigin) / (this.maxXOrigin - this.minXOrigin));
                indexJ = (int) (numberOfIntervals * (centerY - this.minYOrigin) / (this.maxYOrigin - this.minYOrigin));
                if (0 <= indexI && indexI < numberOfIntervals && 0 <= indexJ && indexJ < numberOfIntervals) {
                    if (this.abstractGraph3D[indexI][indexJ] == null) {
                        this.abstractGraph3D[indexI][indexJ] = new ArrayList<>();
                    }
                    this.abstractGraph3D[indexI][indexJ].add(polygon);
                }

            }
        }

        // In jedem Segment die einzelnen Tangentialplättchen der Höhe nach sortieren.
        for (int i = 0; i < numberOfIntervals; i++) {
            for (int j = 0; j < numberOfIntervals; j++) {
                if (this.abstractGraph3D[i][j] == null) {
                    this.abstractGraph3D[i][j] = new ArrayList<>();
                }
                Collections.sort(this.abstractGraph3D[i][j]);
            }
        }

    }

    /**
     * Zeichnet die parametrisierte Fläche.
     */
    private void drawSurfaceFromSurfaceForGraphic(Graphics g) {

        if (this.surfaceGraph3DForGraphic.length == 0) {
            return;
        }

        // Anzahl der Intervalle für das Zeichnen ermitteln.
        int numberOfIntervalsAlongAbsc = this.abstractGraph3D.length - 1;
        int numberOfIntervalsAlongOrd = this.abstractGraph3D[0].length - 1;

        // Dann können keine Graphen gezeichnet werden.
        if (numberOfIntervalsAlongAbsc == 0 || numberOfIntervalsAlongOrd == 0) {
            return;
        }

        if (this.angle <= 90) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    if (this.abstractGraph3D[i][numberOfIntervalsAlongOrd - j - 1].isEmpty()) {
                        continue;
                    }
                    for (int k = 0; k < this.abstractGraph3D[i][numberOfIntervalsAlongOrd - j - 1].size(); k++) {
                        Color c = computeColor(this.color, this.minZOrigin, this.maxZOrigin, this.abstractGraph3D[i][numberOfIntervalsAlongOrd - j - 1].get(k).getCenterZ());
                        drawInfinitesimalTangentSpace(this.abstractGraph3D[i][numberOfIntervalsAlongOrd - j - 1].get(k), g, c);
                    }

                }
            }

        } else if (this.angle <= 180) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    if (this.abstractGraph3D[i][j].isEmpty()) {
                        continue;
                    }
                    for (int k = 0; k < this.abstractGraph3D[i][j].size(); k++) {
                        Color c = computeColor(this.color, this.minZ, this.maxZ, this.abstractGraph3D[i][j].get(k).getCenterZ());
                        drawInfinitesimalTangentSpace(this.abstractGraph3D[i][j].get(k), g, c);
                    }

                }
            }

        } else if (this.angle <= 270) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    if (this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][j].isEmpty()) {
                        continue;
                    }
                    for (int k = 0; k < this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][j].size(); k++) {
                        Color c = computeColor(this.color, this.minZ, this.maxZ, this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][j].get(k).getCenterZ());
                        drawInfinitesimalTangentSpace(this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][j].get(k), g, c);
                    }

                }
            }

        } else {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    if (this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1].isEmpty()) {
                        continue;
                    }
                    for (int k = 0; k < this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1].size(); k++) {
                        Color c = computeColor(this.color, this.minZ, this.maxZ, this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1].get(k).getCenterZ());
                        drawInfinitesimalTangentSpace(this.abstractGraph3D[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1].get(k), g, c);
                    }

                }
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

        convertGraphToCoarserGraph();

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
