package abstractexpressions.expression.commutativealgebra;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.basic.ExpressionCollection;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GroebnerBasisUtils {

    private static final Set<TypeSimplify> simplifyTypesBuchbergerAlgorithmRationalCase = new HashSet<>();

    static {
        simplifyTypesBuchbergerAlgorithmRationalCase.add(TypeSimplify.order_difference_and_division);
        simplifyTypesBuchbergerAlgorithmRationalCase.add(TypeSimplify.order_sums_and_products);
        simplifyTypesBuchbergerAlgorithmRationalCase.add(TypeSimplify.simplify_basic);
    }

    /**
     * Vereinfachungsmodus für den Buchberger-Algorithmus.
     */
    public enum SimplifyCase {
        GENERAL_CASE, RATIONAL_CASE;
    }

    /**
     * Konstanten, welche Standardtermordnungen repräsentieren.
     */
    public enum TermOrderings {
        LEX, DEGLEX, REVLEX, DEGREVLEX;
    }

    private static String[] monomialVars;
    // Default-Einstellung soll GENERAL_CASE sein!
    private static SimplifyCase simplifyCase = SimplifyCase.GENERAL_CASE;
    // Default-Einstellung soll LEX sein!
    private static TermOrderings termOrdering = TermOrderings.LEX;

    public static class Monomial implements Comparable<Monomial> {

        private Expression coefficient;
        private int[] term;

        public Monomial(Expression coefficient, int[] term) {
            this.coefficient = coefficient;
            this.term = new int[monomialVars.length];
            /*
             Damit es keine Dimensionsfehler gibt: wenn term größere Länge besitzt als monomialVars, 
             wird nur bis zur Länge von monomialVars kopiert. Der Rest wird mit Nullen aufgefüllt.
             */
            System.arraycopy(term, 0, this.term, 0, Math.min(monomialVars.length, term.length));
            for (int i = Math.min(monomialVars.length, term.length); i < monomialVars.length; i++) {
                this.term[i] = 0;
            }
        }

        public Monomial(Expression coefficient, Integer... term) {
            this.coefficient = coefficient;
            this.term = new int[monomialVars.length];
            /*
             Damit es keine Dimensionsfehler gibt: wenn term größere Länge besitzt als monomialVars, 
             wird nur bis zur Länge von monomialVars kopiert. Der Rest wird mit Nullen aufgefüllt.
             */
            for (int i = 0; i < Math.min(monomialVars.length, term.length); i++) {
                this.term[i] = term[i];
            }
            for (int i = Math.min(monomialVars.length, term.length); i < monomialVars.length; i++) {
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

        public boolean isRationalMonomial() {
            return this.coefficient.isIntegerConstantOrRationalConstant();
        }

        public Monomial multipliWithExpression(Expression expr) throws EvaluationException {
            if (simplifyCase == SimplifyCase.GENERAL_CASE) {
                return new Monomial(this.coefficient.mult(expr).simplify(), this.term);
            }
            return new Monomial(this.coefficient.mult(expr).simplify(simplifyTypesBuchbergerAlgorithmRationalCase), this.term);
        }

        public Monomial multiplyWithMonomial(Monomial m) throws EvaluationException {
            Expression resultCoefficient;
            if (simplifyCase == SimplifyCase.GENERAL_CASE) {
                resultCoefficient = this.coefficient.mult(m.coefficient).simplify();
            } else {
                resultCoefficient = this.coefficient.mult(m.coefficient).simplify(simplifyTypesBuchbergerAlgorithmRationalCase);
            }
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
            if (simplifyCase == SimplifyCase.GENERAL_CASE) {
                return new Monomial(this.coefficient.div(expr).simplify(), this.term);
            }
            return new Monomial(this.coefficient.div(expr).simplify(simplifyTypesBuchbergerAlgorithmRationalCase), this.term);
        }

        public Monomial divideByMonomial(Monomial m) throws EvaluationException {
            int[] termOfQuotient = new int[this.term.length];
            for (int i = 0; i < this.term.length; i++) {
                termOfQuotient[i] = this.term[i] - m.term[i];
            }
            if (simplifyCase == SimplifyCase.GENERAL_CASE) {
                return new Monomial(this.coefficient.div(m.coefficient).simplify(), termOfQuotient);
            }
            return new Monomial(this.coefficient.div(m.coefficient).simplify(simplifyTypesBuchbergerAlgorithmRationalCase), termOfQuotient);
        }

        public Monomial lcm(Monomial m) {
            int[] termOfLCM = new int[this.term.length];
            for (int i = 0; i < this.term.length; i++) {
                termOfLCM[i] = Math.max(this.term[i], m.term[i]);
            }
            return new Monomial(ONE, termOfLCM);
        }

        public Monomial replaceVarByExpression(String var, Expression expr) {

            int indexOfVarInMonomials = -1;
            for (int i = 0; i < monomialVars.length; i++) {
                if (monomialVars[i].equals(var)) {
                    indexOfVarInMonomials = i;
                    break;
                }
            }

            if (indexOfVarInMonomials < 0) {
                return this;
            }

            int[] resultTerm = new int[this.term.length];
            for (int i = 0; i < this.term.length; i++) {
                if (i != indexOfVarInMonomials) {
                    resultTerm[i] = this.term[i];
                } else {
                    resultTerm[i] = 0;
                }
            }

            return new Monomial(this.coefficient.mult(expr.pow(this.term[indexOfVarInMonomials])), resultTerm);

        }

        public boolean equalsInTerm(Monomial m) {
            for (int i = 0; i < this.term.length; i++) {
                if (this.term[i] != m.term[i]) {
                    return false;
                }
            }
            return true;
        }

        public boolean equalsToMonomial(Monomial m) {
            return this.coefficient.equals(m.coefficient) && this.equalsInTerm(m);
        }

        public boolean equivalentToMonomial(Monomial m) {
            return this.coefficient.equivalent(m.coefficient) && this.equalsInTerm(m);
        }

        public Monomial simplify() throws EvaluationException {
            return new Monomial(this.coefficient.simplify(), this.term);
        }

        public Monomial simplify(Set<TypeSimplify> simplifyTypes) throws EvaluationException {
            return new Monomial(this.coefficient.simplify(simplifyTypes), this.term);
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

        private final List<Monomial> monomials = new ArrayList<>();

        public MultiPolynomial() {
        }

        public MultiPolynomial(List<Monomial> monomials) {
            for (Monomial m : monomials) {
                this.monomials.add(new Monomial(m.getCoefficient(), m.getTerm()));
            }
        }

        public MultiPolynomial(Monomial... monomials) {
            this.monomials.addAll(Arrays.asList(monomials));
        }

        public List<Monomial> getMonomials() {
            return this.monomials;
        }

        public void addMonomial(Monomial m) {
            this.monomials.add(m);
        }

        public MultiPolynomial copy() {
            return new MultiPolynomial(this.monomials);
        }

        public Expression toExpression() {
            Expression multiPloynomialAsExpression = ZERO;
            for (Monomial m : this.monomials) {
                multiPloynomialAsExpression = multiPloynomialAsExpression.add(m.toExpression());
            }
            return multiPloynomialAsExpression;
        }

        public ExpressionCollection toPolynomial(String var) {

            ExpressionCollection coefficients = new ExpressionCollection();

            int indexOfVar = -1;
            for (int i = 0; i < monomialVars.length; i++) {
                if (monomialVars[i].equals(var)) {
                    indexOfVar = i;
                    break;
                }
            }

            if (indexOfVar < 0) {
                coefficients.add(this.toExpression());
                return coefficients;
            }

            int exponent;
            int[] termOfMDividedOutPowerOfVar;
            for (Monomial m : this.monomials) {
                exponent = m.getTerm()[indexOfVar];
                termOfMDividedOutPowerOfVar = new int[m.getTerm().length];
                for (int i = 0; i < m.getTerm().length; i++) {
                    if (i != indexOfVar) {
                        termOfMDividedOutPowerOfVar[i] = m.getTerm()[i];
                    } else {
                        termOfMDividedOutPowerOfVar[i] = 0;
                    }
                }
                if (coefficients.get(exponent) == null) {
                    coefficients.put(exponent, new Monomial(m.getCoefficient(), termOfMDividedOutPowerOfVar).toExpression());
                } else {
                    coefficients.put(exponent, coefficients.get(exponent).add(new Monomial(m.getCoefficient(), termOfMDividedOutPowerOfVar).toExpression()));
                }
            }

            // Restliche Koeffizienten, die bisher null sind, auf 0 setzen.
            for (int i = 0; i < coefficients.getBound(); i++) {
                if (coefficients.get(i) == null) {
                    coefficients.put(i, ZERO);
                }
            }

            return coefficients;

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

        public boolean isRationalPolynomial() {
            for (Monomial m : this.monomials) {
                if (!m.isRationalMonomial()) {
                    return false;
                }
            }
            return true;
        }

        public boolean equalsToMultiPolynomial(MultiPolynomial f) {
            if (this.monomials.size() != f.monomials.size()) {
                return false;
            }
            for (int i = 0; i < this.monomials.size(); i++) {
                if (!this.monomials.get(i).equalsToMonomial(f.monomials.get(i))) {
                    return false;
                }
            }
            return true;
        }

        public boolean equivalentToMultiPolynomial(MultiPolynomial f) {
            if (this.monomials.size() != f.monomials.size()) {
                return false;
            }
            
            MultiPolynomial thisCopy = this.copy();
            MultiPolynomial fCopy = f.copy();
            
            for (int i = 0; i < thisCopy.monomials.size(); i++) {
                for (int j = 0; j < fCopy.monomials.size(); j++) {
                    if (thisCopy.monomials.get(i).equivalentToMonomial(fCopy.monomials.get(j))) {
                        thisCopy.monomials.remove(i);
                        fCopy.monomials.remove(j);
                        i--;
                        break;
                    }
                }
            }
            return thisCopy.monomials.isEmpty() && fCopy.monomials.isEmpty();
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
                        if (simplifyCase == SimplifyCase.GENERAL_CASE) {
                            thisCopy.monomials.get(i).setCoefficient(thisCopy.monomials.get(i).getCoefficient().add(fCopy.monomials.get(j).getCoefficient()).simplify());
                        } else {
                            thisCopy.monomials.get(i).setCoefficient(thisCopy.monomials.get(i).getCoefficient().add(fCopy.monomials.get(j).getCoefficient()).simplify());
                        }
                        thisCopy.monomials.remove(i);
                        fCopy.monomials.remove(j);
                        i--;
                        break;
                    }
                }
            }
            for (Monomial m : fCopy.monomials) {
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
                        if (simplifyCase == SimplifyCase.GENERAL_CASE) {
                            thisCopy.monomials.get(i).setCoefficient(thisCopy.monomials.get(i).getCoefficient().sub(fCopy.monomials.get(j).getCoefficient()).simplify());
                        } else {
                            thisCopy.monomials.get(i).setCoefficient(thisCopy.monomials.get(i).getCoefficient().sub(fCopy.monomials.get(j).getCoefficient()).simplify(simplifyTypesBuchbergerAlgorithmRationalCase));
                        }
                        fCopy.monomials.remove(j);
                        i--;
                        break;
                    }
                }
            }
            for (Monomial m : fCopy.monomials) {
                thisCopy.monomials.add(m.multipliWithExpression(MINUS_ONE));
            }
            thisCopy.clearZeroMonomials();
            return thisCopy;
        }

        public MultiPolynomial replaceVarByExpression(String var, Expression expr) {

            MultiPolynomial multiPolynomialReplacedVar = new MultiPolynomial();

            List<Monomial> monomialsCopy = new ArrayList<>();
            for (Monomial m : this.monomials) {
                monomialsCopy.add(m);
            }

            for (Monomial m : monomialsCopy) {
                multiPolynomialReplacedVar.addMonomial(m.replaceVarByExpression(var, expr));
            }

            MultiPolynomial resultMultiPolynomial = new MultiPolynomial();
            Monomial m;
            List<Monomial> monomialList = multiPolynomialReplacedVar.getMonomials();
            // Monome mit gleichem Term aufsammeln.
            while (!monomialList.isEmpty()) {
                m = monomialList.get(0);
                monomialList.remove(0);
                for (int i = 0; i < monomialList.size(); i++) {
                    if (m.equalsInTerm(monomialList.get(i))) {
                        m.setCoefficient(m.getCoefficient().add(monomialList.get(i).getCoefficient()));
                        monomialList.remove(i);
                        i--;
                    }
                }
                resultMultiPolynomial.addMonomial(m);
            }

            return resultMultiPolynomial;

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

        public MultiPolynomial simplify() throws EvaluationException {
            MultiPolynomial f = new MultiPolynomial();
            for (Monomial m : this.monomials) {
                f.addMonomial(m.simplify());
            }
            f.clearZeroMonomials();
            return f;
        }

        public MultiPolynomial simplify(Set<TypeSimplify> simplifyTypes) throws EvaluationException {
            MultiPolynomial f = new MultiPolynomial();
            for (Monomial m : this.monomials) {
                f.addMonomial(m.simplify(simplifyTypes));
            }
            f.clearZeroMonomials();
            return f;
        }

    }

    public static void setTermOrdering(TermOrderings termOrdering) {
        GroebnerBasisUtils.termOrdering = termOrdering;
    }

    public String[] getMonomialVars() {
        return monomialVars;
    }

    public static void setMonomialVars(String[] monomialVars) {
        GroebnerBasisUtils.monomialVars = monomialVars;
    }

    public static void setMonomialVars(List<String> monomialVars) {
        GroebnerBasisUtils.monomialVars = new String[monomialVars.size()];
        GroebnerBasisUtils.monomialVars = monomialVars.toArray(GroebnerBasisUtils.monomialVars);
    }

    public static boolean isMultiPolynomialFamilyRational(List<MultiPolynomial> polynomials) {
        for (MultiPolynomial polynomial : polynomials) {
            if (!polynomial.isRationalPolynomial()) {
                return false;
            }
        }
        return true;
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

        if (reductionPolynomial.isZero()) {
            return f;
        }

        Monomial leadingMonomial = reductionPolynomial.getLeadingMonomial();
        MultiPolynomial reducedPolynomial = f;

        boolean reductionPerformed;
        do {
            reductionPerformed = false;
            List<Monomial> monomialsOfF = new ArrayList<>();
            for (Monomial m : reducedPolynomial.getMonomials()) {
                monomialsOfF.add(m);
            }
            for (Monomial m : monomialsOfF) {
                if (m.isDivisibleByMonomial(leadingMonomial)) {
                    reducedPolynomial = reducedPolynomial.sub(reductionPolynomial.multiplyWithMonomial(m.divideByMonomial(leadingMonomial)));
                    reductionPerformed = true;
                }
            }
        } while (reductionPerformed);

        return reducedPolynomial;

    }

    public static MultiPolynomial reduce(MultiPolynomial f, List<MultiPolynomial> reductionPolynomials) throws EvaluationException {
        MultiPolynomial reducedPolynomial = f;
        for (MultiPolynomial polynomial : reductionPolynomials) {
            reducedPolynomial = reduce(reducedPolynomial, polynomial);
        }
        return reducedPolynomial;
    }

    /**
     * Gibt die Gröbnerbasis zu den Polynomen in polynomials zurück. Die
     * Berechnung erfolg mittels Buchberger-Algorithmus.
     *
     * @throws EvaluationException
     */
    public static List<MultiPolynomial> getNormalizedReducedGroebnerBasis(List<MultiPolynomial> polynomials) throws EvaluationException {

        // Vereinfachungsmodus setzen.
        if (isMultiPolynomialFamilyRational(polynomials)) {
            simplifyCase = SimplifyCase.RATIONAL_CASE;
        } else {
            simplifyCase = SimplifyCase.GENERAL_CASE;
        }

        List<MultiPolynomial> groebnerBasis = new ArrayList<>();
        List<MultiPolynomial> groebnerBasisAfterBuchbergerAlgorithmStep = new ArrayList<>();
        for (MultiPolynomial polynomial : polynomials) {
            groebnerBasis.add(polynomial);
            groebnerBasisAfterBuchbergerAlgorithmStep.add(polynomial);
        }

        // Gröbnerbasis berechnen.
        List<int[]> indexPairsWhichReduceToZero = new ArrayList<>();
        do {
            groebnerBasis = groebnerBasisAfterBuchbergerAlgorithmStep;
            groebnerBasisAfterBuchbergerAlgorithmStep = buchbergerAlgorithmSingleStep(groebnerBasis, indexPairsWhichReduceToZero);
        } while (groebnerBasis.size() != groebnerBasisAfterBuchbergerAlgorithmStep.size());

        // Reduzieren.
        groebnerBasis = reduceGroebnerBasisSystem(groebnerBasis);

        // Normieren.
        normalizeSystem(groebnerBasis);

        return groebnerBasis;

    }

    public static List<MultiPolynomial> getNormalizedReducedGroebnerBasis(MultiPolynomial... polynomials) throws EvaluationException {
        List<MultiPolynomial> polynomialsAsList = new ArrayList<>();
        polynomialsAsList.addAll(Arrays.asList(polynomials));
        return getNormalizedReducedGroebnerBasis(polynomialsAsList);
    }

    private static List<MultiPolynomial> buchbergerAlgorithmSingleStep(List<MultiPolynomial> polynomials, List<int[]> indexPairsWhichReduceToZero) throws EvaluationException {

        List<MultiPolynomial> polynomialsAfterStep = new ArrayList<>();
        for (MultiPolynomial polynomial : polynomials) {
            polynomialsAfterStep.add(polynomial);
        }

        MultiPolynomial SPolynomial;
        for (int i = 0; i < polynomials.size(); i++) {
            for (int j = i + 1; j < polynomials.size(); j++) {
                if (containsIndexPair(indexPairsWhichReduceToZero, i, j)){
                    continue;
                }
                SPolynomial = getSyzygyPolynomial(polynomials.get(i), polynomials.get(j));
                // S-Polynom soweit es geht mittels der bestehenden Polynome reduzieren.
                SPolynomial = reduce(SPolynomial, polynomials);
                if (!SPolynomial.isZero()) {
                    polynomialsAfterStep.add(SPolynomial);
                    return polynomialsAfterStep;
                } else { 
                    indexPairsWhichReduceToZero.add(new int[]{i, j});
                }
            }
        }
        
        return polynomialsAfterStep;

    }
    
    private static boolean containsIndexPair(List<int[]> indexPairs, int i, int j){
        for (int[] pair : indexPairs){
            if (pair.length == 2 && pair[0] == i && pair[1] == j){
                return true;
            }
        }
        return false;
    }

    private static List<MultiPolynomial> reduceGroebnerBasisSystem(List<MultiPolynomial> polynomials) throws EvaluationException {

        List<MultiPolynomial> polynomialsAfterReduction = new ArrayList<>();
        MultiPolynomial polynomial;
        Monomial leadingMonomial;
        Set<Integer> ignoreList = new HashSet<>();

        for (int i = 0; i < polynomials.size(); i++) {
            if (ignoreList.contains(i)) {
                continue;
            }
            polynomial = polynomials.get(i);
            leadingMonomial = polynomial.getLeadingMonomial();
            for (int j = 0; j < polynomials.size(); j++) {
                if (ignoreList.contains(j) || i == j) {
                    continue;
                }
                if (leadingMonomial.isDivisibleByMonomial(polynomials.get(j).getLeadingMonomial())) {
                    // In diesem Fall kann polynomial zu 0 reduziert werden.
                    polynomial = new MultiPolynomial();
                    ignoreList.add(i);
                    break;
                } else {
                    polynomial = reduce(polynomial, polynomials.get(j));
                }
            }
            if (!polynomial.isZero()) {
                polynomialsAfterReduction.add(polynomial);
            }
        }

        return polynomialsAfterReduction;

    }

    private static void normalizeSystem(List<MultiPolynomial> polynomials) throws EvaluationException {
        for (MultiPolynomial polynomial : polynomials) {
            polynomial.normalize();
        }
    }

}
