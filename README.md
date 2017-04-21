blueprint-gradle-plugin
=========
Gradle port of apache blueprint-maven-plugin
http://aries.apache.org/modules/blueprint-maven-plugin.html

Options are mostly the same as in original


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
        classpath "gradle.plugin.info.globalbus:blueprint-gradle-plugin:0.0.1"
      }
    }
    apply plugin: "info.globalbus.blueprint-gradle"
    
### Plugin options

| Option | Default value | Description |
| ------ | ------------- | ----------- |
| scanPaths | null | Paths to search annotations. If not provided, will be resolved from source directory |
| namespaces | null | Which extension namespaces should the plugin support |
| generatedFileName | "autowire.xml" | Name of file to write |
| defaultActivation | null | Lazy or Eager type of blueprint activation (eager is blueprint default, if not specified) |
| customParameters | empty Map<String,String> | Specifies additional parameters which could be used in extensions |
| generatedDir | "OSGI-INF/blueprint/" | Base directory to write into (relative to ${buildDir}/generatedsources/) |
| camelOpts | null | Additional options to configure camel context (incubating) |

### CamelContext Support (incubating)
Currently, if camelOpts was set, the CamelContext definition will be added to blueprint file. 
If any class extending RouteBuilder can be found in scanPaths, the <package> element will be added to the CamelContext.
You can provide id in camelOpts, to add id property on CamelContext

	blueprintGenerate {
		settings {
			camelOpts = [id : "myCamelContext"]
		}
	}
