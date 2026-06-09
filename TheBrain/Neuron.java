package TheBrain;

// import java.awt.Color;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import Brains.Hyper_Parameter;
import TheBrain.utils.Linked_HashSet_Queue;
import TheBrain.utils.Neurons_ArrayList;
import Visualisation.Visual_Neuron;
import javafx.scene.Group;

public class Neuron implements Comparable<Neuron> {
    
    public Map<Neuron, Link> links_in = new HashMap<>(); // neuron, wieght
    public Map<Neuron, Link> links_out = new HashMap<>();
    // int links_in_limit = 20;
    public int links_out_limit = 10;
    
    public double threshold_excitation_level  = Hyper_Parameter.THRESHOLD_EXCITATION_LEVEL.get_double();
    public double excitation_depletion_rate   = Hyper_Parameter.EXCITATION_DEPLETION_RATE.get_double();
    public double threshold_depletion_rate    = Hyper_Parameter.THRESHOLD_DEPLETION_RATE.get_double();
    private Neurons_ArrayList parent_array;
    public double excitation_level = Hyper_Parameter.EXCITATION_LEVEL.get_double();                     // will never be negative 
    public int last_excitation_time = 0;


    public Visual_Neuron visual_neuron;

    public Neuron() {
        visual_neuron = new Visual_Neuron(this);
    }

    public Neuron(Neurons_ArrayList parentArray) {
        this.parent_array = parentArray;
        visual_neuron = new Visual_Neuron(this);
    }
    
    public Neuron(int threshold_excitation_level, double excitation_depletion_rate, Neurons_ArrayList parentArray) {
        this.threshold_excitation_level = threshold_excitation_level;
        this.excitation_depletion_rate = excitation_depletion_rate;
        this.parent_array = parentArray;
        visual_neuron = new Visual_Neuron(this);
    }

    @Override
    public int compareTo(Neuron o) {
        return Long.compare(o.last_excitation_time, this.last_excitation_time); // descending order
    }

    public int get_index() {
        return parent_array.indexOf(this);
    }
    public void set_parent_array(Neurons_ArrayList parent_array) {
        this.parent_array = parent_array;
    }
    public boolean is_excited(int clock) {
        // deplet_excitation(clock);
        return excitation_level >= threshold_excitation_level;
    }

    public void make_neuron_constant() {
        excitation_depletion_rate = 0;
        threshold_depletion_rate = 0;
    }

    public void excite(double excitation, int clock) {
        deplet_excitation(clock);
        last_excitation_time  = clock;
        excitation_level += excitation;
        excitation_level = Math.min(excitation_level, threshold_excitation_level);
    }
    
