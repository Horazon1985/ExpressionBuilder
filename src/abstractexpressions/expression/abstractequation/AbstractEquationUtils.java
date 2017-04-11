package abstractexpressions.expression.abstractequation;

import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyUtilities;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import exceptions.EvaluationException;
import notations.NotationLoader;

/**
 * Util-Klasse für allgemeine Hilfsmethoden zum Lösen abstrakter Gleichungen 
 * (gewöhnliche Gleichungen, Differentialgleichungen, ...).
 */
public class AbstractEquationUtils {

    /**
     * In f sind Variablen enthalten, unter anderem "Parametervariablen" K_1,
     * K_2, .... Diese Funktion liefert dasjenige K_i, welches in h noch nicht
     * vorkommt und welches den kleinsten Index i besitzt.
     */
    public static String getParameterVariable(Expression f) {
        String var = NotationLoader.FREE_INTEGER_PARAMETER_VAR + "_";
        int j = 1;
        while (f.contains(var + j)) {
            j++;
        }
        return var + j;
    }
    
    /**
     * Hilfsmethode. Liefert alle gemeinsamen Faktoren (mit Vielfachheiten) von
     * f und g.
     */
    public static ExpressionCollection getCommonFactors(Expression f, Expression g) {

        ExpressionCollection factorsF, factorsG;

        if (f.isQuotient()) {
            factorsF = SimplifyUtilities.getFactorsOfNumeratorInExpression(((BinaryOperation) f).getLeft());
        } else {
            factorsF = SimplifyUtilities.getFactors(f);
        }

        if (g.isQuotient()) {
            factorsG = SimplifyUtilities.getFactorsOfNumeratorInExpression(((BinaryOperation) g).getLeft());
        } else {
            factorsG = SimplifyUtilities.getFactors(g);
        }

        ExpressionCollection factorsFCopy = factorsF.copy();
        ExpressionCollection factorsGCopy = factorsG.copy();

        try {

            /*
             Idee: Falls f und g Faktoren der Form h^m und h^n besitzen, wobei
             m und n rationale Zahlen sind, so wird h^(min(m, n)) zur Menge
             der gemeinsamen Faktoren hinzugefügt. Andernfalls werden Faktoren
             nur dann hinzugefügt, wenn sie äquivalent sind.
             */
            ExpressionCollection commonFactors = new ExpressionCollection();
            Expression base, baseToCompare, exponent, exponentToCompare, exponentDifference, exponentOfCommonFactor;

            for (int i = 0; i < factorsF.getBound(); i++) {
                if (factorsF.get(i) == null) {
                    continue;
                }

                if (factorsF.get(i).isPower() && ((BinaryOperation) factorsF.get(i)).getRight().isIntegerConstantOrRationalConstant()) {
                    base = ((BinaryOperation) factorsF.get(i)).getLeft();
                    exponent = ((BinaryOperation) factorsF.get(i)).getRight();
                } else {
                    base = factorsF.get(i);
                    exponent = ONE;
                }

                for (int j = 0; j < factorsG.getBound(); j++) {
                    if (factorsG.get(j) == null) {
                        continue;
                    }

                    if (factorsG.get(j).isPower() && ((BinaryOperation) factorsG.get(j)).getRight().isIntegerConstantOrRationalConstant()) {
                        baseToCompare = ((BinaryOperation) factorsG.get(j)).getLeft();
                        exponentToCompare = ((BinaryOperation) factorsG.get(j)).getRight();
                    } else {
                        baseToCompare = factorsG.get(j);
                        exponentToCompare = ONE;
                    }

                    if (base.equivalent(baseToCompare)) {
                        exponentDifference = exponent.sub(exponentToCompare).simplify();
                        if (exponentDifference.isNonNegative()) {
                            exponentOfCommonFactor = exponentToCompare;
                        } else {
                            exponentOfCommonFactor = exponent;
                        }
                        if (exponentOfCommonFactor.equals(ONE)) {
                            commonFactors.add(base);
                        } else {
                            commonFactors.add(base.pow(exponentOfCommonFactor));
                        }
                        factorsF.remove(i);
                        factorsG.remove(j);
                        break;
                    }

                }
            }

            return commonFactors;

        } catch (EvaluationException e) {
        }

        return SimplifyUtilities.intersection(factorsFCopy, factorsGCopy);

    }

}
