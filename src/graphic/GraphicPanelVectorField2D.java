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
import java.math.BigDecimal;
import java.util.ArrayList;
import abstractexpressions.matrixexpression.classes.Matrix;
import lang.translator.Translator;

public class GraphicPanelVectorField2D extends AbstractGraphicPanel2D {

    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varAbsc, varOrd;

    private Matrix vectorFieldExpr;
    private final ArrayList<double[]> vectorField2D = new ArrayList<>();

    private final Color color = Color.blue;

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

    public Color getColor() {
        return this.color;
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
     * Berechnet die Pixelkoordinaten des (gröberen) Vektorfeldes.
     * Voraussetzung: maxX, maxY sind bekannt!
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

    /**
     * Zeichnet die Achsen und die grauen Niveaulinien. Die erste Koordinate
     * heißt varAbsc, die zweite varOrd.
     */
    private void drawAxesAndLines(Graphics g) {
        g.setColor(Color.lightGray);

        int linePosition;
        int k = (int) (this.axeCenterX * Math.pow(10, -this.expX));

        // x-Niveaulinien zeichnen
        linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];

        while (linePosition <= 500) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
                g.drawLine(linePosition, 0, linePosition, 500);

                if (250 * this.axeCenterY / this.maxY - 3 <= 248 && 250 * this.axeCenterY / this.maxY - 3 >= -230) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
                } else if (250 * this.axeCenterY / this.maxY - 3 > 248) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 495);
                } else {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 20);
                }

            }

            k++;

        }

        k = (int) (this.axeCenterX * Math.pow(10, -this.expX)) - 1;
        linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];

        while (linePosition >= 0) {

            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
                g.drawLine(linePosition, 0, linePosition, 500);

                if (250 * this.axeCenterY / this.maxY - 3 <= 248 && (250 * this.axeCenterY / this.maxY - 3 >= -230)) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
                } else if (250 * this.axeCenterY / this.maxY - 3 > 248) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 495);
                } else {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 20);
                }

            }

            k--;

        }

        // y-Niveaulinien zeichnen
        k = (int) (this.axeCenterY * Math.pow(10, -this.expY));
        linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];

        while (linePosition >= 0) {
            linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
            g.drawLine(0, linePosition, 500, linePosition);

            if (250 * this.axeCenterX / this.maxX - 3 >= -225 && 250 * this.axeCenterX / this.maxX - 3 <= 245) {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, linePosition - 3);
            } else if (250 * this.axeCenterX / this.maxX - 3 >= -225) {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 5, linePosition - 3);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 475, linePosition - 3);
            }

            k++;

        }

        k = (int) (this.axeCenterY * Math.pow(10, -this.expY)) - 1;
        linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];

        while (linePosition <= 500) {
            linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
            g.drawLine(0, linePosition, 500, linePosition);

            if (250 * this.axeCenterX / this.maxX - 3 >= -225 && 250 * this.axeCenterX / this.maxX - 3 <= 245) {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, linePosition - 3);
            } else if (250 * this.axeCenterX / this.maxX - 3 >= -225) {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 5, linePosition - 3);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(k, this.expY)), 475, linePosition - 3);
            }

            k--;

        }

        // Achsen inkl. Achsenbezeichnungen eintragen
        // Achsen
        g.setColor(Color.black);
        g.drawLine(0, 250 + (int) (250 * this.axeCenterY / this.maxY), 500, 250 + (int) (250 * this.axeCenterY / this.maxY));
        g.drawLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX), 500);
        // Achsenpfeile
        g.drawLine(500, 250 + (int) (250 * this.axeCenterY / this.maxY), 494, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
        g.drawLine(500, 250 + (int) (250 * this.axeCenterY / this.maxY), 494, 250 + (int) (250 * this.axeCenterY / this.maxY) + 3);
        g.drawLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX) + 3, 6);
        g.drawLine(250 - (int) (250 * this.axeCenterX / this.maxX), 0, 250 - (int) (250 * this.axeCenterX / this.maxX) - 3, 6);
        /**
         * Achsenbeschriftung WICHTIG: In der Prozedur drawString werden die
         * Achsenbeschriftung derart eingetragen, dass (1) Die Beschriftung der
         * Variablen var innerhalb des Bildschirms liegt (5 px) (2) Die
         * Beschriftung f(var) links von der vertikalen Achse liegt (5 px)
         * Hierzu müssen die Pixellängen der gezeichneten Strings ausgerechnet
         * werden (mittels g.getFontMetrics().stringWidth()).
         */
        g.drawString(this.varAbsc, 500 - 5 - g.getFontMetrics().stringWidth(this.varAbsc), 250 + (int) (250 * this.axeCenterY / this.maxY) + 15);
        g.drawString(this.varOrd, 250 - (int) (250 * this.axeCenterX / this.maxX) - 5 - g.getFontMetrics().stringWidth(this.varOrd), 20);
    }

    private void drawVectorField2D(Graphics g) {

        // Weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        // Markierungen an den Achsen berechnen.
        computeExpXExpY();

        // Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g);

        // Vektorfeld zeichnen
        if (this.vectorField2D.isEmpty()) {
            return;
        }

        ArrayList<int[]> graphicalVectorField = convertVectorFieldToGraphicalVectorField();
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
                drawArrow(g, vectorArrow[2], vectorArrow[3], 5, angle);

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

    private void drawArrow(Graphics g, int x, int y, int length, double angleOfArrow) {
        if (angleOfArrow == -1) {
            return;
        }
        double angleForLeftArrowPart = angleOfArrow - Math.PI / 4, angleForRightArrowPart = angleOfArrow + Math.PI / 4;
        g.drawLine(x, y, x - (int) (length * Math.cos(angleForLeftArrowPart)), y - (int) (length * Math.sin(angleForLeftArrowPart)));
        g.drawLine(x, y, x - (int) (length * Math.cos(angleForRightArrowPart)), y - (int) (length * Math.sin(angleForRightArrowPart)));
    }

    public void drawVectorField2D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, Expression[] vectorFieldComponents) throws EvaluationException {
        setVectorFieldExpression(vectorFieldComponents);
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

    }

}
