package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import lang.translator.Translator;

public class GraphicPanelImplicit2D extends AbstractGraphicPanel2D {

    // Variablennamen für 2D-Graphen: Absc = Abszisse, Ord = Ordinate.
    private String varAbsc, varOrd;

    /*
     Es können sich mehrere Graphen jn graph2D befinden. Auf dje einzelnen
     Graphen kann dann jeweils über die Keys 0, 1, 2, ..., this.graph.size() -
     1 zugegriffen werden.
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
     * Voraussetzung: expr und var sind bereits gesetzt.
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
     * Zeichnet die Achsen und die grauen Niveaulinien. Die erste Koordinate
     * heißt varAbsc, die zweite varOrd.
     */
    private void drawAxesAndLines(Graphics g) {
        g.setColor(Color.lightGray);
        
        int linePosition;
        int k = (int) (this.axeCenterX * Math.pow(10, -this.expX));

        //x-Niveaulinien zeichnen
        linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
        
        while (linePosition <= 500) {
            
            if (k != 0) {
                linePosition = convertToPixel(k * Math.pow(10, this.expX), 0)[0];
                g.drawLine(linePosition, 0, linePosition, 500);
                
                if ((250 * this.axeCenterY / this.maxY - 3 <= 248) && (250 * this.axeCenterY / this.maxY - 3 >= -230)) {
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
                
                if ((250 * this.axeCenterY / this.maxY - 3 <= 248) && (250 * this.axeCenterY / this.maxY - 3 >= -230)) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 250 + (int) (250 * this.axeCenterY / this.maxY) - 3);
                } else if (250 * this.axeCenterY / this.maxY - 3 > 248) {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 495);
                } else {
                    g.drawString(String.valueOf(roundAxisEntries(k, this.expX)), linePosition + 3, 20);
                }
                
            }
            
            k--;
            
        }

        //y-Niveaulinien zeichnen
        k = (int) (this.axeCenterY * Math.pow(10, -this.expY));
        linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
        
        while (linePosition >= 0) {
            linePosition = convertToPixel(0, k * Math.pow(10, this.expY))[1];
            g.drawLine(0, linePosition, 500, linePosition);
            
            if ((250 * this.axeCenterX / this.maxX - 3 >= -225) && (250 * this.axeCenterX / this.maxX - 3 <= 245)) {
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
            
            if ((250 * this.axeCenterX / this.maxX - 3 >= -225) && (250 * this.axeCenterX / this.maxX - 3 <= 245)) {
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
        g.drawLine(500, 250 + (int) (250 * this.axeCenterY / this.maxY), 494, 250 + (int) (250 *this. axeCenterY / this.maxY) + 3);
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
        g.drawString(this.varOrd, 250 - (int) (250 * this.axeCenterX / this.maxX) - 5 - g.getFontMetrics().stringWidth(varOrd), 20);
    }
    
    private void drawImplicitGraph2D(Graphics g, String varAbsc, String varOrd) {
        // Weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);
        // Markierungen an den Achsen berechnen.
        computeExpXExpY();
        // Niveaulinien und Achsen zeichnen
        drawAxesAndLines(g);
        g.setColor(this.color);
        int[] pixelsOfImplicitGraph;
        for (double[] point : this.implicitGraph2D) {
            pixelsOfImplicitGraph = convertToPixel(point[0], point[1]);
            g.drawLine(pixelsOfImplicitGraph[0], pixelsOfImplicitGraph[1], pixelsOfImplicitGraph[0], pixelsOfImplicitGraph[1]);
        }
    }
    
    public void drawImplicitGraph2D(ArrayList<double[]> implicitGraph2D, Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {
        this.implicitGraph2D = implicitGraph2D;
        computeScreenSizes(exprAbscStart, exprAbscEnd, exprOrdStart, exprOrdEnd);
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawImplicitGraph2D(g, this.varAbsc, this.varOrd);
    }

}
