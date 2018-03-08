package graphic.swing;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import java.util.List;

public class GraphicPanelVectorFieldPolar extends AbstractGraphicPanel2D {

    private static final int NUMBER_OF_SUBDIV_R = 20;
    private static final int NUMBER_OF_SUBDIV_PHI = 20;

    /**
     * Parametervariable für den Radius 0i0n den Polarkoordinaten.
     */
    private String varR;

    /**
     * Parametervariable für den Winkel in den Polarkoordinaten.
     */
    private String varPhi;

    /**
     * Funktionsvorschrift für das Vektorfeld als (2x1)-Matrix.
     */
    private Matrix vectorFieldExpr;
    private final List<double[]> vectorFieldPolar = new ArrayList<>();

    private final Color color = Color.blue;

    public GraphicPanelVectorFieldPolar() {
        super(10, 0.1);
    }

    public Color getColor() {
        return this.color;
    }

    public List<double[]> getvectorField() {
        return this.vectorFieldPolar;
    }

    public Matrix getVectorFieldExpression() {
        return this.vectorFieldExpr;
    }

    public void setVars(String varR, String varPhi) {
        this.varR = varR;
        this.varPhi = varPhi;
        this.varAbsc = "1. axis";
        this.varOrd = "2. axis";
    }

    public void setVectorFieldExpression(Expression[] vectorFieldComponents) {
        Matrix vectorField = new Matrix(vectorFieldComponents);
        this.vectorFieldExpr = vectorField;
        this.vectorFieldPolar.clear();
    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: vectorFieldExpr, varR und varPhi sind bereits
     * initialisiert.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprRStart, Expression exprREnd, Expression exprPhiStart, Expression exprPhiEnd) throws EvaluationException {

        double varRStart = exprRStart.evaluate();
        double varREnd = exprREnd.evaluate();
        double varPhiStart = exprPhiStart.evaluate();
        double varPhiEnd = exprPhiEnd.evaluate();

        // Muss eventuell noch feiner angepasst werden.
        this.axeCenterX = 0;
        this.axeCenterY = 0;
        this.maxX = varREnd;
        this.maxY = varREnd;

    }

