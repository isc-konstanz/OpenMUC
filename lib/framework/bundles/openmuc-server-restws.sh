#!/bin/bash
#Description: Setup script to install Rest Server
OWNER="isc-konstanz"
PROJECT="OpenMUC"
SERVICE="server"
BUNDLE="restws"

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
