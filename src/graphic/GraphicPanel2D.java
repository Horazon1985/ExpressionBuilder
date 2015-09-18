package graphic;

import expressionbuilder.Constant;
import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import translator.Translator;

public class GraphicPanel2D extends JPanel {

    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varAbsc, varOrd;

    /*
     Es können sich mehrere Graphen jn graph2D befinden. Auf dje einzelnen
     Graphen kann dann jeweils über die Keys 0, 1, 2, ..., this.graph.size() -
     1 zugegriffen werden.
     */
    private final ArrayList<Expression> expr = new ArrayList<>();
    private final ArrayList<double[][]> graph2D = new ArrayList<>();
    private ArrayList<double[]> implicitGraph2D = new ArrayList<>();
    private final ArrayList<Color> colors = new ArrayList<>();

    final static Color[] fixedColors = {Color.blue, Color.green, Color.orange, Color.red, Color.PINK};

    private double axeCenterX, axeCenterY;
    private double maxX, maxY;
    private int expX, expY;

    private boolean isInitialized;
    private boolean isExplicit;
    private boolean isFixed;
    private boolean specialPointsOccur;
    private boolean movable = false;

    private double[][] specialPoints;

    private Point lastMousePosition;

    public GraphicPanel2D() {

        this.isInitialized = false;

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
                if (isExplicit) {
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
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (isExplicit) {
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
            }
        });
    }

    public boolean getIsExplicit() {
        return this.isExplicit;
    }

    public ArrayList<double[][]> getGraph() {
        return this.graph2D;
    }

    public ArrayList<double[]> getImplicitGraph() {
        return this.implicitGraph2D;
    }

    public double getAxeCenterX() {
        return this.axeCenterX;
    }

    public double getAxeCenterY() {
        return this.axeCenterY;
    }

    public ArrayList<Color> getColors() {
        return this.colors;
    }

    public ArrayList<Expression> getExpressions() {
        return this.expr;
    }

    public ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        if (this.isExplicit) {
            // Expliziter Fall
            instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
            instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
            instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_MOVE_MOUSE_WHEEL"));
            return instructions;
        }
        // Impliziter Fall
        instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_IMPOSSIBLE_MOVE_GRAPH"));
        return instructions;
    }

    public void setIsInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public void setVarAbsc(String varAbsc) {
        this.varAbsc = varAbsc;
    }

    public void setVars(String varAbsc, String varOrd) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
    }

    public void setIsExplicit(boolean isExplicit) {
        this.isExplicit = isExplicit;
    }

    public void setIsFixed(boolean isFixed) {
        this.isFixed = isFixed;
    }

    public void setSpecialPointsOccur(boolean specialPointsOccur) {
        this.specialPointsOccur = specialPointsOccur;
    }

    public void setSpecialPoints(double[][] specialPoints) {
        this.specialPoints = specialPoints;
    }

    public void addExpression(Expression expr) {
        this.expr.add(this.expr.size(), expr);
        setColors();
    }

    public void addGraph(double[][] graph) {
        this.graph2D.add(this.graph2D.size(), graph);
        setColors();
    }

    public void setColors() {
        int numberOfColors = Math.max(this.expr.size(), this.graph2D.size());
        for (int i = this.colors.size(); i < numberOfColors; i++) {
            if (i < GraphicPanel2D.fixedColors.length) {
                this.colors.add(GraphicPanel2D.fixedColors[i]);
            } else {
                this.colors.add(new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random())));
            }
        }
    }

    public void clearExpressionAndGraph() {
        this.expr.clear();
        this.graph2D.clear();
        this.colors.clear();
    }

    /**
     * Voraussetzung: graph2D ist bereits initialisiert (bzw. mit
     * Funktionswerten gefüllt). Voraussetzung: alle Graphen werden über
     * demselben x-Bereich gezeichnet.
     *
     * @throws EvaluationException
     */
    public void computeScreenSizes() throws EvaluationException {

        this.axeCenterX = 0;
        this.axeCenterY = 0;

        double globalMinY = Double.NaN;
        double globalMaxY = Double.NaN;

        if (!this.graph2D.isEmpty()) {
            this.axeCenterX = (this.graph2D.get(0)[this.graph2D.get(0).length - 1][0] + this.graph2D.get(0)[0][0]) / 2;
            this.maxX = (this.graph2D.get(0)[this.graph2D.get(0).length - 1][0] - this.graph2D.get(0)[0][0]) / 2;
        }

        for (int i = 0; i < this.graph2D.size(); i++) {

            if (this.graph2D.get(i).length > 0) {
                for (double[] graph : this.graph2D.get(i)) {
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
    public void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();

        if (varAbscStart >= varAbscEnd) {
            throw new EvaluationException(Translator.translateExceptionMessage("MCC_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOT2D_1")
                    + (expr.size() + 1)
                    + Translator.translateExceptionMessage("MCC_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOT2D_2")
                    + (expr.size() + 2)
                    + Translator.translateExceptionMessage("MCC_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOT2D_3"));
        }

        this.axeCenterX = (varAbscStart + varAbscEnd) / 2;
        this.maxX = varAbscEnd - this.axeCenterX;
        double globalMinY = Double.NaN;
        double globalMaxY = Double.NaN;

        double y;
        for (int i = 0; i < expr.size(); i++) {
            for (int j = 0; j < 100; j++) {
                Variable.setValue(this.varAbsc, varAbscStart + j * (varAbscEnd - varAbscStart) / 100);
                try {
                    y = expr.get(i).evaluate();
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

            // Falls alle expr.get(i) konstant sind.
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

        if (varAbscStart >= varAbscEnd) {
            throw new EvaluationException(Translator.translateExceptionMessage("MCC_FIRST_LIMITS_MUST_BE_WELL_ORDERED_IN_IMPLICIT_PLOT2D"));
        }
        if (varOrdStart >= varOrdEnd) {
            throw new EvaluationException(Translator.translateExceptionMessage("MCC_SECOND_LIMITS_MUST_BE_WELL_ORDERED_IN_IMPLICIT_PLOT2D"));
        }

        this.axeCenterX = (varAbscStart + varAbscEnd) / 2;
        this.axeCenterY = (varOrdStart + varOrdEnd) / 2;
        this.maxX = varAbscEnd - this.axeCenterX;
        this.maxY = varOrdEnd - this.axeCenterY;

    }

    public void setImplicitGraph(ArrayList<double[]> implicitGraph2D) {
        this.implicitGraph2D = implicitGraph2D;
    }

    /**
     * Voraussetzung: graph ist bereits initialisiert (bzw. mit Funktionswerten
     * gefüllt). max_x, max_y sind bekannt/initialisiert.
     */
    private void computeExpXExpY() {

        /*
         Markierungen an den Achsen anbringen; exp_x = max. Exponent einer
         10er-Potenz, die kleiner als der größte x-Wert ist exp_y = min.
         Exponent einer 10er-Potenz, die kleiner als der größte y-Wert ist
         */
        this.expX = 0;
        this.expY = 0;

        if (this.maxX >= 1) {
            while (this.maxX / Math.pow(10, this.expX) >= 1) {
                this.expX++;
            }
            this.expX--;
        } else {
            while (this.maxX / Math.pow(10, this.expX) < 1) {
                this.expX--;
            }
        }

        if (this.maxY >= 1) {
            while (this.maxY / Math.pow(10, this.expY) >= 1) {
                this.expY++;
            }
            this.expY--;
        } else {
            while (this.maxY / Math.pow(10, this.expY) < 1) {
                this.expY--;
            }
        }

    }

    /**
     * Berechnet die Gitterpunkte für die Graphen aus den Ausdrücken in expr.
     * Voraussetzung: expr wurde mittels setExpression gesetzt.
     *
     * @throws EvaluationException
     */
    public void expressionToGraph(String varAbsc, Expression exprAbscStart, Expression exprAbscEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();

        this.graph2D.clear();
        double[][] pointsOnGraphs;

        for (int i = 0; i < this.expr.size(); i++) {

            pointsOnGraphs = new double[1001][2];

            /*
             Falls this.expr.get(i) konstant ist -> den Funktionswert nur
             einmal berechnen!
             */
            if (this.expr.get(i).isConstant()) {
                Variable.setValue(varAbsc, varAbscStart);
                double constOrdValue;
                try {
                    constOrdValue = this.expr.get(i).evaluate();
                } catch (EvaluationException e) {
                    constOrdValue = Double.NaN;
                }
                for (int j = 0; j <= 1000; j++) {
                    pointsOnGraphs[j][0] = varAbscStart + (varAbscEnd - varAbscStart) * j / 1000;
                    pointsOnGraphs[j][1] = constOrdValue;
                }
            } else {
                Variable.setValue(varAbsc, varAbscStart);
                for (int j = 0; j <= 1000; j++) {
                    pointsOnGraphs[j][0] = varAbscStart + (varAbscEnd - varAbscStart) * j / 1000;
                    Variable.setValue(varAbsc, varAbscStart + (varAbscEnd - varAbscStart) * j / 1000);
                    try {
                        pointsOnGraphs[j][1] = this.expr.get(i).evaluate();
                    } catch (EvaluationException e) {
                        pointsOnGraphs[j][1] = Double.NaN;
                    }
                }
            }

            this.graph2D.add(i, pointsOnGraphs);

        }

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen. Voraussetzung:
     * max_x, max_y sind bekannt!
     */
    private HashMap<Integer, int[][]> convertGraphToGraphicalGraph() {

        HashMap<Integer, int[][]> result = new HashMap<>();

        for (int i = 0; i < this.graph2D.size(); i++) {

            int[][] graphicalGraph = new int[this.graph2D.get(i).length][2];
            for (int j = 0; j < this.graph2D.get(i).length; j++) {
                graphicalGraph[j][0] = (int) Math.round(250 + 250 * (this.graph2D.get(i)[j][0] - this.axeCenterX) / this.maxX);
                graphicalGraph[j][1] = (int) Math.round(250 - 250 * (this.graph2D.get(i)[j][1] - this.axeCenterY) / this.maxY);
            }
            result.put(i, graphicalGraph);

        }

        return result;

    }

    /**
     * Berechnet aus Punktkoordinaten (x, y) Koordjnaten (x', y') für die
     * graphische Darstellung Voraussetzung: maxX, maxY sind bekannt (und nicht
     * 0).
     */
    private int[] convertToPixel(double x, double y) {

        int[] pixel = new int[2];
        pixel[0] = 250 + (int) Math.round(250 * (x - axeCenterX) / maxX);
        pixel[1] = 250 - (int) Math.round(250 * (y - axeCenterY) / maxY);
        return pixel;

    }

    /**
     * Berechnet den Achseneintrag m*10^(-k) ohne den Double-typischen
     * Nachkommastellenfehler.
     */
    private BigDecimal roundAxisEntries(int m, int k) {
        if (k >= 0) {
            return new BigDecimal(m).multiply(BigDecimal.TEN.pow(k));
        }
        return new BigDecimal(m).divide(BigDecimal.TEN.pow(-k));
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

    /**
     * Zeichnet die Achsen und die grauen Niveaulinien. Die erste Koordinate
     * heißt varAbsc, die zweite varOrd.
     */
    private void drawAxesAndLines(Graphics g, String varAbsc, String varOrd) {
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
        g.drawString(varOrd, 250 - (int) (250 * axeCenterX / maxX) - 5 - g.getFontMetrics().stringWidth(varOrd), 20);
    }

    /**
     * Zeichnet rote Punkte an wichtigen Stellen (etwa Markierung von
     * Nullstellen etc.)
     */
    public void drawSpecialPoints(Graphics g, double[][] specialPoints) {
        g.setColor(Color.red);
        int[][] specialPointsCoordinates = new int[specialPoints.length][2];
        for (int i = 0; i < specialPoints.length; i++) {
            specialPointsCoordinates[i] = convertToPixel(specialPoints[i][0], specialPoints[i][1]);
            g.fillOval(specialPointsCoordinates[i][0] - 3, specialPointsCoordinates[i][1] - 3, 7, 7);
        }
    }

    private void drawGraph2D(Graphics g, String varAbsc) {

        /**
         * Weißen Hintergrund zeichnen.
         */
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        /**
         * Markierungen an den Achsen berechnen.
         */
        computeExpXExpY();

        //Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g, varAbsc);

        //Graphen zeichnen
        if (this.graph2D.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.graph2D.size(); i++) {

            g.setColor(this.colors.get(i));

            if (graph2D.get(i).length > 1) {

                HashMap<Integer, int[][]> graphicalGraph = convertGraphToGraphicalGraph();
                for (int j = 0; j < graphicalGraph.get(i).length - 1; j++) {
                    if (!Double.isNaN(graph2D.get(i)[j][1]) && !Double.isInfinite(graph2D.get(i)[j][1])
                            && !Double.isNaN(graph2D.get(i)[j + 1][1]) && !Double.isInfinite(graph2D.get(i)[j + 1][1])) {

                        if ((axeCenterY + maxY < graph2D.get(i)[j][1]) && (axeCenterY - maxY > graph2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 0, graphicalGraph.get(i)[j + 1][0], 500);
                        } else if ((axeCenterY - maxY > graph2D.get(i)[j][1]) && (axeCenterY + maxY < graph2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 500, graphicalGraph.get(i)[j + 1][0], 0);
                        } else if ((axeCenterY + 2 * maxY >= graph2D.get(i)[j][1]) && (axeCenterY - 2 * maxY <= graph2D.get(i)[j][1])
                                && (axeCenterY + 2 * maxY >= graph2D.get(i)[j + 1][1]) && (axeCenterY - 2 * maxY <= graph2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], graphicalGraph.get(i)[j][1], graphicalGraph.get(i)[j + 1][0], graphicalGraph.get(i)[j + 1][1]);
                        }

                    }
                }

            }

        }

        g.setColor(Color.black);

    }

    private void drawImplicitGraph2D(Graphics g, String varAbsc, String varOrd) {
        /**
         * Weißen Hintergrund zeichnen.
         */
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        /**
         * Markierungen an den Achsen berechnen.
         */
        computeExpXExpY();

        //Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g, varAbsc, varOrd);

        g.setColor(Color.blue);
        int[] pixelsOfImplicitGraph;
        double[] point;
        for (int i = 0; i < this.implicitGraph2D.size(); i++) {
            point = this.implicitGraph2D.get(i);
            pixelsOfImplicitGraph = convertToPixel(point[0], point[1]);
            g.drawLine(pixelsOfImplicitGraph[0], pixelsOfImplicitGraph[1], pixelsOfImplicitGraph[0], pixelsOfImplicitGraph[1]);
        }

    }

    public void drawGraph2D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {

        /**
         * Falls repaint() aufgerufen wird, bevor irgendwelche Attribute gesetzt
         * wurden -> Abbruch!
         */
        if (!this.isInitialized) {
            return;
        }

        super.paintComponent(g);
        if (this.isExplicit) {
            drawGraph2D(g, this.varAbsc);
        } else {
            drawImplicitGraph2D(g, this.varAbsc, this.varOrd);
        }

        /**
         * Im Folgenden wird der Graph in einem größeren/kleineren Bereich
         * gezeichnet, falls der aktuelle Zoomfaktor derart berechnet wurde,
         * dass der Graph zu grob oder zu klein ist.
         */
        if (this.isExplicit && !this.isFixed && (this.graph2D.size() > 0)) {
            try {
                Constant varAbscStart = new Constant(this.axeCenterX - 2 * this.maxX);
                Constant varAbscEnd = new Constant(this.axeCenterX + 2 * this.maxX);
                expressionToGraph(varAbsc, varAbscStart, varAbscEnd);
            } catch (EvaluationException e) {
            }
            convertGraphToGraphicalGraph();
        }

        if (this.specialPointsOccur) {
            drawSpecialPoints(g, this.specialPoints);
        }

    }

}
