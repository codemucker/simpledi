#!/usr/bin/env nix-shell

{ pkgs ? import <nixpkgs> {
        config.allowUnfree = true;
    } }:
(
    pkgs.buildFHSUserEnv {
        name = "codemucker-kotlin-dev";
        targetPkgs = pkgs: (with pkgs; [
            libtool
            nodejs_20
            zlib
            jdk21
            vscode
            android-studio
            # see https://github.com/facebook/react-native/issues/3091
            android-tools
            # aider-chat
            # disabled for now, until we run it direct on hardware
            # android-udev-rules
            # for x64linux/posix kotlin kmp (inside kotlin dist)
            # Else get libcrypt.so.1: cannot open shared object file: No such file or directory on nixos
            # remember to reload gradle daemon
            libxcrypt
            libxcrypt-legacy
    ]);
    runScript = "bash";
}).env
