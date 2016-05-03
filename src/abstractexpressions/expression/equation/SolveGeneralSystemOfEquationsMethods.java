package abstractexpressions.expression.equation;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods.MultiPolynomial;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyMultiPolynomialMethods;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.computation.GaussAlgorithm;
import exceptions.EvaluationException;
import exceptions.NotAlgebraicallySolvableException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class SolveGeneralSystemOfEquationsMethods {

    /**
     * Konstanten, die aussagen, ob ein Gleichungssystem keine Lösungen besitzt
     * oder ob es definitiv alle reellen Zahlentupel als Lösungen besitzt.
     */
    public static final ArrayList<Expression[]> ALL_REALS = new ArrayList<>();
    public static final ArrayList<Expression[]> NO_SOLUTIONS = new ArrayList<>();

    private static HashSet<String> getIndeterminatesInEquation(Expression equation, ArrayList<String> vars) {
        HashSet<String> allVarsInEquation = equation.getContainedIndeterminates();
        HashSet<String> varsInEquation = new HashSet<>();
        for (String var : allVarsInEquation) {
            if (vars.contains(var)) {
                varsInEquation.add(var);
            }
        }
        return varsInEquation;
    }

    private static HashSet<String> getIndeterminatesInEquation(MultiPolynomial equation, ArrayList<String> vars) {
        HashSet<String> allVarsInEquation = equation.toExpression().getContainedIndeterminates();
        HashSet<String> varsInEquation = new HashSet<>();
        for (String var : allVarsInEquation) {
            if (vars.contains(var)) {
                varsInEquation.add(var);
            }
        }
        return varsInEquation;
    }

    private static boolean isSystemPolynomial(Expression[] equations, ArrayList<String> vars) {
        for (Expression equation : equations) {
            if (!SimplifyMultiPolynomialMethods.isMultiPolynomial(equation, vars)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSystemLinear(Expression[] equations, ArrayList<String> vars) {
        if (!isSystemPolynomial(equations, vars)) {
            return false;
        }
        // Prüfen, ob alle Grade <= 1 sind.
        for (Expression equation : equations) {
            if (SimplifyMultiPolynomialMethods.getDegreeOfMultiPolynomial(equation, vars).compareTo(BigInteger.ONE) > 0) {
                return false;
            }
        }
        return true;
    }

    public static Expression[] solveLinearSystemOfEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Prüfung, ob alle Gleichungen linear in den angegebenen Variablen sind.
        if (!isSystemLinear(equations, vars)) {
            throw new NotAlgebraicallySolvableException();
        }

        Expression[][] matrixEntries = new Expression[equations.length][vars.size()];
        Expression[] vectorEntries = new Expression[equations.length];

        try {
            for (int i = 0; i < equations.length; i++) {
                for (int j = 0; j < vars.size(); j++) {
                    matrixEntries[i][j] = equations[i].diff(vars.get(j)).simplify();
                }
            }

            // Alle Variablen durch 0 ersetzen.
            for (int i = 0; i < equations.length; i++) {
                vectorEntries[i] = equations[i];
                for (String solutionVar : vars) {
                    vectorEntries[i] = vectorEntries[i].replaceVariable(solutionVar, ZERO);
                }
                vectorEntries[i] = MINUS_ONE.mult(vectorEntries[i]).simplify();
            }

            Matrix m = new Matrix(matrixEntries);
            Matrix b = new Matrix(vectorEntries);

            Expression[] solutions = GaussAlgorithm.solveLinearSystemOfEquations(m, b);
            return solutions;
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

    }

    public static ArrayList<Expression[]> solvePolynomialSystemOfEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        if (equations.length != vars.size()) {
            throw new NotAlgebraicallySolvableException();
        }

        // Prüfung, ob alle Gleichungen Polynome sind.
        for (Expression equation : equations) {
            if (!SimplifyMultiPolynomialMethods.isMultiPolynomial(equation, vars)) {
                throw new NotAlgebraicallySolvableException();
            }
        }

        ArrayList<MultiPolynomial> polynomials;
        try {
            polynomials = SimplifyMultiPolynomialMethods.getMultiPolynomialsFromExpressions(equations, vars);
            if (polynomials.size() < equations.length) {
                throw new NotAlgebraicallySolvableException();
            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        ArrayList<MultiPolynomial> groebnerBasis;
        try {
            groebnerBasis = GroebnerBasisMethods.getNormalizedReducedGroebnerBasis(polynomials);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        ArrayList<String> varsCopy = new ArrayList<>(vars);
        ArrayList<HashMap<String, Expression>> solutions = solveTriangularPolynomialSystemOfEquations(groebnerBasis, varsCopy);

        // Die Lösungstupel in Arrays verwandeln und ausgeben.
        ArrayList<Expression[]> orderedSolutions = new ArrayList<>();
        Expression[] orderedSolution;
        for (HashMap<String, Expression> solution : solutions) {
            orderedSolution = new Expression[solution.size()];
            for (String var : solution.keySet()) {
                orderedSolution[vars.indexOf(var)] = solution.get(var);
            }
            orderedSolutions.add(orderedSolution);
        }

        return orderedSolutions;

    }

    private static ArrayList<HashMap<String, Expression>> solveTriangularPolynomialSystemOfEquations(ArrayList<MultiPolynomial> equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Dann ist es nur eine Gleichung in einer Variablen.
        if (vars.size() == 1) {

            String var = null;
            for (String v : vars) {
                var = v;
            }
            if (var == null) {
                throw new NotAlgebraicallySolvableException();
            }

            ExpressionCollection solutions;
            try {
                solutions = SolveGeneralEquationMethods.solveEquation(equations.get(0).toExpression(), ZERO, var);
            } catch (EvaluationException e) {
                throw new NotAlgebraicallySolvableException();
            }

            ArrayList<HashMap<String, Expression>> solutionsOfSystem = new ArrayList<>();
            HashMap<String, Expression> solutionOfSystem;
            for (Expression solution : solutions) {
                solutionOfSystem = new HashMap<>();
                solutionOfSystem.put(var, solution);
                solutionsOfSystem.add(solutionOfSystem);
            }

            return solutionsOfSystem;

        }

        String[] monomialVars = new String[vars.size()];
        for (int i = 0; i < vars.size(); i++) {
            monomialVars[i] = vars.get(i);
        }
        GroebnerBasisMethods.setMonomialVars(vars.toArray(monomialVars));

        HashSet<String> varsInEquation;
        MultiPolynomial equation = null;
        String var = null;
        for (MultiPolynomial f : equations) {
            varsInEquation = getIndeterminatesInEquation(f, vars);
            if (varsInEquation.size() == 1) {
                equation = f;
                for (String v : varsInEquation) {
                    var = v;
                }
                equations.remove(equation);
                break;
            }
        }

        if (equation == null) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection coefficientsOfEquation = equation.toPolynomial(var);

        ExpressionCollection solutionsOfEquation;
        try {
            solutionsOfEquation = PolynomialRootsMethods.solvePolynomialEquation(coefficientsOfEquation, var);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        vars.remove(var);
        ArrayList<MultiPolynomial> equationsWithVarReplacedBySolution = new ArrayList<>();
        ArrayList<HashMap<String, Expression>> solutions = new ArrayList<>();

        // Jede gefundene Lösung in die restlichen Gleichungen einsetzen und weiter rekursiv auflösen.
        try {
            for (Expression solutionOfEquation : solutionsOfEquation) {

                equationsWithVarReplacedBySolution.clear();

                for (MultiPolynomial f : equations) {
                    equationsWithVarReplacedBySolution.add(f.replaceVarByExpression(var, solutionOfEquation).simplify());
                }

                ArrayList<HashMap<String, Expression>> solutionsOfReducedPolynomialSystem = solveTriangularPolynomialSystemOfEquations(equationsWithVarReplacedBySolution, vars);

                HashMap<String, Expression> singleSolution;
                for (HashMap<String, Expression> solutionOfReducedPolynomialSystem : solutionsOfReducedPolynomialSystem) {
                    singleSolution = new HashMap<>();
                    for (String v : solutionOfReducedPolynomialSystem.keySet()) {
                        singleSolution.put(v, solutionOfReducedPolynomialSystem.get(v));
                    }
                    singleSolution.put(var, solutionOfEquation);
                    solutions.add(singleSolution);
                }

            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        return solutions;

    }

    public static ArrayList<Expression[]> solveGeneralSystemOfEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Triviale Gleichungen beseitigen.
        try {
            equations = removeTrivialEquations(equations, vars);
        } catch (NotAlgebraicallySolvableException e) {
            return NO_SOLUTIONS;
        }
        // Redundante Gleichungen beseitigen.
        equations = removeRedundantEquations(equations, vars);
        
        if (equations.length == 1 && equations[0].equals(ZERO)){
            return ALL_REALS;
        }
        
        if (equations.length != vars.size()) {
            throw new NotAlgebraicallySolvableException();
        }

        ArrayList<String> varsCopy = new ArrayList<>(vars);
        ArrayList<HashMap<String, Expression>> solutions = solveTriangularGeneralSystemOfEquations(new ArrayList<>(Arrays.asList(equations)), varsCopy);

        // Die Lösungstupel in Arrays verwandeln und ausgeben.
        ArrayList<Expression[]> orderedSolutions = new ArrayList<>();
        Expression[] orderedSolution;
        for (HashMap<String, Expression> solution : solutions) {
            orderedSolution = new Expression[solution.size()];
            for (String var : solution.keySet()) {
                orderedSolution[vars.indexOf(var)] = solution.get(var);
            }
            orderedSolutions.add(orderedSolution);
        }

        return orderedSolutions;

    }

    private static boolean doesExpressionContainSomeVar(Expression f, ArrayList<String> vars) {
        for (String var : vars) {
            if (f.contains(var)) {
                return true;
            }
        }
        return false;
    }

    private static Expression[] removeTrivialEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {
        ArrayList<Expression> nonTrivialEquations = new ArrayList<>();
        for (Expression equation : equations) {
            if (!equation.equals(ZERO)) {
                nonTrivialEquations.add(equation);
            } else if (!doesExpressionContainSomeVar(equation, vars) && !equation.equals(ZERO)) {
                throw new NotAlgebraicallySolvableException();
            }
        }
        Expression[] nonTrivialEquationsAsArray = new Expression[nonTrivialEquations.size()];
        return nonTrivialEquations.toArray(nonTrivialEquationsAsArray);
    }

    private static Expression[] removeRedundantEquations(Expression[] equations, ArrayList<String> vars) {

        ArrayList<Expression> nonTrivialEquations = new ArrayList<>();
        Expression quotient;
        ArrayList<Integer> indicesWithRedundantEquations = new ArrayList<>();
        for (int i = 0; i < equations.length; i++) {
            if (indicesWithRedundantEquations.contains(i)) {
                continue;
            }
            for (int j = i + 1; j < equations.length; j++) {
                try {
                    quotient = equations[i].div(equations[j]).simplify();
                    if (!doesExpressionContainSomeVar(quotient, vars)) {
                        indicesWithRedundantEquations.add(j);
                    }
                } catch (EvaluationException e) {
                    // Nichts tun.
                }
            }
        }

        for (int i = 0; i < equations.length; i++) {
            if (!indicesWithRedundantEquations.contains(i)) {
                nonTrivialEquations.add(equations[i]);
            }
        }

        Expression[] nonTrivialEquationsAsArray = new Expression[nonTrivialEquations.size()];
        return nonTrivialEquations.toArray(nonTrivialEquationsAsArray);

    }

    private static ArrayList<HashMap<String, Expression>> solveTriangularGeneralSystemOfEquations(ArrayList<Expression> equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Dann ist es nur eine Gleichung in einer Variablen.
        if (vars.size() == 1) {

            String var = null;
            for (String v : vars) {
                var = v;
            }
            if (var == null) {
                throw new NotAlgebraicallySolvableException();
            }

            ExpressionCollection solutions;
            try {
                solutions = SolveGeneralEquationMethods.solveEquation(equations.get(0), ZERO, var);
            } catch (EvaluationException e) {
                throw new NotAlgebraicallySolvableException();
            }

            ArrayList<HashMap<String, Expression>> solutionsOfSystem = new ArrayList<>();
            HashMap<String, Expression> solutionOfSystem;
            for (Expression solution : solutions) {
                solutionOfSystem = new HashMap<>();
                solutionOfSystem.put(var, solution);
                solutionsOfSystem.add(solutionOfSystem);
            }

            return solutionsOfSystem;

        }

        String[] monomialVars = new String[vars.size()];
        for (int i = 0; i < vars.size(); i++) {
            monomialVars[i] = vars.get(i);
        }
        GroebnerBasisMethods.setMonomialVars(vars.toArray(monomialVars));

        HashSet<String> varsInEquation;
        Expression equation = null;
        String var = null;
        for (Expression f : equations) {
            varsInEquation = getIndeterminatesInEquation(f, vars);
            if (varsInEquation.size() == 1) {
                equation = f;
                for (String v : varsInEquation) {
                    var = v;
                }
                equations.remove(equation);
                break;
            }
        }

        if (equation == null) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection solutionsOfEquation;
        try {
            solutionsOfEquation = SolveGeneralEquationMethods.solveEquation(equation, ZERO, var);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        vars.remove(var);
        ArrayList<Expression> equationsWithVarReplacedBySolution = new ArrayList<>();
        ArrayList<HashMap<String, Expression>> solutions = new ArrayList<>();

        // Jede gefundene Lösung in die restlichen Gleichungen einsetzen und weiter rekursiv auflösen.
        try {
            ArrayList<String> varsCopy = new ArrayList<>();
            for (Expression solutionOfEquation : solutionsOfEquation) {

                // vars in varsCopy kopieren.
                varsCopy.clear();
                varsCopy.addAll(vars);

                equationsWithVarReplacedBySolution.clear();

                for (Expression f : equations) {
                    equationsWithVarReplacedBySolution.add(f.replaceVariable(var, solutionOfEquation).simplify());
                }

                ArrayList<HashMap<String, Expression>> solutionsOfReducedPolynomialSystem = solveTriangularGeneralSystemOfEquations(equationsWithVarReplacedBySolution, varsCopy);

                HashMap<String, Expression> singleSolution;
                for (HashMap<String, Expression> solutionOfReducedPolynomialSystem : solutionsOfReducedPolynomialSystem) {
                    singleSolution = new HashMap<>();
                    for (String v : solutionOfReducedPolynomialSystem.keySet()) {
                        singleSolution.put(v, solutionOfReducedPolynomialSystem.get(v));
                    }
                    singleSolution.put(var, solutionOfEquation);
                    solutions.add(singleSolution);
                }

            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        return solutions;

    }

    public static ArrayList<Expression[]> solveSystemOfEquations(Expression[] equations, ArrayList<String> vars) {

        // Typ: lineares Gleichungssystem.
        ArrayList<Expression[]> solutions = new ArrayList<>();
        try {
            Expression[] solution = solveLinearSystemOfEquations(equations, vars);
            if (solution == GaussAlgorithm.NO_SOLUTIONS){
                return NO_SOLUTIONS;
            }
            solutions.add(solution);
            return solutions;
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Typ: polynomiales Gleichungssystem.
        try {
            return solvePolynomialSystemOfEquations(equations, vars);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Typ: allgemeines Gleichunggsystem in Dreiecksform.
        try {
            return solveGeneralSystemOfEquations(equations, vars);
        } catch (NotAlgebraicallySolvableException e) {
        }

        return new ArrayList<>();

    }

}
