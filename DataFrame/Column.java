package DataFrame;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Column {
    public static void main(String[] args) {
        Class<?> c = Column.class;
        try {
            System.out.println(c.getClassLoader().loadClass("DataFrame.Column"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public String heading;
    public String dataType;
    public ArrayList<Object> dataRows = new ArrayList<Object>();

    Column(){};

    Column(String heading) {
        this.heading = heading;
    }
    public Column(String heading, String dataType, ArrayList<Object> dataRow) {
        this.heading = heading;
        this.dataType = dataType;
        this.dataRows = dataRow;
    }
    Column(String heading, Object object, ArrayList<Object> dataRow) {
        this.heading = heading;
        this.dataType = object.getClass().getSimpleName();
        this.dataRows = dataRow;
    }

    public void addData(Object data) {
        this.dataRows.add(data);
    }

    public void addData(int index, Object data) {
        this.dataRows.add(index, data);
    }

    public long calculateSize() {
        long size = 0;

        size += Agent.getObjectSize(this);

        size += Agent.getObjectSize(this.heading);
        size += Agent.getObjectSize(this.dataType);

        for (Object data : this.dataRows) {
            if (data instanceof TreeMap) {
                size += calculateTreeMapSize((TreeMap<?, ?>) data);
            } else if (data instanceof int[][]) {
                size += calculate2DArraySize((int[][]) data);
            } else {
                size += Agent.getObjectSize(data); // For other objects
            }
        }

        return size;
    }

    private long calculateTreeMapSize(TreeMap<?, ?> treeMap) {
        long size = Agent.getObjectSize(treeMap); // Shallow size of the TreeMap

        for (Map.Entry<?, ?> entry : treeMap.entrySet()) {
            size += Agent.getObjectSize(entry); // Shallow size of the entry
            size += Agent.getObjectSize(entry.getKey()); // Size of the key
            size += Agent.getObjectSize(entry.getValue()); // Size of the value
        }

        return size;
    }

    private long calculate2DArraySize(int[][] array) {
        long size = Agent.getObjectSize(array); // Shallow size of the outer array
    
        for (int[] innerArray : array) {
            size += Agent.getObjectSize(innerArray); // Shallow size of each inner array
        }
    
        return size;
    }
}
