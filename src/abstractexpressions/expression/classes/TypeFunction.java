package abstractexpressions.expression.classes;

public enum TypeFunction {
    
    id, abs, sgn, exp, lg, ln, sinh, cosh, tanh, coth, sech, cosech, sin, cos, tan, cot, sec, cosec,
        arcsin, arccos, arctan, arccot, arcsec, arccosec, arsinh, arcosh, artanh, arcoth, arsech, arcosech, sqrt; 

    public boolean isTrigonometric(){
        return this.equals(sin) || this.equals(cos) || this.equals(tan) || this.equals(cot) || this.equals(cosec) || this.equals(sec);
    }   
        
    public boolean isExponential(){
        return this.equals(exp) || this.equals(sinh) || this.equals(cosh) || this.equals(tanh) || this.equals(coth) || this.equals(cosech) || this.equals(sech);
    }   
        
    public boolean isLogarithmic(){
        return this.equals(lg) || this.equals(ln);
    }   
    
    public boolean isEvenFunction(){
        return this.equals(cosh) || this.equals(cos) 
                || this.equals(sech) || this.equals(sec) 
                || this.equals(abs);
    }
        
    public boolean isOddFunction(){
        return this.equals(sinh) || this.equals(sin) 
                || this.equals(tanh) || this.equals(tan) 
                || this.equals(arsinh) || this.equals(arcsin) 
                || this.equals(cosech) || this.equals(cosec) 
                || this.equals(arcosech) || this.equals(arccosec) 
                || this.equals(sgn);
    }
        
}
