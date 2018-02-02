/*
 * hashshmash - An aspect to record HashMap/Set allocations
 * Copyright 2013-2018 MeBigFatGuy.com
 * Copyright 2013-2018 Dave Brosius
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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashSetDetails extends HashDetails {
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