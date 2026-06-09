package Brain_Regions;

import TheBrain.Feed_Forward_Network;

public class Copy_Cat_Centre extends Feed_Forward_Network {

    public Copy_Cat_Centre(int number_of_inputs, int number_of_outputs, int verticle_neuron_count, int horizontal_neuron_count) {
        super(number_of_inputs, number_of_outputs, verticle_neuron_count, horizontal_neuron_count);
        
        int c = 0;
        
        for (int i = 0; i < number_of_inputs*3; i++) {
            if (make_input_connection() != null) c++;
        }
        for (int i = 0; i < hidden_neurons.size()*2; i++) {
            if (make_internal_connection() != null) c++;
        }
        for (int i = 0; i < number_of_outputs*5; i++) {
            if (make_output_connection() != null) c++;
        }
        System.out.println("Connections made: " + c);        
    }
    
}
