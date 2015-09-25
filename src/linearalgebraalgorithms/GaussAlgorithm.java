package linearalgebraalgorithms;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import java.awt.Dimension;
import java.util.ArrayList;
import matrixexpressionbuilder.Matrix;
import matrixsimplifymethods.MatrixExpressionCollection;

public class GaussAlgorithm {

    /**
     * Gibt die Zeilenstufenform der Matrix matrix zurück.
     *
     * @throws EvaluationException
     */
    public static Matrix computeRowEcholonForm(Matrix matrix) throws EvaluationException {

        Dimension dim = matrix.getDimension();

        if (dim.height <= 1 || dim.width == 0) {
            /*
             dim.height == 0 oder dim.width == 0 sollte beim normalen Vorgehen
             eigentlich nie passieren. Wenn die Matrix nur eine Zeile besitzt,
             dann soll sie selbst wieder zurückgegeben werden.
             */
            return matrix;
        }

        // Jetzt wird die Zeilenstufenform von matrix rekursiv berechnets.
        int indexOfFirstRowWithNonZeroFirstEntry = 0;
        Expression currentEntry;

        for (int i = 0; i < matrix.getRowNumber(); i++) {
            currentEntry = matrix.getEntry(i, 0).simplify();
            if (currentEntry.equals(Expression.ZERO)) {
                indexOfFirstRowWithNonZeroFirstEntry++;
            } else {
                break;
            }
        }

        if (indexOfFirstRowWithNonZeroFirstEntry == matrix.getRowNumber()) {
            // Dann ist die gesamte erste Spalte die 0-Spalte.
            if (dim.width == 1) {
                return matrix;
            }
            Expression[][] minorEntry = new Expression[dim.height][dim.width - 1];
            for (int i = 0; i < dim.height; i++) {
                for (int j = 1; j < dim.width; j++) {
                    minorEntry[i][j - 1] = matrix.getEntry(i, j);
                }
            }
            Matrix rightPartMatrix = computeRowEcholonForm(new Matrix(minorEntry));
            Expression[][] resultEntries = new Expression[dim.height][dim.width];
            for (int i = 0; i < dim.height; i++) {
                for (int j = 0; j < dim.width; j++) {
                    if (j == 0) {
                        resultEntries[i][0] = Expression.ZERO;
                    } else {
                        resultEntries[i][j] = rightPartMatrix.getEntry(i, j - 1);
                    }
                }
            }
            return new Matrix(resultEntries);
        }

        // Hier angelangt ist die erste Spalte nicht identisch 0.
        matrix = matrix.interchangeRows(0, indexOfFirstRowWithNonZeroFirstEntry);
        Expression[][] minorEntry = new Expression[dim.height - 1][dim.width - 1];

        Expression pivotElement = matrix.getEntry(0, 0);
        for (int i = 1; i < dim.height; i++) {
            if (!matrix.getEntry(i, 0).equals(Expression.ZERO)) {
                matrix = (Matrix) matrix.addMultipleOfRowToRow(i, 0, Expression.MINUS_ONE.mult(matrix.getEntry(i, 0)).div(pivotElement)).simplify();
            }
        }

        for (int i = 1; i < dim.height; i++) {
            for (int j = 1; j < dim.width; j++) {
                minorEntry[i - 1][j - 1] = matrix.getEntry(i, j);
            }
        }

        Matrix minorMatrix = computeRowEcholonForm(new Matrix(minorEntry));

        Expression[][] resultEntry = new Expression[dim.height][dim.width];
        for (int i = 0; i < dim.height; i++) {
            for (int j = 0; j < dim.width; j++) {
                if (j == 0 && i > 0) {
                    resultEntry[i][0] = Expression.ZERO;
                } else if (i == 0) {
                    resultEntry[0][j] = matrix.getEntry(0, j);
                } else {
                    resultEntry[i][j] = minorMatrix.getEntry(i - 1, j - 1);
                }
            }
        }

        return new Matrix(resultEntry);

    }

