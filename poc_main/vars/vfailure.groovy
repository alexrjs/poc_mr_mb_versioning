#!/usr/bin/env groovy

def call(String text = 'Current', Closure body) {
    currentBuild.displayName = "FAILURE"
    currentBuild.description = "Failure: ${text}"
    body()
}
