package TheBrain.utils;

public interface Emitter {
    void emit(String event, Object... args);
}