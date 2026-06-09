package Brain_Regions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import Brains.Hyper_Parameter;
import TheBrain.Feed_Forward_Network;
import TheBrain.Link;
import TheBrain.Neuron;
import TheBrain.utils.Linked_HashSet_Queue;
import TheBrain.utils.Neurons_ArrayList;
import TheBrain.utils.PairDataStructure;

public class Output_Word_Processing_Centre extends Feed_Forward_Network {

    public static void main(String[] args) {
        System.out.println((int)'b');
    }
    
    HashMap<Character, Integer> char_encoding = new HashMap<>();
    {
        char_encoding.put('\0', 0);
        char_encoding.put('a', 26342);
        char_encoding.put('b', 11255);
        char_encoding.put('c', 50148);
        char_encoding.put('d', 47024);
        char_encoding.put('e', 54463);
        char_encoding.put('f', 9833);
        char_encoding.put('g', 15480);
        char_encoding.put('h', 6710);
        char_encoding.put('i', 28056);
        char_encoding.put('j', 9858);
        char_encoding.put('k', 43418);
        char_encoding.put('l', 3624);
        char_encoding.put('m', 58077);
        char_encoding.put('n', 8955);
        char_encoding.put('o', 21424);
        char_encoding.put('p', 20805);
        char_encoding.put('q', 48028);
        char_encoding.put('r', 43167);
        char_encoding.put('s', 55319);
        char_encoding.put('t', 46834);
        char_encoding.put('u', 53075);
        char_encoding.put('v', 3731);
        char_encoding.put('w', 6827);
        char_encoding.put('x', 37164);
        char_encoding.put('y', 3249);
        char_encoding.put('z', 51043);
    }
    PairDataStructure word_map = new PairDataStructure();
    // int number_of_active_neurons = 0;
    int numberOfActiveNeuronsThreshold = 5;
    public int current_pattern_number = 0;

    public char ans_word = '\0';
    public char produced_word = '\0';

    public boolean[] outputs;
    public boolean[] correct_outputs;

    double activity_threshold = Hyper_Parameter.THRESHOLD_EXCITATION_LEVEL.get_double()*0.5;

    public Output_Word_Processing_Centre(int number_of_inputs, int number_of_outputs, int verticle_neurons_count, int horizontal_neurons_count) {
        super(number_of_inputs, number_of_outputs, verticle_neurons_count, horizontal_neurons_count);

        outputs = new boolean[number_of_outputs];
        correct_outputs = new boolean[number_of_outputs];
        
        int c = 0;
        
        for (int i = 0; i < number_of_inputs*3; i++) {
            if (make_input_connection() != null) c++;
        }
        for (int i = 0; i < hidden_neurons.size()*2; i++) {
            if (make_internal_connection() != null) c++;
        }
        for (int i = 0; i < number_of_outputs*3; i++) {
            if (make_output_connection() != null) c++;
        }
        System.out.println("Connections made: " + c);
        
        // set_output_nerons_excitation_depletion_rate(Hyper_Parameter.EXCITATION_DEPLETION_RATE.get_double()*10);
        make_threshod_level_of_output_neurons_high();
        // make_threshod_level_of_hidden_neurons_high();
        make_threshold_depletion_rate_of_output_neurons_zero();
    }
    
    
    public Output_Word_Processing_Centre(Neurons_ArrayList input_neurons, int number_of_outputs, int number_of_verticle_neurons, int number_of_horizontal_neurons) {  
        super(input_neurons, number_of_outputs, number_of_verticle_neurons, number_of_horizontal_neurons);

        outputs = new boolean[number_of_outputs];
        correct_outputs = new boolean[number_of_outputs];

        int number_of_neurons = number_of_verticle_neurons*number_of_verticle_neurons;
        int c =0;
        for (int i = 0; i < number_of_neurons/2; i++) {
            if (make_input_connection() != null) c++;
        }
        for (int i = 0; i < number_of_neurons; i++) {
            if (make_internal_connection() != null) c++;
        }
        for (int i = 0; i < number_of_neurons/2; i++) {
            if (make_output_connection() != null) c++;
        }
        System.out.println("Connections made: " + c);

        // make_threshod_level_of_output_neurons_high();
        // make_threshod_level_of_hidden_neurons_high();
        make_threshold_depletion_rate_of_output_neurons_zero();
        make_depletion_rate_zero_for_input_links();
    }

    public void readCharacter(char c, Linked_HashSet_Queue<Neuron> excitedNeurons, int clock) {
        int codePoint = char_encoding.get(c);
        for (int i = 0; i < in_neurons.size(); i++) {
            if ((codePoint & 1) != 0) {
                in_neurons.get(i).excite(Hyper_Parameter.THRESHOLD_EXCITATION_LEVEL.get_double()*1.5, clock);
                excitedNeurons.offer(in_neurons.get(i));
            }
            codePoint = codePoint >> 1;
        }
    }
        

    public boolean is_word_ready(int clock) {
        int number_of_active_neurons = 0;
        for (int i = 0; i < out_neurons.size(); i++) {
            if (out_neurons.get(i).is_excited(clock)) number_of_active_neurons++;
        }
        return number_of_active_neurons >= numberOfActiveNeuronsThreshold;
    }
    
