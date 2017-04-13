/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright 2013-2017 Philip Helger + contributors
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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.BitSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.jcodemodel.util.JCValueEnforcer;
import com.helger.jcodemodel.util.UnicodeEscapeWriter;

/**
 * Receives generated code and writes to the appropriate storage.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class AbstractCodeWriter implements Closeable
{
  private static final class JavaUnicodeEscapeWriter extends UnicodeEscapeWriter
  {
    private static final BitSet ESCAPE = new BitSet (128);

    static
    {
      for (int i = 0; i < 0x20; i++)
        if (i != '\t' && i != '\r' && i != '\n')
          ESCAPE.set (i, true);
    }

    // can't change this signature to Encoder because
    // we can't have Encoder in method signature
    private final CharsetEncoder m_aEncoder;

    private JavaUnicodeEscapeWriter (@Nonnull final OutputStreamWriter bw)
    {
      super (bw);
      m_aEncoder = Charset.forName (bw.getEncoding ()).newEncoder ();
    }

    @Override
    protected boolean requireEscaping (final int ch)
    {
      // control characters
      if (ESCAPE.get (ch))
        return true;

      // check ASCII chars, for better performance
      if (ch < 0x80)
        return false;

      return !m_aEncoder.canEncode ((char) ch);
    }
  }

  /**
   * Encoding to be used by the writer. Null means platform specific encoding.
   */
  private final Charset m_aEncoding;
  private final String m_sNewLine;

  @Nonnull
  protected static String getDefaultNewLine ()
  {
    String ret = null;
    try
    {
      ret = System.getProperty ("line.separator");
    }
    catch (final Exception ex)
    {
      // Fall through
    }

    if (ret == null || ret.length () == 0)
      ret = "\n";
    return ret;
  }

  @Deprecated
  protected AbstractCodeWriter (@Nullable final Charset aEncoding)
  {
    this (aEncoding, getDefaultNewLine ());
  }

  protected AbstractCodeWriter (@Nullable final Charset aEncoding, @Nonnull final String sNewLine)
  {
    JCValueEnforcer.notNull (sNewLine, "NewLine");

    m_aEncoding = aEncoding;
    m_sNewLine = sNewLine;
  }

  @Nullable
  public Charset encoding ()
  {
    return m_aEncoding;
  }

  /**
   * @return The new line string as provided in the constructor. Defaults to
   *         <code>System.getProperty ("line.separator")</code>
   */
  @Nonnull
  public String getNewLine ()
  {
    return m_sNewLine;
  }

  /**
   * Called by CodeModel to store the specified file. The callee must allocate a
   * storage to store the specified file. <br>
   * The returned stream will be closed before the next file is stored. So the
   * callee can assume that only one OutputStream is active at any given time.
   *
   * @param pkg
   *        The package of the file to be written.
   * @param fileName
   *        File name without the path. Something like "Foo.java" or
   *        "Bar.properties"
   * @return OutputStream to write to
   * @throws IOException
   *         On IO error
   */
  @Nonnull
  public abstract OutputStream openBinary (@Nonnull JPackage pkg, @Nonnull String fileName) throws IOException;

  /**
   * Called by CodeModel to store the specified file. The callee must allocate a
   * storage to store the specified file. <br>
   * The returned stream will be closed before the next file is stored. So the
   * callee can assume that only one OutputStream is active at any given time.
   *
   * @param pkg
   *        The package of the file to be written.
   * @param sFilename
   *        File name without the path. Something like "Foo.java" or
   *        "Bar.properties"
   * @return Writer to write to
   * @throws IOException
   *         On IO error
   */
  @Nonnull
  public SourcePrintWriter openSource (@Nonnull final JPackage pkg, @Nonnull final String sFilename) throws IOException
  {
    final OutputStream os = openBinary (pkg, sFilename);
    final OutputStreamWriter bw = m_aEncoding != null ? new OutputStreamWriter (os, m_aEncoding)
                                                      : new OutputStreamWriter (os);

    // create writer
    Writer aWriter;
    try
    {
      aWriter = new JavaUnicodeEscapeWriter (bw);
    }
    catch (final Throwable t)
    {
      aWriter = new UnicodeEscapeWriter (bw);
    }

    // Ensure result is buffered
    return new SourcePrintWriter (new BufferedWriter (aWriter), m_sNewLine);
  }

  /**
   * Called by CodeModel at the end of the process.
   */
  public abstract void close () throws IOException;
}
