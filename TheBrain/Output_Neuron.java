package TheBrain;

import TheBrain.utils.Linked_HashSet_Queue;
import TheBrain.utils.Neurons_ArrayList;

public class Output_Neuron extends Neuron {
    // double excitation_depletion_rate = Hyper_Parameter.EXCITATION_DEPLETION_RATE.get_double()*50;
    
    public Output_Neuron(Neurons_ArrayList out_neurons) {
        super(out_neurons);
    }
    // @Override
    // public void pass_excitation(Linked_HashSet_Queue<Neuron> excited_neurons, int clock){
    //     // deplet_excitation(clock);
    //     if (excitation_level >= threshold_excitation_level) {
    //         excitation_level = threshold_excitation_level;
    //     }
    // }
}
