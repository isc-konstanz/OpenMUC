#!/bin/bash
#Description: Setup script to install IEC 62056 part 21 Driver
OWNER="isc-konstanz"
PROJECT="OpenMUC"
SERVICE="driver"
BUNDLE="iec62056p21"

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
