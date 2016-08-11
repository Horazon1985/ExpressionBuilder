package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class GraphicPanelCurves3D extends AbstractGraphicPanel3D {

    //Parameter für 3D-Graphen
    //Variablennamen der ersten und der zweiten Achse
    private String var;
    //Array, indem die Punkte am Graphen gespeichert sind
    private Expression[] exprs = new Expression[3];
    /*
     "Vergröberte Version" von Graph3D (GRUND: beim herauszoomen dürfen die
     Plättchen am Graphen nicht so klein sein -> Graph muss etwas vergröbert
     werden).
     */
    private final ArrayList<double[]> curve3D = new ArrayList<>();

    public GraphicPanelCurves3D(){
        super();
    }

    public Expression[] getExpressions() {
        return this.exprs;
    }

    private void setExpression(Expression[] expr) {
        this.exprs = expr;
        this.curve3D.clear();
    }

    public void setParameters(String var, double bigRadius, double heightProjection, double angle, double verticalAngle) {
        this.var = var;
        this.heightProjection = heightProjection;
        this.bigRadius = bigRadius;
        this.smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
        this.height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);
        this.angle = angle;
        this.verticalAngle = verticalAngle;
        this.zoomfactor = 1;
    }

    /**
     * Voraussetzung: expr, var_1 und var_2 sind bereits gesetzt.
     */
    private void computeScreenSizes(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        double globalMaxX = Double.NaN;
        double globalMaxY = Double.NaN;
        double globalMaxZ = Double.NaN;

        double x, y, z;
        for (int i = 0; i < 100; i++) {

            Variable.setValue(this.var, t_0 + i * (t_1 - t_0) / 100);
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

        this.maxXOrigin = this.maxX;
        this.maxYOrigin = this.maxY;
        this.maxZOrigin = this.maxZ;
        
    }

    /**
     * Berechnet die Gitterpunkte für den 3D-Graphen aus dem Ausdruck expr.
     *
     * @throws EvaluationException
     */
    private void expressionToCurve(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        double t = t_0;
        double h;

        Expression[] tangentVector = new Expression[3];
        tangentVector[0] = this.exprs[0].diff(var).simplify();
        tangentVector[1] = this.exprs[1].diff(var).simplify();
        tangentVector[2] = this.exprs[2].diff(var).simplify();

        // Der Graph der Kurve soll aus maximal 10000 Teilstrecken bestehen.
        for (int i = 0; i < 10000; i++) {

            double[] pointOnCurve = new double[3];
            Variable.setValue(var, t);
            try {
                /*
                 h wird nun passend ermittelt, so dass die Distanz zwischen
                 zwei Punkten auf der Kurve nur wenige Pixel beträgt.
                 */
                if (t_1 >= t_0) {
                    h = (this.maxX / 250 + this.maxY / 250 + this.maxZ / 250) / (Math.abs(tangentVector[0].evaluate()) + Math.abs(tangentVector[1].evaluate())
                            + Math.abs(tangentVector[2].evaluate()));
                } else {
                    h = -(this.maxX / 250 + this.maxY / 250 + this.maxZ / 250) / (Math.abs(tangentVector[0].evaluate()) + Math.abs(tangentVector[1].evaluate())
                            + Math.abs(tangentVector[2].evaluate()));
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
            } else {
                if (t < t_1) {
                    t = t_1;
                }
            }

            Variable.setValue(var, t);
            try {
                pointOnCurve[0] = this.exprs[0].evaluate();
                pointOnCurve[1] = this.exprs[1].evaluate();
                pointOnCurve[2] = this.exprs[2].evaluate();
            } catch (EvaluationException e) {
                pointOnCurve[0] = Double.NaN;
                pointOnCurve[1] = Double.NaN;
                pointOnCurve[2] = Double.NaN;
            }

            this.curve3D.add(pointOnCurve);

            if (t == t_1) {
                break;
            }

        }

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen. Voraussetzung:
     * max_x, max_y sind bekannt!
     */
    private int[][] convertCurveToGraphicalCurve(Graphics g) {

        int[][] result = new int[this.curve3D.size()][2];
        for (int i = 0; i < this.curve3D.size(); i++) {
            result[i] = convertToPixel(this.curve3D.get(i)[0], this.curve3D.get(i)[1], this.curve3D.get(i)[2]);
        }
        return result;

    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Kurven.
     */
    private void drawCurve3D(Graphics g) {

        /*
         Falls kein echter Graph vorhanden ist, dann nur den weißen
         Hintergrund zeichnen und beenden. GRUND: Zu Beginn wird sofort
         repaint() aufgerufen, welches wiederum paint() aufruft. Dann sind
         aber var_1 und var_2 nicht initialisiert und es gibt eine Exception.
         Dies wird hiermit verhindert.
         */
        if (this.curve3D.size() <= 1) {
            return;
        }

        drawLevelsOnEast(g, null, null, null);
        drawLevelsOnSouth(g, null, null, null);
        drawLevelsOnWest(g, null, null, null);
        drawLevelsOnNorth(g, null, null, null);
        drawLevelsBottom(g);

        g.setColor(Color.blue);

        int[][] graphicalCurve = convertCurveToGraphicalCurve(g);
        for (int i = 0; i < graphicalCurve.length - 1; i++) {
            if (!Double.isNaN(this.curve3D.get(i)[0]) && !Double.isInfinite(this.curve3D.get(i)[0])
                    && !Double.isNaN(this.curve3D.get(i + 1)[0]) && !Double.isInfinite(this.curve3D.get(i + 1)[0])
                    && !Double.isNaN(this.curve3D.get(i)[1]) && !Double.isInfinite(this.curve3D.get(i)[1])
                    && !Double.isNaN(this.curve3D.get(i + 1)[1]) && !Double.isInfinite(this.curve3D.get(i + 1)[1])) {

                g.drawLine(graphicalCurve[i][0], graphicalCurve[i][1], graphicalCurve[i + 1][0], graphicalCurve[i + 1][1]);

            }
        }

        g.setColor(Color.black);

    }

    public void drawCurve3D(Expression t_0, Expression t_1, Expression[] expr) throws EvaluationException {
        this.zoomfactor = 1;
        setExpression(expr);
        computeScreenSizes(t_0, t_1);
        expressionToCurve(t_0, t_1);
        drawCurve3D();
    }
    
    private void drawCurve3D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCurve3D(g);
    }

}
