package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import abstractexpressions.matrixexpression.classes.Matrix;

public class GraphicPanel2D extends AbstractGraphicPanel2D {

    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varAbsc, varOrd;

    /*
     Es können sich mehrere Graphen jn graph2D befinden. Auf dje einzelnen
     Graphen kann dann jeweils über die Keys 0, 1, 2, ..., this.graph.size() -
     1 zugegriffen werden.
     */
    private ArrayList<Expression> exprs = new ArrayList<>();
    private final ArrayList<double[][]> graphs2D = new ArrayList<>();
    private final ArrayList<Color> colors = new ArrayList<>();

    public GraphicPanel2D() {

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastMousePosition = e.getPoint();
                    movable = true;
                } else {
                    lastMousePosition = e.getPoint();
                    movable = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (movable) {
                    axeCenterX += (lastMousePosition.x - e.getPoint().x) * maxX / 250;
                    axeCenterY += (-lastMousePosition.y + e.getPoint().y) * maxY / 250;
                    lastMousePosition = e.getPoint();
                    repaint();
                } else {
                    maxX = maxX * Math.pow(1.02, lastMousePosition.x - e.getPoint().x);
                    maxY = maxY * Math.pow(1.02, lastMousePosition.y - e.getPoint().y);
                    lastMousePosition = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                maxX *= Math.pow(1.1, e.getWheelRotation());
                maxY *= Math.pow(1.1, e.getWheelRotation());

                /*
                     Vorbeugende Maßnahme: Falls der Wert
                     axe_center_x*Math.pow(10,-exp_x) oder
                     axe_center_y*Math.pow(10,-exp_y) zu groß ist, so ist der
                     maximale Zoom-Grad erreicht! -> Keine weitere
                     Vergrößerung mehr.
                 */
                if (Math.abs(axeCenterX * Math.pow(10, -expX)) >= 500000000 || Math.abs(axeCenterY * Math.pow(10, -expY)) >= 500000000) {
                    maxX /= Math.pow(1.1, e.getWheelRotation());
                    maxY /= Math.pow(1.1, e.getWheelRotation());
                    computeExpXExpY();
                }

                repaint();
            }
        });
    }

    public ArrayList<double[][]> getGraphs() {
        return this.graphs2D;
    }

    public ArrayList<Color> getColors() {
        return this.colors;
    }

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public void setVarAbsc(String varAbsc) {
        this.varAbsc = varAbsc;
    }

    public void setVars(String varAbsc, String varOrd) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
    }

    public void setExpressions(ArrayList<Expression> exprs) {
        this.exprs = exprs;
        this.graphs2D.clear();
        this.colors.clear();
        setColors();
    }

    public void setExpressions(Expression... exprs) {
        this.exprs = new ArrayList<>();
        this.exprs.addAll(Arrays.asList(exprs));
        this.graphs2D.clear();
        this.colors.clear();
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
        this.colors.clear();
        for (int i = 0; i < numberOfColors; i++) {
            if (i < fixedColors.length) {
                this.colors.add(fixedColors[i]);
            } else {
                this.colors.add(generateColor());
            }
        }
    }

    /**
     * Erzeugt eine neue Zufallsfarbe.
     */
    private Color generateColor() {
        return new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
    }

    /**
     * Voraussetzung: graph2D ist bereits initialisiert (bzw. mit
     * Funktionswerten gefüllt). Voraussetzung: alle Graphen werden über
     * demselben x-Bereich gezeichnet.
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
     * Voraussetzung: expr und var sind bereits gesetzt.
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
        for (Expression expr : exprs) {
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
     * Voraussetzung: expr und var sind bereits gesetzt.
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
     * Berechnet die Gitterpunkte für die Graphen aus den Ausdrücken in expr.
     * Voraussetzung: expr wurde mittels setExpression gesetzt.
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
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen. Voraussetzung:
     * maxX, maxY sind bekannt!
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
     * Zeichnet die Achsen und die grauen Niveaulinien. Die erste Koordinate
     * heißt varAbsc, die zweite f(varAbsc); f = zu zeichnende Funktion(en).
     */
    private void drawAxesAndLines(Graphics g, String varAbsc) {
        g.setColor(Color.lightGray);

        int linePosition;
        int k = (int) (axeCenterX * Math.pow(10, -expX));

        //x-Niveaulinien zeichnen
        linePosition = convertToPixel(k * Math.pow(10, expX), 0)[0];

        while (linePosition <= 500) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, expX), 0)[0];
                g.drawLine(linePosition, 0, linePosition, 500);

                if ((250 * axeCenterY / maxY - 3 <= 248) && (250 * axeCenterY / maxY - 3 >= -230)) {
                    g.drawString(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 250 + (int) (250 * axeCenterY / maxY) - 3);
                } else if (250 * axeCenterY / maxY - 3 > 248) {
                    g.drawString(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 495);
                } else {
                    g.drawString(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 20);
                }

            }

            k++;

        }

        k = (int) (axeCenterX * Math.pow(10, -expX)) - 1;
        linePosition = convertToPixel(k * Math.pow(10, expX), 0)[0];

        while (linePosition >= 0) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, expX), 0)[0];
                g.drawLine(linePosition, 0, linePosition, 500);

                if ((250 * axeCenterY / maxY - 3 <= 248) && (250 * axeCenterY / maxY - 3 >= -230)) {
                    g.drawString(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 250 + (int) (250 * axeCenterY / maxY) - 3);
                } else if (250 * axeCenterY / maxY - 3 > 248) {
                    g.drawString(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 495);
                } else {
                    g.drawString(String.valueOf(roundAxisEntries(k, expX)), linePosition + 3, 20);
                }

            }

            k--;

        }

        //y-Niveaulinien zeichnen
        k = (int) (axeCenterY * Math.pow(10, -expY));
        linePosition = convertToPixel(0, k * Math.pow(10, expY))[1];

        while (linePosition >= 0) {
            linePosition = convertToPixel(0, k * Math.pow(10, expY))[1];
            g.drawLine(0, linePosition, 500, linePosition);

            if ((250 * axeCenterX / maxX - 3 >= -225) && (250 * axeCenterX / maxX - 3 <= 245)) {
                g.drawString(String.valueOf(roundAxisEntries(k, expY)), 250 - (int) (250 * axeCenterX / maxX) + 3, linePosition - 3);
            } else if (250 * axeCenterX / maxX - 3 >= -225) {
                g.drawString(String.valueOf(roundAxisEntries(k, expY)), 5, linePosition - 3);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(k, expY)), 475, linePosition - 3);
            }

            k++;

        }

        k = (int) (axeCenterY * Math.pow(10, -expY)) - 1;
        linePosition = convertToPixel(0, k * Math.pow(10, expY))[1];

        while (linePosition <= 500) {
            linePosition = convertToPixel(0, k * Math.pow(10, expY))[1];
            g.drawLine(0, linePosition, 500, linePosition);

            if ((250 * axeCenterX / maxX - 3 >= -225) && (250 * axeCenterX / maxX - 3 <= 245)) {
                g.drawString(String.valueOf(roundAxisEntries(k, expY)), 250 - (int) (250 * axeCenterX / maxX) + 3, linePosition - 3);
            } else if (250 * axeCenterX / maxX - 3 >= -225) {
                g.drawString(String.valueOf(roundAxisEntries(k, expY)), 5, linePosition - 3);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(k, expY)), 475, linePosition - 3);
            }

            k--;

        }

        //Achsen inkl. Achsenbezeichnungen eintragen
        //Achsen
        g.setColor(Color.black);
        g.drawLine(0, 250 + (int) (250 * axeCenterY / maxY), 500, 250 + (int) (250 * axeCenterY / maxY));
        g.drawLine(250 - (int) (250 * axeCenterX / maxX), 0, 250 - (int) (250 * axeCenterX / maxX), 500);
        //Achsenpfeile
        g.drawLine(500, 250 + (int) (250 * axeCenterY / maxY), 494, 250 + (int) (250 * axeCenterY / maxY) - 3);
        g.drawLine(500, 250 + (int) (250 * axeCenterY / maxY), 494, 250 + (int) (250 * axeCenterY / maxY) + 3);
        g.drawLine(250 - (int) (250 * axeCenterX / maxX), 0, 250 - (int) (250 * axeCenterX / maxX) + 3, 6);
        g.drawLine(250 - (int) (250 * axeCenterX / maxX), 0, 250 - (int) (250 * axeCenterX / maxX) - 3, 6);
        /**
         * Achsenbeschriftung WICHTIG: In der Prozedur drawString werden die
         * Achsenbeschriftung derart eingetragen, dass (1) Die Beschriftung der
         * Variablen var innerhalb des Bildschirms liegt (5 px) (2) Die
         * Beschriftung f(var) links von der vertikalen Achse liegt (5 px)
         * Hierzu müssen die Pixellängen der gezeichneten Strings ausgerechnet
         * werden (mittels g.getFontMetrics().stringWidth()).
         */
        g.drawString(varAbsc, 500 - 5 - g.getFontMetrics().stringWidth(varAbsc), 250 + (int) (250 * axeCenterY / maxY) + 15);
        g.drawString("f(" + varAbsc + ")", 250 - (int) (250 * axeCenterX / maxX) - 5 - g.getFontMetrics().stringWidth("f(" + varAbsc + ")"), 20);
    }

    private void drawGraphs2D(Graphics g) {

        // Weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        // Markierungen an den Achsen berechnen.
        computeExpXExpY();

        //Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g, this.varAbsc);

        //Graphen zeichnen
        if (this.graphs2D.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.graphs2D.size(); i++) {

            g.setColor(this.colors.get(i));

            if (this.graphs2D.get(i).length > 1) {

                HashMap<Integer, int[][]> graphicalGraph = convertGraphToGraphicalGraph();
                for (int j = 0; j < graphicalGraph.get(i).length - 1; j++) {
                    if (!Double.isNaN(graphs2D.get(i)[j][1]) && !Double.isInfinite(graphs2D.get(i)[j][1])
                            && !Double.isNaN(graphs2D.get(i)[j + 1][1]) && !Double.isInfinite(graphs2D.get(i)[j + 1][1])) {

                        if ((axeCenterY + maxY < graphs2D.get(i)[j][1]) && (axeCenterY - maxY > graphs2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 0, graphicalGraph.get(i)[j + 1][0], 500);
                        } else if ((axeCenterY - maxY > graphs2D.get(i)[j][1]) && (axeCenterY + maxY < graphs2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 500, graphicalGraph.get(i)[j + 1][0], 0);
                        } else if ((axeCenterY + 2 * maxY >= graphs2D.get(i)[j][1]) && (axeCenterY - 2 * maxY <= graphs2D.get(i)[j][1])
                                && (axeCenterY + 2 * maxY >= graphs2D.get(i)[j + 1][1]) && (axeCenterY - 2 * maxY <= graphs2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], graphicalGraph.get(i)[j][1], graphicalGraph.get(i)[j + 1][0], graphicalGraph.get(i)[j + 1][1]);
                        }

                    }
                }

            }

        }

        g.setColor(Color.black);

    }

    public void drawGraphs2D(Expression x_0, Expression x_1, ArrayList<Expression> exprs) throws EvaluationException {
        setExpressions(exprs);
        computeScreenSizes(x_0, x_1);
        expressionToGraph(x_0, x_1);
        drawGraphs2D();
    }

    public void drawGraphs2D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, ArrayList<Expression> exprs) throws EvaluationException {
        setExpressions(exprs);
        computeScreenSizes(x_0, x_1, y_0, y_1);
        expressionToGraph(x_0, x_1);
        drawGraphs2D();
    }

    public void drawGraphs2D(double[][] graph) throws EvaluationException {
        setGraph(graph);
        computeScreenSizes();
        drawGraphs2D();
    }

    public void drawGraphs2D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        drawGraphs2D(g);

        /**
         * Im Folgenden wird der Graph in einem größeren/kleineren Bereich
         * gezeichnet, falls der aktuelle Zoomfaktor derart berechnet wurde,
         * dass der Graph zu grob oder zu klein ist.
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

        drawSpecialPoints(g, this.specialPoints);

    }

}
