package TheBrain.utils;

import java.util.ArrayList;

public class Node{
    public String name;
    public ArrayList<Node> next = new ArrayList<>();
    public boolean visited = false;

    Node(String brainRegion){
        this.name = brainRegion;
        this.next = null;
    }
}