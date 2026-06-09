package TheBrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.text.Position;

import Brains.Hyper_Parameter;
import TheBrain.utils.Neurons_ArrayList;
import Visualisation.Point;

public class Feed_Forward_Network extends Brain_Region {

    List<Neurons_ArrayList> hidden_layers = new ArrayList<>();
    int verticle_neuron_count;
    int horizontal_neuron_count;

    int gap_verticle = 50;
    int gap_horizontal = 200;

    double max_distance_to_be_linked = (Math.pow(gap_verticle, 2) + Math.pow(gap_horizontal, 2));

    protected Feed_Forward_Network(Neurons_ArrayList input_neurons, int number_of_outputs, int verticle_neuron_count, int horizontal_neuron_count) {
        super(input_neurons, number_of_outputs, verticle_neuron_count*horizontal_neuron_count);

        this.verticle_neuron_count = verticle_neuron_count;
        this.horizontal_neuron_count = horizontal_neuron_count;
        
        for (int i = 0; i < horizontal_neuron_count; i++) {
            Neurons_ArrayList layer = new Neurons_ArrayList();
            for (int j = 0; j < verticle_neuron_count; j++) {
                layer.add(hidden_neurons.get(i*verticle_neuron_count + j));
            }
            hidden_layers.add(layer);
        }
        
        make_feed_forward_archetecture(verticle_neuron_count, horizontal_neuron_count);
    }
    public Feed_Forward_Network(int number_of_inputs, int number_of_outputs, int verticle_neuron_count, int horizontal_neuron_count) {
        super(number_of_inputs, number_of_outputs, verticle_neuron_count*horizontal_neuron_count);
        
        this.verticle_neuron_count = verticle_neuron_count;
        this.horizontal_neuron_count = horizontal_neuron_count;
        
        for (int i = 0; i < horizontal_neuron_count; i++) {
            Neurons_ArrayList layer = new Neurons_ArrayList();
            for (int j = 0; j < verticle_neuron_count; j++) {
                layer.add(hidden_neurons.get(i*verticle_neuron_count + j));
            }
            hidden_layers.add(layer);
        }
        make_feed_forward_archetecture(verticle_neuron_count, horizontal_neuron_count);
    }

    void make_feed_forward_archetecture(int verticle_neuron_count, int horizontal_neuron_count) {
        set_input_neurons_positions();
        set_hidden_neurons_positions_in_feed_forward_archetecture(verticle_neuron_count, horizontal_neuron_count);
        set_output_neurons_positions();
    }
    
    void set_hidden_neurons_positions_in_feed_forward_archetecture(int verticle_neuron_count, int horizontal_neuron_count) {
        for (int h = 0; h < horizontal_neuron_count; h++) {
            for (int v = 0; v < verticle_neuron_count; v++) {
                hidden_neurons.get(h*verticle_neuron_count + v).visual_neuron.set_center(h*gap_horizontal, v*gap_verticle);
            }
        }
    }

    public void fire_together_wire_together(int clock) {
        double new_link_weight = Hyper_Parameter.NEW_LINK_WEIGHT.get_double();
        double link_weight_reduction = Hyper_Parameter.LINK_WEIGHT_REDUCTION.get_double();

        for (Neurons_ArrayList layer : hidden_layers) {
            Collections.sort(layer);
        }
        
        int wired = 0;
        int weakened = 0;
        for (int i = 1; i < hidden_layers.size(); i++) {
            Neurons_ArrayList layer1 = hidden_layers.get(i-1);
            Neurons_ArrayList layer2 = hidden_layers.get(i);
            
            for (int j = 0; j < layer1.size(); j++) {
                Neuron n1 = layer1.get(j);

                if (n1.last_excitation_time < clock - fire_together_threshold) break;

                for (int k = 0; k < layer2.size(); k++) {
                    Neuron n2 = layer2.get(k);

                    if (n1.last_excitation_time + fire_together_threshold < n2.last_excitation_time) continue;
                    if (n1.last_excitation_time > n2.last_excitation_time) break;
                    if (distance_between(n1, n2) > max_distance_to_be_linked) continue;
                    
                    if (Math.random()*100 > 90) continue;

                    if (!n1.is_linked_to(n2)) {
                        Link link = n1.link_to(n2, new_link_weight);
                        if (link == null) continue;
                        link.make_visual_link(clock);
                        wired++;
                    }
                    if (n2.is_linked_to(n1)) {
                        Link link = n2.get_link_to(n1);
                        link.decrease_weight_by(link_weight_reduction);
                        weakened++;
                    }
                }
            }
        }
        System.out.println("Wired: " + wired + " Weakened: " + weakened);
    }

