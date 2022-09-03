#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#
#   Runs the pipeline in the piper file specified by -p (piperfile)
#   with any other provided parameters.  Standard parameters are:
#     -i , --inputDir {inputDirectory}
#     -o , --outputDir {outputDirectory}
#     -s , --subDir {subDirectory}  (for i/o)
#     --xmiOut {xmiOutputDirectory} (if different from -o)
#     -l , --lookupXml {dictionaryConfigFile} (fast only)
#     --key {umlsPasskey}
#     -? , --help
#
#   Other parameters may be declared in the piper file using the cli command:
#     cli {parameterName}={singleCharacter}
#   For instance, for declaration of ParagraphAnnotator path to regex file optional parameter PARAGRAPH_TYPES_PATH,
#   in the custom piper file add the line:
#     cli PARAGRAPH_TYPES_PATH=t
#   and when executing this script use:
#      runPiperFile -p path/to/my/custom.piper -t path/to/my/custom.bsv
#
# Requires JAVA JDK 1.8+
#

USER_DIR=`pwd`

# Only set CTAKES_HOME if not already set
if [ -z "$CTAKES_HOME" ]; then
   echo Value of cTAKES Home CTAKES_HOME is not set.  Attempting to resolve ...
#   PRG="$0"
   SCRIPT_DIR=`dirname "$0"`
   echo $SCRIPT_DIR
   cd $SCRIPT_DIR
   CTAKES_HOME=`pwd`
fi

echo Changing directory into cTAKES Home Directory $CTAKES_HOME
cd $CTAKES_HOME
echo

CTAKES_CLASS_PATH=.
# Add compiled classes of each module to the ctakes class path.
# These classes should be created using mvn compile
for MODULE in `ls -d */ | grep -v .*\-res | grep -v target | grep -v resources`; do
   CTAKES_CLASS_PATH=$CTAKES_CLASS_PATH:${MODULE}target/classes/
done
# Add resources of each module to the ctakes class path.
# These resources are original "source" resources, not what might be in a root /resources/ directory.
for MODULE_RESOURCE in `ls -d */ | grep .*\-res`; do
   CTAKES_CLASS_PATH=$CTAKES_CLASS_PATH:${MODULE_RESOURCE}src/main/resources/
done

echo Using cTAKES Class Path $CTAKES_CLASS_PATH
echo

# The standard cTAKES class to run Piper files.
PIPER_RUNNER=org.apache.ctakes.core.pipeline.PiperFileRunner
# The standard cTAKES log4j configuration file.
LOG_CONF=file:$CTAKES_HOME/config/log4j.xml

# Run a user-specified Piper file with user-specified parameters.
java -cp $CTAKES_CLASS_PATH -Dlog4j.configuration=$LOG_CONF -Xms512M -Xmx3g $PIPER_RUNNER "$@"

cd $USER_DIR
