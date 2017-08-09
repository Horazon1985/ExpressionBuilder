package graphic.util;

public class MarchingSquare {

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

    public boolean containsGraph() {
        return !(this.vertexValues[0][0] < 0 && this.vertexValues[0][1] < 0 && this.vertexValues[1][0] < 0 && this.vertexValues[1][0] < 0
                || this.vertexValues[0][0] > 0 && this.vertexValues[0][1] > 0 && this.vertexValues[1][0] > 0 && this.vertexValues[1][0] > 0);
    }

}
