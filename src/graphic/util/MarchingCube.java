package graphic.util;

import java.util.ArrayList;
import java.util.List;

public class MarchingCube {

    List<Boolean[]> innerVertices = new ArrayList<>();

    public List<Boolean[]> getInnerVertices() {
        return this.innerVertices;
    }

    public void addInnerVertex(Boolean[] vertex) {
        this.innerVertices.add(vertex);
    }

    @Override
    public String toString() {
        String cube = "[";
        for (int i = 0; i < this.innerVertices.size(); i++) {
            cube += "(";
            if (this.innerVertices.get(i)[0] == false) {
                cube += "0,";
            } else {
                cube += "1,";
            }
            if (this.innerVertices.get(i)[1] == false) {
                cube += "0,";
            } else {
                cube += "1,";
            }
            if (this.innerVertices.get(i)[2] == false) {
                cube += "0)";
            } else {
                cube += "1)";
            }
            if (i < this.innerVertices.size() - 1) {
                cube = cube + ", ";
            }
        }
        return cube + "]";
    }

    public void switchPoints() {
        List<Boolean[]> switchedVertices = new ArrayList<>();
        Boolean[] vertex;

        boolean containsVertex;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {

                    vertex = new Boolean[3];
                    vertex[0] = i == 0;
                    vertex[1] = j == 0;
                    vertex[2] = k == 0;
                    containsVertex = false;
                    for (Boolean[] innerVertex : this.innerVertices) {
                        if (innerVertex[0].booleanValue() == vertex[0].booleanValue()
                                && innerVertex[1].booleanValue() == vertex[1].booleanValue()
                                && innerVertex[2].booleanValue() == vertex[2].booleanValue()) {
                            containsVertex = true;
                            break;
                        }
                    }
                    if (!containsVertex) {
                        switchedVertices.add(vertex);
                    }

                }
            }
        }
        this.innerVertices = switchedVertices;
    }

    public static Boolean[] getNeighboringVertex(Boolean[] vertex, int axe) {
        if (axe == 0) {
            return new Boolean[]{!vertex[0], vertex[1], vertex[2]};
        }
        if (axe == 1) {
            return new Boolean[]{vertex[0], !vertex[1], vertex[2]};
        }
        return new Boolean[]{vertex[0], vertex[1], !vertex[2]};
    }

    private boolean isInnerPoint(Boolean[] vertex) {
        for (Boolean[] innerVertex : this.innerVertices) {
            if (innerVertex[0].booleanValue() == vertex[0].booleanValue()
                    && innerVertex[1].booleanValue() == vertex[1].booleanValue()
                    && innerVertex[2].booleanValue() == vertex[2].booleanValue()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInnerPointAndIsolatedVertex(Boolean[] vertex) {
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

    public static boolean areNeighbors(Boolean[] firstVertex, Boolean[] secondVertex) {
        return !firstVertex[0].booleanValue() == secondVertex[0].booleanValue()
                && firstVertex[1].booleanValue() == secondVertex[1].booleanValue()
                && firstVertex[2].booleanValue() == secondVertex[2].booleanValue()
                || firstVertex[0].booleanValue() == secondVertex[0].booleanValue()
                && !firstVertex[1].booleanValue() == secondVertex[1].booleanValue()
                && firstVertex[2].booleanValue() == secondVertex[2].booleanValue()
                || firstVertex[0].booleanValue() == secondVertex[0].booleanValue()
                && firstVertex[1].booleanValue() == secondVertex[1].booleanValue()
                && !firstVertex[2].booleanValue() == secondVertex[2].booleanValue();
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
        return isTriangle(vertex_1, vertex_2, vertex_3);
    }

    private static boolean isTriangle(Boolean[] vertex_1, Boolean[] vertex_2, Boolean[] vertex_3) {
        // Prüfung, ob die x-, y- oder z-Koordinate bei allen Ecken konstant ist.
        return (boolean) vertex_1[0] == (boolean) vertex_2[0] && (boolean) vertex_1[0] == (boolean) vertex_3[0]
                || (boolean) vertex_1[1] == (boolean) vertex_2[1] && (boolean) vertex_1[1] == (boolean) vertex_3[1]
                || (boolean) vertex_1[2] == (boolean) vertex_2[2] && (boolean) vertex_1[2] == (boolean) vertex_3[2];
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

    public boolean consistsOfAnIsolatedTriangleAndAnIsolatedVertex() {

        if (this.innerVertices.size() != 4) {
            return false;
        }
        Boolean[] vertex_1 = this.innerVertices.get(0);
        Boolean[] vertex_2 = this.innerVertices.get(1);
        Boolean[] vertex_3 = this.innerVertices.get(2);
        Boolean[] vertex_4 = this.innerVertices.get(3);

        return isInnerPointAndIsolatedVertex(vertex_1) && isTriangle(vertex_2, vertex_3, vertex_4)
                || isInnerPointAndIsolatedVertex(vertex_2) && isTriangle(vertex_1, vertex_3, vertex_4)
                || isInnerPointAndIsolatedVertex(vertex_3) && isTriangle(vertex_1, vertex_2, vertex_4)
                || isInnerPointAndIsolatedVertex(vertex_4) && isTriangle(vertex_1, vertex_2, vertex_3);

    }

}