    public Link make_input_connection() {
        int tries = in_neurons.size();
        int index1 = get_random_int(0, in_neurons.size());
        int index2 = get_random_int( 0,  verticle_neuron_count);

        while (in_neurons.get(index1).is_linked_to(hidden_neurons.get(index2))) {
            if (--tries == 0) return null;
            index2 = get_random_int( 0,  verticle_neuron_count);
        }
        return in_neurons.get(index1).link_to(hidden_neurons.get(index2));
    }
    public Link make_input_connection(double weight) {
        int tries = in_neurons.size();
        int index1 = get_random_int(0, in_neurons.size());
        int index2 = get_random_int( 0,  verticle_neuron_count);

        while (in_neurons.get(index1).is_linked_to(hidden_neurons.get(index2))) {
            if (--tries == 0) return null;
            index2 = get_random_int( 0,  verticle_neuron_count);
        }
        return in_neurons.get(index1).link_to(hidden_neurons.get(index2), weight);
    }
    
    public Link make_internal_connection() {
        int tries = verticle_neuron_count;
        int index1 = get_random_int(0, verticle_neuron_count);
        int index2 = get_random_int(0, verticle_neuron_count);

        int index3 = get_random_int(0, hidden_layers.size()-1);
        Neurons_ArrayList layer1 = hidden_layers.get(index3);
        Neurons_ArrayList layer2 = hidden_layers.get(index3+1);
        
        while ( index2 < 0 || index2 >= verticle_neuron_count || layer1.get(index1).is_linked_to(layer2.get(index2)) || Math.abs(index1-index2) > 4) {
            if (--tries == 0) return null;    
            index2 = get_random_int(index1-4, index1+4);
            // index2 = get_random_int(0, verticle_neuron_count);
        }
        return layer1.get(index1).link_to(layer2.get(index2));
    }
    public Link make_internal_connection(double weight) {
        int tries = verticle_neuron_count;
        int index1 = get_random_int(0, verticle_neuron_count);
        int index2 = get_random_int(0, verticle_neuron_count);
        
        int index3 = get_random_int(0, hidden_layers.size()-1);
        Neurons_ArrayList layer1 = hidden_layers.get(index3);
        Neurons_ArrayList layer2 = hidden_layers.get(index3+1);
        
        while ( index2 < 0 || index2 >= verticle_neuron_count || layer1.get(index1).is_linked_to(layer2.get(index2)) || Math.abs(index1-index2) > 4) {
            if (--tries == 0) return null;    
            index2 = get_random_int(index1-4, index1+4);
            // index2 = get_random_int(0, verticle_neuron_count);
        }
        return layer1.get(index1).link_to(layer2.get(index2), weight);
    }
    
    public Link make_output_connection() {
        int tries = out_neurons.size();
        int index1 = get_random_int( 0,  verticle_neuron_count);
        int index2 = get_random_int(0, out_neurons.size());

        Neurons_ArrayList last_layer = hidden_layers.get(hidden_layers.size()-1);

        while (last_layer.get(index1).is_linked_to(out_neurons.get(index2))) {
            if (--tries == 0) return null;
            index2 = get_random_int(0, out_neurons.size());
        }
        return last_layer.get(index1).link_to(out_neurons.get(index2));
    }
    public Link make_output_connection(double weight) {
        int tries = out_neurons.size();
        int index1 = get_random_int( 0,  verticle_neuron_count);
        int index2 = get_random_int(0, out_neurons.size());
        
        Neurons_ArrayList last_layer = hidden_layers.get(hidden_layers.size()-1);
        
        while (last_layer.get(index1).is_linked_to(out_neurons.get(index2))) {
            if (--tries == 0) return null;
            index2 = get_random_int(0, out_neurons.size());
        }
        return last_layer.get(index1).link_to(out_neurons.get(index2), weight);
    }
    @Override
    protected double distance_between(Neuron n1, Neuron n2) {
        Point p1 = n1.visual_neuron.center;
        Point p2 = n2.visual_neuron.center;
        int x = p1.x.get() - p2.x.get();
        int y = p1.y.get() - p2.y.get();
        return  (x*x + y*y);
    }
}
