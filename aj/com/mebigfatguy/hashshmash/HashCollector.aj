package com.mebigfatguy.hashshmash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public aspect HashCollector {
    
    private static final Journaller journaller = new Journaller();

    after() returning (Map<?,?> m): call(HashMap.new(..)) {
        String fileName = thisJoinPointStaticPart.getSourceLocation().getFileName();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashMapDetails details = new HashMapDetails(m, new Date(), fileName + ":" + line);
        journaller.add(details);
    }
    
    after() returning (Set<?> s): call(HashSet.new(..)) {
        String fileName = thisJoinPointStaticPart.getSourceLocation().getFileName();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashSetDetails details = new HashSetDetails(s, new Date(), fileName + ":" + line);
        journaller.add(details);
    }
    
    before(): execution(public static void *.main(String[])) {
        Class<?> c = HashCollector.class;
    }
}

final class Journaller implements Runnable {
    
    private static final int SLEEP_TIME = 5 * 1000;
    
    private Field tableField;
    private ConcurrentHashMap<HashDetails, HashDetails> hDetails = new ConcurrentHashMap<HashDetails, HashDetails>();
    private PrintWriter writer;
    private boolean operational = false;

    public Journaller() {
        try {
            System.out.println("*************************");
            System.out.println("HASHSHMASH ASPECT ENABLED");
            System.out.println("*************************");
            
            tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true);
              
            File dir = new File(System.getProperty("user.home"), ".hashshmash");
            dir.mkdirs();  
            
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, new Date().toString() + ".txt")))));
            
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            operational = true;
        }
    }
    
    public void add(HashDetails details) {
        if (operational)
            hDetails.put(details,  details);
    }
    
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(SLEEP_TIME);
                
                for (HashDetails details : hDetails.keySet()) {
                    try {
                        Map<?, ?> map = details.getMap();
                        if (map != null) {
                            int newSize = details.getMap().size();
                            if (newSize == details.size) {
                                Entry<?, ?>[] table = (Entry<?, ?>[])tableField.get(map);
                                details.totalSlots = table.length;
                                for (Entry<?, ?> e : table) {
                                    if (e != null) {
                                        details.usedSlots++;
                                    }
                                }
                                writer.println(details);
                                hDetails.remove(details);
                            } else
                                details.size = newSize;
                        } else {
                            hDetails.remove(details);
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

abstract class HashDetails {
    private static SimpleDateFormat FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    public Date allocTime;
    public String caller;
    public int size;
    public int usedSlots;
    public int totalSlots;
    
    public abstract Map<?,?> getMap();
    
    public abstract String getDetailType();
    
    public String toString() {
        return getDetailType() + "\t" + FORMATTER.format(allocTime) + "\t" + caller + "\t" + getMap().size() + "\t" + totalSlots + "\t" + usedSlots;
    }
}

class HashMapDetails extends HashDetails {
    public Map<?, ?> map;

    public HashMapDetails(Map<?, ?> m, Date d, String c) {
        map = m;
        allocTime = d;
        caller = c;
        size = map.size();
    }
    
    public Map<?,?> getMap() {
        return map;
    }
    
    public String getDetailType() {
        return map.getClass().getSimpleName();
    }
}

class HashSetDetails extends HashDetails {
    private static Field MAP_FIELD;
    static {
        try {
            MAP_FIELD = HashSet.class.getDeclaredField("map");
            MAP_FIELD.setAccessible(true);
        } catch (Exception e) {
            MAP_FIELD = null;
        }
    }
    
    public Set<?> set;

    public HashSetDetails(Set<?> s, Date d, String c) {
        set = s;
        allocTime = d;
        caller = c;
        size = set.size();
    }
    
    public Map<?,?> getMap() {
        try {
            if (MAP_FIELD == null)
                return null;
            
            return (Map<?,?>) MAP_FIELD.get(set);
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getDetailType() {
        return set.getClass().getSimpleName();
    }
}
