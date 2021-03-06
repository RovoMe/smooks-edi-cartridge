/*-
 * ========================LICENSE_START=================================
 * smooks-ect
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.edi.ect;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.smooks.edi.ect.formats.unedifact.UnEdifactSpecificationReader;
import org.smooks.edi.edisax.EDIConfigurationException;
import org.smooks.edi.edisax.EDIParser;
import org.smooks.edi.edisax.model.internal.Edimap;
import org.smooks.io.StreamUtils;
import org.smooks.util.ClassUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlunit.builder.DiffBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UnEdifactSpecificationReaderTest.
 *
 * @author bardl
 */
@SuppressWarnings("deprecation")
public class UnEdifactSpecificationReaderTest {

    private static UnEdifactSpecificationReader d08AReader_longnames;
    private static UnEdifactSpecificationReader d08AReader_shortnames;
    private XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

    @BeforeAll
    public static void parseD08A() throws Exception {
        InputStream inputStream = UnEdifactSpecificationReaderTest.class.getResourceAsStream("D08A.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        d08AReader_longnames = new UnEdifactSpecificationReader(zipInputStream, false, false);

        inputStream = UnEdifactSpecificationReaderTest.class.getResourceAsStream("D08A.zip");
        zipInputStream = new ZipInputStream(inputStream);
        d08AReader_shortnames = new UnEdifactSpecificationReader(zipInputStream, false, true);
    }

    public void _disabled_test_D08A_Messages() throws IOException, EdiParseException {
        test("BANSTA", d08AReader_longnames);
        test("CASRES", d08AReader_longnames);
        test("INVOIC", d08AReader_longnames);
        test("PAYMUL", d08AReader_longnames);
        test("TPFREP", d08AReader_longnames);
    }

    @Test
    public void test_getMessagesLongName() throws IOException {
        Set<String> messages = d08AReader_longnames.getMessageNames();
        for (String message : messages) {
            Edimap model = d08AReader_longnames.getMappingModel(message);
            StringWriter writer = new StringWriter();
            model.write(writer);
        }
    }

    @Test
    public void test_getMessagesShortName() throws IOException {
        Set<String> messages = d08AReader_shortnames.getMessageNames();
        for (String message : messages) {
            Edimap model = d08AReader_shortnames.getMappingModel(message);
            StringWriter writer = new StringWriter();
            model.write(writer);
        }
    }

    @Test
    public void test_D08A_SegmentsLongName() throws IOException, EdiParseException, SAXException, JDOMException {

        Edimap edimap = d08AReader_longnames.getDefinitionModel();

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);

        Document document = new SAXBuilder().build(new StringReader(stringWriter.toString()));

        testSegment("BGM", document, false);
        testSegment("DTM", document, false);
        testSegment("NAD", document, false);
        testSegment("PRI", document, false);
    }

    @Test
    public void test_D08A_Segments_ShortName() throws IOException, EdiParseException, SAXException, JDOMException {

        Edimap edimap = d08AReader_shortnames.getDefinitionModel();

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);

        Document document = new SAXBuilder().build(new StringReader(stringWriter.toString()));

