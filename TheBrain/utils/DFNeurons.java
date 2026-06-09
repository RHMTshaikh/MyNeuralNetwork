package TheBrain.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import DataFrame.Column;
import DataFrame.Data_Frame;
import TheBrain.Neuron;

public class DFNeurons extends Data_Frame {

    public DFNeurons(){}
    
    public DFNeurons(String path) {
        String[] dataTypes = new String[] {"LinksArrayList", "double", "int", "double", "double"};

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            
            if ((line = reader.readLine()) != null) {

                String[] headings = line.split(",");

                if (headings.length != dataTypes.length) {
                    throw new IllegalArgumentException("Number of headings does not match number of data types");
                }

                for (int i = 0; i < headings.length; i++) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> dataRow = arrayListOfType(dataTypes[i]);
                    this.columns.add(new Column(headings[i], dataTypes[i], dataRow));
                }
            }

            while ((line = reader.readLine()) != null) {

                String[] dataPoints = line.split(",");

                if (dataPoints.length != dataTypes.length) {
                    throw new IllegalArgumentException("Number of data points does not match number of data types");
                }

                for (int i = 0; i < dataTypes.length; i++) {
                    if (dataTypes[i].equals("LinksArrayList")) {
                        columns.get(i).dataRows.add(convertStringTo("String" ,dataPoints[i]));
                        continue;                        
                    }
                    columns.get(i).dataRows.add(convertStringTo(dataTypes[i] ,dataPoints[i]));
                }
            }
            reader.close();

        } catch (IOException e) {
            System.err.println("Error reading file: " + path + " - " + e.getMessage());
        }
        
    }

    @SuppressWarnings("unchecked")
    public DFNeurons(ArrayList<Neuron> arrayListOfObjects) {
        if (arrayListOfObjects == null || arrayListOfObjects.size() == 0) {
            throw new IllegalArgumentException("ArrayList is Null or Empty");            
        }
        Object object = arrayListOfObjects.get(0);
        for (Field field : object.getClass().getFields()) {
            ArrayList<Object> dataRow = arrayListOfType(field.getType().getSimpleName());
            try {
                dataRow.add(field.get(object));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
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


    @SuppressWarnings("rawtypes")
    public static ArrayList arrayListOfType(String type) {
        switch (type.toLowerCase()) {
            case "linksarraylist":
                return new ArrayList<Links_ArrayList>();
            default:
                return Data_Frame.arrayListOfType(type);
        }
    }

    public void addNeurons(Neurons_ArrayList neurons) {
        if (columns.size() == 0) {
            for (Field field : neurons.get(0).getClass().getFields()) {
                @SuppressWarnings("unchecked")
                ArrayList<Object> dataRow = arrayListOfType(field.getType().getSimpleName());
                dataRow.add(field.getName());
                this.columns.add(new Column(field.getName(), field.getType().getSimpleName(), dataRow));
            }
            
        }
        for (Neuron neuron : neurons) {
            for (Field field : neuron.getClass().getFields()) {
                Column column = this.column(field.getName());
                try {
                    column.dataRows.add(field.get(neuron));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }   
        }
    }
}
