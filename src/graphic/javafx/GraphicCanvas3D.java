package graphic.javafx;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphicCanvas3D extends AbstractGraphicCanvas3D {

    /**
     * Variablenname für 3D-Graphen: varAbsc = Abszissenname.
     */
    private String varAbsc;
    /**
     * Variablenname für 3D-Graphen: varOrd = Ordinatenname.
     */
    private String varOrd;
    private final ArrayList<Expression> exprs = new ArrayList<>();
    private ArrayList<double[][][]> graphs3D = new ArrayList<>();
    /**
     * "Vergröberte Version" von Graphs3D (GRUND: beim Herauszoomen dürfen die
     * Plättchen am Graphen nicht so klein sein. Deshalb muss der Graph etwas
     * vergröbert werden).
     */
    private ArrayList<double[][][]> graphs3DForGraphic = new ArrayList<>();
    private ArrayList<boolean[][]> graphs3DAreDefined = new ArrayList<>();

    private final ArrayList<Color> colors = new ArrayList<>();

    private final static Color[] FIXED_COLORS = {Color.rgb(170, 170, 70), Color.rgb(170, 70, 170), Color.rgb(70, 170, 170)};

    public GraphicCanvas3D() {
        super();
    }

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public ArrayList<Color> getColors() {
        return this.colors;
    }

    private void setExpressions(ArrayList<Expression> exprs) {
        this.exprs.clear();
        this.exprs.addAll(exprs);
        setColors();
    }

    private void setExpressions(Expression... exprs) {
        this.exprs.clear();
        this.exprs.addAll(Arrays.asList(exprs));
        setColors();
    }

    private void setColors() {
        int numberOfColors = Math.max(this.exprs.size(), this.graphs3D.size());
        this.colors.clear();
        for (int i = this.colors.size(); i < numberOfColors; i++) {
            if (i < FIXED_COLORS.length) {
                this.colors.add(FIXED_COLORS[i]);
            } else {
                this.colors.add(generateColor());
            }
        }
    }

    private Color generateColor() {
        return Color.rgb((int) (70 + 100 * Math.random()), (int) (100 * Math.random()), (int) (70 + 100 * Math.random()));
    }

    public void setParameters(String varAbsc, String varOrd, double bigRadius, double heightProjection, double angle,
            double verticalAngle) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
        this.heightProjection = heightProjection;
        this.bigRadius = bigRadius;
        this.smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
        this.height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);
        this.angle = angle;
        this.verticalAngle = verticalAngle;
        this.zoomfactor = 1;
    }

    /**
     * Berechnet die Maße Darstellungsbereichs der Graphen.<br>
     * VOLRAUSSETZUNG: graphs3D ist bereits initialisiert.
     */
    private void computeScreenSizes() {
        super.computeScreenSizes(this.graphs3D, false, false, true);
    }

    /**
     * Berechnet die Gitterpunkte für die 3D-Graphen aus den Ausdrücken exprs.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(Expression exprX_0, Expression exprX_1, Expression exprY_0, Expression exprY_1) throws EvaluationException {

        double x_0 = exprX_0.evaluate();
        double x_1 = exprX_1.evaluate();
        double y_0 = exprY_0.evaluate();
        double y_1 = exprY_1.evaluate();

        this.graphs3D = new ArrayList<>();
        this.graphs3DAreDefined = new ArrayList<>();

        double[][][] singleGraph;
        boolean[][] singleGraphIsDefined;
        for (Expression expr : exprs) {

            singleGraph = new double[101][101][3];
            singleGraphIsDefined = new boolean[101][101];
            Variable.setValue(this.varAbsc, x_0);
            Variable.setValue(this.varOrd, y_0);
            for (int i = 0; i <= 100; i++) {
                for (int j = 0; j <= 100; j++) {
                    singleGraph[i][j][0] = x_0 + (x_1 - x_0) * i / 100;
                    singleGraph[i][j][1] = y_0 + (y_1 - y_0) * j / 100;
                    Variable.setValue(this.varAbsc, x_0 + (x_1 - x_0) * i / 100);
                    Variable.setValue(this.varOrd, y_0 + (y_1 - y_0) * j / 100);
                    try {
                        singleGraph[i][j][2] = expr.evaluate();
                        singleGraphIsDefined[i][j] = true;
                    } catch (EvaluationException e) {
                        singleGraph[i][j][2] = Double.NaN;
                        singleGraphIsDefined[i][j] = false;
                    }
                }
            }
            this.graphs3D.add(singleGraph);
            this.graphs3DAreDefined.add(singleGraphIsDefined);

        }

        // Zeichenbereich berechnen.
        computeScreenSizes();

    }

    /**
     * Gibt (eventuell) etwas gröbere Graphen zurück, damit sie gezeichnet
     * werden können.<br>
     * VORAUSSETZUNG: maxX, maxY und maxZ sind initialisiert.
     */
    private void convertGraphsToCoarserGraphs() {

        int numberOfIntervals = (int) (50 * this.zoomfactor);

        if (numberOfIntervals > 50) {
            numberOfIntervals = 50;
        }
        if (numberOfIntervals < 2) {
            numberOfIntervals = 2;
        }

        this.graphs3DForGraphic = new ArrayList<>();

        double[][][] graph3DForGraphic;
        boolean[][] coarserGraph3DIsDefined;
        this.graphs3DAreDefined.clear();
        for (double[][][] graph3D : this.graphs3D) {

            graph3DForGraphic = new double[numberOfIntervals][numberOfIntervals][3];
            coarserGraph3DIsDefined = new boolean[numberOfIntervals][numberOfIntervals];

            int currentIndexI, currentIndexJ;

            for (int i = 0; i < numberOfIntervals; i++) {

                if (graph3D.length <= numberOfIntervals) {
                    currentIndexI = graph3D.length - 1;
                } else {
                    currentIndexI = (int) (i * ((double) graph3D.length - 1) / (numberOfIntervals - 1));
                }

                for (int j = 0; j < numberOfIntervals; j++) {
                    if (graph3D[0].length <= numberOfIntervals) {
                        currentIndexJ = graph3D[0].length - 1;
                    } else {
                        currentIndexJ = (int) (j * ((double) graph3D[0].length - 1) / (numberOfIntervals - 1));
                    }
                    graph3DForGraphic[i][j][0] = graph3D[currentIndexI][currentIndexJ][0];
                    graph3DForGraphic[i][j][1] = graph3D[currentIndexI][currentIndexJ][1];
                    graph3DForGraphic[i][j][2] = graph3D[currentIndexI][currentIndexJ][2];
                    // Prüft, ob der Funktionswert this.graph3D[i][j][2] definiert ist.
                    coarserGraph3DIsDefined[i][j] = !(Double.isNaN(graph3D[currentIndexI][currentIndexJ][2]) || Double.isInfinite(graph3D[currentIndexI][currentIndexJ][2]));
                }

            }

            this.graphs3DForGraphic.add(graph3DForGraphic);
            this.graphs3DAreDefined.add(coarserGraph3DIsDefined);

        }

    }

    /**
     * Berechnet die Höhe des Schwerpunktes eines Tangentialplättchens, falls es
     * definiert ist.
     */
    private double computeAverageHeightOfInfinitesimalTangentSpace(int indexOfGraph3D, int i, int j) {

        double averageHeightOfTangentSpace = 0;
        int numberOfDefinedBorderPoints = 0;

        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i][j]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i + 1][j]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i + 1][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i][j + 1]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i][j + 1][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.graphs3DAreDefined.get(indexOfGraph3D)[i + 1][j + 1]) {
            averageHeightOfTangentSpace += this.graphs3DForGraphic.get(indexOfGraph3D)[i + 1][j + 1][2];
            numberOfDefinedBorderPoints++;
        }

        if (numberOfDefinedBorderPoints == 4) {
            return averageHeightOfTangentSpace / numberOfDefinedBorderPoints;
        }
        return Double.NaN;

    }

    /**
     * Zeichnet alle 3D-Graphen in kartesischen Koordinaten.
     */
    private void drawGraphsFromGraphs3DForGraphic(GraphicsContext g, double minExpr, double maxExpr) {

        int numberOfIntervalsAlongAbsc = 0;
        int numberOfIntervalsAlongOrd = 0;

        // Anzahl der Intervalle für das Zeichnen ermitteln.
        for (double[][][] graph3DForGraphic : this.graphs3DForGraphic) {
            if (graph3DForGraphic.length > 0) {
                numberOfIntervalsAlongAbsc = graph3DForGraphic.length - 1;
                numberOfIntervalsAlongOrd = graph3DForGraphic[0].length - 1;
                break;
            }
        }

        // Dann können keine Graphen gezeichnet werden.
        if (numberOfIntervalsAlongAbsc == 0 || numberOfIntervalsAlongOrd == 0) {
            return;
        }

        // Koordinaten der einzelnen Graphen in graphische Koordinaten umwandeln
        ArrayList<int[][][]> graphicalGraphs = new ArrayList<>();
        int[][][] graphicalGraph;

        for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
            graphicalGraph = new int[numberOfIntervalsAlongAbsc + 1][numberOfIntervalsAlongOrd + 1][2];
            for (int i = 0; i < numberOfIntervalsAlongAbsc + 1; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd + 1; j++) {
                    graphicalGraph[i][j] = convertToPixel(this.graphs3DForGraphic.get(k)[i][j][0], this.graphs3DForGraphic.get(k)[i][j][1],
                            this.graphs3DForGraphic.get(k)[i][j][2]);
                }
            }
            graphicalGraphs.add(graphicalGraph);
        }

        /*
         Jetzt wird der Graph, abhängig vom Winkel angle, gezeichnet. Es muss
         deshalb nach dem Winkel unterschieden werden, da der Graph stets "von
         hinten nach vorne" gezeichnet werden soll. Voraussetzung: 0 <= angle
         < 360.
         */
        HashMap<Integer, Double> heightsOfCentersOfInfinitesimalTangentSpaces = new HashMap<>();
        Double heightOfCentersOfInfinitesimalTangentSpace;
        ArrayList<Integer> indices;

        if (this.angle <= 90) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    TangentPolygon p = new TangentPolygon();
                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        p.setPoints(new double[]{graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][1]});
                        drawInfinitesimalTangentSpace(p, g, c);

                    }

                }

            }

        } else if (this.angle <= 180) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    TangentPolygon p = new TangentPolygon();
                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[i][j][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        p.setPoints(new double[]{graphicalGraphs.get(indices.get(k))[i][j][0], graphicalGraphs.get(indices.get(k))[i][j][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[i + 1][j][0], graphicalGraphs.get(indices.get(k))[i + 1][j][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[i + 1][j + 1][0], graphicalGraphs.get(indices.get(k))[i + 1][j + 1][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[i][j + 1][0], graphicalGraphs.get(indices.get(k))[i][j + 1][1]});
                        drawInfinitesimalTangentSpace(p, g, c);

                    }

                }

            }

        } else if (this.angle <= 270) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    TangentPolygon p = new TangentPolygon();
                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        p.setPoints(new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][1]});
                        drawInfinitesimalTangentSpace(p, g, c);

                    }

                }

            }

        } else {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    TangentPolygon p = new TangentPolygon();
                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        p.setPoints(new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][1]},
                                new double[]{graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][1]});
                        drawInfinitesimalTangentSpace(p, g, c);

                    }

                }

            }

        }

    }

    /**
     * Hilfsmethode. Gibt eine Liste von Indizes zurück, gemäß welcher die
     * Double-Werte im HashMap im Parameter aufsteigend sortiert sind.
     */
    private static ArrayList<Integer> getIndicesForAscendingSorting(HashMap<Integer, Double> centersOfInfinitesimalTangentSpaces) {

        HashMap<Integer, Double> copyOfCenters = new HashMap<>();
        ArrayList<Integer> sortedCenters = new ArrayList<>();

        // Manuell kopieren. clone() traue ich nicht!
        for (Integer i : centersOfInfinitesimalTangentSpaces.keySet()) {
            copyOfCenters.put(i, centersOfInfinitesimalTangentSpaces.get(i));
        }

        int indexWithLeastElement;
        double minimalValue;
        while (!copyOfCenters.isEmpty()) {

            indexWithLeastElement = -1;
            minimalValue = Double.POSITIVE_INFINITY;
            for (Integer i : copyOfCenters.keySet()) {
                if (indexWithLeastElement == -1) {
                    indexWithLeastElement = i;
                    minimalValue = copyOfCenters.get(i);
                } else if (copyOfCenters.get(i) < minimalValue) {
                    indexWithLeastElement = i;
                    minimalValue = copyOfCenters.get(i);
                }
            }

            sortedCenters.add(indexWithLeastElement);
            copyOfCenters.remove(indexWithLeastElement);

        }

        return sortedCenters;

    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Graphen in kartesischen Koordinaten.
     */
    private void drawGraph3D(GraphicsContext gc) {

        /*
         Falls kein echter Graph vorhanden ist, dann nur den weißen
         Hintergrund zeichnen und beenden. GRUND: Zu Beginn wird sofort
         repaint() aufgerufen, welches wiederum paint() aufruft. Dann sind
         aber varAbsc und varOrd nicht initialisiert und es gibt eine
         Exception. Dies wird hiermit verhindert.
         */
        if (this.graphs3D.isEmpty()) {
            return;
        }

        convertGraphsToCoarserGraphs();

        /*
         Ermittelt den kleinsten und den größten Funktionswert Notwendig, um
         das Farbspektrum im 3D-Graphen zu berechnen.
         */
        double minExpr = Double.NaN;
        double maxExpr = Double.NaN;

        // Zunächst wird geprüft, ob mindestens ein Graph IRGENDWO definiert ist
        boolean graphIsSomewhereDefined = false;
        for (int k = 0; k < this.graphs3DAreDefined.size(); k++) {
            for (int i = 0; i < this.graphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.graphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.graphs3DAreDefined.get(k)[i][j]) {
                        graphIsSomewhereDefined = true;
                        minExpr = this.graphs3DForGraphic.get(k)[i][j][2];
                        maxExpr = this.graphs3DForGraphic.get(k)[i][j][2];
                        break;
                    }
                }
                if (graphIsSomewhereDefined) {
                    break;
                }
            }
            if (graphIsSomewhereDefined) {
                break;
            }
        }

        // Falls kein Graph (irgendwo) definiert ist, Defaultwerte für Maße setzen.
        if (!graphIsSomewhereDefined) {
            minExpr = 0;
            maxExpr = 1;
        }

        for (int k = 0; k < this.graphs3DForGraphic.size(); k++) {
            for (int i = 0; i < this.graphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.graphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.graphs3DAreDefined.get(k)[i][j]) {
                        minExpr = Math.min(minExpr, this.graphs3DForGraphic.get(k)[i][j][2]);
                        maxExpr = Math.max(maxExpr, this.graphs3DForGraphic.get(k)[i][j][2]);
                    }
                }
            }
        }

        drawLevelsOnEast(gc, this.varAbsc, this.varOrd, null);
        drawLevelsOnSouth(gc, this.varAbsc, this.varOrd, null);
        drawLevelsOnWest(gc, this.varAbsc, this.varOrd, null);
        drawLevelsOnNorth(gc, this.varAbsc, this.varOrd, null);
        drawLevelsBottom(gc);
        drawGraphsFromGraphs3DForGraphic(gc, minExpr, maxExpr);

    }

    /**
     * Öffentliche Hauptmethode zum Zeichnen von 3D-Graphen in kartesischen
     * Koordinaten.
     */
    public void drawGraphs3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, Expression... exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(x_0, x_1, y_0, y_1);
        draw();
    }

    public void drawGraphs3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, ArrayList<Expression> exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(x_0, x_1, y_0, y_1);
        draw();
    }

    @Override
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        super.draw();
        drawGraph3D(gc);
    }

}
