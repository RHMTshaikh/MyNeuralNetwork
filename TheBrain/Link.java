package TheBrain;

import Brains.Hyper_Parameter;
import Visualisation.Visual_Link;
import javafx.scene.paint.Color;

public class Link {
    public Neuron to;
    public Neuron from;
    private double weight  = Hyper_Parameter.WEIGHT.get_double();
    private double weight_threshold = Hyper_Parameter.WEIGHT_THRESHOLD.get_double();
    public double depletion_rate = Hyper_Parameter.DEPLETION_RATE.get_double();
    public double repletion_rate = Hyper_Parameter.REPLETION_RATE.get_double();
    // public double wrong_feedback_rate = Hyper_Parameter.WRONG_FEEDBACK_RATE.get_double();
    // public double correct_feedback_rate = Hyper_Parameter.CORRECT_FEEDBACK_RATE.get_double();
    public int last_visited_time = 0;
    public final int sign;
    public double back_prop_repletion_rate = 0;
    // public double back_prop_depletion_rate = Hyper_Parameter.BACK_PROP_DEPLETION_RATE.get_double();
    
    public Visual_Link  visual_link;

    Link(Neuron from, Neuron to) {
        this.from = from;
        this.to = to;
        sign = get_weighted_random_value();
        this.weight = weight*sign;
        this.weight_threshold = sign*weight_threshold;
        this.depletion_rate = depletion_rate*sign;
        this.repletion_rate = repletion_rate*sign;
        // this.wrong_feedback_rate = wrong_feedback_rate*sign;
        // this.correct_feedback_rate = correct_feedback_rate*sign;

        this.back_prop_repletion_rate = back_prop_repletion_rate*sign;
        // this.back_prop_depletion_rate = back_prop_depletion_rate*sign;
        
    }
    
    Link(Neuron from, Neuron to, double weight) {
        // if (weight < -1 || weight > 1) {
        //     throw new IllegalArgumentException("Weight must be between -1 and 1");
        // }
        this.from = from;
        this.to = to;
        this.weight = weight;
        sign = weight > 0 ? 1 : -1;
        this.weight_threshold = sign*weight_threshold;
        this.depletion_rate = depletion_rate*sign;
        this.repletion_rate = repletion_rate*sign;
        // this.wrong_feedback_rate = wrong_feedback_rate*sign;
        // this.correct_feedback_rate = correct_feedback_rate*sign;

        this.back_prop_repletion_rate = back_prop_repletion_rate*sign;
        // this.back_prop_depletion_rate = back_prop_depletion_rate*sign;
    }
    
    public double get_weight(int clock) {
        depletion(clock);
        return weight;
    }
    public void set_weight(double weight) {
        if (Math.abs(weight) > Math.abs(weight_threshold)) {
            throw new IllegalArgumentException("Weight must be less than the threshold weight: " + weight_threshold);
        }
        if (sign*weight < 0) {
            throw new IllegalArgumentException("Sign of weight must be the same as the original weight");
        }
        this.weight = weight;
    }

    public Visual_Link make_visual_link(int clock) {
        this.visual_link = new Visual_Link(this, clock);
        return visual_link;
    }
    
    // public void weaken() {
    //     weight -= wrong_feedback_rate;
    //     if (sign*weight < 0) weight = 0;
    // }
    // public void strengthen() {
    //     weight += correct_feedback_rate;
    //     // if (Math.abs(weight) > Math.abs(weight_threshold)) weight = weight_threshold;
    // }
    public void weaken_by(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");    
        amount = amount*sign;
        weight -= amount;
        if (sign*weight < 0) weight = 0;
    }
    public void strengthen_by(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");    
        amount = amount*sign;
        weight += amount;
        // if (Math.abs(weight) > Math.abs(weight_threshold)) weight = weight_threshold;            
    }
    
    public void incerease_weight_by(double amount) {
        if (amount < 0) throw new IllegalArgumentException(amount + " Amount must be positive");
        weight += amount;
    }
    public void decrease_weight_by(double amount) {
        if (amount < 0) throw new IllegalArgumentException(amount + " Amount must be positive");
        weight -= amount;
        if (weight < 0){
            visual_link.curve.setStroke(Color.BLACK);
        }
    }

    // public void incerease_repletion() {
    //     back_prop_repletion_rate += sign*Hyper_Parameter.BACK_PROP_REPLETION_RATE.get_double();
    // }
    // public void decrease_repletion() {
    //     back_prop_repletion_rate -= sign*Hyper_Parameter.BACK_PROP_REPLETION_RATE.get_double();
    // }
    public void alter_repletion_by(double amount) {
        back_prop_repletion_rate += amount;
    }

    public void depletion(int clock) {
        if (Math.abs(weight) >= Math.abs(weight_threshold)) {
            last_visited_time = clock;
            return;
        }
        double d = depletion_rate*(clock - last_visited_time);
        last_visited_time = clock;
        if (d==0) return;
        weight -= d;
        if (sign*weight < 0) weight = 0;
    }
    
    public void repletion(int clock) {
        weight += (repletion_rate + back_prop_repletion_rate);
        double abs = Math.abs(weight_threshold);
        weight = Math.min(weight, abs);
        weight = Math.max(weight, -abs);
        last_visited_time = clock;
    }
    
    public void make_link_constant() {
        depletion_rate = 0;
        repletion_rate = 0;
    }

    public void excite(int clock){
        depletion(clock);
        to.excite(weight, clock);
        repletion(clock);
        last_visited_time = clock;
    }
    
    public static int get_weighted_random_value() {
        return Math.random() < 0.9 ? 1 : -1;
    }
    
    public void update_visual(int clock) {
        if (visual_link == null) return;

        double w = get_weight(clock);
        visual_link.weight.set(Math.abs(w));
        if (w < 0) {
            visual_link.curve.setStroke(Color.BLACK);
        } else {
            if (visual_link.curve.getStroke().equals(Color.BLACK)) visual_link.curve.setStroke(Color.hsb(Math.random() * 360, 1.0, 1.0));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(from.get_index())
        .append(":")
        .append(weight)
        .append(":")
        .append(to.get_index())
        .append(" ");
        
        return sb.toString();
    }
    
}
