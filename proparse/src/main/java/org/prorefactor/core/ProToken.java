/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.core;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.base.Splitter;

import antlr.CommonHiddenStreamToken;

public class ProToken extends CommonHiddenStreamToken implements Serializable {
  private static final long serialVersionUID = 6330218429653110333L;

  private ABLNodeType nodeType;
  private final boolean synthetic;
  private final int fileIndex;
  private final String fileName;
  private final int macroSourceNum;
  private final int endFile;
  private final int endLine;
  private final int endColumn;
  private final String analyzeSuspend;
  private final boolean macroExpansion;

  public ProToken(ABLNodeType type, String txt) {
    this(type, txt, 0, "", 0, 0, 0, 0, 0, 0, "", true, false);
  }

  public ProToken(@Nonnull ABLNodeType type, @Nonnull String txt, int file, String fileName, int line, int col, int endFile, int endLine, int endCol,
      int macroSourceNum, @Nonnull String analyzeSuspend, boolean synthetic, boolean macroExpansion) {
    // Make sure that the type field is completely hidden in base Token class 
    super(0, txt);

    this.nodeType = type;
    this.fileIndex = file;
    this.fileName = fileName;
    this.macroSourceNum = macroSourceNum;
    this.line = line;
    this.col = col;
    this.endFile = endFile;
    this.endLine = endLine;
    this.endColumn = endCol;
    this.analyzeSuspend = analyzeSuspend;
    this.synthetic = synthetic;
    this.macroExpansion = macroExpansion;
  }

  @Override
  public int getType() {
    return nodeType.getType();
  }

  @Override
  public void setType(int type) {
    this.nodeType = ABLNodeType.getNodeType(type);
    if (this.nodeType == null)
      throw new IllegalArgumentException("Invalid type number " + type);
  }

  public ABLNodeType getNodeType() {
    return nodeType;
  }

  public int getFileIndex() {
    return fileIndex;
  }

  public int getMacroSourceNum() {
    return macroSourceNum;
  }

  @Override
  public String getFilename() {
    return fileName;
  }

  /**
   * Convenience method for (ProToken) getHiddenAfter()
   */
  public ProToken getNext() {
    return (ProToken) getHiddenAfter();
  }

  /**
   * Convenience method for (ProToken) getHiddenBefore()
   */
  public ProToken getPrev() {
    return (ProToken) getHiddenBefore();
  }

  public void setHiddenAfter(ProToken t) {
    // In order to change visibility
    super.setHiddenAfter(t);
  }

  public void setHiddenBefore(ProToken t) {
    // In order to change visibility
    super.setHiddenBefore(t);
  }

  /**
   * @return Ending line of token. Not guaranteed to be identical to the start line
   */
  public int getEndLine() {
    return endLine;
  }

  /**
   * @return Ending column of token. Not guaranteed to be greater than start column, as some tokens may include the
   *         newline character
   */
  public int getEndColumn() {
    return endColumn;
  }

  /**
   * @return File number of end of token. Not guaranteed to be identical to file index, as a token can be spread over
   *         two different files, thanks to the magic of the preprocessor
   */
  public int getEndFileIndex() {
    return endFile;
  }

  /**
   * @return Comma-separated list of &amp;ANALYZE-SUSPEND options. Null for code not managed by AppBuilder.
   */
  public String getAnalyzeSuspend() {
    return analyzeSuspend;
  }

  /**
   * @see org.prorefactor.proparse.antlr4.ProToken#isMacroExpansion()
   */
  public boolean isMacroExpansion() {
    return macroExpansion;
  }

  /**
   * @return True if token is part of an editable section in AppBuilder managed code
   */
  public boolean isEditableInAB() {
    return (analyzeSuspend == null) || isEditableInAB(analyzeSuspend);
  }

  /**
   * @return True if token has been generated by ProParser and not by the lexer
   */
  public boolean isSynthetic() {
    return synthetic;
  }

  /**
   * @return True if token has been generated by the lexer and not by ProParser
   */
  public boolean isNatural() {
    return !synthetic;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProToken) {
      ProToken tok = (ProToken) obj;
      return ((tok.nodeType == this.nodeType) && (tok.text.equals(this.text)) && (tok.line == this.line)
          && (tok.col == this.col) && (tok.fileIndex == this.fileIndex) && (tok.endFile == this.endFile)
          && (tok.endLine == this.endLine) && (tok.endColumn == this.endColumn)
          && (tok.macroSourceNum == this.macroSourceNum));
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeType, text, line, col, fileIndex, endFile, endLine, endColumn, macroSourceNum);
  }

  @Override
  public String toString() {
    return "[\"" + getText().replace('\r', ' ').replace('\n', ' ') + "\",<" + nodeType + ">,macro=" + macroSourceNum
        + ",file=" + fileIndex + ":" + endFile + ",line=" + line + ":" + endLine + ",col=" + col + ":" + endColumn
        + "]";
  }

  /**
   * @return True if token is part of an editable section in AppBuilder managed code
   */
  public static boolean isEditableInAB(@Nonnull String str) {
    List<String> attrs = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(str);
    if (attrs.isEmpty() || !"_UIB-CODE-BLOCK".equalsIgnoreCase(attrs.get(0)))
      return false;

    if ((attrs.size() >= 3) && "_CUSTOM".equalsIgnoreCase(attrs.get(1))
        && "_DEFINITIONS".equalsIgnoreCase(attrs.get(2)))
      return true;
    else if ((attrs.size() >= 2) && "_CONTROL".equalsIgnoreCase(attrs.get(1)))
      return true;
    else if ((attrs.size() == 4) && "_PROCEDURE".equals(attrs.get(1)))
      return true;
    else if ((attrs.size() == 5) && "_PROCEDURE".equals(attrs.get(1)) && "_FREEFORM".equals(attrs.get(4)))
      return true;
    else if ((attrs.size() >= 2) && "_FUNCTION".equals(attrs.get(1)))
      return true;

    return false;
  }
}
