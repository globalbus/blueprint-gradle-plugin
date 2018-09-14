blueprint-gradle-plugin
=========
Gradle port of apache blueprint-maven-plugin
http://aries.apache.org/modules/blueprint-maven-plugin.html

Options are mostly the same as in original

### Improvements
- Faster plugin execution, using [ClassGraph](https://github.com/classgraph/classgraph)
- Support for Camel
- Automatically add all required imports to MANIFEST, thanks to [Gradle OSGi plugin](https://docs.gradle.org/current/userguide/osgi_plugin.html)
- Can scan packages outside of the bundle (from runtime dependencies)


### Tasks

| Name | Description | Dependency |
| ---- | ----------- | --------- |
| blueprintGenerate | Generate blueprint files from annotations | classes depends on blueprintGenerate |

## Usage

To use this plugin, you must
- modify your buildscript to have dependencies to the plugin
- apply the plugin

### Applying the plugin

    buildscript {
      repositories {
        maven {
          url "https://plugins.gradle.org/m2/"
        }
      }
      dependencies {
        classpath "gradle.plugin.info.globalbus:blueprint-gradle-plugin:0.1.0"
      }
    }
    apply plugin: "info.globalbus.blueprint-gradle"
    
### Plugin options

| Option | Default value | Description |
| ------ | ------------- | ----------- |
| scanPaths | null | Additional paths to search annotations. Main scan path will be resolved from compiled sources of the bundle |
| namespaces | null | Which extension namespaces should the plugin support |
| generatedFileName | "autowire.xml" | Name of file to write |
| defaultActivation | null | Lazy or Eager type of blueprint activation (eager is blueprint default, if not specified) |
| generatedDir | "OSGI-INF/blueprint/" | Base directory to write into (relative to ${buildDir}/generatedsources/) |
| customParameters | empty Map<String,String> | Specifies additional parameters which could be used in legacy extensions (deprecated) |
| customOptions | null | Additional options to configure extensions. Mostly used by Camel Support (incubating) |

### Camel Support (incubating)
Currently, if customOptions.contextId was set, the CamelContext definition will be added to blueprint file. 
If any class extending RouteBuilder can be found in scanPaths, the <package> element will be added to the CamelContext.
You can provide additional property resolvers to be used by [Camel PropertyPlaceholder](http://camel.apache.org/using-propertyplaceholder.html)
BlueprintCamelContext will be available to inject in any place (via contextEnricher).

	blueprintGenerate {
		settings {
			customOptions = [
			    contextId : 'context-' + project.name
			    properties: [id: 'service-properties', pid: project.name.replace('-', '.'), defaults: [version: project.version]]
			]
		}
	}
