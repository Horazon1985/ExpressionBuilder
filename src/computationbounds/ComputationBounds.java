package computationbounds;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ComputationBounds {

    public static final int BOUND_COMMAND_MAX_DIGITS_OF_E;
    public static final int BOUND_COMMAND_MAX_DIGITS_OF_PI;
    public static final int BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS;
    public static final int BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS;
    public static final int BOUND_ARITHMETIC_MAX_ROOTDEGREE_OF_RATIONALS;
    public static final int BOUND_ARITHMETIC_MAX_INTEGER_FACTORIAL;
    public static final int BOUND_ARITHMETIC_MAX_FACTORIAL_WITH_DENOMINATOR_TWO;
    public static final int BOUND_ALGEBRA_MAX_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL;
    public static final int BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL;
    public static final int BOUND_ALGEBRA_MAX_DEGREE_OF_COMMON_ROOT;
    public static final int BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS;
    public static final int BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_POWERFUL_EXPANSION;
    public static final int BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_MODERATE_EXPANSION;
    public static final int BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_SHORT_EXPANSION;
    public static final int BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL;
    public static final int BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL_FOR_DECOMPOSITION;
    public static final int BOUND_OPERATOR_MAX_DEGREE_OF_POLYNOMIAL_INSIDE_SUM;
    public static final int BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT;
    public static final int BOUND_OPERATOR_MAX_INTEGRABLE_POWER;
    public static final int BOUND_OPERATOR_MAX_NUMBER_OF_INTEGRABLE_SUMMANDS;
    public static final int BOUND_MATRIX_MAX_POWER_OF_GENERAL_MATRIX;
    public static final int BOUND_MATRIX_MAX_POWER_OF_RATIONAL_MATRIX;
    public static final int BOUND_MATRIX_MAX_DIM_FOR_COMPUTE_DET_EXPLICITELY;
    public static final int BOUND_NUMERIC_DEFAULT_NUMBER_OF_INTERVALS;
    public static final int BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION;

    static {
        BOUND_COMMAND_MAX_DIGITS_OF_E = getBound("BOUND_COMMAND_MAX_DIGITS_OF_E");
        BOUND_COMMAND_MAX_DIGITS_OF_PI = getBound("BOUND_COMMAND_MAX_DIGITS_OF_PI");
        BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS = getBound("BOUND_ARITHMETIC_DIVISORS_OF_INTEGERS");
        BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS = getBound("BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS");
        BOUND_ARITHMETIC_MAX_ROOTDEGREE_OF_RATIONALS = getBound("BOUND_ARITHMETIC_MAX_ROOTDEGREE_OF_RATIONALS");
        BOUND_ARITHMETIC_MAX_INTEGER_FACTORIAL = getBound("BOUND_ARITHMETIC_MAX_INTEGER_FACTORIAL");
        BOUND_ARITHMETIC_MAX_FACTORIAL_WITH_DENOMINATOR_TWO = getBound("BOUND_ARITHMETIC_MAX_FACTORIAL_WITH_DENOMINATOR_TWO");
        BOUND_ALGEBRA_MAX_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL = getBound("BOUND_ALGEBRA_MAX_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL");
        BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL = getBound("BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL");
        BOUND_ALGEBRA_MAX_DEGREE_OF_COMMON_ROOT = getBound("BOUND_ALGEBRA_MAX_DEGREE_OF_COMMON_ROOT");
        BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS = getBound("BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS");
        BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_POWERFUL_EXPANSION = getBound("BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_POWERFUL_EXPANSION");
        BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_MODERATE_EXPANSION = getBound("BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_MODERATE_EXPANSION");
        BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_SHORT_EXPANSION = getBound("BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_SHORT_EXPANSION");
        BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL = getBound("BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL");
        BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL_FOR_DECOMPOSITION = getBound("BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL_FOR_DECOMPOSITION");
        BOUND_OPERATOR_MAX_DEGREE_OF_POLYNOMIAL_INSIDE_SUM = getBound("BOUND_OPERATOR_MAX_DEGREE_OF_POLYNOMIAL_INSIDE_SUM");
        BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT = getBound("BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT");
        BOUND_OPERATOR_MAX_INTEGRABLE_POWER = getBound("BOUND_OPERATOR_MAX_INTEGRABLE_POWER");
        BOUND_OPERATOR_MAX_NUMBER_OF_INTEGRABLE_SUMMANDS = getBound("BOUND_OPERATOR_MAX_NUMBER_OF_INTEGRABLE_SUMMANDS");
        BOUND_MATRIX_MAX_POWER_OF_GENERAL_MATRIX = getBound("BOUND_MATRIX_MAX_POWER_OF_GENERAL_MATRIX");
        BOUND_MATRIX_MAX_POWER_OF_RATIONAL_MATRIX = getBound("BOUND_MATRIX_MAX_POWER_OF_RATIONAL_MATRIX");
        BOUND_MATRIX_MAX_DIM_FOR_COMPUTE_DET_EXPLICITELY = getBound("BOUND_MATRIX_MAX_DIM_FOR_COMPUTE_DET_EXPLICITELY");
        BOUND_NUMERIC_DEFAULT_NUMBER_OF_INTERVALS = getBound("BOUND_NUMERIC_DEFAULT_NUMBER_OF_INTERVALS");
        BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION = getBound("BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION");
    }

    /**
     * Gibt zu einer gegebenen boundId die entsprechende Berechnungsschranke
     * zurück, die in der Bounds.xml festgelegt ist.
     */
    public static int getBound(String boundId) {

        try {
            URL langFile = ClassLoader.getSystemResource("computationbounds/Bounds.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(langFile.openStream());

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("object");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    if (eElement.getAttribute("id").equals(boundId)) {
                        return Integer.parseInt(eElement.getElementsByTagName("value").item(0).getTextContent());
                    }

                }
            }
        } catch (Exception e) {
        }

        // Sollte bei korrekter boundId nie eintreten.
        return 0;

    }

}
