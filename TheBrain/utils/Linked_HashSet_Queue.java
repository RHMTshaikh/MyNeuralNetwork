package TheBrain.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class Linked_HashSet_Queue<E> {
    private final LinkedHashSet<E> set = new LinkedHashSet<>();
    
    public boolean offer(E e) {
        return set.add(e);
    }
    
    public E poll() {
        Iterator<E> iterator = set.iterator();
        if (iterator.hasNext()) {
            E first = iterator.next();
            iterator.remove();
            return first;
        }
        return null;
    }

    public E peek() {
        Iterator<E> iterator = set.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
    
    public boolean has(E e) {
        return set.contains(e);
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }
    
    public int size() {
        return set.size();
    }
    
    public void clear() {
        set.clear();
    }
    
    @Override
    public String toString() {
        return set.toString();
    }

    public static void main(String[] args) {
        Linked_HashSet_Queue<String> queue = new Linked_HashSet_Queue<>();
        queue.offer("first");
        queue.offer("second");
        queue.offer("third");
        queue.offer("first");
    
        System.out.println("Queue after offers: " + queue);
    
        System.out.println("Peek: " + queue.peek());
        System.out.println("Poll: " + queue.poll());
        System.out.println("Queue after poll: " + queue);
    
        System.out.println("Peek: " + queue.peek());
        System.out.println("Poll: " + queue.poll());
        System.out.println("Queue after poll: " + queue);
    
        System.out.println("Peek: " + queue.peek());
        System.out.println("Poll: " + queue.poll());
        System.out.println("Queue after poll: " + queue);
    }
}

