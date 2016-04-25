package abstractexpressions.expression.equation;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods.MultiPolynomial;
import abstractexpressions.expression.utilities.SimplifyMultiPolynomialMethods;
import exceptions.EvaluationException;
import exceptions.NotAlgebraicallySolvableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class SolveGeneralSystemOfEquationsMethods {

    private static boolean containsSet(HashSet<String> varsToBeContained, HashSet<String> vars) {
        for (String var : varsToBeContained) {
            if (!vars.contains(var)) {
                return false;
            }
        }
        return true;
    }
    
    private static HashSet<String> getIndeterminatesInEquation(Expression equation, ArrayList<String> vars){
        HashSet<String> allVarsInEquation = equation.getContainedIndeterminates();
        HashSet<String> varsInEquation = new HashSet<>();
        for (String var : allVarsInEquation){
            if (vars.contains(var)){
                varsInEquation.add(var);
            }
        }
        return varsInEquation;
    }

    private static HashSet<String> getIndeterminatesInEquation(MultiPolynomial equation, ArrayList<String> vars){
        HashSet<String> allVarsInEquation = equation.toExpression().getContainedIndeterminates();
        HashSet<String> varsInEquation = new HashSet<>();
        for (String var : allVarsInEquation){
            if (vars.contains(var)){
                varsInEquation.add(var);
            }
        }
        return varsInEquation;
    }

    private static boolean isSystemInTriangularForm(Expression[] equations, ArrayList<String> vars) {

        if (equations.length != vars.size()) {
            return false;
        }

        // Zunächst: für jede Gleichung Variablen aus vars ermitteln, die darin auftauchen.
        ArrayList<HashSet<String>> varSets = new ArrayList<>();
        for (Expression f : equations) {
            varSets.add(getIndeterminatesInEquation(f, vars));
        }
        // Nun: prüfen, ob die Mengen in varSets eine strikt aufsteigende Kette bilden.
        boolean[] sizes = new boolean[vars.size()];
        for (HashSet<String> varSet : varSets) {
            sizes[varSet.size() - 1] = true;
        }

        /* 
         Prüfen, ob unter den Variablenmengen zu jedem i = 1, 2, ..., vars.size()
         eine Menge mit Mächtigkeit i auftaucht.
         */
        for (boolean size : sizes) {
            if (!size) {
                return false;
            }
        }

        // Auf strikte Aufsteigung prüfen.
        for (int i = 0; i < vars.size() - 1; i++) {
            for (HashSet<String> varSet : varSets) {
                if (varSet.size() != i + 1) {
                    continue;
                }
                for (HashSet<String> biggerVarSet : varSets) {
                    if (biggerVarSet.size() != i + 2) {
                        continue;
                    }
                    if (!containsSet(varSet, biggerVarSet)) {
                        return false;
                    }
                }
            }
        }

        return true;

    }

    public static ArrayList<Expression[]> solveTriangularPolynomialSystemOfEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Prüfung, ob alle Gleichungen Polynome sind.
        for (Expression equation : equations) {
            if (SimplifyMultiPolynomialMethods.isMultiPolynomial(equation, new ArrayList(Arrays.asList(vars)))) {
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

        ArrayList<MultiPolynomial> groebnerBasis;
        try {
            groebnerBasis = GroebnerBasisMethods.getNormalizedReducedGroebnerBasis(polynomials);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        if (!isSystemInTriangularForm(equations, vars)) {
            throw new NotAlgebraicallySolvableException();
        }

        // Das Gleichungssystem ist nun in Dreiecksform. Nun kann es rekursiv gelöst werden.
        ArrayList<HashMap<String, Expression>> solutions = solveTriangularPolynomialSystemOfEquations(groebnerBasis, vars);
        
        // Die Lösungstupel in Arrays verwandeln und ausgeben.
        ArrayList<Expression[]> orderedSolutions = new ArrayList<>();
        Expression[] orderedSolution;
        for(HashMap<String, Expression> solution : solutions){
            orderedSolution = new Expression[solution.size()];
            for (String var : solution.keySet()){
                orderedSolution[vars.indexOf(var)] = solution.get(var);
            }
            orderedSolutions.add(orderedSolution);
        }
        
        return orderedSolutions;

    }

    private static ArrayList<HashMap<String, Expression>> solveTriangularPolynomialSystemOfEquations(ArrayList<MultiPolynomial> equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {
        
        HashSet<String> varsInEquation = null;
        MultiPolynomial equation = null;
        for (MultiPolynomial f : equations){
            varsInEquation = getIndeterminatesInEquation(f, vars);
            if (varsInEquation.size() == 1){
                equation = f;
                equations.remove(equation);
                break;
            }
        }
        
        if (varsInEquation == null || equation == null){
            throw new NotAlgebraicallySolvableException();
        }
        
        String var = null;
        for (String v : varsInEquation){
            var = v;
        }
        
        if (var == null){
            throw new NotAlgebraicallySolvableException();
        }
        
        
        
        
        return null;
        
    }
    
    public static ArrayList<ArrayList<Expression>> solveGeneralTriangularSystemOfEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        if (!isSystemInTriangularForm(equations, vars)) {
            throw new NotAlgebraicallySolvableException();
        }

        // Das Gleichungssystem ist nun in Dreiecksform. Nun kann es rekursiv gelöst werden.
        ArrayList<ArrayList<Expression>> solutions = new ArrayList<>();

        return solutions;

    }

}
