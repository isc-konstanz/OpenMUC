#!/bin/bash
#Description: Setup script to install EmonCMS Datalogger
OWNER="isc-konstanz"
PROJECT="emonjava"
SERVICE="datalogger"
BUNDLE="emoncms"
VERSION="1.1.5"

install() {
  # Verify, if the specific version does exists already
  if [ ! -f "$EMONMUC_DIR/bundles/openmuc-$SERVICE-$BUNDLE-$VERSION.jar" ]; then
    remove
    github_bundle "$OWNER" "$PROJECT" "openmuc-$SERVICE-$BUNDLE" "$VERSION"
  fi
}

remove() {
  rm -f "$EMONMUC_DIR/bundles/openmuc-$SERVICE-$BUNDLE"*
}
