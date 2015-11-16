package integrationmethods;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.MathToolException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyBinaryOperationMethods;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigInteger;
import java.util.HashSet;
import linearalgebraalgorithms.GaussAlgorithm;
import matrixexpressionbuilder.Matrix;

public abstract class PartialFractionDecompositionMethods {

    /**
     * Private Fehlerklasse für den Fall, dass die Partialbruchzerlegung nicht
     * ermittelt werden konnte.
     */
    private static class PartialFractionDecompositionNotComputableException extends MathToolException {

        private static final String NO_EXPLICIT_PFD_MESSAGE = "Function admits no explicit partial fraction decomposition.";

        public PartialFractionDecompositionNotComputableException() {
            super(NO_EXPLICIT_PFD_MESSAGE);
        }

        public PartialFractionDecompositionNotComputableException(String s) {
            super(s);
        }

    }

    private static final HashSet<TypeSimplify> simplifyTypesForPFD = getSimplifyTypesForPFD();

    private static HashSet<TypeSimplify> getSimplifyTypesForPFD() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_expand_powerful);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        return simplifyTypes;
    }

    /**
     * Hauptmethode für die Zerlegung einer rationalen Funktion f in
     * Partialbrüche.
     */
    public static Expression getPartialFractionDecomposition(Expression f, String var) throws EvaluationException {

        try {

            if (!isRationalFunctionInCanonicalForm(f, var)) {
                throw new PartialFractionDecompositionNotComputableException();
            }

            // Ab hier ist f eine echte rationale Funktion.
            BigInteger degEnumerator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
            BigInteger degDenominator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getRight(), var);

            if (degEnumerator.compareTo(BigInteger.ZERO) < 0
                    || degEnumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION)) > 0
                    || degDenominator.compareTo(BigInteger.ZERO) < 0
                    || degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION)) > 0) {
                throw new PartialFractionDecompositionNotComputableException();
            }

            // Grad des Nenners.
            int n = degDenominator.intValue();

            Expression[] polynomialAndFractionalPartOfF = performPolynomialDivisionForFractionalDecomposition(f, var);

            Expression polynomialPart = polynomialAndFractionalPartOfF[0];
            Expression fractionalPart = polynomialAndFractionalPartOfF[1];

            System.out.println("polPart = " + polynomialPart.toString());
            System.out.println("fracPart = " + fractionalPart.toString());

            // Sicherheitsabfrage.
            if (fractionalPart.isNotQuotient()) {
                throw new PartialFractionDecompositionNotComputableException();
            }

            ExpressionCollection coefficientsForCompare = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) fractionalPart).getLeft(), var);

            // f schreiben als: Zähler ausmultipliziert und zusammengefasst, Nenner faktorisiert.
            fractionalPart = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsForCompare, var).div(
                    SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(((BinaryOperation) fractionalPart).getRight(), var));

            System.out.println("fracPart faktorisiert = " + fractionalPart.toString());

            if (!isDenominatorOfCorrectForm(((BinaryOperation) fractionalPart).getRight(), var)) {
                // Dann kann keine Partialbruchzerlegung ermittelt werden.
                throw new PartialFractionDecompositionNotComputableException();
            }

            // Ansatz für die Partialbruchzerlegung herstellen.
            //Zunächst: Konstanten aus dem Nenner herausziehen.
            Expression constantFactorInDenominator = SimplifyUtilities.produceProduct(SimplifyUtilities.getConstantFactors(((BinaryOperation) fractionalPart).getRight(), var));
            fractionalPart = ((BinaryOperation) fractionalPart).getLeft().div(
                    SimplifyUtilities.produceProduct(SimplifyUtilities.getNonConstantFactors(((BinaryOperation) fractionalPart).getRight(), var)));
            if (fractionalPart.isNotQuotient()) {
                throw new PartialFractionDecompositionNotComputableException();
            }

            Expression approachForPFD = getPartialFractionDecompositionApproach((BinaryOperation) fractionalPart, var);
            Expression[] coefficientsForPFD;

            System.out.println("Ansatz für PBZ = " + approachForPFD.toString());

            if (!(approachForPFD instanceof BinaryOperation)) {
                throw new PartialFractionDecompositionNotComputableException();
            }

            try {
                /* 
                 Elementarer Ansatz: falls der Nenner von f ein Produkt paarweise verschiedener 
                 Linearfaktoren ist, so können die Koeffizienten schnell ermittelt werden.
                 */
                coefficientsForPFD = getCoefficientsForPFDInSeparableCase(fractionalPart, approachForPFD, var);
            } catch (PartialFractionDecompositionNotComputableException e) {
                /* 
                 Sonstiger Fall: Ansatz auf einen Nenner bringen und Zähler betrachten.
                 Danach lineares Gleichungssystem für die Koeffizienten aufstellen und lösen.
                 */
                Expression approachForPFDAsOneFraction = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) approachForPFD);

                System.out.println("Ansatz für PBZ als ein Bruch = " + approachForPFDAsOneFraction.toString());

                if (approachForPFDAsOneFraction.isNotQuotient()) {
                    throw new PartialFractionDecompositionNotComputableException();
                }

                // fractionalPart ist ein Quotient!
                Expression enumeratorInPFDApproach = ((BinaryOperation) approachForPFDAsOneFraction).getLeft();

                // Nun Koeffizientenvergleich durchführen.
                enumeratorInPFDApproach = enumeratorInPFDApproach.simplify(simplifyTypesForPFD);

                System.out.println("Zähler des Ansatzes für PBZ = " + enumeratorInPFDApproach.toString());

                ExpressionCollection coefficientsOfEnumeratorInPFDApproach = SimplifyPolynomialMethods.getPolynomialCoefficients(enumeratorInPFDApproach, var);

                System.out.println("Polynomkoeffizienten des Zählers des Ansatzes = " + coefficientsOfEnumeratorInPFDApproach.toString());

                coefficientsForPFD = getCoefficientsForPFD(coefficientsOfEnumeratorInPFDApproach, coefficientsForCompare, n);

                System.out.println("Koeffizienten für die PBZ:");
                for (int k = 0; k < coefficientsForPFD.length; k++) {
                    System.out.println("A_" + k + " = " + coefficientsForPFD[k].toString());
                }

            }

            for (int i = 0; i < n; i++) {
                approachForPFD = approachForPFD.replaceVariable(getPFDVariable(i), coefficientsForPFD[i].div(constantFactorInDenominator));
            }

            return polynomialPart.add(approachForPFD);

        } catch (PartialFractionDecompositionNotComputableException e) {
            return f;
        }

    }

    /**
     * Gibt zurück, ob f ein Quotient zweier Polynome in var ist.
     */
    public static boolean isRationalFunctionInCanonicalForm(Expression f, String var) {
        return f.isQuotient()
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getRight(), var);
    }

    private static Expression[] performPolynomialDivisionForFractionalDecomposition(Expression f, String var) throws EvaluationException, PartialFractionDecompositionNotComputableException {
        if (!isRationalFunctionInCanonicalForm(f, var)) {
            throw new PartialFractionDecompositionNotComputableException();
        }

        ExpressionCollection coefficientsEnumerator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getRight(), var);

        if (coefficientsEnumerator.getBound() >= coefficientsDenominator.getBound()) {
            ExpressionCollection[] resultOfPolynomialDivision = SimplifyPolynomialMethods.polynomialDivision(coefficientsEnumerator, coefficientsDenominator);
            return new Expression[]{SimplifyPolynomialMethods.getPolynomialFromCoefficients(resultOfPolynomialDivision[0], var), SimplifyPolynomialMethods.getPolynomialFromCoefficients(resultOfPolynomialDivision[1], var).div(
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
                    coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) factors.get(i)).getLeft(), var);
                } else {
                    coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(factors.get(i), var);
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
        int indexOfPFDVar = 0;

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

            for (int j = 1; j <= exponent; j++) {
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

    private static Expression[] getCoefficientsForPFD(ExpressionCollection coefficientsOfEnumeratorInPFDApproach,
            ExpressionCollection coefficientsInEnumeratorForCompare, int n) throws EvaluationException, PartialFractionDecompositionNotComputableException {

        // Matrix für das entsprechende Gleichungssystem bilden.
        Expression[][] matrixEntries = new Expression[n][n + 1];

        // Koeffizienten der entsprechenden Unbestimmten in der Partialbruchzerlegung eintragen.
        Expression currentCoefficientForCompare;
        for (int i = 0; i < n; i++) {
            if (coefficientsOfEnumeratorInPFDApproach.get(i) != null) {
                currentCoefficientForCompare = coefficientsOfEnumeratorInPFDApproach.get(i);
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
        /* 
         Zunächst eine Sicherheitsabfrage, ob alle Koeffizienten auf der 
         Diagonalen != 0 sind. Das sollte bei jeder ordentlichen Partialbruchzerlegung 
         der Fall sein.
         */
        for (int i = 0; i < n; i++) {
            if (matrix.getEntry(i, i).equals(ZERO)) {
                throw new PartialFractionDecompositionNotComputableException();
            }
        }

        Expression[] coefficientsForPFD = new Expression[n];
        Expression currentCoefficient;
        for (int i = n - 1; i >= 0; i--) {
            currentCoefficient = matrix.getEntry(i, n);
            for (int j = n - 1; j > i; j--) {
                currentCoefficient = currentCoefficient.sub(matrix.getEntry(i, j).mult(coefficientsForPFD[j]));
            }
            currentCoefficient = currentCoefficient.div(matrix.getEntry(i, i)).simplify();
            coefficientsForPFD[i] = currentCoefficient;
        }

        return coefficientsForPFD;

    }

    /**
     * Hilfsmethode: ermittelt (schnell) die Koeffizienten für die
     * Partialbruchzerlegung von f, falls der Nenner von f ein Produkt von
     * paarweise verschiedenen Linearfaktoren ist.
     *
     * @throws EvaluationException
     * @throws PartialFractionDecompositionNotComputableException
     */
    private static Expression[] getCoefficientsForPFDInSeparableCase(Expression f, Expression approachForPFD, String var) throws EvaluationException, PartialFractionDecompositionNotComputableException {

        if (!isDenominatorOfRationalFunctionInSeparableForm(f, var) || !isDenominatorOfRationalFunctionInSeparableForm(f, var)) {
            throw new PartialFractionDecompositionNotComputableException();
        }

        int n = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getRight(), var).intValue();
        Expression[] coefficientsForPFD = new Expression[n];
        ExpressionCollection summandsApproach = SimplifyUtilities.getSummands(approachForPFD);
        ExpressionCollection coefficientsDenominator;
        Expression summand, currentZero, currentCoefficient;
        String varName;
        int index;

        for (int i = 0; i < summandsApproach.getBound(); i++) {
            summand = summandsApproach.get(i);
            if (summand.isNotQuotient()) {
                throw new PartialFractionDecompositionNotComputableException();
            }
            if (!(((BinaryOperation) summand).getLeft() instanceof Variable)) {
                throw new PartialFractionDecompositionNotComputableException();
            }

            varName = ((Variable) ((BinaryOperation) summand).getLeft()).getName();

            try {
                index = Integer.parseInt(varName.substring(2, varName.length()));
                coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) summand).getRight(), var);
                currentZero = Expression.MINUS_ONE.mult(coefficientsDenominator.get(0)).div(coefficientsDenominator.get(1)).simplify();
                currentCoefficient = f.mult(((BinaryOperation) summand).getRight()).simplify().replaceVariable(var, currentZero).simplify();
                coefficientsForPFD[index] = currentCoefficient;
            } catch (Exception e) {
                throw new PartialFractionDecompositionNotComputableException();
            }

        }

        for (int i = 0; i < n; i++) {
            if (coefficientsForPFD[i] == null) {
                throw new PartialFractionDecompositionNotComputableException();
            }
        }

        return coefficientsForPFD;

    }

    private static boolean isDenominatorOfRationalFunctionInSeparableForm(Expression f, String var) {

        if (!isRationalFunctionInCanonicalForm(f, var)) {
            return false;
        }

        // Hier sind beide Grade >= 0.
        BigInteger degEnumerator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degDenominator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getRight(), var);

        if (degEnumerator.compareTo(degDenominator) >= 0) {
            return false;
        }

        // Nun wird geprüft, ob der Nenner in paarweise verschiedene Linearfaktoren zerlegt ist.
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(((BinaryOperation) f).getRight());
        BigInteger degOfFactor;

        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            degOfFactor = SimplifyPolynomialMethods.degreeOfPolynomial(factorsDenominator.get(i), var);
            if (degOfFactor.compareTo(BigInteger.ONE) > 0) {
                return false;
            }
            for (int j = i + 1; j < factorsDenominator.getBound(); j++) {
                if (factorsDenominator.get(i).equivalent(factorsDenominator.get(j))) {
                    return false;
                }
            }
        }

        return true;

    }

}
