# KeyboardSnake
Snake on any Logitech RGB Keyboard

## Building 
To build the project you need to add the following libraries:
 * [com.1stleg:jnativehook:2.1.01](https://github.com/kwhat/jnativehook)
 * [Logitech Illumination SDK 9.00](https://www.logitechg.com/de-de/innovation/developer-lab.html)
 
Make sure you have added the right jars, there are ones for 64 Bit system and for 32 Bit systems, depending on what jre you use.

## Installing and running

Download the latest [release](https://github.com/joblo2213/KeyboardSnake/releases). Make sure you have the right version for your jre.
Also make sure you have the Logitech GHub Software installed and haven't installed any third party wrapper patches that could break the integration.

To run the programm open the console and use the following command, or use the predefined `.bat`-Files.
```
java -jar KeyboardSnake-x64.jar 
```

There are also some parameters to change the game settings:
 * `--speed=1000` / `-s=1000` change the game speed (higher numbers are slower)
 * `--border` / `-b` activate the border so that you can't pass through walls to apear on the other side
 * `--monochrome` / `-m` no colors, only black/white
