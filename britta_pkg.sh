#!/bin/sh

rm -rf britta_tuner
mkdir britta_tuner
mkdir britta_tuner/lib

cp target/Tuner-assembly-0.9.jar britta_tuner/
cp -R lib/opengl/* britta_tuner/lib/

cp run_tuner.* britta_tuner/

cp -R test_data/britta_proj britta_tuner/

zip -r britta_tuner.zip britta_tuner && rm -rf britta_tuner

