package lang.translator;

import enums.TypeLanguage;
import abstractexpressions.expression.classes.Expression;
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
                langFile = ClassLoader.getSystemResource("lang/messages/LangComputationalClasses.xml");
            } else if (exceptionId.substring(0, 2).equals("EB")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 2).equals("FC")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangFlowController.xml");
            } else if (exceptionId.substring(0, 3).equals("GUI")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangGUI.xml");
            } else if (exceptionId.substring(0, 2).equals("GR")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangGraphic.xml");
            } else if (exceptionId.substring(0, 3).equals("LAA")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangLinearAlgebraAlgorithms.xml");
            } else if (exceptionId.substring(0, 3).equals("LEB")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangLogicalExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 3).equals("MCC")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangMathCommandCompiler.xml");
            } else if (exceptionId.substring(0, 3).equals("MTF")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangMathToolForm.xml");
            } else if (exceptionId.substring(0, 3).equals("MEB")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangMatrixExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 3).equals("PRM")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangPolynomialRootMethods.xml");
            } else if (exceptionId.substring(0, 2).equals("SM")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangSimplifyMethods.xml");
            } else {
                // Datei für unbekannten Fehler öffnen.
                langFile = ClassLoader.getSystemResource("lang/messages/LangUndefinedError.xml");
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