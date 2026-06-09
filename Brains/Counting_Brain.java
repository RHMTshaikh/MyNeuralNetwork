package Brains;

import java.util.List;

import Brain_Regions.Counting_Centre;
import TheBrain.Brain;
import TheBrain.Neuron;

public class Counting_Brain extends Brain {
    public static void main(String[] args) {
        double num = 0.0;
        for (int i = 0; i <= 10; i++) {
            num = num + 0.1;
            System.out.println(i + " " + num);
        }
    }
    Counting_Centre counting_centre = new Counting_Centre(10, 10, 10);

    public Counting_Brain() {
        brain_regions.add(counting_centre);
        set_positions_and_add_to_group(200);
    }

    @Override
    public void step() {
        pass_excitation();

        List<String> content = read_file_content("texts\\input.txt");
        char ch = '\0';
        if (content.size() != 0) {
            char[] charArray = content.get(0).toCharArray();
            for (int i = 0; i < charArray.length; i++) {

                ch = charArray[i];
                if (ch == '1') {
                    Neuron neuron = counting_centre.in_neurons.get(i);
                    neuron.excite(1, clock);
                    excited_neurons.offer(neuron);
                }
            }
            number_of__excited_neurons = excited_neurons.size();
        }

        clock++;
    }
}
