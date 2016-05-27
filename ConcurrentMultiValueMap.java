import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2016/5/10.
 */
public class ConcurrentMultiValueMap<K, V> {
    private Map<K, List<V>> chm = new ConcurrentHashMap<>();
    private Map<K, K> lockMap = new ConcurrentHashMap<>();
    final CountDownLatch latch = new CountDownLatch(500);

    public V get(K key, int index) {
        List<V> resultList = chm.get(key);
        if (resultList == null) {
            throw new NullPointerException("The ConcurrentMultiValueMap dose not contain given key.");
        } else if (resultList.size() <= index) {
            return null;
        } else {
            return resultList.get(index);
        }
    }

    public List<V> getList(K key) {
        return chm.get(key);
    }

    public void put(K key, V value) {
        K keyLock;
        if ((keyLock = lockMap.get(key)) == null) {
            lockMap.put(key, key);
             keyLock = lockMap.get(key);
        }
        List<V> list = chm.get(key);
        if (list == null) {
            list = new ArrayList<>();
            chm.put(key, list);
        }
        synchronized (keyLock) {
            list.add(value);
        }
    }

    public void put(K key, List<V> value) {
        chm.put(key, value);
    }

    public V remove(K key, int index) {
        List<V> list = chm.get(key);
        K keyLock;
        V removed;
        if ((keyLock = lockMap.get(key)) == null) {
            lockMap.put(key, key);
            keyLock = lockMap.get(key);
        }
        if (list == null) {
            throw new NullPointerException("The ConcurrentMultiValueMap dose not contain given key.");
        } else if (list.size() == 0) {
            chm.remove(key);
            removed = null;
        } else if (list.size() <= index){
            throw new IndexOutOfBoundsException("The list corresponding to given key dose not have enough Node.");
        } else {
            synchronized (keyLock) {
                removed = list.remove(index);
            }
        }
        return removed;
    }

    public V remove(K key) {
        return remove(key, 0);
    }

    public int size(K key) {
        return chm.get(key).size();
    }

    public static void main(String[] args) throws Exception {
        ConcurrentMultiValueMap<Integer, Integer> cmm = new ConcurrentMultiValueMap<>();

        int N = 500;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < N; i++) { //测试并发向不同key中添加value的性能
            new Thread(new MyRunnable(i, cmm, i)).start();
        }
//        for (int i = 0; i < N; i++) { //测试并发向同一key中添加value是否线程安全
//            new Thread(new MyRunnable(i, cmm, i)).start();
//        }
        cmm.latch.await();
        long endTime = System.currentTimeMillis();
        double time = (double) (endTime - startTime) / 1000.0;
        //System.out.println("The size of key 1's list is: " + cmm.size(1) + ", and took " + time + " seconds.");
        System.out.println("Add to 500 different keys up to 6000 value(12 per) took " + time + " seconds");
    }
}

class MyRunnable implements Runnable {
    private int key;
    private ConcurrentMultiValueMap<Integer, Integer> map;
    private int startValue;

    public MyRunnable(int key, ConcurrentMultiValueMap<Integer, Integer> map, int startValue) {
        this.key = key;
        this.map = map;
        this.startValue = startValue;
    }
    
    public void run() {
        map.put(key, startValue);
        map.put(key, startValue * 2);
        map.put(key, startValue * 3);
        map.put(key, startValue * 4);
        map.put(key, startValue * 5);
        map.put(key, startValue * 6);
        map.put(key, startValue);
        map.put(key, startValue * 2);
        map.put(key, startValue * 3);
        map.put(key, startValue * 4);
        map.put(key, startValue * 5);
        map.put(key, startValue * 6);
        map.latch.countDown();
    }
}
