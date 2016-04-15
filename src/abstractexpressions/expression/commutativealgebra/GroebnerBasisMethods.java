package abstractexpressions.expression.commutativealgebra;

import abstractexpressions.expression.classes.Expression;
import java.util.ArrayList;
import java.util.Arrays;

public class GroebnerBasisMethods {

    public class Monomial {

        private int dim;
        private Expression coefficient;
        private Expression[] term;

        public Monomial(int dim, Expression coefficient, Expression[] term) {
            this.dim = dim;
            this.coefficient = coefficient;
            this.term = term;
        }

        public int getDim() {
            return dim;
        }

        public void setDim(int dim) {
            this.dim = dim;
        }

        public Expression getCoefficient() {
            return coefficient;
        }

        public void setCoefficient(Expression coefficient) {
            this.coefficient = coefficient;
        }

        public Expression[] getTerm() {
            return term;
        }

        public void setTerm(Expression[] term) {
            this.term = term;
        }

    }

    public class MultiPolynomial {

        private ArrayList<Monomial> monomials = new ArrayList<>();
        
        public MultiPolynomial(Monomial... monomials){
            this.monomials.addAll(Arrays.asList(monomials));
        }
        
    }

}
