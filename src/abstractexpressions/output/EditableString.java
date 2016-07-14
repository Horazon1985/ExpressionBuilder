package abstractexpressions.output;

public class EditableString {

    private final String text;
    private final boolean editable;
    
    public EditableString(String text, boolean editable){
        this.text = text;
        this.editable = editable;
    }
    
    public String getText(){
        return this.text;
    }

    public boolean isEditable(){
        return this.editable;
    }
    
}