    public boolean match_word() {
        return ans_word == produced_word;
    }

    public void make_output_pattern_array_and_number(int clock) {
        int mast = 1;
        current_pattern_number = 0;
        for (int i = 0; i < out_neurons.size(); i++) {
            if (out_neurons.get(i).is_excited(clock)) {
                outputs[i] = true;
                current_pattern_number |= mast;
            } else{
                outputs[i] = false;
            }
            mast = mast << 1;
        }
    }
    public boolean[] make_correct_pattern_array(int current_pattern_number, int clock) throws Exception {
        ans_word = read_answer_file();
        produced_word = produce_word(ans_word, current_pattern_number, clock);
        System.out.println("Answer:   " + ans_word);
        System.out.println("Produced: " + produced_word);
        if (ans_word == '\0') correct_outputs = null;
        return correct_outputs;
    }

    public char produce_word(char ans_word, int current_pattern_number, int clock) {
        produced_word = '\0';
        correct_outputs = null;

        Integer word = word_map.get_char(current_pattern_number);
        Integer pattern = word_map.get_pattern(ans_word);
        
        if (word != null) {
            produced_word = (char) word.intValue();
        }

        if (pattern != null) {
            int correct_pattern = pattern.intValue();
            correct_outputs = new boolean[out_neurons.size()];
            for (int i = 0; i < out_neurons.size(); i++) {
                if ((correct_pattern & 1) != 0) {
                    correct_outputs[i] = true;
                }else{
                    correct_outputs[i] = false;
                }
                correct_pattern = correct_pattern >> 1;
            }
        }
        
        if ((word == null || (char)word.intValue() != ans_word) && pattern == null && is_word_ready(clock)) {
            remember_new_word(ans_word, current_pattern_number);
            produced_word = ans_word;
            
            int correct_pattern = current_pattern_number;
            correct_outputs = new boolean[out_neurons.size()];
            for (int i = 0; i < out_neurons.size(); i++) {
                if ((correct_pattern & 1) != 0) {
                    correct_outputs[i] = true;
                }else{
                    correct_outputs[i] = false;
                }
                correct_pattern = correct_pattern >> 1;
            }
        }

        return produced_word;
    }
    
    char read_answer_file() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("texts\\answer.txt"));
        String line = reader.readLine();
        if (line == null) {
            reader.close();
            return '\0';
        }
        reader.close();
        return line.trim().charAt(0);
    }
    void remember_new_word(char word, int pattern) {
        System.out.println("Remembering new word: " + word);
        word_map.insert(pattern, word);
    }
    
    private void make_threshold_depletion_rate_of_output_neurons_zero() {
        for (Neuron neuron : out_neurons) {
            neuron.threshold_depletion_rate = 0;
        }
    }
    private void make_threshod_level_of_output_neurons_high() {
        for (Neuron neuron : out_neurons) {
            neuron.threshold_excitation_level = Hyper_Parameter.THRESHOLD_EXCITATION_LEVEL.get_double()*2.5;
        }
    }
    // private void set_output_nerons_excitation_depletion_rate(double rate) {
    //     for (Neuron neuron : out_neurons) {
    //         neuron.excitation_depletion_rate = rate;
    //     }
    // }
    private void make_threshod_level_of_hidden_neurons_high() {
        for (Neuron neuron : hidden_neurons) {
            neuron.threshold_excitation_level = 1;
        }
    }
    private void make_depletion_rate_zero_for_input_links(){
        for( Neuron n : in_neurons) {
            for ( Link l : n.links_out.values()) {
                l.depletion_rate = 0;                
            }
        }
    }

    void saveWordMapToFile(String fileName) {
        // save word map to fiel
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();            
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(word_map.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();  // Handle exception during closing
                }
            }
        }
    }

    // void createHashMapFromFile(String filePath) {

    //     try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             // Remove curly braces and split the string
    //             String content = line.trim().substring(1, line.length() - 1); // Remove '{' and '}'
    //             String[] pairs = content.split(", ");

    //             for (String pair : pairs) {
    //                 String[] keyValue = pair.split("=");
    //                 if (keyValue.length != 2) {
    //                     throw new IllegalArgumentException("Invalid key-value pair: " + pair);
    //                 }

    //                 int key = Integer.parseInt(keyValue[0].trim());  // Convert key to integer
    //                 String value = keyValue[1].trim();  // Get the value as a string
    //                 wordMap.put(key, value);
    //             }
    //         }

    //     } catch (IOException e) {
    //         System.out.println("Error reading the file.");
    //         e.printStackTrace();
    //     } catch (NumberFormatException e) {
    //         System.out.println("Error: Invalid number format in the file.");
    //         e.printStackTrace();
    //     } catch (IllegalArgumentException e) {
    //         System.out.println("Error: " + e.getMessage());
    //         e.printStackTrace();
    //     } catch (Exception e) {
    //         System.out.println("An unexpected error occurred.");
    //         e.printStackTrace();
    //     }
    // }
}


class BinaryPrinter {
    public static void printBinary(int number) {
        System.out.println(Integer.toBinaryString(number));
    }

    public static void main(String[] args) {
        int number = 11255;
        System.out.print("Binary of " + number + " is: ");
        printBinary(number);
    }
}