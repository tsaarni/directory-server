/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.eve.jndi.ibs;


import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attribute;

import org.apache.eve.jndi.AbstractJndiTest;


/**
 * Tests to see that the binary property filtering in the schema service's
 * filter class {@link SchemaService.BinaryAttributeFilter} is working
 * properly.
 *
 * @author <a href="mailto:directory-dev@incubator.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class BinaryAttributeFilterTest extends AbstractJndiTest
{
    private static final String BINARY_KEY = "java.naming.ldap.attributes.binary";


    public void testBinaryExtension() throws NamingException
    {
        Attributes attributes = new BasicAttributes();
        attributes.put( "objectClass", "top" );
        attributes.put( "objectClass", "organizationalUnit" );
        attributes.put( "objectClass", "extensibleObject" );
        attributes.put( "ou", "testing" );
        sysRoot.createSubcontext( "ou=test", attributes );

        // test without turning on the property
        DirContext ctx = ( DirContext ) sysRoot.lookup( "ou=test" ) ;
        Attribute ou = ctx.getAttributes( "" ).get( "ou" );
        Object value = ou.get();
        assertTrue( value instanceof String );

        // test with the property now making ou into a binary value
        sysRoot.addToEnvironment( BINARY_KEY, "ou" );
        ctx = ( DirContext ) sysRoot.lookup( "ou=test" ) ;
        ou = ctx.getAttributes( "" ).get( "ou" );
        value = ou.get();
        assertTrue( value instanceof byte[] );

        byte[] keyValue = new byte[] { 0x45, 0x23, 0x7d, 0x7f };
        // try krb5key which should be binary automatically
        attributes.put( "krb5Key", keyValue );
        sysRoot.createSubcontext( "ou=anothertest", attributes );
        ctx = ( DirContext ) sysRoot.lookup( "ou=anothertest" ) ;
        ou = ctx.getAttributes( "" ).get( "ou" );
        value = ou.get();
        assertTrue( value instanceof byte[] );

        Attribute krb5Key = ctx.getAttributes( "" ).get( "krb5Key" );
        value = krb5Key.get();
        assertTrue( value instanceof byte[] );

        attributes.remove( "krb5Key" );
        attributes.put( "krb5Key", "testing a string" );
        sysRoot.createSubcontext( "ou=yetanothertest", attributes );
        ctx = ( DirContext ) sysRoot.lookup( "ou=yetanothertest" ) ;
        ou = ctx.getAttributes( "" ).get( "ou" );
        value = ou.get();
        assertTrue( value instanceof byte[] );

        krb5Key = ctx.getAttributes( "" ).get( "krb5Key" );
        value = krb5Key.get();
        assertTrue( value instanceof byte[] );
    }
}
