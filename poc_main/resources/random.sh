#!/usr/bin/env bash

export ret=$(expr $RANDOM % $1) 
if [ $ret -gt $2 ]; then
  echo $ret means failed
  exit 1
else
  echo $ret means success
  exit 0
fi
