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
package org.sonar.plugins.openedge.sensor;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.MessageException;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorException;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorSession;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.IProgressSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.IProparseSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.ProgressSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.ProparseSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

import com.google.common.io.Files;

public class OpenEdgeProparseSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeProparseSensor.class);

  private final FileSystem fileSystem;
  private final ActiveRules activeRules;
  private final OpenEdgeSettings settings;
  private final OpenEdgeComponents components;

  public OpenEdgeProparseSensor(FileSystem fileSystem, ActiveRules activesRules, OpenEdgeSettings settings,
      OpenEdgeComponents components) {
    this.fileSystem = fileSystem;
    this.activeRules = activesRules;
    this.settings = settings;
    this.components = components;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdge.KEY) && !settings.skipProparseSensor();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    IProgressSettings settings1 = new ProgressSettings(true, "", "WIN32", this.settings.getPropathAsString(), "11.5",
        "MS-WIN95");
    IProparseSettings settings2 = new ProparseSettings();
    RefactorSession session = new RefactorSession(settings1, settings2, this.settings.getProparseSchema(), fileSystem.encoding());

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.debug("Parsing {}", new Object[] {file.relativePath()});
      if ("i".equals(Files.getFileExtension(file.relativePath()))) {
        // Included files are not parsed
        continue;
      }

      try {
        long time = System.currentTimeMillis();

        ParseUnit unit = new ParseUnit(file.file(), session);
        unit.parse();

        LOG.debug("{} milliseconds to generate ParseUnit", System.currentTimeMillis() - time);
        if (settings.useProparseDebug()) {
          String fileName = ".proparse/" + file.relativePath();
          File dbgFile = new File(fileName);
          dbgFile.getParentFile().mkdirs();
//          try (PrintWriter writer = new PrintWriter(dbgFile)) {
//            JPNodeLister nodeLister = new JPNodeLister(root, writer, new TokenTypes());
//            nodeLister.print();
//          } catch (IOException caught) {
//            LOG.error("Unable to write proparse debug file", caught);
//          }
        }

        for (ActiveRule rule : activeRules.findByLanguage(OpenEdge.KEY)) {
          LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}",
              new Object[] {rule.internalKey(), rule.ruleKey().repository(), rule.ruleKey().rule()});
          RuleKey ruleKey = RuleKey.of(rule.ruleKey().repository(), rule.ruleKey().rule());
          try {
            AbstractLintRule lint = components.getProparseAnalyzer(ruleKey.rule()); // getLintRule(rule);
            if (lint != null) {
              configureFields(rule, lint);
              lint.execute(unit, context, file, ruleKey);
            }
          } catch (ReflectiveOperationException caught) {

          }
        }
      } catch (RefactorException caught) {
        LOG.error("Error during code parsing for " + file.relativePath(), caught);
        NewIssue issue = context.newIssue();
        issue.forRule(
            RuleKey.of(OpenEdgeRulesDefinition.REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY)).at(
                issue.newLocation().on(file).message(caught.getMessage())).save();
      }
    }
    new File("listingparser.txt").delete();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private void configureFields(ActiveRule activeRule, Object check) {
    for (String param : activeRule.params().keySet()) {
      Field field = getField(check, param);
      if (field == null) {
        throw MessageException.of("The field " + param
            + " does not exist or is not annotated with @RuleProperty in the class " + check.getClass().getName());
      }
      if (StringUtils.isNotBlank(activeRule.param(param))) {
        configureField(check, field, activeRule.param(param));
      }
    }

  }

  private void configureField(Object check, Field field, String value) {
    try {
      field.setAccessible(true);

      if (field.getType().equals(String.class)) {
        field.set(check, value);

      } else if ("int".equals(field.getType().getSimpleName())) {
        field.setInt(check, Integer.parseInt(value));

      } else if ("short".equals(field.getType().getSimpleName())) {
        field.setShort(check, Short.parseShort(value));

      } else if ("long".equals(field.getType().getSimpleName())) {
        field.setLong(check, Long.parseLong(value));

      } else if ("double".equals(field.getType().getSimpleName())) {
        field.setDouble(check, Double.parseDouble(value));

      } else if ("boolean".equals(field.getType().getSimpleName())) {
        field.setBoolean(check, Boolean.parseBoolean(value));

      } else if ("byte".equals(field.getType().getSimpleName())) {
        field.setByte(check, Byte.parseByte(value));

      } else if (field.getType().equals(Integer.class)) {
        field.set(check, new Integer(Integer.parseInt(value)));

      } else if (field.getType().equals(Long.class)) {
        field.set(check, new Long(Long.parseLong(value)));

      } else if (field.getType().equals(Double.class)) {
        field.set(check, new Double(Double.parseDouble(value)));

      } else if (field.getType().equals(Boolean.class)) {
        field.set(check, Boolean.valueOf(Boolean.parseBoolean(value)));

      } else {
        throw MessageException.of("The type of the field " + field + " is not supported: " + field.getType());
      }
    } catch (IllegalAccessException e) {
      throw MessageException.of(
          "Can not set the value of the field " + field + " in the class: " + check.getClass().getName());
    }
  }

  private Field getField(Object check, String key) {
    Field[] fields = check.getClass().getDeclaredFields();
    for (Field field : fields) {
      RuleProperty propertyAnnotation = field.getAnnotation(RuleProperty.class);
      if (propertyAnnotation != null) {
        if (StringUtils.equals(key, field.getName()) || StringUtils.equals(key, propertyAnnotation.key())) {
          return field;
        }
      }
    }
    return null;
  }

}
