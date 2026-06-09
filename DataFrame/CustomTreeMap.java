package DataFrame;

import java.util.Map;
import java.util.TreeMap;

public class CustomTreeMap extends TreeMap<Integer, Integer> {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<Integer, Integer> entry : this.entrySet()) {
            sb.append(entry.getKey())
              .append("=")
              .append(entry.getValue())
              .append("; ");
        }
        if (!this.isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove trailing semicolon and space
        }
        sb.append("}");
        return sb.toString();
    }

    public void merge(CustomTreeMap map){
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            this.put(entry.getKey(),  (entry.getValue() + this.getOrDefault(entry.getKey(), 0)));
        }

    }    
    
}
