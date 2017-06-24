package lang.translator;

import abstractexpressions.expression.classes.Expression;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Translator {

    private static final String PREFIX_CANCELLER_MESSAGES = "CN";
    private static final String PREFIX_COMPUTATIONAL_CLASSES_MESSAGES = "CC";
    private static final String PREFIX_EXPRESSION_BUILDER_MESSAGES = "EB";
    private static final String PREFIX_GRAPHIC_MESSAGES = "GR";
    private static final String PREFIX_LOGICAL_EXPRESSION_BUILDER_MESSAGES = "LEB";
    private static final String PREFIX_MATRIX_EXPRESSION_BUILDER_MESSAGES = "MEB";
    private static final String PREFIX_OPERATION_PARSING_UTILS_MESSAGES = "OU";
    private static final String PREFIX_SIMPLIFY_UTILS_MESSAGES = "SU";
    private static final String PREFIX_OPERATION_PARSER_MESSAGES = "OP";

    private static final String PATH_CANCELLER_MESSAGES = "lang/messages/LangCanceller.xml";
    private static final String PATH_COMPUTATIONAL_CLASSES_MESSAGES = "lang/messages/LangComputationalClasses.xml";
    private static final String PATH_EXPRESSION_BUILDER_MESSAGES = "lang/messages/LangExpressionBuilder.xml";
    private static final String PATH_GRAPHIC_MESSAGES = "lang/messages/LangGraphic.xml";
    private static final String PATH_LOGICAL_EXPRESSION_BUILDER_MESSAGES = "LangLogicalExpressionBuilder.xml";
    private static final String PATH_MATRIX_EXPRESSION_BUILDER_MESSAGES = "lang/messages/LangMatrixExpressionBuilder.xml";
    private static final String PATH_OPERATION_PARSING_UTILS_MESSAGES = "lang/messages/LangOperationParsingUtils.xml";
    private static final String PATH_SIMPLIFY_UTILS_MESSAGES = "lang/messages/LangSimplifyUtils.xml";
    private static final String PATH_OPERATION_PARSER_MESSAGES = "lang/messages/LangOperationParser.xml";
    
    private static final String PATH_UNKNOWN_ERROR_MESSAGES = "mathtool/lang/messages/LangUndefinedError.xml";

    private static final String ELEMENT_NAME_OBJECT = "object";
    private static final String ELEMENT_ATTRIBUTE_ID = "id";
    
    private static final String ELEMENT_NAME_DE = "German";
    private static final String ELEMENT_NAME_EN = "English";
    private static final String ELEMENT_NAME_RU = "Russian";
    private static final String ELEMENT_NAME_UA = "Ukrainian";
    
    private static final Map<String, String> RESOURCES = new HashMap<>();

    static {
        RESOURCES.put(PREFIX_CANCELLER_MESSAGES, PATH_CANCELLER_MESSAGES);
        RESOURCES.put(PREFIX_COMPUTATIONAL_CLASSES_MESSAGES, PATH_COMPUTATIONAL_CLASSES_MESSAGES);
        RESOURCES.put(PREFIX_EXPRESSION_BUILDER_MESSAGES, PATH_EXPRESSION_BUILDER_MESSAGES);
        RESOURCES.put(PREFIX_GRAPHIC_MESSAGES, PATH_GRAPHIC_MESSAGES);
        RESOURCES.put(PREFIX_LOGICAL_EXPRESSION_BUILDER_MESSAGES, PATH_LOGICAL_EXPRESSION_BUILDER_MESSAGES);
        RESOURCES.put(PREFIX_MATRIX_EXPRESSION_BUILDER_MESSAGES, PATH_MATRIX_EXPRESSION_BUILDER_MESSAGES);
        RESOURCES.put(PREFIX_OPERATION_PARSING_UTILS_MESSAGES, PATH_OPERATION_PARSING_UTILS_MESSAGES);
        RESOURCES.put(PREFIX_SIMPLIFY_UTILS_MESSAGES, PATH_SIMPLIFY_UTILS_MESSAGES);
        RESOURCES.put(PREFIX_OPERATION_PARSER_MESSAGES, PATH_OPERATION_PARSER_MESSAGES);
    }
    
    public static Collection<String> getResources() {
        Collection<String> resources = new HashSet<>();
        /*
        Manipulationen an RESOURCES.values() können Änderungen an der Map 
        RESOURCES nach sich ziehen können. Deshalb wird hier eine Kopie 
        zurückgegeben.
        */
        resources.addAll(RESOURCES.values());
        return resources;
    }
    
    /**
     * Gibt eine Meldung entsprechend der exceptionId und der eingestellten
     * Sprache zurück.
     */
    private static String translateMessage(String exceptionId) {

        // Die entsprechende XML-Datei öffnen.
        try {
            URL langFile = null;
            for (String key : RESOURCES.keySet()) {
                if (exceptionId.startsWith(key)) {
                    langFile = ClassLoader.getSystemResource(RESOURCES.get(key));
                    break;
                }
            }
            if (langFile == null) {
                // Fall: Unbekannten Fehler aufgetreten (Präfix nicht identifizierbar).
                langFile = ClassLoader.getSystemResource(PATH_UNKNOWN_ERROR_MESSAGES);
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(langFile.openStream());

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName(ELEMENT_NAME_OBJECT);

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    if (eElement.getAttribute(ELEMENT_ATTRIBUTE_ID).equals(exceptionId)) {
                        switch (Expression.getLanguage()) {
                            case DE:
                                return eElement.getElementsByTagName(ELEMENT_NAME_DE).item(0).getTextContent();
                            case EN:
                                return eElement.getElementsByTagName(ELEMENT_NAME_EN).item(0).getTextContent();
                            case RU:
                                return eElement.getElementsByTagName(ELEMENT_NAME_RU).item(0).getTextContent();
                            case UA:
                                return eElement.getElementsByTagName(ELEMENT_NAME_UA).item(0).getTextContent();
                            default:
                                break;
                        }
                    }

                }
            }
        } catch (Exception e) {
        }

        // Sollte nie eintreten.
        return "";

    }

    /**
     * Gibt eine Meldung entsprechend der exceptionId und der eingestellten
     * Sprache zurück, wobei Tokens der Form [0], [1], [2], ... nacheinander
     * durch die Parameter params ersetzt werden.
     */
    public static String translateOutputMessage(String messageId, Object... params) {
        String message = translateMessage(messageId);
        String token;
        for (int i = 0; i < params.length; i++) {
            token = "[" + i + "]";
            while (message.contains(token)) {
                message = message.substring(0, message.indexOf(token)) + params[i].toString() + message.substring(message.indexOf(token) + token.length());
            }
        }
        return message;
    }

}
