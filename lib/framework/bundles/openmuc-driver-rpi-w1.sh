#!/bin/bash
#Description: Setup script to install Raspberry Pi 1-Wire Driver
OWNER="isc-konstanz"
PROJECT="OpenMUC"
SERVICE="driver"
BUNDLE="rpi-w1"

install() {
  # Verify, if the specific version does exists already
  if [ ! -f "$EMONMUC_DIR/bundles/openmuc-$SERVICE-$BUNDLE-$EMONMUC_VERSION.jar" ]; then
    remove
    github_bundle "$OWNER" "$PROJECT" "openmuc-$SERVICE-$BUNDLE" "$EMONMUC_VERSION"
  fi
}

remove() {
  rm -f "$EMONMUC_DIR/bundles/openmuc-$SERVICE-$BUNDLE"*
}