        testSegment("BGM", document, true);
        testSegment("DTM", document, true);
        testSegment("NAD", document, true);
        testSegment("PRI", document, true);
    }

    @Test
    public void testRealLifeInputFilesD96ALongName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        //Test INVOIC
        String mappingModel = getEdiMessageAsString(d08AReader_longnames, "INVOIC");
        testPackage("d96a-invoic-1", mappingModel);
    }

    @Test
    public void testRealLifeInputFilesD96AShortName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        //Test INVOIC
        String mappingModel = getEdiMessageAsString(d08AReader_shortnames, "INVOIC");
        testPackage("d96a-invoic-shortname", mappingModel);
    }

    @Test
    public void testRealLifeInputFilesD93ALongName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        InputStream inputStream = ClassUtil.getResourceAsStream("d93a.zip", this.getClass());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false, false);

        //Test INVOIC
        String mappingModel = getEdiMessageAsString(ediSpecificationReader, "INVOIC");
        testPackage("d93a-invoic-1", mappingModel);
    }

    @Test
    public void testRealLifeInputFilesD93AShortName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        InputStream inputStream = ClassUtil.getResourceAsStream("d93a.zip", this.getClass());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false, true);

        //Test INVOIC
        String mappingModel = getEdiMessageAsString(ediSpecificationReader, "INVOIC");
        testPackage("d93a-invoic-shortname", mappingModel);
    }

    public void testPackage(String packageName, String mappingModel) throws IOException, SAXException, EDIConfigurationException {
        InputStream testFileInputStream = getClass().getResourceAsStream("testfiles/" + packageName + "/input.edi");

        MockContentHandler contentHandler = new MockContentHandler();
        EDIParser parser = new EDIParser();
        parser.setContentHandler(contentHandler);
        parser.setMappingModel(EDIParser.parseMappingModel(new StringReader(mappingModel)));
        parser.parse(new InputSource(testFileInputStream));

        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("testfiles/" + packageName + "/expected-result.xml"))).trim();
        String actual = contentHandler.xmlMapping.toString();
        
        assertFalse(DiffBuilder.compare(expected).withTest(actual).ignoreComments().ignoreWhitespace().build().hasDifferences());
    }

    private String getEdiMessageAsString(EdiSpecificationReader ediSpecificationReader, String messageType) throws IOException {
        Edimap edimap = ediSpecificationReader.getMappingModel(messageType);
        StringWriter sw = new StringWriter();
        edimap.write(sw);
        return sw.toString();
    }

    private void testSegment(final String segmentCode, Document doc, boolean useShortName) throws IOException, SAXException, JDOMException {
        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/segment/expected-" + (useShortName ? "shortname-" : "") + segmentCode.toLowerCase() + ".xml"))).trim();
        XPath lookup = XPath.newInstance("//medi:segment[@segcode='" + segmentCode + "']");
        lookup.addNamespace("medi", "http://www.milyn.org/schema/edi-message-mapping-1.5.xsd");
        Element node = (Element) lookup.selectSingleNode(doc);
        assertNotNull(node, "Node with segment code " + segmentCode + " wasn't found");
        
        assertFalse(DiffBuilder.compare(expected).withTest(out.outputString(node)).ignoreComments().ignoreWhitespace().build().hasDifferences(), "Failed to compare XMLs for " + segmentCode);
    }

    private void test(String messageName, EdiSpecificationReader ediSpecificationReader) throws IOException {
        Edimap edimap = ediSpecificationReader.getMappingModel(messageName);

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);
//		String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/message/expected-" + messageName.toLowerCase() + ".xml"))).trim();
//
//        String result = removeCRLF(stringWriter.toString());
//		expected = removeCRLF(expected);
//
//        if(!result.equals(expected)) {
//            System.out.println("Expected: \n[" + expected + "]");
//            System.out.println("Actual: \n[" + result + "]");
//            assertEquals("Message [" + messageName + "] failed.", expected, result);
//        }

        StringWriter result = new StringWriter();
        edimap.write(result);
        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/message/expected-" + messageName.toLowerCase() + ".xml"))).trim();

        assertFalse(DiffBuilder.compare(expected).withTest(result.toString()).ignoreWhitespace().build().hasDifferences());
    }

    /************************************************************************
     * Private class MockContentHandler                                     *
     ************************************************************************/
    private class MockContentHandler extends DefaultHandler {

        protected StringBuffer xmlMapping = new StringBuffer();

        public void startDocument() {
            xmlMapping.setLength(0);
        }

        public void characters(char[] ch, int start, int length) {
            xmlMapping.append(ch, start, length);
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            xmlMapping.append("<").append(localName).append(">");
        }

        public void endElement(String namespaceURI, String localName, String qName) {
            xmlMapping.append("</").append(localName).append(">");
        }
    }
}
