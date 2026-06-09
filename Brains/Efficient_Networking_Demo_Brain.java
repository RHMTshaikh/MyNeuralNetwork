package Brains;

import Brain_Regions.Efficient_Networking_Demo;
import TheBrain.Brain;
import TheBrain.Neuron;

public class Efficient_Networking_Demo_Brain extends Brain {
    int number_of_inputs = 1;
    int number_of_outputs = 1;
    int internal_neurons = 50;

    Efficient_Networking_Demo efficient_networking_demo = new Efficient_Networking_Demo(number_of_inputs, number_of_outputs, internal_neurons);

    public Efficient_Networking_Demo_Brain() {
        brain_regions.add(efficient_networking_demo);
        set_positions_and_add_to_group(100);
    }

    @Override
    public void step() {
        pass_excitation();

        if (clock%9 == 0) {
            Neuron input_Neuron = efficient_networking_demo.in_neurons.get(0);
            input_Neuron.excite(1.1, clock);
            excited_neurons.offer(input_Neuron);
            number_of__excited_neurons = excited_neurons.size();
        }


        efficient_networking_demo.fire_together_wire_together(clock);

        // for (Neuron neuron : efficient_networking_demo.hidden_neurons) {
        //     if (neuron.is_excited()) {
        //         System.out.println("Neuron " + neuron.get_index() + " fired at clock " + clock);
        //     }
        // }
        clock++;
    }
    
}
