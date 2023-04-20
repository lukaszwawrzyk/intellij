package com.google.idea.blaze.clwb.index.test;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.cidr.lang.search.scopes.OCExplicitSourcesSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class TestSearchScope {

    private static final Key<CachedValue<Collection<VirtualFile>>> EXPLICIT_PROJECT_SOURCE_TEST_FILES =
            Key.create("EXPLICIT_PROJECT_TEST_SOURCE_FILES");

    @NotNull
    public static Collection<VirtualFile> getExplicitlySpecifiedProjectTestSourceFiles(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(
                project, EXPLICIT_PROJECT_SOURCE_TEST_FILES, () -> {
                    ApplicationManager.getApplication().assertReadAccessAllowed();

                    final HashSet<VirtualFile> result = new HashSet<>();

                    final ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
                    final ProjectFileIndex index = rootManager.getFileIndex();
                    final FileTypeRegistry typeRegistry = FileTypeRegistry.getInstance();

                    for (VirtualFile eachSourceRoot : rootManager.getContentSourceRoots()) {
                        ProgressManager.checkCanceled();
                        VfsUtilCore.visitChildrenRecursively(eachSourceRoot, new VirtualFileVisitor<Void>() {
                            @Override
                            public boolean visitFile(@NotNull VirtualFile each) {
                                ProgressManager.checkCanceled();
                                if (index.isExcluded(each) || typeRegistry.isFileIgnored(each)) return false;
                                if (!each.isDirectory() &&
                                        OCExplicitSourcesSearchScope.isInExplicitProjectSources(index, each) &&
                                        TestSourcesFilter.isTestSources(each, project))
                                    result.add(each);
                                return true;
                            }
                        });
                    }

                    return CachedValueProvider.Result.create(
                            Collections.unmodifiableCollection(result),
                            (Object[]) OCExplicitSourcesSearchScope.getProjectSourcesCacheDependenciesExplicitRootsOnly(project));
                }, false);

    }
}
