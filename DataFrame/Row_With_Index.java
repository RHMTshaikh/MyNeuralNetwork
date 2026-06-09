package DataFrame;

public class Row_With_Index {
    public final Row row;
    public final int index;

    public Row_With_Index(Row row, int index) {
        this.row = row;
        this.index = index;
    }

    public static void main(String[] args) {
        // Example data
        String[] headings = {"Name", "Age", "Occupation"};
        String[] dataTypes = {"String", "Integer", "String"};
        Object[] dataPoints = {"Alice", 30, "Engineer"};

        // Create a Row object
        Row row = new Row(headings, dataTypes, dataPoints);

        // Print the Row object
        System.out.println(row);
    }
}

