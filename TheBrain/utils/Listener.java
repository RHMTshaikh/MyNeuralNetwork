package TheBrain.utils;

public interface Listener {
    void on(String event, Object... args);
}