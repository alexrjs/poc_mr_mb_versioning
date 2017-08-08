#!/usr/bin/env groovy

// bumpversion is put here instead of success, to capture errors in script as failure
def call(int maxNumber = 10, int successMax = 7) {
    def random = libraryResource('random.sh')
    writeFile file: ".random.sh", text: "${random}"
    sh("bash .random.sh ${maxNumber} ${successMax}")
    sh("rm -f .random.sh")
}
