#!/usr/bin/env groovy

// bumpversion is put here instead of success, to capture errors in script as failure
def call(String version = '') {
    def bumpversion = libraryResource('bumpversion.sh')
    if (version != '') {
        writeFile file: ".bumpversions.sh", text: "${bumpversion}"
        sh("bash .bumpversions.sh bump ${version} ${BRANCH_REPO}")
        sh("rm -f .bumpversions.sh")
    } else {
        sh("${bumpversion}")
    }
}
