package Brain_Regions;

import TheBrain.Brain_Region;
import TheBrain.Link;
import TheBrain.Neuron;
import TheBrain.utils.Neurons_ArrayList;

public class Logic_Centre extends Brain_Region {

    public Logic_Centre(Neurons_ArrayList inputNeurons, int numberOfOutputs, int numberOfNeurons) {
        super(inputNeurons, numberOfOutputs, numberOfNeurons);

        int c =0;
        for (int i = 0; i < numberOfNeurons/2; i++) {
            if (make_input_connection() != null) c++;
        }
        for (int i = 0; i < numberOfNeurons/5; i++) {
            if (make_internal_connection() != null) c++;
        }
        for (int i = 0; i < numberOfNeurons/2; i++) {
            if (make_output_connection() != null) c++;
        }
        System.out.println("Connections made: " + c);

    }

    @Override
    public void fire_together_wire_together(int clock) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fire_together_wire_together'");
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
    public Link make_output_connection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_output_connection'");
    }

    @Override
    public Link make_internal_connection(double weight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_internal_connection'");
    }

    @Override
    public Link make_output_connection(double weight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_output_connection'");
    }

    @Override
    protected double distance_between(Neuron n1, Neuron n2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'distance_between'");
    }
}


