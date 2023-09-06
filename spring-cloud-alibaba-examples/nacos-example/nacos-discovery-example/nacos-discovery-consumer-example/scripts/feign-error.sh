#!/usr/bin/env bash
n=1
while [ $n -le 10 ]
do
    echo `curl -s http://localhost:18083/divide-feign?a=1\&b=0`
    let n++
done
