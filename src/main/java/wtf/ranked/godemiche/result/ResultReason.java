package wtf.ranked.godemiche.result;

public interface ResultReason {

    enum Default implements ResultReason {
        SUCCEED
    }

    interface Failure extends ResultReason {

        enum Default implements Failure {
            NOT_PROVIDED_REASON
        }
    }
}
