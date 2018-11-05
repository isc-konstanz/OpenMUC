#!/bin/bash
#Description: Setup script to install OpenHomeMatic Driver
OWNER="isc-konstanz"
PROJECT="OpenHomeMatic"
SERVICE="driver"
BUNDLE="homematic-cc1101"
VERSION="1.0.0"

install() {
  # Verify, if the specific version does exists already
  if [ ! -f "$EMONMUC_DIR/bundles/openmuc-$SERVICE-$BUNDLE-$VERSION.jar" ]; then
    remove
    github_bundle "$OWNER" "$PROJECT" "openmuc-$SERVICE-$BUNDLE" "$VERSION"
    github "$tmp" "$OWNER" "$PROJECT" "$BUNDLE" "$VERSION" "zip"
    unzip -q "$tmp/$BUNDLE-$VERSION.zip" -d "$tmp"
    mv -f "$tmp/lib/device/$BUNDLE" "$EMONMUC_DIR/lib/device/$BUNDLE"
  fi
}

remove() {
  rm -f "$EMONMUC_DIR/bundles/openmuc-$SERVICE-$BUNDLE"*
  rm -rf "$EMONMUC_DIR/lib/device/$BUNDLE"*
}
