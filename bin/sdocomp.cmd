@REM
@REM  Copyright 2008 BEA Systems Inc.
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM

@echo off

setlocal
if "%SDO_HOME%" EQU "" (set SDO_HOME=%~dp0..)
set XMLBEANS_LIB=%SDO_HOME%\build\lib

set CP=
set CP=%SDO_HOME%\build\ar\sdo.jar
set CP=%CP%;%XMLBEANS_LIB%\xbean.jar
set CP=%CP%;%XMLBEANS_LIB%\jsr173_1.0_api.jar

java -ea -classpath "%CP%" davos.sdo.impl.binding.SDOCompiler %*

:done
