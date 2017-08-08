#!/usr/bin/env groovy

def call(String text = '', Closure body, Boolean skip = true) {
    def display_name
    def display_info
    if (text != '') {
        display_name = "SUCCESS ${text}"
        display_info = "Success ${text} - #${env.BUILD_ID}"
    } else {
        display_name = "SUCCESS"
        display_info = "Success #${env.BUILD_ID}"
    }
    if (!skip) {
        dir ("${WORKSPACE}/versions") {
            def fe = fileExists "version.txt"
            if (fe) {
                def version = readFile "version.txt"
                sh("echo Tag: ${version}")
                display_name = "SUCCESS v${version.trim()}"
                if (text != '') {
                    display_info = "Success ${text} - v${version.trim()} - #${env.BUILD_ID}"
                } else {
                    display_info = "Success v${version.trim()} - #${env.BUILD_ID}"
                }
            }
        }
    }
    currentBuild.displayName = display_name
    currentBuild.description = display_info
    body()
}
