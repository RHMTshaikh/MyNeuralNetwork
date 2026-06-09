package TheBrain.utils;

import java.util.LinkedList;

import TheBrain.Neuron;

public class ExcitedNeuronsQueue extends LinkedList<Neuron> {
    @Override
    public boolean add(Neuron e) {
        Neuron n = find(e);
        if (n != null) {
            return true;
        }
        return super.add(e);
    }

    public Neuron find(Object o) {
        Neuron n = (Neuron) o;
        for (Neuron excitedNeuron : this) {
            if (excitedNeuron == n) {
                return excitedNeuron;
            }
        }
        return null;
    }
}
