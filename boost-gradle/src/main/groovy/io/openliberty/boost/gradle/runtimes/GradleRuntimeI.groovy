package boost.gradle.runtimes

import boost.common.runtimes.RuntimeI
import org.gradle.api.Project

public interface GradleRuntimeI extends RuntimeI {
    public void configureRuntimePlugin(Project project)
}