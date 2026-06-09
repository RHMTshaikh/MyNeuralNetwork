package DataFrame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Data_Frame implements Iterable<Row_With_Index> {

    public static void main(String[] args) {
    }
    
    private int itrationIndex = 0;
    public void decrement_itration_index(){
        itrationIndex--;
    }
    public void increment_itration_index(){
        itrationIndex++;
    }

    @Override
    public Iterator<Row_With_Index> iterator() {
        return new Iterator<Row_With_Index>() {
            // private int itrationIndex = 0;

            @Override
            public boolean hasNext() {
                if (itrationIndex >= columns.get(0).dataRows.size()) {
                    itrationIndex = 0;
                    return false;
                }
                return itrationIndex < columns.get(0).dataRows.size();
            }

            @Override
            public Row_With_Index next() {
                return new Row_With_Index(new Row(Data_Frame.this, itrationIndex), itrationIndex++);
            }
        };
    }
    
    
    protected List<Column> columns;

    public Data_Frame(){}
    
    public Data_Frame(String Filepath, String[] dataTypes) {
        if( !(Filepath.endsWith(".csv")) ){
           throw new IllegalArgumentException("Filepath should be a csv file");
        }
        File file = new File(Filepath);
        if( !file.exists() ){
            throw new IllegalArgumentException("File does not exist");
        }
        // Read the file and get the headings and rows
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            if ((line = reader.readLine()) != null) {
                String[] headings = line.split(",");
                if (headings.length != dataTypes.length) {
                    throw new IllegalArgumentException("Number of headings does not match number of data types");
                }
                for (int i = 0; i < headings.length; i++) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> dataRows =  arrayListOfType(dataTypes[i]);
                    this.columns.add(new Column(headings[i], dataTypes[i], dataRows));
                }
            }

            while ((line = reader.readLine()) != null) {
                String[] dataPoints = line.split(",");
                for (int i = 0; i < dataTypes.length; i++) {
                    columns.get(i).dataRows.add(convertStringTo(dataTypes[i] ,dataPoints[i]));
                }
            }
            for (int i = 0; i < columns.size(); i++) {
                columns.get(i).dataRows.add(convertStringTo(dataTypes[i], line));;
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getName() + " - " + e.getMessage());
        }
    }

    public Data_Frame(Object object) {
        if (object instanceof String) {
            throw new IllegalArgumentException("Object cannot be a string");
        }
        for (Field field : object.getClass().getFields()) {
            try {
                @SuppressWarnings("unchecked")
                ArrayList<Object> dataRows = arrayListOfType(field.getType().getSimpleName());
                dataRows.add(field.get(object));
                this.columns.add(new Column(field.getName(), field.getType().getSimpleName(), dataRows));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Data_Frame(ArrayList<?> arrayListOfObjects) throws IllegalArgumentException, IllegalAccessException {
        if (arrayListOfObjects == null || arrayListOfObjects.size() == 0) {
            throw new IllegalArgumentException("ArrayList is Null or Empty");            
        }
        Object object = arrayListOfObjects.get(0);
        System.out.println(arrayListOfObjects.getClass().getSimpleName());
        for (Field field : object.getClass().getFields()) {
            @SuppressWarnings("unchecked")
            ArrayList<Object> dataRow = arrayListOfType(field.getType().getSimpleName());
            dataRow.add(field.get(object));
            this.columns.add(new Column(field.getName(), field.getType().getSimpleName(), dataRow));
        }
        for (int i = 1; i < arrayListOfObjects.size(); i++) {
            for (Column column : columns) {
                try {
                    column.dataRows.add(arrayListOfObjects.get(i).getClass().getField(column.heading).get(arrayListOfObjects.get(i)));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public Data_Frame(String[] headings, String[] dataTypes){
        if (headings.length != dataTypes.length) {
            throw new IllegalArgumentException("number of datatypes and headings are not equal");
        }
        for (int i = 0; i < dataTypes.length; i++) {
            Column newColumn = new Column();
            newColumn.heading = headings[i];
            newColumn.dataType = dataTypes[i];
            newColumn.dataRows = arrayListOfType(dataTypes[i]);
            this.columns.add(newColumn);
        }
    }

    public Data_Frame (Row row){
        for (Cell cell : row.cells) {
            @SuppressWarnings("unchecked")
            ArrayList<Object> dataRows = arrayListOfType(cell.dataType);
            columns.add(new Column(cell.heading, cell.dataType, dataRows));
            
        }
    }


    public long calculateSize() {
        long size = 0;
        
        size += Agent.getObjectSize(this);

        for (Column column : columns) {
            size += column.calculateSize();
        }

        return size;
    }

    @SuppressWarnings("rawtypes")
    protected static ArrayList arrayListOfType(String type) {
        switch (type.toLowerCase()) {
            case "int":
                return new ArrayList<Integer>();
            case "integer":
                return new ArrayList<Integer>();
            case "double":
                return new ArrayList<Double>();
            case "[[i":
                return new ArrayList<int[][]>();
            case "int[][]":
                return new ArrayList<int[][]>();
            case "float":
                return new ArrayList<Float>();
            case "string":
                return new ArrayList<String>();
            case "boolean":
                return new ArrayList<Boolean>();
            case "bool":
                return new ArrayList<Boolean>();
            case "treemap":
                return new ArrayList<CustomTreeMap>();
            default:
                throw new IllegalArgumentException("Invalid data type");
        }
    }

    protected static Object convertStringTo(String type, String value) {
        switch (type.toLowerCase()) {
            case "int": case "integer":
                return Integer.parseInt(value);
            case "float":
                return Float.parseFloat(value);
            case "double":
                return Double.parseDouble(value);
            case "string":
                return value;
            case "boolean": case "bool":
                return Boolean.parseBoolean(value);
            case "treemap":
                return fromStringToMaps(value);
            case "[[i": case "int[][]":
                return fromStringToArray(value);
            default:
                throw new IllegalArgumentException("Invalid data type: " + type);
        }
    }

    public void append(Data_Frame df) {
        if (this.columns.size() != df.columns.size()) {
            throw new IllegalArgumentException("DataFrames have different number of columns");
        }

        for (int i = 0; i < this.columns.size(); i++) {
            if (!this.columns.get(i).dataType.equals(df.columns.get(i).dataType)) {
                throw new IllegalArgumentException("DataFrames have different data types at column " + i);
            }
        }

        for (Row_With_Index rowAIndex : df) {
            addRow(rowAIndex.row);
        }
    }
    
    public static CustomTreeMap fromStringToMaps(String input) {
        // String input = "{1=2; 2=4}";
        CustomTreeMap treeMap = new CustomTreeMap();
    
        // Remove '{' and '}' from the input
        input = input.substring(1, input.length() - 1);
    
        // Split entries by "; " (semicolon followed by a space)
        String[] entries = input.split("; ");
    
        for (String entry : entries) {
            // Split each entry into key and value by '='
            String[] keyValue = entry.split("=");
            
            // Parse key and value as integers and add them to the map
            Integer key = Integer.parseInt(keyValue[0].trim());
            Integer value = Integer.parseInt(keyValue[1].trim());
            treeMap.put(key, value);
        }
    
        return treeMap;
    }
    
    public static int[][] fromStringToArray(String input) {
        // String input = "[[1 2 3] [4 5 6]]";

        input = input.substring(2, input.length() - 2); // Remove '[[' and ']]'
        
        String[] rows = input.split("\\]\\[");

        int[][] array = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            String[] values = rows[i].split(" ");
            array[i] = new int[values.length];
            for (int j = 0; j < values.length; j++) {
                array[i][j] = Integer.parseInt(values[j]);
            }
        }

        return array;
    }
    
    public void to_csv_overwrite(String path) {
        try {
            File file = new File(path);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            StringBuilder sb = new StringBuilder();
            for (Column column : columns) {
                sb.append(column.heading + ",");
            }
            sb.setLength(sb.length()-1);
            sb.append("\n");
            
            for (int i = 0; i < columns.get(0).dataRows.size(); i++) {
                for (Column column : columns) {
                    
                    if (column.dataType.equals("[[I") || column.dataType.equals("int[][]")) {

                        sb.append(("["));
                        int[][] array = (int[][]) column.dataRows.get(i);
                        
                        for (int[] row : array) {
                            sb.append("[");
                            for (int val : row) {
                                sb.append(val + " ");
                            }
                            sb.append("]");
                        }
                        sb.append("],");

                    } else {
                        sb.append(column.dataRows.get(i).toString() + ",");
                    }
                }
                sb.setLength(sb.length()-1);
                sb.append("\n");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(sb.toString());
            writer.close();

        } catch (IOException e) {
            System.err.println("Error writing to file: "  + e.getMessage());
        }
    }

    public void to_csv_append(String path) {
        try {
            File file = new File(path);
            
            if (!file.exists()) {
                file.createNewFile();
                to_csv_overwrite(path);
                return;
            }
    
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String existingLine = reader.readLine();
            
            if (existingLine == null) {
                reader.close();
                to_csv_overwrite(path);
                return;
            }
            
            String[] headings = existingLine.split(",");

            if (columns.size() != headings.length) {
                reader.close();
                throw new IllegalArgumentException("DataFrames have different number of columns");
            }
    
            for (int i = 0; i < columns.size(); i++) {
                if (!columns.get(i).heading.equals(headings[i])) {
                    reader.close();
                    throw new IllegalArgumentException("DataFrames have different headings at column " + i);
                }
            }
            reader.close();
    
            // Append new rows to the existing file
            BufferedWriter writer = new BufferedWriter(new FileWriter(path, true)); // Enable append mode
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < columns.get(0).dataRows.size(); i++) {
                for (Column column : columns) {
                    if (column.dataType.equals("[[I") || column.dataType.equals("int[][]")) {
                        sb.append("[");
                        int[][] array = (int[][]) column.dataRows.get(i);
                        for (int[] row : array) {
                            sb.append("[");
                            for (int val : row) {
                                sb.append(val + " ");
                            }
                            sb.append("]");
                        }
                        sb.append("],");
                    } else {
                        sb.append(column.dataRows.get(i).toString() + ",");
                    }
                }
                sb.setLength(sb.length()-1);
                sb.append("\n");
            }
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + path + " - " + e.getMessage());
        }
    }
    
    public int length(){
        return columns.get(0).dataRows.size();
    }
    
    public void addRow(Object object) {
        for (Column column : columns) {
            try {
                Object value = object.getClass().getField(column.heading).get(object);
                column.addData(value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRow(int index, Object object) {
        for (Column column : columns) {
            try {
                Object value = object.getClass().getField(column.heading).get(object);
                column.addData(index, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void addRow(Row row) {
        for (Column column : columns) {
            for (Cell cell : row.cells) {
                if (cell.heading.equals(column.heading)) {
                    column.dataRows.add(cell.data);
                }
                
            }
        }
    }
    
    public void addRow(int index, Row row) {
        for (Column column : columns) {
            for (Cell cell : row.cells) {
                if (cell.heading.equals(column.heading)) {
                    column.dataRows.add(index, cell.data);
                }
                
            }
        }
    }
    
    public void addRow(Object[] dataObjects){
        if (dataObjects.length != columns.size()) {
            throw new IllegalArgumentException("Number of datapoints is not equal to number of columns");
        }
        for (int i = 0; i < dataObjects.length; i++) {
            columns.get(i).dataRows.add(dataObjects[i]);
        }

    }

    @SuppressWarnings("unchecked")
    public void clear(){
        for (Column column : columns) {
            column.dataRows = arrayListOfType(column.dataType);
        }

    }

    public Column column(String heading) {
        for (Column column : columns) {
            if (column.heading.equals(heading)) {
                return column;
            }
        }
        throw new IllegalArgumentException("Column not found");
    }

    public int columnIndex(String heading) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).heading.equals(heading)) {
                return i;
            }
        }
        return -1;
    }
    
    public String head(){
        String s = "" ;
        for (Column column : columns) {
            s += column.heading;
            s += ", ";
        } 
        s += '\n';
        for (Column column : columns) {
            String heading = column.heading.toString();
            s += String.format("%-" + heading.length() + "s", column.dataType.substring(0, Math.min(column.dataType.length(), heading.length())));
            s += ", ";
        } 
        s += '\n';

        for (int i = 0; i < columns.get(0).dataRows.size() && i < 5 ; i++) {
            for (Column column : columns) {
                String heading = column.heading.toString();
                String data = column.dataRows.get(i).toString();
                s += String.format("%-" + heading.length() + "s", data.substring(0, Math.min(data.length(), heading.length())));
                s += ", ";
            }
            s += '\n';
        }
        return s;
    }

    public Row removeRow(int index){
        Row row = new Row();
        for (Column column : columns) {
            row.cells.add(new Cell(column.heading, column.dataType, column.dataRows.get(index)));
            column.dataRows.remove(index);
        }
        return row;
    }
    
    public void removeColumn(String heading) {
        columns.removeIf(column -> column.heading.equals(heading));
    }

    public Column addColumn(String heading, Object object) {
        @SuppressWarnings("unchecked")
        ArrayList<Object> dataRows = arrayListOfType(object.getClass().getSimpleName());
        dataRows.add(object);
        columns.add(new Column(heading, object, dataRows));
        return columns.get(columns.size() - 1);
    }

    public Row row(int index){
        if (index < 0) throw new IllegalArgumentException("index cannot be negative");
        if (index >= columns.get(0).dataRows.size()) throw new IllegalArgumentException("index " + index + " out of bound for datafram size " + columns.get(0).dataRows.size() );

        return new Row(this, index);
    }

    public Row lastRow(){

        if (this.isEmpty()) {
            throw new IllegalStateException("Data Frame is Empty");
        }
        return new Row(this, columns.get(0).dataRows.size() - 1);
    }

    public boolean isEmpty(){
        return columns.get(0).dataRows.size() == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Column column : columns) {
            sb.append(column.heading)
              .append("=")
              .append(column.dataRows)
              .append("; ");
        }
        return sb.toString();
    }
}
