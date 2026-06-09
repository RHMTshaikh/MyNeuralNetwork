package TheBrain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import Brains.Hyper_Parameter;

import java.util.concurrent.Executors;

import TheBrain.utils.Linked_HashSet_Queue;
import TheBrain.utils.Neurons_ArrayList;
import javafx.scene.Group;


public abstract class Brain {

    public final ScheduledExecutorService scheduled_executor_service = Executors.newScheduledThreadPool(4);
    public static volatile boolean observing = false;
    public static volatile boolean predicting = false;
    public List<Brain_Region> brain_regions = new ArrayList<>();    


    public int number_of__excited_neurons = 0;  
    public int clock = 0;
    public Group group;

    public List<String> observerd_content = new ArrayList<>();

    public Linked_HashSet_Queue<Neuron> excited_neurons = new Linked_HashSet_Queue<Neuron>();

    public abstract void step();

    public void pass_excitation() {
        while (number_of__excited_neurons > 0) {
            Neuron excited_neuron = excited_neurons.poll();
            excited_neuron.pass_excitation(excited_neurons, clock);
            number_of__excited_neurons--;
        }
        number_of__excited_neurons = excited_neurons.size();
    }

    public void update_visual() {
        for (Brain_Region brainRegion : brain_regions) {
            brainRegion.update_visual(group, clock-1);
        }
        if (!brain_regions.isEmpty()) {
            Brain_Region lastBrainRegion = brain_regions.get(brain_regions.size()-1);
            for (Neuron neuron : lastBrainRegion.out_neurons) {
                neuron.update_visual(group, clock-1);
            }
        }
    }

    private void print_boolean_array(boolean[] array) {
        for (int i = 0; i < array.length; i++) {
            char bool = array[i] ? 'T' : '_';
            System.out.print(bool + " ");
        }
        System.out.println();
    }

    public void back_propogate(Neurons_ArrayList out_neurons, boolean[] wrong_pattern, boolean[] correction_pattern) {
        System.out.println("Back Propogating");
        print_boolean_array(correction_pattern);
        
        if (out_neurons.size() != wrong_pattern.length || out_neurons.size() != correction_pattern.length) {
            throw new IllegalArgumentException("The size of the wrong_pattern and correction_pattern arrays must match the size of the out_neurons array.");
        }
        
        double correction_amount = Hyper_Parameter.CORRECTION_RATE.get_double();
        
        
        for (int i = 0; i < out_neurons.size(); i++) {
            Linked_HashSet_Queue <Neuron> neuron_set_queue = new Linked_HashSet_Queue<Neuron>();
            Set<Neuron> visited_neurons = new HashSet<Neuron>();
            
            Neuron neuron = out_neurons.get(i);
            neuron_set_queue.offer(neuron);

            if (wrong_pattern[i]==false && correction_pattern[i]==true) {
                while (!neuron_set_queue.isEmpty()) {
                    Neuron n = neuron_set_queue.poll();
                    visited_neurons.add(n);
                    n.increse_weights_by(correction_amount, neuron_set_queue, visited_neurons);
                }
            }
            if (wrong_pattern[i]==true && correction_pattern[i]==false) {
                while (!neuron_set_queue.isEmpty()) {
                    Neuron n = neuron_set_queue.poll();
                    visited_neurons.add(n);
                    n.decrease_weights_by(correction_amount, neuron_set_queue, visited_neurons);
                }
            }
        }
    }
    
