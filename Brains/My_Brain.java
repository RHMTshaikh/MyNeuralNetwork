package Brains;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import Brain_Regions.Input_Pattern_Recognition;
import Brain_Regions.Logic_Centre;
import Brain_Regions.Output_Word_Processing_Centre;
import Brain_Regions.Word_Processing_Centre;
import TheBrain.Brain;
import TheBrain.Brain_Region;
import TheBrain.Neuron;

public class My_Brain  extends Brain {
    public static void main(String[] args) {
        My_Brain brain = new My_Brain();
        // brain.visualizer.launch(args);
        // brain.scheduledExecutorService.submit(() -> {
        // });
        brain.clock = 0;
        brain.running = true;
        brain.start();
        Scanner scanner = new Scanner(System.in);
        
        while (brain.running) {
            System.out.println("\nEnter command : ");
            String input = scanner.nextLine().trim();

            synchronized (brain) {
                if (input.equals("start")) {
                    brain.running = true;
                    brain.scheduled_executor_service.submit(() -> {
                        brain.start();
                    });
                } else if (input.equals("stop")) {
                    // brain.saveToFile("/mybrain");
                    brain.running = false;
                    brain.notifyAll();
                    brain.scheduled_executor_service.shutdown();
                } else {
                    System.out.println("Invalid command. Try again.");
                }
            }
        }
        scanner.close();
    }  

    private boolean running = true;

    Input_Pattern_Recognition input_letters_processing_centere;
    Word_Processing_Centre input_word_processing_centere;
    Logic_Centre logic_center;
    Output_Word_Processing_Centre output_word_processing_center;

    char[] charArray;
    
    public My_Brain() {
        // input_letters_processing_centere = new Letters_Processing_Centere(16,16,16);
        // input_word_processing_centere = new Word_Processing_Centere(input_letters_processing_centere.out_neurons, 16, 16);
        // logic_center = new Logic_Center(input_word_processing_centere.out_neurons, 16, 16);
        output_word_processing_center = new Output_Word_Processing_Centre(16, 30, 100, 6);
        // output_word_processing_center.set_architecture(Architectures.FEED_FORWARD, 20, 5);
        brain_regions.add(output_word_processing_center);
        // brain_regions.add(input_word_processing_centere);
        // brain_regions.add(logic_center);
        // brain_regions.add(output_word_processing_center);

        System.out.println("Brain created.");

        add_excited_neurons();

        set_positions_and_add_to_group(200);
    }

    public void start(){
        System.out.println("Brain started.");

        observerd_content = read_file_content("texts\\input.txt");
    
        String firstLine = observerd_content.get(0);
        char[] charArray = firstLine.toCharArray();

        while (running) {

            char ch = clock < charArray.length ? charArray[clock] : ' ';
            try {
                input_letters_processing_centere.excite_input_layer(excited_neurons, clock);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            number_of__excited_neurons = excited_neurons.size();


            System.out.println("propogating()...");
            // propogate();
            printExcitations();
            System.out.println("Clock: " + clock + " | Excited Neurons: " + number_of__excited_neurons);

            for (Brain_Region brainRegion : brain_regions) {
                // brainRegion.fire_together_wire_together();                
            }
            System.out.println("fireTogetherWireTogether() complete.");
            if (output_word_processing_center.is_word_ready(clock)) {
                System.out.println("wordReady() complete.");
            }
            System.out.println("Word processing complete.");
            clock++;
        }
    }

    
    public void step() {
        pass_excitation();

        List<String> content = read_file_content("texts\\input.txt");
        char ch = '\0';
        if (content.size() != 0) {
            charArray = content.get(0).toCharArray();
            ch = charArray.length==0 ? '\0' : charArray[clock%charArray.length];
        }
        // char randomChar = (char) ('A' + Math.random() * 26);
        // System.out.println("Character: " + randomChar);
        // inputLettersProcessingCentere.readCharacter(ch, excitedNeurons, clock);
        output_word_processing_center.readCharacter(ch, excited_neurons, clock);
        number_of__excited_neurons = excited_neurons.size();
    
        // System.out.println("Clock: " + clock + " | Excited Neurons: " + currentlyExcitedNeurons);
        
        for (Brain_Region brain_region : brain_regions) {
            // brain_region.fire_together_wire_together(clock);
            for (int i = 0; i < 1; i++) {
                // Optional.ofNullable(brain_region.make_input_connection(Hyper_Parameter.WEIGHT.get_double()/5)).ifPresent(connection -> connection.make_visual_link(clock));
                Optional.ofNullable(brain_region.make_internal_connection(Hyper_Parameter.WEIGHT.get_double()/5)).ifPresent(connection -> connection.make_visual_link(clock));
                // Optional.ofNullable(brain_region.make_output_connection(Hyper_Parameter.WEIGHT.get_double()/5)).ifPresent(connection -> connection.make_visual_link(clock));
            }
        }

        output_word_processing_center.make_output_pattern_array_and_number(clock);
        // output_word_processing_center.make_correct_pattern_array(output_word_processing_center.current_pattern_number);
    
        if (output_word_processing_center.is_word_ready(clock)) {
        }
        if (output_word_processing_center.correct_outputs != null) {
            if(output_word_processing_center.match_word()){
                java.awt.Toolkit.getDefaultToolkit().beep();
                System.out.println("word matched.");
            }
            back_prop_repletion(output_word_processing_center.out_neurons, output_word_processing_center.outputs, output_word_processing_center.correct_outputs);
        }
        clock++;
    }

    void printExcitations(){
        StringBuilder sb = new StringBuilder();
        for(Brain_Region brainRegion : brain_regions){
            sb.append(brainRegion.getClass().getSimpleName()).append('\n');
            for(Neuron neuron : brainRegion.in_neurons){
                sb.append(neuron.last_excitation_time).append(" ");
            }
            sb.append('\n');
            for(Neuron neuron : brainRegion.hidden_neurons){
                sb.append(neuron.last_excitation_time).append(" ");
            }
            sb.append('\n');
            for(Neuron neuron : brainRegion.out_neurons){
                sb.append(neuron.last_excitation_time).append(" ");
            }
            sb.append('\n');
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("excitations.txt"))) {
            writer.write(sb.toString());
            System.out.println("Excitations successfully saved to excitations.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void saveContentToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : observerd_content) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Content successfully saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



class OptimizedCombinationCalculator {

    // Optimized method to calculate nCr
    public static long calculateNCR(int n, int r) {
        if (r > n) {
            throw new IllegalArgumentException("r cannot be greater than n");
        }
        if (r == 0 || r == n) return 1;  // Base cases
        r = Math.min(r, n - r);  // Use nCr = nC(n-r) to minimize r
        
        long result = 1;
        for (int i = 0; i < r; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }

    public static void main(String[] args) {

        int n = 5;
        int r = 4;

        try {
            long result = calculateNCR(n, r);
            System.out.println("nCr (" + n + "C" + r + ") = " + result);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

    }
}
