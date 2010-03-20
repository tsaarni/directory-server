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
package org.apache.directory.server.core.factory;


import java.io.File;
import java.util.Set;

import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.avl.AvlIndex;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.xdbm.Index;


/**
 * A factory used to generate {@link LdifPartition}s.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class LdifPartitionFactory implements PartitionFactory
{

    /**
     * {@inheritDoc}
     */
    public LdifPartition createPartition( String id, String suffix, int cacheSize, File workingDirectory )
        throws Exception
    {
        LdifPartition partition = new LdifPartition();
        partition.setId( id );
        partition.setSuffix( suffix );
        partition.setCacheSize( 500 );
        partition.setWorkingDirectory( workingDirectory.getAbsolutePath() );
        partition.setPartitionDir( workingDirectory );
        return partition;
    }


    /**
     * {@inheritDoc}
     */
    public void addIndex( Partition partition, String attributeId, int cacheSize ) throws Exception
    {
        if ( !( partition instanceof LdifPartition ) )
        {
            throw new IllegalArgumentException( "Partition must be a LdifPartition" );
        }

        LdifPartition ldifPartition = ( LdifPartition ) partition;
        Set<Index<? extends Object, ServerEntry, Long>> indexedAttributes = ldifPartition.getIndexedAttributes();

        AvlIndex<Object, ServerEntry> index = new AvlIndex<Object, ServerEntry>( attributeId );
        //index.setCacheSize( cacheSize );

        indexedAttributes.add( index );
        ldifPartition.setIndexedAttributes( indexedAttributes );
    }

}
