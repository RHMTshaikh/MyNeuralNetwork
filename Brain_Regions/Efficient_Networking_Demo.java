package Brain_Regions;

import java.util.Collections;

import Brains.Hyper_Parameter;
import TheBrain.Brain_Region;
import TheBrain.Link;
import TheBrain.Neuron;

public class Efficient_Networking_Demo extends Brain_Region {
    public Efficient_Networking_Demo(int number_of_inputs, int number_of_outputs, int internal_neurons) {
        super(number_of_inputs, number_of_outputs, internal_neurons);
        Neuron last_internal_neuron = hidden_neurons.get(hidden_neurons.size() - 1);
        Neuron first_internal_neuron = hidden_neurons.get(0);

        for (int i = 0; i < in_neurons.size(); i++) {
            Neuron n = in_neurons.get(i);
            n.link_to(first_internal_neuron, 1.1).make_link_constant();
            n.make_neuron_constant();
        }
        for (int i = 0; i < out_neurons.size(); i++) {
            Neuron n = out_neurons.get(i);
            last_internal_neuron.link_to(n, 1.1).make_link_constant();
            n.make_neuron_constant();
        }

        set_input_neurons_positions();
        set_hidden_neurons_positions(number_of_inputs, 1);
        set_output_neurons_positions();

        int radius = 300;
        int center_x = radius;
        int center_y = radius;
        double phase = -Math.PI/2;
        for(int i = 0; i < hidden_neurons.size(); i++) {
            Neuron n = hidden_neurons.get(i);
            n.make_neuron_constant();
            int x = (int)(center_x + radius * Math.cos(Math.PI * i / hidden_neurons.size() + phase));
            int y = (int)(center_y + radius * Math.sin(Math.PI * i / hidden_neurons.size()+ phase));
            n.visual_neuron.set_center(x, y);
        }

        // set_hidden_neurons_positions(internal_neurons, 1);
        for(int i = 1; i < hidden_neurons.size(); i++) {
            Neuron n = hidden_neurons.get(i);
            Neuron n1 = hidden_neurons.get(i-1);
            n1.link_to(n, 1.1);
        }
        for(int i = 0; i < out_neurons.size(); i++) {
            Neuron n = out_neurons.get(i);
            n.visual_neuron.set_center(radius, 2*radius);
        }
    }


    @Override
    public void fire_together_wire_together(int clock) {
        double new_link_weight = Hyper_Parameter.NEW_LINK_WEIGHT.get_double();
        double link_weight_reduction = Hyper_Parameter.LINK_WEIGHT_REDUCTION.get_double();

        new_link_weight  =  0.2;
        link_weight_reduction = 0.2;

        // Collections.sort(hidden_neurons);
        
        int wired = 0;
        int weakened = 0;

        for (int i = 0; i < hidden_neurons.size(); i++) {
            Neuron n1 = hidden_neurons.get(i);
            for (int j = i+1; j < hidden_neurons.size(); j++) {
                Neuron n2 = hidden_neurons.get(j);

                if (Math.random() < 0.5) continue;
                    

                if (fire_together(n1, n2)) {
                    Link link = n1.get_link_to(n2);
                    if (link == null) {
                        link = n1.link_to(n2, new_link_weight);
                        link.make_visual_link(clock);
                    }
                    wired++;

                    link = n2.get_link_to(n1);
                    if (link != null) {
                        link.weaken_by(link_weight_reduction);
                        weakened++;
                    }
                }
                if (fire_together(n2, n1)) {
                    Link link;
                    link = n2.get_link_to(n1);
                    if (link == null) {
                        link = n2.link_to(n1, new_link_weight);
                        link.make_visual_link(clock);
                    } else {
                        // link.strengthen_by(new_link_weight);
                    }
                    wired++;

                    link = n1.get_link_to(n2);
                    if (link != null) {
                        link.weaken_by(link_weight_reduction);
                        weakened++;
                    }
                }
            }
        }
        System.out.println("Wired: " + wired + " Weakened: " + weakened);
    }

    @Override
    protected double distance_between(Neuron n1, Neuron n2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'distance_between'");
    }

    @Override
    public Link make_input_connection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_input_connection'");
    }

    @Override
    public Link make_input_connection(double weight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_input_connection'");
    }

    @Override
    public Link make_internal_connection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_internal_connection'");
    }

    @Override
    public Link make_internal_connection(double weight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_internal_connection'");
    }

    @Override
    public Link make_output_connection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_output_connection'");
    }

    @Override
    public Link make_output_connection(double weight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_output_connection'");
    }
}