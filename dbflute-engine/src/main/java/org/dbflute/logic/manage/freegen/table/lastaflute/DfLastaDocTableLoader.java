/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.logic.manage.freegen.table.lastaflute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTable;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLastaDocTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = LASTA_DOC
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaDocHtml.vm
    //     ; outputDirectory = $$baseDir$$/../test/resources
    //     ; package = doc
    //     ; className = dockside-lastadoc
    //     ; fileExt = html
    // }
    // ; tableMap = map:{
    //     ; targetDir = $$baseDir$$/java
    // }
    public DfFreeGenTable loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getTableMap();
        final String targetDir = resource.resolveBaseDir((String) tableMap.get("targetDir"));
        final File rootDir = new File(targetDir);
        if (!rootDir.exists()) {
            throw new IllegalStateException("Not found the targetDir: " + targetDir);
        }
        final DfLastaInfo lastaInfo = new DfLastaInfo();
        final FileHierarchyTracer tracer = new FileHierarchyTracer();
        tracer.trace(rootDir, new FileHierarchyTracingHandler() {
            public boolean isTargetFileOrDir(File currentFile) {
                return true;
            }

            public void handleFile(File currentFile) throws IOException {
                final String path = toPath(currentFile);
                if (path.contains("/app/web/") && path.endsWith("Action.java")) {
                    lastaInfo.addAction(currentFile);
                }
            }
        });
        // TODO jflute lastaflute: LASTA_DOC
        final List<Map<String, Object>> columnList = new ArrayList<Map<String, Object>>();
        final List<File> actionList = lastaInfo.getActionList();
        for (File action : actionList) {
            Map<String, Object> columnMap = new LinkedHashMap<String, Object>();
            final String className = Srl.substringLastFront(action.getName(), ".");
            final String url = calculateUrl(className);
            columnMap.put("className", className);
            columnMap.put("url", url);
            columnList.add(columnMap);
        }
        return new DfFreeGenTable(tableMap, "unused", columnList);
    }

    protected String calculateUrl(String className) {
        if ("RootAction".equals(className)) {
            return "/";
        } else {
            return "/" + Srl.decamelize(Srl.removeSuffix(className, "Action"), "/").toLowerCase() + "/";
        }
    }

    public static class DfLastaInfo {

        protected final List<File> actionList = new ArrayList<File>();

        public List<File> getActionList() {
            return actionList;
        }

        public void addAction(File action) {
            actionList.add(action);
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String toPath(File file) {
        return replace(file.getPath(), "\\", "/");
    }

    protected String replace(String str, String fromStr, String toStr) {
        return Srl.replace(str, fromStr, toStr);
    }
}
