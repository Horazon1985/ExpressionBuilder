package graphic.javafx;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.util.ArrayList;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphicCanvasVectorField2D extends AbstractGraphicCanvas2D {

    /**
     * Funktionsvorschrift für das Vektorfeld als (2x1)-Matrix.
     */
    private Matrix vectorFieldExpr;
    private final List<double[]> vectorField2D = new ArrayList<>();

    private final Color color = Color.BLUE;

    public GraphicCanvasVectorField2D() {
        super(100000000, 0.00000001);
    }

    public Color getColor() {
        return this.color;
    }

    public List<double[]> getvectorField() {
        return this.vectorField2D;
    }

    public Matrix getVectorFieldExpression() {
        return this.vectorFieldExpr;
    }

    public void setVars(String varAbsc, String varOrd) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
    }

    public void setVectorFieldExpression(Expression[] vectorFieldComponents) {
        Matrix vectorField = new Matrix(vectorFieldComponents);
        this.vectorFieldExpr = vectorField;
        this.vectorField2D.clear();
    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: vectorFieldExpr, varAbsc und varOrd sind bereits
     * initialisiert.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();
        double varOrdStart = exprOrdStart.evaluate();
        double varOrdEnd = exprOrdEnd.evaluate();

        this.axeCenterX = (varAbscStart + varAbscEnd) / 2;
        this.axeCenterY = (varOrdStart + varOrdEnd) / 2;
        this.maxX = varAbscEnd - this.axeCenterX;
        this.maxY = varOrdEnd - this.axeCenterY;

    }

    /**
     * Berechnet die Gitterpunkte für den Graphen des Vektorfeldes aus dem
     * Matrizenausdruck vectorFieldExpr.<br>
     * VORAUSSETZUNG: vectorFieldExpr ist bereits initialisiert.
     *
     * @throws EvaluationException
     */
    private void expressionToVectorField(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();
        double varOrdStart = exprOrdStart.evaluate();
        double varOrdEnd = exprOrdEnd.evaluate();

        this.vectorField2D.clear();
        double[] vectorFieldArrow;

        /*
         Falls this.expr.get(i) konstant ist -> den Funktionswert nur
         einmal berechnen!
         */
        if (this.vectorFieldExpr.isConstant()) {
            Variable.setValue(this.varAbsc, varAbscStart);
            Variable.setValue(this.varOrd, varOrdStart);
            double constAbscValue, constOrdValue;
            try {
                constAbscValue = this.vectorFieldExpr.getEntry(0, 0).evaluate();
                constOrdValue = this.vectorFieldExpr.getEntry(1, 0).evaluate();
            } catch (EvaluationException e) {
                constAbscValue = Double.NaN;
                constOrdValue = Double.NaN;
            }
            for (int i = 0; i <= 20; i++) {
                for (int j = 0; j <= 20; j++) {
                    vectorFieldArrow = new double[4];
                    vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * i / 20;
                    vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 20;
                    vectorFieldArrow[2] = vectorFieldArrow[0] + constAbscValue;
                    vectorFieldArrow[3] = vectorFieldArrow[1] + constOrdValue;
                    this.vectorField2D.add(vectorFieldArrow);
                }
            }
        } else {
            Variable.setValue(this.varAbsc, varAbscStart);
            Variable.setValue(this.varOrd, varOrdStart);
            for (int i = 0; i <= 20; i++) {
                for (int j = 0; j <= 20; j++) {
                    vectorFieldArrow = new double[4];
                    vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * i / 20;
                    vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 20;
                    Variable.setValue(this.varAbsc, varAbscStart + (varAbscEnd - varAbscStart) * i / 20);
                    Variable.setValue(this.varOrd, varOrdStart + (varOrdEnd - varOrdStart) * j / 20);
                    try {
                        vectorFieldArrow[2] = vectorFieldArrow[0] + this.vectorFieldExpr.getEntry(0, 0).evaluate();
                        vectorFieldArrow[3] = vectorFieldArrow[1] + this.vectorFieldExpr.getEntry(1, 0).evaluate();
                    } catch (EvaluationException e) {
                        vectorFieldArrow[2] = Double.NaN;
                        vectorFieldArrow[3] = Double.NaN;
                    }
                    this.vectorField2D.add(vectorFieldArrow);
                }
            }
        }

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Vektorfeldes.<br>
     * VORAUSSETZUNG: maxX und maxY sind bereits initialisiert.
     */
    private ArrayList<int[]> convertVectorFieldToGraphicalVectorField() {

        ArrayList<int[]> graphicalVectorField = new ArrayList<>();
        int[] graphicalVectorFieldArrow;

        for (int i = 0; i < this.vectorField2D.size(); i++) {
            graphicalVectorFieldArrow = new int[4];
            graphicalVectorFieldArrow[0] = (int) Math.round(250 + 250 * (this.vectorField2D.get(i)[0] - this.axeCenterX) / this.maxX);
            graphicalVectorFieldArrow[1] = (int) Math.round(250 - 250 * (this.vectorField2D.get(i)[1] - this.axeCenterY) / this.maxY);
            graphicalVectorFieldArrow[2] = (int) Math.round(250 + 250 * (this.vectorField2D.get(i)[2] - this.axeCenterX) / this.maxX);
            graphicalVectorFieldArrow[3] = (int) Math.round(250 - 250 * (this.vectorField2D.get(i)[3] - this.axeCenterY) / this.maxY);
            graphicalVectorField.add(graphicalVectorFieldArrow);
        }

        return graphicalVectorField;

    }

    private void drawVectorField2D(GraphicsContext gc) {

        if (this.vectorField2D.isEmpty()) {
            return;
        }

        ArrayList<int[]> graphicalVectorField = convertVectorFieldToGraphicalVectorField();
        gc.setStroke(this.color);

        double angle;
        for (int[] vectorArrow : graphicalVectorField) {
            if (!Double.isNaN(vectorArrow[0]) && !Double.isInfinite(vectorArrow[0])
                    && !Double.isNaN(vectorArrow[1]) && !Double.isInfinite(vectorArrow[1])
                    && !Double.isNaN(vectorArrow[2]) && !Double.isInfinite(vectorArrow[2])
                    && !Double.isNaN(vectorArrow[3]) && !Double.isInfinite(vectorArrow[3])) {

                gc.strokeLine(vectorArrow[0], vectorArrow[1], vectorArrow[2], vectorArrow[3]);
                // Pfeilspitze zeichnen.
                angle = getAngleOfVector(vectorArrow[0], vectorArrow[1], vectorArrow[2], vectorArrow[3]);
                drawVectorArrow(gc, vectorArrow[2], vectorArrow[3], 5, angle);

            }
        }

        gc.setStroke(Color.BLACK);

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
     * Hauptmethode zum Zeichnen eines Vektorfeldes in kartesischen Koordinaten.
     *
     * @throws EvaluationException
     */
    public void drawVectorField2D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, Expression[] vectorFieldComponents) throws EvaluationException {
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
        setVectorFieldExpression(vectorFieldComponents);
        computeScreenSizes(x_0, x_1, y_0, y_1);
        expressionToVectorField(x_0, x_1, y_0, y_1);
        drawVectorField2D();
    }

    private void drawVectorField2D() {
        draw();
    }

    @Override
    public void draw() {

        GraphicsContext gc = getGraphicsContext2D();
        super.draw();
        drawVectorField2D(gc);

        /*
         Im Folgenden wird das Vektorfeld in einem größeren/kleineren Bereich
         gezeichnet, falls der aktuelle Zoomfaktor derart berechnet wurde,
         dass das Vektorfeld zu grob oder zu klein ist.
         */
        try {
            Constant varAbscStart = new Constant(this.axeCenterX - this.maxX);
            Constant varAbscEnd = new Constant(this.axeCenterX + this.maxX);
            Constant varOrdStart = new Constant(this.axeCenterY - this.maxY);
            Constant varOrdEnd = new Constant(this.axeCenterY + this.maxY);
            if (this.vectorFieldExpr != null) {
                expressionToVectorField(varAbscStart, varAbscEnd, varOrdStart, varOrdEnd);
            }
        } catch (EvaluationException e) {
        }
        convertVectorFieldToGraphicalVectorField();

        if (pointsAreShowable) {
            drawMousePointOnGraph();
        }

    }

    @Override
    protected void drawMousePointOnGraph() {
        GraphicsContext gc = getGraphicsContext2D();
        Variable.setValue(this.varAbsc, convertToEuclideanCoordinateX(this.mouseCoordinateX));
        Variable.setValue(this.varOrd, convertToEuclideanCoordinateY(this.mouseCoordinateY));
        try {
            MatrixExpression vector = this.vectorFieldExpr.evaluate();
            if (vector.isMatrix(2, 1)) {
                int endPointVectorX = convertToPixelX(((Matrix) vector).getEntry(0, 0).add(this.varAbsc).evaluate());
                int endPointVectorY = convertToPixelY(((Matrix) vector).getEntry(1, 0).add(this.varOrd).evaluate());
                drawCirclePoint(gc, this.mouseCoordinateX, this.mouseCoordinateY, true);
                drawVectorLine(gc, this.mouseCoordinateX, this.mouseCoordinateY, endPointVectorX, endPointVectorY, true);
                double angle = getAngleOfVector(this.mouseCoordinateX, this.mouseCoordinateY, endPointVectorX, endPointVectorY);
                drawVectorArrow(gc, endPointVectorX, endPointVectorY, 5, angle);
            }
        } catch (EvaluationException e) {
            // Nichts tun.
        }
    }

}
