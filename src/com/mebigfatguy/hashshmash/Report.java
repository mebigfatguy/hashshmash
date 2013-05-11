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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Report {

    private static SimpleDateFormat FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss-SSS");
    
    private File directory;
    
    public Report() throws FileNotFoundException {
        directory = new File(System.getProperty("user.home"), ".hashshmash");
        if (!directory.isDirectory()) {
            throw new FileNotFoundException("Directory " + directory + " was not found");
        }
    }
    
    public void generateReports() {
        for (File f : directory.listFiles(new AllocationsFileFilter())) {          
            BufferedReader br = null;
            PrintWriter pw = null;
            try {
                br = new BufferedReader(new FileReader(f));
                Map<String, SiteAllocationInfo> allocations = generateStatistics(br);
                File output = new File(f.getParentFile(), f.getName().substring(0, f.getName().lastIndexOf('.')) + ".html");
                pw = new PrintWriter(new BufferedWriter(new FileWriter(output)));
                writeReport(pw, allocations);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeQuietly(br);
                closeQuietly(pw);
            }
        }
    }

    private Map<String, SiteAllocationInfo> generateStatistics(BufferedReader br) throws IOException, ParseException {
        Map<String, SiteAllocationInfo> allocations = new HashMap<String, SiteAllocationInfo>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] elements = line.split("\t");
            if (elements.length != 6)
                throw new IllegalArgumentException("Invalid input file");
            
            String type = elements[0];
            Date allocationTime = FORMATTER.parse(elements[1]);
            String location = elements[2];
            int size = Integer.parseInt(elements[3]);
            int buckets = Integer.parseInt(elements[4]);
            int usedBuckets = Integer.parseInt(elements[5]);
            
            SiteAllocationInfo info = allocations.get(type);
            if (info == null) {
                info = new SiteAllocationInfo();
                allocations.put(type, info);
            }
            info.add(allocationTime, location, size, buckets, usedBuckets);
        }
        
        return allocations;
    }
    
    
    private void writeReport(PrintWriter pw,
            Map<String, SiteAllocationInfo> allocations) {

        
    }
    
    private static void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        
        Report r = new Report();
        r.generateReports();
    }
}