/*
 * hashshmash - An aspect to record HashMap/Set allocations
 * Copyright 2013-2015 MeBigFatGuy.com
 * Copyright 2013-2015 Dave Brosius
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
    
    after() returning (Map<?,?> m): call(ConcurrentHashMap.new(..)) {
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
