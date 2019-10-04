package org.apache.maven.plugins.dependency.tree;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;

import java.io.Writer;
import java.util.List;

/**
 * A dependency node visitor that serializes visited nodes to
 * <a href="https://en.wikipedia.org/wiki/XML">XML format</a>
 *
 * @author <a href="mailto:sikora.bogdan@webscope.io">Bogdan Sikora</a>
 * @since 3.1.2
 */
public class XMLDependencyNodeVisitor
    extends AbstractSerializingVisitor
    implements DependencyNodeVisitor
{
    /**
     * Constructor.
     *
     * @param writer the writer to write to.
     */
    public XMLDependencyNodeVisitor( Writer writer )
    {
        super( writer );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit( DependencyNode node )
    {
        try
        {
            if ( node.getParent() == null || node.getParent() == node )
            {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.newDocument();
                Element rootElement = getNode( doc, node );
                doc.appendChild( rootElement );

                List<DependencyNode> children = node.getChildren();
                for ( DependencyNode child : children )
                {
                    handleChild( doc, child, rootElement );
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource( doc );

                StreamResult console = new StreamResult( writer );

                transformer.transform( source, console );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endVisit( DependencyNode node )
    {
        return true;
    }

    /**
     * Render child with its children recursively
     *
     * @param doc Docuemnt to use
     * @param node child node to handle
     * @param depth depth of the child
     */
    private void handleChild( Document doc, DependencyNode node, Element parent )
    {
        Element element = getNode( doc, node );
        parent.appendChild( element );

        List<DependencyNode> children = node.getChildren();
        for ( DependencyNode child : children )
        {
            handleChild( doc, child, element );
        }
    }

    /**
     * Get element from node
     *
     * @param doc Docuemnt to use
     * @param node Node to get data from
     */
    private Element getNode( Document doc, DependencyNode node )
    {
        Artifact artifact = node.getArtifact();
        Element element = doc.createElement( artifact.getArtifactId() );
        element.setAttribute( "version", artifact.getVersion() );
        return element;
    }
}
