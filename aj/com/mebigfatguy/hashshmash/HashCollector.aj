/*
 * hashshmash - An aspect to record HashMap/Set allocations
 * Copyright 2013 MeBigFatGuy.com
 * Copyright 2013 Dave Brosius
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    
    after() returning (Map<?,?> m): call(LinkedHashMap.new(..)) {
        String fileName = thisJoinPointStaticPart.getSourceLocation().getFileName();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashMapDetails details = new LinkedHashMapDetails(m, new Date(), fileName + ":" + line);
        journaller.add(details);
    }
    
    after() returning (Map<?,?> m): call(ConcurrentHashMapMap.new(..)) {
        String fileName = thisJoinPointStaticPart.getSourceLocation().getFileName();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashMapDetails details = new ConcurrentHashMapDetails(m, new Date(), fileName + ":" + line);
        journaller.add(details);
    }
    
    after() returning (Set<?> s): call(HashSet.new(..)) {
        String fileName = thisJoinPointStaticPart.getSourceLocation().getFileName();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashSetDetails details = new HashSetDetails(s, new Date(), fileName + ":" + line);
        journaller.add(details);
    }
    
    after() returning (Set<?> s): call(LinkedHashSet.new(..)) {
        String fileName = thisJoinPointStaticPart.getSourceLocation().getFileName();
        int line = thisJoinPointStaticPart.getSourceLocation().getLine();
        
        HashSetDetails details = new LinkedHashSetDetails(s, new Date(), fileName + ":" + line);
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
            operational = true;
            t.start();
        } catch (Exception e) {
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
    private static SimpleDateFormat FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss-SSS");
    
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

class LinkedHashMapDetails extends HashMapDetails {

    public LinkedHashMapDetails(Map<?, ?> m, Date d, String c) {
       super(m, d, c);
    }
    
    public String getDetailType() {
        return map.getClass().getSimpleName();
    }
}

class ConcurrentHashMapDetails extends HashMapDetails {
    
    public ConcurrentHashMapDetails(Map<?, ?> m, Date d, String c) {
       super(m, d, c);
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

class LinkedHashSetDetails extends HashSetDetails {
    public Map<?, ?> map;

    public LinkedHashSetDetails(Set<?> s, Date d, String c) {
       super(s, d, c);
    }
    
    public String getDetailType() {
        return map.getClass().getSimpleName();
    }
}
