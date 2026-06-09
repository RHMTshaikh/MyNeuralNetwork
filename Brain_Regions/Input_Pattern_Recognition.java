package Brain_Regions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import TheBrain.Feed_Forward_Network;
import TheBrain.Neuron;
import TheBrain.utils.Linked_HashSet_Queue;

public class Input_Pattern_Recognition extends Feed_Forward_Network {

    
    String input_file_path = "texts\\input.txt";
    File input_file = new File(input_file_path);

    String[][] pattern_map = null;

    public Input_Pattern_Recognition(int number_of_inputs, int number_of_outputs, int verticle_neuron_count, int horizontal_neuron_count) {
        super(number_of_inputs, number_of_outputs, verticle_neuron_count, horizontal_neuron_count);

        int c = 0;
        for (int i = 0; i < verticle_neuron_count*4; i++) {
            if (make_input_connection() != null) c++;
        }
        for (int i = 0; i < verticle_neuron_count*horizontal_neuron_count; i++) {
            if (make_internal_connection() != null) c++;
        }
        for (int i = 0; i < verticle_neuron_count*4; i++) {
            if (make_output_connection() != null) c++;
        }
        System.out.println("Connections made: " + c);

        double p = 0.3;
        int number_of_patterns = 20;

        pattern_map = new String[number_of_patterns][2]; // 20 patterns, 2 strings (input and output)

        for (int i = 0; i < number_of_patterns; i++) {
            StringBuilder input = new StringBuilder();
            StringBuilder output = new StringBuilder();

            for (int j = 0; j < number_of_inputs; j++) {
                if (Math.random() < p) input.append('1');
                else input.append('0');

                if (Math.random() < p) output.append('1');
                else output.append('0');
            }

            pattern_map[i][0] = input.toString();
            pattern_map[i][1] = output.toString();
        }

        // for (int i = 0; i < pattern_map.length; i++) {
        //     System.out.println("Pattern " + i + ": " + pattern_map[i][0] + " -> " + pattern_map[i][1]);
        // }
    }

    public String excite_input_layer(Linked_HashSet_Queue<Neuron> excited_neurons, int clock) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input_file));
        String line; // 0101100111:0110110001
        line = reader.readLine();
        reader.close();
        if (line == null) throw new IOException("Input file is empty");
        // if (line.length() != 1) throw new IOException("Input file requires a single character");
        int index = Integer.parseInt(line);


        String[] patterns = pattern_map[index];
        String input_pattern = patterns[0];
        String output_pattern = patterns[1];

        for (int i = 0; i < input_pattern.length(); i++) {
            Neuron neuron = in_neurons.get(i);
            if (input_pattern.charAt(i) == '1') {
                neuron.excite(1, clock);
                excited_neurons.offer(neuron);
            }
        }

        return output_pattern;
    }
}




class CharBinaryRepresentation {
    public static void main(String[] args) {
        char[] chars = {'A', 'b', '1', '@', ' '};
        
        for (char c : chars) {
            int codePoint = c;
            String binary16 = String.format("%16s", Integer.toBinaryString(codePoint)).replace(' ', '0');
            System.out.printf("Character: %c | Code Point: %d | 16-bit Binary: %s%n", c, codePoint, binary16);
        }
    }
}
