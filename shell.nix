#!/usr/bin/env nix-shell

{ pkgs ? import <nixpkgs> {
        config.allowUnfree = true;
    } }:
(
    pkgs.buildFHSUserEnv {
        name = "myda-dev";
        targetPkgs = pkgs: (with pkgs; [
            libtool
            nodejs-18_x
            zlib
            jdk17
            vscode
            android-studio
    ]);
    runScript = "bash";
}).env