    /**
     * Berechnet die Gitterpunkte für den Graphen des Vektorfeldes aus dem
     * Matrizenausdruck vectorFieldExpr.<br>
     * VORAUSSETZUNG: vectorFieldExpr ist bereits initialisiert.
     *
     * @throws EvaluationException
     */
    private void expressionToVectorField(Expression exprRStart, Expression exprREnd, Expression exprPhiStart, Expression exprPhiEnd) throws EvaluationException {

        double varRStart = exprRStart.evaluate();
        double varREnd = exprREnd.evaluate();
        double varPhiStart = exprPhiStart.evaluate();
        double varPhiEnd = exprPhiEnd.evaluate();

        this.vectorFieldPolar.clear();
        double[] vectorFieldArrow;

        /*
         Falls this.expr.get(i) konstant ist -> den Funktionswert nur
         einmal berechnen!
         */
        double currentR;
        double currentPhi;
        if (this.vectorFieldExpr.isConstant()) {
            Variable.setValue(this.varR, varRStart);
            Variable.setValue(this.varPhi, varPhiStart);
            double constRValue, constPhiValue;
            try {
                constRValue = this.vectorFieldExpr.getEntry(0, 0).evaluate();
                constPhiValue = this.vectorFieldExpr.getEntry(1, 0).evaluate();
            } catch (EvaluationException e) {
                constRValue = Double.NaN;
                constPhiValue = Double.NaN;
            }
            for (int i = 0; i <= NUMBER_OF_SUBDIV_R; i++) {
                currentR = varRStart + (varREnd - varRStart) * i / NUMBER_OF_SUBDIV_R;
                for (int j = 0; j <= NUMBER_OF_SUBDIV_PHI; j++) {
                    currentPhi = varPhiStart + (varPhiEnd - varPhiStart) * j / NUMBER_OF_SUBDIV_PHI;
                    vectorFieldArrow = new double[4];
                    vectorFieldArrow[0] = currentR * Math.cos(currentPhi);
                    vectorFieldArrow[1] = currentR * Math.sin(currentPhi);
                    vectorFieldArrow[2] = vectorFieldArrow[0] + constRValue * Math.cos(constPhiValue);
                    vectorFieldArrow[3] = vectorFieldArrow[1] + constRValue * Math.sin(constPhiValue);
                    this.vectorFieldPolar.add(vectorFieldArrow);
                }
            }
        } else {
            Variable.setValue(this.varR, varRStart);
            Variable.setValue(this.varPhi, varPhiStart);
            double vectorFieldREvaluated;
            double vectorFieldPhiEvaluated;
            for (int i = 0; i <= NUMBER_OF_SUBDIV_R; i++) {
                currentR = varRStart + (varREnd - varRStart) * i / NUMBER_OF_SUBDIV_R;
                for (int j = 0; j <= NUMBER_OF_SUBDIV_PHI; j++) {
                    currentPhi = varPhiStart + (varPhiEnd - varPhiStart) * j / NUMBER_OF_SUBDIV_PHI;
                    vectorFieldArrow = new double[4];
                    vectorFieldArrow[0] = currentR * Math.cos(currentPhi);
                    vectorFieldArrow[1] = currentR * Math.sin(currentPhi);
                    Variable.setValue(this.varR, varRStart + (varREnd - varRStart) * i / NUMBER_OF_SUBDIV_R);
                    Variable.setValue(this.varPhi, varPhiStart + (varPhiEnd - varPhiStart) * j / NUMBER_OF_SUBDIV_PHI);
                    try {
                        vectorFieldREvaluated = this.vectorFieldExpr.getEntry(0, 0).evaluate();
                        vectorFieldPhiEvaluated = this.vectorFieldExpr.getEntry(1, 0).evaluate();
                        vectorFieldArrow[2] = vectorFieldArrow[0] + vectorFieldREvaluated * Math.cos(vectorFieldPhiEvaluated);
                        vectorFieldArrow[3] = vectorFieldArrow[1] + vectorFieldREvaluated * Math.sin(vectorFieldPhiEvaluated);
                    } catch (EvaluationException e) {
                        vectorFieldArrow[2] = Double.NaN;
                        vectorFieldArrow[3] = Double.NaN;
                    }
                    this.vectorFieldPolar.add(vectorFieldArrow);
                }
            }
        }

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Vektorfeldes.<br>
     * VORAUSSETZUNG: maxX und maxY sind bereits initialisiert.
     */
    private List<int[]> convertVectorFieldToGraphicalVectorField() {

        List<int[]> graphicalVectorField = new ArrayList<>();
        int[] graphicalVectorFieldArrow;

        for (int i = 0; i < this.vectorFieldPolar.size(); i++) {
            graphicalVectorFieldArrow = new int[4];
            graphicalVectorFieldArrow[0] = (int) Math.round(250 + 250 * (this.vectorFieldPolar.get(i)[0] - this.axeCenterX) / this.maxX);
            graphicalVectorFieldArrow[1] = (int) Math.round(250 - 250 * (this.vectorFieldPolar.get(i)[1] - this.axeCenterY) / this.maxY);
            graphicalVectorFieldArrow[2] = (int) Math.round(250 + 250 * (this.vectorFieldPolar.get(i)[2] - this.axeCenterX) / this.maxX);
            graphicalVectorFieldArrow[3] = (int) Math.round(250 - 250 * (this.vectorFieldPolar.get(i)[3] - this.axeCenterY) / this.maxY);
            graphicalVectorField.add(graphicalVectorFieldArrow);
        }

        return graphicalVectorField;

    }

    private void drawVectorFieldPolar(Graphics g) {

        if (this.vectorFieldPolar.isEmpty()) {
            return;
        }

        List<int[]> graphicalVectorField = convertVectorFieldToGraphicalVectorField();
        g.setColor(this.color);

        double angle;
        for (int[] vectorArrow : graphicalVectorField) {
            if (!Double.isNaN(vectorArrow[0]) && !Double.isInfinite(vectorArrow[0])
                    && !Double.isNaN(vectorArrow[1]) && !Double.isInfinite(vectorArrow[1])
                    && !Double.isNaN(vectorArrow[2]) && !Double.isInfinite(vectorArrow[2])
                    && !Double.isNaN(vectorArrow[3]) && !Double.isInfinite(vectorArrow[3])) {

                g.drawLine(vectorArrow[0], vectorArrow[1], vectorArrow[2], vectorArrow[3]);
                // Pfeilspitze zeichnen.
                angle = getAngleOfVector(vectorArrow[0], vectorArrow[1], vectorArrow[2], vectorArrow[3]);
                drawVectorArrow(g, vectorArrow[2], vectorArrow[3], 5, angle);

            }
        }

        g.setColor(Color.black);

    }

    private double getAngleOfVector(int xStart, int yStart, int xEnd, int yEnd) {

        int x = xEnd - xStart, y = yEnd - yStart;
        if (x == 0 && y == 0) {
            return -1;
        }

        if (y == 0) {
            if (x > 0) {
                return 0;
            }
            return Math.PI;
        }
        if (x == 0) {
            if (y > 0) {
                return Math.PI / 2;
            }
            return 3 * Math.PI / 2;
        }

        if (x > 0) {
            if (y > 0) {
                return Math.atan(y / x);
            }
            return Math.atan(y / x) + 2 * Math.PI;
        }
        return Math.atan(y / x) + Math.PI;

    }

    /**
     * Hauptmethode zum Zeichnen eines Vektorfeldes in Polarkoordinaten.
     *
     * @throws EvaluationException
     */
    public void drawVectorFieldPolar(Expression rStart, Expression rEnd, Expression phiStart, Expression phiEnd, Expression[] vectorFieldComponents) throws EvaluationException {
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
        setVectorFieldExpression(vectorFieldComponents);
        computeScreenSizes(rStart, rEnd, phiStart, phiEnd);
        expressionToVectorField(rStart, rEnd, phiStart, phiEnd);
        drawVectorFieldPolar();
    }

    private void drawVectorFieldPolar() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawVectorFieldPolar(g);
        if (pointsAreShowable) {
            drawMousePointOnGraph(g);
        }
    }

