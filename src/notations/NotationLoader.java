package notations;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class NotationLoader {
    
    public static final String SUBSTITUTION_VAR = getVarName("SUBSTITUTION_VAR");
    public static final String PFD_VAR = getVarName("PFD_VAR");
    public static final String FREE_INTEGER_PARAMETER_VAR = getVarName("FREE_INTEGER_PARAMETER_VAR");
    public static final String FREE_REAL_PARAMETER_VAR = getVarName("FREE_REAL_PARAMETER_VAR");
    public static final String FREE_INTEGRATION_CONSTANT_VAR = getVarName("FREE_INTEGRATION_CONSTANT_VAR");
    public static final String SELFDEFINEDFUNCTION_VAR = getVarName("SELFDEFINEDFUNCTION_VAR");
    
    public static String getVarName(String varName) {

        // XML-Datei öffnen.
        try {
            URL langFile = ClassLoader.getSystemResource("notations/NotationConfig.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(langFile.openStream());
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("object");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    if (eElement.getAttribute("id").equals(varName)) {
                        return eElement.getElementsByTagName("value").item(0).getTextContent();
                    }

                }
            }
        } catch (Exception e) {
        }

        // Sollte nie eintreten.
        return "";

    }
    
}
