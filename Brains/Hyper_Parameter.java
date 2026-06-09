package Brains;

public enum Hyper_Parameter {
    //Neuron
    THRESHOLD_EXCITATION_LEVEL(1.0, Double.class),
    EXCITATION_DEPLETION_RATE(0.3, Double.class),
    THRESHOLD_DEPLETION_RATE(0.00, Double.class),
    EXCITATION_LEVEL(0.0, Double.class),
    //Link
    WEIGHT(0.5, Double.class),
    WEIGHT_THRESHOLD(THRESHOLD_EXCITATION_LEVEL.get_double()*2, Double.class),
    DEPLETION_RATE(0.00001, Double.class),
    REPLETION_RATE(0.001, Double.class),
    // WRONG_FEEDBACK_RATE(0.1, Double.class),
    // CORRECT_FEEDBACK_RATE(0.1, Double.class),
    BACK_PROP_REPLETION_RATE(0.01,Double.class),
    // BACK_PROP_DEPLETION_RATE(0.01, Double.class),
    //Brain Region
    FIRE_TOGETHER_THRESHOLD(2, Integer.class),
    NEW_LINK_WEIGHT(0.05, Double.class),
    LINK_WEIGHT_REDUCTION(0.05, Double.class),
    //Brain
    CORRECTION_RATE(0.1, Double.class);


    private final Object value;
    private final Class<?> type;

    private Hyper_Parameter(Object value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    public Double get_double() {
        if (type == Double.class) {
            return (Double) value;
        }
        throw new IllegalStateException("Not a double parameter");
    }

    public Integer get_int() {
        if (type == Integer.class) {
            return (Integer) value;
        }
        throw new IllegalStateException("Not an integer parameter");
    }

    public Boolean get_boolean() {
        if (type == Boolean.class) {
            return (Boolean) value;
        }
        throw new IllegalStateException("Not a boolean parameter");
    }

}