    @Override
    protected void drawMousePointOnGraph(Graphics g) {
        double x = convertToEuclideanCoordinateX(this.mouseCoordinateX);
        double y = convertToEuclideanCoordinateY(this.mouseCoordinateY);
        double[] polarRep = getPolarRepresentation(x, y);
        Variable.setValue(this.varR, polarRep[0]);
        Variable.setValue(this.varPhi, polarRep[1]);
        try {
            MatrixExpression vector = this.vectorFieldExpr.evaluate();
            if (vector.isMatrix(2, 1)) {
                double currentR = ((Matrix) vector).getEntry(0, 0).evaluate();
                double currentPhi = ((Matrix) vector).getEntry(1, 0).evaluate();
                int endPointVectorX = convertToPixelX(x + currentR * Math.cos(currentPhi));
                int endPointVectorY = convertToPixelY(y + currentR * Math.sin(currentPhi));
                drawCirclePoint(g, this.mouseCoordinateX, this.mouseCoordinateY, true);
                drawVectorLine(g, this.mouseCoordinateX, this.mouseCoordinateY, endPointVectorX, endPointVectorY, true);
                double angle = getAngleOfVector(this.mouseCoordinateX, this.mouseCoordinateY, endPointVectorX, endPointVectorY);
                drawVectorArrow(g, endPointVectorX, endPointVectorY, 5, angle);
            }
        } catch (EvaluationException e) {
            // Nichts tun.
        }
    }

    private double[] getPolarRepresentation(double x, double y) {
        double[] polarRep = new double[2];
        polarRep[0] = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        polarRep[1] = Math.atan2(x, y);
        return polarRep;
    }

}
