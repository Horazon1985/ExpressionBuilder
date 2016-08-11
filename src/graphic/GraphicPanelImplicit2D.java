package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import lang.translator.Translator;

public class GraphicPanelImplicit2D extends AbstractGraphicPanel2D {

    /**
     * Funktionsterme für den Graphen einer implizit gegebenen Funktion. Dieses
     * ArrayList wird vor dem Plotten mit genau zwei Ausdrücken f und g gefüllt,
     * so dass der Graph der implizit gegebenen Funktion f = g geplottet wird.
     */
    private ArrayList<Expression> exprs = new ArrayList<>();
    private ArrayList<double[]> implicitGraph2D = new ArrayList<>();

    private final Color color = Color.blue;

    public GraphicPanelImplicit2D() {
        super();
    }

    public Color getColor() {
        return this.color;
    }

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_GraphicImplicit2D_IMPOSSIBLE_MOVE_GRAPH"));
        return instructions;
    }

    public void setVars(String varAbsc, String varOrd) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
    }

    public void setExpressions(Expression... exprs) {
        this.exprs = new ArrayList<>();
        this.exprs.addAll(Arrays.asList(exprs));
    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: implicitGraph2D ist bereits initialisiert.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {
        this.axeCenterX = (exprAbscStart.evaluate() + exprAbscEnd.evaluate()) / 2;
        this.axeCenterY = (exprOrdStart.evaluate() + exprOrdEnd.evaluate()) / 2;
        this.maxX = exprAbscEnd.evaluate() - this.axeCenterX;
        this.maxY = exprOrdEnd.evaluate() - this.axeCenterY;
    }

    /**
     * Hauptmethode zum Zeichnen eines Graphen einer implizit gegebenen
     * Funktion, die von der öffentlichen Methode drawImplicitGraph2D(...)
     * aufgerufen wird.
     *
     * @throws EvaluationException
     */
    private void drawImplicitGraph2D(Graphics g) {
        g.setColor(this.color);
        int[] pixelsOfImplicitGraph;
        for (double[] point : this.implicitGraph2D) {
            pixelsOfImplicitGraph = convertToPixel(point[0], point[1]);
            g.drawLine(pixelsOfImplicitGraph[0], pixelsOfImplicitGraph[1], pixelsOfImplicitGraph[0], pixelsOfImplicitGraph[1]);
        }
    }

    /**
     * Hauptmethode zum Zeichnen eines Graphen einer implizit gegebenen
     * Funktion.
     *
     * @throws EvaluationException
     */
    public void drawImplicitGraph2D(ArrayList<double[]> implicitGraph2D, Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {
        this.implicitGraph2D = implicitGraph2D;
        computeScreenSizes(exprAbscStart, exprAbscEnd, exprOrdStart, exprOrdEnd);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawImplicitGraph2D(g);
    }

}
