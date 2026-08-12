import java.io.Serializable;

public class DLList<E> implements Serializable{
  private Node<E> head;
  private Node<E> tail;
  private int size;

  public DLList() {
    head = new Node<E>(null);
    tail = new Node<E>(null);
    head.setNext(tail);
    head.setPrev(null);
    tail.setNext(null);
    tail.setPrev(head);
    size = 0;
  }

  public boolean add(E element) {
    Node<E> newNode = new Node<E>(element);
    Node<E> prevNode = tail.prev();
    prevNode.setNext(newNode);
    newNode.setPrev(prevNode);
    newNode.setNext(tail);
    tail.setPrev(newNode);
    size++;
    return true;
  }

  public void add(int index, E element) {
    if (index == size) {
      add(element);
      return;
    }

    Node<E> current = getNode(index);
    Node<E> prevNode = current.prev();
    Node<E> newNode = new Node<E>(element);
    prevNode.setNext(newNode);
    newNode.setPrev(prevNode);
    newNode.setNext(current);
    current.setPrev(newNode);
    size++;
  }

  public E get(int index) {
    return getNode(index).get();
  }

  public E remove(int index) {
    Node<E> current = getNode(index);
    Node<E> prevNode = current.prev();
    Node<E> nextNode = current.next();
    prevNode.setNext(current.next());
    nextNode.setPrev(current.prev());
    size--;
    return current.get();
  }

  public boolean remove(Object o) {
    Node<E> current = head.next();
    while (current != tail) {
      if ((o == null && current.get() == null) || (o != null && o.equals(current.get()))) {

        Node<E> prevNode = current.prev();
        Node<E> nextNode = current.next();

        prevNode.setNext(nextNode);
        nextNode.setPrev(prevNode);

        size--;
        return true;
      }
      current = current.next();
    }
    return false;
  }

  public int size() {
    return size;
  }

  public E set(int index, E element) {
    Node<E> current = getNode(index);
    E oldElement = current.get();
    current.setData(element);
    return oldElement;
  }

  public String toString() {
    String toString = "[";
    Node<E> current = head.next();
    while (current != tail) {
      toString += current.get();
      if (current.next() != tail) {
        toString += ", ";
      }
      current = current.next();
    }
    toString += "]";
    return toString;
  }

  private Node<E> getNode(int index) {
    Node<E> current;
    if (index < size / 2) {
      current = head.next();
      for (int i = 0; i < index; i++) {
        current = current.next();
      }
    } else {
      current = tail.prev();
      for (int i = size - 1; i > index; i--) {
        current = current.prev();
      }
    }
    return current;
  }

  public void clear() {
    head.setNext(tail);
    tail.setPrev(head);
    size = 0;
  }
}
