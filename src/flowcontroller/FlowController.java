package flowcontroller;

import exceptions.EvaluationException;
import translator.Translator;

public class FlowController {

    /**
     * Bricht den aktuellen Thread ab und wirft einen entsprechenden Fehler.
     * 
     * @throws EvaluationException 
     */
    public void interruptComputation() throws EvaluationException {
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("FC_FlowController_COMPUTATION_ABORTED"));
        }
    }

}
