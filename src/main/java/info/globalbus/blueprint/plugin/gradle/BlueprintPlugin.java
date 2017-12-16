package info.globalbus.blueprint.plugin.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class BlueprintPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        target.getPlugins().apply("java");
        target.getPlugins().apply("osgi");
        target.getExtensions().add("settings", PluginSettings.class);
        BlueprintGenerate blueprintGenerate = target.getTasks().create("blueprintGenerate", BlueprintGenerate.class);
        SourceSet main = ((SourceSetContainer) target.getProperties().get("sourceSets")).getByName("main");
        main.getResources().srcDir(blueprintGenerate.getGeneratedDir());
        blueprintGenerate.mustRunAfter("compileJava");
        target.getTasks().getByName("processResources").dependsOn(blueprintGenerate);
        ImportMixinTask importMixin = target.getTasks().create("importMixin", ImportMixinTask.class);
        importMixin.dependsOn(blueprintGenerate);
        target.getTasks().getByName("jar").dependsOn(importMixin);
    }

}