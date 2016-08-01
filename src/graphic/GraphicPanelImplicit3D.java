package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import lang.translator.Translator;

public class GraphicPanelImplicit3D extends JPanel implements Runnable, Exportable {

    /**
     * Boolsche Variable, die angibt, ob der Graph gerade rotiert oder nicht.
     */
    private boolean isRotating;
    /**
     * Variablennamen für 3D-Graphen: Absc = Abszisse, Ord = Ordinate, Appl = Applikate.
     */
    private String varAbsc, varOrd, varAppl;
    private ArrayList<Expression> exprs = new ArrayList<>();

    private MarchingCube[][][] implicitGraph3D;
    private MarchingCubeForComputation[][][] cubesForComputation;
    private Color color = new Color(170, 170, 70);

    private double zoomfactor;
    private double maxX, maxY, maxZ;
    private double minX, minY, minZ;
    private int expX, expY, expZ;

    //Radien für die Grundellipse
    private double bigRadius, smallRadius;
    private double height, heightProjection;
    /*
     Neigungswinkel des Graphen: angle = horizontaler Winkel (er wird
     inkrementiert, wenn der Graph im Uhrzeigersinn rotiert) verticalAngle =
     Winkel, unter dem man die dritte Achse sieht. 0 = man schaut seitlich auf
     den Graphen, 90 = man schaut von oben auf den Graphen.
     */
    private double angle, verticalAngle;
    /*
     Die boolsche Variable isAngleMeant ist true <-> der aktuelle Winkel
     (welcher durch das Auslösen eines MouseEvent verändert wird) ist angle.
     Ansonsten verticalAngle.
     */
    private boolean isAngleMeant = true;
    private Point lastMousePosition;

    private PresentationMode presentationMode = PresentationMode.WHOLE_GRAPH;
    private BackgroundColorMode backgroundColorMode = BackgroundColorMode.BRIGHT;

    private static final Color backgroundColorBright = Color.white;
    private static final Color backgroundColorDark = Color.black;
    private static final Color gridColorWholeGraphBright = Color.black;
    private static final Color gridColorWholeGraphDark = Color.green;
    private static final Color gridColorGridOnlyBright = Color.black;
    private static final Color gridColorGridOnlyDark = Color.green;

    public enum BackgroundColorMode {

        BRIGHT, DARK;

    }

    public enum PresentationMode {

        WHOLE_GRAPH, GRID_ONLY;

    }

    public static class MarchingCube {

        ArrayList<Boolean[]> innerVertices = new ArrayList<>();

        public ArrayList<Boolean[]> getInnerVertices() {
            return this.innerVertices;
        }

        public void addInnerVertex(Boolean[] vertex) {
            this.innerVertices.add(vertex);
        }
        
        @Override
        public String toString(){
            String cube = "[";
            for (int i = 0; i < this.innerVertices.size(); i++){
                cube += "(";
                if (this.innerVertices.get(i)[0] == false){
                    cube += "0,";
                } else {
                    cube += "1,";
                }
                if (this.innerVertices.get(i)[1] == false){
                    cube += "0,";
                } else {
                    cube += "1,";
                }
                if (this.innerVertices.get(i)[2] == false){
                    cube += "0)";
                } else {
                    cube += "1)";
                }
                if (i < this.innerVertices.size()){
                    cube = cube + ", ";
                }
            }
            return cube + "]";
        }

