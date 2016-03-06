package abstractexpressions.matrixexpression.utilities;

import java.util.HashMap;
import java.util.Iterator;
import abstractexpressions.matrixexpression.classes.MatrixExpression;

public class MatrixExpressionCollection implements Iterable<MatrixExpression> {

    private final HashMap<Integer, MatrixExpression> matrixTerms;
    private int bound;

    public MatrixExpressionCollection() {
        this.matrixTerms = new HashMap<>();
        this.bound = 0;
    }

    public int getBound() {
        return this.bound;
    }

    public boolean isEmpty() {
        return this.matrixTerms.isEmpty();
    }

    public MatrixExpression get(int i) {
        return this.matrixTerms.get(i);
    }

    public void put(int i, MatrixExpression matExpr) {
        if (i < 0) {
            return;
        }
        this.matrixTerms.put(i, matExpr);
        if (i >= this.bound - 1) {
            this.bound = i + 1;
        }
    }

    public void add(MatrixExpression matExpr) {
        if (matExpr != null) {
            this.matrixTerms.put(this.bound, matExpr);
            this.bound++;
        }
    }
    
    public void insert(int i, MatrixExpression matExpr) {
        
        if (i < 0) {
            return;
        }
        
        if (matExpr != null) {
            if (this.matrixTerms.get(i) == null){
                this.matrixTerms.put(i, matExpr);
                this.bound = Math.max(this.bound, i + 1);
            } else {
                if (i >= this.bound){
                    put(i, matExpr);
                } else {
                    for (int j = this.bound; j > i; j--){
                        this.matrixTerms.put(j, this.matrixTerms.get(j - 1));
                        this.matrixTerms.remove(j - 1);
                    }
                    put(i, matExpr);
                    this.bound++;
                }
            }
        }
        
    }

    public void addAll(MatrixExpressionCollection newMatrixTerms) {
        for (int i = 0; i < newMatrixTerms.getBound(); i++) {
            if (newMatrixTerms.get(i) != null) {
                this.add(newMatrixTerms.get(i));
            }
        }
    }

    public void remove(int i) {
        this.matrixTerms.remove(i);
        for (int j = this.bound - 1; j >= 0; j--) {
            if (this.matrixTerms.get(j) != null) {
                this.bound = j + 1;
                return;
            }
        }
        this.bound = 0;
    }

    public void clear() {
        this.matrixTerms.clear();
        this.bound = 0;
    }

    @Override
    public String toString() {
        if (this.bound == 0) {
            return "[]";
        }
        String result = "[";
        for (int i = 0; i < this.bound; i++) {
            if (this.matrixTerms.get(i) != null) {
                result = result + this.matrixTerms.get(i).toString() + ", ";
            }
        }
        return result.substring(0, result.length() - 2) + "]";
    }

    /**
     * Kopiert terms.
     */
    public static MatrixExpressionCollection copy(MatrixExpressionCollection matTerms) {

        MatrixExpressionCollection result = new MatrixExpressionCollection();
        for (int i = 0; i < matTerms.getBound(); i++) {
            if (matTerms.get(i) != null) {
                result.put(i, matTerms.get(i).copy());
            }
        }
        result.bound = matTerms.bound;
        return result;

    }

    /**
     * Kopiert terms(m), ..., terms(n - 1) bzw. ganz terms im Falle von
     * Indexüberläufen.
     */
    public static MatrixExpressionCollection copy(MatrixExpressionCollection matTerms, int m, int n) {

        MatrixExpressionCollection result = new MatrixExpressionCollection();
        for (int i = m; i < n; i++) {
            if (matTerms.get(i) != null) {
                result.put(i, matTerms.get(i).copy());
            }
        }
        result.bound = matTerms.bound;
        return result;

    }

    @Override
    public Iterator<MatrixExpression> iterator() {
        return new Iterator<MatrixExpression>() {
            
            private int currentIndex = -1;
            
            @Override
            public boolean hasNext() {
                return this.currentIndex < getBound() - 1;
            }

            @Override
            public MatrixExpression next() {
                for (int i = this.currentIndex + 1; i < getBound() - 1; i++){
                    if (get(i) != null){
                        this.currentIndex = i;
                        return get(i);
                    }
                }
                this.currentIndex = getBound() - 1;
                return get(getBound() - 1);
            }
            
            @Override
            public void remove() {
                matrixTerms.remove(currentIndex);
                for (int j = bound - 1; j >= 0; j--) {
                    if (matrixTerms.get(j) != null) {
                        bound = j + 1;
                        return;
                    }
                }
                bound = 0;
            }

        };
    }
    
}
