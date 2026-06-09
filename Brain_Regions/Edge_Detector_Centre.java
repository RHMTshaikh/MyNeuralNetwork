package Brain_Regions;

import TheBrain.Brain_Region;
import TheBrain.Link;
import TheBrain.Neuron;
import TheBrain.utils.Linked_HashSet_Queue;
import TheBrain.utils.Neurons_ArrayList;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Edge_Detector_Centre extends Brain_Region {
    int number_of_lines;
    int number_of_neurons_in_line;
    List<Neurons_ArrayList> input_lines = new ArrayList<>();
    List<Neurons_ArrayList> output_lines = new ArrayList<>();
    double sensitivity = 0.5;

    public Edge_Detector_Centre(int number_of_lines, int number_of_neurons_in_line, double sensitivity) {
        this.sensitivity = sensitivity;
        this.number_of_lines = number_of_lines;
        this.number_of_neurons_in_line = number_of_neurons_in_line;
        int gap = 15;

        // making input neurons
        for (int i = 0; i < number_of_lines; i++) {
            Neurons_ArrayList line = new Neurons_ArrayList();
            input_lines.add(line);
            int offset = i%2==0 ? 0 : (int)(0.5*gap);
            for (int j = 0; j < number_of_neurons_in_line; j++) {
                Neuron neuron = new Neuron(in_neurons);
                neuron.make_neuron_constant();
                line.add(neuron);
                in_neurons.add(neuron);

                neuron.visual_neuron.set_center(j*gap + offset, i*gap);
            }
        }
        
        // making output neurons
        for (int i = 0; i < number_of_lines; i++) {
            Neurons_ArrayList input_line = input_lines.get(i);
            Neurons_ArrayList output_line = new Neurons_ArrayList();
            output_lines.add(output_line);

            for (int j = 0; j < number_of_neurons_in_line; j++) {
                Neuron central_neuron = input_line.get(j);
                Neuron output_neuron = new Neuron(out_neurons);

                output_neuron.make_neuron_constant();
                output_line.add(output_neuron);
                out_neurons.add(output_neuron);

                central_neuron.link_to(output_neuron, sensitivity).make_link_constant();

                central_neuron.visual_neuron.threshold_circle.setStroke(Color.BLUE);
                output_neuron.visual_neuron.set_center(j*gap+(gap*(number_of_neurons_in_line)), i*gap+(gap*(number_of_lines)));

                if (j > 0) {
                    Neuron preceding_neuron = input_line.get(j-1);
                    preceding_neuron.link_to(output_neuron, -sensitivity/6).make_link_constant();
                }
                if (j < number_of_neurons_in_line-1) {
                    Neuron following_neuron = input_line.get(j+1);
                    following_neuron.link_to(output_neuron, -sensitivity/6).make_link_constant();                    
                }
                
                int a,b;
                if (i%2==0) {
                    a = j;
                    b = j-1;
                } else {
                    a = j;
                    b = j+1;
                }
                if (i > 0) {
                    Neurons_ArrayList line_above = input_lines.get(i-1);
                    if (a > -1 && a < number_of_neurons_in_line) {
                        Neuron neuron1 = line_above.get(a);
                        neuron1.link_to(output_neuron, -sensitivity/6).make_link_constant();                        
                    }
                    if (b > -1 && b < number_of_neurons_in_line) {
                        Neuron neuron2 = line_above.get(b);
                        neuron2.link_to(output_neuron, -sensitivity/6).make_link_constant();;
                    }
                }
                if (i+1 < number_of_lines) {
                    Neurons_ArrayList line_below = input_lines.get(i+1);
                    if (a > -1 && a < number_of_neurons_in_line) {
                        Neuron neuron1 = line_below.get(a);
                        neuron1.link_to(output_neuron, -sensitivity/6).make_link_constant();                        
                    }
                    if (b > -1 && b < number_of_neurons_in_line) {
                        Neuron neuron2 = line_below.get(b);
                        neuron2.link_to(output_neuron, -sensitivity/6).make_link_constant();;
                    }
                }
            }
        }

    }

    public void pixel_intensity_reader(String imagePath, Linked_HashSet_Queue<Neuron> excitedNeurons, int clock) {
        

        // double[][] pixel_intensities = new double[number_of_lines][number_of_neurons_in_line+2];

        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Error: Image file not found at " + imagePath);
                return;
            }
            BufferedImage image = ImageIO.read(imageFile);

            if (image == null) {
                System.err.println("Error: Could not load image. Format might be unsupported or file is corrupt.");
                return;
            }

            int width = image.getWidth();
            int height = image.getHeight();
            // System.out.println("Image loaded successfully: " + width + "x" + height + " pixels.");

            int horizontal_gap = width / (number_of_neurons_in_line + 1);
            int vertical_gap = height / (number_of_lines - 1);

            for (int i = 0; i < number_of_lines; i++) {
                int offset = i%2==0 ? 0 : (int)(horizontal_gap*1.5);

                for (int j = 0; j < number_of_neurons_in_line; j++) {

                    int y = i * vertical_gap;
                    int x = j * horizontal_gap + offset;
                    
                    if (j < 0 || j >= width || i < 0 || i >= height) {
                        System.err.println("Error: Coordinates (" + j + ", " + i + ") are outside the image bounds (0-" + (width - 1) + ", 0-" + (height - 1) + ").");
                        break;
                    }
                    
                    int pixel = image.getRGB(x, y);
        
                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;
        
                    double luminanceIntensity = 0.299 * red + 0.587 * green + 0.114 * blue;
                    double averageIntensity = (red + green + blue) / 3.0;

                    // pixel_intensities[y/(vertical_gap)][x/(horizontal_gap)] = luminanceIntensity;
                    Neuron neuron = input_lines.get(i).get(j);
                    neuron.excite(luminanceIntensity/125, clock);
                    excitedNeurons.offer(neuron);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading image file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void fire_together_wire_together(int clock) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fire_together_wire_together'");
    }

    @Override
    protected double distance_between(Neuron n1, Neuron n2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'distance_between'");
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
    public Link make_internal_connection(double weight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_internal_connection'");
    }

    @Override
    public Link make_output_connection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_output_connection'");
    }

    @Override
    public Link make_output_connection(double weight) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'make_output_connection'");
    }
    
}
