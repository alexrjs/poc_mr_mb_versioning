#!/usr/bin/env bash

increment_version ()
{
  num=${1//./}
  let num++

  re=${1//./)(}
  re=${re//[0-9]/.}')'
  re=${re#*)}

  count=${1//[0-9]/}
  count=$(wc -c<<<$count)
  out=''
  for ((i=count-1;i>0;i--)) ; do
      out='.\'$i$out
  done

  sed -r s/$re$/$out/ <<<$num
} 

# Check variables
BRANCH_USER=${BRANCH_USER:?Error\: BRANCH_USER not set\!}
BRANCH_USER=${BRANCH_EMAIL:?Error\: BRANCH_EMAIL not set\!}
BRANCH_REPO=${BRANCH_REPO:?Error\: BRANCH_REPO not set\!}

git config user.name "${BRANCH_USER}"
git config user.email ${BRANCH_EMAIL}

if [ -z $1 ]; then
  # sometimes a standard appears, but it is not needed
  git tag | grep standard
  if [ $? == 0 ]; then
    git tag -d standard
  fi
  # get orig version
  version_orig=$(git ls-remote --tags --refs -q ${BRANCH_REPO} | grep -v 'poc_comp' | cut -d / -f 3 | sort -b -t. -k 1,1n -k 2,2n -k 3,3n -k 4,4n | tail -1)
  # if no orig version start with 1.0.0.0
  if [ -z $version_orig ]; then
      version_orig=1.0.0.0
  fi
  # get the first three and the last digit strings
  version_rest=$(echo $version_orig | cut -d '.' -f 1-3)
  version_last=$(echo $version_orig | cut -d '.' -f 4)
  # get the new version
  version_new="$version_rest.$(increment_version $version_last)"
  # write to file for later use
  [[ ! -d versions ]] && mkdir -p versions || true
  echo $version_new > versions/version.txt
  # tag and push
  git tag -f -a $version_new -m "Success $version_new"
  if [ $? -eq 0 ]; then
    git push -f ${BRANCH_REPO} --tags
  fi
else
  if [ $1 == 'bump' ]; then
    if [ ! -z $2 -a ! -z $3 ]; then
      [[ ! -d versions ]] && mkdir -p versions || true
      echo $2 > versions/version.txt
      # tag and push
      git tag -f -a $2 -m "Bumped version to $2"
      if [ $? -eq 0 ]; then
        git push -f $3 --tags
      fi
    fi
  fi
fi
