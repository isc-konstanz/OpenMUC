#!/bin/bash
#Description: Setup script to install EmonMUC bundles
EMONMUC_VERSION="0.17.1"

TMP_DIR="/var/tmp/emonmuc"

install_framework() {
  install_core github "openmuc"            "openmuc-core-api"                  $EMONMUC_VERSION
  install_core github "openmuc"            "openmuc-core-spi"                  $EMONMUC_VERSION
  install_core github "openmuc"            "openmuc-core-datamanager"          $EMONMUC_VERSION
  install_core github "openmuc"            "openmuc-server-restws"             $EMONMUC_VERSION

  install_core github "emonjava"           "openmuc-datalogger-emoncms"        "1.1.5"

  #--------------------------------------------------------------------------------------------------
  # RXTX is a native interface to serial ports in java.
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.openmuc"        "jrxtx"                             "1.0.1"

  #--------------------------------------------------------------------------------------------------
  # The Apache Felix Gogo standard shell for OSGi (http://felix.apache.org/site/apache-felix-gogo.html)
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.apache.felix"   "org.apache.felix.gogo.runtime"     "1.1.0"
  install_core maven  "org.apache.felix"   "org.apache.felix.gogo.command"     "1.0.2"
  install_core maven  "org.apache.felix"   "org.apache.felix.gogo.jline"       "1.1.0"
  install_core maven  "org.jline"          "jline"                             "3.8.0"

  #--------------------------------------------------------------------------------------------------
  # Adds a telnet server so that the Felix Gogo Shell can be accessed
  # using telnet clients. By default this server only listens on
  # localhost port 6666. Therefor you can on only access it from the
  # same host on which felix is running.
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.apache.felix"   "org.apache.felix.shell.remote"     "1.2.0"

  #--------------------------------------------------------------------------------------------------
  # Apache Felix Service Component Runtime that implements the OSGi Declarative Services Specification
  # the OpenMUC core bundles use declarative services and thus depend on them
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.apache.felix"   "org.apache.felix.scr"              "2.1.0"

  #--------------------------------------------------------------------------------------------------
  # An implementation of the OSGi HTTP Service Specification, needed by the WebUI bundles
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.apache.felix"   "org.apache.felix.http.servlet-api" "1.1.2"
  install_core maven  "org.apache.felix"   "org.apache.felix.http.api"         "3.0.0"
  install_core maven  "org.apache.felix"   "org.apache.felix.http.jetty"       "4.0.0"

  #--------------------------------------------------------------------------------------------------
  # Implementations of the OSGi Event Admin, Configuration Admin and MetaType services, needed by jetty
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.apache.felix"   "org.apache.felix.eventadmin"       "1.5.0"
  install_core maven  "org.apache.felix"   "org.apache.felix.configadmin"      "1.9.2"
  install_core maven  "org.apache.felix"   "org.apache.felix.metatype"         "1.2.0"

  #--------------------------------------------------------------------------------------------------
  # Adds a web console for felix bundle management
  # http://localhost:8080/system/console/httpservice
  # https://localhost:8443/system/console/httpservice
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.apache.felix"   "org.apache.felix.webconsole"       "4.3.4"
  install_core maven  "org.apache.felix"   "org.apache.felix.log"              "1.0.1"
  install_core maven  "commons-io"         "commons-io"                        "2.6"
  install_core maven  "commons-fileupload" "commons-fileupload"                "1.3.3"

  #--------------------------------------------------------------------------------------------------
  # Message logging libraries, SLF4J is a light-weight logging API,
  # Logback is a message logger implementation that implements SLF4J
  # natively
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.slf4j"          "slf4j-api"                         "1.7.25"
  install_core maven  "ch.qos.logback"     "logback-classic"                   "1.2.3"
  install_core maven  "ch.qos.logback"     "logback-core"                      "1.2.3"

  #--------------------------------------------------------------------------------------------------
  # The Apache Felix main executable
  #--------------------------------------------------------------------------------------------------
  install_core maven  "org.apache.felix"   "org.apache.felix.main"             "6.0.0"

  mv -f "$EMONMUC_DIR/bundles/org.apache.felix.main-"* "$EMONMUC_DIR/bin/felix.jar"

  read -a bundles < "$EMONMUC_DIR/conf/bundles.conf"
  for bundle in "${bundles[@]}"; do
    install_bundle "$bundle"
  done
}

install_core() {
  if [ ! -f  "$EMONMUC_DIR/bundles/$3-$4.jar" ]; then
    rm -f "$EMONMUC_DIR/bundles/$3"*

    case "$1" in
      github)
        github_bundle "isc-konstanz" ${@:2}
        ;;
      maven)
        maven  ${@:2}
        ;;
    esac
  fi
}

install_bundle() {
  if ! find_bundle "$1"; then
    echo "Unable to install unknown bundle: $1"
    exit 1
  fi
  tmp="$TMP_DIR/$bundle"
  mkdir -p "$tmp"

  source "$EMONMUC_DIR/lib/framework/bundles/$bundle.sh"
  install

  if [ -f "$EMONMUC_DIR/conf/bundles.conf" ]; then
    read -a bundles < "$EMONMUC_DIR/conf/bundles.conf"
    if [[ ! " ${bundles[@]} " =~ " $bundle " ]]; then
      bundles+=("$bundle")
      IFS=$'\n' installed=($(sort <<<"${bundles[*]}"))
      echo "${installed[@]}" > "$EMONMUC_DIR"/conf/bundles.conf
      unset IFS
    fi
  fi
  rm -rf "$tmp"
}

remove_bundle() {
  if ! find_bundle "$1"; then
    echo "Unable to remove unknown bundle: $1"
    exit 1
  fi

  source "$EMONMUC_DIR/lib/framework/bundles/$bundle.sh"
  remove

  if [ -f "$EMONMUC_DIR/conf/bundles.conf" ]; then
    read -a bundles < "$EMONMUC_DIR/conf/bundles.conf"
    if [[ " ${bundles[@]} " =~ " $bundle " ]]; then
      delete=($bundle)
      installed=${bundles[@]/$delete}
      echo "${installed[@]}" > "$EMONMUC_DIR"/conf/bundles.conf
    fi
  fi
}

find_bundle() {
  bundle="$1"
  if [ -f "$EMONMUC_DIR"/lib/framework/bundles/"$bundle".sh ]; then
    return 0
  fi
  files=("$EMONMUC_DIR"/lib/framework/bundles/*"$bundle"*.sh)
  if [ ${#files[@]} -gt 1 ]; then
    files=("$EMONMUC_DIR"/lib/framework/bundles/*"driver-$bundle"*.sh)
  fi

  if [ ${#files[@]} -eq 1 ]; then
    if [ -f ${files[0]} ]; then
      bundle=$(basename -- "${files[0]%.*}")
      return 0
    fi
  else
    echo "Please clarify bundle to install:"
    for file in "${files[@]}"; do
      echo "  $(basename -- "${file%.*}")"
    done
    exit 1
  fi
  return 1
}

github_bundle() {
  github "$EMONMUC_DIR/bundles" $@ "jar"
}

github() {
  wget --quiet \
       --show-progress \
       --directory-prefix="$1" \
       "https://github.com/$2/$3/releases/download/v$5/$4-$5.$6"
}

maven() {
  wget --quiet \
       --show-progress \
       --directory-prefix="$EMONMUC_DIR/bundles" \
       "http://central.maven.org/maven2/${1//./\/}/$2/$3/$2-$3.jar"
}
