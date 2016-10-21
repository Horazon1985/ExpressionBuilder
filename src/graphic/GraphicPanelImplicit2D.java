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

    private MarchingSquare[][] implicitGraph2D;

    private final Color color = Color.blue;

    public GraphicPanelImplicit2D() {
        super();
    }

    public static class MarchingSquare {

        private final Double[][] vertexValues = new Double[2][2];

        public MarchingSquare() {
            this.vertexValues[0][0] = (double) 0;
            this.vertexValues[0][1] = (double) 0;
            this.vertexValues[1][0] = (double) 0;
            this.vertexValues[1][1] = (double) 0;
        }

        public Double[][] getVertexValues() {
            return this.vertexValues;
        }

        public Double getVertexValue(int i, int j) {
            return this.vertexValues[i][j];
        }

        public void setVertexValue(int i, int j, double value) {
            if (i >= 0 && i <= 1 && j >= 0 && j <= 1) {
                this.vertexValues[i][j] = value;
            }
        }

        public boolean isZeroSquare() {
            return this.vertexValues[0][0] == 0 && this.vertexValues[0][1] == 0
                    && this.vertexValues[1][0] == 0 && this.vertexValues[1][1] == 0;
        }

        @Override
        public String toString() {
            return "[" + this.vertexValues[0][0] + ", " + this.vertexValues[0][1] + ", " + this.vertexValues[1][0] + ", " + this.vertexValues[1][1] + "]";
        }

        public int getNumberOfInnerVertices() {
            int number = 0;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    if (this.vertexValues[i][j] <= 0) {
                        number++;
                    }
                }
            }
            return number;
        }

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

    private void drawMarchingSquares(Graphics g) {
        for (int i = 0; i < this.implicitGraph2D.length; i++) {
            for (int j = 0; j < this.implicitGraph2D.length; j++) {
                drawSingleMarchingSquare(g, i, j);
            }
        }
    }

    private void drawSingleMarchingSquare(Graphics g, int i, int j) {

        int[] pixel, pixelNext;
        MarchingSquare square = this.implicitGraph2D[i][j];
        Double[][] squareVertexValues = this.implicitGraph2D[i][j].getVertexValues();

        /* 
        Sonderfall: Alle Ecken haben die Werte 0. Dann soll das Rechteck 
        komplett gefüllt werden.
         */
        if (square.isZeroSquare()) {
            drawSingleZeroMarchingSquare(g, i, j);
            return;
        }

        int numberOfIntervalsAlongX = this.implicitGraph2D.length;
        int numberOfIntervalsAlongY = this.implicitGraph2D[0].length;
        double deltaX = 2 * this.maxX / numberOfIntervalsAlongX;
        double deltaY = 2 * this.maxY / numberOfIntervalsAlongY;
        double[] vertexCoordinates;

        // Behandlung aller Fälle, in denen isolierte Ecken vorliegen.
        if (square.getNumberOfInnerVertices() == 3) {
            if (squareVertexValues[0][0] >= 0) {
                vertexCoordinates = getCoordinates(i, j);
                pixel = convertToPixel(vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][0], squareVertexValues[1][0]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[0][0], squareVertexValues[0][1]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[1][0] >= 0) {
                vertexCoordinates = getCoordinates(i + 1, j);
                pixel = convertToPixel(vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][0], squareVertexValues[0][0]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[1][0], squareVertexValues[1][1]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[0][1] >= 0) {
                vertexCoordinates = getCoordinates(i, j + 1);
                pixel = convertToPixel(vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][1], squareVertexValues[1][1]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[0][1], squareVertexValues[0][0]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else {
                vertexCoordinates = getCoordinates(i + 1, j + 1);
                pixel = convertToPixel(vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][1], squareVertexValues[0][1]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[1][1], squareVertexValues[1][0]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
        } else if (square.getNumberOfInnerVertices() <= 2) {
            if (squareVertexValues[0][0] <= 0 && squareVertexValues[1][0] > 0 && squareVertexValues[0][1] > 0) {
                vertexCoordinates = getCoordinates(i, j);
                pixel = convertToPixel(vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][0], squareVertexValues[1][0]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[0][0], squareVertexValues[0][1]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            if (squareVertexValues[1][0] <= 0 && squareVertexValues[0][0] > 0 && squareVertexValues[1][1] > 0) {
                vertexCoordinates = getCoordinates(i + 1, j);
                pixel = convertToPixel(vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][0], squareVertexValues[0][0]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[1][0], squareVertexValues[1][1]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            if (squareVertexValues[0][1] <= 0 && squareVertexValues[0][0] > 0 && squareVertexValues[1][1] > 0) {
                vertexCoordinates = getCoordinates(i, j + 1);
                pixel = convertToPixel(vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][1], squareVertexValues[1][1]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[0][1], squareVertexValues[0][0]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            if (squareVertexValues[1][1] <= 0 && squareVertexValues[1][0] > 0 && squareVertexValues[0][1] > 0) {
                vertexCoordinates = getCoordinates(i + 1, j + 1);
                pixel = convertToPixel(vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][1], squareVertexValues[0][1]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[1][1], squareVertexValues[1][0]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            // Behandlung aller Fälle, in denen eine isolierte Kante vorliegt.
            if (squareVertexValues[0][0] <= 0 && squareVertexValues[1][0] <= 0) {
                vertexCoordinates = getCoordinates(i, j);
                pixel = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[0][0], squareVertexValues[0][1]));
                pixelNext = convertToPixel(vertexCoordinates[0] + deltaX, vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[1][0], squareVertexValues[1][1]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[0][0] <= 0 && squareVertexValues[0][1] <= 0) {
                vertexCoordinates = getCoordinates(i, j);
                pixel = convertToPixel(vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][0], squareVertexValues[1][0]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][1], squareVertexValues[1][1]), vertexCoordinates[1] + deltaY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[1][0] <= 0 && squareVertexValues[1][1] <= 0) {
                vertexCoordinates = getCoordinates(i + 1, j);
                pixel = convertToPixel(vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][0], squareVertexValues[0][0]), vertexCoordinates[1]);
                pixelNext = convertToPixel(vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][1], squareVertexValues[0][1]), vertexCoordinates[1] + deltaY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[0][1] <= 0 && squareVertexValues[1][1] <= 0) {
                vertexCoordinates = getCoordinates(i, j + 1);
                pixel = convertToPixel(vertexCoordinates[0], vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[0][1], squareVertexValues[0][0]));
                pixelNext = convertToPixel(vertexCoordinates[0] + deltaX, vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[1][1], squareVertexValues[1][0]));
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }

        }

    }

    /**
     * Füllt das Rechteck an der Position (i, j) vollständig mit der Farbe des
     * Graphen aus.
     */
    private void drawSingleZeroMarchingSquare(Graphics g, int i, int j) {
        int numberOfIntervalsAlongX = this.implicitGraph2D.length;
        int numberOfIntervalsAlongY = this.implicitGraph2D[0].length;
        double deltaX = 2 * this.maxX / numberOfIntervalsAlongX;
        double deltaY = 2 * this.maxY / numberOfIntervalsAlongY;
        double[] vertexCoordinates = getCoordinates(i, j);
        int[] bottomLeft = convertToPixel(vertexCoordinates[0], vertexCoordinates[1]);
        int[] topRight = convertToPixel(vertexCoordinates[0] + deltaX, vertexCoordinates[1] + deltaY);
        g.fillRect(bottomLeft[0], bottomLeft[1], topRight[0] - bottomLeft[0], bottomLeft[1] - topRight[1]);
    }

    private double[] getCoordinates(int i, int j) {
        double[] coordinates = new double[2];
        coordinates[0] = this.axeCenterX - this.maxX + 2 * i * this.maxX / this.implicitGraph2D.length;
        coordinates[1] = this.axeCenterY - this.maxY + 2 * j * this.maxY / this.implicitGraph2D[0].length;
        return coordinates;
    }

    private double getFactor(double valueOne, double valueTwo) {
        if (valueOne == 0 && valueTwo == 0) {
            return 0;
        }
        return Math.abs(valueOne) / (Math.abs(valueOne) + Math.abs(valueTwo));
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
        drawMarchingSquares(g);
    }

    /**
     * Hauptmethode zum Zeichnen eines Graphen einer implizit gegebenen
     * Funktion.
     *
     * @throws EvaluationException
     */
    public void drawImplicitGraph2D(MarchingSquare[][] implicitGraph2D, Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd) throws EvaluationException {
        this.implicitGraph2D = implicitGraph2D;
        computeScreenSizes(exprAbscStart, exprAbscEnd, exprOrdStart, exprOrdEnd);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawImplicitGraph2D(g);
    }

    protected void drawMousePointOnGraph(Graphics g) {

    }
    
    private int[] getIndexPair(int x, int y){
        int[] indices = new int[4];
        int indexX = (x * this.implicitGraph2D.length)/ 500;
        int indexY = (x * this.implicitGraph2D[0].length)/ 500;
        indices[0] = Math.max(0, indexX - 2);
        indices[1] = Math.min(this.implicitGraph2D.length - 1, indexX + 2);
        indices[2] = Math.max(0, indexY - 2);
        indices[3] = Math.min(this.implicitGraph2D[0].length - 1, indexY + 2);
        return indices;
    }

}
