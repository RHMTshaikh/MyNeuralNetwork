package TheBrain;

import java.util.ArrayList;

import Brains.Hyper_Parameter;
import DataFrame.Row_With_Index;
import TheBrain.utils.DFNeurons;
import TheBrain.utils.Neurons_ArrayList;
import javafx.scene.Group;

public abstract class Brain_Region {

    public Neurons_ArrayList in_neurons = new Neurons_ArrayList();
    public Neurons_ArrayList hidden_neurons = new Neurons_ArrayList();
    public Neurons_ArrayList out_neurons = new Neurons_ArrayList();
  
    protected int fire_together_threshold = Hyper_Parameter.FIRE_TOGETHER_THRESHOLD.get_int();

    ArrayList<Brain_Region> input_brain_regions = new ArrayList<>();
    ArrayList<Brain_Region> output_brain_regions = new ArrayList<>();

    protected Brain_Region(){}
    
    Brain_Region( Neurons_ArrayList neurons) {
        this.hidden_neurons = neurons;
    }
    
    protected Brain_Region(int number_of_neurons) {
        for (int i = 0; i < number_of_neurons; i++) {
            hidden_neurons.add(new Neuron(hidden_neurons));
        }
    }
    
    protected Brain_Region(Neurons_ArrayList input_neurons, int number_of_outputs, int number_of_neurons) {
        this.in_neurons = input_neurons;
        for (int i = 0; i < number_of_neurons; i++) {
            hidden_neurons.add(new Neuron(hidden_neurons));
        }
        for (int i = 0; i < number_of_outputs; i++) {
            out_neurons.add(new Neuron(out_neurons));
        }
    }
    protected Brain_Region(int number_of_inputs, int number_of_outputs, int number_of_neurons) {
        for (int i = 0; i < number_of_inputs; i++) {
            in_neurons.add(new Neuron(in_neurons));
        }
        for (int i = 0; i < number_of_neurons; i++) {
            hidden_neurons.add(new Neuron(hidden_neurons));
        }
        for (int i = 0; i < number_of_outputs; i++) {
            out_neurons.add(new Output_Neuron(out_neurons));
        }
    }
    
    private Brain_Region(String path) {
        DFNeurons neuronsDF = new DFNeurons(path + "/neurons.csv" );
        DFNeurons inputDF = new DFNeurons(path + "/input.csv" );
        DFNeurons outputDF = new DFNeurons(path + "/output.csv" );
        
        dataFrameToNeurons(neuronsDF, hidden_neurons);
        dataFrameToNeurons(inputDF, in_neurons);
        dataFrameToNeurons(outputDF, out_neurons);
        
        // mergedSortedNeurons = merge(inNeurons, hiddenNeurons, outNeurons);
    }

    void make_connections_between(Neurons_ArrayList neurons1, Neurons_ArrayList neurons2, int number_of_connections) {
        for (int i = 0; i < number_of_connections; i++) {
            int tries = (neurons1.size()+neurons2.size())/2;
            
            Neuron n1 = neurons1.get(get_random_int(0, neurons1.size()-1));
            Neuron n2 = neurons2.get(get_random_int(0, neurons2.size()-1));

            while (n1.is_linked_to(n2)) {
                if (--tries == 0) break;
                n1 = neurons1.get(get_random_int(0, neurons1.size()-1));
                n2 = neurons2.get(get_random_int(0, neurons2.size()-1));
            }
            n1.link_to(n2);
        }
    }
    
    private void dataFrameToNeurons(DFNeurons neuronsDF, Neurons_ArrayList neurons) {
        addNeurons(neuronsDF, neurons);
        add_links(neuronsDF, neurons);
    }
    private void addNeurons(DFNeurons neuronsDF, Neurons_ArrayList neurons) {
        for (Row_With_Index index_row : neuronsDF) {
            int threshold_excitation_level = (int)index_row.row.column("thresholdExcitationLevel");
            double depletion_rate = (double)index_row.row.column("excitationDepletionRate");
            neurons.add(new Neuron(threshold_excitation_level, depletion_rate, neurons));
        }
    }
    private void add_links(DFNeurons neuronsDF, Neurons_ArrayList neurons) {
        for (Row_With_Index index_row : neuronsDF) {
            Neuron neuron = neurons.get(index_row.index);

            String links = (String)index_row.row.column("links");

            links = links.substring(1, links.length() - 1);
            String[] parts_incoming = links.split(" ");

            for (String part : parts_incoming) {
                // String[] subparts = part.split(":");

                // int from = Integer.parseInt(subparts[0]);
                // double weight = Double.parseDouble(subparts[1]);
                // int to = Integer.parseInt(subparts[2]);

                // neuron.links.add(new Link(neurons.get(from), neurons.get(to), weight));
            }
        }
    }

