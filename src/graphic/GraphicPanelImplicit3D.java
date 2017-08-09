package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import static graphic.AbstractGraphicPanel3D.presentationMode;
import graphic.util.MarchingCube;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GraphicPanelImplicit3D extends AbstractGraphicPanel3D {

    /**
     * Variablennamen für 3D-Graphen: Absc = Abszisse, Ord = Ordinate, Appl =
     * Applikate.
     */
    private String varAbsc, varOrd, varAppl;
    private ArrayList<Expression> exprs = new ArrayList<>();

    private MarchingCube[][][] implicitGraph3D;
    private MarchingCubeForComputation[][][] cubesForComputation;
    private final Color color = new Color(170, 100, 70);

    private double minX, minY, minZ;

    public GraphicPanelImplicit3D(){
        super();
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
        public String toString() {
            String cube = "[";
            for (int i = 0; i < this.vertices.size(); i++) {
                cube += "(" + this.vertices.get(i)[0] + ", " + this.vertices.get(i)[1] + ", " + this.vertices.get(i)[2] + ")";
                if (i < this.vertices.size() - 1) {
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
            } else if (cube.consistsOfAnIsolatedTriangleAndAnIsolatedVertex()) {
                computePolygonsInCaseOfIsolatedTrangleAndIsolatedVertex(cube);
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

        private void addPolygonOfAnIsolatedTriangle(Boolean[] vertex_1, Boolean[] vertex_2, Boolean[] vertex_3) {

            ArrayList<PolygonVertexCoordinate[]> polygonTriangle = new ArrayList<>();
            ArrayList<PolygonVertexCoordinate[]> polygonRectangle = new ArrayList<>();
            Boolean[] fourthPointInSquare;

            if (vertex_1[0].booleanValue() == vertex_2[0].booleanValue() && vertex_1[0].booleanValue() == vertex_3[0].booleanValue()) {
                polygonTriangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[1]), convertToPolygonVertexCoordinate(vertex_1[2])});
                polygonTriangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[1]), convertToPolygonVertexCoordinate(vertex_2[2])});
                polygonTriangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_3[1]), convertToPolygonVertexCoordinate(vertex_3[2])});
                if (MarchingCube.areNeighbors(vertex_1, vertex_2) && MarchingCube.areNeighbors(vertex_1, vertex_3)) {
                    fourthPointInSquare = new Boolean[]{vertex_1[0], !vertex_1[1], !vertex_1[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_2));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[1]), convertToPolygonVertexCoordinate(vertex_2[2])});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_3[1]), convertToPolygonVertexCoordinate(vertex_3[2])});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_3));
                } else if (MarchingCube.areNeighbors(vertex_2, vertex_1) && MarchingCube.areNeighbors(vertex_2, vertex_3)) {
                    fourthPointInSquare = new Boolean[]{vertex_2[0], !vertex_2[1], !vertex_2[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_1));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[1]), convertToPolygonVertexCoordinate(vertex_1[2])});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_3[1]), convertToPolygonVertexCoordinate(vertex_3[2])});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_3));
                } else if (MarchingCube.areNeighbors(vertex_3, vertex_1) && MarchingCube.areNeighbors(vertex_3, vertex_2)) {
                    fourthPointInSquare = new Boolean[]{vertex_3[0], !vertex_3[1], !vertex_3[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_1));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[1]), convertToPolygonVertexCoordinate(vertex_1[2])});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[1]), convertToPolygonVertexCoordinate(vertex_2[2])});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_2));
                }
            } else if (vertex_1[1].booleanValue() == vertex_2[1].booleanValue() && vertex_1[1].booleanValue() == vertex_3[1].booleanValue()) {
                polygonTriangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[2])});
                polygonTriangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[2])});
                polygonTriangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_3[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_3[2])});
                if (MarchingCube.areNeighbors(vertex_1, vertex_2) && MarchingCube.areNeighbors(vertex_1, vertex_3)) {
                    fourthPointInSquare = new Boolean[]{!vertex_1[0], vertex_1[1], !vertex_1[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_2));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[2])});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_3[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_3[2])});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_3));
                } else if (MarchingCube.areNeighbors(vertex_2, vertex_1) && MarchingCube.areNeighbors(vertex_2, vertex_3)) {
                    fourthPointInSquare = new Boolean[]{!vertex_2[0], vertex_2[1],! vertex_2[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_1));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[2])});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_3[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_3[2])});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_3));
                } else if (MarchingCube.areNeighbors(vertex_3, vertex_1) && MarchingCube.areNeighbors(vertex_3, vertex_2)) {
                    fourthPointInSquare = new Boolean[]{!vertex_3[0], vertex_3[1], !vertex_3[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_1));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[2])});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]), PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[2])});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_2));
                }
            } else if (vertex_1[2].booleanValue() == vertex_2[2].booleanValue() && vertex_1[2].booleanValue() == vertex_3[2].booleanValue()) {
                polygonTriangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]), convertToPolygonVertexCoordinate(vertex_1[1]), PolygonVertexCoordinate.HALF});
                polygonTriangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]), convertToPolygonVertexCoordinate(vertex_2[1]), PolygonVertexCoordinate.HALF});
                polygonTriangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_3[0]), convertToPolygonVertexCoordinate(vertex_3[1]), PolygonVertexCoordinate.HALF});
                if (MarchingCube.areNeighbors(vertex_1, vertex_2) && MarchingCube.areNeighbors(vertex_1, vertex_3)) {
                    fourthPointInSquare = new Boolean[]{!vertex_1[0], !vertex_1[1], vertex_1[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_2));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]), convertToPolygonVertexCoordinate(vertex_2[1]), PolygonVertexCoordinate.HALF});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_3[0]), convertToPolygonVertexCoordinate(vertex_3[1]), PolygonVertexCoordinate.HALF});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_3));
                } else if (MarchingCube.areNeighbors(vertex_2, vertex_1) && MarchingCube.areNeighbors(vertex_2, vertex_3)) {
                    fourthPointInSquare = new Boolean[]{!vertex_2[0], !vertex_2[1], vertex_2[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_1));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]), convertToPolygonVertexCoordinate(vertex_1[1]), PolygonVertexCoordinate.HALF});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_3[0]), convertToPolygonVertexCoordinate(vertex_3[1]), PolygonVertexCoordinate.HALF});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_3));
                } else if (MarchingCube.areNeighbors(vertex_3, vertex_1) && MarchingCube.areNeighbors(vertex_3, vertex_2)) {
                    fourthPointInSquare = new Boolean[]{!vertex_3[0], !vertex_3[1], vertex_3[2]};
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_1));
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]), convertToPolygonVertexCoordinate(vertex_1[1]), PolygonVertexCoordinate.HALF});
                    polygonRectangle.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]), convertToPolygonVertexCoordinate(vertex_2[1]), PolygonVertexCoordinate.HALF});
                    polygonRectangle.add(getMiddlePoint(fourthPointInSquare, vertex_2));
                }
            }

            this.polygonVertices.add(polygonTriangle);
            this.polygonVertices.add(polygonRectangle);

        }

        private static PolygonVertexCoordinate[] getMiddlePoint(Boolean[] vertex_1, Boolean[] vertex_2) {
            PolygonVertexCoordinate[] middlePoint = new PolygonVertexCoordinate[3];
            if (vertex_1[0].booleanValue() != vertex_2[0].booleanValue()) {
                middlePoint[0] = PolygonVertexCoordinate.HALF;
                middlePoint[1] = convertToPolygonVertexCoordinate(vertex_1[1]);
                middlePoint[2] = convertToPolygonVertexCoordinate(vertex_1[2]);
            } else if (vertex_1[1].booleanValue() != vertex_2[1].booleanValue()) {
                middlePoint[0] = convertToPolygonVertexCoordinate(vertex_1[0]);
                middlePoint[1] = PolygonVertexCoordinate.HALF;
                middlePoint[2] = convertToPolygonVertexCoordinate(vertex_1[2]);
            } else {
                middlePoint[0] = convertToPolygonVertexCoordinate(vertex_1[0]);
                middlePoint[1] = convertToPolygonVertexCoordinate(vertex_1[1]);
                middlePoint[2] = PolygonVertexCoordinate.HALF;
            }
            return middlePoint;
        }

        private void addPolygonOfAnEdge(Boolean[] vertex_1, Boolean[] vertex_2) {

            if (!MarchingCube.areNeighbors(vertex_1, vertex_2)) {
                return;
            }

            ArrayList<PolygonVertexCoordinate[]> polygon = new ArrayList<>();

            if (vertex_1[0].booleanValue() != vertex_2[0].booleanValue()) {
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]),
                    convertToPolygonVertexCoordinate(vertex_1[1]), PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]),
                    PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[2])});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]),
                    PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_2[2])});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]),
                    convertToPolygonVertexCoordinate(vertex_2[1]), PolygonVertexCoordinate.HALF});
            } else if (vertex_1[1].booleanValue() != vertex_2[1].booleanValue()) {
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]),
                    convertToPolygonVertexCoordinate(vertex_1[1]), PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    convertToPolygonVertexCoordinate(vertex_1[1]), convertToPolygonVertexCoordinate(vertex_1[2])});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    convertToPolygonVertexCoordinate(vertex_2[1]), convertToPolygonVertexCoordinate(vertex_2[2])});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]),
                    convertToPolygonVertexCoordinate(vertex_2[1]), PolygonVertexCoordinate.HALF});
            } else if (vertex_1[2].booleanValue() != vertex_2[2].booleanValue()) {
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_1[0]),
                    PolygonVertexCoordinate.HALF, convertToPolygonVertexCoordinate(vertex_1[2])});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    convertToPolygonVertexCoordinate(vertex_1[1]), convertToPolygonVertexCoordinate(vertex_1[2])});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF,
                    convertToPolygonVertexCoordinate(vertex_2[1]), convertToPolygonVertexCoordinate(vertex_2[2])});
                polygon.add(new PolygonVertexCoordinate[]{convertToPolygonVertexCoordinate(vertex_2[0]),
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
            ArrayList<Boolean[]> innerVertices = cube.getInnerVertices();
            addPolygonOfAnIsolatedTriangle(innerVertices.get(0), innerVertices.get(1), innerVertices.get(2));
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

            if ((boolean) innerVertices.get(0)[0] == (boolean) innerVertices.get(1)[0]
                    && (boolean) innerVertices.get(0)[0] == (boolean) innerVertices.get(2)[0]
                    && (boolean) innerVertices.get(0)[0] == (boolean) innerVertices.get(3)[0]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ZERO});
            } else if ((boolean) innerVertices.get(0)[1] == (boolean) innerVertices.get(1)[1]
                    && (boolean) innerVertices.get(0)[1] == (boolean) innerVertices.get(2)[1]
                    && (boolean) innerVertices.get(0)[1] == (boolean) innerVertices.get(3)[1]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ONE});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF, PolygonVertexCoordinate.ZERO});
            } else if ((boolean) innerVertices.get(0)[2] == (boolean) innerVertices.get(1)[2]
                    && (boolean) innerVertices.get(0)[2] == (boolean) innerVertices.get(2)[2]
                    && (boolean) innerVertices.get(0)[2] == (boolean) innerVertices.get(3)[2]) {
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.HALF});
                polygon.add(new PolygonVertexCoordinate[]{PolygonVertexCoordinate.ONE, PolygonVertexCoordinate.ZERO, PolygonVertexCoordinate.HALF});
            }

            this.polygonVertices.add(polygon);

        }

        private void computePolygonsInCaseOfTetraeder(MarchingCube cube) {

            ArrayList<Boolean[]> innerVertices = cube.getInnerVertices();

            Boolean[] vertex_1 = innerVertices.get(0);
            Boolean[] vertex_2 = innerVertices.get(1);
            Boolean[] vertex_3 = innerVertices.get(2);
            Boolean[] vertex_4 = innerVertices.get(3);
            ArrayList<PolygonVertexCoordinate[]> polygon = new ArrayList<>();

            if (MarchingCube.areNeighbors(vertex_1, vertex_2) && MarchingCube.areNeighbors(vertex_1, vertex_3) && MarchingCube.areNeighbors(vertex_1, vertex_4)) {
                this.polygonVertices.add(getHexagonInFrontOfVertex(vertex_1));
            } else if (MarchingCube.areNeighbors(vertex_2, vertex_1) && MarchingCube.areNeighbors(vertex_2, vertex_3) && MarchingCube.areNeighbors(vertex_2, vertex_4)) {
                this.polygonVertices.add(getHexagonInFrontOfVertex(vertex_2));
            } else if (MarchingCube.areNeighbors(vertex_3, vertex_1) && MarchingCube.areNeighbors(vertex_3, vertex_2) && MarchingCube.areNeighbors(vertex_3, vertex_4)) {
                this.polygonVertices.add(getHexagonInFrontOfVertex(vertex_3));
            } else if (MarchingCube.areNeighbors(vertex_4, vertex_1) && MarchingCube.areNeighbors(vertex_4, vertex_2) && MarchingCube.areNeighbors(vertex_4, vertex_3)) {
                this.polygonVertices.add(getHexagonInFrontOfVertex(vertex_4));
            }

        }

        private ArrayList<PolygonVertexCoordinate[]> getHexagonInFrontOfVertex(Boolean[] vertex) {
            ArrayList<PolygonVertexCoordinate[]> polygon = new ArrayList<>();
            polygon.add(getNeighboringMiddlePoint(MarchingCube.getNeighboringVertex(vertex, 0), 1));
            polygon.add(getNeighboringMiddlePoint(MarchingCube.getNeighboringVertex(vertex, 0), 2));
            polygon.add(getNeighboringMiddlePoint(MarchingCube.getNeighboringVertex(vertex, 2), 0));
            polygon.add(getNeighboringMiddlePoint(MarchingCube.getNeighboringVertex(vertex, 2), 1));
            polygon.add(getNeighboringMiddlePoint(MarchingCube.getNeighboringVertex(vertex, 1), 2));
            polygon.add(getNeighboringMiddlePoint(MarchingCube.getNeighboringVertex(vertex, 1), 0));
            return polygon;
        }
        
        private static PolygonVertexCoordinate[] getNeighboringMiddlePoint(Boolean[] vertex, int axe){
            return getMiddlePoint(vertex, MarchingCube.getNeighboringVertex(vertex, axe));
        }

        private void computePolygonsInCaseOfAFourChain(MarchingCube cube) {

        }

        private void computePolygonsInCaseOfIsolatedTrangleAndIsolatedVertex(MarchingCube cube) {

            ArrayList<Boolean[]> innerVertices = cube.getInnerVertices();

            Boolean[] vertex_1 = innerVertices.get(0);
            Boolean[] vertex_2 = innerVertices.get(1);
            Boolean[] vertex_3 = innerVertices.get(2);
            Boolean[] vertex_4 = innerVertices.get(3);

            if (cube.isInnerPointAndIsolatedVertex(vertex_1)) {
                addPolygonOfAnIsolatedVertex(vertex_1);
                addPolygonOfAnIsolatedTriangle(vertex_2, vertex_3, vertex_4);
            } else if (cube.isInnerPointAndIsolatedVertex(vertex_2)) {
                addPolygonOfAnIsolatedVertex(vertex_2);
                addPolygonOfAnIsolatedTriangle(vertex_1, vertex_3, vertex_4);
            } else if (cube.isInnerPointAndIsolatedVertex(vertex_3)) {
                addPolygonOfAnIsolatedVertex(vertex_3);
                addPolygonOfAnIsolatedTriangle(vertex_1, vertex_2, vertex_4);
            } else if (cube.isInnerPointAndIsolatedVertex(vertex_4)) {
                addPolygonOfAnIsolatedVertex(vertex_4);
                addPolygonOfAnIsolatedTriangle(vertex_1, vertex_2, vertex_3);
            }

        }

    }

    public class MarchingCubeForComputation {

        private final ArrayList<Polygon> polygons = new ArrayList<>();

        public ArrayList<Polygon> getPolygons() {
            return this.polygons;
        }

        @Override
        public String toString() {
            String cube = "[";
            for (int i = 0; i < this.polygons.size(); i++) {
                cube += this.polygons.get(i).toString();
                if (i < this.polygons.size() - 1) {
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

    public ArrayList<Expression> getExpressions() {
        return this.exprs;
    }

    public Color getColor() {
        return this.color;
    }

    public void setExpressions(Expression... exprs) {
        this.exprs = new ArrayList<>();
        this.exprs.addAll(Arrays.asList(exprs));
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
                    /* 
                    Falls es mehr als 4 innere Punkte gibt: innere und äußere Punkte 
                    vertauschen und Polygone berechnen. Danach wieder zurücktauschen.
                     */
                    cubesWithPolygons[i][j][k] = new MarchingCubeWithPolygons();
                    if (cubes[i][j][k].getInnerVertices().size() > 4) {
                        cubes[i][j][k].switchPoints();
                        cubesWithPolygons[i][j][k].computePolygons(cubes[i][j][k]);
                        cubes[i][j][k].switchPoints();
                    } else {
                        cubesWithPolygons[i][j][k].computePolygons(cubes[i][j][k]);
                    }
                }
            }
        }

        return cubesWithPolygons;

    }

    private MarchingCubeForComputation[][][] convertToMarchingCubesForComputation(MarchingCubeWithPolygons[][][] cubes,
            double x_0, double x_1, double y_0, double y_1, double z_0, double z_1) {

        MarchingCubeForComputation[][][] cubesForComputation = new MarchingCubeForComputation[cubes.length][cubes[0].length][cubes[0][0].length];

        int numberOfIntervalsX = cubes.length;
        int numberOfIntervalsY = cubes[0].length;
        int numberOfIntervalsZ = cubes[0][0].length;

        for (int i = 0; i < cubes.length; i++) {
            for (int j = 0; j < cubes[0].length; j++) {
                for (int k = 0; k < cubes[0][0].length; k++) {
                    cubesForComputation[i][j][k] = new MarchingCubeForComputation();
                    cubesForComputation[i][j][k].computePolygons(cubes[i][j][k], x_0 + i * (x_1 - x_0) / numberOfIntervalsX, x_0 + (i + 1) * (x_1 - x_0) / numberOfIntervalsX,
                            y_0 + j * (y_1 - y_0) / numberOfIntervalsY, y_0 + (j + 1) * (y_1 - y_0) / numberOfIntervalsY,
                            z_0 + k * (z_1 - z_0) / numberOfIntervalsZ, z_0 + (k + 1) * (z_1 - z_0) / numberOfIntervalsZ);
                }
            }
        }

        return cubesForComputation;

    }

    /**
     * Voraussetzung: expr und var sind bereits gesetzt.
     *
     * @throws EvaluationException
     */
    private void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd,
            Expression exprApplStart, Expression exprApplEnd) throws EvaluationException {
        this.minX = exprAbscStart.evaluate();
        this.minY = exprOrdStart.evaluate();
        this.minZ = exprApplStart.evaluate();
        this.maxX = exprAbscEnd.evaluate();
        this.maxY = exprOrdEnd.evaluate();
        this.maxZ = exprApplEnd.evaluate();
        this.minXOrigin = this.minX;
        this.minYOrigin = this.minY;
        this.minZOrigin = this.minZ;
        this.maxXOrigin = this.maxX;
        this.maxYOrigin = this.maxY;
        this.maxZOrigin = this.maxZ;
        this.axeCenterX = (this.minX + this.maxX) / 2;
        this.axeCenterY = (this.minY + this.maxY) / 2;
        this.axeCenterZ = (this.minZ + this.maxZ) / 2;
        this.axeCenterXOrigin = this.axeCenterX;
        this.axeCenterYOrigin = this.axeCenterY;
        this.axeCenterZOrigin = this.axeCenterZ;
    }

    /**
     * Zeichnet ein (tangentiales) rechteckiges Plättchen des 3D-Graphen
     */
    private void drawInfinitesimalTangentPolygone(Graphics g, Color c, ArrayList<Point> points) {

        if (points.isEmpty()) {
            return;
        }

        GeneralPath tangent = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());
        tangent.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            tangent.lineTo(points.get(i).x, points.get(i).y);
        }
        tangent.closePath();
        Graphics2D g2 = (Graphics2D) g;

        if (presentationMode.equals(AbstractGraphicPanel3D.PresentationMode.WHOLE_GRAPH)) {
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

    private void drawMarchingCubesForComputation(Graphics g) {

        Color c;
        
        if (this.angle <= 90) {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        c = computeColor(this.color, this.minZ, this.maxZ, this.minZ + k * (this.maxZ - this.minZ) / this.cubesForComputation[0][0].length);
                        drawSingleMarchingCube(g, c, i, this.cubesForComputation[0].length - 1 - j, k);
                    }
                }
            }

        } else if (this.angle <= 180) {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        c = computeColor(this.color, this.minZ, this.maxZ, this.minZ + k * (this.maxZ - this.minZ) / this.cubesForComputation[0][0].length);
                        drawSingleMarchingCube(g, c, i, j, k);
                    }
                }
            }

        } else if (this.angle <= 270) {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        c = computeColor(this.color, this.minZ, this.maxZ, this.minZ + k * (this.maxZ - this.minZ) / this.cubesForComputation[0][0].length);
                        drawSingleMarchingCube(g, c, this.cubesForComputation.length - 1 - i, j, k);
                    }
                }
            }

        } else {

            for (int i = 0; i < this.cubesForComputation.length; i++) {
                for (int j = 0; j < this.cubesForComputation[0].length; j++) {
                    for (int k = 0; k < this.cubesForComputation[0][0].length; k++) {
                        c = computeColor(this.color, this.minZ, this.maxZ, this.minZ + k * (this.maxZ - this.minZ) / this.cubesForComputation[0][0].length);
                        drawSingleMarchingCube(g, c, this.cubesForComputation.length - 1 - i, this.cubesForComputation[0].length - 1 - j, k);
                    }
                }
            }

        }

    }

    private void sortPolygonsForDraw(MarchingCubeForComputation cube) {
        Collections.sort(cube.getPolygons());
    }

    private void drawSingleMarchingCube(Graphics g, Color c, int i, int j, int k) {

        int[] pixel;
        ArrayList<Point> polygon;

        MarchingCubeForComputation cube = this.cubesForComputation[i][j][k];
        sortPolygonsForDraw(cube);

        for (Polygon p : cube.getPolygons()) {
            polygon = new ArrayList<>();
            for (Double[] polygonVerexCoordinates : p.getVertices()) {
                pixel = convertToPixel(polygonVerexCoordinates[0], polygonVerexCoordinates[1], polygonVerexCoordinates[2]);
                polygon.add(new Point(pixel[0], pixel[1]));
            }
            drawInfinitesimalTangentPolygone(g, c, polygon);
        }

    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Graphen.
     */
    private void drawImplicitGraph3D(Graphics g) {

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

        drawLevelsOnEast(g, this.varAbsc, this.varOrd, this.varAppl);
        drawLevelsOnSouth(g, this.varAbsc, this.varOrd, this.varAppl);
        drawLevelsOnWest(g, this.varAbsc, this.varOrd, this.varAppl);
        drawLevelsOnNorth(g, this.varAbsc, this.varOrd, this.varAppl);
        drawLevelsBottom(g);
        drawMarchingCubesForComputation(g);

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
        drawImplicitGraph3D(g);
    }

}
