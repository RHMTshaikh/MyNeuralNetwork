package Brain_Regions;

import TheBrain.Brain_Region;
import TheBrain.Link;
import TheBrain.Neuron;

public class Counting_Centre extends Brain_Region {
    public Counting_Centre(int number_of_inputs, int number_of_outputs, int internal_neurons) {
        super(number_of_inputs, number_of_outputs, internal_neurons);
        if (number_of_inputs != number_of_outputs || number_of_outputs != internal_neurons || internal_neurons != number_of_inputs) {
            throw new IllegalArgumentException("Invalid arguments for Counting_Centre constructor.");
        }
        set_links_out_limit();
        make_connections();
        set_input_neurons_positions();
        set_hidden_neurons_positions(number_of_inputs, 1);
        set_output_neurons_positions();
    }

    void make_connections() {
        for (int i = 0; i < in_neurons.size(); i++) {
            for (int j = 0; j < hidden_neurons.size(); j++) {
                Link link = in_neurons.get(i).link_to( hidden_neurons.get(j), 1);
                link.depletion_rate = 0;
                link.repletion_rate = 0;
            }
        }
        for (int i = 1; i < hidden_neurons.size(); i++) {
            Link link1 = hidden_neurons.get(i).link_to( out_neurons.get(i-1), -1.0);
            Link link2 =  hidden_neurons.get(i).link_to( out_neurons.get(i), 1.0);
            link1.depletion_rate = 0;
            link1.repletion_rate = 0;
            link2.depletion_rate = 0;
            link2.repletion_rate = 0;
        }
        Link link = hidden_neurons.get(0).link_to( out_neurons.get(0), 1.0);
        link.depletion_rate = 0;
        link.repletion_rate = 0;
    }

    void set_links_out_limit() {
        for (int i = 0; i < in_neurons.size(); i++) {
            Neuron n = in_neurons.get(i);
            n.threshold_depletion_rate = 0;
            n.threshold_excitation_level = 1;
        }
        for (int i = 0; i < hidden_neurons.size(); i++) {
            Neuron n = hidden_neurons.get(i);
            n.threshold_depletion_rate = 0;
            n.threshold_excitation_level = 1*(i+1);
        }
        for (int i = 0; i < out_neurons.size(); i++) {
            Neuron n = out_neurons.get(i);
            n.threshold_depletion_rate = 0;
            n.threshold_excitation_level = 1;
        }
    }

    @Override
    public void fire_together_wire_together(int clock) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fire_together_wire_together'");
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
