package graphic.swing;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import graphic.util.MarchingSquare;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lang.translator.Translator;

public class GraphicPanelImplicit2D extends AbstractGraphicPanel2D {

    /**
     * Funktionsterme für den Graphen einer implizit gegebenen Funktion. Diese
     * Liste wird vor dem Plotten mit genau zwei Ausdrücken f und g gefüllt,
     * so dass der Graph der implizit gegebenen Funktion f = g geplottet wird.
     */
    private List<Expression> exprs = new ArrayList<>();

    private MarchingSquare[][] implicitGraph2D;

    private GraphPointsInMarchingSquare[][] graphPoints;

    private final Color color = Color.blue;

    public GraphicPanelImplicit2D() {
        super();
    }

    private static class GraphPointsInMarchingSquare {

        private final List<Double[]> points = new ArrayList<>();

        public void addPoints(Double[]... points) {
            this.points.addAll(Arrays.asList(points));
        }

        public List<Double[]> getPoints() {
            return this.points;
        }

        @Override
        public String toString() {
            String graphPoints = "[";
            for (int i = 0; i < this.points.size(); i++) {
                graphPoints += "(" + this.points.get(i)[0] + ", " + this.points.get(i)[1] + ")";
                if (i < this.points.size() - 1) {
                    graphPoints += ", ";
                }
            }
            return graphPoints + "]";
        }

    }

    public Color getColor() {
        return this.color;
    }

    public List<Expression> getExpressions() {
        return this.exprs;
    }

    public static List<String> getInstructions() {
        List<String> instructions = new ArrayList<>();
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
        this.graphPoints = new GraphPointsInMarchingSquare[this.implicitGraph2D.length][this.implicitGraph2D[0].length];
        for (int i = 0; i < this.implicitGraph2D.length; i++) {
            for (int j = 0; j < this.implicitGraph2D.length; j++) {
                drawSingleMarchingSquareAndComputeGraphPoints(g, i, j);
            }
        }
    }

