package info.globalbus.blueprint.plugin.gradle;

import java.util.Collections;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GradlePluginTest {

    @Test
    public void demo_plugin_should_add_task_to_project() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java");
        project.getPlugins().apply("info.globalbus.blueprint-gradle");
        project.getExtensions().findByType(PluginSettings.class)
            .setScanPaths(Collections.singletonList("info.globalbus.blueprint.plugin.test"));

        final Task importMixinTask = project.getTasks().getByName("importMixin");
        assertTrue(importMixinTask instanceof ImportMixinTask);
        Task task = importMixinTask;
        Action<? super Task> actions = task.getActions().get(0);
        actions.execute(task);

        final Task blueprintGenerate = project.getTasks().getByName("blueprintGenerate");
        assertTrue(blueprintGenerate instanceof BlueprintGenerate);
        task = blueprintGenerate;
        actions = task.getActions().get(0);
        actions.execute(task);
    }
}