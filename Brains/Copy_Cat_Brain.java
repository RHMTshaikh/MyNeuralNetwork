package Brains;

import java.util.List;
import java.util.Optional;

import Brain_Regions.Copy_Cat_Centre;
import TheBrain.Brain;
import TheBrain.Brain_Region;
import TheBrain.Neuron;

public class Copy_Cat_Brain extends Brain{
    public static void main(String[] args) {
        boolean[] a = new boolean[10];
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }
    Copy_Cat_Centre copy_cat_centre = new Copy_Cat_Centre(10, 10, 20, 5);
    
    public Copy_Cat_Brain() {
        brain_regions.add(copy_cat_centre);
        set_positions_and_add_to_group(200);
    }

    public void step() {
        pass_excitation();

        List<String> content = read_file_content("texts\\input.txt");
        char ch = '\0';
        int num=0;
        if (content.size() != 0) {
            char[] charArray = content.get(0).toCharArray();
            ch = charArray.length == 0 ? '\0' : charArray[0];
            num = Character.getNumericValue(ch);
        }
        if (num == 0 ) {
            clock++;
            return;
        }
        Neuron neuron = copy_cat_centre.in_neurons.get(num - 1);
        neuron.excite(1, clock);
        excited_neurons.offer(neuron);
        number_of__excited_neurons = excited_neurons.size();

        for (Brain_Region brain_region : brain_regions) {
            brain_region.fire_together_wire_together(clock);
            for (int i = 0; i < 1; i++) {
                Optional.ofNullable(brain_region.make_input_connection(Hyper_Parameter.WEIGHT.get_double()/5)).ifPresent(connection -> connection.make_visual_link(clock));
                Optional.ofNullable(brain_region.make_internal_connection(Hyper_Parameter.WEIGHT.get_double()/5)).ifPresent(connection -> connection.make_visual_link(clock));
                Optional.ofNullable(brain_region.make_output_connection(Hyper_Parameter.WEIGHT.get_double()/5)).ifPresent(connection -> connection.make_visual_link(clock));
            }
        }

        boolean[] outputs = new boolean[copy_cat_centre.out_neurons.size()]; // all false
        boolean[] correct_outputs = new boolean[copy_cat_centre.out_neurons.size()];

        // if (num != 0) correct_outputs[copy_cat_centre.out_neurons.size() - num ] = true;
        if (num != 0) correct_outputs[ num - 1 ] = true;
        for (int i = 0; i < copy_cat_centre.out_neurons.size(); i++) {
            outputs[i] = copy_cat_centre.out_neurons.get(i).is_excited(clock);
        }

        back_prop_repletion(copy_cat_centre.out_neurons, outputs, correct_outputs);
        clock++;
    }
    
}


