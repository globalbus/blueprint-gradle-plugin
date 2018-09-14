package info.globalbus.blueprint.plugin.gradle;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.model.Blueprint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

@Slf4j
@CacheableTask
public class BlueprintGenerate extends DefaultTask {

    public static final String BLUEPRINT_IMPORTS_TMP = "BlueprintImports.tmp";

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
            throw new GradleException("Error on creating blueprint description", ex);
        }
    }

    @OutputDirectory
    public File getGeneratedDir() {
        return new File(getProject().getBuildDir(), "generatedsources");
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public Set<File> getSourceDir() {
        SourceSet main = ((SourceSetContainer) getProject().getProperties().get("sourceSets")).getByName("main");
        return main.getAllJava().getSrcDirs();
    }

    @OutputDirectory
    public File getTmpDir() {
        return new File(getProject().getBuildDir(), "tmp");
    }

    @RequiredArgsConstructor
    private class BlueprintGenerateImpl {
        final PluginSettings extension;

        void execute() {
            List<String> toScan = getPackagesToScan();

            try {
                ClassLoader classFinder = createProjectScopeFinder();
                BlueprintConfigurationImpl blueprintConfiguration = new BlueprintConfigurationImpl(extension,
                    classFinder);
                Set<Class<?>> classes = FilteredClassFinder.findClasses(classFinder, toScan);
                Blueprint blueprint = new Blueprint(blueprintConfiguration, classes);
                writeBlueprintIfNeeded(blueprint);
            } catch (Exception e) {
                throw new GradleException("Error building commands help", e);
            }
        }

        private void addToManifest(Blueprint blueprint) {
            File outputFile = new File(getTmpDir(), BLUEPRINT_IMPORTS_TMP);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)))) {
                for (String str : blueprint.getGeneratedPackages()) {
                    writer.write(str);
                    writer.write("\n");
                }
            } catch (IOException ex) {
                throw new GradleException("Cannot save blueprint imports", ex);
            }
        }

        private ClassLoader createProjectScopeFinder() throws MalformedURLException {

            List<URL> urls = Stream.of(getClassesDir(getProject())).flatMap(Collection::stream).map(File::toURI)
                .map(v -> {
                    try {
                        return v.toURL();
                    } catch (MalformedURLException ex) {
                        log.info("Cannot parse classDir", ex);
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
            //
            for (File artifact : getProject().getConfigurations().getByName("runtime").getResolvedConfiguration()
                .getFiles()) {
                urls.add(artifact.toURI().toURL());
            }

            return new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
        }

        private Set<File> getClassesDir(Project project) {
            SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");
            return main.getOutput().getClassesDirs().getFiles();
        }

        private List<String> getPackagesToScan() {
            List<String> toScan = extension.getScanPaths();
            log.info("Scan paths not specified - searching for packages");
            Set<String> packages = PackageFinder.findPackagesInSources(getSourceDir());
            if (packages.contains(null)) {
                throw new GradleException("Found file without package");
            }
            if (toScan == null || toScan.isEmpty() || toScan.iterator().next() == null) {
                toScan = new ArrayList<>(packages);
            } else {
                toScan.addAll(packages);
            }
            Collections.sort(toScan);
            extension.setScanPaths(toScan);
            for (String aPackage : toScan) {
                log.info("Package " + aPackage + " will be scanned");
            }
            return toScan;
        }

        private void writeBlueprint(Blueprint blueprint) throws IOException, XMLStreamException {
            File dir = new File(getGeneratedDir(), extension.getGeneratedDir());
            File file = new File(dir, extension.getGeneratedFileName());
            dir.mkdirs();
            log.info("Generating blueprint to " + file);

            try (OutputStream fos = new FileOutputStream(file)) {
                new BlueprintFileWriter(fos).write(blueprint);
            }
        }

        private void writeBlueprintIfNeeded(Blueprint blueprint) throws IOException, XMLStreamException {
            if (blueprint.shouldBeGenerated()) {
                writeBlueprint(blueprint);
                addToManifest(blueprint);
            } else {
                log.warn("Skipping blueprint generation because no beans were found");
            }
        }

    }
}