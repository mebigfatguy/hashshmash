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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.xalan.extensions.ExpressionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XSLTBean {

    private Map<String, SiteAllocationInfo> allocations;
    private List<String> types;
    private final Document doc;
    
    public XSLTBean(Map<String, SiteAllocationInfo> allocs) throws ParserConfigurationException {
        allocations = allocs;
        
        types = new ArrayList<String>(allocations.keySet());
        Collections.<String>sort(types);
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }
    
    public NodeList getTypes(final ExpressionContext ec) {
        return new NodeList() {
            
            
            
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
    
    private static Document getDocument(Node n) {
        return n.getOwnerDocument();
    }

}
