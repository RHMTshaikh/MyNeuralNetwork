package Brains;

import Brain_Regions.Edge_Detector_Centre;
import TheBrain.Brain;

public class Edge_Detector_Brain extends Brain {
    int number_of_lines = 200;
    int number_of_neurons_in_line = 400;
    double sensitivity = 3;

    Edge_Detector_Centre edge_detector_centre = new Edge_Detector_Centre(number_of_lines, number_of_neurons_in_line, sensitivity);

    public Edge_Detector_Brain() {
        brain_regions.add(edge_detector_centre);
        set_positions_and_add_to_group(0);
    }
    
    @Override
    public void step() {

        pass_excitation();

        String path = "texts\\images\\Warm-Final-Residence-designed-by-Takashi-Okuno.jpg";

        edge_detector_centre.pixel_intensity_reader(path, excited_neurons, clock);
        number_of__excited_neurons = excited_neurons.size();

        clock++;

    }

    
}
