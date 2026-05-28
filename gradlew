#!/usr/bin/env sh

DIR="$(cd "$(dirname "$0")"; pwd)"

java -version > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "Java not found"
    exit 1
fi

exec "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
