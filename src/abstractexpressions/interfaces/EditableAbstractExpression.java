package abstractexpressions.interfaces;

public class EditableAbstractExpression {

    private AbstractExpression abstrExpr;
    private boolean editable;
    
    public EditableAbstractExpression(AbstractExpression abstrExpr, boolean editable){
        this.abstrExpr = abstrExpr;
        this.editable = editable;
    }
    
    public AbstractExpression getAbstractExpression(){
        return this.abstrExpr;
    }

    public boolean isEditable(){
        return this.editable;
    }
    
}
