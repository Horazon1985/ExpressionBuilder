package abstractexpressions.expression.commutativealgebra;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import exceptions.EvaluationException;
import java.util.ArrayList;
import java.util.Arrays;

public class GroebnerBasisMethods {

    /**
     * Konstanten, welche Standardtermordnungen repräsentieren.
     */
    private enum TermOrderings {

        LEX, DEGLEX, REVLEX, DEGREVLEX;
    }

    private static String[] monomialVars;
    private static TermOrderings termOrdering;

    public class Monomial implements Comparable<Monomial> {

        private Expression coefficient;
        private int[] term;

        public Monomial(Expression coefficient, int[] term) {
            this.coefficient = coefficient;
            this.term = new int[monomialVars.length];
            /*
             Damit es keine Dimensionsfehler gibt: wenn term und this.term in den
             Dimensionen nicht übereinstimmen (warum auch immer), wird nur bis
             zur gemeinsamen Schranke kopiert und der Rest (falls notwendig) mit 
             Nullen aufgefüllt.
             */
            System.arraycopy(term, 0, this.term, 0, Math.min(monomialVars.length, term.length));
            for (int i = Math.min(monomialVars.length, term.length); i < this.term.length; i++) {
                this.term[i] = 0;
            }
        }

        public Expression getCoefficient() {
            return coefficient;
        }

        public void setCoefficient(Expression coefficient) {
            this.coefficient = coefficient;
        }

        public int[] getTerm() {
            return term;
        }

        public void setTerm(int[] term) {
            this.term = term;
        }

        public int getDegree() {
            int degree = 0;
            for (int exponent : term) {
                degree += exponent;
            }
            return degree;
        }

        public Expression toExpression() {
            Expression monomialAsExpression = coefficient;
            for (int i = 0; i < this.term.length; i++) {
                monomialAsExpression = monomialAsExpression.mult(Variable.create(monomialVars[i]).pow(this.term[i]));
            }
            return monomialAsExpression;
        }

        public boolean isZero() {
            return this.coefficient.equals(ZERO);
        }

        public Monomial multiplyWithMonomial(Monomial m) throws EvaluationException {
            Expression resultCoefficient = this.coefficient.mult(m.coefficient).simplify();
            int[] resultTerm = new int[this.term.length];
            for (int i = 0; i < this.term.length; i++) {
                resultTerm[i] = this.term[i] + m.term[i];
            }
            return new Monomial(resultCoefficient, resultTerm);
        }

        private boolean isDivisibleByMonomial(Monomial m){
            for (int i = 0; i < this.term.length; i++){
                if (this.term[i] < m.term[i]){
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public int compareTo(Monomial m) {
            if (termOrdering == TermOrderings.LEX) {
                return compateToWithRespectToLex(m);
            }
            if (termOrdering == TermOrderings.DEGLEX) {
                return compateToWithRespectToDegLex(m);
            }
            if (termOrdering == TermOrderings.REVLEX) {
                return compateToWithRespectToRevLex(m);
            }
            if (termOrdering == TermOrderings.DEGREVLEX) {
                return compateToWithRespectToDegRevLex(m);
            }
            return 0;
        }

        private int compateToWithRespectToLex(Monomial m) {
            for (int i = 0; i < this.term.length; i++) {
                if (this.term[i] < m.term[i]) {
                    return -1;
                }
                if (this.term[i] > m.term[i]) {
                    return 1;
                }
            }
            return 0;
        }

        private int compateToWithRespectToDegLex(Monomial m) {
            if (this.getDegree() > m.getDegree()) {
                return 1;
            }
            if (this.getDegree() < m.getDegree()) {
                return -1;
            }
            return compateToWithRespectToLex(m);
        }

        private int compateToWithRespectToRevLex(Monomial m) {
            for (int i = this.term.length - 1; i >= 0; i++) {
                if (this.term[i] < m.term[i]) {
                    return 1;
                }
                if (this.term[i] > m.term[i]) {
                    return -1;
                }
            }
            return 0;
        }

        private int compateToWithRespectToDegRevLex(Monomial m) {
            if (this.getDegree() > m.getDegree()) {
                return 1;
            }
            if (this.getDegree() < m.getDegree()) {
                return -1;
            }
            return compateToWithRespectToRevLex(m);
        }

    }

    public class MultiPolynomial {

        private final ArrayList<Monomial> monomials = new ArrayList<>();

        public MultiPolynomial() {
        }

        public MultiPolynomial(ArrayList<Monomial> monomials) {
            this.monomials.addAll(monomials);
        }

        public MultiPolynomial(Monomial... monomials) {
            this.monomials.addAll(Arrays.asList(monomials));
        }

        public ArrayList<Monomial> getMonomials() {
            return this.monomials;
        }

        public void addMonomial(Monomial m) {
            this.monomials.add(m);
        }

        public Expression toExpression() {
            Expression multiPloynomialAsExpression = ZERO;
            for (Monomial m : this.monomials) {
                multiPloynomialAsExpression = multiPloynomialAsExpression.add(m.toExpression());
            }
            return multiPloynomialAsExpression;
        }

        public void clearZeroMonomials() {
            for (int i = 0; i < this.monomials.size(); i++){
                if (this.monomials.get(i) != null && this.monomials.get(i).getCoefficient().equals(ZERO)){
                    this.monomials.remove(i);
                }
            }
        }

        public boolean isZero() {
            return this.monomials.isEmpty();
        }

        public MultiPolynomial multiplyWithMonomial(Monomial m) throws EvaluationException {
            MultiPolynomial resultPolynomial = new MultiPolynomial();
            for (Monomial monomial : this.monomials) {
                resultPolynomial.addMonomial(monomial.multiplyWithMonomial(m));
            }
            return resultPolynomial;
        }

    }

    public String[] getMonomialVars() {
        return monomialVars;
    }

    public void setMonomialVars(String[] monomialVars) {
        GroebnerBasisMethods.monomialVars = monomialVars;
    }

    private static Monomial getMaximalMonomial(ArrayList<Monomial> monomials) {
        Monomial maxMonomial = null;
        for (Monomial m : monomials) {
            if (maxMonomial == null) {
                maxMonomial = m;
            } else if (maxMonomial.compareTo(m) < 0) {
                maxMonomial = m;
            }
        }
        return maxMonomial;
    }

    private static MultiPolynomial getSyzygyPolynomial(MultiPolynomial f, MultiPolynomial g) {

        return null;
    }

}
