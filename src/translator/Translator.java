package translator;

import enumerations.TypeLanguage;
import expressionbuilder.Expression;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Translator {

    public static String translateExceptionMessage(String exceptionId) {

        // Die entsprechende XML-Datei öffnen.
        try {
            URL langFile;
            if (exceptionId.substring(0, 2).equals("CC")) {
                langFile = ClassLoader.getSystemResource("languages/LangComputationalClasses.xml");
            } else if (exceptionId.substring(0, 2).equals("EB")) {
                langFile = ClassLoader.getSystemResource("languages/LangExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 2).equals("FC")) {
                langFile = ClassLoader.getSystemResource("languages/LangFlowController.xml");
            } else if (exceptionId.substring(0, 3).equals("GUI")) {
                langFile = ClassLoader.getSystemResource("languages/LangGUI.xml");
            } else if (exceptionId.substring(0, 2).equals("GR")) {
                langFile = ClassLoader.getSystemResource("languages/LangGraphic.xml");
            } else if (exceptionId.substring(0, 3).equals("LEB")) {
                langFile = ClassLoader.getSystemResource("languages/LangLogicalExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 3).equals("MCC")) {
                langFile = ClassLoader.getSystemResource("languages/LangMathCommandCompiler.xml");
            } else if (exceptionId.substring(0, 3).equals("MTF")) {
                langFile = ClassLoader.getSystemResource("languages/LangMathToolForm.xml");
            } else if (exceptionId.substring(0, 3).equals("MEB")) {
                langFile = ClassLoader.getSystemResource("languages/LangMatrixExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 3).equals("PRM")) {
                langFile = ClassLoader.getSystemResource("languages/LangPolynomialRootMethods.xml");
            } else if (exceptionId.substring(0, 2).equals("SM")) {
                langFile = ClassLoader.getSystemResource("languages/LangSimplifyMethods.xml");
            } else {
                // Datei für unbekannten Fehler öffnen.
                langFile = ClassLoader.getSystemResource("languages/LangUndefinedError.xml");
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

                    if (eElement.getAttribute("id").equals(exceptionId)) {
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
