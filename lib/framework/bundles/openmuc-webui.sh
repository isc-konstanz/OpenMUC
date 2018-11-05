#!/bin/bash
#Description: Setup script to install the WebUI
OWNER="isc-konstanz"
PROJECT="OpenMUC"

install() {
  # Verify, if the specific version does exists already
  files=("$EMONMUC_DIR"/bundles/openmuc-webui-*-"$EMONMUC_VERSION.jar" ];
  if [ ! ${#files[@]} -gt 1 ]; then
    remove
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-spi" "$EMONMUC_VERSION"
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-base" "$EMONMUC_VERSION"
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-channelconfigurator" "$EMONMUC_VERSION"
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-channelaccesstool" "$EMONMUC_VERSION"
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-userconfigurator" "$EMONMUC_VERSION"
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-dataexporter" "$EMONMUC_VERSION"
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-dataplotter" "$EMONMUC_VERSION"
    github_bundle "$OWNER" "$PROJECT" "openmuc-webui-mediaviewer" "$EMONMUC_VERSION"
  fi
}

remove() {
  rm -f "$EMONMUC_DIR/bundles/openmuc-webui"*
}
