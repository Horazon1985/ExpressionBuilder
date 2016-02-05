package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import lang.translator.Translator;

public class GraphicPanelPolar2D extends JPanel implements Exportable {

    //Parameter für 2D-Graphen
    private String var;

    /*
     Es können sich mehrere Graphen jn graph2D befinden. Auf dje einzelnen
     Graphen kann dann jeweils über dje Keys 0, 1, 2, ..., this.graph.size() -
     1 zugegriffen werden.
     */
    private final ArrayList<Expression> exprs = new ArrayList<>();
    private final ArrayList<double[][]> polarGraph2D = new ArrayList<>();
    private final ArrayList<Color> colors = new ArrayList<>();

    final static Color[] fixedColors = {Color.blue, Color.green, Color.orange, Color.red, Color.PINK};

    private double zoomfactor, zoomfactorX, zoomfactorY;
    private double axeCenterX, axeCenterY;
    private double maxX, maxY;
    private int expX, expY;

    private boolean isInitialized;
    private boolean movable = false;

    private Point lastMousePosition;

    public GraphicPanelPolar2D() {

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
                if (movable) {
                    axeCenterX += (lastMousePosition.x - e.getPoint().x) * maxX / 250;
                    axeCenterY += (-lastMousePosition.y + e.getPoint().y) * maxY / 250;
                    lastMousePosition = e.getPoint();
                    repaint();
                } else {
                    if ((lastMousePosition.x - e.getPoint().x >= 0 && zoomfactorX < 10)
                            || (lastMousePosition.x - e.getPoint().x <= 0 && zoomfactorX > 0.1)) {
                        maxX = maxX * Math.pow(1.02, lastMousePosition.x - e.getPoint().x);
                        zoomfactorX = zoomfactorX * Math.pow(1.02, lastMousePosition.x - e.getPoint().x);
                    }
                    if ((lastMousePosition.y - e.getPoint().y >= 0 && zoomfactorY < 10)
                            || (lastMousePosition.y - e.getPoint().y <= 0 && zoomfactorY > 0.1)) {
                        maxY = maxY * Math.pow(1.02, lastMousePosition.y - e.getPoint().y);
                        zoomfactorY = zoomfactorY * Math.pow(1.02, lastMousePosition.y - e.getPoint().y);
                    }
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
                /**
                 * Der Zoomfaktor darf höchstens 10 sein (und mindestens 0.1)
                 */
                if (((e.getWheelRotation() >= 0) && (zoomfactor < 10))
                        || ((e.getWheelRotation() <= 0) && (zoomfactor > 0.1))) {
                    maxX *= Math.pow(1.1, e.getWheelRotation());
                    maxY *= Math.pow(1.1, e.getWheelRotation());
                    zoomfactor *= Math.pow(1.1, e.getWheelRotation());
                    computeExpXExpY();
                    repaint();
                }

                repaint();
            }
        });
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
        return this.exprs;
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    public void setIsInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void addExpression(Expression expr) {
        this.exprs.add(expr);
        setColors();
    }

    public void setColors() {
        int numberOfColors = Math.max(this.exprs.size(), this.polarGraph2D.size());
        for (int i = this.colors.size(); i < numberOfColors; i++) {
            if (i < GraphicPanelPolar2D.fixedColors.length) {
                this.colors.add(GraphicPanelPolar2D.fixedColors[i]);
            } else {
                this.colors.add(new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random())));
            }
        }
    }

    public void clearExpressionAndGraph() {
        this.exprs.clear();
        this.polarGraph2D.clear();
        this.colors.clear();
    }

    /**
     * Voraussetzung: expr und var sind bereits gesetzt.
     */
    public void computeScreenSizes(Expression exprPhi_0, Expression exprPhi_1) throws EvaluationException {

        double phi_0 = exprPhi_0.evaluate();
        double phi_1 = exprPhi_1.evaluate();

        if (phi_0 >= phi_1) {
            throw new EvaluationException(Translator.translateExceptionMessage("MCC_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOTPOLAR_1")
                    + (exprs.size() + 2)
                    + Translator.translateExceptionMessage("MCC_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOTPOLAR_2")
                    + (exprs.size() + 1)
                    + Translator.translateExceptionMessage("MCC_LIMITS_MUST_BE_WELL_ORDERED_IN_PLOTPOLAR_3"));
        }

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
     * Voraussetzung: graph ist bereits initialisiert (bzw. mit Funktionswerten
     * gefüllt). max_x, max_y sind bekannt/initialisiert.
     */
    private void computeExpXExpY() {

        /**
         * Markierungen an den Achsen anbringen; exp_x = max. Exponent einer
         * 10er-Potenz, die kleiner als der größte x-Wert ist exp_y = min.
         * Exponent einer 10er-Potenz, die kleiner als der größte y-Wert ist
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
    public void expressionToGraph(String var, double phiStart, double phiEnd) throws EvaluationException {

        this.polarGraph2D.clear();
        double[][] pointsOnGraphs;

        for (int i = 0; i < this.exprs.size(); i++) {

            pointsOnGraphs = new double[1001][2];

            // Falls this.expr.get(i) konstant ist -> den Funktionswert nur einmal berechnen!
            Variable.setValue(var, phiStart);
            for (int j = 0; j <= 1000; j++) {
                Variable.setValue(var, phiStart + (phiEnd - phiStart) * j / 1000);
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

    /**
     * Berechnet aus Punktkoordinaten (x, y) Koordjnaten (x', y') für die
     * graphische Darstellung Voraussetzung: max_x, max_y sind bekannt (und
     * nicht 0).
     */
    private int[] convertToPixel(double x, double y) {

        int[] pixel = new int[2];
        pixel[0] = 250 + (int) Math.round(250 * (x - axeCenterX) / maxX);
        pixel[1] = 250 - (int) Math.round(250 * (y - axeCenterY) / maxY);
        return pixel;

    }

    //Berechnet den Achseneintrag m*10^(-k) ohne den Double-typischen Nachkommastellenfehler.
    private BigDecimal roundAxisEntries(int m, int k) {
        if (k >= 0) {
            return new BigDecimal(m).multiply(BigDecimal.TEN.pow(k));
        }
        return new BigDecimal(m).divide(BigDecimal.TEN.pow(-k));
    }

    private void drawAxesAndLines(Graphics g) {
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
        g.drawString("x", 500 - 5 - g.getFontMetrics().stringWidth("x"), 250 + (int) (250 * axeCenterY / maxY) + 15);
        g.drawString("y", 250 - (int) (250 * axeCenterX / maxX) - 5 - g.getFontMetrics().stringWidth("y"), 20);
    }

    private void drawPolarGraph2D(Graphics g) {

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
        drawAxesAndLines(g);

        //Graphen zeichnen
        if (this.polarGraph2D.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.polarGraph2D.size(); i++) {

            g.setColor(this.colors.get(i));

            if (polarGraph2D.get(i).length > 1) {

                HashMap<Integer, int[][]> graphicalGraph = convertGraphToGraphicalGraph();
                for (int j = 0; j < graphicalGraph.get(i).length - 1; j++) {
                    if (!Double.isNaN(polarGraph2D.get(i)[j][1]) && !Double.isInfinite(polarGraph2D.get(i)[j][1])
                            && !Double.isNaN(polarGraph2D.get(i)[j + 1][1]) && !Double.isInfinite(polarGraph2D.get(i)[j + 1][1])) {

                        if ((axeCenterY + maxY < polarGraph2D.get(i)[j][1]) && (axeCenterY - maxY > polarGraph2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 0, graphicalGraph.get(i)[j + 1][0], 500);
                        } else if ((axeCenterY - maxY > polarGraph2D.get(i)[j][1]) && (axeCenterY + maxY < polarGraph2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], 500, graphicalGraph.get(i)[j + 1][0], 0);
                        } else if ((axeCenterY + 2 * maxY >= polarGraph2D.get(i)[j][1]) && (axeCenterY - 2 * maxY <= polarGraph2D.get(i)[j][1])
                                && (axeCenterY + 2 * maxY >= polarGraph2D.get(i)[j + 1][1]) && (axeCenterY - 2 * maxY <= polarGraph2D.get(i)[j + 1][1])) {
                            g.drawLine(graphicalGraph.get(i)[j][0], graphicalGraph.get(i)[j][1], graphicalGraph.get(i)[j + 1][0], graphicalGraph.get(i)[j + 1][1]);
                        }

                    }
                }

            }

        }

        g.setColor(Color.black);

    }

    public void drawPolarGraph2D() {
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
        drawPolarGraph2D(g);

    }

    // Grafikexport.
    @Override
    public void export(String filePath) throws IOException {
        BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        paintComponent(g);
        ImageIO.write(bi, "PNG", new File(filePath));
    }

}
