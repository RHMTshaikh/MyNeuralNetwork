package TheBrain.utils;

import java.util.ArrayList;

import TheBrain.Neuron;



public class Neurons_ArrayList extends ArrayList<Neuron> {

    public Neurons_ArrayList() {
    }    
    Neurons_ArrayList(Neurons_ArrayList neuronsArrayList) {
        super(neuronsArrayList);
    }    
    Neurons_ArrayList(int length) {
        super(length);
    }    
    
    @Override
    public void add(int index, Neuron neuron) {
        while (super.size() <= index) {
            super.add(null);            
        }
        // neuron.setParentArray(this);
        super.add(index, neuron);
    }
    
    @Override
    public boolean add(Neuron neuron) {
        // neuron.setParentArray(this);
        return super.add(neuron);
    }
    
    @Override
    public boolean remove(Object neuron) {
        ((Neuron)neuron).set_parent_array(null);
        return super.remove(neuron);
    }
    
    
}