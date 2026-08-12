public class MyArrayList<E> {
    private Object[] list;
    private int size = 0;
    private int capacity = 10;

    public MyArrayList() {
        list = new Object[capacity];
    }

    public boolean add(E e) {
        if (size == capacity) {
            capacity += 10;

            Object[] listNew = new Object[capacity];

            for (int i = 0; i < list.length; i++) {
                listNew[i] = list[i];
            }

            list = listNew;
        }

        list[size] = e;
        size++;

        return true;
    }

    public void add(int index, E e) {
        if (size == capacity) {
            capacity += 10;

            Object[] listNew = new Object[capacity];

            for (int i = 0; i < list.length; i++) {
                listNew[i] = list[i];
            }

            list = listNew;
        }
        if (index == size) {
            list[size] = e;
            size++;
        } else {
            for (int i = size - 1; i > index - 1; i--) {
                list[i + 1] = list[i];
            }
            list[index] = e;
            size++;
        }

    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) (list[index]);
    }

    public int size() {
        return size;
    }

    public boolean contains(E e) {
        for (int i = 0; i < size; i++) {
            if (list[i].equals(e)) {
                return true;
            }
        }

        return false;
    }

    public boolean remove(E e) {
        for (int i = 0; i < size; i++) {
            if (list[i].equals(e)) {
                for (int j = i + 1; j < size; j++) {
                    list[j - 1] = list[j];
                }
                size--;
                list[size] = null;
                return true;
            }
        }
        return false;
    }

    public E remove(int index) {
        E e = get(index);

        for(int i = index; i<size-1; i++) {
            list[i] = list[i+1];
        }
        size--;
        list[size] = null;
        return e;
    }

    public void set(int i, E e) {
        list[i] = e;
    }

    public String toString() {
        String stringToReturn = "[";
        for (int i = 0; i < size - 1; i++) {
            stringToReturn = stringToReturn + list[i].toString() + ", ";

            stringToReturn += list[size-1].toString() + "]";
        }
        return stringToReturn;
    }
}