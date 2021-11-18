
## OpenRC-Turbo

OpenRC is a modified version of the official [FTC SDK](https://github.com/FIRST-Tech-Challenge/SkyStone)
in which all of the source code that is normally tucked away inside the AAR files has been extracted into modules. This makes it easy to see and modify almost the entirety of the Robot Controller app's source code. In addition, the history in Git shows all changes that have been made to the core code since OpenRC's inception. This complements the changelogs that FIRST provides, allowing teams to see exactly what code has been changed.


## Legality for competition use

According to the [2021-2022 Game Manual Part 1](https://www.firstinspires.org/sites/default/files/uploads/resource_library/ftc/game-manual-part-1-traditional-events.pdf), teams are not allowed to replace or modify the portions of the SDK which are distributed as AAR files, per `<RS08>`. This means that in its default configuration, OpenRC is **not** legal for competition.

**HOWEVER**, in order to address this, OpenRC has a `stock` build variant which will compile the `TeamCode` and `FtcRobotController` modules against the official, unmodified AAR files, rather than against the extracted modules.

## Device Compatibility

Compatible with all legal FTC Robot Controller devices for the 2021-2022 season, including the Control Hub.

## Build variants

### Variant Descriptions

Normal SDK 7.0 APK size: 63MB

 - **Stock - 52.1MB APK** *(1.2x smaller)*
     - Competition legal
     - 64-bit libs removed

 - **Turbo - 11.6MB APK** *(5.4x smaller)*

     *Note: If you would like to use Blocks, you will need to copy your private Vuforia key into the `Blocks/src/main/assets/CzechWolf` file*
     - Vuforia native library loaded dynamically
     - Vuforia/TF datasets loaded dynamically
     - OnBotJava removed

 - **Extreme Turbo - 7.1MB APK** *(8.9x smaller)*
     - Vuforia native library loaded dynamically
     - Vuforia/TF datasets loaded dynamically
     - OnBotJava removed
     - Blocks removed
     - Sound files removed