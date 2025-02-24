#!/usr/bin/env bash
set -e
if [[ -f ~/.sdkman/bin/sdkman-init.sh ]]; then
  # shellcheck source=/dev/null
  source ~/.sdkman/bin/sdkman-init.sh
  if [[ -f ~/bin/jdk17 ]]; then
    # shellcheck source=/dev/null
    source ~/bin/jdk17
  fi
fi

mvn -Prelease clean site deploy

