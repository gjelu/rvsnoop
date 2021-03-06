#!/bin/bash
# Start script for RvSnoop @version@ on Unix platforms.
# $Id$

export RVSNOOP_HOME=`dirname $0`/..

if [ -z "$TIBCO_HOME" ] ; then
    echo Warning: TIBCO_HOME not set, using /opt/tibco by default.
    export TIBCO_HOME=/opt/tibco
fi

if [ -z "$TIBRV_HOME" ] ; then
    echo Warning: TIBRV_HOME not set, using $TIBCO_HOME/tibrv by default.
    export TIBRV_HOME=$TIBCO_HOME/tibrv
fi

CP="$TIBRV_HOME/lib/tibrvj.jar"
LP="$TIBRV_HOME/bin:$TIBRV_HOME/lib"

for jar in $RVSNOOP_HOME/lib/*.jar ; do
    CP="$jar:$CP"
done

if [ -n "$TIBCO_TRA_HOME" ] ; then
    CP="$CP:$TIBCO_TRA_HOME/lib/TIBCOrt.jar"
fi

if [ -n "$TIBSDK_HOME" ] ; then
    CP="$CP:$TIBSDK_HOME/lib/Maverick4.jar"
fi

if [ -n "$RVSCRIPT_HOME" ] ; then
    CP="$CP:$RVSCRIPT_HOME/rvscript.jar"
fi

if [ -n "$RVTEST_HOME" ] ; then
    for jar in $RVTEST_HOME/lib/*.jar ; do
        CP="$CP:$jar"
    done
fi

if [ -n "$CLASSPATH" ] ; then
  CP="$CP:$CLASSPATH"
fi

export LIBPATH=$LP           # AIX
export SHLIB_PATH=$LP        # HP-UX
export DYLD_LIBRARY_PATH=$LP # Darwin / Mac OS X
export LD_LIBRARY_PATH=$LP   # Solaris & Linux

java -Xmx128m \
  -Drvsnoop.home="$RVSNOOP_HOME" \
  -Dfile.encoding=UTF-8 \
  -Djava.library.path="$LP" \
  -Dapple.laf.useScreenMenuBar=true \
  -Dcom.apple.mrj.application.apple.menu.about.name="RvSnoop" \
  -Djava.util.logging.config.file="${RVSNOOP_HOME}/lib/commons-logging.properties" \
  -classpath "$CP" org.rvsnoop.ui.RvSnoopApplication $* &
