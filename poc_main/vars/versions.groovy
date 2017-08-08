#!/usr/bin/env groovy

def component
def version
def call(script, comp_file, Closure body) {
    // read components
    def components = readFile comp_file
    // get lines
    def lines = components.split('\n')
    // loop over lines
    for (int i = 0; i < lines.size(); i++) {
        def info = lines[i].split(':')
        component = info[0]
        version = info[1]
        dir ('versions') {
            // get version
            version = get_version(this, "${component}", "${version}")
        }
        // do you stuff here
        body()
    }
}

def getComponent() {
    component
}

def getVersion() {
    version
}

def get_version(script, name, version) {
    //echo "Hi, ${name} ${version}!"
    if ('latest' == version) {
        //echo "Latest"
        def repo = "ssh://git@servix/extra/git/${name}.git"
        script.sh "set +x; git ls-remote --tags --refs -q ${repo} | grep -v 'poc_comp' | cut -d / -f 3 | sort -b -t. -k 1,1n -k 2,2n -k 3,3n -k 4,4n | tail -1 2>&1 > ${WORKSPACE}/versions/${name}.txt"
        readFile "${WORKSPACE}/versions/${name}.txt"
    } else {
        writeFile file: "${WORKSPACE}/versions/${name}.txt", text: "${version}"
        version
    }
}