    private void drawSingleMarchingSquareAndComputeGraphPoints(Graphics g, int i, int j) {

        int[] pixel, pixelNext;
        MarchingSquare square = this.implicitGraph2D[i][j];
        Double[][] squareVertexValues = this.implicitGraph2D[i][j].getVertexValues();

        this.graphPoints[i][j] = new GraphPointsInMarchingSquare();

        /* 
        Sonderfall: Alle Ecken haben die Werte 0. Dann soll das Rechteck 
        komplett gefüllt werden.
         */
        if (square.isZeroSquare()) {
            this.graphPoints[i][j].addPoints(getWrappedCoordinates(i, j),
                    getWrappedCoordinates(i + 1, j),
                    getWrappedCoordinates(i, j + 1),
                    getWrappedCoordinates(i + 1, j + 1));
            drawSingleZeroMarchingSquare(g, i, j);
            return;
        }

        int numberOfIntervalsAlongX = this.implicitGraph2D.length;
        int numberOfIntervalsAlongY = this.implicitGraph2D[0].length;
        double deltaX = 2 * this.maxX / numberOfIntervalsAlongX;
        double deltaY = 2 * this.maxY / numberOfIntervalsAlongY;
        double[] vertexCoordinates;

        double coordinateX, coordinateY;

        // Behandlung aller Fälle, in denen isolierte Ecken vorliegen.
        if (square.getNumberOfInnerVertices() == 3) {
            if (squareVertexValues[0][0] >= 0) {
                vertexCoordinates = getCoordinates(i, j);
                coordinateX = vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][0], squareVertexValues[1][0]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[0][0], squareVertexValues[0][1]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[1][0] >= 0) {
                vertexCoordinates = getCoordinates(i + 1, j);
                coordinateX = vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][0], squareVertexValues[0][0]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[1][0], squareVertexValues[1][1]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[0][1] >= 0) {
                vertexCoordinates = getCoordinates(i, j + 1);
                coordinateX = vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][1], squareVertexValues[1][1]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[0][1], squareVertexValues[0][0]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else {
                vertexCoordinates = getCoordinates(i + 1, j + 1);
                coordinateX = vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][1], squareVertexValues[0][1]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[1][1], squareVertexValues[1][0]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
        } else if (square.getNumberOfInnerVertices() <= 2) {
            if (squareVertexValues[0][0] <= 0 && squareVertexValues[1][0] > 0 && squareVertexValues[0][1] > 0) {
                vertexCoordinates = getCoordinates(i, j);
                coordinateX = vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][0], squareVertexValues[1][0]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[0][0], squareVertexValues[0][1]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            if (squareVertexValues[1][0] <= 0 && squareVertexValues[0][0] > 0 && squareVertexValues[1][1] > 0) {
                vertexCoordinates = getCoordinates(i + 1, j);
                coordinateX = vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][0], squareVertexValues[0][0]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[1][0], squareVertexValues[1][1]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            if (squareVertexValues[0][1] <= 0 && squareVertexValues[0][0] > 0 && squareVertexValues[1][1] > 0) {
                vertexCoordinates = getCoordinates(i, j + 1);
                coordinateX = vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][1], squareVertexValues[1][1]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[0][1], squareVertexValues[0][0]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            if (squareVertexValues[1][1] <= 0 && squareVertexValues[1][0] > 0 && squareVertexValues[0][1] > 0) {
                vertexCoordinates = getCoordinates(i + 1, j + 1);
                coordinateX = vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][1], squareVertexValues[0][1]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[1][1], squareVertexValues[1][0]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            }
            // Behandlung aller Fälle, in denen eine isolierte Kante vorliegt.
            if (squareVertexValues[0][0] <= 0 && squareVertexValues[1][0] <= 0) {
                vertexCoordinates = getCoordinates(i, j);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[0][0], squareVertexValues[0][1]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0] + deltaX;
                coordinateY = vertexCoordinates[1] + deltaY * getFactor(squareVertexValues[1][0], squareVertexValues[1][1]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[0][0] <= 0 && squareVertexValues[0][1] <= 0) {
                vertexCoordinates = getCoordinates(i, j);
                coordinateX = vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][0], squareVertexValues[1][0]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0] + deltaX * getFactor(squareVertexValues[0][1], squareVertexValues[1][1]);
                coordinateY = vertexCoordinates[1] + deltaY;
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[1][0] <= 0 && squareVertexValues[1][1] <= 0) {
                vertexCoordinates = getCoordinates(i + 1, j);
                coordinateX = vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][0], squareVertexValues[0][0]);
                coordinateY = vertexCoordinates[1];
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0] - deltaX * getFactor(squareVertexValues[1][1], squareVertexValues[0][1]);
                coordinateY = vertexCoordinates[1] + deltaY;
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
                g.drawLine(pixel[0], pixel[1], pixelNext[0], pixelNext[1]);
            } else if (squareVertexValues[0][1] <= 0 && squareVertexValues[1][1] <= 0) {
                vertexCoordinates = getCoordinates(i, j + 1);
                coordinateX = vertexCoordinates[0];
                coordinateY = vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[0][1], squareVertexValues[0][0]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixel = convertToPixel(coordinateX, coordinateY);
                coordinateX = vertexCoordinates[0] + deltaX;
                coordinateY = vertexCoordinates[1] - deltaY * getFactor(squareVertexValues[1][1], squareVertexValues[1][0]);
                this.graphPoints[i][j].addPoints(new Double[]{coordinateX, coordinateY});
                pixelNext = convertToPixel(coordinateX, coordinateY);
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

    private Double[] getWrappedCoordinates(int i, int j) {
        Double[] coordinates = new Double[2];
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
        if (pointsAreShowable) {
            drawMousePointOnGraph(g);
        }
    }

    @Override
    protected void drawMousePointOnGraph(Graphics g) {

        int minIndexX = Math.max(0, (int) Math.round((double) (this.mouseCoordinateX * this.implicitGraph2D.length) / 500) - (this.implicitGraph2D.length * MOUSE_DISTANCE_FOR_SHOWING_POINT / 500));
        int maxIndexX = Math.min(this.implicitGraph2D.length - 1, (int) Math.round((double) (this.mouseCoordinateX * this.implicitGraph2D.length) / 500) + (this.implicitGraph2D.length * MOUSE_DISTANCE_FOR_SHOWING_POINT / 500));
        int minIndexY = Math.max(0, (int) Math.round((double) ((500 - this.mouseCoordinateY) * this.implicitGraph2D[0].length) / 500) - (this.implicitGraph2D[0].length * MOUSE_DISTANCE_FOR_SHOWING_POINT / 500));
        int maxIndexY = Math.min(this.implicitGraph2D[0].length - 1, (int) Math.round((double) ((500 - this.mouseCoordinateY) * this.implicitGraph2D[0].length) / 500) + (this.implicitGraph2D[0].length * MOUSE_DISTANCE_FOR_SHOWING_POINT / 500));

        int minimalDistance = -1;
        int currentDistance;
        int coordinateXWithMinimalDistance = -1;
        int coordinateYWithMinimalDistance = -1;

        for (int i = minIndexX; i <= maxIndexX; i++) {
            for (int j = minIndexY; j <= maxIndexY; j++) {

                if (!this.implicitGraph2D[i][j].containsGraph()) {
                    continue;
                }

                for (Double[] points : this.graphPoints[i][j].getPoints()) {
                    int[] pixel = convertToPixel(points[0], points[1]);
                    currentDistance = computeDistanceOfPixels(pixel, getMouseCoordinates());
                    if (currentDistance <= MOUSE_DISTANCE_FOR_SHOWING_POINT) {
                        if (minimalDistance == -1 || currentDistance < minimalDistance) {
                            minimalDistance = currentDistance;
                            coordinateXWithMinimalDistance = pixel[0];
                            coordinateYWithMinimalDistance = pixel[1];
                        }
                    }
                }

            }
        }

        if (minimalDistance != -1) {
            drawCirclePoint(g, coordinateXWithMinimalDistance, coordinateYWithMinimalDistance, true);
        }

    }

}
