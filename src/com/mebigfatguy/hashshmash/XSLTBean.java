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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.xalan.extensions.ExpressionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XSLTBean {

    private static final double HIGH_ALLOC_COUNT = 1000.0;
    private static final double HIGH_ALLOC_RATE = 10000.0;
    private static final int ONE_MINUTE = 60 * 1000;
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static NumberFormat NUMBER_FORMATTER = NumberFormat.getInstance();
    
    static {
        NUMBER_FORMATTER.setMaximumFractionDigits(2);
        NUMBER_FORMATTER.setMinimumFractionDigits(2);
    }

    private Map<String, Map<String, SiteAllocationInfo>> allocations;
    
    private final Document doc;
    
    public XSLTBean(Map<String, Map<String, SiteAllocationInfo>> allocs) throws ParserConfigurationException {
        allocations = allocs;
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }
    
    public NodeList getTypes(final ExpressionContext ec) {
        return new NodeList() {

            private List<String> types;
            {
                types = new ArrayList<String>(allocations.keySet());
                Collections.<String>sort(types);
            }
            
            @Override
            public Node item(int index) {
                return doc.createTextNode(types.get(index));
            }

            @Override
            public int getLength() {
                return types.size();
            }
        };
    }
    
    public NodeList getLocations(final ExpressionContext ec, final String type) {
        return new NodeList() {

            private List<String> locations;
            {
                final Map<String, SiteAllocationInfo> siteInfo = allocations.get(type);
                if (siteInfo != null) {
                    locations = new ArrayList<String>(siteInfo.keySet());
                    Collections.<String>sort(locations, new Comparator<String>() {

                        @Override
                        public int compare(String site1, String site2) {
                            SiteAllocationInfo sai1 = siteInfo.get(site1);
                            SiteAllocationInfo sai2 = siteInfo.get(site2);
                            return sai2.getNumAllocations() - sai1.getNumAllocations();    
                        }
                    });
                } else {
                    locations = new ArrayList<String>();
                }
            }
            
            @Override
            public Node item(int index) {
                return doc.createTextNode(locations.get(index));
            }

            @Override
            public int getLength() {
                return locations.size();
            }
        };
    }
    
    public NodeList getStatisticRows(final ExpressionContext ec, final String type, final String location) {
        return new NodeList() {

            private SiteAllocationInfo info;
            {
                info = allocations.get(type).get(location);
            }
            
            @Override
            public Node item(int index) {
                Element tr = doc.createElement("tr");
                
                Element td = doc.createElement("td");
                Text txt = doc.createTextNode(DATE_FORMATTER.format(info.getStartAllocationTime()));
                td.appendChild(txt);
                tr.appendChild(td);
                
                td = doc.createElement("td");
                txt = doc.createTextNode(DATE_FORMATTER.format(info.getEndAllocationTime()));
                td.appendChild(txt);
                tr.appendChild(td);
                
                td = doc.createElement("td");
                txt = doc.createTextNode(String.valueOf(info.getNumAllocations()));
                td.appendChild(txt);
                tr.appendChild(td);
                
                td = doc.createElement("td");
                double delta = info.getEndAllocationTime().getTime() - info.getStartAllocationTime().getTime();
                double apm;
                
                if (delta < ONE_MINUTE)
                    apm = info.getNumAllocations();
                else
                    apm = (delta == 0.0) ? 0.0 : ((60000.0 * info.getNumAllocations()) / delta);
                txt = doc.createTextNode(NUMBER_FORMATTER.format(apm));
                if ((info.getNumAllocations() > HIGH_ALLOC_COUNT) && (apm > HIGH_ALLOC_RATE)) {
                    td.setAttribute("class", "high");
                } else if (delta < ONE_MINUTE) {
                    td.setAttribute("class", "suspect");
                }
                td.appendChild(txt);
                tr.appendChild(td);
                
                
                td = doc.createElement("td");
                txt = doc.createTextNode(NUMBER_FORMATTER.format(info.getAverageSize()));
                td.appendChild(txt);
                tr.appendChild(td);
                
                td = doc.createElement("td");
                txt = doc.createTextNode(NUMBER_FORMATTER.format(info.getAverageBuckets()));
                td.appendChild(txt);
                tr.appendChild(td);
                
                td = doc.createElement("td");
                txt = doc.createTextNode(NUMBER_FORMATTER.format(info.getAverageUsedBuckets()));
                td.appendChild(txt);
                tr.appendChild(td);
                
                return tr;
            }

            @Override
            public int getLength() {
                return (info == null) ? 0 : 1;
            }
        };
    }
}
