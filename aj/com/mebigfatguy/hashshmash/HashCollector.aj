package com.mebigfatguy.hashshmash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.Signature;

public aspect HashCollector {
    
    private static final Journaller journaller = new Journaller();

    after() returning (Map m): call(HashMap.new(..)) {
        Signature sig = thisJoinPointStaticPart.getSignature();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashMapDetails details = new HashMapDetails(m, System.currentTimeMillis(), sig + ":" + line);
        journaller.add(details);
    }
}

final class Journaller implements Runnable {
    
    private static final int SLEEP_TIME = 10 * 1000;
    
    private Field tableField;
    private ConcurrentHashMap<HashMapDetails, HashMapDetails> hmDetails = new ConcurrentHashMap<HashMapDetails, HashMapDetails>();
    private PrintWriter writer;

    public Journaller() {
        try {
            tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true);
                        
            File dir = new File(System.getProperty("user.home"), ".hashshmash");
            dir.mkdirs();
            
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, new Date().toString() + ".txt")))));
            
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
        }
    }
    
    public void add(HashMapDetails details) {
        hmDetails.put(details,  details);
    }
    
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(SLEEP_TIME);
                
                for (HashMapDetails details : hmDetails.keySet()) {
                    try {
                        if (details.map != null) {
                            int newSize = details.map.size();
                            if (newSize == details.size) {
                                Entry<?, ?>[] table = (Entry<?, ?>[])tableField.get(details.map);
                                details.totalSlots = table.length;
                                for (Entry<?, ?> e : table) {
                                    if (e != null) {
                                        details.usedSlots++;
                                    }
                                }
                                writer.println(details.allocTime + "\t" + details.caller + "\t" + details.map.size() + "\t" + details.totalSlots + "\t" + details.usedSlots);
                                hmDetails.remove(details);
                            }
                        }
                    } catch (Exception e) {
                        //might get ConcurrentModificationException or such, just ignore
                    }
                }
                writer.flush();
            }
        } catch (InterruptedException ie) {    
        }
    }
}

class HashMapDetails {
    public Map<?, ?> map;
    public long allocTime;
    public String caller;
    public int size;
    public int usedSlots;
    public int totalSlots;

    public HashMapDetails(Map<?, ?> m, long t, String c) {
        map = m;
        allocTime = t;
        caller = c;
        size = map.size();
    }
}
