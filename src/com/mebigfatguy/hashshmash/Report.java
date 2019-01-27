/*
 * hashshmash - An aspect to record HashMap/Set allocations
 * Copyright 2013-2019 MeBigFatGuy.com
 * Copyright 2013-2019 Dave Brosius
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final String REPORT_XML = "/com/mebigfatguy/hashshmash/report.xml";
    private static final String REPORT_XSLT = "/com/mebigfatguy/hashshmash/report.xslt";
    private static final String REPORT_CSS = "/com/mebigfatguy/hashshmash/hashshmash.css";

    private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss-SSS");
    private Path directory;

    public Report() throws FileNotFoundException {
        directory = Paths.get(System.getProperty("user.home"), ".hashshmash");
        if (!Files.isDirectory(directory)) {
            throw new FileNotFoundException("Directory " + directory + " was not found");
        }
    }

    public void generateReports() {
        try {
            for (Path f : Files.newDirectoryStream(directory, new AllocationsPathFilter())) {
                try (BufferedReader br = Files.newBufferedReader(f)) {
                    Map<String, Map<String, SiteAllocationInfo>> allocations = generateStatistics(br);
                    String name = f.getName(-1).toString();
                    Path output = f.getParent().resolve(name.substring(0, name.lastIndexOf('.')) + ".html");
                    try (BufferedWriter bw = Files.newBufferedWriter(output)) {
                        writeReport(bw, name, allocations);
                    }
                    writeCSS(f.getParent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Map<String, SiteAllocationInfo>> generateStatistics(BufferedReader br) throws IOException, ParseException {
        Map<String, Map<String, SiteAllocationInfo>> allocations = new HashMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] elements = line.split("\t");
            if (elements.length != 6) {
                throw new IllegalArgumentException("Invalid input file");
            }

            String type = elements[0];
            Date allocationTime = formatter.parse(elements[1]);
            String location = elements[2];
            int size = Integer.parseInt(elements[3]);
            int buckets = Integer.parseInt(elements[4]);
            int usedBuckets = Integer.parseInt(elements[5]);

            Map<String, SiteAllocationInfo> siteInfo = allocations.get(type);
            if (siteInfo == null) {
                siteInfo = new HashMap<>();
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

    private void writeReport(BufferedWriter bw, String title, Map<String, Map<String, SiteAllocationInfo>> allocations)
            throws TransformerException, ParserConfigurationException {

        InputStream xml = null;
        InputStream xsl = null;

        try {
            xml = new BufferedInputStream(Report.class.getResourceAsStream(REPORT_XML));
            xsl = new BufferedInputStream(Report.class.getResourceAsStream(REPORT_XSLT));

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

    private void writeCSS(Path directory) throws IOException {

        try (BufferedInputStream bis = new BufferedInputStream(Report.class.getResourceAsStream(REPORT_CSS));
                BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(directory.resolve("hashshmash.css")))) {

            byte[] buffer = new byte[1024];
            int length = bis.read(buffer);
            while (length >= 0) {
                bos.write(buffer, 0, length);
                length = bis.read(buffer);
            }
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
