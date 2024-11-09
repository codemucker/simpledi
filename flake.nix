{
  inputs = {
    flake-utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = inputs:
    inputs.flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = (import (inputs.nixpkgs) {
            inherit system;
            config.allowUnfree = true;
        });
      in {
        devShell = pkgs.mkShell {
          buildInputs = with pkgs; [
            # nodePackages.pnpm
            # nodePackages.typescript
            # nodePackages.typescript-language-server

               libtool
               nodejs_20
               zlib
              jdk21
               vscode
               android-studio
              # see https://github.com/facebook/react-native/issues/3091
               android-tools
              # disabled for now, until we run it direct on hardware
              # android-udev-rules
              # for x64linux/posix kotlin kmp (inside kotlin dist)
              # Else get libcrypt.so.1: cannot open shared object file: No such file or directory on nixos
              # remember to reload gradle daemon
               libxcrypt
               libxcrypt-legacy

               aider-chat
               git

          ];
        };
      }
    );
}