    public void pass_excitation(Linked_HashSet_Queue<Neuron> excited_neurons, int clock){
        if (excitation_level >= threshold_excitation_level) {

            Iterator<Map.Entry<Neuron, Link>> itr = links_out.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry<Neuron, Link> entry = itr.next();
                Link link = entry.getValue();
                if (link.get_weight(clock) == 0) {
                    // itr.remove();
                    link.to.links_in.remove(this);
                    continue;                    
                }
                link.excite(clock);
                excited_neurons.offer(link.to);
            }
            decrease_threshold_on_excitation();
            excitation_level = 0;
        }
    }

    public void update_visual(Group group, int clock) {
        deplet_excitation(clock);
        update_visual_neuron();
        
        Iterator<Map.Entry<Neuron, Link>> itr = links_out.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Neuron, Link> entry = itr.next();
            Link link = entry.getValue();
            if (!group.getChildren().contains(link.visual_link.curve)) {
                group.getChildren().add(link.visual_link.curve);
            }
            if (link.get_weight(clock) == 0) {
                itr.remove();
                // link.to.links_in.remove(this);
                group.getChildren().remove(link.visual_link.curve);
            } else {
                link.update_visual(clock);
                if (link.get_weight(clock) < 0) {
                    link.visual_link.curve.setStroke(Color.BLACK);
                }
            }
        }
    }
    private void update_visual_neuron() {
        visual_neuron.excitationLevel.set(excitation_level);
        visual_neuron.thresholdExcitationLevel.set(threshold_excitation_level);
    }

    public void strengthen_synapses_by(double value) {
        for (Map.Entry<Neuron, Link> entry : links_in.entrySet()) {
            Link link = entry.getValue();
            link.strengthen_by(value);
        }
    }

    public void weaken_synapses_by(double value) {
        for (Map.Entry<Neuron, Link> entry : links_in.entrySet()) {
            Link link = entry.getValue();
            link.weaken_by(value);
        }
    }
    
    public void increse_weights_by(double value, Linked_HashSet_Queue<Neuron> neuron_set_queue, Set<Neuron> visited_neurons) {
        for (Map.Entry<Neuron, Link> entry : links_in.entrySet()) {
            Link link = entry.getValue();
            link.incerease_weight_by(value);
            if (visited_neurons.contains(link.from)) continue;
            neuron_set_queue.offer(link.from);
        }
    }
    public void decrease_weights_by(double value, Linked_HashSet_Queue<Neuron> neuron_set_queue, Set<Neuron> visited_neurons) {
        for (Map.Entry<Neuron, Link> entry : links_in.entrySet()) {
            Link link = entry.getValue();
            link.decrease_weight_by(value);
            if (visited_neurons.contains(link.from)) continue;
            neuron_set_queue.offer(link.from);
        }
    }
    // public void increse_repletions(Linked_HashSet_Queue<Neuron> first, Linked_HashSet_Queue<Neuron> second, Set<Neuron> visited_neurons) {
    //     for (Map.Entry<Neuron, Link> entry : links_in.entrySet()) {
    //         Link link = entry.getValue();
    //         link.incerease_repletion();
    //         if (visited_neurons.contains(link.from)) continue;
    //         second.offer(link.from);
    //     }

    // }
    // public void decrease_repletions(Linked_HashSet_Queue<Neuron> neuron_set_queue, Set<Neuron> visited_neurons) {
    //     for (Map.Entry<Neuron, Link> entry : links_in.entrySet()) {
    //         Link link = entry.getValue();
    //         link.decrease_repletion();
    //         if (visited_neurons.contains(link.from)) continue;
    //         neuron_set_queue.offer(link.from);
    //     }
    // }
    
    protected void deplet_excitation(int clock) {
        excitation_level -= excitation_depletion_rate * (clock - last_excitation_time);
        excitation_level = Math.max(excitation_level, 0);
    }
    private void decrease_threshold_on_excitation() {
        threshold_excitation_level -= threshold_depletion_rate;
        if (threshold_excitation_level < 0) {
            threshold_excitation_level = 0;            
        }
    }
    
    public Link get_link_to(Neuron to) {
        if (links_out.containsKey(to)) {
            return links_out.get(to);
        }
        return null;
    }

    public Link link_to(Neuron to) {
        // if (links_out.size() == links_out_limit) return null;
        
        Link link = new Link(this, to);
        links_out.put(to, link);
        to.links_in.put(this, link);
        return link;
    }
    
    public Link link_to(Neuron to, double weight) {
        // if (links_out.size() == links_out_limit) return null;
        
        Link link = new Link(this, to, weight);
        links_out.put(to, link);
        to.links_in.put(this, link);
        return link;
    }

    public boolean is_linked_to(Neuron neuron) {
        return links_out.containsKey(neuron);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Neuron ")
          .append(get_index())
          .append(" ")
          .append(threshold_excitation_level)
          .append(" ")
          .append(excitation_level)
          .append(" ")
        //   .append(excitation_depletion_rate)
          .append(" ")
          .append(links_in.toString())
          .append(" ")
          .append(links_out.toString());
        return sb.toString();
    }
}