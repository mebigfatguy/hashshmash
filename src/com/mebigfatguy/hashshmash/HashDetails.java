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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public abstract class HashDetails {
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