package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.util.ArrayList;

public class GraphicPanelCurves3D extends AbstractGraphicPanel3D {

    //Parameter für 3D-Graphen
    //Variablennamen der ersten und der zweiten Achse
    private String var;
    //Array, indem die Punkte am Graphen gespeichert sind
    private Expression[] expr = new Expression[3];
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
        return this.expr;
    }

    private void setExpression(Expression[] expr) {
        this.expr = expr;
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
                x = expr[0].evaluate();
                y = expr[1].evaluate();
                z = expr[2].evaluate();
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
        tangentVector[0] = this.expr[0].diff(var).simplify();
        tangentVector[1] = this.expr[1].diff(var).simplify();
        tangentVector[2] = this.expr[2].diff(var).simplify();

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
                pointOnCurve[0] = this.expr[0].evaluate();
                pointOnCurve[1] = this.expr[1].evaluate();
                pointOnCurve[2] = this.expr[2].evaluate();
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

    //Berechnet den Achseneintrag m*10^(-k) ohne den Double-typischen Nachkommastellenfehler.
    private BigDecimal roundAxisEntries(int m, int k) {
        if (k >= 0) {
            return new BigDecimal(m).multiply(BigDecimal.TEN.pow(k));
        }
        return new BigDecimal(m).divide(BigDecimal.TEN.pow(-k));
    }

    /**
     * Die folgenden vier Prozeduren zeichnen Niveaulinien am Rand des Graphen.
     * Voraussetzung: max_x, max_y, max_z, R, r, h, angle sind
     * bekannt/initialisiert.
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

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (this.angle >= 270) {
            g.drawString("y", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("y", borderPixels[0][0] - g.getFontMetrics().stringWidth("y") - 10, borderPixels[0][1] + 15);
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

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (this.angle >= 90) {
            g.drawString("y", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("y", borderPixels[0][0] - g.getFontMetrics().stringWidth("y") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZ) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 90 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxY / Math.pow(10, expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxY;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = -this.maxY;
            lineLevel[1][1] = i * Math.pow(10, expY);
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
            g.drawString("x", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("x", borderPixels[0][0] - g.getFontMetrics().stringWidth("x") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
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

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 180 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
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
            g.drawString("x", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("x", borderPixels[0][0] - g.getFontMetrics().stringWidth("x") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
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

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle < 90 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
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

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        // Horizontale y-Niveaulinien zeichnen
        bound = (int) (this.maxY / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = i * Math.pow(10, expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Kurven.
     */
    private void drawCurve3D(Graphics g) {

        //Zunächst weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

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

        computeExpXExpYExpZ();

        drawLevelsOnEast(g);
        drawLevelsOnSouth(g);
        drawLevelsOnWest(g);
        drawLevelsOnNorth(g);
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
