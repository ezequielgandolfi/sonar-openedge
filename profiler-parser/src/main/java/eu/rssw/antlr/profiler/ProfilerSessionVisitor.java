/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
 * contact AT riverside DASH software DOT fr
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.antlr.profiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.rssw.antlr.profiler.ProfilerGrammarParser.Call_tree_dataContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.Call_tree_data_lineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.Coverage_sectionContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.Coverage_section_lineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.DescriptionContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.Line_summary_lineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.Module_data_lineContext;

public class ProfilerSessionVisitor extends ProfilerGrammarBaseVisitor<Void> {
  private final List<Module> modules;
  private ProfilerSession session;
  private Module lastModule;

  public ProfilerSessionVisitor() {
    modules = new ArrayList<>();
  }

  public ProfilerSession getSession() {
    return session;
  }

  @Override
  public Void visitDescription(DescriptionContext ctx) {
    if (session != null)
      throw new IllegalStateException("Session already created");

    session = new ProfilerSession(ctx.desc.getText(), ctx.author.getText(), ctx.date.getText() + " "
        + ctx.time.getText());

    return null;
  }

  @Override
  public Void visitModule_data_line(Module_data_lineContext ctx) {
    modules.add(new Module(Integer.parseInt(ctx.id.getText()), ctx.name.getText(), ctx.debugListingFile.getText(),
        Integer.parseInt(ctx.crc.getText())));

    return null;
  }

  @Override
  public Void visitCall_tree_data(Call_tree_dataContext ctx) {
    // Modules are parsed, adding them to ProfilerSession
    Collections.sort(modules);
    for (Module m : modules) {
      session.addModule(m);
    }
    session.initializeCallTreeMatrix();

    return visitChildren(ctx);
  }

  @Override
  public Void visitCall_tree_data_line(Call_tree_data_lineContext ctx) {
    session.addCall(Integer.parseInt(ctx.callerId.getText()), Integer.parseInt(ctx.calleeId.getText()),
        Integer.parseInt(ctx.callCount.getText()));

    return null;
  }

  @Override
  public Void visitLine_summary_line(Line_summary_lineContext ctx) {
    Module module = session.getModuleById(Integer.parseInt(ctx.moduleId.getText()));
    if (module == null)
      return null;
    LineData lineData = new LineData(Integer.parseInt(ctx.lineNumber.getText()),
        Integer.parseInt(ctx.execCount.getText()), Float.parseFloat(ctx.actualTime.getText()),
        Float.parseFloat(ctx.cumulativeTime.getText()));
    module.addLineSummary(lineData);

    return null;
  }

  @Override
  public Void visitCoverage_section(Coverage_sectionContext ctx) {
    lastModule = session.getModuleById(Integer.parseInt(ctx.moduleId.getText()));

    return visitChildren(ctx);
  }

  @Override
  public Void visitCoverage_section_line(Coverage_section_lineContext ctx) {
    lastModule.addLineToCover(Integer.parseInt(ctx.linenum.getText()));

    return null;
  }
}
