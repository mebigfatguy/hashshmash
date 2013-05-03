package com.mebigfatguy.hashshmash;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.Signature;

public aspect HashCollector {

    private static final int SLEEP_TIME = 10 * 1000;
    private static Field TABLE_FIELD;
    private static ConcurrentHashMap<HashMapDetails, HashMapDetails> HM_DETAILS = new ConcurrentHashMap<HashMapDetails, HashMapDetails>();
    
    static {
        try {
            TABLE_FIELD = HashMap.class.getDeclaredField("table");
            TABLE_FIELD.setAccessible(true);
            
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (!Thread.interrupted()) {
                            Thread.sleep(SLEEP_TIME);
                            
                            for (HashMapDetails details : HM_DETAILS.keySet()) {
                                try {
                                    if (details.map != null) {
                                        int newSize = details.map.size();
                                        if (newSize == details.size) {
                                            Entry<?, ?>[] table = (Entry<?, ?>[])TABLE_FIELD.get(details.map);
                                            details.totalSlots = table.length;
                                            for (Entry<?, ?> e : table) {
                                                if (e != null) {
                                                    details.usedSlots++;
                                                }
                                            }
                                            details.map = null;
                                        }
                                    }
                                } catch (Exception e) {
                                    //might get ConcurrentModificationException or such, just ignore
                                }
                            }
                        }
                    } catch (InterruptedException ie) {    
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            
        }
    }
    
    after() returning (Map m): call(HashMap.new(..)) {
        Signature sig = thisJoinPointStaticPart.getSignature();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashMapDetails details = new HashMapDetails(m, System.currentTimeMillis(), sig + ":" + line);
        HM_DETAILS.put(details,  details);
    }
    
    class HashMapDetails {
        public HashMapDetails(Map<?, ?> m, long t, String c) {
            map = m;
            allocTime = t;
            caller = c;
            size = map.size();
        }
        
        public Map<?, ?> map;
        public long allocTime;
        public String caller;
        public int size;
        public int usedSlots;
        public int totalSlots;
    }
}
