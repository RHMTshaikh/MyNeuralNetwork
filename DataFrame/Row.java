package DataFrame;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class Row {
    public static void main(String[] args) {
        
    }
    ArrayList<Cell> cells = new ArrayList<>();
    
    Row(){}
    
    Row(String[] headings, String[] dataTypes, Object[] dataPoints){
        if (headings.length != dataTypes.length || dataTypes.length != dataPoints.length || headings.length != dataPoints.length) {
            throw new IllegalArgumentException("All arrays must have the same length");
        }

        for (int i = 0; i < dataPoints.length; i++) {
            cells.add(new Cell(headings[i], dataTypes[i], dataPoints[i]));
        }
    }

    Row(Data_Frame dataFrame, int index) {
        for (Column column : dataFrame.columns) {
           cells.add(new Cell(column.heading, column.dataType, column.dataRows.get(index)));
        }
    } 

    Row(Object object) throws IllegalArgumentException, IllegalAccessException {
        for (Field field : object.getClass().getFields()) {
            cells.add(new Cell(field.getName(), field.getType().getSimpleName(), field.get(object)));
        }
    }   

    public Object column(String heading){
        for (Cell cell : cells) {
            if (cell.heading.equals(heading)) {
                return cell.data;
            }
        }
        throw new IllegalArgumentException("Column not found");
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Cell cell : cells) {

            if (cell.dataType.equals("int[][]") || cell.dataType.equals("[[I")) {
                sb.append("[");

                for (int[] row :(int[][]) cell.data) {
                    sb.append("[");
                    for (int element : row) {
                        sb.append(element).append(' ');
                    }
                    sb.append("]");
                }
                sb.append("], ");
                sb.append(Arrays.toString((int[]) cell.data)).append(", ");
                continue;
            }
            sb.append(cell.data.toString()).append(", ");
        }
        
        return sb.toString();
    }
}
