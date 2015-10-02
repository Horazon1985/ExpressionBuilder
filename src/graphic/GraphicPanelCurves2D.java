package graphic;

import exceptions.EvaluationException;
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
import javax.swing.JPanel;
import translator.Translator;

public class GraphicPanelCurves2D extends JPanel {

    //Parameter für 2D-Graphen
    private String var;
    // expr[0] und expr[1] sind die Komponenten in der Kurvendarstellung.
    private Expression[] expr = new Expression[2];
    private final ArrayList<double[]> curve2D = new ArrayList<>();

    private double zoomfactor, zoomfactorX, zoomfactorY;
    private double axeCenterX, axeCenterY;
    private double maxX, maxY;
    private int expX, expY;

    private boolean isInitialized;
    private boolean movable = false;

    private Point lastMousePosition;

    public GraphicPanelCurves2D() {

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
                // Der Zoomfaktor darf höchstens 10 sein (und mindestens 0.1)
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

    public Expression[] getExpressions() {
        return this.expr;
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateExceptionMessage("GR_Graphic2D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    public void setIsInitialized(boolean is_initialized) {
        this.isInitialized = is_initialized;
        this.zoomfactor = 1;
        this.zoomfactorX = 1;
        this.zoomfactorY = 1;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setExpression(Expression[] expr) {
        this.expr = expr;
        this.curve2D.clear();
    }

    /**
     * Voraussetzung: expr und var sind bereits gesetzt.
     */
    public void computeScreenSizes(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();

        double globalMinX = Double.NaN;
        double globalMaxX = Double.NaN;
        double globalMinY = Double.NaN;
        double globalMaxY = Double.NaN;

        double x, y;
        for (int i = 0; i < 100; i++) {

            Variable.setValue(this.var, t_0 + i * (t_1 - t_0) / 100);
            try {
                x = expr[0].evaluate();
                y = expr[1].evaluate();
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

            // Falls alle expr.get(i) konstant sind.
            if (this.maxX == 0) {
                this.maxX = 1;
            }
            if (this.maxY == 0) {
                this.maxY = 1;
            }

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
    public void expressionToGraph(Expression exprT_0, Expression exprT_1) throws EvaluationException {

        double t_0 = exprT_0.evaluate();
        double t_1 = exprT_1.evaluate();
        
        double t = t_0;
        double h;

        Expression[] tangentVector = new Expression[2];
        tangentVector[0] = this.expr[0].diff(var).simplify();
        tangentVector[1] = this.expr[1].diff(var).simplify();

        // Der Graph der Kurve soll aus maximal 10000 Teilstrecken bestehen.
        for (int i = 0; i < 10000; i++) {

            double[] pointOnCurve = new double[2];
            Variable.setValue(var, t);
            try {
                /*
                 h wird nun passend ermittelt, so dass die Distanz zwischen
                 zwei Punkten auf der Kurve nur wenige Pixel beträgt.
                 */
                if (t_1 >= t_0) {
                    h = (this.maxX / 250 + this.maxY / 250) / (Math.abs(tangentVector[0].evaluate()) + Math.abs(tangentVector[1].evaluate()));
                } else {
                    h = -(this.maxX / 250 + this.maxY / 250) / (Math.abs(tangentVector[0].evaluate()) + Math.abs(tangentVector[1].evaluate()));
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
            } catch (EvaluationException e) {
                pointOnCurve[0] = Double.NaN;
                pointOnCurve[1] = Double.NaN;
            }

            this.curve2D.add(pointOnCurve);

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

        int[][] result = new int[this.curve2D.size()][2];
        for (int i = 0; i < this.curve2D.size(); i++) {
            result[i] = convertToPixel(this.curve2D.get(i)[0], this.curve2D.get(i)[1]);
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
        pixel[0] = 250 + (int) Math.round(250 * (x - this.axeCenterX) / this.maxX);
        pixel[1] = 250 - (int) Math.round(250 * (y - this.axeCenterY) / this.maxY);
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
        /*
         Achsenbeschriftung WICHTIG: In der Prozedur drawString werden die
         Achsenbeschriftung derart eingetragen, dass (1) Die Beschriftung der
         Variablen var innerhalb des Bildschirms liegt (5 px) (2) Die
         Beschriftung f(var) links von der vertikalen Achse liegt (5 px)
         Hierzu müssen die Pixellängen der gezeichneten Strings ausgerechnet
         werden (mittels g.getFontMetrics().stringWidth()).
         */
        g.drawString("x", 500 - 5 - g.getFontMetrics().stringWidth("x"), 250 + (int) (250 * axeCenterY / maxY) + 15);
        g.drawString("y", 250 - (int) (250 * axeCenterX / maxX) - 5 - g.getFontMetrics().stringWidth("y"), 20);
    }

    private void drawCurve2D(Graphics g, String var) {

        // Weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        // Markierungen an den Achsen berechnen.
        computeExpXExpY();

        //Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g);

        //Graphen zeichnen
        if (this.curve2D.size() <= 1) {
            return;
        }

        g.setColor(Color.blue);

        int[][] graphicalCurve = convertCurveToGraphicalCurve(g);
        for (int i = 0; i < graphicalCurve.length - 1; i++) {
            if (!Double.isNaN(this.curve2D.get(i)[0]) && !Double.isInfinite(this.curve2D.get(i)[0])
                    && !Double.isNaN(this.curve2D.get(i + 1)[0]) && !Double.isInfinite(this.curve2D.get(i + 1)[0])
                    && !Double.isNaN(this.curve2D.get(i)[1]) && !Double.isInfinite(this.curve2D.get(i)[1])
                    && !Double.isNaN(this.curve2D.get(i + 1)[1]) && !Double.isInfinite(this.curve2D.get(i + 1)[1])) {

                g.drawLine(graphicalCurve[i][0], graphicalCurve[i][1], graphicalCurve[i + 1][0], graphicalCurve[i + 1][1]);

            }
        }

        g.setColor(Color.black);

    }

    public void drawCurve2D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {

        // Falls repaint() aufgerufen wird, bevor irgendwelche Attribute gesetzt wurden -> Abbruch!
        if (!this.isInitialized) {
            return;
        }
        super.paintComponent(g);
        drawCurve2D(g, this.var);

    }

}
