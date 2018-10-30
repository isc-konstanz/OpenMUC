#!/bin/bash
#Description: Setup script to install the EmonMUC framework

# Set the targeted location of the emonmuc framework and the emoncms webserver.
# If a specified directory is empty, the component will be installed.
ROOT_DIR="/opt/emonmuc"
WEB_DIR="/var/www/html"
WEB_USER="www-data"
GIT_SERVER="https://github.com/emoncms"
GIT_BRANCH="stable"
USER="pi"

if [[ $EUID -ne 0 ]]; then
    echo "Please make sure to run the emonmuc setup as root user"
    exit 1
fi

API_KEY=""
while [[ $# -gt 0 ]]; do
case "$1" in
    -d|--dir)
    WEB_DIR="$2"
    shift
    shift
    ;;
    -a|--apikey)
    API_KEY="$2"
    shift
    shift
    ;;
    *)
    echo "Usage: emonmuc setup [-d|--dir][-a|--apikey]"
    exit 1
    ;;
esac
done

if type -p java >/dev/null 2>&1; then
    JAVA_CMD=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    apt-get install openjdk-8-jre-headless -y
fi

if [[ "$JAVA_CMD" ]]; then
    JAVA_VERS=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$JAVA_VERS" < "1.8" ]]; then
        echo "Installed java version is below 1.8 and not compatible with emonmuc"
        exit 1
    fi
fi

if [ ! -d "$ROOT_DIR" ]; then
    echo "Installing emonmuc framework"
    apt-get install -y git-core netcat

    git clone -b $GIT_BRANCH "https://github.com/isc-konstanz/emonmuc.git" $ROOT_DIR
    chown $USER:root -R $ROOT_DIR
fi
#echo -e "\e[96m\e[1m$(cat $ROOT_DIR/lib/framework/welcome.txt)\e[0m"

if [ ! -d "$WEB_DIR/emoncms" ]; then
    echo "Installing emoncms webserver"
    apt-get install -y apache2 php7.0 libapache2-mod-php7.0 php7.0-mysql php7.0-gd php7.0-opcache php7.0-curl php7.0-dev php7.0-mcrypt php7.0-common php-pear php-redis
    a2enmod rewrite
    pear channel-discover pear.swiftmailer.org
    pecl channel-update pecl.php.net
    pecl install swift/swift

    mkdir -p $WEB_DIR /var/log/emoncms /var/lib/emoncms/{phpfiwa,phpfina,phptimeseries}
    touch /var/log/emoncms/emoncms.log
    chmod 666 /var/log/emoncms/emoncms.log
    chown $WEB_USER:root /var/log/emoncms/emoncms.log
    chown $WEB_USER:root -R $WEB_DIR /var/lib/emoncms

    sudo -u $WEB_USER git clone -b $GIT_BRANCH $GIT_SERVER/emoncms.git $WEB_DIR/emoncms
    sudo -u $WEB_USER git clone -b master $GIT_SERVER/device.git $WEB_DIR/emoncms/Modules/device
    sudo -u $WEB_USER git clone -b $GIT_BRANCH $GIT_SERVER/graph.git $WEB_DIR/emoncms/Modules/graph
    #sudo -u $WEB_USER git clone -b $GIT_BRANCH $GIT_SERVER/app.git $WEB_DIR/emoncms/Modules/app
    if [ "$WEB_DIR" != "/var/www/html" ]; then
        sudo -u $WEB_USER ln -sf $WEB_DIR/emoncms /var/www/html/emoncms
    fi
    cp $ROOT_DIR/conf/emoncms.apache2.conf /etc/apache2/sites-available/emoncms.conf
    a2ensite emoncms
    systemctl reload apache2

    sudo DEBIAN_FRONTEND=noninteractive apt-get install -y mariadb-server mariadb-client redis-server
    if ! [ -x "$(command -v pwgen)" ]; then
        apt-get install -y pwgen
    fi
    SQL_ROOT=$(pwgen -s1 32)
    #SQL_ROOT=$(echo "$SQL_ROOT" | tr \\\´\`\'\"\$\@\( $(pwgen -1 1))

    SQL_USER=$(pwgen -s1 32)
    #SQL_USER=$(echo "$SQL_USER" | tr \\\´\`\'\"\$\@\( $(pwgen -1 1))

    mysql -uroot --execute="SET PASSWORD FOR 'root'@'localhost' = PASSWORD('$SQL_ROOT');
CREATE DATABASE emoncms DEFAULT CHARACTER SET utf8;
CREATE USER 'emoncms'@'localhost' IDENTIFIED BY '$SQL_USER';
GRANT ALL ON emoncms.* TO 'emoncms'@'localhost';
FLUSH PRIVILEGES;"

    sudo -u $WEB_USER cp $ROOT_DIR/conf/emoncms.default.php $WEB_DIR/emoncms/settings.php
    sed -i "7s/<password>/$SQL_USER/" $WEB_DIR/emoncms/settings.php
    php -f $ROOT_DIR/lib/upgrade.php

    echo "[MySQL]" > ./setup_pwd.txt
    echo "root:$SQL_ROOT" >> ./setup_pwd.txt
    echo "emoncms:$SQL_USER" >> ./setup_pwd.txt
fi
$ROOT_DIR/bin/emonmuc install -d "$WEB_DIR/emoncms" -a "$API_KEY"

echo "Successfully installed the emonmuc framework"

exit 0

