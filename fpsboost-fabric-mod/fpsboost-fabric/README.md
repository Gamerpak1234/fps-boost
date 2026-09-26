# FPS Boost (Fabric mod for Minecraft 1.21.11)

A small Fabric mod with two real, honest features:

1. **High OS process priority** — on launch, the mod asks your operating
   system to schedule the Minecraft process with higher-than-normal
   priority, so it gets more CPU time relative to background apps.
2. **Performance Mode toggle (`F10` by default)** — instantly drops
   particles, clouds, graphics quality, entity shadows, and clamps your
   view/entity render distance, to cut GPU/CPU render load. Press it
   again to restore your previous settings.

### What this mod will *not* do
It won't rewrite Minecraft's renderer or fix a GPU bottleneck — that's
what dedicated optimization mods do. If you don't already have them,
install these from Modrinth alongside this mod for a much bigger FPS
gain:
- **Sodium** — rendering engine replacement
- **Lithium** — general/server-side optimizations
- **Starlight** (or FerriteCore for RAM) — lighting engine optimization

This mod plays nicely alongside all three.

### A note on the priority boost
- **Windows**: works without admin rights in almost all cases.
- **Linux/macOS**: raising priority (`renice` to a negative value)
  normally requires root or the `CAP_SYS_NICE` capability. Without it,
  the command will fail quietly and the game just runs at normal
  priority — check `latest.log` for a line starting with `[fpsboost]`
  to see whether it succeeded.

---

## Project layout
```
fpsboost-fabric/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/main/
    ├── java/com/fpsboost/
    │   ├── FpsBoostMod.java          (main entrypoint - sets priority)
    │   ├── FpsBoostClient.java       (client entrypoint - keybind)
    │   ├── PerformanceMode.java      (the F10 toggle logic)
    │   └── ProcessPriorityBooster.java
    └── resources/
        ├── fabric.mod.json
        └── assets/fpsboost/lang/en_us.json
```

## Before you build
This was written against the public Fabric versions for **Minecraft
1.21.11** current as of writing, but Fabric ships new loader/API/mapping
builds frequently. **Check `gradle.properties`** and update
`yarn_mappings`, `loader_version`, and `fabric_version` to whatever
`https://fabricmc.net/develop/` currently lists for 1.21.11 before you
build — using stale build numbers is the #1 cause of a failed first
build with any Fabric mod template.

I wasn't able to compile this in the sandbox this was written in (no
network access to Fabric's Maven repo), so treat this as a solid,
correctly-structured starting point rather than a pre-tested jar — if a
method name has shifted slightly in a recent Yarn mapping (this
happens), your IDE's autocomplete on `GameOptions` will show you the
current name in seconds.

## Build
Requires **JDK 21**.

```bash
git clone <wherever you put this> fpsboost-fabric
cd fpsboost-fabric
./gradlew build
```

If you don't have a Gradle wrapper yet, generate one first:
```bash
gradle wrapper --gradle-version 8.10
```

The built mod jar will be in `build/libs/fpsboost-1.0.0.jar`.

## Install
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft
   1.21.11.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) matching
   1.21.11 (required — this mod uses it for the keybind).
3. Drop `fpsboost-1.0.0.jar` into your `.minecraft/mods/` folder.
4. Launch the game with the Fabric profile. Press **F10** in-game to
   toggle Performance Mode; the priority boost applies automatically on
   launch.

## Customizing
- Change the keybind default in `FpsBoostClient.java`
  (`InputUtil.GLFW_KEY_F10`).
- Adjust which settings Performance Mode changes, and their target
  values, in `PerformanceMode.java`.
- Adjust the OS "nice" value or Windows priority class in
  `ProcessPriorityBooster.java` (avoid `Realtime` on Windows — it can
  starve system processes and cause instability).
