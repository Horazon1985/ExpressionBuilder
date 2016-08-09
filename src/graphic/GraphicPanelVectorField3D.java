package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.matrixexpression.classes.Matrix;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class GraphicPanelVectorField3D extends AbstractGraphicPanel3D {

    //Parameter für 3D-Graphen
    //Variablennamen der ersten und der zweiten Achse
    private String varAbsc, varOrd, varAppl;
    //Array, indem die Punkte am Graphen gespeichert sind
    private Matrix vectorFieldExpr;
    private final ArrayList<double[]> vectorField3D = new ArrayList<>();
    private final ArrayList<double[]> vectorField3DArrows = new ArrayList<>();

    private final Color color = Color.blue;

    public GraphicPanelVectorField3D() {
        super();
    }

    public Color getColor() {
        return this.color;
    }

    public Matrix getVectorFieldExpression() {
        return this.vectorFieldExpr;
    }

    public void setVectorFieldExpression(Expression[] vectorFieldComponents) {
        Matrix vectorField = new Matrix(vectorFieldComponents);
        this.vectorFieldExpr = vectorField;
        this.vectorField3D.clear();
    }

    public void setParameters(String varAbsc, String varOrd, String varAppl, double bigRadius, double heightProjection, double angle, double verticalAngle) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
        this.varAppl = varAppl;
        this.heightProjection = heightProjection;
        this.bigRadius = bigRadius;
        this.smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
        this.height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);
        this.angle = angle;
        this.verticalAngle = verticalAngle;
        this.zoomfactor = 1;
    }

    /**
     * Voraussetzung: vectorFieldExpr, varAbsc und varOrd sind bereits gesetzt.
     *
     * @throws EvaluationException
     */
    public void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd,
            Expression exprApplStart, Expression exprApplEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();
        double varOrdStart = exprOrdStart.evaluate();
        double varOrdEnd = exprOrdEnd.evaluate();
        double varApplStart = exprApplStart.evaluate();
        double varApplEnd = exprApplEnd.evaluate();

        this.maxX = Math.max(Math.abs(varAbscStart), Math.abs(varAbscEnd));
        this.maxY = Math.max(Math.abs(varOrdStart), Math.abs(varOrdEnd));
        this.maxZ = Math.max(Math.abs(varApplStart), Math.abs(varApplEnd));

    }

    /**
     * Berechnet die Gitterpunkte für den 3D-Graphen aus dem Ausdruck expr.
     *
     * @throws EvaluationException
     */
    private void expressionToVectorField(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd, Expression exprApplStart, Expression exprApplEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();
        double varOrdStart = exprOrdStart.evaluate();
        double varOrdEnd = exprOrdEnd.evaluate();
        double varApplStart = exprApplStart.evaluate();
        double varApplEnd = exprApplEnd.evaluate();

        this.vectorField3D.clear();
        double[] vectorFieldArrow;

        /*
         Falls this.expr.get(i) konstant ist -> den Funktionswert nur
         einmal berechnen!
         */
        if (this.vectorFieldExpr.isConstant()) {
            Variable.setValue(this.varAbsc, varAbscStart);
            Variable.setValue(this.varOrd, varOrdStart);
            Variable.setValue(this.varAppl, varApplStart);
            double constAbscValue, constOrdValue, constApplValue;
            try {
                constAbscValue = this.vectorFieldExpr.getEntry(0, 0).evaluate();
                constOrdValue = this.vectorFieldExpr.getEntry(1, 0).evaluate();
                constApplValue = this.vectorFieldExpr.getEntry(2, 0).evaluate();
            } catch (EvaluationException e) {
                constAbscValue = Double.NaN;
                constOrdValue = Double.NaN;
                constApplValue = Double.NaN;
            }
            for (int i = 0; i <= 20; i++) {
                for (int j = 0; j <= 20; j++) {
                    for (int k = 0; k <= 20; k++) {
                        vectorFieldArrow = new double[6];
                        vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * i / 20;
                        vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 20;
                        vectorFieldArrow[2] = varApplStart + (varApplEnd - varApplStart) * k / 20;
                        vectorFieldArrow[3] = vectorFieldArrow[0] + constAbscValue;
                        vectorFieldArrow[4] = vectorFieldArrow[1] + constOrdValue;
                        vectorFieldArrow[4] = vectorFieldArrow[2] + constApplValue;
                    }
                }
            }
        } else {
            Variable.setValue(this.varAbsc, varAbscStart);
            Variable.setValue(this.varOrd, varOrdStart);
            Variable.setValue(this.varAppl, varApplStart);
            for (int i = 0; i <= 20; i++) {
                for (int j = 0; j <= 20; j++) {
                    for (int k = 0; k <= 20; k++) {
                        vectorFieldArrow = new double[6];
                        vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * i / 20;
                        vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 20;
                        vectorFieldArrow[2] = varApplStart + (varApplEnd - varApplStart) * k / 20;
                        Variable.setValue(this.varAbsc, varAbscStart + (varAbscEnd - varAbscStart) * i / 20);
                        Variable.setValue(this.varOrd, varOrdStart + (varOrdEnd - varOrdStart) * j / 20);
                        Variable.setValue(this.varAppl, varApplStart + (varApplEnd - varApplStart) * k / 20);
                        try {
                            vectorFieldArrow[3] = vectorFieldArrow[0] + this.vectorFieldExpr.getEntry(0, 0).evaluate();
                            vectorFieldArrow[4] = vectorFieldArrow[1] + this.vectorFieldExpr.getEntry(1, 0).evaluate();
                            vectorFieldArrow[5] = vectorFieldArrow[2] + this.vectorFieldExpr.getEntry(2, 0).evaluate();
                        } catch (EvaluationException e) {
                            vectorFieldArrow[3] = Double.NaN;
                            vectorFieldArrow[4] = Double.NaN;
                            vectorFieldArrow[5] = Double.NaN;
                        }
                        this.vectorField3D.add(vectorFieldArrow);
                    }
                }
            }
        }

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen. Voraussetzung:
     * maxX, maxY, maxZ sind bekannt!
     */
    private int[][] convertVectorFieldToGraphicalVectorField(Graphics g) {

        int[][] resultGraphicalVectorField3D = new int[this.vectorField3D.size()][4];
        int[] vectorStart, vectorEnd;
        for (int i = 0; i < this.vectorField3D.size(); i++) {
            vectorStart = convertToPixel(this.vectorField3D.get(i)[0], this.vectorField3D.get(i)[1], this.vectorField3D.get(i)[2]);
            vectorEnd = convertToPixel(this.vectorField3D.get(i)[3], this.vectorField3D.get(i)[4], this.vectorField3D.get(i)[5]);
            resultGraphicalVectorField3D[i] = new int[]{vectorStart[0], vectorStart[1], vectorEnd[0], vectorEnd[1]};
        }
        return resultGraphicalVectorField3D;

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
            g.drawString(this.varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varOrd) - 10, borderPixels[0][1] + 15);
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

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (angle >= 90) {
            g.drawString(this.varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varOrd) - 10, borderPixels[0][1] + 15);
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

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 90 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxY / Math.pow(10, expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = -this.maxX;
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
            g.drawString(this.varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varAbsc) - 10, borderPixels[0][1] + 15);
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
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2]);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2]);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (this.angle > 180 && (i + 1) * Math.pow(10, this.expZ) <= this.maxZ) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, this.expX));
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
            g.drawString(this.varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varAbsc) - 10, borderPixels[0][1] + 15);
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
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, this.expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
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

    /**
     * Hauptmethode zum Zeichnen von 3D-Vektorfeldern.
     */
    private void drawVectorField3D(Graphics g, double angle) {

        //Zunächst weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        computeExpXExpYExpZ();

        drawLevelsOnEast(g);
        drawLevelsOnSouth(g);
        drawLevelsOnWest(g);
        drawLevelsOnNorth(g);
        drawLevelsBottom(g);

        if (this.vectorField3D.isEmpty()) {
            return;
        }

        g.setColor(Color.blue);
        int[][] graphicalVectorField = convertVectorFieldToGraphicalVectorField(g);

        for (int[] vectorArrow : graphicalVectorField) {
            if (!Double.isNaN(vectorArrow[0]) && !Double.isInfinite(vectorArrow[0])
                    && !Double.isNaN(vectorArrow[1]) && !Double.isInfinite(vectorArrow[1])
                    && !Double.isNaN(vectorArrow[2]) && !Double.isInfinite(vectorArrow[2])
                    && !Double.isNaN(vectorArrow[3]) && !Double.isInfinite(vectorArrow[3])) {

                g.drawLine(vectorArrow[0], vectorArrow[1], vectorArrow[2], vectorArrow[3]);
                // Pfeilspitze zeichnen (TO DO).

            }
        }

        g.setColor(Color.black);

    }

    private void drawArrow(Graphics g, int i, int length) {
//        if (angleOfArrow == -1) {
//            return;
//        }
        int[] pointStart = convertToPixel(this.vectorField3DArrows.get(i)[0], this.vectorField3DArrows.get(i)[1], this.vectorField3DArrows.get(i)[2]);
        int[] pointEnd = convertToPixel(this.vectorField3DArrows.get(i)[3], this.vectorField3DArrows.get(i)[4], this.vectorField3DArrows.get(i)[5]);
        g.drawLine(pointStart[0], pointStart[1], pointEnd[0], pointEnd[1]);
        pointStart = convertToPixel(this.vectorField3DArrows.get(i)[6], this.vectorField3DArrows.get(i)[7], this.vectorField3DArrows.get(i)[8]);
        pointEnd = convertToPixel(this.vectorField3DArrows.get(i)[9], this.vectorField3DArrows.get(i)[10], this.vectorField3DArrows.get(i)[11]);
        g.drawLine(pointStart[0], pointStart[1], pointEnd[0], pointEnd[1]);
    }

    public void drawVectorField3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1,
            Expression z_0, Expression z_1, Expression[] vectorFieldComponents) throws EvaluationException {
        this.zoomfactor = 1;
        setVectorFieldExpression(vectorFieldComponents);
        computeScreenSizes(x_0, x_1, y_0, y_1, z_0, z_1);
        expressionToVectorField(x_0, x_1, y_0, y_1, z_0, z_1);
        drawVectorField3D();
    }

    private void drawVectorField3D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawVectorField3D(g, angle);
    }

}
