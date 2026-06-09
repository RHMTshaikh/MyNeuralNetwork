package TheBrain.utils;

import java.util.*;

public class PairDataStructure {
    private Map<Integer, Integer> map;

    public PairDataStructure() {
        map = new HashMap<>();
    }

    public void insert(int x, int y) {
        map.put(x, y);
        map.put(y, x);
    }

    public Integer get_pattern(int x) {
        return map.get(x);
    }
    public Integer get_char(int x) {
        return map.get(x);
    }

    public String toString() {
        return map.toString();
    }

}

