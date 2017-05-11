package info.globalbus.blueprint.plugin.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class BlueprintPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        target.getPlugins().apply("java");
        target.getExtensions().add("settings", PluginSettings.class);
        BlueprintGenerate task = target.getTasks().create("blueprintGenerate", BlueprintGenerate.class);
        SourceSet main = ((SourceSetContainer) target.getProperties().get("sourceSets")).getByName("main");
        main.getResources().srcDir(task.generatedDir);
        task.mustRunAfter("compileJava");
        target.getTasks().getByName("processResources").dependsOn(task);
    }

}