package org.apache.jena.sparql.core.mem;

import java.util.HashMap;
import java.util.function.Function;

public class DefaultingHashMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 1L;
    
    private final Function<K,V> makeDefault;

    public DefaultingHashMap(final Function<K, V> mD) {
        super();
        this.makeDefault = mD;
    }

    @Override
    public V get(Object key) {
        @SuppressWarnings("unchecked")
        final K typedKey = (K) key;
        return getOrDefault(key, makeDefault.apply(typedKey));
    }
    
    

}
