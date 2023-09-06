#!/usr/bin/env bash
n=1
while [ $n -le 10 ]
do
    echo `curl -s http://localhost:18083/index`
    let n++
done
