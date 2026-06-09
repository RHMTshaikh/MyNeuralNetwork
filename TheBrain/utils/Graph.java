package TheBrain.utils;

import java.util.ArrayList;

public class Graph {
    ArrayList<Node> allNodes = new ArrayList<>();
    public ArrayList<Node> inputNodes = new ArrayList<>();

    public Graph(){
    }

    public Node addNode(String brainRegion){ 
        Node node = new Node(brainRegion);
        allNodes.add(node);
        return node;
    }

    public Node searchNode(String brainRegion){
        for (Node node : allNodes){
            if (node.name.equals(brainRegion)){
                return node;
            }
        }
        return null;
    }

}

