package expressionsimplifymethods;

import exceptions.EvaluationException;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.TypeSimplify;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;

public class ExpressionCollection {

    private final HashMap<Integer, Expression> terms;
    private int bound;

    public ExpressionCollection() {
        this.terms = new HashMap<>();
        this.bound = 0;
    }

    public ExpressionCollection(ExpressionCollection terms) {
        this.terms = new HashMap<>();
        this.bound = 0;
        for (int i = 0; i < terms.bound; i++) {
            this.add(terms.get(i));
        }
    }

    public ExpressionCollection(Object... terms) {
        this.terms = new HashMap<>();
        this.bound = 0;
        for (Object term : terms) {
            if (term != null) {
                if (term instanceof Expression) {
                    this.add((Expression) term);
                } else if (term instanceof BigInteger) {
                    this.add(new Constant((BigInteger) term));
                } else if (term instanceof BigDecimal) {
                    this.add(new Constant((BigDecimal) term));
                } else if (term instanceof Integer) {
                    this.add(new Constant((int) term));
                }
            }
        }
    }

    public int getBound() {
        return this.bound;
    }

    public int getSize() {
        return this.terms.size();
    }

    public boolean isEmpty() {
        return this.terms.isEmpty();
    }

    public Expression get(int i) {
        return this.terms.get(i);
    }

    public void put(int i, Expression expr) {
        if (i < 0) {
            return;
        }
        this.terms.put(i, expr);
        if (i >= this.bound - 1) {
            this.bound = i + 1;
        }
    }

    public void add(Expression expr) {
        if (expr != null) {
            this.terms.put(this.bound, expr);
            this.bound++;
        }
    }

    public void add(ExpressionCollection newTerms) {
        for (int i = 0; i < newTerms.getBound(); i++) {
            if (newTerms.get(i) != null) {
                this.add(newTerms.get(i));
            }
        }
    }

    public void remove(int i) {
        this.terms.remove(i);
        for (int j = this.bound - 1; j >= 0; j--) {
            if (this.terms.get(j) != null) {
                this.bound = j + 1;
                return;
            }
        }
        this.bound = 0;
    }

    public void clear() {
        this.terms.clear();
        this.bound = 0;
    }

    /**
     * Gibt zurück, ob die vorliegende ExpressionCollection gleich der
     * ExpressionCollection exprCol ist. Die Elemente werden mit der Methode
     * equals() der Klasse Expression verglichen.
     */
    public boolean equals(ExpressionCollection exprCol) {
        if (this.bound != exprCol.bound) {
            return false;
        }
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) == null && exprCol.terms.get(i) != null
                    || this.terms.get(i) != null && exprCol.terms.get(i) == null) {
                return false;
            }
            if (!this.terms.get(i).equals(exprCol.terms.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gibt zurück, ob die vorliegende ExpressionCollection und exprCol Mengen
     * mit äquivalente Termen bilden (also ungeachtet der Reihenfolge).
     */
    public boolean equivalent(ExpressionCollection exprCol) {
        return SimplifyUtilities.difference(this, exprCol).isEmpty() && SimplifyUtilities.difference(exprCol, this).isEmpty();
    }

    /**
     * Gibt zurück, ob die ExpressionCollection den Ausdruck expr enthält
     * (verglichen wird mit equals()).
     */
    public boolean contains(Expression expr) {
        if (expr == null) {
            for (int i = 0; i < this.bound; i++) {
                if (this.terms.get(i) == null) {
                    return true;
                }
            }
            return false;
        }
        for (int i = 0; i < this.bound; i++) {
            if (expr.equals(this.terms.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt zurück, ob die ExpressionCollection den Ausdruck expr enthält
     * (verglichen wird mit equivalent()). Ist expr == null, so wird false
     * zurückgegeben.
     */
    public boolean containsExquivalent(Expression expr) {
        if (expr == null) {
            for (int i = 0; i < this.bound; i++) {
                if (this.terms.get(i) == null) {
                    return true;
                }
            }
            return false;
        }
        for (int i = 0; i < this.bound; i++) {
            if (expr.equivalent(this.terms.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (this.bound == 0) {
            return "[]";
        }
        String result = "[";
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                result = result + this.terms.get(i).toString() + ", ";
            } else {
                result = result + "-, ";
            }
        }
        return result.substring(0, result.length() - 2) + "]";
    }

    /**
     * Kopiert terms.
     */
    public static ExpressionCollection copy(ExpressionCollection terms) {

        ExpressionCollection result = new ExpressionCollection();
        for (int i = 0; i < terms.getBound(); i++) {
            if (terms.get(i) != null) {
                result.put(i, terms.get(i).copy());
            }
        }
        result.bound = terms.bound;
        return result;

    }

    /**
     * Gibt eine ExpressionCollection zurück, die genau aus den Elemente
     * terms(m), ..., terms(n - 1) bzw. ganz terms im Falle von Indexüberläufen
     * besteht.
     */
    public static ExpressionCollection copy(ExpressionCollection terms, int m, int n) {
        ExpressionCollection result = new ExpressionCollection();
        for (int i = m; i < n; i++) {
            if (terms.get(i) != null) {
                result.add(terms.get(i).copy());
            }
        }
        return result;
    }

    /**
     * Entfernt alle mehrfachen Kopien bereits vorhandener Terme.
     */
    public void removeMultipleTerms() {

        for (int i = 0; i < this.bound; i++) {
            if (terms.get(i) == null) {
                for (int j = i + 1; j < this.bound; j++) {
                    if (terms.get(j) == null) {
                        continue;
                    }
                    terms.put(i, terms.get(j));
                    remove(j);
                }
            }
            for (int j = i + 1; j < this.bound; j++) {
                if (terms.get(j) == null) {
                    continue;
                }
                if (this.terms.get(j).equivalent(this.terms.get(i))) {
                    remove(j);
                }
            }
        }

    }

    public void addExpression(Expression expr) {
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                this.terms.put(i, this.terms.get(i).add(expr));
            }
        }
    }

    public void subExpression(Expression expr) {
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                this.terms.put(i, this.terms.get(i).sub(expr));
            }
        }
    }

    public void multExpression(Expression expr) {
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                this.terms.put(i, this.terms.get(i).mult(expr));
            }
        }
    }

    public void divByExpression(Expression expr) {
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                this.terms.put(i, this.terms.get(i).div(expr));
            }
        }
    }

    public void powExpression(Expression expr) {
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                this.terms.put(i, this.terms.get(i).pow(expr));
            }
        }
    }

    public ExpressionCollection simplify() throws EvaluationException {
        ExpressionCollection result = new ExpressionCollection();
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                result.put(i, terms.get(i).simplify());
            }
        }
        return result;
    }

    public ExpressionCollection simplify(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException {
        ExpressionCollection result = new ExpressionCollection();
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                result.put(i, terms.get(i).simplify(simplifyTypes));
            }
        }
        return result;
    }

}
