package abstractexpressions.expression.commutativealgebra;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import exceptions.EvaluationException;
import java.util.ArrayList;
import java.util.Arrays;

public class GroebnerBasisMethods {

    /**
     * Konstanten, welche Standardtermordnungen repräsentieren.
     */
    public enum TermOrderings {

        LEX, DEGLEX, REVLEX, DEGREVLEX;
    }

    private static String[] monomialVars;
    private static TermOrderings termOrdering;

    public static class Monomial implements Comparable<Monomial> {

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

        @Override
        public String toString() {
            return this.toExpression().toString();
        }

        public boolean isZero() {
            return this.coefficient.equals(ZERO);
        }

        public Monomial multipliWithExpression(Expression expr) throws EvaluationException {
            return new Monomial(this.coefficient.mult(expr).simplify(), this.term);
        }

        public Monomial multiplyWithMonomial(Monomial m) throws EvaluationException {
            Expression resultCoefficient = this.coefficient.mult(m.coefficient).simplify();
            int[] resultTerm = new int[this.term.length];
            for (int i = 0; i < this.term.length; i++) {
                resultTerm[i] = this.term[i] + m.term[i];
            }
            return new Monomial(resultCoefficient, resultTerm);
        }

        public boolean isDivisibleByMonomial(Monomial m) {
            for (int i = 0; i < this.term.length; i++) {
                if (this.term[i] < m.term[i]) {
                    return false;
                }
            }
            return true;
        }

        public Monomial divideByExpression(Expression expr) throws EvaluationException {
            return new Monomial(this.coefficient.div(expr).simplify(), this.term);
        }

        public Monomial divideByMonomial(Monomial m) throws EvaluationException {
            int[] termOfQuotient = new int[this.term.length];
            for (int i = 0; i < this.term.length; i++) {
                termOfQuotient[i] = this.term[i] - m.term[i];
            }
            return new Monomial(this.coefficient.div(m.coefficient).simplify(), termOfQuotient);
        }

        public Monomial lcm(Monomial m) {
            int[] termOfLCM = new int[this.term.length];
            for (int i = 0; i < this.term.length; i++) {
                termOfLCM[i] = Math.max(this.term[i], m.term[i]);
            }
            return new Monomial(ONE, termOfLCM);
        }

        public boolean equalsInTerm(Monomial m) {
            for (int i = 0; i < this.term.length; i++) {
                if (this.term[i] != m.term[i]) {
                    return false;
                }
            }
            return true;
        }
        
