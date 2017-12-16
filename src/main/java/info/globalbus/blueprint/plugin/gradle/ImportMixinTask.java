package info.globalbus.blueprint.plugin.gradle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.plugins.osgi.OsgiManifest;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;

@Slf4j
public class ImportMixinTask extends DefaultTask {

    public static final String IMPORT_PACKAGE = "Import-Package";

    @TaskAction
    public void addToManifest() {
        PluginSettings extension = getProject().getExtensions().findByType(PluginSettings.class);
        if (extension == null) {
            throw new GradleException("There is no settings for blueprint!");
        }
        Jar jarTask = (Jar) getProject().getTasks().getByName("jar");
        Manifest manifest = jarTask.getManifest();
        if (manifest != null && manifest instanceof OsgiManifest) {
            OsgiManifest osgiManifest = ((OsgiManifest) manifest);
            List<String> actual = osgiManifest.getInstructions().get(IMPORT_PACKAGE);
            if (actual == null) {
                osgiManifest.instruction(IMPORT_PACKAGE, "*");
                actual = osgiManifest.getInstructions().get(IMPORT_PACKAGE);
            }
            if (actual.contains("*")) {
                actual.remove("*");
            }
            final List<String> packages = new LinkedList<>();
            Optional.ofNullable(extension.getCustomOptions()).map(v -> v.get("importMixin"))
                .filter(v -> v instanceof Collection).map(v -> (Collection<String>) v)
                .ifPresent(packages::addAll);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getImportsFile())))) {
                while (reader.ready()) {
                    packages.add(reader.readLine());
                }
            } catch (IOException ex) {
                log.error("Cannot read blueprint imports");
            }
            packages.add("*");
            Stream<String> filter = packages.stream();
            for (String inst : actual) {
                String prefix = StringUtils.split(inst, "*")[0];
                filter = filter.filter(p -> !p.startsWith(prefix));
            }
            actual.addAll(filter.collect(Collectors.toList()));
        }
    }

    @InputFile
    public File getImportsFile() {
        File tmp = new File(new File(getProject().getBuildDir(), "tmp"), BlueprintGenerate.BLUEPRINT_IMPORTS_TMP);
        if (!tmp.exists()) {
            try {
                OutputStream out = FileUtils.openOutputStream(tmp);
                IOUtils.closeQuietly(out);
            } catch (IOException ex) {
                throw new GradleException("Cannot touch file", ex);
            }
        }
        return tmp;
    }

}
