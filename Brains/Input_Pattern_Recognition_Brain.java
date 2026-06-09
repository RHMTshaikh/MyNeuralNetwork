package Brains;

import java.io.IOException;

import Brain_Regions.Input_Pattern_Recognition;
import TheBrain.Brain;
import TheBrain.Neuron;

public class Input_Pattern_Recognition_Brain extends Brain{
    int number_of_inputs = 50;
    int number_of_outputs = number_of_inputs;
    int verticle_neuron_count = number_of_inputs;
    int horizontal_neuron_count = 5;
    Input_Pattern_Recognition input_pattern_recognition_centre = new Input_Pattern_Recognition(number_of_inputs, number_of_outputs, verticle_neuron_count, horizontal_neuron_count);

    public Input_Pattern_Recognition_Brain () {
        brain_regions.add(input_pattern_recognition_centre);
        set_positions_and_add_to_group(200);
    }
    
    @Override
    public void step() {
        pass_excitation();

        String output_pattern = null;
        try {
            output_pattern = input_pattern_recognition_centre.excite_input_layer(excited_neurons, clock);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        number_of__excited_neurons = excited_neurons.size();

        input_pattern_recognition_centre.fire_together_wire_together(clock);

        boolean[] outputs = new boolean[input_pattern_recognition_centre.out_neurons.size()]; // all false
        boolean[] correct_outputs = new boolean[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = input_pattern_recognition_centre.out_neurons.get(i).is_excited(clock);
            correct_outputs[i] = output_pattern.charAt(i) == '1';
        }


        back_prop_repletion(input_pattern_recognition_centre.out_neurons, outputs, correct_outputs);

        clock++;
    }
    
}
