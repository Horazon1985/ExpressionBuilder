package graphic.swing;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class GraphicPanelCurves2D extends AbstractGraphicPanel2D {

    /**
     * Parametervariable für die parametrisierte Kurve.
     */
    private String var;
    /**
     * expr[0] und expr[1] sind die Komponenten in der Kurvendarstellung.
     */
    private Expression[] expr = new Expression[2];
    /**
     * Der Graph der 2D-Kurve, gegeben durch seine (numerisch berechneten)
     * Punkte. Die Punkte, welche durch die Elemente im ArrayList gegeben sind,
     * werden durch ein Double-Array mit genau zwei Elementen angegeben und die
     * benachbarten ArrayList-Elemente werden beim Zeichnen miteinander
     * verbunden.
     */
    private final ArrayList<double[]> curve2D = new ArrayList<>();

    public GraphicPanelCurves2D() {
        super(10, 0.1);
    }

    public Expression[] getExpressions() {
        return this.expr;
    }

    public void setVar(String var) {
        this.var = var;
    }

    private void setExpression(Expression[] expr) {
        this.expr = expr;
        this.curve2D.clear();
    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: expr und var ist bereits initialisiert.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        double globalMinX = Double.NaN;
        double globalMaxX = Double.NaN;
        double globalMinY = Double.NaN;
        double globalMaxY = Double.NaN;

        double x, y;
        for (int i = 0; i < 100; i++) {

            Variable.setValue(this.var, t_0 + i * (t_1 - t_0) / 100);
            try {
                x = this.expr[0].evaluate();
                y = this.expr[1].evaluate();
            } catch (EvaluationException e) {
                x = Double.NaN;
                y = Double.NaN;
            }

            if (!Double.isNaN(x) && !Double.isInfinite(x) && !Double.isNaN(y) && !Double.isInfinite(y)) {
                if (Double.isNaN(globalMinX)) {
                    globalMinX = x;
                    globalMaxX = x;
                    globalMinY = y;
                    globalMaxY = y;
                } else {
                    globalMinX = Math.min(globalMinX, x);
                    globalMaxX = Math.max(globalMaxX, x);
                    globalMinY = Math.min(globalMinY, y);
                    globalMaxY = Math.max(globalMaxY, y);
                }
            }
        }

        if (Double.isNaN(globalMinX) || Double.isNaN(globalMaxX) || Double.isInfinite(globalMinX) || Double.isInfinite(globalMaxX)
                || Double.isNaN(globalMinY) || Double.isNaN(globalMaxY) || Double.isInfinite(globalMinY) || Double.isInfinite(globalMaxY)) {
            this.axeCenterX = 0;
            this.axeCenterY = 0;
            this.maxX = 1;
            this.maxY = 1;
        } else {
            this.axeCenterX = (globalMaxX + globalMinX) / 2;
            this.axeCenterY = (globalMaxY + globalMinY) / 2;
            this.maxX = (globalMaxX - globalMinX) / 2;
            this.maxY = (globalMaxY - globalMinY) / 2;

            // Falls alle expr.get(i) konstant sind.
            if (this.maxX == 0) {
                this.maxX = 1;
            }
            if (this.maxY == 0) {
                this.maxY = 1;
            }

            // 30 % Rand lassen!
            this.maxX = this.maxX * 1.3;
            this.maxY = this.maxY * 1.3;
        }

    }

    /**
     * Berechnet die Gitterpunkte für die Graphen aus den Ausdrücken in
     * expr.<br>
     * VORAUSSETZUNG: expr ist initialisiert.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        double t = t_0;
        double h;

        Expression[] tangentVector = new Expression[2];
        tangentVector[0] = this.expr[0].diff(this.var).simplify();
        tangentVector[1] = this.expr[1].diff(this.var).simplify();

        // Der Graph der Kurve soll aus maximal 10000 Teilstrecken bestehen.
        for (int i = 0; i < 10000; i++) {

            double[] pointOnCurve = new double[2];
            Variable.setValue(this.var, t);
            try {
                /*
                 h wird nun passend ermittelt, so dass die Distanz zwischen
                 zwei Punkten auf der Kurve nur wenige Pixel beträgt.
                 */
                if (t_1 >= t_0) {
                    h = (this.maxX / 250 + this.maxY / 250) / (Math.abs(tangentVector[0].evaluate()) + Math.abs(tangentVector[1].evaluate()));
                } else {
                    h = -(this.maxX / 250 + this.maxY / 250) / (Math.abs(tangentVector[0].evaluate()) + Math.abs(tangentVector[1].evaluate()));
                }
            } catch (EvaluationException e) {
                h = (t_1 - t_0) / 1000;
            }

            if (Double.isInfinite(h) || Double.isNaN(h) || Math.abs(h) >= Math.abs((t_1 - t_0)) / 1000) {
                h = (t_1 - t_0) / 1000;
            }

            t = t + h;

            if (t_1 >= t_0) {
                if (t > t_1) {
                    t = t_1;
                }
            } else if (t < t_1) {
                t = t_1;
            }

            Variable.setValue(this.var, t);
            try {
                pointOnCurve[0] = this.expr[0].evaluate();
                pointOnCurve[1] = this.expr[1].evaluate();
            } catch (EvaluationException e) {
                pointOnCurve[0] = Double.NaN;
                pointOnCurve[1] = Double.NaN;
            }

            this.curve2D.add(pointOnCurve);

            if (t == t_1) {
                break;
            }

        }

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen.<br>
     * VORAUSSETZUNG: curve2D ist bereits initialisiert.
     */
    private int[][] convertCurveToGraphicalCurve() {
        int[][] result = new int[this.curve2D.size()][2];
        for (int i = 0; i < this.curve2D.size(); i++) {
            result[i] = convertToPixel(this.curve2D.get(i)[0], this.curve2D.get(i)[1]);
        }
        return result;
    }

    private void drawCurve2D(Graphics g) {

        if (this.curve2D.size() <= 1) {
            return;
        }

        g.setColor(Color.blue);

        int[][] graphicalCurve = convertCurveToGraphicalCurve();
        for (int i = 0; i < graphicalCurve.length - 1; i++) {
            if (!Double.isNaN(this.curve2D.get(i)[0]) && !Double.isInfinite(this.curve2D.get(i)[0])
                    && !Double.isNaN(this.curve2D.get(i + 1)[0]) && !Double.isInfinite(this.curve2D.get(i + 1)[0])
                    && !Double.isNaN(this.curve2D.get(i)[1]) && !Double.isInfinite(this.curve2D.get(i)[1])
                    && !Double.isNaN(this.curve2D.get(i + 1)[1]) && !Double.isInfinite(this.curve2D.get(i + 1)[1])) {

                g.drawLine(graphicalCurve[i][0], graphicalCurve[i][1], graphicalCurve[i + 1][0], graphicalCurve[i + 1][1]);

            }
        }

        g.setColor(Color.black);

    }

    /**
     * Hauptmethode zum Zeichnen einer parametrisierten Kurve.
     *
     * @throws EvaluationException
     */
    public void drawCurve2D(Expression t_0, Expression t_1, Expression[] expr) throws EvaluationException {
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
        setExpression(expr);
        computeScreenSizes(t_0, t_1);
        expressionToGraph(t_0, t_1);
        drawCurve2D();
    }

    private void drawCurve2D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCurve2D(g);
        if (pointsAreShowable) {
            drawMousePointOnGraph(g);
        }
    }

    @Override
    protected void drawMousePointOnGraph(Graphics g) {

        int coarseIndexWithNearestDistance = -1;

        int currentIndex;
        for (int i = 0; i < 50; i++) {
            currentIndex = Math.min((i * this.curve2D.size()) / 50, this.curve2D.size() - 1);
            if (computeDistanceOfPixels(convertToPixel(this.curve2D.get(currentIndex)[0], this.curve2D.get(currentIndex)[1]),
                    new int[]{this.mouseCoordinateX, this.mouseCoordinateY}) < 5 * MOUSE_DISTANCE_FOR_SHOWING_POINT) {
                if (coarseIndexWithNearestDistance == -1) {
                    coarseIndexWithNearestDistance = currentIndex;
                } else if (computeDistanceOfPixels(convertToPixel(this.curve2D.get(currentIndex)[0], this.curve2D.get(currentIndex)[1]),
                        new int[]{this.mouseCoordinateX, this.mouseCoordinateY})
                        < computeDistanceOfPixels(convertToPixel(this.curve2D.get(coarseIndexWithNearestDistance)[0], this.curve2D.get(coarseIndexWithNearestDistance)[1]),
                                new int[]{this.mouseCoordinateX, this.mouseCoordinateY})) {
                    coarseIndexWithNearestDistance = currentIndex;
                }
            }

        }

        if (coarseIndexWithNearestDistance == -1) {
            return;
        }

        int indexLeft, indexRight;

        indexLeft = Math.max(0, coarseIndexWithNearestDistance - this.curve2D.size() / 50);
        indexRight = Math.min(this.curve2D.size() - 1, coarseIndexWithNearestDistance + this.curve2D.size() / 50);

        int[] pixel;
        int indexWithMinimalDistance;
        
        if (computeDistanceOfPixels(convertToPixel(this.curve2D.get(indexLeft)[0], this.curve2D.get(indexLeft)[1]),
                new int[]{this.mouseCoordinateX, this.mouseCoordinateY})
                < computeDistanceOfPixels(convertToPixel(this.curve2D.get(indexRight)[0], this.curve2D.get(indexRight)[1]),
                        new int[]{this.mouseCoordinateX, this.mouseCoordinateY})) {

            indexWithMinimalDistance = coarseIndexWithNearestDistance;
            for (int i = coarseIndexWithNearestDistance; i >= indexLeft; i--) {
                if (computeDistanceOfPixels(convertToPixel(this.curve2D.get(i)[0], this.curve2D.get(i)[1]),
                        new int[]{this.mouseCoordinateX, this.mouseCoordinateY})
                        < computeDistanceOfPixels(convertToPixel(this.curve2D.get(indexWithMinimalDistance)[0], this.curve2D.get(indexWithMinimalDistance)[1]),
                                new int[]{this.mouseCoordinateX, this.mouseCoordinateY})) {
                    indexWithMinimalDistance = i;
                }
            }

        } else {

            indexWithMinimalDistance = coarseIndexWithNearestDistance;
            for (int i = coarseIndexWithNearestDistance; i <= indexRight; i++) {
                if (computeDistanceOfPixels(convertToPixel(this.curve2D.get(i)[0], this.curve2D.get(i)[1]),
                        new int[]{this.mouseCoordinateX, this.mouseCoordinateY})
                        < computeDistanceOfPixels(convertToPixel(this.curve2D.get(indexWithMinimalDistance)[0], this.curve2D.get(indexWithMinimalDistance)[1]),
                                new int[]{this.mouseCoordinateX, this.mouseCoordinateY})) {
                    indexWithMinimalDistance = i;
                }
            }

        }

        if (computeDistanceOfPixels(convertToPixel(this.curve2D.get(indexWithMinimalDistance)[0], this.curve2D.get(indexWithMinimalDistance)[1]),
                new int[]{this.mouseCoordinateX, this.mouseCoordinateY}) > MOUSE_DISTANCE_FOR_SHOWING_POINT){
            return;
        }
        
        pixel = convertToPixel(this.curve2D.get(indexWithMinimalDistance)[0], this.curve2D.get(indexWithMinimalDistance)[1]);
        drawCirclePoint(g, pixel[0], pixel[1], true);
        
    }

}
