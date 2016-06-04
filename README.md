##基于ConcurrentHashMap实现的ConcurrentMultiValueMap
1. ConcurrentMultiValueMap<K, V>类内部聚合了两个ConcurrentHashMap类型的成员变量：其中一个是Map<K, List<V>>类型的chm，用于保存键值对，这里的值是List<V>对象；另一个是Map<K, K>类型的lockMap，用于保存每个键所关联的锁对象，用于synchronized关键字来对“写同一个key”时进行同步，在这里每个键的锁对象使用了这个键对象本身。
2. 每个key对应的List<V>采用ArrayList实现，并发读无需加锁；并发对同一个key写时，需要进行同步，这里会先从lockMap中取得相应key所关联的锁对象，然后获取那个锁对象的monitor来实现同步，这样一来其他线程在要对一个key进行写时，便无法获取它要写的key的锁对象的monitor，从而保证了并发写的线程安全。
3. 在对不同key进行并发写时，由于是向不同的list中添加value，所以无需同步。上面的实现中由于不同的key关联不同的对象，所以写的时候获取的monitor也不同，因此对不同key能够实现并行写而无需同步。

