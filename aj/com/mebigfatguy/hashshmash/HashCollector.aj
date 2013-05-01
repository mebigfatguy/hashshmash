package com.mebigfatguy.hashshmash;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.Signature;

public aspect HashCollector {

    private static final int SLEEP_TIME = 10 * 1000;
    private static ConcurrentHashMap<HashMapDetails, HashMapDetails> hmDetails = new ConcurrentHashMap<HashMapDetails, HashMapDetails>();
    
    static {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        Thread.sleep(SLEEP_TIME);
                        
                        for (HashMapDetails details : hmDetails.keySet()) {
                            try {
                                int newSize = details.map.size();
                                if (newSize == details.size) {
                                    //TODO: HashMap hasn't changed size for a while report and release reference
                                    hmDetails.remove(details);
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
    }
    
    after() returning (Map m): call(HashMap.new(..)) {
        Signature sig = thisJoinPointStaticPart.getSignature();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashMapDetails details = new HashMapDetails(m, System.currentTimeMillis(), sig + ":" + line);
        hmDetails.put(details,  details);
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
    }
}
