package translator;

import expressionbuilder.Expression;
import expressionbuilder.TypeLanguage;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Translator {

    public static String translateExceptionMessage(String exception_id) {

        // Die entsprechende XML-Datei öffnen.
        try {
            URL langFile;
            if (exception_id.substring(0, 2).equals("CC")) {
                langFile = ClassLoader.getSystemResource("languages/LangComputationalClasses.xml");
            } else if (exception_id.substring(0, 2).equals("EB")) {
                langFile = ClassLoader.getSystemResource("languages/LangExpressionBuilder.xml");
            } else if (exception_id.substring(0, 3).equals("LEB")) {
                langFile = ClassLoader.getSystemResource("languages/LangLogicalExpressionBuilder.xml");
            } else if (exception_id.substring(0, 3).equals("MEB")) {
                langFile = ClassLoader.getSystemResource("languages/LangMatrixExpressionBuilder.xml");
            } else if (exception_id.substring(0, 3).equals("MTF")) {
                langFile = ClassLoader.getSystemResource("languages/LangMathToolForm.xml");
            } else if (exception_id.substring(0, 3).equals("MCC")) {
                langFile = ClassLoader.getSystemResource("languages/LangMathCommandCompiler.xml");
            } else if (exception_id.substring(0, 3).equals("PRM")) {
                langFile = ClassLoader.getSystemResource("languages/LangPolynomialRootMethods.xml");
            } else if (exception_id.substring(0, 2).equals("GR")) {
                langFile = ClassLoader.getSystemResource("languages/LangGraphic.xml");
            } else if (exception_id.substring(0, 2).equals("SM")) {
                langFile = ClassLoader.getSystemResource("languages/LangSimplifyMethods.xml");
            } else {
                // Hier sind es die GUI-Sprachdateien
                langFile = ClassLoader.getSystemResource("languages/LangGUI.xml");
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(langFile.openStream());

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("object");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    if (eElement.getAttribute("id").equals(exception_id)) {
                        if (Expression.getLanguage().equals(TypeLanguage.DE)) {
                            return eElement.getElementsByTagName("German").item(0).getTextContent();
                        } else if (Expression.getLanguage().equals(TypeLanguage.EN)) {
                            return eElement.getElementsByTagName("English").item(0).getTextContent();
                        } else if (Expression.getLanguage().equals(TypeLanguage.RU)) {
                            return eElement.getElementsByTagName("Russian").item(0).getTextContent();
                        } else if (Expression.getLanguage().equals(TypeLanguage.UA)) {
                            return eElement.getElementsByTagName("Ukrainian").item(0).getTextContent();
                        }
                    }

                }
            }
        } catch (Exception e) {
        }

        // Sollte nie eintreten.
        return "";

    }

}
