package computationbounds;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ComputationBounds {

    public static final int BOUND_MAX_DIGITS_OF_E = getBound("Bound_MAX_DIGITS_OF_E");
    public static final int BOUND_MAX_DIGITS_OF_PI = getBound("Bound_MAX_DIGITS_OF_PI");
    public static final int BOUND_DIVISORS_OF_INTEGERS = getBound("Bound_DIVISORS_OF_INTEGERS");
    public static final int BOUND_POWER_OF_RATIONALS = getBound("Bound_POWER_OF_RATIONALS");
    public static final int BOUND_ROOTDEGREE_OF_RATIONALS = getBound("Bound_ROOTDEGREE_OF_RATIONALS");
    public static final int BOUND_INTEGER_FACTORIAL = getBound("Bound_INTEGER_FACTORIAL");
    public static final int BOUND_FACTORIAL_WITH_DENOMINATOR_TWO = getBound("Bound_FACTORIAL_WITH_DENOMINATOR_TWO");
    public static final int BOUND_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL = getBound("Bound_ROOTDEGREE_OF_SUMMAND_WITHIN_BINOMIAL");
    public static final int BOUND_POWER_OF_BINOMIAL = getBound("Bound_POWER_OF_BINOMIAL");
    public static final int BOUND_DEGREE_OF_COMMON_ROOT = getBound("Bound_DEGREE_OF_COMMON_ROOT");
    public static final int BOUND_DEGREE_OF_POLYNOMIAL_FOR_SOLVING_EQUATION = getBound("Bound_DEGREE_OF_POLYNOMIAL_FOR_SOLVING_EQUATION");
    public static final int BOUND_DEGREE_OF_POLYNOMIAL_FOR_SIMPLIFYING_SUMS = getBound("Bound_DEGREE_OF_POLYNOMIAL_FOR_SIMPLIFYING_SUMS");
    public static final int BOUND_NUMBER_OF_SUMMANDS_IN_EXPANSION = getBound("Bound_NUMBER_OF_SUMMANDS_IN_EXPANSION");
    public static final int BOUND_DEGREE_OF_POLYNOMIAL_FOR_SIMPLIFY = getBound("Bound_DEGREE_OF_POLYNOMIAL_FOR_SIMPLIFY");
    public static final int BOUND_DEGREE_OF_MULTIPOLYNOMIAL_FOR_SIMPLIFY = getBound("Bound_DEGREE_OF_MULTIPOLYNOMIAL_FOR_SIMPLIFY");
    public static final int BOUND_MAXIMAL_INTEGRABLE_POWER = getBound("Bound_MAXIMAL_INTEGRABLE_POWER");
    public static final int BOUND_POWER_OF_GENERAL_MATRIX = getBound("Bound_POWER_OF_GENERAL_MATRIX");
    public static final int BOUND_POWER_OF_RATIONAL_MATRIX = getBound("Bound_POWER_OF_RATIONAL_MATRIX");
    public static final int BOUND_COMPUTE_DET_EXPLICITELY = getBound("Bound_COMPUTE_DET_EXPLICITLY");
    
    public static int getBound(String bound_id) {

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

                    if (eElement.getAttribute("id").equals(bound_id)) {
                        return Integer.parseInt(eElement.getElementsByTagName("value").item(0).getTextContent());
                    }

                }
            }
        } catch (Exception e) {
        }

        /**
         * Sollte nie eintreten.
         */
        return 0;

    }
    
}
