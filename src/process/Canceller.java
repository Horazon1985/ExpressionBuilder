package process;

import exceptions.CancellationException;
import lang.translator.Translator;

public class Canceller {

    private Canceller() {
    }

    /**
     * Bricht den aktuellen Thread ab und wirft einen entsprechenden Fehler.
     * @throws CancellationException
     */
    public static void interruptComputationIfNeeded() {
        if (Thread.interrupted()) {
            throw new CancellationException(Translator.translateOutputMessage("MCC_COMPUTATION_ABORTED"));
        }
    }

}