    void update_visual (Group group, int clock) {
        for (Neuron neuron : in_neurons) {
            neuron.update_visual(group, clock);
        }
        for (Neuron neuron : hidden_neurons) {
            neuron.update_visual(group, clock);
        }
    }

    protected void set_input_neurons_positions(){
        int gap_verticle = 50;
        for (int i = 0; i < in_neurons.size(); i++) {
            in_neurons.get(i).visual_neuron.set_center(0, i*gap_verticle);
        }
    }
    protected void set_hidden_neurons_positions(int verticle_neuron_count, int horizontal_neuron_count) {
        int gap_horizontal = 50;
        int gap_verticle = 100;
        for (int h = 0; h < horizontal_neuron_count; h++) {
            for (int v = 0; v < verticle_neuron_count; v++) {
                hidden_neurons.get(h*verticle_neuron_count + v).visual_neuron.set_center(h*gap_horizontal, v*gap_verticle);
            }
        }
    }
    protected void set_output_neurons_positions(){
        int gap_verticle = 50;
        for (int i = 0; i < out_neurons.size(); i++) {
            out_neurons.get(i).visual_neuron.set_center(0, i*gap_verticle);
        }
    }
    
    public abstract void fire_together_wire_together(int clock);
    protected abstract double distance_between(Neuron n1, Neuron n2);

    protected boolean fire_together(Neuron n1, Neuron n2) {
        return n1.last_excitation_time <= n2.last_excitation_time && n2.last_excitation_time <= n1.last_excitation_time+fire_together_threshold && n2.last_excitation_time != 0 && n1.last_excitation_time != 0;
    }

    @SuppressWarnings("unused")
    private Neurons_ArrayList merge(Neurons_ArrayList inNeurons, Neurons_ArrayList hiddenNeurons, Neurons_ArrayList outNeurons) {
        Neurons_ArrayList mergedSortedNeurons = new Neurons_ArrayList();
        Neuron minNeuron = new Neuron();
        minNeuron.last_excitation_time = Integer.MIN_VALUE;
        int i = 0, j = 0, k = 0;

        while (i < inNeurons.size() || j < hiddenNeurons.size() || k < outNeurons.size()) {

            Neuron inNeuron     = i < inNeurons.size()     ? inNeurons.get(i)     : minNeuron;
            Neuron hiddenNeuron = j < hiddenNeurons.size() ? hiddenNeurons.get(j) : minNeuron;
            Neuron outNeuron    = k < outNeurons.size()    ? outNeurons.get(k)    : minNeuron;

            if (inNeuron.compareTo(hiddenNeuron) <= 0 && inNeuron.compareTo(outNeuron) <= 0) {
                mergedSortedNeurons.add(inNeuron);
                i++;
            } else if (hiddenNeuron.compareTo(inNeuron) <= 0 && hiddenNeuron.compareTo(outNeuron) <= 0) {
                mergedSortedNeurons.add(hiddenNeuron);
                j++;
            } else {
                mergedSortedNeurons.add(outNeuron);
                k++;
            }
        }
        return mergedSortedNeurons;
    }

    public abstract Link make_input_connection();
    public abstract Link make_input_connection(double weight);
    
    public abstract Link make_internal_connection();
    public abstract Link make_internal_connection(double weight);

    public abstract Link make_output_connection();
    public abstract Link make_output_connection(double weight);

    protected static int get_random_int(int lower_bound, int upper_bound) {
        if (lower_bound > upper_bound) {
            throw new IllegalArgumentException("Lower bound must be less than or equal to upper bound");
        }
        return (int) (Math.random() * (upper_bound - lower_bound)) + lower_bound;
    }
    
    protected static double skewed_probability_function() {
        // at forward bisa
        double A1 = 1;
        double c1 = 0.03;
        double k1 = 23;
        double A2 = 1.3;
        double c2 = 0;
        double k2 = 9.5;
        double A3 = 1.51;
        double c3 = 1;
        double k3 = 15;
        double C  = -2;
        double x = Math.random();
        
        double term1 = A1 / (1 + Math.exp(-k1 * (x - c1)));
        double term2 = A2 / (1 + Math.exp(-k2 * (x - c2)));
        double term3 = A3 / (1 + Math.exp(-k3 * (x - c3)));
        return term1 + term2 + term3 + C;
    }
}

    

