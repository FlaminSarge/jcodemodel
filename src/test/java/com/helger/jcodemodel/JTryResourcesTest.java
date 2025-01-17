/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright 2013-2022 Philip Helger + contributors
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.helger.jcodemodel;

import static org.junit.Assert.assertEquals;

import java.io.OutputStream;

import org.junit.Test;

import com.helger.commons.io.stream.NonBlockingBufferedOutputStream;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.jcodemodel.util.CodeModelTestsHelper;
import com.helger.jcodemodel.writer.JCMWriter;

/**
 * Test class for class {@link JTryResource}.
 *
 * @author Philip Helger
 */
public final class JTryResourcesTest
{
  private static final String CRLF = JCMWriter.getDefaultNewLine ();

  @Test
  public void testWith1 ()
  {
    final JCodeModel cm = JCodeModel.createUnified ();

    final JTryBlock aTB = new JTryBlock ();

    final JTryResource aTR1 = new JTryResource (cm.ref (OutputStream.class), "os", cm.ref (NonBlockingByteArrayOutputStream.class)._new ());
    aTB.tryResources ().add (aTR1);
    aTB.body ().add (aTR1.var ().invoke ("read"));
    assertEquals ("try(final java.io.OutputStream os = new com.helger.commons.io.stream.NonBlockingByteArrayOutputStream()) {" +
                  CRLF +
                  "    os.read();" +
                  CRLF +
                  "}" +
                  CRLF,
                  CodeModelTestsHelper.toString (aTB));
  }

  @Test
  public void testWith2 ()
  {
    final JCodeModel cm = JCodeModel.createUnified ();

    final JTryBlock aTB = new JTryBlock ();

    final JTryResource aTR1 = new JTryResource (cm.ref (OutputStream.class), "os", cm.ref (NonBlockingByteArrayOutputStream.class)._new ());
    aTB.tryResources ().add (aTR1);
    final JTryResource aTR2 = new JTryResource (cm.ref (NonBlockingBufferedOutputStream.class),
                                                "bos",
                                                cm.ref (NonBlockingBufferedOutputStream.class)._new ().arg (aTR1.var ()));
    aTB.tryResources ().add (aTR2);
    aTB.body ().add (aTR2.var ().invoke ("readLine"));
    assertEquals ("try(final java.io.OutputStream os = new com.helger.commons.io.stream.NonBlockingByteArrayOutputStream();" +
                  CRLF +
                  "final com.helger.commons.io.stream.NonBlockingBufferedOutputStream bos = new com.helger.commons.io.stream.NonBlockingBufferedOutputStream(os)) {" +
                  CRLF +
                  "    bos.readLine();" +
                  CRLF +
                  "}" +
                  CRLF,
                  CodeModelTestsHelper.toString (aTB));
  }
}
