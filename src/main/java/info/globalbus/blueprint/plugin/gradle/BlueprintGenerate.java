package info.globalbus.blueprint.plugin.gradle;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.model.Blueprint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@CacheableTask
public class BlueprintGenerate extends DefaultTask {

    File sourcesDir = new File(getProject().getProjectDir(), "src/main/java");

    File generatedDir = new File(getProject().getBuildDir(), "generatedsources");

    @OutputDirectory
    public File getGeneratedDir(){
        return generatedDir;
    }

    @InputDirectory
    public File getSourcesDir(){
        return sourcesDir;
    }

    @TaskAction
    public void blueprintGenerate() {
        try {
            PluginSettings extension = getProject().getExtensions().findByType(PluginSettings.class);
            if (extension == null) {
                extension = new PluginSettings();
            }
            BlueprintGenerateImpl impl = new BlueprintGenerateImpl(extension);
            impl.execute();
        } catch (Exception ex) {
            log.error("Error on creating blueprint description", ex);
            throw ex;
        }
    }

    @RequiredArgsConstructor
    private class BlueprintGenerateImpl {
        final PluginSettings extension;

        private List<String> getPackagesToScan() {
            List<String> toScan = extension.getScanPaths();
            if (toScan == null || toScan.size() == 0 || toScan.iterator().next() == null) {
                log.info("Scan paths not specified - searching for packages");
                Set<String> packages = PackageFinder.findPackagesInSources(getSourceDir());
                if (packages.contains(null)) {
                    throw new GradleException("Found file without package");
                }
                toScan = new ArrayList<>(packages);
                Collections.sort(toScan);
                extension.setScanPaths(toScan);
            }

            for (String aPackage : toScan) {
                log.info("Package " + aPackage + " will be scanned");
            }
            return toScan;
        }

        public Set<File> getSourceDir() {
            SourceSet main = ((SourceSetContainer) getProject().getProperties().get("sourceSets")).getByName("main");
            return main.getAllJava().getSrcDirs();
        }


        private File getClassesDir(Project project) {
            SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
            return main.getOutput().getClassesDir();
        }

        public void execute() {
            List<String> toScan = getPackagesToScan();

            try {
                ClassLoader classFinder = createProjectScopeFinder();
                BlueprintConfigurationImpl blueprintConfiguration = new BlueprintConfigurationImpl(extension, classFinder);
                Set<Class<?>> classes = FilteredClassFinder.findClasses(classFinder, toScan);
                Blueprint blueprint = new Blueprint(blueprintConfiguration, classes);
                writeBlueprintIfNeeded(blueprint);
            } catch (Exception e) {
                throw new GradleException("Error building commands help", e);
            }
        }

        private void writeBlueprintIfNeeded(Blueprint blueprint) throws Exception {
            if (blueprint.shouldBeGenerated()) {
                writeBlueprint(blueprint);
            } else {
                log.warn("Skipping blueprint generation because no beans were found");
            }
        }

        private void writeBlueprint(Blueprint blueprint) throws Exception {
            File dir = new File(generatedDir, extension.getGeneratedDir());
            File file = new File(dir, extension.getGeneratedFileName());
            dir.mkdirs();
            log.info("Generating blueprint to " + file);

            try (OutputStream fos = new FileOutputStream(file)) {
                new BlueprintFileWriter(fos).write(blueprint);
            }
        }

        private ClassLoader createProjectScopeFinder() throws MalformedURLException {
            List<URL> urls = new ArrayList<>();

            urls.add(getClassesDir(getProject()).toURI().toURL());
            //
            for (File artifact : getProject().getConfigurations().getByName("compile")
                    .getAllDependencies().withType(ProjectDependency.class).stream().map(d-> getClassesDir(d.getDependencyProject())).collect(Collectors.toList())) {
                urls.add(artifact.toURI().toURL());
            }
            ClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());

            return loader;
        }

    }
}