package com.google.idea.blaze.clwb.index.test;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.execution.testing.CidrPotentialTestHolderRootsProvider;
import com.jetbrains.cidr.execution.testing.CidrTestFrameworkBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class TestSourcesIndexer extends CidrPotentialTestHolderRootsProvider {

    @NotNull
    public Result getPotentialTestHolderRoots(@NotNull Project project, @NotNull CidrTestFrameworkBase<?> framework) {
        Collection<VirtualFile> files = ReadAction.compute(
                () -> TestSearchScope.getExplicitlySpecifiedProjectTestSourceFiles(project));
        return new Result(files, true);
    }
}
