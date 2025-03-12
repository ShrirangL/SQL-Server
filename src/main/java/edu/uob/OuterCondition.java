package edu.uob;

public class OuterCondition {
    InnerCondition firstCondition;
    String booleanOperator;
    InnerCondition secondCondition;

    OuterCondition() {
        firstCondition = null;
        secondCondition = null;
        booleanOperator = "";
    }

    OuterCondition(InnerCondition condition) {
        firstCondition = condition;
        secondCondition = null;
        booleanOperator = "";
    }

    OuterCondition(InnerCondition condition1, String operator, InnerCondition condition2) {
        firstCondition = condition1;
        secondCondition = condition2;
        booleanOperator = operator;
    }
}
