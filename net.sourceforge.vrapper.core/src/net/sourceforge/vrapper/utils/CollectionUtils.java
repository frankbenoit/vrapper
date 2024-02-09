package net.sourceforge.vrapper.utils;

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class CollectionUtils {

    public static <T> Queue<T> emptyQueue() {
        return new EmptyQueue<T>();
    }
    
    @SafeVarargs
	public static <T> Queue<T> asQueue(T...contents) {
        LinkedList<T> result = new LinkedList<T>();
        Collections.addAll(result, contents);
        return result;
    }

    protected static class EmptyQueue<T> extends AbstractQueue<T> {

        @Override
        public boolean offer(T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T poll() {
            return null;
        }

        @Override
        public T peek() {
            return null;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return 0;
        }
    }
}
