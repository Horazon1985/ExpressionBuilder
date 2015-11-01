package integrationmethods;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.MathToolException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyBinaryOperationMethods;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigInteger;
import linearalgebraalgorithms.GaussAlgorithm;
import matrixexpressionbuilder.Matrix;
import solveequationmethods.PolynomialRootsMethods;

public abstract class PartialFractionDecompositionMethods {

    private class PartialFractionDecompositionNotComputableException extends MathToolException {

        private static final String NO_EXPLICIT_PFD_MESSAGE = "Function admits no explicit partial fraction decomposition.";

        public PartialFractionDecompositionNotComputableException() {
            super(NO_EXPLICIT_PFD_MESSAGE);
        }

        public PartialFractionDecompositionNotComputableException(String s) {
            super(s);
        }

    }

    /**
     * Hauptmethode für die Zerlegung einer rationalen Funktion f in
     * Partialbrüche.
     */
    public static Expression getPartialFractionDecomposition2(Expression f, String var) throws EvaluationException {

        if (!isRationalFunctionInCanonicalForm(f, var)) {
            return f;
        }

        // Ab hier ist f eine echte rationale Funktion.
        BigInteger degEnumerator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degDenominator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getRight(), var);

        if (degEnumerator.compareTo(BigInteger.ZERO) < 0
                || degEnumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION)) > 0
                || degDenominator.compareTo(BigInteger.ZERO) < 0
                || degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION)) > 0) {
            return f;
        }

        // Grad des Nenners.
        int n = degDenominator.intValue();

        Expression[] polynomialAndFractionalPartOfF = performPolynomialDivisionForFractionalDecomposition(f, var);

        Expression polynomialPart = polynomialAndFractionalPartOfF[0];
        Expression fractionalPart = polynomialAndFractionalPartOfF[1];

        // Sicherheitsabfrage.
        if (!fractionalPart.isNotQuotient() || !isDenominatorOfCorrectForm(((BinaryOperation) fractionalPart).getRight(), var)) {
            return f;
        }

        ExpressionCollection coefficientsForCompare = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);

        // f schreiben als: Zähler ausmultipliziert und zusammengefasst, Nenner faktorisiert.
        fractionalPart = PolynomialRootsMethods.getPolynomialFromCoefficients(coefficientsForCompare, var).div(
                PolynomialRootsMethods.decomposePolynomialInIrreducibleFactors(((BinaryOperation) fractionalPart).getRight(), var));

        // Ansatz für die Partialbruchzerlegung herstellen.
        Expression approachForPFD = getPartialFractionDecompositionApproach((BinaryOperation) fractionalPart, var);

        if (!(approachForPFD instanceof BinaryOperation)) {
            return f;
        }

        // Ansatz auf einen Nenner bringen und Zähler betrachten.
        approachForPFD = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) approachForPFD);

        if (approachForPFD.isNotQuotient()) {
            return f;
        }

        // fractionalPart ist ein Quotient!
        Expression enumeratorInPFDApproach = ((BinaryOperation) approachForPFD).getLeft();

        // Nun Koeffizientenvergleich durchführen.
        enumeratorInPFDApproach = enumeratorInPFDApproach.simplifyExpandPowerful();
        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(enumeratorInPFDApproach, var);

        return null;

    }

    /**
     * Gibt zurück, ob f ein Quotient zweier Polynome in var ist.
     */
    public static boolean isRationalFunctionInCanonicalForm(Expression f, String var) {
        return f.isQuotient()
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getRight(), var);
    }

    private static Expression[] performPolynomialDivisionForFractionalDecomposition(Expression f, String var) throws EvaluationException {
        if (!isRationalFunctionInCanonicalForm(f, var)) {
            // Nichts tun.
            return new Expression[]{ZERO, f};
        }

        ExpressionCollection coefficientsEnumerator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getRight(), var);

        if (coefficientsEnumerator.getBound() >= coefficientsDenominator.getBound()) {

            ExpressionCollection[] resultOfPolynomialDivision = PolynomialRootsMethods.polynomialDivision(coefficientsEnumerator, coefficientsDenominator);
            Expression polynomialPart = Expression.ZERO;
            for (int i = 0; i < resultOfPolynomialDivision[0].getBound(); i++) {
                if (polynomialPart.equals(Expression.ZERO)) {
                    polynomialPart = resultOfPolynomialDivision[0].get(i).mult(Variable.create(var).pow(i + 1)).div(i + 1);
                } else {
                    polynomialPart = polynomialPart.add(resultOfPolynomialDivision[0].get(i).mult(Variable.create(var).pow(i + 1)).div(i + 1));
                }
            }

            return new Expression[]{polynomialPart, PolynomialRootsMethods.getPolynomialFromCoefficients(resultOfPolynomialDivision[1], var).div(
                ((BinaryOperation) f).getRight())};

        }

        return new Expression[]{ZERO, f};

    }

    private static boolean isDenominatorOfCorrectForm(Expression f, String var) {
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        ExpressionCollection coefficients;
        for (int i = 0; i < factors.getBound(); i++) {
            if (!SimplifyPolynomialMethods.isPolynomial(factors.get(i), var)) {
                return false;
            }
            try {
                if (factors.get(i).isPower()
                        && ((BinaryOperation) factors.get(i)).getRight().isIntegerConstant()
                        && ((BinaryOperation) factors.get(i)).getRight().isPositive()) {
                    coefficients = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) factors.get(i)).getLeft(), var);
                } else {
                    coefficients = PolynomialRootsMethods.getPolynomialCoefficients(factors.get(i), var);
                }
                if (coefficients.getBound() > 3) {
                    return false;
                }
            } catch (EvaluationException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Liefert einen Ansatz für die Partialbruchzerlegung von f.<br>
     * VORAUSSETZUNG: f ist eine rationale Funktion, deg(Zähler) < deg(Nenner),
     * und der Nenner von f besitzt die korrekte Form.
     */
    private static Expression getPartialFractionDecompositionApproach(BinaryOperation f, String var) {

        Expression approachForPFD = Expression.ZERO;

        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(f.getRight());
        int degOfFactorBase, exponent;
        Expression currentBase, currentEnumeratorInPFD;
        int indexOfPFDVar = 1;

        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i).isPower()
                    && ((BinaryOperation) factorsDenominator.get(i)).getRight().isIntegerConstant()
                    && ((BinaryOperation) factorsDenominator.get(i)).getRight().isPositive()) {
                exponent = ((Constant) ((BinaryOperation) factorsDenominator.get(i)).getRight()).getValue().intValue();
                degOfFactorBase = SimplifyPolynomialMethods.degreeOfPolynomial(
                        ((BinaryOperation) factorsDenominator.get(i)).getLeft(), var).intValue();
                currentBase = ((BinaryOperation) factorsDenominator.get(i)).getLeft();
            } else {
                exponent = 1;
                degOfFactorBase = SimplifyPolynomialMethods.degreeOfPolynomial(factorsDenominator.get(i), var).intValue();
                currentBase = factorsDenominator.get(i);
            }

            for (int j = 0; j < exponent; j++) {
                currentEnumeratorInPFD = Expression.ZERO;
                for (int k = 0; k < degOfFactorBase; k++) {
                    currentEnumeratorInPFD = currentEnumeratorInPFD.add(
                            Variable.create(getPFDVariable(indexOfPFDVar)).mult(Variable.create(var).pow(k)));
                    indexOfPFDVar++;
                }
                approachForPFD = approachForPFD.add(currentEnumeratorInPFD.div(currentBase.pow(j)));
            }
        }

        return approachForPFD;

    }

    private static String getPFDVariable(int i) {
        return "A_" + i;
    }

    private static Expression[] getCoefficientsForPFD(ExpressionCollection coefficientsOfEnumeratorInPFDApproach, ExpressionCollection coefficientsInEnumeratorForCompare, int n, String var) throws EvaluationException {

        // Matrix für das entsprechende Gleichungssystem bilden.
        Expression[][] matrixEntries = new Expression[n][n + 1];

        // Koeffizienten der entsprechenden Unbestimmten in der Partialbruchzerlegung eintragen.
        Expression currentCoefficientForCompare;
        for (int i = 0; i < n; i++) {
            if (coefficientsInEnumeratorForCompare.get(i) != null) {
                currentCoefficientForCompare = coefficientsInEnumeratorForCompare.get(i);
            } else {
                currentCoefficientForCompare = ZERO;
            }
            for (int j = 0; j < n; j++) {
                matrixEntries[i][j] = currentCoefficientForCompare.diff(getPFDVariable(j)).simplify();
            }
        }

        // Koeffizienten aus dem Zähler von fractionalPart eintragen.
        for (int i = 0; i < n; i++) {
            if (coefficientsInEnumeratorForCompare.get(i) != null) {
                matrixEntries[i][n] = coefficientsInEnumeratorForCompare.get(i);
            } else {
                matrixEntries[i][n] = ZERO;
            }
        }

        // Matrix für das entsprechende LGS bilden.
        Matrix matrix = new Matrix(matrixEntries);
        // Matrix matrix auf Zeilenstufenform bringen.
        matrix = GaussAlgorithm.computeRowEcholonForm(matrix);

        // Nun sukzessive nach den gesuchten Koeffizienten auflösen.
        Expression[] coefficientsForPFD = new Expression[n];
        Expression currentCoefficient;
        for (int i = 0; i < n; i++) {
            currentCoefficient = matrix.getEntry(i, n);
            for (int j = n - 1; j > i; j--) {
                currentCoefficient = currentCoefficient.sub(matrix.getEntry(i, j).mult(coefficientsForPFD[j]));
            }
            currentCoefficient = currentCoefficient.div(matrix.getEntry(i, i)).simplify();
        }

        return coefficientsForPFD;

    }

}
