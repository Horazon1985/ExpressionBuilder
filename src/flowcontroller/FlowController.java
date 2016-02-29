package flowcontroller;

import exceptions.EvaluationException;
import lang.translator.Translator;

public final class FlowController {

    private FlowController() {
    }

    /**
     * Bricht den aktuellen Thread ab und wirft einen entsprechenden Fehler.
     *
     * @throws EvaluationException
     */
    public static void interruptComputationIfNeeded() throws EvaluationException {
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateOutputMessage("FC_FlowController_COMPUTATION_ABORTED"));
        }
    }

}
