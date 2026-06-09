package TheBrain.utils;

import TheBrain.Neuron;

public class ExcitedNeuron {
    Neuron neuron;
    double excitation;

    ExcitedNeuron(Neuron neuron, double excitation) {
        this.neuron = neuron;
        this.excitation = excitation;
    }
}
