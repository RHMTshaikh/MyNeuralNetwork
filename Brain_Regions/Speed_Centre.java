package Brain_Regions;

import java.util.ArrayList;
import java.util.List;

import TheBrain.Brain_Region;
import TheBrain.Link;
import TheBrain.Neuron;
import TheBrain.utils.Neurons_ArrayList;
import Visualisation.Visual_Neuron;

public class Speed_Centre extends Brain_Region {
    List<Neurons_ArrayList> speed_lines = new ArrayList<>();


    public Speed_Centre(int number_of_inputs, int max_speed) {
        super(number_of_inputs, max_speed, (max_speed )*number_of_inputs);

        
        for (int i = 0; i < max_speed; i++) speed_lines.add(new Neurons_ArrayList());
        
        for (int i = 0; i < max_speed; i++) {
            Neurons_ArrayList line = speed_lines.get(i);
            
            for (int j = 0; j < number_of_inputs; j++) {
                Neuron neuron = hidden_neurons.get(i*number_of_inputs + j);
                line.add(neuron);
            }
            
            for (int j = i+1; j < number_of_inputs; j++) {
                line.get(j).threshold_excitation_level = 2.0;
            }
        }
        
        for (int i = 0; i < number_of_inputs; i++) {
            Neuron input_neuron = in_neurons.get(i);

            for (int j = 0; j < max_speed; j++) {
                Neuron neuron = speed_lines.get(j).get(i);
                input_neuron.link_to(neuron, 1.0).make_link_constant();;
            }
        }
        
        for (int i = 1; i <= max_speed; i++) {
            Neurons_ArrayList line = speed_lines.get(i-1);
            
            for (int j = 0; j+i < number_of_inputs; j++) {
                Neuron neuron1 = line.get(j);
                Neuron neuron2 = line.get(j+i);
                neuron1.link_to(neuron2, 1.0).make_link_constant();
                neuron2.link_to(out_neurons.get(i-1), 1.0).make_link_constant();;
            }

        }
        for (Neuron neuron : in_neurons) neuron.make_neuron_constant();
        for (Neuron neuron : hidden_neurons) neuron.make_neuron_constant();
        for (Neuron neuron : out_neurons) neuron.make_neuron_constant();
        
        set_input_neurons_positions();
        set_hidden_neurons_positions(number_of_inputs, max_speed);
        set_output_neurons_positions();

        offset_speed_lines_neurons(number_of_inputs, max_speed);
        
    }
    protected void offset_speed_lines_neurons(int number_of_inputs, int max_speed) {
        for (int i = 0; i < max_speed; i++) {
            Neurons_ArrayList line = speed_lines.get(i);
            for (int j = 0; j < line.size(); j++) {
                Visual_Neuron visual_neuron = line.get(j).visual_neuron;
                int y = visual_neuron.center.y.get();
                visual_neuron.center.y.set(y - 100*i);
            }
        }
        for (int i = 0; i < number_of_inputs; i++) {
            Visual_Neuron visual_neuron = in_neurons.get(i).visual_neuron;
            int y = visual_neuron.center.y.get();
            visual_neuron.center.y.set(y + 100*(max_speed-1));
        }
        for (int i = 0; i < max_speed; i++) {
            Visual_Neuron visual_neuron = out_neurons.get(i).visual_neuron;
            int y = visual_neuron.center.y.get();
            visual_neuron.center.y.set(y + 100*(max_speed-1));
        }
        for (int i = 0; i < max_speed; i++) {
            Neurons_ArrayList line = speed_lines.get(i);
            for (int j = 0; j < line.size(); j++) {
                Visual_Neuron visual_neuron = line.get(j).visual_neuron;
                int y = visual_neuron.center.y.get();
                visual_neuron.center.y.set(y + 100*(max_speed-1));

            }
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
