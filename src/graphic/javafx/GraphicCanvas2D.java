package graphic.javafx;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphicCanvas2D extends AbstractGraphicCanvas2D {

    /**
     * Funktionsterme für Graphen.
     */
    private List<Expression> exprs = new ArrayList<>();
    private final List<double[][]> graphs2D = new ArrayList<>();
    private final List<Color> COLORS = new ArrayList<>();

    public GraphicCanvas2D() {
        super(100000000, 0.00000001);
    }

    public List<double[][]> getGraphs() {
        return this.graphs2D;
    }

    public List<Color> getColors() {
        return this.COLORS;
    }

    public List<Expression> getExpressions() {
        return this.exprs;
    }

    public void setVarAbsc(String varAbsc) {
        this.varAbsc = varAbsc;
        this.varOrd = null;
    }

    public void setVars(String varAbsc, String varOrd) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
    }

    public void setExpressions(List<Expression> exprs) {
        this.exprs = exprs;
        this.graphs2D.clear();
        this.COLORS.clear();
        setColors();
    }

    public void setExpressions(Expression... exprs) {
        this.exprs = new ArrayList<>();
        this.exprs.addAll(Arrays.asList(exprs));
        this.graphs2D.clear();
        this.COLORS.clear();
        this.specialPoints = null;
        setColors();
    }

    public void addExpression(Expression expr) {
        this.exprs.add(expr);
        setColors();
    }

    public void setGraph(double[][] graph) {
        this.exprs.clear();
        this.graphs2D.clear();
        this.graphs2D.add(graph);
        this.specialPoints = null;
        setColors();
    }

    private void setColors() {
        int numberOfColors = Math.max(this.exprs.size(), this.graphs2D.size());
        this.COLORS.clear();
        for (int i = 0; i < numberOfColors; i++) {
            if (i < FIXED_COLORS.length) {
                this.COLORS.add(FIXED_COLORS[i]);
            } else {
                this.COLORS.add(generateColor());
            }
        }
    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: graphs2D ist bereits initialisiert.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes() {

        this.axeCenterX = 0;
        this.axeCenterY = 0;

        double globalMinY = Double.NaN;
        double globalMaxY = Double.NaN;

        if (!this.graphs2D.isEmpty()) {
            this.axeCenterX = (this.graphs2D.get(0)[this.graphs2D.get(0).length - 1][0] + this.graphs2D.get(0)[0][0]) / 2;
            this.maxX = (this.graphs2D.get(0)[this.graphs2D.get(0).length - 1][0] - this.graphs2D.get(0)[0][0]) / 2;
        }

        for (double[][] graph2D : this.graphs2D) {
            if (graph2D.length > 0) {
                for (double[] graph : graph2D) {
                    if (!(Double.isNaN(graph[1])) && !(Double.isInfinite(graph[1]))) {
                        if (Double.isNaN(globalMinY)) {
                            globalMinY = graph[1];
                            globalMaxY = graph[1];
                        } else {
                            globalMinY = Math.min(globalMinY, graph[1]);
                            globalMaxY = Math.max(globalMaxY, graph[1]);
                        }
                    }
                }
            }
        }

        if (Double.isNaN(globalMinY) || Double.isNaN(globalMaxY) || Double.isInfinite(globalMinY) || Double.isInfinite(globalMaxY)) {
            this.axeCenterX = 0;
            this.axeCenterY = 0;
            this.maxX = 1;
            this.maxY = 1;
        } else {
            this.axeCenterY = (globalMaxY + globalMinY) / 2;
            this.maxY = globalMaxY - this.axeCenterY;
            // 20 % Rand lassen!
            this.maxY = this.maxY * 1.2;
        }

    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: graphs2D ist bereits initialisiert.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();

        this.axeCenterX = (varAbscStart + varAbscEnd) / 2;
        this.maxX = varAbscEnd - this.axeCenterX;
        double globalMinY = Double.NaN;
        double globalMaxY = Double.NaN;

        double y;
        for (Expression expr : this.exprs) {
            for (int j = 0; j < 100; j++) {
                Variable.setValue(this.varAbsc, varAbscStart + j * (varAbscEnd - varAbscStart) / 100);
                try {
                    y = expr.evaluate();
                } catch (EvaluationException e) {
                    y = Double.NaN;
                }
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    if (Double.isNaN(globalMinY)) {
                        globalMinY = y;
                        globalMaxY = y;
                    } else {
                        globalMinY = Math.min(globalMinY, y);
                        globalMaxY = Math.max(globalMaxY, y);
                    }
                }
            }
        }

        if (Double.isNaN(globalMinY) || Double.isNaN(globalMaxY) || Double.isInfinite(globalMinY) || Double.isInfinite(globalMaxY)) {
            this.axeCenterY = 0;
            this.maxY = 1;
        } else {
            this.axeCenterY = (globalMaxY + globalMinY) / 2;
            this.maxY = (globalMaxY - globalMinY) / 2;

            // Falls alle exprs.get(i) konstant sind.
            if (this.maxY < 0.000000001) {
                this.maxY = 1;
            }

            // 20 % Rand lassen!
            this.maxY = this.maxY * 1.2;
        }

    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: graphs2D ist bereits initialisiert.
     *
     * @throws EvaluationException
     */
    public void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {

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
     * Berechnet die Gitterpunkte für die Graphen aus den Ausdrücken in
     * expr.<br>
     * VORAUSSETZUNG: exprs wurde initialisiert.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(Expression exprAbscStart, Expression exprAbscEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();

        this.graphs2D.clear();
        double[][] pointsOnGraphs;

        for (int i = 0; i < this.exprs.size(); i++) {

            pointsOnGraphs = new double[1001][2];

            /*
             Falls this.expr.get(i) konstant ist -> den Funktionswert nur
             einmal berechnen!
             */
            if (this.exprs.get(i).isConstant()) {
                Variable.setValue(this.varAbsc, varAbscStart);
                double constOrdValue;
                try {
                    constOrdValue = this.exprs.get(i).evaluate();
                } catch (EvaluationException e) {
                    constOrdValue = Double.NaN;
                }
                for (int j = 0; j <= 1000; j++) {
                    pointsOnGraphs[j][0] = varAbscStart + (varAbscEnd - varAbscStart) * j / 1000;
                    pointsOnGraphs[j][1] = constOrdValue;
                }
            } else {
                Variable.setValue(this.varAbsc, varAbscStart);
                for (int j = 0; j <= 1000; j++) {
                    pointsOnGraphs[j][0] = varAbscStart + (varAbscEnd - varAbscStart) * j / 1000;
                    Variable.setValue(this.varAbsc, varAbscStart + (varAbscEnd - varAbscStart) * j / 1000);
                    try {
                        pointsOnGraphs[j][1] = this.exprs.get(i).evaluate();
                    } catch (EvaluationException e) {
                        pointsOnGraphs[j][1] = Double.NaN;
                    }
                }
            }

            this.graphs2D.add(i, pointsOnGraphs);

        }

    }

    /**
     * Berechnet die Pixelkoordinaten des gröberen Graphen, welcher für das
     * eigentliche Zeichnen verwendet wird.<br>
     * VORAUSSETZUNG: maxX und maxY sind initialisiert.
     */
    private HashMap<Integer, int[][]> convertGraphToGraphicalGraph() {

        HashMap<Integer, int[][]> result = new HashMap<>();

        for (int i = 0; i < this.graphs2D.size(); i++) {

            int[][] graphicalGraph = new int[this.graphs2D.get(i).length][2];
            for (int j = 0; j < this.graphs2D.get(i).length; j++) {
                graphicalGraph[j][0] = (int) Math.round(250 + 250 * (this.graphs2D.get(i)[j][0] - this.axeCenterX) / this.maxX);
                graphicalGraph[j][1] = (int) Math.round(250 - 250 * (this.graphs2D.get(i)[j][1] - this.axeCenterY) / this.maxY);
            }
            result.put(i, graphicalGraph);

        }

        return result;

    }

    /**
     * Hauptmethode zum Zeichnen von Graphen, die von den öffentlichen Methoden
     * drawGraphs2D(...) aufgerufen wird.
     *
     * @throws EvaluationException
     */
    private void drawGraphs2D(GraphicsContext gc) {

        if (this.graphs2D.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.graphs2D.size(); i++) {

            gc.setStroke(this.COLORS.get(i));

            if (this.graphs2D.get(i).length > 1) {

                HashMap<Integer, int[][]> graphicalGraph = convertGraphToGraphicalGraph();
                for (int j = 0; j < graphicalGraph.get(i).length - 1; j++) {
                    if (!Double.isNaN(graphs2D.get(i)[j][1]) && !Double.isInfinite(graphs2D.get(i)[j][1])
                            && !Double.isNaN(graphs2D.get(i)[j + 1][1]) && !Double.isInfinite(graphs2D.get(i)[j + 1][1])) {

                        if (this.axeCenterY + this.maxY < this.graphs2D.get(i)[j][1] && this.axeCenterY - this.maxY > this.graphs2D.get(i)[j + 1][1]) {
                            gc.strokeLine(graphicalGraph.get(i)[j][0], 0, graphicalGraph.get(i)[j + 1][0], 500);
                        } else if (this.axeCenterY - this.maxY > this.graphs2D.get(i)[j][1] && this.axeCenterY + this.maxY < this.graphs2D.get(i)[j + 1][1]) {
                            gc.strokeLine(graphicalGraph.get(i)[j][0], 500, graphicalGraph.get(i)[j + 1][0], 0);
                        } else if (this.axeCenterY + 2 * this.maxY >= this.graphs2D.get(i)[j][1] && this.axeCenterY - 2 * this.maxY <= this.graphs2D.get(i)[j][1]
                                && this.axeCenterY + 2 * this.maxY >= this.graphs2D.get(i)[j + 1][1] && this.axeCenterY - 2 * this.maxY <= this.graphs2D.get(i)[j + 1][1]) {
                            gc.strokeLine(graphicalGraph.get(i)[j][0], graphicalGraph.get(i)[j][1], graphicalGraph.get(i)[j + 1][0], graphicalGraph.get(i)[j + 1][1]);
                        }

                    }
                }

            }

        }

        gc.setStroke(Color.BLACK);

    }

    /**
     * Hauptmethode zum Zeichnen von Graphen.
     *
     * @throws EvaluationException
     */
    public void drawGraphs2D(Expression x_0, Expression x_1, List<Expression> exprs) throws EvaluationException {
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
        setExpressions(exprs);
        computeScreenSizes(x_0, x_1);
        expressionToGraph(x_0, x_1);
        drawGraphs2D();
    }

    /**
     * Hauptmethode zum Zeichnen von Graphen in kartesischen Koordinaten.
     *
     * @throws EvaluationException
     */
    public void drawGraphs2D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, List<Expression> exprs) throws EvaluationException {
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
        setExpressions(exprs);
        computeScreenSizes(x_0, x_1, y_0, y_1);
        expressionToGraph(x_0, x_1);
        drawGraphs2D();
    }

    /**
     * Hauptmethode zum Zeichnen des Graphen graph, welcher numerisch ermittelt
     * wurde.
     *
     * @throws EvaluationException
     */
    public void drawGraphs2D(double[][] graph) throws EvaluationException {
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
        setGraph(graph);
        computeScreenSizes();
        drawGraphs2D();
    }

    private void drawGraphs2D() {
        draw();
    }

    @Override
    public void draw() {

        GraphicsContext gc = getGraphicsContext2D();

        super.draw();
        drawGraphs2D(gc);

        /*
         Im Folgenden wird der Graph in einem größeren/kleineren Bereich
         gezeichnet, falls der aktuelle Zoomfaktor derart berechnet wurde,
         dass der Graph zu grob oder zu klein ist.
         */
        try {
            Constant varAbscStart = new Constant(this.axeCenterX - 2 * this.maxX);
            Constant varAbscEnd = new Constant(this.axeCenterX + 2 * this.maxX);
            if (this.exprs.size() > 0) {
                expressionToGraph(varAbscStart, varAbscEnd);
            }
        } catch (EvaluationException e) {
        }
        convertGraphToGraphicalGraph();

        drawSpecialPoints(gc, this.specialPoints);
        if (pointsAreShowable) {
            drawMousePointOnGraph();
        }

    }

    @Override
    protected void drawMousePointOnGraph() {

        GraphicsContext gc = getGraphicsContext2D();

        int lowerPixelBoundX = Math.max(0, this.mouseCoordinateX - MOUSE_DISTANCE_FOR_SHOWING_POINT);
        int upperPixelBoundX = Math.min((int) this.getWidth(), this.mouseCoordinateX + MOUSE_DISTANCE_FOR_SHOWING_POINT);

        Integer indexOfGraph = null;
        Integer pixelX = null;
        Integer pixelY = null;

        double functionValue;
        int functionValueAsPixel, distance;
        Integer lowestDistance = null;

        for (int i = lowerPixelBoundX; i <= upperPixelBoundX; i++) {
            for (int j = 0; j < this.exprs.size(); j++) {
                try {
                    Variable.setValue(this.varAbsc, convertToEuclideanCoordinateX(i));
                    functionValue = this.exprs.get(j).evaluate();
                    functionValueAsPixel = convertToPixelY(functionValue);
                    distance = computeDistanceOfPixels(new int[]{this.mouseCoordinateX, this.mouseCoordinateY},
                            new int[]{i, functionValueAsPixel});
                    if (distance <= MOUSE_DISTANCE_FOR_SHOWING_POINT) {
                        if (lowestDistance == null || distance < lowestDistance) {
                            lowestDistance = distance;
                            indexOfGraph = j;
                            pixelX = i;
                            pixelY = functionValueAsPixel;
                        }
                    }
                } catch (EvaluationException e) {
                    // Nichts tun.
                }
            }
        }

        if (indexOfGraph != null) {
            drawCirclePoint(gc, pixelX, pixelY, true);
        }

    }

}
