package abstractexpressions.expression.basic;

import enums.TypeSimplify;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ExpressionCollection implements Iterable<Expression> {

    private final Map<Integer, Expression> terms;
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

    public ExpressionCollection(Expression[] terms) {
        this.terms = new HashMap<>();
        this.bound = 0;
        for (Expression term : terms) {
            this.add(term);
        }
    }

    public ExpressionCollection(Object... terms) {
        this.terms = new HashMap<>();
        this.bound = 0;
        for (Object term : terms) {
            if (term != null) {
                if (term instanceof String) {
                    try {
                        this.add(Expression.build((String) term));
                    } catch (ExpressionException e) {
                        // Dann einfach nichts hinzufügen.
                    }
                } else if (term instanceof Expression) {
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

    public Expression getLast() {
        if (isEmpty()){
            return null;
        }
        return get(getBound() - 1);
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

    public void addAll(ExpressionCollection newTerms) {
        for (int i = 0; i < newTerms.getBound(); i++) {
            if (newTerms.get(i) != null) {
                this.add(newTerms.get(i));
            }
        }
    }

    public void insert(int i, Expression expr) {

        if (i < 0) {
            return;
        }

        if (expr != null) {
            if (this.terms.get(i) == null) {
                this.terms.put(i, expr);
                this.bound = Math.max(this.bound, i + 1);
            } else if (i >= this.bound) {
                put(i, expr);
            } else {
                for (int j = this.bound; j > i; j--) {
                    this.terms.put(j, this.terms.get(j - 1));
                    this.terms.remove(j - 1);
                }
                put(i, expr);
                this.bound++;
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
    public boolean equivalentInTerms(ExpressionCollection exprCol) {
        return SimplifyUtilities.difference(this, exprCol).isEmpty() && SimplifyUtilities.difference(exprCol, this).isEmpty();
    }

    /**
     * Gibt zurück, ob die ExpressionCollection einen Ausdruck enthält, in
     * welchem die Veränderliche var vorkommt.
     */
    public boolean contains(String var) {
        for (Expression term : this) {
            if (term.contains(var)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt zurück, ob die ExpressionCollection den Ausdruck expr enthält
     * (verglichen wird mit equals()).
     */
    public boolean containsExpression(Expression expr) {
        if (expr == null) {
            for (int i = 0; i < this.bound; i++) {
                if (this.terms.get(i) == null) {
                    return true;
                }
            }
            return false;
        }
        for (Expression term : this) {
            if (expr.equals(term)) {
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
        for (Expression term : this) {
            if (expr.equivalent(term)) {
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
     * Gibt eine Kopie der vorliegenden ExpressionCollection-Instanz zurück.
     */
    public ExpressionCollection copy() {

        ExpressionCollection result = new ExpressionCollection();
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                result.put(i, this.terms.get(i).copy());
            }
        }
        result.bound = this.bound;
        return result;

    }

    /**
     * Gibt eine ExpressionCollection zurück, die genau aus den Elemente
     * der vorliegenden ExpressionCollection-Instanz mit den Indizes m, ..., 
     * n - 1 bzw. aus allen Elementen im Falle von Indexüberläufen besteht.
     */
    public ExpressionCollection copy(int m, int n) {
        ExpressionCollection result = new ExpressionCollection();
        for (int i = m; i < n; i++) {
            if (this.terms.get(i) != null) {
                result.add(this.terms.get(i).copy());
            }
        }
        return result;
    }

    /**
     * Entfernt alle mehrfachen Kopien bereits vorhandener Terme.
     */
    public void removeMultipleEquivalentTerms() {

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

    public void subtractExpression(Expression expr) {
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                this.terms.put(i, this.terms.get(i).sub(expr));
            }
        }
    }

    public void multiplyWithExpression(Expression expr) {
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                this.terms.put(i, this.terms.get(i).mult(expr));
            }
        }
    }

    public void divideByExpression(Expression expr) {
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

    public ExpressionCollection simplify(Set<TypeSimplify> simplifyTypes) throws EvaluationException {
        ExpressionCollection result = new ExpressionCollection();
        for (int i = 0; i < this.bound; i++) {
            if (this.terms.get(i) != null) {
                result.put(i, terms.get(i).simplify(simplifyTypes));
            }
        }
        return result;
    }

    @Override
    public Iterator<Expression> iterator() {
        return new Iterator<Expression>() {

            private int currentIndex = -1;

            @Override
            public boolean hasNext() {
                return this.currentIndex < getBound() - 1;
            }

            @Override
            public Expression next() {
                for (int i = this.currentIndex + 1; i < getBound() - 1; i++) {
                    if (get(i) != null) {
                        this.currentIndex = i;
                        return get(i);
                    }
                }
                this.currentIndex = getBound() - 1;
                return get(getBound() - 1);
            }

            @Override
            public void remove() {
                terms.remove(currentIndex);
                for (int j = bound - 1; j >= 0; j--) {
                    if (terms.get(j) != null) {
                        bound = j + 1;
                        return;
                    }
                }
                bound = 0;
            }

        };
    }

}
