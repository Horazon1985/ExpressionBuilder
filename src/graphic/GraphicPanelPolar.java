package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphicPanelPolar extends AbstractGraphicPanel2D {

    // Parameter für 2D-Graphen
    private String var;

    /*
     Es können sich mehrere Graphen jn graph2D befinden. Auf dje einzelnen
     Graphen kann dann jeweils über dje Keys 0, 1, 2, ..., this.graph.size() -
     1 zugegriffen werden.
     */
    private final ArrayList<Expression> exprs = new ArrayList<>();
    private final ArrayList<double[][]> polarGraph2D = new ArrayList<>();
    private final ArrayList<Color> colors = new ArrayList<>();

    public GraphicPanelPolar() {
        super(10,0.1);
    }

    public ArrayList<Color> getColors() {
        return this.colors;
    }

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setExpressions(ArrayList<Expression> exprs) {
        this.exprs.clear();
        this.exprs.addAll(exprs);
        setColors();
    }

    public void setColors() {
        int numberOfColors = Math.max(this.exprs.size(), this.polarGraph2D.size());
        for (int i = this.colors.size(); i < numberOfColors; i++) {
            if (i < AbstractGraphicPanel2D.fixedColors.length) {
                this.colors.add(GraphicPanelPolar.fixedColors[i]);
            } else {
                this.colors.add(generateColor());
            }
        }
    }

    /**
     * Voraussetzung: expr und var sind bereits gesetzt.
     * 
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprPhi_0, Expression exprPhi_1) throws EvaluationException {

        double phi_0 = exprPhi_0.evaluate();
        double phi_1 = exprPhi_1.evaluate();

        double globalMinX = Double.NaN;
        double globalMaxX = Double.NaN;
        double globalMinY = Double.NaN;
        double globalMaxY = Double.NaN;

        double x, y;
        for (int i = 0; i < exprs.size(); i++) {
            for (int j = 0; j < 100; j++) {

                Variable.setValue(this.var, phi_0 + j * (phi_1 - phi_0) / 100);
                try {
                    x = exprs.get(i).evaluate() * Math.cos(phi_0 + j * (phi_1 - phi_0) / 100);
                    y = exprs.get(i).evaluate() * Math.sin(phi_0 + j * (phi_1 - phi_0) / 100);
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
            // 30 % Rand lassen!
            this.maxX = this.maxX * 1.3;
            this.maxY = this.maxY * 1.3;
        }

    }

    /**
     * Berechnet die Gitterpunkte für die Graphen aus den Ausdrücken in expr.
     * Voraussetzung: expr wurde mittels setExpression gesetzt.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(double phiStart, double phiEnd) throws EvaluationException {

        this.polarGraph2D.clear();
        double[][] pointsOnGraphs;

        for (int i = 0; i < this.exprs.size(); i++) {

            pointsOnGraphs = new double[1001][2];

            // Falls this.expr.get(i) konstant ist -> den Funktionswert nur einmal berechnen!
            Variable.setValue(this.var, phiStart);
            for (int j = 0; j <= 1000; j++) {
                Variable.setValue(this.var, phiStart + (phiEnd - phiStart) * j / 1000);
                try {
                    pointsOnGraphs[j][0] = this.exprs.get(i).evaluate() * Math.cos(phiStart + (phiEnd - phiStart) * j / 1000);
                    pointsOnGraphs[j][1] = this.exprs.get(i).evaluate() * Math.sin(phiStart + (phiEnd - phiStart) * j / 1000);
                } catch (EvaluationException e) {
                    pointsOnGraphs[j][0] = Double.NaN;
                    pointsOnGraphs[j][1] = Double.NaN;
                }
            }

            this.polarGraph2D.add(pointsOnGraphs);

        }

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen. Voraussetzung:
     * max_x, max_y sind bekannt!
     */
    private HashMap<Integer, int[][]> convertGraphToGraphicalGraph() {

        HashMap<Integer, int[][]> result = new HashMap<>();

        for (int i = 0; i < this.polarGraph2D.size(); i++) {

            int[][] graphicalGraph = new int[this.polarGraph2D.get(i).length][2];
            for (int j = 0; j < this.polarGraph2D.get(i).length; j++) {
                graphicalGraph[j][0] = (int) Math.round(250 + 250 * (this.polarGraph2D.get(i)[j][0] - this.axeCenterX) / this.maxX);
                graphicalGraph[j][1] = (int) Math.round(250 - 250 * (this.polarGraph2D.get(i)[j][1] - this.axeCenterY) / this.maxY);
            }
            result.put(i, graphicalGraph);

        }

        return result;

    }

    public void drawGraphPolar(Expression phi_0, Expression phi_1, ArrayList<Expression> exprs) throws EvaluationException {
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
        setExpressions(exprs);
        computeScreenSizes(phi_0, phi_1);
        expressionToGraph(phi_0.evaluate(), phi_1.evaluate());
        drawGraphPolar();
    }

    private void drawGraphPolar(Graphics g) {

        if (this.polarGraph2D.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.polarGraph2D.size(); i++) {

            g.setColor(this.colors.get(i));

            if (this.polarGraph2D.get(i).length > 1) {

                HashMap<Integer, int[][]> graphicalGraph = convertGraphToGraphicalGraph();
                for (int j = 0; j < graphicalGraph.get(i).length - 1; j++) {
                    if (!Double.isNaN(this.polarGraph2D.get(i)[j][1]) && !Double.isInfinite(this.polarGraph2D.get(i)[j][1])
                            && !Double.isNaN(this.polarGraph2D.get(i)[j + 1][1]) && !Double.isInfinite(this.polarGraph2D.get(i)[j + 1][1])) {

                        if (this.axeCenterY + this.maxY < this.polarGraph2D.get(i)[j][1] && this.axeCenterY - this.maxY > this.polarGraph2D.get(i)[j + 1][1]) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 0, graphicalGraph.get(i)[j + 1][0], 500);
                        } else if (this.axeCenterY - this.maxY > this.polarGraph2D.get(i)[j][1] && this.axeCenterY + this.maxY < this.polarGraph2D.get(i)[j + 1][1]) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 500, graphicalGraph.get(i)[j + 1][0], 0);
                        } else if (this.axeCenterY + 2 * this.maxY >= this.polarGraph2D.get(i)[j][1] && this.axeCenterY - 2 * this.maxY <= this.polarGraph2D.get(i)[j][1]
                                && this.axeCenterY + 2 * this.maxY >= this.polarGraph2D.get(i)[j + 1][1] && this.axeCenterY - 2 * this.maxY <= this.polarGraph2D.get(i)[j + 1][1]) {
                            g.drawLine(graphicalGraph.get(i)[j][0], graphicalGraph.get(i)[j][1], graphicalGraph.get(i)[j + 1][0], graphicalGraph.get(i)[j + 1][1]);
                        }

                    }
                }

            }

        }

        g.setColor(Color.black);

    }

    private void drawGraphPolar() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGraphPolar(g);
    }

}
