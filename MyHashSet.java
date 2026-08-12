import java.util.Iterator;

public class MyHashSet<E> implements Iterable<E> {
  private Object[] hashArray;
  private int size;

  public MyHashSet() {
    size = 0;
    hashArray = new Object[10000];
  }

  public E get(int id) { // big o of 1
    if (hashArray[id] != null) {
      return (E) hashArray[id];
    }
    return null;
  }

  public boolean add(E obj) {
    if (contains(obj))
      return false;

    hashArray[obj.hashCode()] = obj;
    size++;
    return true;
  }

  public void clear() {
    hashArray = new Object[1000];
    size = 0;
  }

  public boolean contains(Object obj) {
    int hashCode = obj.hashCode();
    return hashArray[hashCode] != null
        && hashArray[hashCode].equals(obj)
        && hashArray[hashCode].hashCode() == obj.hashCode();
  }

  public boolean remove(Object obj) {
    int hashCode = obj.hashCode();

    if (this.contains(obj)) {
      hashArray[hashCode] = null;
      size--;
      return true;
    }
    return false;
  }

  public int size() {
    return size;
  }

  @SuppressWarnings("unchecked")
  public DLList<E> toDLList() {
    DLList<E> list = new DLList<>();
    for (int i = 0; i < hashArray.length; i++) {
      if (hashArray[i] != null) {
        list.add((E) hashArray[i]);
      }
    }
    return list;
  }

  @Override
  public Iterator<E> iterator() {
    return new HashSetIterator<>();
  }

  private class HashSetIterator<F> implements Iterator<F> {
    private int index = 0;
    private int progress = 0;

    @Override
    public boolean hasNext() {
      return progress < size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F next() {
      Object cur = null;
      while (cur == null) {
        index++;
        cur = hashArray[index];
      }
      progress++;
      return (F) cur;
    }
  }
}
