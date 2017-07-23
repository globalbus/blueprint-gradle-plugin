package info.globalbus.blueprint.plugin.gradle;

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

        assertTrue(project.getTasks().getByName("blueprintGenerate") instanceof BlueprintGenerate);
        Task task = project.getTasks().getByName("blueprintGenerate");
        Action<? super Task> actions = task.getActions().get(0);
        actions.execute(task);
    }
}