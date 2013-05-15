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

public class SiteAllocationInfo {

    private Date startAllocationTime;
    private Date endAllocationTime;
    private int numAllocations;
    private int totalSize;
    private int totalBuckets;
    private int totalUsedBuckets;
    
    public void add(Date allocationTime, int size, int buckets, int usedBuckets) {
        if (startAllocationTime == null) {
            startAllocationTime = allocationTime;
            endAllocationTime = allocationTime;
        } else {
            if (allocationTime.before(startAllocationTime))
                startAllocationTime = allocationTime;
            if (allocationTime.after(endAllocationTime))
                endAllocationTime = allocationTime;
        }
        endAllocationTime = allocationTime;
        ++numAllocations;
        totalSize += size;
        totalBuckets += buckets;
        totalUsedBuckets += usedBuckets;
    }
    
    public Date getStartAllocationTime() {
        return startAllocationTime;
    }
    
    public Date getEndAllocationTime() {
        return endAllocationTime;
    }
    
    public int getNumAllocations() {
        return numAllocations;
    }
    
    public double getAverageSize() {
        return totalSize / numAllocations;
    }
    
    public double getAverageBuckets() {
        return totalBuckets / numAllocations;
    }
    
    public double getAverageUsedBuckets() {
        return totalUsedBuckets / numAllocations;
    }
}
