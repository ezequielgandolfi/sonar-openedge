/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2014 Riverside Software
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
package org.sonar.plugins.openedge.foundation;

import java.util.Arrays;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.plugins.openedge.api.AnnotationBasedRulesDefinition;

public class OpenEdgeRulesDefinition implements RulesDefinition {
  public static final String REPOSITORY_KEY = "rssw-oe";
  public static final String REPOSITORY_NAME = "Riverside Software";

  public static final String COMPILER_WARNING_RULEKEY = "compiler.warning";
  public static final String PROPARSE_ERROR_RULEKEY = "proparse.error";

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(REPOSITORY_KEY, OpenEdge.KEY).setName(REPOSITORY_NAME);

    AnnotationBasedRulesDefinition annotationLoader = new AnnotationBasedRulesDefinition(repository, OpenEdge.KEY);
    annotationLoader.addRuleClasses(false, false, Arrays.<Class> asList(OpenEdgeRulesRegistrar.ppCheckClasses()));
    annotationLoader.addRuleClasses(false, false, Arrays.<Class> asList(OpenEdgeRulesRegistrar.xrefCheckClasses()));

    // Manually created rule for compiler warnings
    repository.createRule(COMPILER_WARNING_RULEKEY).setName("Compiler warnings").setHtmlDescription(
        "Warnings generated by the OpenEdge compiler").setSeverity(Priority.CRITICAL.name());
    // Manually created rule for proparse errors
    repository.createRule(PROPARSE_ERROR_RULEKEY).setName("Proparse error").setHtmlDescription(
        "Error during generation of Proparse AST").setSeverity(Priority.CRITICAL.name());

    repository.done();
  }

}