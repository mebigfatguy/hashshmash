/*
 * hashshmash - An aspect to record HashMap/Set allocations
 * Copyright 2013-2014 MeBigFatGuy.com
 * Copyright 2013-2014 Dave Brosius
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class Journaller implements Runnable {
    
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
                                try {
                                    Entry<?, ?>[] table = (Entry<?, ?>[])tableField.get(map);
                                    details.totalSlots = table.length;
                                    for (Entry<?, ?> e : table) {
                                        if (e != null) {
                                            details.usedSlots++;
                                        }
                                    }
                                } catch (IllegalArgumentException iae) {
                                    //ConcurrentHashMap doesn't have a table entry
                                    details.totalSlots = -1;
                                    details.usedSlots = -1;
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