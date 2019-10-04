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
public class XMLDependencyNodeVisitor extends AbstractSerializingVisitor implements DependencyNodeVisitor
{
    public static final String WHITESPACE_TOKEN = "   ";

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
        if ( node.getParent() == null || node.getParent() == node )
        {
            writeNode( node, true, false, 0 );

            List<DependencyNode> children = node.getChildren();
            for ( DependencyNode child : children )
            {
                handleChild( child, 1 );
            }

        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endVisit( DependencyNode node )
    {
        if ( node.getParent() == null || node.getParent() == node )
        {
            writeNode( node, true, true, 0 );
        }
        return true;
    }

    /**
     * Render child with its children recursively
     *
     * @param node child node to handle
     * @param depth depth of the child
     */
    private void handleChild( DependencyNode node, int depth )
    {
        List<DependencyNode> children = node.getChildren();
        if ( children.size() == 0 )
        {
            writeNode( node, false, false, depth );
            return;
        }

        writeNode( node, true, false, depth );

        for ( DependencyNode child : children )
        {
            handleChild( child, depth + 1 );
        }

        writeNode( node, true, true, depth );
    }

    /**
     * Get amount of whitespace
     *
     * @param amount amount of {@link XMLDependencyNodeVisitor.WHITESPACE_TOKEN}
     */
    private String getSpaces( int amount )
    {
        String ret = "";
        for ( int i = 0; i < amount; i += 1 )
        {
            ret += WHITESPACE_TOKEN;
        }
        return ret;
    }

    /**
     * Write node with writer
     *
     * @param node Node to write
     * @param withChildren does the node have children
     * @param closing closing tag?
     * @param depth depth of this node
     */
    private void writeNode( DependencyNode node, boolean withChildren, boolean closing, int depth )
    {
        Artifact artifact = node.getArtifact();

        writer.write(
            getSpaces( depth )
            + ( closing ? "</" : "<" )
            + artifact.getArtifactId()
            + ( closing ? "" : " version=\"" + artifact.getVersion() + "\"" )
            + ( withChildren && !closing ? ">" : "/>" )
            + System.lineSeparator()
        );
    }

}