    public void back_prop_repletion(Neurons_ArrayList out_neurons, boolean[] wrong_pattern, boolean[] correction_pattern) {
        System.out.println("Back Propogating Replation");
        print_boolean_array(correction_pattern);
        print_boolean_array(wrong_pattern);
        
        if (out_neurons.size() != wrong_pattern.length || out_neurons.size() != correction_pattern.length) {
            throw new IllegalArgumentException("The size of the wrong_pattern and correction_pattern arrays must match the size of the out_neurons array.");
        }

        reset_back_prop_repletion_rate();
        
        int c = 0;
        for (int i = 0; i < out_neurons.size(); i++) {
            
            Neuron neuron = out_neurons.get(i);

            double amount = Hyper_Parameter.BACK_PROP_REPLETION_RATE.get_double();

            if(wrong_pattern[i]^correction_pattern[i]) {
                c++;
                if(wrong_pattern[i]) amount *= -1;
                alter_repletions(amount, neuron);
            }
        }
        System.out.println("c: " + c);
    }
    private void alter_repletions(double amount, Neuron neuron) {
        Linked_HashSet_Queue <Neuron> temp = new Linked_HashSet_Queue<Neuron>();
        Linked_HashSet_Queue <Neuron> first = new Linked_HashSet_Queue<Neuron>();
        Linked_HashSet_Queue <Neuron> second = new Linked_HashSet_Queue<Neuron>();
        Set<Neuron> visited_neurons = new HashSet<Neuron>();

        first.offer(neuron);
        while (!first.isEmpty()) {
            Neuron n = first.poll();
            visited_neurons.add(n);
            for (Map.Entry<Neuron, Link> entry : n.links_in.entrySet()) {
                Link link = entry.getValue();
                link.alter_repletion_by(amount);
                if (visited_neurons.contains(link.from)) continue;
                second.offer(link.from);
            }
            temp = first;
            first = second;
            second = temp;

            // amount /=1.1;
        }
    }
    private void reset_back_prop_repletion_rate() {
        for (Brain_Region brain_Region : brain_regions) {
            for (Neuron neuron : brain_Region.hidden_neurons) {
                for (Map.Entry<Neuron, Link> entry : neuron.links_in.entrySet()) {
                    Link link = entry.getValue();
                    link.back_prop_repletion_rate = 0;
                }
            }
            for (Neuron neuron : brain_Region.out_neurons) {
                for (Map.Entry<Neuron, Link> entry : neuron.links_in.entrySet()) {
                    Link link = entry.getValue();
                    link.back_prop_repletion_rate = 0;
                }
            }
        }
    }
    
    public void set_positions_and_add_to_group(int gap_between_layers) {
        group = new Group();
        int last_x = 20;
        Neurons_ArrayList first_input_layer = brain_regions.get(0).in_neurons;
        for (Neuron neuron : first_input_layer) {
            neuron.visual_neuron.center.x.set(last_x + neuron.visual_neuron.center.x.get());
            group.getChildren().add(neuron.visual_neuron.threshold_circle);
            group.getChildren().add(neuron.visual_neuron.excitation_circle);
        }
        last_x += gap_between_layers;
        
        for (Brain_Region brain_region : brain_regions) {
            int last = last_x;
            for(Neuron neuron : brain_region.hidden_neurons) {
                last = last_x + neuron.visual_neuron.center.x.get();
                neuron.visual_neuron.center.x.set(last);
                group.getChildren().add(neuron.visual_neuron.threshold_circle);
                group.getChildren().add(neuron.visual_neuron.excitation_circle);
            }
            last_x = last;
            last_x += gap_between_layers;

            for (Neuron neuron : brain_region.out_neurons) {
                neuron.visual_neuron.center.x.set(last_x + neuron.visual_neuron.center.x.get());
                group.getChildren().add(neuron.visual_neuron.threshold_circle);
                group.getChildren().add(neuron.visual_neuron.excitation_circle);
            }
        }
        
        for (Brain_Region brain_region : brain_regions) {
            for(Neuron neuron : brain_region.hidden_neurons) {
                for (Map.Entry<Neuron, Link> entry : neuron.links_in.entrySet()) {
                    Link link = entry.getValue();
                    group.getChildren().add(link.make_visual_link(clock).curve);
                }
            }
            for (Neuron neuron : brain_region.out_neurons) {
                for (Map.Entry<Neuron, Link> entry : neuron.links_in.entrySet()) {
                    Link link = entry.getValue();
                    group.getChildren().add(link.make_visual_link(clock).curve);
                }
            }
        }
    }

    public void add_excited_neurons(){
        for (Brain_Region brain_region : brain_regions) {
            for (Neuron neuron : brain_region.in_neurons) {
                if(neuron.is_excited(clock)){
                    excited_neurons.offer(neuron);
                }
            }
            for (Neuron neuron : brain_region.hidden_neurons) {
                if(neuron.is_excited(clock)){
                    excited_neurons.offer(neuron);
                }
            }
            for (Neuron neuron : brain_region.out_neurons) {
                if(neuron.is_excited(clock)){
                    excited_neurons.offer(neuron);
                }
            }
        }
    }

    public List<String> read_file_content(String filePath) {
        List<String> content = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        observerd_content = content;
        return content;
    }
}



class OptimizedCombinationCalculator {

    // Optimized method to calculate nCr
    public static long calculateNCR(int n, int r) {
        if (r > n) {
            throw new IllegalArgumentException("r cannot be greater than n");
        }
        if (r == 0 || r == n) return 1;  // Base cases
        r = Math.min(r, n - r);  // Use nCr = nC(n-r) to minimize r
        
        long result = 1;
        for (int i = 0; i < r; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }

    public static void main(String[] args) {

        int n = 16;
        // int r = 4;

        try {
            for (int r = 1; r <= n; r++) {
                long result = calculateNCR(n, r);
                System.out.println("nCr (" + n + "C" + r + ") = " + result);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

    }
}
