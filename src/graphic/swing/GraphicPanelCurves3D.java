package graphic.swing;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class GraphicPanelCurves3D extends AbstractGraphicPanel3D {

    /**
     * Parametervariable für die parametrisierte Kurve.
     */
    private String var;
    /**
     * expr[0] und expr[1] sind die Komponenten in der Kurvendarstellung.
     */ 
    private Expression[] exprs = new Expression[3];
    /**
     * Der Graph der 3D-Kurve, gegeben durch seine (numerisch berechneten)
     * Punkte. Die Punkte, welche durch die Elemente im ArrayList gegeben sind,
     * werden durch ein Double-Array mit genau drei Elementen angegeben und die
     * benachbarten ArrayList-Elemente werden beim Zeichnen miteinander
     * verbunden.
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
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: expr und var sind bereits initialisiert.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        this.minX = Double.NaN;
        this.minY = Double.NaN;
        this.minZ = Double.NaN;
        this.maxX = Double.NaN;
        this.maxY = Double.NaN;
        this.maxZ = Double.NaN;

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
                if (Double.isNaN(this.maxX)) {
                    this.minX = x;
                    this.minY = y;
                    this.minZ = z;
                    this.maxX = x;
                    this.maxY = y;
                    this.maxZ = z;
                } else {
                    this.minX = Math.min(this.minX, x);
                    this.minY = Math.min(this.minY, y);
                    this.minZ = Math.min(this.minZ, z);
                    this.maxX = Math.max(this.maxX, x);
                    this.maxY = Math.max(this.maxY, y);
                    this.maxZ = Math.max(this.maxZ, z);
                }
            }
        }

        if (Double.isNaN(this.minX) || Double.isInfinite(this.minX) || Double.isNaN(this.maxX) || Double.isInfinite(this.maxX)
                || Double.isNaN(this.minY) || Double.isInfinite(this.minY) || Double.isNaN(this.maxY) || Double.isInfinite(this.maxY)
                || Double.isNaN(this.minZ) || Double.isInfinite(this.minZ) || Double.isNaN(this.maxZ) || Double.isInfinite(this.maxZ)) {
            this.minX = -1;
            this.minY = -1;
            this.minZ = -1;
            this.maxX = 1;
            this.maxY = 1;
            this.maxZ = 1;
        } else {
            // Falls alle expr.get(i) konstant sind.
            if (this.minX == this.maxX) {
                this.minX = this.minX - 1;
                this.maxX = this.maxX + 1;
            }
            if (this.minY == this.maxY) {
                this.minY = this.minY - 1;
                this.maxY = this.maxY + 1;
            }
            if (this.minZ == this.maxZ) {
                this.minZ = this.minZ - 1;
                this.maxZ = this.maxZ + 1;
            }
            // 30 % Rand lassen!
            this.maxX = this.maxX + 0.3*(this.maxX - this.minX);
            this.minX = this.minX - 0.3*(this.maxX - this.minX);
            this.maxY = this.maxY + 0.3*(this.maxY - this.minY);
            this.minY = this.minY - 0.3*(this.maxY - this.minY);
            this.maxZ = this.maxZ + 0.3*(this.maxZ - this.minZ);
            this.minZ = this.minZ - 0.3*(this.maxZ - this.minZ);
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
     * Berechnet die Gitterpunkte für die Graphen aus den Ausdrücken in
     * expr.<br>
     * VORAUSSETZUNG: expr ist initialisiert.
     *
     * @throws EvaluationException
     */
    private void expressionToCurve(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        double t = t_0;
        double h;

        Expression[] tangentVector = new Expression[3];
        tangentVector[0] = this.exprs[0].diff(this.var).simplify();
        tangentVector[1] = this.exprs[1].diff(this.var).simplify();
        tangentVector[2] = this.exprs[2].diff(this.var).simplify();

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
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen.<br>
     * VORAUSSETZUNG: curve3D ist bereits initialisiert.
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