    /**
     * Gibt eine Basis der Matrix matrix zurück. Ist insbesondere die
     * zurückgegebene MatrixExpressionCollection leer, so besteht der Kern nur
     * aus [0; ...; 0].
     */
    public static MatrixExpressionCollection computeKernelOfMatrix(Matrix matrix) {

        MatrixExpressionCollection basis = new MatrixExpressionCollection();

        try {
            // Matrix auf Zeilenstufenform bringen.
            matrix = computeRowEcholonForm(matrix);
        } catch (EvaluationException e) {
            return basis;
        }

        ArrayList<Integer> listOfIndicesWithJumpings = new ArrayList<>();
        for (int j = 0; j < matrix.getColumnNumber(); j++) {
            if (listOfIndicesWithJumpings.size() >= matrix.getRowNumber()) {
                break;
            }
            if (!matrix.getEntry(listOfIndicesWithJumpings.size(), j).equals(Expression.ZERO)) {
                listOfIndicesWithJumpings.add(j);
            }
        }

        /*
         Basis des Lösungsraumes bilden. Sei n = matrix.getColumnNumber() und
         k = listOfIndicesWithJumpings.size(). Dann ist k = rank(matrix) und
         die Dimension des Lösungsraumes ist dim(L) = n - k.
         */
        Expression[][] basisVectorEntry = new Expression[matrix.getColumnNumber() - listOfIndicesWithJumpings.size()][matrix.getColumnNumber()];
        int basisVectorCount = 0;
        Expression componentOfSolutionVector;

        for (int j = 0; j < matrix.getColumnNumber(); j++) {

            if (listOfIndicesWithJumpings.contains(j)) {
                continue;
            }

            // j-ten Basisvektor bilden.
            int line;
            for (int k = matrix.getColumnNumber() - 1; k >= 0; k--) {
                componentOfSolutionVector = Expression.ZERO;
                /*
                 Sei n = matrix.getColumnNumber() Hier werden der Reihe nach
                 die n-te, (n - 1)-te, ..., 2-te, 1-te Komponente des j-ten
                 Basisvektors berechnet.
                 */
                if (k > j) {
                    basisVectorEntry[basisVectorCount][k] = Expression.ZERO;
                } else if (k == j) {
                    basisVectorEntry[basisVectorCount][k] = Expression.ONE;
                } else {
                    if (!listOfIndicesWithJumpings.contains(k)) {
                        basisVectorEntry[basisVectorCount][k] = Expression.ZERO;
                    } else {

                        line = listOfIndicesWithJumpings.indexOf(k);
                        for (int m = j; m >= k + 1; m--) {
                            if (componentOfSolutionVector.equals(Expression.ZERO)) {
                                componentOfSolutionVector = Expression.MINUS_ONE.mult(basisVectorEntry[basisVectorCount][m].mult(
                                        matrix.getEntry(line, m)));
                            } else {
                                componentOfSolutionVector = componentOfSolutionVector.sub(basisVectorEntry[basisVectorCount][m].mult(
                                        matrix.getEntry(line, m)));
                            }
                        }
                        componentOfSolutionVector = componentOfSolutionVector.div(matrix.getEntry(line, k));
                        basisVectorEntry[basisVectorCount][k] = componentOfSolutionVector;

                    }
                }
            }

            basisVectorCount++;

        }

        // Einzelne Basisvektoren bilden.
        for (int i = 0; i < basisVectorEntry.length; i++) {
            try {
                for (int j = 0; j < basisVectorEntry[i].length; j++) {
                    basisVectorEntry[i][j] = basisVectorEntry[i][j].simplify();
                }
                basis.add(new Matrix(basisVectorEntry[i]));
            } catch (EvaluationException e) {
                /*
                 Falls beim Vereinfachen des Basisvektors Fehler geworfen
                 wurden, dann wird dieser zur Lösungsmenge nicht hinzugefügt.
                 Sollte bei allen vernünftigen linearen Gleichungssystemen
                 nicht passieren.
                 */
            }
        }

        return basis;

    }

}
