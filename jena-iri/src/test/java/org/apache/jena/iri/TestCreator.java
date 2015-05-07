/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.iri;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.IRIRelativize ;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


final class TestCreator extends DefaultHandler implements IRIRelativize {
    
    static final int RelativizeFlags = ABSOLUTE|GRANDPARENT|NETWORK|PARENT|CHILD|SAMEDOCUMENT;
    public static String substituteStandardEntities(String s) {
        s = replace(s, "&", "&amp;");
        s = replace(s, "<", "&lt;");
        s = replace(s, ">", "&gt;");
        s = replace(s, "'", "&apos;");
        s = replace(s, "\t", "&#9;");
        s = replace(s, "\n", "&#xA;");
        s = replace(s, "\r", "&#xD;");
        return replace(s, "\"", "&quot;");
    }

    public static String replace(
        String s,
        String oldString,
        String newString) {
        String result = "";
        int length = oldString.length();
        int pos = s.indexOf(oldString);
        int lastPos = 0;
        while (pos >= 0) {
            result = result + s.substring(lastPos, pos) + newString;
            lastPos = pos + length;
            pos = s.indexOf(oldString, lastPos);
        }
        return result + s.substring(lastPos, s.length());
    }
//    static final IRI empty = IRIFactory.defaultFactory().emptyIRI();
    static final Class<?> attSign[] = new Class[] { Attributes.class };
    static final Class<?> nullSign[] = new Class[] { };

    static PrintWriter out;
    static void load() throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory fact = SAXParserFactory.newInstance();
        out = new PrintWriter(new OutputStreamWriter(
          new FileOutputStream("src/test/resources/org/apache/jena/iri/test.xml"),
          "utf-8"
        ));
        out.println("<UriTests>");
        
        try ( InputStream in = TestCreator.class.getClassLoader().getResourceAsStream("org/apache/jena/iri/uris.xml") ) {
            fact.newSAXParser().parse(in, new TestCreator() );
            out.println("</UriTests>");
            out.close();
        }
    }
    
    static public void main(String args[]) throws IOException, ParserConfigurationException, SAXException{
        try {
            load();
        } catch (SAXParseException e) {
            System.err.println(e.getLineNumber());
            System.err.println(e.toString());
            System.err.println(e.getMessage());
        } 
    }
    
    
    @Override
    public void startElement(String arg1,
                             String arg2,
                             String name,
                             Attributes att
        ) {
        try {
            this.getClass().getDeclaredMethod(name,attSign)
            .invoke(this, att );
        } catch (IllegalArgumentException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void endElement(String arg1,
                           String arg2,
                           String name
                )
    {
    }
            
    static String methods[] =  {
        "getRawHost",
        "getRawPath",
        "getPort",
        "getRawQuery",
        "getScheme",
        "getRawUserinfo",
        "getRawFragment",
        "getASCIIHost",
        "isRootless",
        "toString",
        "toDisplayString",
//        "hasException",
        "isAbsolute",
//        "isIRI",
//        "isOpaque",
//        "isRDFURIReference",
        "isRelative",
//        "isURIinASCII",
//        "isVeryBad",
//        "isXSanyURI",
        "toASCIIString"
    };

    // TODO set conformance level for this factory
    static IRIFactory factory = new IRIFactory();
    static {
        factory.setSameSchemeRelativeReferences("file");
        factory.useSchemeSpecificRules("*",true);
    }
}
