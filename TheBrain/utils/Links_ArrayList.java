package TheBrain.utils;

import java.util.ArrayList;

import TheBrain.Link;

public class Links_ArrayList extends ArrayList<Link> {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Link link : this) {
            sb.append(link.toString());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int indexOf(Object o) {
        // TODO Auto-generated method stub
        return super.indexOf(o);
    }

    @Override
    public boolean add(Link e) {
        if (this.contains(e)) {
            return true;            
        }
        return super.add(e);
    }
}
