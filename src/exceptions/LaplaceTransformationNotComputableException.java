package exceptions;

public class LaplaceTransformationNotComputableException extends MathToolException {

    private static final String LAPLACE_TRANSFORMATION_NOT_COMPUTABLE = "Laplace transformation not computable.";

    public LaplaceTransformationNotComputableException() {
        super(LAPLACE_TRANSFORMATION_NOT_COMPUTABLE);
    }

}
