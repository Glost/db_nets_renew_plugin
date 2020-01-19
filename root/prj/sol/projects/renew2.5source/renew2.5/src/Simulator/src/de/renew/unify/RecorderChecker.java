package de.renew.unify;

class RecorderChecker {
    boolean recorderUsed;

    RecorderChecker(StateRecorder recorder) {
        recorderUsed = (recorder != null);
    }

    void checkRecorder(StateRecorder recorder) {
        if (recorder == null) {
            if (recorderUsed) {
                throw new RuntimeException("Permanent action after undoable action. Strange.");
            }
        } else {
            // It would be possible to record the original
            // value in the state recorder. That way we could
            // unify undoably, undo, and then unify permanently.
            // But that is dangerous anyway and should be reported.
            // Hence we are conservative.
            recorderUsed = true;
        }
    }
}