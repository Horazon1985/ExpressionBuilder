package operationparser;

import java.util.ArrayList;

/*
Diese Klasse stellt ein technisches Ergebnis für ein geparstes Pattern, welches 
eine allegemeine Operation (Operator, Befehl, ...) darstellt. Das Pattern soll
die folgende Form besitzen und die folgenden Bedingungen erfüllen:
(1) Struktur: operationsName(typ_1(Restriktionen),...,typ_n(Restriktionen)).
(2) typ_1, ..., typ_n sind wie folgt zusammengesetzt: Präfix = "equation",
"expr", "integer", "logexpr", "matexpr", "type", "uniquevar", "var", Suffix = "", "+" ("+" bedeutet
immer ein Vorkommen von mindestens einem Mal).
(3) Restriktionen: Bei alle Typen außer "var": (a, b), wobei a und b entweder ganze
Zahlen sind oder "none". Bei Ausdrücken geben sie an, wieviele Variablen mindestens (a)
und höchstens (b) vorkommen müssen. "none" bedeutet keine Einschränkung.
(4) Restriktionen: Bei Typen "uniquevar" und "var": (a_1, ..., a_m), wobei a_i entweder die Form
<natürliche Zahl> oder "!"<natürliche Zahl>. <natürliche Zahl> = k bedeutet, dass diese 
Variable im k-ten Parameter (im Ausdruck im Patterm) vorkommen muss, "!"<natürliche Zahl>
= !k, dass diese Variable im k-ten Parameter (im Ausdruck im Patterm) nicht vorkommen 
darf.
(5) Typ "uniquevar" bedeutet: Diese Variable(n) darf nur einmal unter allen Parametern,
welche Variablen darstellen, vorkommen. 
(6) Typ "type" bedeutet: Typ/Modus. Die Restriktionen geben dabei den Typnamen an. 
(7) Folgende Form ist verboten: zwei aufeinanderfolgende Parameter besitzen denselben
Typ, der vorherige besitzt ein "+", der nachfolgende keinen (beispielsweise ist
ein Pattern der Form "op(expr+(3,7),expr(2,5),var)" nicht zulässig).
 */
public class ParseResultPattern {

    private final String operationName;

    private final ArrayList<ParameterPattern> parameterPatterns;

    public ParseResultPattern(String operationName, ArrayList<ParameterPattern> paramPatterns) {
        this.operationName = operationName;
        this.parameterPatterns = paramPatterns;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public ParameterPattern getParameterPattern(int i) {
        return this.parameterPatterns.get(i);
    }

    public int size() {
        return this.parameterPatterns.size();
    }

}
