package Brains;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import Brain_Regions.Speed_Centre;
import TheBrain.Brain;
import TheBrain.Neuron;

public class Speed_Brain extends Brain {
    public static void main(String[] args) {
        System.out.println((10/3));
    }
    Speed_Centre speed_centre = new Speed_Centre(20, 6);
    int speed = 0;
    int phase = 0;
    int cycle = 0;

    
    public Speed_Brain() {
        brain_regions.add(speed_centre);

        set_positions_and_add_to_group(200);
    }
    
    @Override
    public void step() {
        pass_excitation();

        List<String> content = read_file_content("texts\\input.txt");
        if (content.size() != 0) {
            char[] charArray = content.get(0).toCharArray();
            speed = Character.getNumericValue(charArray[0]);
            phase = Character.getNumericValue(charArray[1]);
            cycle = 0;
            System.out.println("Speed: " + speed + " Phase: " + phase);
        
            clear_file_content("texts\\input.txt");
        }

        if (speed != 0) {
            cycle = cycle % (speed_centre.in_neurons.size()/speed + 1);
            int i = cycle*speed + phase - 1;
            if (i < speed_centre.in_neurons.size()) {
                Neuron neuron = speed_centre.in_neurons.get(i);
                cycle++;
                neuron.excite(1, clock);
                excited_neurons.offer(neuron);
                number_of__excited_neurons = excited_neurons.size();
            } else {
                cycle = 0;
            }
        }

        clock++;
    }

    void clear_file_content(String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path));) {
            writer.write("");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // public void set_positions_and_add_to_group() {
    //     group = new Group();
    //     int last_x = 20;
    //     int gap_between_layers = 200;
    //     Neurons_ArrayList first_input_layer = brain_regions.get(0).in_neurons;
    //     for (Neuron neuron : first_input_layer) {
    //         neuron.visual_neuron.center.x.set(last_x + neuron.visual_neuron.center.x.get());
    //         group.getChildren().add(neuron.visual_neuron.threshold_circle);
    //         group.getChildren().add(neuron.visual_neuron.excitation_circle);
    //     }
    //     last_x += gap_between_layers;
        
    //     for (Brain_Region brain_region : brain_regions) {
    //         int last = last_x;
    //         for(Neuron neuron : brain_region.hidden_neurons) {
    //             last = last_x + neuron.visual_neuron.center.x.get();
    //             neuron.visual_neuron.center.x.set(last);
    //             group.getChildren().add(neuron.visual_neuron.threshold_circle);
    //             group.getChildren().add(neuron.visual_neuron.excitation_circle);
    //         }
    //         last_x = last;
    //         last_x += gap_between_layers;
    
    //         for (Neuron neuron : brain_region.out_neurons) {
    //             neuron.visual_neuron.center.x.set(last_x + neuron.visual_neuron.center.x.get());
    //             group.getChildren().add(neuron.visual_neuron.threshold_circle);
    //             group.getChildren().add(neuron.visual_neuron.excitation_circle);
    //         }
    //     }
        
    //     for (Brain_Region brain_region : brain_regions) {
    //         for(Neuron neuron : brain_region.hidden_neurons) {
    //             for (Map.Entry<Neuron, Link> entry : neuron.links_in.entrySet()) {
    //                 Link link = entry.getValue();
    //                 group.getChildren().add(link.make_visual_link(clock).curve);
    //             }
    //         }
    //         for (Neuron neuron : brain_region.out_neurons) {
    //             for (Map.Entry<Neuron, Link> entry : neuron.links_in.entrySet()) {
    //                 Link link = entry.getValue();
    //                 group.getChildren().add(link.make_visual_link(clock).curve);
    //             }
    //         }
    //     }
    // }
}