        public void switchPoints() {
            ArrayList<Boolean[]> switchedVertices = new ArrayList<>();
            Boolean[] vertex = new Boolean[3];
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 2; k++) {
                        vertex[0] = i == 0;
                        vertex[1] = j == 0;
                        vertex[2] = k == 0;
                        if (!this.innerVertices.contains(vertex)) {
                            switchedVertices.add(vertex);
                        }
                    }
                }
            }
            this.innerVertices = switchedVertices;
        }

        private static Boolean[] getNeighboringVertex(Boolean[] vertex, int axe) {
            if (axe == 0) {
                return new Boolean[]{!vertex[0], vertex[1], !vertex[2]};
            }
            if (axe == 1) {
                return new Boolean[]{vertex[0], !vertex[1], !vertex[2]};
            }
            return new Boolean[]{vertex[0], vertex[1], !vertex[2]};
        }

        private boolean isInnerPoint(Boolean[] vertex) {
            return this.innerVertices.contains(vertex);
        }

        private boolean isInnerPointAndIsolatedVertex(Boolean[] vertex) {
            return isInnerPoint(vertex) && !isInnerPoint(getNeighboringVertex(vertex, 0))
                    && !isInnerPoint(getNeighboringVertex(vertex, 1)) && !isInnerPoint(getNeighboringVertex(vertex, 2));
        }

        private boolean hasNeighbor(Boolean[] vertex) {

            Boolean[] neighborVertex = new Boolean[3];
            // Prüfung entlang der x-Achse.
            neighborVertex[0] = !vertex[0];
            neighborVertex[1] = vertex[1];
            neighborVertex[2] = vertex[2];
            if (this.innerVertices.contains(neighborVertex)) {
                return true;
            }
            // Prüfung entlang der y-Achse.
            neighborVertex[0] = vertex[0];
            neighborVertex[1] = !vertex[1];
            neighborVertex[2] = vertex[2];
            if (this.innerVertices.contains(neighborVertex)) {
                return true;
            }
            // Prüfung entlang der z-Achse.
            neighborVertex[0] = vertex[0];
            neighborVertex[1] = vertex[1];
            neighborVertex[2] = !vertex[2];
            return this.innerVertices.contains(neighborVertex);

        }

        private static boolean areNeighbors(Boolean[] firstVertex, Boolean[] secondVertex) {
            return Arrays.equals(getNeighboringVertex(firstVertex, 0), secondVertex)
                    || Arrays.equals(getNeighboringVertex(firstVertex, 1), secondVertex)
                    || Arrays.equals(getNeighboringVertex(firstVertex, 2), secondVertex);
        }

        public boolean containsNoInnerPoints() {
            return this.innerVertices.isEmpty();
        }

        public boolean consistsOfIsolatedInnerPoints() {
            for (Boolean[] vertex : this.innerVertices) {
                if (!isInnerPointAndIsolatedVertex(vertex)) {
                    return false;
                }
            }
            return true;
        }

        public boolean consistsOfOneEdge() {
            return this.innerVertices.size() == 2 && areNeighbors(this.innerVertices.get(0), this.innerVertices.get(1));
        }

        public boolean consistsOfIsolatedEdges() {

            if (this.innerVertices.size() != 4) {
                return false;
            }
            Boolean[] vertex_1 = this.innerVertices.get(0);
            Boolean[] vertex_2 = this.innerVertices.get(1);
            Boolean[] vertex_3 = this.innerVertices.get(2);
            Boolean[] vertex_4 = this.innerVertices.get(3);

            if (areNeighbors(vertex_1, vertex_2)) {
                return areNeighbors(vertex_3, vertex_4)
                        && !areNeighbors(vertex_1, vertex_3)
                        && !areNeighbors(vertex_2, vertex_3)
                        && !areNeighbors(vertex_1, vertex_4)
                        && !areNeighbors(vertex_2, vertex_4);
            }
            if (areNeighbors(vertex_1, vertex_3)) {
                return areNeighbors(vertex_2, vertex_4)
                        && !areNeighbors(vertex_1, vertex_2)
                        && !areNeighbors(vertex_3, vertex_2)
                        && !areNeighbors(vertex_1, vertex_4)
                        && !areNeighbors(vertex_3, vertex_4);
            }
            if (areNeighbors(vertex_1, vertex_4)) {
                return areNeighbors(vertex_2, vertex_3)
                        && !areNeighbors(vertex_1, vertex_2)
                        && !areNeighbors(vertex_4, vertex_2)
                        && !areNeighbors(vertex_1, vertex_3)
                        && !areNeighbors(vertex_4, vertex_3);
            }

            return false;

        }

        public boolean consistsOfIsolatedTriangle() {

            if (this.innerVertices.size() != 3) {
                return false;
            }
            Boolean[] vertex_1 = this.innerVertices.get(0);
            Boolean[] vertex_2 = this.innerVertices.get(1);
            Boolean[] vertex_3 = this.innerVertices.get(2);

            // Prüfung, ob die x-, y- oder z-Koordinate bei allen Ecken konstant ist.
            return (boolean) vertex_1[0] == (boolean) vertex_2[0] == (boolean) vertex_3[0]
                    || (boolean) vertex_1[1] == (boolean) vertex_2[1] == (boolean) vertex_3[1]
                    || (boolean) vertex_1[2] == (boolean) vertex_2[2] == (boolean) vertex_3[2];

        }

        public boolean consistsOfIsolatedEdgeAndIsolatedVertex() {

            if (this.innerVertices.size() != 3) {
                return false;
            }
            Boolean[] vertex_1 = this.innerVertices.get(0);
            Boolean[] vertex_2 = this.innerVertices.get(1);
            Boolean[] vertex_3 = this.innerVertices.get(2);

            // Prüfung, ob die x-, y- oder z-Koordinate bei allen Ecken konstant ist.
            return isInnerPointAndIsolatedVertex(vertex_1) && areNeighbors(vertex_2, vertex_3)
                    || isInnerPointAndIsolatedVertex(vertex_2) && areNeighbors(vertex_1, vertex_3)
                    || isInnerPointAndIsolatedVertex(vertex_3) && areNeighbors(vertex_1, vertex_2);

        }

        public boolean consistsOfIsolatedPlane() {

            if (this.innerVertices.size() != 4) {
                return false;
            }
            Boolean[] vertex_1 = this.innerVertices.get(0);
            Boolean[] vertex_2 = this.innerVertices.get(1);
            Boolean[] vertex_3 = this.innerVertices.get(2);
            Boolean[] vertex_4 = this.innerVertices.get(3);

            // Prüfung, ob die x-, y- oder z-Koordinate bei allen Ecken konstant ist.
            return (boolean) vertex_1[0] == (boolean) vertex_2[0] == (boolean) vertex_3[0] == (boolean) vertex_4[0]
                    || (boolean) vertex_1[1] == (boolean) vertex_2[1] == (boolean) vertex_3[1] == (boolean) vertex_4[1]
                    || (boolean) vertex_1[2] == (boolean) vertex_2[2] == (boolean) vertex_3[2] == (boolean) vertex_4[2];

        }

        public boolean consistsOfTetraeder() {

            if (this.innerVertices.size() != 4) {
                return false;
            }
            Boolean[] vertex_1 = this.innerVertices.get(0);
            Boolean[] vertex_2 = this.innerVertices.get(1);
            Boolean[] vertex_3 = this.innerVertices.get(2);
            Boolean[] vertex_4 = this.innerVertices.get(3);

            return areNeighbors(vertex_1, vertex_2) && areNeighbors(vertex_1, vertex_3) && areNeighbors(vertex_1, vertex_4)
                    || areNeighbors(vertex_2, vertex_1) && areNeighbors(vertex_2, vertex_3) && areNeighbors(vertex_2, vertex_4)
                    || areNeighbors(vertex_3, vertex_1) && areNeighbors(vertex_3, vertex_1) && areNeighbors(vertex_3, vertex_4)
                    || areNeighbors(vertex_4, vertex_1) && areNeighbors(vertex_4, vertex_2) && areNeighbors(vertex_4, vertex_3);

        }

        public boolean consistsOfAFourChain() {

            if (this.innerVertices.size() != 4) {
                return false;
            }
            Boolean[] vertex_1 = this.innerVertices.get(0);
            Boolean[] vertex_2 = this.innerVertices.get(1);
            Boolean[] vertex_3 = this.innerVertices.get(2);
            Boolean[] vertex_4 = this.innerVertices.get(3);

            return hasNeighbor(vertex_1) && hasNeighbor(vertex_2) && hasNeighbor(vertex_3) && hasNeighbor(vertex_4)
                    && !consistsOfIsolatedPlane() && !consistsOfTetraeder();

        }

    }

    public enum PolygonVertexCoordinate {

        ZERO, HALF, ONE;
    }

    public class Polygon implements Comparable {

        double lengthOfAmbientCube;
        ArrayList<Double[]> vertices = new ArrayList<>();

        public Polygon(Double[]... vertexCoordinates) {
            this.vertices.addAll(Arrays.asList(vertexCoordinates));
        }

        @Override
        public String toString(){
            String cube = "[";
            for (int i = 0; i < this.vertices.size(); i++){
                cube += "(" + this.vertices.get(i)[0] + ", " + this.vertices.get(i)[1] + ", " + this.vertices.get(i)[2] + ")";
                if (i < this.vertices.size()){
                    cube = cube + ", ";
                }
            }
            return cube + "]";
        }
        
        public ArrayList<Double[]> getVertices() {
            return this.vertices;
        }

        public Double[] getVertex(int i) {
            return this.vertices.get(i);
        }

        public void setLengthOfAmbientCube(double length) {
            this.lengthOfAmbientCube = length;
        }

        public void add(Double[] vertex) {
            this.vertices.add(vertex);
        }

        @Override
        public int compareTo(Object o) {

            if (!(o instanceof Polygon)) {
                // Dieser Fall tritt nicht auf.
                return 0;
            }

            Polygon p = (Polygon) o;

            if (angle <= 90) {

                if (verticalAngle <= 45) {
                    if (getCenterCoordinateY() >= p.getCenterCoordinateY()) {
                        return -1;
                    }
                    return 1;
                } else {
                    double centerY = getCenterCoordinateY();
                    double centerYOfP = p.getCenterCoordinateY();
                    if (Math.abs(centerY - centerYOfP) <= this.lengthOfAmbientCube / 10) {
                        if (getCenterCoordinateZ() >= p.getCenterCoordinateZ()) {
                            return 1;
                        }
                        return -1;
                    }
                    if (getCenterCoordinateY() >= p.getCenterCoordinateY()) {
                        return -1;
                    }
                    return 1;
                }

            } else if (angle <= 180) {

                if (verticalAngle <= 45) {
                    if (getCenterCoordinateX() <= p.getCenterCoordinateX()) {
                        return -1;
                    }
                    return 1;
                } else {
                    double centerX = getCenterCoordinateX();
                    double centerXOfP = p.getCenterCoordinateX();
                    if (Math.abs(centerX - centerXOfP) <= this.lengthOfAmbientCube / 10) {
                        if (getCenterCoordinateZ() >= p.getCenterCoordinateZ()) {
                            return 1;
                        }
                        return -1;
                    }
                    if (getCenterCoordinateX() <= p.getCenterCoordinateX()) {
                        return -1;
                    }
                    return 1;
                }

            } else if (angle <= 270) {

                if (verticalAngle <= 45) {
                    if (getCenterCoordinateY() <= p.getCenterCoordinateY()) {
                        return -1;
                    }
                    return 1;
                } else {
                    double centerY = getCenterCoordinateY();
                    double centerYOfP = p.getCenterCoordinateY();
                    if (Math.abs(centerY - centerYOfP) <= this.lengthOfAmbientCube / 10) {
                        if (getCenterCoordinateZ() >= p.getCenterCoordinateZ()) {
                            return 1;
                        }
                        return -1;
                    }
                    if (getCenterCoordinateY() <= p.getCenterCoordinateY()) {
                        return -1;
                    }
                    return 1;
                }

            } else if (verticalAngle <= 45) {
                if (getCenterCoordinateX() >= p.getCenterCoordinateX()) {
                    return -1;
                }
                return 1;
            } else {
                double centerX = getCenterCoordinateX();
                double centerXOfP = p.getCenterCoordinateX();
                if (Math.abs(centerX - centerXOfP) <= this.lengthOfAmbientCube / 10) {
                    if (getCenterCoordinateZ() >= p.getCenterCoordinateZ()) {
                        return 1;
                    }
                    return -1;
                }
                if (getCenterCoordinateX() >= p.getCenterCoordinateX()) {
                    return -1;
                }
                return 1;
            }

        }

        private double getCenterCoordinateX() {
            double centerX = 0;
            for (Double[] vertex : this.vertices) {
                centerX += vertex[0];
            }
            centerX = centerX / this.vertices.size();
            return centerX;
        }

        private double getCenterCoordinateY() {
            double centerY = 0;
            for (Double[] vertex : this.vertices) {
                centerY += vertex[1];
            }
            centerY = centerY / this.vertices.size();
            return centerY;
        }

        private double getCenterCoordinateZ() {
            double centerZ = 0;
            for (Double[] vertex : this.vertices) {
                centerZ += vertex[2];
            }
            centerZ = centerZ / this.vertices.size();
            return centerZ;
        }

    }

    public static class MarchingCubeWithPolygons {

        private final ArrayList<ArrayList<PolygonVertexCoordinate[]>> polygonVertices = new ArrayList<>();

        public ArrayList<ArrayList<PolygonVertexCoordinate[]>> getPolygonVertices() {
            return this.polygonVertices;
        }

        private static PolygonVertexCoordinate convertToPolygonVertexCoordinate(boolean b) {
            if (b) {
                return PolygonVertexCoordinate.ONE;
            }
            return PolygonVertexCoordinate.ZERO;
        }

        public void computePolygons(MarchingCube cube) {

            if (cube.consistsOfIsolatedInnerPoints()) {
                computePolygonsInCaseOfIsolatedInnerPoints(cube);
            } else if (cube.consistsOfOneEdge()) {
                computePolygonsInCaseOfOneEdge(cube);
            } else if (cube.consistsOfIsolatedEdges()) {
                computePolygonsInCaseOfIsolatedEdges(cube);
            } else if (cube.consistsOfIsolatedTriangle()) {
                computePolygonsInCaseOfIsolatedTrangle(cube);
            } else if (cube.consistsOfIsolatedEdgeAndIsolatedVertex()) {
                computePolygonsInCaseOfIsolatedEdgeAndIsolatedVertex(cube);
            } else if (cube.consistsOfIsolatedPlane()) {
                computePolygonsInCaseOfIsolatedPlane(cube);
            } else if (cube.consistsOfTetraeder()) {
                computePolygonsInCaseOfTetraeder(cube);
            } else if (cube.consistsOfAFourChain()) {
                computePolygonsInCaseOfAFourChain(cube);
            }

        }

        private void computePolygonsInCaseOfIsolatedInnerPoints(MarchingCube cube) {
            for (Boolean[] vertex : cube.getInnerVertices()) {
                addPolygonOfAnIsolatedVertex(vertex);
            }
        }

        private void addPolygonOfAnIsolatedVertex(Boolean[] vertex) {
            ArrayList<PolygonVertexCoordinate[]> polygon = new ArrayList<>();
            polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                convertToPolygonVertexCoordinate(vertex[1]), convertToPolygonVertexCoordinate(vertex[2])});
            polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex[0]),
                PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex[2])});
            polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex[0]),
                convertToPolygonVertexCoordinate(vertex[1]), PolygonVertexCoordinate.HALF});
            this.polygonVertices.add(polygon);
        }

        private void addPolygonOfAnEdge(Boolean[] vertex_1, Boolean[] vertex_2) {

            if (!MarchingCube.areNeighbors(vertex_1, vertex_2)) {
                return;
            }

            ArrayList<PolygonVertexCoordinate[]> polygon = new ArrayList<>();

            if ((boolean) vertex_1[0] != (boolean) vertex_2[0]) {
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]),
                    PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]),
                    PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]),
                    PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]),
                    PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF});
            } else if ((boolean) vertex_1[1] != (boolean) vertex_2[1]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO,
                    convertToPolygonVertexCoordinate(vertex_1[1]), PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    convertToPolygonVertexCoordinate(vertex_1[1]), PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    convertToPolygonVertexCoordinate(vertex_2[1]), PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO,
                    convertToPolygonVertexCoordinate(vertex_2[1]), PolygonVertexCoordinate.HALF});
            } else if ((boolean) vertex_1[2] != (boolean) vertex_2[2]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO,
                    PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[2])});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    PolygonVertexCoordinate.ZERO, convertToPolygonVertexCoordinate(vertex_1[2])});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    PolygonVertexCoordinate.ZERO, convertToPolygonVertexCoordinate(vertex_2[2])});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO,
                    PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[2])});
            }

            this.polygonVertices.add(polygon);

        }

        private void computePolygonsInCaseOfOneEdge(MarchingCube cube) {
            ArrayList<Boolean[]> innerVertices = cube.getInnerVertices();
            addPolygonOfAnEdge(innerVertices.get(0), innerVertices.get(1));
        }

        private void computePolygonsInCaseOfIsolatedEdges(MarchingCube cube) {
            ArrayList<Boolean[]> innerVertices = cube.getInnerVertices();
            if (MarchingCube.areNeighbors(innerVertices.get(0), innerVertices.get(1))) {
                addPolygonOfAnEdge(innerVertices.get(0), innerVertices.get(1));
                addPolygonOfAnEdge(innerVertices.get(2), innerVertices.get(3));
            } else if (MarchingCube.areNeighbors(innerVertices.get(0), innerVertices.get(2))) {
                addPolygonOfAnEdge(innerVertices.get(0), innerVertices.get(2));
                addPolygonOfAnEdge(innerVertices.get(1), innerVertices.get(3));
            } else if (MarchingCube.areNeighbors(innerVertices.get(0), innerVertices.get(3))) {
                addPolygonOfAnEdge(innerVertices.get(0), innerVertices.get(3));
                addPolygonOfAnEdge(innerVertices.get(1), innerVertices.get(2));
            }
        }

        private void computePolygonsInCaseOfIsolatedTrangle(MarchingCube cube) {

        }

        private void computePolygonsInCaseOfIsolatedEdgeAndIsolatedVertex(MarchingCube cube) {
            ArrayList<Boolean[]> innerVertices = cube.getInnerVertices();
            if (cube.isInnerPointAndIsolatedVertex(innerVertices.get(0))) {
                addPolygonOfAnIsolatedVertex(innerVertices.get(0));
                addPolygonOfAnEdge(innerVertices.get(1), innerVertices.get(2));
            } else if (cube.isInnerPointAndIsolatedVertex(innerVertices.get(1))) {
                addPolygonOfAnIsolatedVertex(innerVertices.get(1));
                addPolygonOfAnEdge(innerVertices.get(0), innerVertices.get(2));
            } else if (cube.isInnerPointAndIsolatedVertex(innerVertices.get(2))) {
                addPolygonOfAnIsolatedVertex(innerVertices.get(2));
                addPolygonOfAnEdge(innerVertices.get(0), innerVertices.get(1));
            }
        }

        private void computePolygonsInCaseOfIsolatedPlane(MarchingCube cube) {

            ArrayList<Boolean[]> innerVertices = cube.getInnerVertices();
            ArrayList<PolygonVertexCoordinate[]> polygon = new ArrayList<>();

            if ((boolean) innerVertices.get(0)[0] == (boolean) innerVertices.get(1)[0] == (boolean) innerVertices.get(2)[0] == (boolean) innerVertices.get(3)[0]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ZERO});
            } else if ((boolean) innerVertices.get(0)[1] == (boolean) innerVertices.get(1)[1] == (boolean) innerVertices.get(2)[1] == (boolean) innerVertices.get(3)[1]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO});
            } else if ((boolean) innerVertices.get(0)[2] == (boolean) innerVertices.get(1)[2] == (boolean) innerVertices.get(2)[2] == (boolean) innerVertices.get(3)[2]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF});
            }

            this.polygonVertices.add(polygon);

        }

        private void computePolygonsInCaseOfTetraeder(MarchingCube cube) {

        }

        private void computePolygonsInCaseOfAFourChain(MarchingCube cube) {

        }

    }

    public class MarchingCubeForComputation {

        private final ArrayList<Polygon> polygons = new ArrayList<>();

        public ArrayList<Polygon> getPolygons() {
            return this.polygons;
        }
        
        @Override
        public String toString(){
            String cube = "[";
            for (int i = 0; i < this.polygons.size(); i++){
                cube += this.polygons.get(i).toString();
                if (i < this.polygons.size()){
                    cube = cube + ", ";
                }
            }
            return cube + "]";
        }

        public void computePolygons(MarchingCubeWithPolygons cube, double x_0, double x_1, double y_0, double y_1, double z_0, double z_1) {

            ArrayList<ArrayList<PolygonVertexCoordinate[]>> polygonVertices = cube.getPolygonVertices();

            Polygon polygonWithCoordinates;
            double x, y, z;

            for (ArrayList<PolygonVertexCoordinate[]> polygon : polygonVertices) {
                polygonWithCoordinates = new Polygon();
                for (PolygonVertexCoordinate[] vertices : polygon) {
                    switch (vertices[0]) {
                        case ZERO:
                            x = x_0;
                            break;
                        case HALF:
                            x = (x_0 + x_1) / 2;
                            break;
                        default:
                            x = x_1;
                    }
                    switch (vertices[1]) {
                        case ZERO:
                            y = y_0;
                            break;
                        case HALF:
                            y = (y_0 + y_1) / 2;
                            break;
                        default:
                            y = y_1;
                    }
                    switch (vertices[2]) {
                        case ZERO:
                            z = z_0;
                            break;
                        case HALF:
                            z = (z_0 + z_1) / 2;
                            break;
                        default:
                            z = z_1;
                    }
                    polygonWithCoordinates.add(new Double[]{x, y, z});
                    polygonWithCoordinates.setLengthOfAmbientCube(x_1 - x_0);
                }
                this.polygons.add(polygonWithCoordinates);
            }

        }

    }

    public GraphicPanelImplicit3D() {
        isRotating = false;
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isAngleMeant = true;
                    lastMousePosition = e.getPoint();
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isAngleMeant = false;
                    lastMousePosition = e.getPoint();
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
                if (isAngleMeant) {
                    angle += (lastMousePosition.x - e.getPoint().x) * 0.5;

                    if (angle >= 360) {
                        angle = angle - 360;
                    }
                    if (angle < 0) {
                        angle = angle + 360;
                    }

                    lastMousePosition = e.getPoint();
                    repaint();
                } else {
                    verticalAngle -= (lastMousePosition.y - e.getPoint().y) * 0.3;

                    if (verticalAngle >= 90) {
                        verticalAngle = 90;
                    }
                    if (verticalAngle < 1) {
                        verticalAngle = 1;
                    }

                    smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
                    height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);

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
                    maxZ *= Math.pow(1.1, e.getWheelRotation());
                    minX *= Math.pow(1.1, e.getWheelRotation());
                    minY *= Math.pow(1.1, e.getWheelRotation());
                    minZ *= Math.pow(1.1, e.getWheelRotation());
                    zoomfactor *= Math.pow(1.1, e.getWheelRotation());
                    repaint();
                }
            }
        });

    }

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public Color getColor() {
        return this.color;
    }

    public boolean getIsRotating() {
        return this.isRotating;
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    public BackgroundColorMode getBackgroundColorMode() {
        return this.backgroundColorMode;
    }

    public void setBackgroundColorMode(BackgroundColorMode backgroundColorMode) {
        this.backgroundColorMode = backgroundColorMode;
    }

    public PresentationMode getPresentationMode() {
        return this.presentationMode;
    }

    public void setPresentationMode(PresentationMode presentationMode) {
        this.presentationMode = presentationMode;
    }

    public void setExpressions(Expression... exprs) {
        this.exprs = new ArrayList<>();
        this.exprs.addAll(Arrays.asList(exprs));
    }

    public void setIsRotating(boolean isRotating) {
        this.isRotating = isRotating;
    }

    public void setParameters(String varAbsc, String varOrd, String varAppl, double bigRadius, double heightProjection, double angle,
            double verticalAngle) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
        this.varAppl = varAppl;
        this.heightProjection = heightProjection;
        this.bigRadius = bigRadius;
        this.smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
        this.height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);
        this.angle = angle;
        this.verticalAngle = verticalAngle;
        this.zoomfactor = 1;
    }

    private MarchingCubeWithPolygons[][][] convertToMarchingCubesWithPolygones(MarchingCube[][][] cubes) {

        MarchingCubeWithPolygons[][][] cubesWithPolygons = new MarchingCubeWithPolygons[cubes.length][cubes[0].length][cubes[0][0].length];

        for (int i = 0; i < cubes.length; i++) {
            for (int j = 0; j < cubes[0].length; j++) {
                for (int k = 0; k < cubes[0][0].length; k++) {
                    cubesWithPolygons[i][j][k] = new MarchingCubeWithPolygons();
                    cubesWithPolygons[i][j][k].computePolygons(cubes[i][j][k]);
                }
            }
        }

        return cubesWithPolygons;

    }

    private MarchingCubeForComputation[][][] convertToMarchingCubesForComputation(MarchingCubeWithPolygons[][][] cubes,
            double x_0, double x_1, double y_0, double y_1, double z_0, double z_1) {

        MarchingCubeForComputation[][][] cubesWithCoordinates = new MarchingCubeForComputation[cubes.length][cubes[0].length][cubes[0][0].length];

        int numberOfIntervalsX = cubes.length - 1;
        int numberOfIntervalsY = cubes[0].length - 1;
        int numberOfIntervalsZ = cubes[0][0].length - 1;

        for (int i = 0; i < cubes.length; i++) {
            for (int j = 0; j < cubes[0].length; j++) {
                for (int k = 0; k < cubes[0][0].length; k++) {
                    cubesWithCoordinates[i][j][k] = new MarchingCubeForComputation();
                    cubesWithCoordinates[i][j][k].computePolygons(cubes[i][j][k], x_0 + i * (x_1 - x_0) / (numberOfIntervalsX), x_0 + (i + 1) * (x_1 - x_0) / (numberOfIntervalsX),
                            y_0 + j * (y_1 - y_0) / (numberOfIntervalsY), y_0 + (j + 1) * (y_1 - y_0) / (numberOfIntervalsY),
                            z_0 + k * (z_1 - z_0) / (numberOfIntervalsZ), z_0 + (k + 1) * (z_1 - z_0) / (numberOfIntervalsZ));
                }
            }
        }

        return cubesWithCoordinates;

    }

    /**
     * Voraussetzung: expr und var sind bereits gesetzt.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd,
            Expression exprApplStart, Expression exprApplEnd) throws EvaluationException {
        this.maxX = Math.max(Math.abs(exprAbscStart.evaluate()), Math.abs(exprAbscEnd.evaluate()));
        this.maxY = Math.max(Math.abs(exprOrdStart.evaluate()), Math.abs(exprOrdEnd.evaluate()));
        this.maxZ = Math.max(Math.abs(exprApplStart.evaluate()), Math.abs(exprApplEnd.evaluate()));
        this.minX = -this.maxX;
        this.minY = -this.maxY;
        this.minZ = -this.maxZ;
    }

    /**
     * Voraussetzung: graph3D ist bereits initialisiert (bzw. mit
     * Funktionswerten gefüllt), maxX, maxY, maxZ sind bekannt/initialisiert.
     */
    private void computeExpXExpYExpZ() {

        this.expX = 0;
        this.expY = 0;
        this.expZ = 0;

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

        if (this.maxZ >= 1) {
            while (this.maxZ / Math.pow(10, this.expZ) >= 1) {
                this.expZ++;
            }
            this.expZ--;
        } else {
            while (this.maxZ / Math.pow(10, this.expZ) < 1) {
                this.expZ--;
            }
        }

    }

    //Berechnet aus dem Winkel "angle" den Winkel, welcher in der graphischen Darstellung auftaucht
    private double getGraphicalAngle(double bigRadius, double smallRadius, double angle) {
        //Vorausgesetzt: 0 <= real_angle < 360
        if ((angle == 0) || (angle == 90) || (angle == 180) || (angle == 270)) {
            return angle;
        } else if (angle < 90) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI;
        } else if (angle < 180) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        } else if (angle < 270) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        } else {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 360;
        }
    }

    /**
     * Berechnet aus Punktkoordinaten (x, y, z) Koordinaten (x', y') für die
     * graphische Darstellung.
     */
    private int[] convertToPixel(double x, double y, double z, double bigRadius, double smallRadius, double height, double angle) {

        double angleAbsc = getGraphicalAngle(bigRadius, smallRadius, angle);
        double angleOrd;
        if (angle < 90) {
            angleOrd = getGraphicalAngle(bigRadius, smallRadius, angle + 270);
        } else {
            angleOrd = getGraphicalAngle(bigRadius, smallRadius, angle - 90);
        }

        // pixel bildet die Pixelkoordinaten für die Graphische Darstellung von (x, y, z)
        int[] pixel = new int[2];

        // Berechnung von pixels[0]
        double x_1, x_2;

        if (angleAbsc == 0) {
            x_1 = bigRadius * x / this.maxX;
            x_2 = 0;
        } else if (angleAbsc == 90) {
            x_1 = 0;
            x_2 = bigRadius * y / this.maxY;
        } else if (angleAbsc == 180) {
            x_1 = -bigRadius * x / this.maxX;
            x_2 = 0;
        } else if (angleAbsc == 270) {
            x_1 = 0;
            x_2 = -bigRadius * y / this.maxY;
        } else if (angleAbsc < 90) {
            x_1 = (x / this.maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = (y / this.maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        } else if (angleAbsc < 180) {
            x_1 = -(x / this.maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = (y / this.maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        } else if (angleAbsc < 270) {
            x_1 = -(x / this.maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = -(y / this.maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        } else {
            x_1 = (x / this.maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleAbsc * Math.PI / 180) / smallRadius, 2));
            x_2 = -(y / this.maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angleOrd * Math.PI / 180) / smallRadius, 2));
        }

        pixel[0] = (int) (250 + x_1 + x_2);

        // Berechnung von pixel[1]
        double y_1, y_2, y_3;

        if (angleAbsc == 0) {
            y_1 = 0;
            y_2 = -smallRadius * y / this.maxY;
        } else if (angleAbsc == 90) {
            y_1 = smallRadius * x / this.maxX;
            y_2 = 0;
        } else if (angleAbsc == 180) {
            y_1 = 0;
            y_2 = smallRadius * y / this.maxY;
        } else if (angleAbsc == 270) {
            y_1 = -smallRadius * x / this.maxX;
            y_2 = 0;
        } else {
            y_1 = x_1 * Math.tan(angleAbsc * Math.PI / 180);
            y_2 = x_2 * Math.tan(angleOrd * Math.PI / 180);
        }

        //maximaler Funktionswert (also max_z) soll h Pixel betragen
        y_3 = -height * z / this.maxZ;
        pixel[1] = (int) (250 + y_1 + y_2 + y_3);

        return pixel;

    }

    /**
     * Zeichnet ein (tangentiales) rechteckiges Plättchen des 3D-Graphen
     */
    private void drawInfinitesimalTangentPolygone(Graphics g, Color c, ArrayList<Point> points) {

        GeneralPath tangent = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());
        tangent.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            tangent.lineTo(points.get(i).x, points.get(i).y);
        }
        tangent.closePath();
        Graphics2D g2 = (Graphics2D) g;

        if (presentationMode.equals(PresentationMode.WHOLE_GRAPH)) {
            g2.setPaint(c);
            g2.fill(tangent);
        }

        switch (backgroundColorMode) {
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
     * Die folgenden vier Prozeduren zeichnen Niveaulinien am Rand des Graphen.
     * Voraussetzung: maxX, maxY, maxZ, bigRadius, smallRadius, height, angle
     * sind bekannt/initialisiert.
     */
    private void drawLevelsOnEast(Graphics g, double angle) {

        if ((angle >= 0) && (angle <= 180)) {
            return;
        }

        double maxValueAbsc = this.maxX;
        double maxValueOrd = this.maxY;
        double maxValueAppl = this.maxZ;

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = maxValueAbsc;
        border[0][1] = maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = maxValueAbsc;
        border[1][1] = -maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = maxValueAbsc;
        border[2][1] = -maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = maxValueAbsc;
        border[3][1] = maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 270) {
            g.drawString(this.varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varOrd) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= maxValueAppl) {
            lineLevel[0][0] = maxValueAbsc;
            lineLevel[0][1] = maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = -maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 270) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (maxValueOrd / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= maxValueOrd) {
            lineLevel[0][0] = maxValueAbsc;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 270) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnWest(Graphics g, double angle) {

        if ((angle >= 180) && (angle <= 360)) {
            return;
        }

        double maxValueAbsc = this.maxX;
        double maxValueOrd = this.maxY;
        double maxValueAppl = this.maxZ;

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -maxValueAbsc;
        border[0][1] = -maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = -maxValueAbsc;
        border[1][1] = maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = -maxValueAbsc;
        border[2][1] = maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = -maxValueAbsc;
        border[3][1] = -maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 90) {
            g.drawString(this.varOrd, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varOrd, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varOrd) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= maxValueAppl) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = -maxValueAbsc;
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 90) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (maxValueOrd / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= maxValueOrd) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = -maxValueAbsc;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnSouth(Graphics g, double angle) {

        if ((angle <= 90) || (angle >= 270)) {
            return;
        }

        double maxValueAbsc = this.maxX;
        double maxValueOrd = this.maxY;
        double maxValueAppl = this.maxZ;

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = maxValueAbsc;
        border[0][1] = -maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = -maxValueAbsc;
        border[1][1] = -maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = -maxValueAbsc;
        border[2][1] = -maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = maxValueAbsc;
        border[3][1] = -maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 180) {
            g.drawString(this.varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varAbsc) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= maxValueAppl) {
            lineLevel[0][0] = maxValueAbsc;
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = -maxValueAbsc;
            lineLevel[1][1] = -maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 180) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        //Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (maxValueAbsc / Math.pow(10, this.expX));
        i = -bound;

        while (i * Math.pow(10, this.expX) <= maxValueAbsc) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = -maxValueOrd;
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 180) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnNorth(Graphics g, double angle) {

        if ((angle >= 90) && (angle <= 270)) {
            return;
        }

        double maxValueAbsc = this.maxX;
        double maxValueOrd = this.maxY;
        double maxValueAppl = this.maxZ;

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -maxValueAbsc;
        border[0][1] = maxValueOrd;
        border[0][2] = maxValueAppl;
        border[1][0] = maxValueAbsc;
        border[1][1] = maxValueOrd;
        border[1][2] = maxValueAppl;
        border[2][0] = maxValueAbsc;
        border[2][1] = maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = -maxValueAbsc;
        border[3][1] = maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle <= 90) {
            g.drawString(this.varAbsc, borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString(this.varAbsc, borderPixels[0][0] - g.getFontMetrics().stringWidth(this.varAbsc) - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (maxValueAppl / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= maxValueAppl) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = maxValueOrd;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle < 90) && ((i + 1) * Math.pow(10, this.expZ) <= maxValueAppl)) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (maxValueAbsc / Math.pow(10, this.expX));
        i = -bound;

        while (i * Math.pow(10, this.expX) <= maxValueAbsc) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = maxValueOrd;
            lineLevel[0][2] = maxValueAppl;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle <= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsBottom(Graphics g, double angle) {

        double maxValueAbsc = this.maxX;
        double maxValueOrd = this.maxY;
        double maxValueAppl = this.maxZ;

        //Zunächst den Rahmen auf dem Boden zeichnen
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = maxValueAbsc;
        border[0][1] = maxValueOrd;
        border[0][2] = -maxValueAppl;
        border[1][0] = -maxValueAbsc;
        border[1][1] = maxValueOrd;
        border[1][2] = -maxValueAppl;
        border[2][0] = -maxValueAbsc;
        border[2][1] = -maxValueOrd;
        border[2][2] = -maxValueAppl;
        border[3][0] = maxValueAbsc;
        border[3][1] = -maxValueOrd;
        border[3][2] = -maxValueAppl;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], this.bigRadius, this.smallRadius, this.height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        //horizontale x-Niveaulinien zeichnen
        int bound = (int) (maxValueAbsc / Math.pow(10, this.expX));
        int i = -bound;

        while (i * Math.pow(10, this.expX) <= maxValueAbsc) {
            lineLevel[0][0] = i * Math.pow(10, this.expX);
            lineLevel[0][1] = -maxValueOrd;
            lineLevel[0][2] = -maxValueAppl;
            lineLevel[1][0] = i * Math.pow(10, this.expX);
            lineLevel[1][1] = maxValueOrd;
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        //horizontale y-Niveaulinien zeichnen
        bound = (int) (maxValueOrd / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= maxValueOrd) {
            lineLevel[0][0] = -maxValueAbsc;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = -maxValueAppl;
            lineLevel[1][0] = maxValueAbsc;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -maxValueAppl;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], this.bigRadius, this.smallRadius, this.height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], this.bigRadius, this.smallRadius, this.height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

    }

    private Color computeColor(Color groundColor, double minExpr, double maxExpr, double height) {

        Color c;
        int red, green, blue;

        int r = groundColor.getRed();
        int g = groundColor.getGreen();
        int b = groundColor.getBlue();

        if (minExpr == maxExpr) {
            red = r;
            green = g;
            blue = b;
        } else {
            red = r - (int) (60 * Math.sin(this.angle / 180 * Math.PI));
            green = g + (int) ((255 - g) * (height - minExpr) / (maxExpr - minExpr));
            blue = b + (int) (60 * Math.sin(this.angle / 180 * Math.PI));
        }

        c = new Color(red, green, blue);
        return c;

    }

    private void drawMarchingCubesForComputation(Graphics g) {

        if (angle <= 90) {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        drawSingleMarchingCube(g, i, this.cubesForComputation[0].length - 1 - j, k);
                    }
                }
            }

        } else if (angle <= 180) {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        drawSingleMarchingCube(g, i, j, k);
                    }
                }
            }
            
        } else if (angle <= 270) {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        drawSingleMarchingCube(g, this.cubesForComputation.length - 1 - i, j, k);
                    }
                }
            }
            
        } else {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        drawSingleMarchingCube(g, this.cubesForComputation.length - 1 - i, this.cubesForComputation[0].length - 1 - j, k);
                    }
                }
            }
            
        }

    }

    private void sortPolygonsForDraw(MarchingCubeForComputation cube) {
        Collections.sort(cube.getPolygons());
    }

    private void drawSingleMarchingCube(Graphics g, int i, int j, int k) {

        int[] pixel;
        ArrayList<Point> polygon;

        MarchingCubeForComputation cube = this.cubesForComputation[i][j][k];
        sortPolygonsForDraw(cube);
        
        for (Polygon p : cube.getPolygons()){
            polygon = new ArrayList<>();
            for (Double[] polygonVerexCoordinates : p.getVertices()){
                pixel = convertToPixel(polygonVerexCoordinates[0], polygonVerexCoordinates[1], polygonVerexCoordinates[2],
                        this.bigRadius, this.smallRadius, this.height, this.angle);
                polygon.add(new Point(pixel[0], pixel[1]));
            }
            drawInfinitesimalTangentPolygone(g, this.color, polygon);
        }
        
    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Graphen.
     */
    private void drawImplicitGraph3D(Graphics g, double angle) {

        //Zunächst Hintergrund zeichnen.
        switch (backgroundColorMode) {
            case BRIGHT:
                g.setColor(Color.white);
                break;
            case DARK:
                g.setColor(Color.black);
                break;
        }
        g.fillRect(0, 0, 500, 500);

        /*
         Falls kein echter Graph vorhanden ist, dann nur den weißen
         Hintergrund zeichnen und beenden. GRUND: Zu Beginn wird sofort
         repaint() aufgerufen, welches wiederum paint() aufruft. Dann sind
         aber varAbsc und varOrd nicht initialisiert und es gibt eine
         Exception. Dies wird hiermit verhindert.
         */
        if (this.implicitGraph3D.length == 0) {
            return;
        }
        computeExpXExpYExpZ();

        drawLevelsOnEast(g, angle);
        drawLevelsOnSouth(g, angle);
        drawLevelsOnWest(g, angle);
        drawLevelsOnNorth(g, angle);
        drawLevelsBottom(g, angle);

    }

    public void drawImplicitGraph3D(MarchingCube[][][] implicitGraph3D, Expression x_0, Expression x_1, Expression y_0, Expression y_1, Expression z_0, Expression z_1) throws EvaluationException {
        this.implicitGraph3D = implicitGraph3D;
        computeScreenSizes(x_0, x_1, y_0, y_1, z_0, z_1);
        MarchingCubeWithPolygons[][][] cubesWithPolygons = convertToMarchingCubesWithPolygones(implicitGraph3D);
        this.cubesForComputation = convertToMarchingCubesForComputation(cubesWithPolygons,
                x_0.evaluate(), x_1.evaluate(), y_0.evaluate(), y_1.evaluate(), z_0.evaluate(), z_1.evaluate());
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawImplicitGraph3D(g, this.angle);
    }

    @Override
    public void run() {
        while (this.isRotating) {

            this.angle = this.angle + 1;
            if (this.angle >= 360) {
                this.angle = this.angle - 360;
            }
            if (this.angle < 0) {
                this.angle = this.angle + 360;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            repaint();

        }
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
