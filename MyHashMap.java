public class MyHashMap<K, V> {
  private Object[] hashArray;
  private int size;
  private MyHashSet<K> keySet;

  public MyHashMap() {
    size = 0;
    hashArray = new Object[10000];
    keySet = new MyHashSet<>();
  }

  public V put(K key, V value) {
    V prev = get(key);

    hashArray[key.hashCode()] = value;
    keySet.add(key);
    if (prev == null) size++;

    return prev;
  }

  @SuppressWarnings("unchecked")
  public V get(K key) {
    return (V) hashArray[key.hashCode()];
  }

  public V remove(K key) {
    V prev = get(key);

    hashArray[key.hashCode()] = null;
    keySet.remove(key);
    if (prev != null) size--;

    return prev;
  }

  public int size() {
    return size;
  }

  public MyHashSet<K> keySet() {
    return keySet;
  }
}
