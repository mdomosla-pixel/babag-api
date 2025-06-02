#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <filename>"
    exit 1
fi

if [ -z $BINTRAY_USER -o -z $BINTRAY_KEY ]; then
    echo '$BINTRAY_USER and $BINTRAY_KEY must be set'
    exit 2
fi

FILE_NAME="$1"
PACKAGE=$(echo $FILE_NAME | awk -F _ '{print $1}')
VERSION="$(echo $FILE_NAME | cut -d '_' -f 2)"

ORG="statoilfuelretail"
REPO="deb"
FIRST_LETTER=${PACKAGE:0:1}

DISTROS="jessie,wily,vivid,xenial"
ARCH="amd64"
COMPONENT="3rd-party"

echo
echo "======= Uploading package ${PACKAGE}======="
curl -s -T $FILE_NAME -u"${BINTRAY_USER}:${BINTRAY_KEY}" \
    "https://api.bintray.com/content/${ORG}/${REPO}/${PACKAGE}/${VERSION}/pool/main/${FIRST_LETTER}/${PACKAGE_NAME}/${FILE_NAME};deb_distribution=${DISTROS};deb_component=${COMPONENT};deb_architecture=${ARCH};override=1;publish=1"

exit $?


