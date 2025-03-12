package edu.uob;

public class InnerCondition {
    public String attribute;
    public String comparator;
    public String value;

    InnerCondition() {
        attribute = "";
        comparator = "";
        value = "";
    }

    InnerCondition(String attribute, String comparator, String value) {
        this.attribute = attribute;
        this.comparator = comparator;
        this.value = value;
    }
}
