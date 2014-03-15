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

import java.util.Date;
import java.util.Map;

public class HashMapDetails extends HashDetails {
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