        public boolean equalsToMonomial(Monomial m){
            return this.coefficient.equals(m.coefficient) && this.equalsInTerm(m);
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

    public static class MultiPolynomial {

        private final ArrayList<Monomial> monomials = new ArrayList<>();

        public MultiPolynomial() {
        }

        public MultiPolynomial(ArrayList<Monomial> monomials) {
            for (Monomial m : monomials){
                this.monomials.add(new Monomial(m.getCoefficient(), m.getTerm()));
            }
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
        
        public MultiPolynomial copy(){
            return new MultiPolynomial(this.monomials);
        }

        public Expression toExpression() {
            Expression multiPloynomialAsExpression = ZERO;
            for (Monomial m : this.monomials) {
                multiPloynomialAsExpression = multiPloynomialAsExpression.add(m.toExpression());
            }
            return multiPloynomialAsExpression;
        }

        @Override
        public String toString() {
            return this.toExpression().toString();
        }

        public void clearZeroMonomials() {
            for (int i = 0; i < this.monomials.size(); i++) {
                if (this.monomials.get(i) != null && this.monomials.get(i).getCoefficient().equals(ZERO)) {
                    this.monomials.remove(i);
                    i--;
                }
            }
        }

        public boolean isZero() {
            return this.monomials.isEmpty();
        }

        public boolean equalsToMultiPolynomial(MultiPolynomial f){
            if (this.monomials.size() != f.monomials.size()){
                return false;
            }
            for (int i = 0; i < this.monomials.size(); i++){
                if (!this.monomials.get(i).equalsToMonomial(f.monomials.get(i))){
                    return false;
                }
            }
            return true;
        }
        
        public MultiPolynomial multiplyWithMonomial(Monomial m) throws EvaluationException {
            MultiPolynomial resultPolynomial = new MultiPolynomial();
            for (Monomial monomial : this.monomials) {
                resultPolynomial.addMonomial(monomial.multiplyWithMonomial(m));
            }
            return resultPolynomial;
        }

        public MultiPolynomial add(MultiPolynomial f) throws EvaluationException {
            MultiPolynomial thisCopy = new MultiPolynomial(this.monomials);
            MultiPolynomial fCopy = new MultiPolynomial(f.monomials);
            for (int i = 0; i < thisCopy.monomials.size(); i++) {
                for (int j = 0; j < fCopy.monomials.size(); j++) {
                    if (thisCopy.monomials.get(i).equalsInTerm(fCopy.monomials.get(j))) {
                        thisCopy.monomials.get(i).setCoefficient(thisCopy.monomials.get(i).getCoefficient().add(fCopy.monomials.get(j).getCoefficient()).simplify());
                        thisCopy.monomials.remove(i);
                        fCopy.monomials.remove(j);
                        i--;
                        break;
                    }
                }
            }
            for (Monomial m : fCopy.monomials){
                thisCopy.monomials.add(m);
            }
            thisCopy.clearZeroMonomials();
            return thisCopy;
        }

        public MultiPolynomial sub(MultiPolynomial f) throws EvaluationException {
            MultiPolynomial thisCopy = new MultiPolynomial(this.monomials);
            MultiPolynomial fCopy = new MultiPolynomial(f.monomials);
            for (int i = 0; i < thisCopy.monomials.size(); i++) {
                for (int j = 0; j < fCopy.monomials.size(); j++) {
                    if (thisCopy.monomials.get(i).equalsInTerm(fCopy.monomials.get(j))) {
                        thisCopy.monomials.get(i).setCoefficient(thisCopy.monomials.get(i).getCoefficient().sub(fCopy.monomials.get(j).getCoefficient()).simplify());
                        thisCopy.monomials.remove(i);
                        fCopy.monomials.remove(j);
                        i--;
                        break;
                    }
                }
            }
            for (Monomial m : fCopy.monomials){
                thisCopy.monomials.add(m.multipliWithExpression(MINUS_ONE));
            }
            thisCopy.clearZeroMonomials();
            return thisCopy;
        }

        public Monomial getLeadingMonomial() {
            Monomial maxMonomial = null;
            for (Monomial m : this.monomials) {
                if (maxMonomial == null) {
                    maxMonomial = m;
                } else if (maxMonomial.compareTo(m) < 0) {
                    maxMonomial = m;
                }
            }
            return maxMonomial;
        }

        public void normalize() throws EvaluationException {
            Expression leadingCoefficient = getLeadingMonomial().getCoefficient();
            Monomial m;
            for (int i = 0; i < this.monomials.size(); i++) {
                m = this.monomials.get(i);
                this.monomials.remove(i);
                m = m.divideByExpression(leadingCoefficient);
                this.monomials.add(i, m);
            }
        }

    }

    public static void setTermOrdering(TermOrderings termOrdering) {
        GroebnerBasisMethods.termOrdering = termOrdering;
    }

    public String[] getMonomialVars() {
        return monomialVars;
    }

    public static void setMonomialVars(String[] monomialVars) {
        GroebnerBasisMethods.monomialVars = monomialVars;
    }

    public static MultiPolynomial getSyzygyPolynomial(MultiPolynomial f, MultiPolynomial g) throws EvaluationException {
        f.normalize();
        g.normalize();
        Monomial leadingMonomialOfF = f.getLeadingMonomial();
        Monomial leadingMonomialOfG = g.getLeadingMonomial();
        Monomial lcmOfLeadingMonomials = leadingMonomialOfF.lcm(leadingMonomialOfG);
        MultiPolynomial syzygyPolynomial = f.multiplyWithMonomial(lcmOfLeadingMonomials.divideByMonomial(leadingMonomialOfF)).sub(
                g.multiplyWithMonomial(lcmOfLeadingMonomials.divideByMonomial(leadingMonomialOfG)));
        syzygyPolynomial.clearZeroMonomials();
        return syzygyPolynomial;
    }

    public static MultiPolynomial reduce(MultiPolynomial f, MultiPolynomial reductionPolynomial) throws EvaluationException {
        
        Monomial leadingMonomial = reductionPolynomial.getLeadingMonomial();
        MultiPolynomial reducedPolynomial = f;
        
        boolean reductionPerformed;
        do {
            reductionPerformed = false;
            ArrayList<Monomial> monomialsOfF = new ArrayList<>();
            for (Monomial m : reducedPolynomial.getMonomials()){
                monomialsOfF.add(m);
            }
            for (Monomial m : monomialsOfF){
                if (m.isDivisibleByMonomial(leadingMonomial)){
                    reducedPolynomial = reducedPolynomial.sub(reductionPolynomial.multiplyWithMonomial(m.divideByMonomial(leadingMonomial)));
                    reductionPerformed = true;
                }
            }
        } while (reductionPerformed);
        
        return reducedPolynomial;
        
    }

}
