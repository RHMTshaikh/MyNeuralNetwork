package TheBrain.utils;

import java.util.HashMap;

import TheBrain.Link;
import TheBrain.Neuron;

public class LinksMap extends HashMap<Neuron, Link> {
    @Override
    public Link remove(Object key) {
        Link link = get(key);
        link.to.links_in.remove(link.from);
        link.from.links_out.remove(link.to);
        return super.remove(key);
    }
    
}
