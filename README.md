# KotlinPluginCore

My personal base for making Paper plugins in Kotlin. I got tired of copy-pasting the same setup every time I started a new project, so I made this to fork from instead.

I've been writing Java plugins for a while but recently started moving to Kotlin. This is where I'm landing on project structure so far, it'll probably keep evolving.

## What's in here

- **Module system** - each feature is its own module with lifecycle hooks. Add one, register it, done.
- **Service locator** - simple map-based thing so modules can share stuff without passing everything through constructors.
- **Brigadier commands** - wired into Paper's lifecycle events, no more declaring commands in YAML.
- **Config wrapper** - modules get their own config sections, can also load standalone files.
- **Logger** - uses Adventure components, has a debug toggle.

## Building

```bash
./gradlew build
```

JAR goes to `build/libs/`. Drop it in your server's `plugins/` folder.

## Adding a module

Make a class, implement `PluginModule`, add the annotation, register it in `CorePlugin.kt`:

```kotlin
@ModuleInfo(id = "my-feature")
class MyFeatureModule : PluginModule {
    override fun onEnable() {
        // your stuff here
    }
    
    override fun onDisable() { }
}
```

There's an `ExampleModule` in the source that shows how commands, events, and config work together. Delete it when you start building for real.

## Notes

- Uses `paper-plugin.yml` instead of the old `plugin.yml`, mostly because it lets me register commands in code without touching YAML.
- Requires Java 21+ and Paper 1.21.4+.
- TODO: might add a way to auto-discover modules instead of registering them manually in CorePlugin
