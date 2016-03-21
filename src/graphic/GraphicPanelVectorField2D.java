package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Constant;
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
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import abstractexpressions.matrixexpression.classes.Matrix;
import lang.translator.Translator;

public class GraphicPanelVectorField2D extends JPanel implements Exportable {

    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varAbsc, varOrd;

    /*
     Es können sich mehrere Graphen jn graph2D befinden. Auf dje einzelnen
     Graphen kann dann jeweils über die Keys 0, 1, 2, ..., this.graph.size() -
     1 zugegriffen werden.
     */
    private Matrix vectorFieldExpr;
    private final ArrayList<double[]> vectorField2D = new ArrayList<>();

    final static Color[] fixedColors = {Color.blue};

    private double axeCenterX, axeCenterY;
    private double maxX, maxY;
    private int expX, expY;

    private boolean movable = false;

    private Point lastMousePosition;

    public GraphicPanelVectorField2D() {

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

    public ArrayList<double[]> getvectorField() {
        return this.vectorField2D;
    }

    public double getAxeCenterX() {
        return this.axeCenterX;
    }

    public double getAxeCenterY() {
        return this.axeCenterY;
    }

    public Matrix getVectorFieldExpression() {
        return this.vectorFieldExpr;
    }

    public ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    public void setVarAbsc(String varAbsc) {
        this.varAbsc = varAbsc;
    }

    public void setVars(String varAbsc, String varOrd) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
    }

    public void setVectorFieldExpression(Matrix vectorFieldExpr) {
        this.vectorFieldExpr = vectorFieldExpr;
        this.vectorField2D.clear();
    }

    /**
     * Voraussetzung: vectorFieldExpr, varAbsc und varOrd sind bereits gesetzt.
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
     * Voraussetzung: graph ist bereits initialisiert (bzw. mit Funktionswerten
     * gefüllt). maxX, maxY sind bekannt/initialisiert.
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
    private void expressionToVectorField(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();
        double varOrdStart = exprOrdStart.evaluate();
        double varOrdEnd = exprOrdEnd.evaluate();

        this.vectorField2D.clear();
        double[] vectorFieldArrow = new double[4];

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
            for (int i = 0; i <= 50; i++) {
                for (int j = 0; j <= 50; j++) {
                    vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * i / 50;
                    vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 50;
                    vectorFieldArrow[2] = vectorFieldArrow[0] + constAbscValue;
                    vectorFieldArrow[3] = vectorFieldArrow[1] + constOrdValue;
                }
            }
        } else {
            Variable.setValue(this.varAbsc, varAbscStart);
            Variable.setValue(this.varOrd, varOrdStart);
            for (int i = 0; i <= 50; i++) {
                for (int j = 0; j <= 50; j++) {
                    vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * j / 50;
                    vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 50;
                    Variable.setValue(this.varAbsc, varAbscStart + (varAbscEnd - varAbscStart) * i / 50);
                    Variable.setValue(this.varOrd, varOrdStart + (varOrdEnd - varOrdStart) * j / 50);
                    try {
                        vectorFieldArrow[2] = this.vectorFieldExpr.getEntry(0, 0).evaluate();
                        vectorFieldArrow[3] = this.vectorFieldExpr.getEntry(1, 0).evaluate();
                    } catch (EvaluationException e) {
                        vectorFieldArrow[2] = Double.NaN;
                        vectorFieldArrow[3] = Double.NaN;
                    }
                }
            }
        }

        this.vectorField2D.add(vectorFieldArrow);

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Vektorfeldes.
     * Voraussetzung: maxX, maxY sind bekannt!
     */
    private ArrayList<int[]> convertVectorFieldToGraphicalVectorField() {

        ArrayList<int[]> graphicalVectorField = new ArrayList<>();
        int[] graphicalVectorFieldArrow = new int[4];

        for (int i = 0; i < this.vectorField2D.size(); i++) {
            graphicalVectorFieldArrow[0] = (int) Math.round(250 + 250 * (this.vectorField2D.get(i)[0] - this.axeCenterX) / this.maxX);
            graphicalVectorFieldArrow[1] = (int) Math.round(250 - 250 * (this.vectorField2D.get(i)[1] - this.axeCenterY) / this.maxY);
            graphicalVectorFieldArrow[2] = (int) Math.round(250 + 250 * (this.vectorField2D.get(i)[2] - this.axeCenterX) / this.maxX);
            graphicalVectorFieldArrow[3] = (int) Math.round(250 - 250 * (this.vectorField2D.get(i)[3] - this.axeCenterY) / this.maxY);
            graphicalVectorField.add(graphicalVectorFieldArrow);
        }

        return graphicalVectorField;

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

    private void drawVectorField2D(Graphics g) {

        // Weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        // Markierungen an den Achsen berechnen.
        computeExpXExpY();

        //Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g, this.varAbsc);

        //Graphen zeichnen
        if (this.vectorField2D.isEmpty()) {
            return;
        }

        ArrayList<int[]> graphicalGraph = convertVectorFieldToGraphicalVectorField();
        g.setColor(fixedColors[0]);

        for (int[] vectorArrow : graphicalGraph) {

            if (!Double.isNaN(vectorArrow[0]) && !Double.isInfinite(vectorArrow[0])
                    && !Double.isNaN(vectorArrow[1]) && !Double.isInfinite(vectorArrow[1])
                    && !Double.isNaN(vectorArrow[2]) && !Double.isInfinite(vectorArrow[2])
                    && !Double.isNaN(vectorArrow[3]) && !Double.isInfinite(vectorArrow[3])) {

                g.drawLine(vectorArrow[0], vectorArrow[1], vectorArrow[2], vectorArrow[3]);

            }

        }

        g.setColor(Color.black);

    }

    public void drawVectorField2D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, Matrix vectorFieldExpr) throws EvaluationException {
        setVectorFieldExpression(vectorFieldExpr);
        computeScreenSizes(x_0, x_1, y_0, y_1);
        expressionToVectorField(x_0, x_1, y_0, y_1);
        drawVectorField2D();
    }

    public void drawVectorField2D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        drawVectorField2D(g);

        /**
         * Im Folgenden wird das Vektorfeld in einem größeren/kleineren Bereich
         * gezeichnet, falls der aktuelle Zoomfaktor derart berechnet wurde,
         * dass das Vektorfeld zu grob oder zu klein ist.
         */
        try {
            Constant varAbscStart = new Constant(this.axeCenterX - 2 * this.maxX);
            Constant varAbscEnd = new Constant(this.axeCenterX + 2 * this.maxX);
            Constant varOrdStart = new Constant(this.axeCenterY - 2 * this.maxY);
            Constant varOrdEnd = new Constant(this.axeCenterY + 2 * this.maxY);
            if (this.vectorFieldExpr != null) {
                expressionToVectorField(varAbscStart, varAbscEnd, varOrdStart, varOrdEnd);
            }
        } catch (EvaluationException e) {
        }
        convertVectorFieldToGraphicalVectorField();

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
