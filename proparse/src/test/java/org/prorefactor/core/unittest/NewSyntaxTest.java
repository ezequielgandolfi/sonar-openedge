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
package org.prorefactor.core.unittest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparserbase.JPTreeParser;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;
import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

/**
 * Test all tree parsers against new syntax. These tests just run the tree parsers against the data/newsyntax directory.
 * If no exceptions are thrown, then the tests pass. The files in the "newsyntax" directories are subject to change, so
 * no other tests should be added other than the expectation that they parse clean.
 */
public class NewSyntaxTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws InvalidRCodeException, IOException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/newsyntax/101b/deep/FindMe.r")).getTypeInfo());
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/newsyntax/101b/Test1.r")).getTypeInfo());
  }

  private void testNewSyntax(String file) throws ANTLRException {
    // Just run the TreeParser to see if file can be parsed without error
    ParseUnit pu = new ParseUnit(new File("src/test/resources/data/newsyntax", file), session);
    pu.treeParser(new JPTreeParser());
    pu.treeParser01();
  }

  @Test
  public void test01() throws ANTLRException {
    testNewSyntax("101b/Test1.cls");
  }

  @Test
  public void test02() throws ANTLRException {
    testNewSyntax("101b/Test2.cls");
  }

  @Test
  public void test03() throws ANTLRException {
    testNewSyntax("101b/deep/FindMe.cls");
  }

  @Test
  public void test04() throws ANTLRException {
    testNewSyntax("101c102a/test01.p");
  }

  @Test
  public void test05() throws ANTLRException {
    testNewSyntax("101c102a/Test02.cls");
  }

  @Test
  public void test06() throws ANTLRException {
    testNewSyntax("102b/ClassDecl.cls");
  }

  @Test
  public void test07() throws ANTLRException {
    testNewSyntax("102b/CustomException.cls");
  }

  @Test
  public void test08() throws ANTLRException {
    testNewSyntax("102b/Display.cls");
  }

  @Test
  public void test09() throws ANTLRException {
    testNewSyntax("102b/DisplayTest.p");
  }

  @Test
  public void test10() throws ANTLRException {
    testNewSyntax("102b/ExtentTest.cls");
  }

  @Test
  public void test11() throws ANTLRException {
    testNewSyntax("102b/IEmpty.cls");
  }

  @Test
  public void test12() throws ANTLRException {
    testNewSyntax("102b/ITest.cls");
  }

  @Test
  public void test13() throws ANTLRException {
    testNewSyntax("102b/Kernel.cls");
  }

  @Test
  public void test14() throws ANTLRException {
    testNewSyntax("102b/KeywordMethodName.cls");
  }

  @Test
  public void test15() throws ANTLRException {
    testNewSyntax("102b/r-CustObj.cls");
  }

  @Test
  public void test16() throws ANTLRException {
    testNewSyntax("102b/r-CustObjAbstract.cls");
  }

  @Test
  public void test17() throws ANTLRException {
    testNewSyntax("102b/r-CustObjAbstractImpl.cls");
  }

  @Test
  public void test18() throws ANTLRException {
    testNewSyntax("102b/r-CustObjAbstractProc.p");
  }

  @Test
  public void test19() throws ANTLRException {
    testNewSyntax("102b/r-CustObjProc.p");
  }

  @Test
  public void test20() throws ANTLRException {
    testNewSyntax("102b/r-CustObjStatic.cls");
  }

  @Test
  public void test21() throws ANTLRException {
    testNewSyntax("102b/r-CustObjStaticProc.p");
  }

  @Test
  public void test22() throws ANTLRException {
    testNewSyntax("102b/r-DefineProperties1.cls");
  }

  @Test
  public void test23() throws ANTLRException {
    testNewSyntax("102b/r-DefineProperties2.cls");
  }

  @Test
  public void test24() throws ANTLRException {
    testNewSyntax("102b/r-EventPublish.cls");
  }

  @Test
  public void test25() throws ANTLRException {
    testNewSyntax("102b/r-EventPubSub.p");
  }

  @Test
  public void test26() throws ANTLRException {
    testNewSyntax("102b/r-EventSubscribe.cls");
  }

  @Test
  public void test27() throws ANTLRException {
    testNewSyntax("102b/r-ICustObj.cls");
  }

  @Test
  public void test28() throws ANTLRException {
    testNewSyntax("102b/r-ICustObjImpl.cls");
  }

  @Test
  public void test29() throws ANTLRException {
    testNewSyntax("102b/r-ICustObjImpl2.cls");
  }

  @Test
  public void test30() throws ANTLRException {
    testNewSyntax("102b/r-ICustObjProc.p");
  }

  @Test
  public void test31() throws ANTLRException {
    testNewSyntax("102b/r-ICustObjProc2.p");
  }

  @Test
  public void test32() throws ANTLRException {
    testNewSyntax("102b/Settings.cls");
  }

  @Test
  public void test33() throws ANTLRException {
    testNewSyntax("102b/stopafter.p");
  }

  @Test
  public void test34() throws ANTLRException {
    testNewSyntax("102b/type_names.p");
  }

  @Test
  public void test35() throws ANTLRException {
    testNewSyntax("11.4/BaseInterface.cls");
  }

  @Test
  public void test36() throws ANTLRException {
    testNewSyntax("11.4/ExtendedInterface.cls");
  }

  @Test
  public void test37() throws ANTLRException {
    testNewSyntax("11.4/getclass.p");
  }

  @Test
  public void test38() throws ANTLRException {
    testNewSyntax("11.4/SerializableClass.cls");
  }

  @Test
  public void test39() throws ANTLRException {
    testNewSyntax("11.4/StreamHandleClass.cls");
  }

  @Test
  public void test40() throws ANTLRException {
    testNewSyntax("11.4/StreamHandleClass2.cls");
  }

  @Test
  public void test41() throws ANTLRException {
    testNewSyntax("11.6/singlelinecomment.p");
  }

  @Test
  public void test42() throws ANTLRException {
    testNewSyntax("11n/Class01.cls");
  }

  @Test
  public void test43() throws ANTLRException {
    testNewSyntax("11n/Class02.cls");
  }

  @Test
  public void test44() throws ANTLRException {
    testNewSyntax("prolint/regrtest-oo/test1.cls");
  }

  @Test
  public void test45() throws ANTLRException {
    testNewSyntax("prolint/regrtest-oo/test2.cls");
  }

  @Test
  public void test46() throws ANTLRException {
    testNewSyntax("prolint/regrtest-oo/test3.cls");
  }

  @Test
  public void test47() throws ANTLRException {
    testNewSyntax("prolint/regrtest-oo/test4.cls");
  }

  @Test
  public void test48() throws ANTLRException {
    testNewSyntax("prolint/regrtest-oo/test5.cls");
  }

  @Test
  public void test49() throws ANTLRException {
    testNewSyntax("prolint/regrtest-oo/test6.cls");
  }

  @Test
  public void test50() throws ANTLRException {
    testNewSyntax("11n/Class03.cls");
  }

  @Test
  public void test51() throws ANTLRException {
    testNewSyntax("11n/ParameterHandleTo.p");
  }

  @Test
  public void testTenantKeywords() throws ANTLRException {
    testNewSyntax("11n/tenant.p");
  }

}
