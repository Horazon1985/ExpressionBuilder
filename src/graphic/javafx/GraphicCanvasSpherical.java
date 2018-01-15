package graphic.javafx;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphicCanvasSpherical extends AbstractGraphicCanvas3D {

    /**
     * Variablenname für 3D-Graphen: varPhi = Name des horizintalen Winkels,
     * gemessen von der positiven x-Achse aus.
     */
    private String varPhi;
    /**
     * Variablenname für 3D-Graphen: varPhi = Name des vertikalen Winkels,
     * gemessen von der positiven z-Achse aus.
     */
    private String varTau;
    private final List<Expression> exprs = new ArrayList<>();
    private List<double[][][]> sphericalGraphs3D = new ArrayList<>();
    /**
     * "Vergröberte Version" von sphericalGraphs3D (GRUND: beim Herauszoomen
     * dürfen die Plättchen am Graphen nicht so klein sein. Deshalb muss der
     * Graph etwas vergröbert werden).
     */
    private List<double[][][]> sphericalGraphs3DForGraphic = new ArrayList<>();
    private List<boolean[][]> sphericalGraphs3DAreDefined = new ArrayList<>();

    private final List<Color> colors = new ArrayList<>();

    private final static Color[] FIXED_COLORS = {Color.rgb(170, 170, 70), Color.rgb(170, 70, 170), Color.rgb(70, 170, 170)};

    private double minPhi, maxPhi, minTau, maxTau;

    public GraphicCanvasSpherical() {
        super();
    }

    public List<Expression> getExpressions() {
        return this.exprs;
    }

    public List<Color> getColors() {
        return this.colors;
    }

    public void setExpressions(List<Expression> exprs) {
        this.exprs.clear();
        this.exprs.addAll(exprs);
        setColors();
    }

    public void setExpressions(Expression... exprs) {
        this.exprs.clear();
        this.exprs.addAll(Arrays.asList(exprs));
        setColors();
    }

    private void setColors() {
        int numberOfColors = Math.max(this.exprs.size(), this.sphericalGraphs3D.size());
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

    public void setParameters(String varPhi, String varTau, double bigRadius, double heightProjection, double angle,
            double verticalAngle) {
        this.varPhi = varPhi;
        this.varTau = varTau;
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
     * VOLRAUSSETZUNG: exprs, varPhi und varTau sind bereits initialisiert.
     */
    private void computeScreenSizes() {
        super.computeScreenSizes(this.sphericalGraphs3D, true, true, true);
    }

    /**
     * Berechnet die Gitterpunkte für den 3D-Graphen aus dem Ausdruck expr.
     *
     * @throws EvaluationException
     */
    private void expressionToGraph(Expression exprR_0, Expression exprR_1, Expression exprPhi_0, Expression exprPhi_1) throws EvaluationException {

        this.minPhi = exprR_0.evaluate();
        this.maxPhi = exprR_1.evaluate();
        this.minTau = exprPhi_0.evaluate();
        this.maxTau = exprPhi_1.evaluate();

        this.sphericalGraphs3D = new ArrayList<>();
        this.sphericalGraphs3DAreDefined = new ArrayList<>();

        double currentPhi, currentTau, currentR;
        double[][][] singleGraph;
        boolean[][] singleGraphIsDefined;
        /*
         Entlang phi wird in 100 * (this.maxPhi - this.minPhi) / (2 * Math.PI) Intervalle 
         unterteilt, entlang tau in 100 * (this.maxTau - this.minTau) / (2 * Math.PI) 
         Intervalle.
         */
        int numberOfIntervalsAlongPhi = (int) (100 * (this.maxPhi - this.minPhi) / (2 * Math.PI));
        int numberOfIntervalsAlongTau = (int) (100 * (this.maxTau - this.minTau) / (2 * Math.PI));
        for (Expression expr : this.exprs) {

            singleGraph = new double[numberOfIntervalsAlongPhi + 1][numberOfIntervalsAlongTau + 1][3];
            singleGraphIsDefined = new boolean[numberOfIntervalsAlongPhi + 1][numberOfIntervalsAlongTau + 1];
            Variable.setValue(this.varPhi, this.minPhi);
            Variable.setValue(this.varTau, this.minTau);
            for (int i = 0; i <= numberOfIntervalsAlongPhi; i++) {
                for (int j = 0; j <= numberOfIntervalsAlongTau; j++) {
                    currentPhi = this.minPhi + (this.maxPhi - this.minPhi) * i / numberOfIntervalsAlongPhi;
                    currentTau = this.minTau + (this.maxTau - this.minTau) * j / numberOfIntervalsAlongTau;
                    Variable.setValue(this.varPhi, currentPhi);
                    Variable.setValue(this.varTau, currentTau);
                    try {
                        currentR = expr.evaluate();
                        singleGraph[i][j][0] = currentR * Math.sin(currentTau) * Math.cos(currentPhi);
                        singleGraph[i][j][1] = currentR * Math.sin(currentTau) * Math.sin(currentPhi);
                        singleGraph[i][j][2] = currentR * Math.cos(currentTau);
                        singleGraphIsDefined[i][j] = true;
                    } catch (EvaluationException e) {
                        singleGraph[i][j][0] = Double.NaN;
                        singleGraph[i][j][1] = Double.NaN;
                        singleGraph[i][j][2] = Double.NaN;
                        singleGraphIsDefined[i][j] = false;
                    }
                }
            }
            this.sphericalGraphs3D.add(singleGraph);
            this.sphericalGraphs3DAreDefined.add(singleGraphIsDefined);

        }

        // Zeichenbereich berechnen.
        computeScreenSizes();

    }

    /**
     * Gibt (eventuell) etwas gröbere Graphen zurück, damit sie gezeichnet
     * werden können.<br>
     * VORAUSSETZUNG: minPhi, maxPhi, minTau und maxTau sind initialisiert.
     */
    private void convertGraphsToCoarserGraphs() {

        int numberOfIntervalsAlongPhi = (int) (50 * this.zoomfactor * (this.maxPhi - this.minPhi) / (2 * Math.PI));
        // Zur Erinnerung: Einschränkung ist maxPhi - minPhi <= 10 * 2 * pi.
        if (numberOfIntervalsAlongPhi > this.sphericalGraphs3D.get(0).length - 1) {
            numberOfIntervalsAlongPhi = this.sphericalGraphs3D.get(0).length - 1;
        }
        if (numberOfIntervalsAlongPhi < 2){
            numberOfIntervalsAlongPhi = 2;
        }
        int numberOfIntervalsAlongTau = (int) (50 * this.zoomfactor * (this.maxPhi - this.minPhi) / (2 * Math.PI));
        // Zur Erinnerung: Einschränkung ist maxTau - minTau <= 10 * 2 * pi.
        if (numberOfIntervalsAlongTau > this.sphericalGraphs3D.get(0)[0].length - 1) {
            numberOfIntervalsAlongTau = this.sphericalGraphs3D.get(0)[0].length - 1;
        }
        if (numberOfIntervalsAlongTau < 2){
            numberOfIntervalsAlongTau = 2;
        }

        this.sphericalGraphs3DForGraphic = new ArrayList<>();

        double[][][] graph3DForGraphic;
        boolean[][] coarserGraph3DIsDefined;
        this.sphericalGraphs3DAreDefined.clear();
        for (double[][][] graph3D : this.sphericalGraphs3D) {

            graph3DForGraphic = new double[numberOfIntervalsAlongPhi][numberOfIntervalsAlongTau][3];
            coarserGraph3DIsDefined = new boolean[numberOfIntervalsAlongPhi][numberOfIntervalsAlongTau];

            int currentIndexI, currentIndexJ;

            for (int i = 0; i < numberOfIntervalsAlongPhi; i++) {

                if (graph3D.length <= numberOfIntervalsAlongPhi) {
                    currentIndexI = graph3D.length - 1;
                } else {
                    currentIndexI = (int) (i * ((double) graph3D.length - 1) / (numberOfIntervalsAlongPhi - 1));
                }

                for (int j = 0; j < numberOfIntervalsAlongTau; j++) {
                    if (graph3D[0].length <= numberOfIntervalsAlongTau) {
                        currentIndexJ = graph3D[0].length - 1;
                    } else {
                        currentIndexJ = (int) (j * ((double) graph3D[0].length - 1) / (numberOfIntervalsAlongTau - 1));
                    }
                    graph3DForGraphic[i][j][0] = graph3D[currentIndexI][currentIndexJ][0];
                    graph3DForGraphic[i][j][1] = graph3D[currentIndexI][currentIndexJ][1];
                    graph3DForGraphic[i][j][2] = graph3D[currentIndexI][currentIndexJ][2];
                    // Prüft, ob der Funktionswert this.graph3D[i][j][2] definiert ist.
                    coarserGraph3DIsDefined[i][j] = !(Double.isNaN(graph3D[currentIndexI][currentIndexJ][2]) || Double.isInfinite(graph3D[currentIndexI][currentIndexJ][2]));
                }

            }

            this.sphericalGraphs3DForGraphic.add(graph3DForGraphic);
            this.sphericalGraphs3DAreDefined.add(coarserGraph3DIsDefined);

        }

    }

    /**
     * Zeichnet ein (tangentiales) viereckiges Plättchen eines 3D-Graphen.
     */
    private void drawInfinitesimalTangentSpace(int x_1, int y_1, int x_2, int y_2,
            int x_3, int y_3, int x_4, int y_4, GraphicsContext gc) {

        switch (backgroundColorMode) {
            case BRIGHT:
                gc.setStroke(gridColorGridOnlyBright);
                break;
            case DARK:
                gc.setStroke(gridColorGridOnlyDark);
                break;
        }

        gc.strokeLine(x_1, y_1, x_2, y_2);
        gc.strokeLine(x_2, y_2, x_3, y_3);
        gc.strokeLine(x_3, y_3, x_4, y_4);
        gc.strokeLine(x_4, y_4, x_1, y_1);

    }

    /**
     * Berechnet die Höhe des Schwerpunktes eines Tangentialplättchens, falls es
     * definiert ist.
     */
    private double computeAverageHeightOfInfinitesimalTangentSpace(int indexOfGraph3D, int i, int j) {

        double averageHeightOfTangentSpace = 0;
        int numberOfDefinedBorderPoints = 0;

        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i][j]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i + 1][j]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i + 1][j][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i][j + 1]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i][j + 1][2];
            numberOfDefinedBorderPoints++;
        }
        if (this.sphericalGraphs3DAreDefined.get(indexOfGraph3D)[i + 1][j + 1]) {
            averageHeightOfTangentSpace += this.sphericalGraphs3DForGraphic.get(indexOfGraph3D)[i + 1][j + 1][2];
            numberOfDefinedBorderPoints++;
        }

        if (numberOfDefinedBorderPoints == 4) {
            return averageHeightOfTangentSpace / numberOfDefinedBorderPoints;
        }
        return Double.NaN;

    }

    /**
     * Zeichnet alle 3D-Graphen in Kugelkoordinaten.
     */
    private void drawGraphsFromCylindricalGraphs3DForGraphic(GraphicsContext g) {

        int numberOfIntervalsAlongAbsc = 0;
        int numberOfIntervalsAlongOrd = 0;

        // Anzahl der Intervalle für das Zeichnen ermitteln.
        for (double[][][] cylindricalgraph3DForGraphic : this.sphericalGraphs3DForGraphic) {
            if (cylindricalgraph3DForGraphic.length > 0) {
                numberOfIntervalsAlongAbsc = cylindricalgraph3DForGraphic.length - 1;
                numberOfIntervalsAlongOrd = cylindricalgraph3DForGraphic[0].length - 1;
                break;
            }
        }

        // Dann können keine Graphen gezeichnet werden.
        if (numberOfIntervalsAlongAbsc == 0 || numberOfIntervalsAlongOrd == 0) {
            return;
        }

        //Koordinaten der einzelnen Graphen in graphische Koordinaten umwandeln
        ArrayList<int[][][]> graphicalGraphs = new ArrayList<>();
        int[][][] graphicalGraph;

        for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
            graphicalGraph = new int[numberOfIntervalsAlongAbsc + 1][numberOfIntervalsAlongOrd + 1][2];
            for (int i = 0; i < numberOfIntervalsAlongAbsc + 1; i++) {
                for (int j = 0; j < numberOfIntervalsAlongOrd + 1; j++) {
                    graphicalGraph[i][j] = convertToPixel(this.sphericalGraphs3DForGraphic.get(k)[i][j][0], this.sphericalGraphs3DForGraphic.get(k)[i][j][1],
                            this.sphericalGraphs3DForGraphic.get(k)[i][j][2]);
                }
            }
            graphicalGraphs.add(graphicalGraph);
        }

        /*
         Jetzt wird der Graph, abhängig vom Winkel angle, gezeichnet. Es muss
         deshalb nach dem Winkel unterschieden werden, da der Graph stets "von
         hinten nach vorne" gezeichnet werden soll. Voraussetzung: 0 <= angle
         < 360
         */
        HashMap<Integer, Double> heightsOfCentersOfInfinitesimalTangentSpaces = new HashMap<>();
        Double heightOfCentersOfInfinitesimalTangentSpace;
        ArrayList<Integer> indices;

        if (this.angle <= 90) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i + 1][numberOfIntervalsAlongOrd - j][1],
                                graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[i][numberOfIntervalsAlongOrd - j][1],
                                g);

                    }

                }

            }

        } else if (this.angle <= 180) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, i, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[i][j][0], graphicalGraphs.get(indices.get(k))[i][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j][0], graphicalGraphs.get(indices.get(k))[i + 1][j][1],
                                graphicalGraphs.get(indices.get(k))[i + 1][j + 1][0], graphicalGraphs.get(indices.get(k))[i + 1][j + 1][1],
                                graphicalGraphs.get(indices.get(k))[i][j + 1][0], graphicalGraphs.get(indices.get(k))[i][j + 1][1],
                                g);

                    }

                }

            }

        } else if (this.angle <= 270) {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, j);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][j + 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][j + 1][1],
                                g);

                    }

                }

            }

        } else {

            for (int i = 0; i < numberOfIntervalsAlongAbsc; i++) {

                for (int j = 0; j < numberOfIntervalsAlongOrd; j++) {

                    // Alle Schwerpunkte der einzelnen Tangentialplättchen berechnen.
                    heightsOfCentersOfInfinitesimalTangentSpaces.clear();
                    for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
                        heightOfCentersOfInfinitesimalTangentSpace = computeAverageHeightOfInfinitesimalTangentSpace(k, numberOfIntervalsAlongAbsc - i - 1, numberOfIntervalsAlongOrd - j - 1);
                        if (!heightOfCentersOfInfinitesimalTangentSpace.equals(Double.NaN)) {
                            heightsOfCentersOfInfinitesimalTangentSpaces.put(k, heightOfCentersOfInfinitesimalTangentSpace);
                        }
                    }
                    // Indizes für eine aufsteigende Ordnung berechnen.
                    indices = getIndicesForAscendingSorting(heightsOfCentersOfInfinitesimalTangentSpaces);

                    for (int k = 0; k < indices.size(); k++) {

                        // Für die vorkommenden Indizes ist der entsprechende Graph automatisch in allen 4 Randpunkten definiert.
                        drawInfinitesimalTangentSpace(graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j - 1][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i][numberOfIntervalsAlongOrd - j][1],
                                graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][0], graphicalGraphs.get(indices.get(k))[numberOfIntervalsAlongAbsc - i - 1][numberOfIntervalsAlongOrd - j][1],
                                g);

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
     * Hauptmethode zum Zeichnen von 3D-Graphen in Kugelkoordinaten.
     */
    private void drawSphericalGraph3D(GraphicsContext gc) {

        /*
         Falls kein echter Graph vorhanden ist, dann nur den weißen
         Hintergrund zeichnen und beenden. GRUND: Zu Beginn wird sofort
         repaint() aufgerufen, welches wiederum paint() aufruft. Dann sind
         aber varAbsc und varOrd nicht initialisiert und es gibt eine
         Exception. Dies wird hiermit verhindert.
         */
        if (this.sphericalGraphs3D.isEmpty()) {
            return;
        }
        
        convertGraphsToCoarserGraphs();

        /*
         Ermittelt den kleinsten und den größten Funktionswert Notwendig, um
         das Farbspektrum im 3D-Graphen zu berechnen!
         */
        double minExpr = Double.NaN;
        double maxExpr = Double.NaN;

        // Zunächst wird geprüft, ob mindestens ein Graph IRGENDWO definiert ist
        boolean graphIsSomewhereDefined = false;
        for (int k = 0; k < this.sphericalGraphs3DAreDefined.size(); k++) {
            for (int i = 0; i < this.sphericalGraphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.sphericalGraphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.sphericalGraphs3DAreDefined.get(k)[i][j]) {
                        graphIsSomewhereDefined = true;
                        minExpr = this.sphericalGraphs3DForGraphic.get(k)[i][j][2];
                        maxExpr = this.sphericalGraphs3DForGraphic.get(k)[i][j][2];
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

        // Falls kein Graph definiert (irgendwo) ist, Defaultwerte für Maße setzen.
        if (!graphIsSomewhereDefined) {
            minExpr = 0;
            maxExpr = 1;
        }

        for (int k = 0; k < this.sphericalGraphs3DForGraphic.size(); k++) {
            for (int i = 0; i < this.sphericalGraphs3DForGraphic.get(k).length; i++) {
                for (int j = 0; j < this.sphericalGraphs3DForGraphic.get(k)[0].length; j++) {
                    if (this.sphericalGraphs3DAreDefined.get(k)[i][j]) {
                        minExpr = Math.min(minExpr, this.sphericalGraphs3DForGraphic.get(k)[i][j][2]);
                        maxExpr = Math.max(maxExpr, this.sphericalGraphs3DForGraphic.get(k)[i][j][2]);
                    }
                }
            }
        }

        drawLevelsOnEast(gc, null, null, null);
        drawLevelsOnSouth(gc, null, null, null);
        drawLevelsOnWest(gc, null, null, null);
        drawLevelsOnNorth(gc, null, null, null);
        drawLevelsBottom(gc);
        drawGraphsFromCylindricalGraphs3DForGraphic(gc);

    }

    /**
     * Öffentliche Hauptmethode zum Zeichnen von 3D-Graphen in Kugelkoordinaten.
     */
    public void drawSphericalGraphs3D(Expression r_0, Expression r_1, Expression phi_0, Expression phi_1, Expression... exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(r_0, r_1, phi_0, phi_1);
        draw();
    }

    public void drawSphericalGraphs3D(Expression r_0, Expression r_1, Expression phi_0, Expression phi_1, List<Expression> exprs) throws EvaluationException {
        setExpressions(exprs);
        expressionToGraph(r_0, r_1, phi_0, phi_1);
        draw();
    }

    @Override
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        super.draw();
        drawSphericalGraph3D(gc);
    }

}
