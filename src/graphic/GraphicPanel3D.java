package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GraphicPanel3D extends AbstractGraphicPanel3D {

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

    private final static Color[] fixedColors = {new Color(170, 170, 70), new Color(170, 70, 170), new Color(70, 170, 170)};

    public GraphicPanel3D() {
        super();
    }

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public ArrayList<Color> getColors() {
        return this.colors;
    }

    public void setExpressions(ArrayList<Expression> exprs) {
        this.exprs.clear();
        this.exprs.addAll(exprs);
        setColors();
    }

    public void setExpressions(Expression... exprs) {
        this.exprs.clear();
        this.exprs.addAll(Arrays.asList(exprs));
        setColors();
    }

    public void addExpression(Expression expr) {
        this.exprs.add(expr);
        setColors();
    }

    private void setColors() {
        int numberOfColors = Math.max(this.exprs.size(), this.graphs3D.size());
        this.colors.clear();
        for (int i = this.colors.size(); i < numberOfColors; i++) {
            if (i < fixedColors.length) {
                this.colors.add(fixedColors[i]);
            } else {
                this.colors.add(generateColor());
            }
        }
    }

    private Color generateColor() {
        return new Color((int) (70 + 100 * Math.random()), (int) (100 * Math.random()), (int) (70 + 100 * Math.random()));
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
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes() {

        if (this.graphs3D.isEmpty()) {

            this.minX = -1;
            this.minY = -1;
            this.minZ = -1;
            this.maxX = 1;
            this.maxY = 1;
            this.maxZ = 1;

        } else {

            for (int k = 0; k < this.graphs3D.size(); k++) {

                if (this.graphs3D.get(0).length == 0) {
                    continue;
                }
                this.minX = this.graphs3D.get(0)[0][0][0];
                this.minY = this.graphs3D.get(0)[0][0][1];
//                this.maxX = this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0];
//                this.maxY = this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1];
                this.maxX = Math.max(Math.abs(this.graphs3D.get(0)[0][0][0]), Math.abs(this.graphs3D.get(0)[this.graphs3D.get(0).length - 1][0][0]));
                this.maxY = Math.max(Math.abs(this.graphs3D.get(0)[0][0][1]), Math.abs(this.graphs3D.get(0)[0][this.graphs3D.get(0)[0].length - 1][1]));
                this.minZ = 0;
                this.maxZ = 0;

                for (int i = 0; i <= this.graphs3D.get(k).length - 1; i++) {
                    for (int j = 0; j <= this.graphs3D.get(k)[0].length - 1; j++) {
                        if (graphs3DAreDefined.get(k)[i][j]) {
                            this.minZ = Math.min(this.minZ, this.graphs3D.get(k)[i][j][2]);
//                            this.maxZ = Math.max(this.maxZ, this.graphs3D.get(k)[i][j][2]);
                            this.maxZ = Math.max(this.maxZ, Math.abs(this.graphs3D.get(k)[i][j][2]));
                        }
                    }
                }
                // 30 % Rand auf der z-Achse lassen!
                this.maxZ = this.maxZ * 1.3;

            }

        }

        if (this.minX == this.maxX) {
            this.minX = this.minX - 1;
            this.maxX = this.maxX + 1;
        }
        if (this.minY == this.maxY) {
            this.minY = this.minY - 1;
            this.maxY = this.maxY + 1;
        }
        if (this.minZ == this.maxZ) {
            this.minZ = this.minZ - 1;
            this.maxZ = this.maxZ + 1;
        }

        this.minXOrigin = this.minX;
        this.minYOrigin = this.minY;
        this.minZOrigin = this.minZ;
        this.maxXOrigin = this.maxX;
        this.maxYOrigin = this.maxY;
        this.maxZOrigin = this.maxZ;

        this.axeCenterX = (this.minX + this.maxX) / 2;
        this.axeCenterY = (this.minY + this.maxY) / 2;
        this.axeCenterZ = (this.minZ + this.maxZ) / 2;

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
    private ArrayList<double[][][]> convertGraphsToCoarserGraphs() {

        int numberOfIntervalsAlongAbsc = this.graphs3D.get(0).length;
        int numberOfIntervalsAlongOrd = this.graphs3D.get(0)[0].length;

        if (numberOfIntervalsAlongAbsc > 1.35 * 20 * (this.graphs3D.get(0)[numberOfIntervalsAlongAbsc - 1][0][0] - this.graphs3D.get(0)[0][0][0]) / this.maxX) {
            numberOfIntervalsAlongAbsc = (int) (1.35 * 20 * (this.graphs3D.get(0)[numberOfIntervalsAlongAbsc - 1][0][0] - this.graphs3D.get(0)[0][0][0]) / this.maxX);
            if (numberOfIntervalsAlongAbsc < 1) {
                numberOfIntervalsAlongAbsc = 1;
            }
        }
        if (numberOfIntervalsAlongOrd > 1.35 * 20 * (this.graphs3D.get(0)[0][numberOfIntervalsAlongOrd - 1][1] - this.graphs3D.get(0)[0][0][1]) / this.maxY) {
            numberOfIntervalsAlongOrd = (int) (1.35 * 20 * (this.graphs3D.get(0)[0][numberOfIntervalsAlongOrd - 1][1] - this.graphs3D.get(0)[0][0][1]) / this.maxY);
            if (numberOfIntervalsAlongOrd < 1) {
                numberOfIntervalsAlongOrd = 1;
            }
        }

        ArrayList<double[][][]> graphsForGraphic = new ArrayList<>();

        double[][][] graph3DForGraphic;
        boolean[][] coarserGraph3DIsDefined;
        this.graphs3DAreDefined.clear();
        for (double[][][] graph3D : this.graphs3D) {

            graph3DForGraphic = new double[numberOfIntervalsAlongAbsc][numberOfIntervalsAlongOrd][3];
            coarserGraph3DIsDefined = new boolean[numberOfIntervalsAlongAbsc][numberOfIntervalsAlongOrd];

            int currentIndexI, currentIndexJ;

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                if (graph3D.length <= numberOfIntervalsAlongAbsc) {
                    currentIndexI = i;
                } else {
                    currentIndexI = (int) (i * ((double) graph3D.length - 1) / (numberOfIntervalsAlongAbsc - 1));
                }

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {
                    if (graph3D[0].length <= numberOfIntervalsAlongOrd) {
                        currentIndexJ = j;
                    } else {
                        currentIndexJ = (int) (j * ((double) graph3D[0].length - 1) / (numberOfIntervalsAlongOrd - 1));
                    }
                    graph3DForGraphic[i][j][0] = graph3D[currentIndexI][currentIndexJ][0];
                    graph3DForGraphic[i][j][1] = graph3D[currentIndexI][currentIndexJ][1];
                    graph3DForGraphic[i][j][2] = graph3D[currentIndexI][currentIndexJ][2];
                    // Prüft, ob der Funktionswert this.graph3D[i][j][2] definiert ist.
                    coarserGraph3DIsDefined[i][j] = !(Double.isNaN(graph3D[currentIndexI][currentIndexJ][2]) || Double.isInfinite(graph3D[currentIndexI][currentIndexJ][2]));
                }

            }

            graphsForGraphic.add(graph3DForGraphic);
            this.graphs3DAreDefined.add(coarserGraph3DIsDefined);

        }

        return graphsForGraphic;

    }

    /**
     * Zeichnet ein (tangentiales) viereckiges Plättchen eines 3D-Graphen.
     */
    private void drawInfinitesimalTangentSpace(int x_1, int y_1, int x_2, int y_2,
            int x_3, int y_3, int x_4, int y_4, Graphics g, Color c) {

        GeneralPath tangent = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
        tangent.moveTo(x_1, y_1);
        tangent.lineTo(x_2, y_2);
        tangent.lineTo(x_3, y_3);
        tangent.lineTo(x_4, y_4);
        tangent.closePath();
        Graphics2D g2 = (Graphics2D) g;

        if (this.presentationMode.equals(PresentationMode.WHOLE_GRAPH)) {
            g2.setPaint(c);
            g2.fill(tangent);
        }

        switch (this.backgroundColorMode) {
            case BRIGHT:
                switch (presentationMode) {
                    case WHOLE_GRAPH:
                        g2.setPaint(gridColorWholeGraphBright);
                        break;
                    case GRID_ONLY:
                        g2.setPaint(gridColorGridOnlyBright);
                        break;
                }
                break;
            case DARK:
                switch (presentationMode) {
                    case WHOLE_GRAPH:
                        g2.setPaint(gridColorWholeGraphDark);
                        break;
                    case GRID_ONLY:
                        g2.setPaint(gridColorGridOnlyDark);
                        break;
                }
                break;
        }

        g2.draw(tangent);

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
    private void drawGraphsFromGraphs3DForGraphic(Graphics g, double minExpr, double maxExpr) {

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

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][1],
                                graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][1],
                                g, c);

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

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[i][j][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][j][0], graphicalGraphs.get(indices.get(k))[i][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j][0], graphicalGraphs.get(indices.get(k))[i + 1][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j + 1][0], graphicalGraphs.get(indices.get(k))[i + 1][j + 1][1],
                                graphicalGraphs.get(indices.get(k))[i][j + 1][0], graphicalGraphs.get(indices.get(k))[i][j + 1][1],
                                g, c);

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

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][1],
                                g, c);

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

                    for (int k = 0; k < indices.size(); k++) {

                        Color c = computeColor(this.colors.get(indices.get(k)), minExpr, maxExpr, this.graphs3DForGraphic.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][2]);
                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][1],
                                g, c);

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
    private void drawGraph3D(Graphics g) {

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
        this.graphs3DForGraphic = convertGraphsToCoarserGraphs();

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

        drawLevelsOnEast(g, this.varAbsc, this.varOrd, null);
        drawLevelsOnSouth(g, this.varAbsc, this.varOrd, null);
        drawLevelsOnWest(g, this.varAbsc, this.varOrd, null);
        drawLevelsOnNorth(g, this.varAbsc, this.varOrd, null);
        drawLevelsBottom(g);
        drawGraphsFromGraphs3DForGraphic(g, minExpr, maxExpr);

    }

    /**
     * Öffentliche Hauptmethode zum Zeichnen von 3D-Graphen in kartesischen
     * Koordinaten.
     */
    public void drawGraphs3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, Expression... exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(x_0, x_1, y_0, y_1);
        drawGraphs3D();
    }

    public void drawGraphs3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, ArrayList<Expression> exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(x_0, x_1, y_0, y_1);
        drawGraphs3D();
    }

    private void drawGraphs3D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGraph3D(g);
    }

}
