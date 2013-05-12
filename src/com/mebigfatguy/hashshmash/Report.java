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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


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
            BufferedWriter bw = null;
            try {
                br = new BufferedReader(new FileReader(f));
                Map<String, Map<String, SiteAllocationInfo>> allocations = generateStatistics(br);
                File output = new File(f.getParentFile(), f.getName().substring(0, f.getName().lastIndexOf('.')) + ".html");
                bw = new BufferedWriter(new FileWriter(output));
                writeReport(bw, f.getName(), allocations);
                writeCSS(f.getParentFile());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeQuietly(br);
                closeQuietly(bw);
            }
        }
    }

    private Map<String, Map<String, SiteAllocationInfo>> generateStatistics(BufferedReader br) throws IOException, ParseException {
        Map<String, Map<String, SiteAllocationInfo>> allocations = new HashMap<String, Map<String, SiteAllocationInfo>>();
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
            
            Map<String, SiteAllocationInfo> siteInfo = allocations.get(type);
            if (siteInfo == null) {
                siteInfo = new HashMap<String, SiteAllocationInfo>();
                allocations.put(type, siteInfo);
            }
            
            SiteAllocationInfo info = siteInfo.get(location);
            if (info == null) {
                info = new SiteAllocationInfo();
                siteInfo.put(location, info);
            }
            info.add(allocationTime, size, buckets, usedBuckets);
        }
        
        return allocations;
    }
    
    
    private void writeReport(BufferedWriter bw, String title,
            Map<String, Map<String, SiteAllocationInfo>> allocations) throws TransformerException, ParserConfigurationException {

        InputStream xml = null;
        InputStream xsl = null;
        
        try {
            xml = Report.class.getResourceAsStream("/com/mebigfatguy/hashshmash/report.xml");
            xsl = Report.class.getResourceAsStream("/com/mebigfatguy/hashshmash/report.xslt");
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer(new StreamSource(xsl));
            
            t.setParameter("title", title);
            t.setParameter("bean", new XSLTBean(allocations));
            t.transform(new StreamSource(xml), new StreamResult(bw));
            
        } finally {
            closeQuietly(xml);
            closeQuietly(xsl);
        }  
    }
    
    private void writeCSS(File directory) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(Report.class.getResourceAsStream("/com/mebigfatguy/hashshmash/hashshmash.css"));
            bos = new BufferedOutputStream(new FileOutputStream(new File(directory, "hashshmash.css")));
            
            byte[] buffer = new byte[1024];
            int length = bis.read(buffer);
            while (length >= 0) {
                bos.write(buffer, 0, length);
                length = bis.read(buffer);
            }
        } finally {
            closeQuietly(bis);
            closeQuietly(bos);
        }
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
