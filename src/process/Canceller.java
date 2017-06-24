package process;

import exceptions.CancellationException;
import lang.translator.Translator;

public abstract class Canceller {

    /**
     * Id für den Fehlertext, dass eine Berechnung abgebrochen wurde.
     */
    private static String CN_COMPUTATION_ABORTED = "CN_COMPUTATION_ABORTED";
    
    private Canceller() {
    }

    /**
     * Bricht den aktuellen Thread ab und wirft einen entsprechenden Fehler.
     * 
     * @throws CancellationException
     */
    public static void interruptComputationIfNeeded() {
        if (Thread.interrupted()) {
            throw new CancellationException(Translator.translateOutputMessage(CN_COMPUTATION_ABORTED));
        }
    }

}
