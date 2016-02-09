#!/bin/bash

echo git add -A
git add -A

commit_msg="`date` syncing"
if [ $# -ne 0 ]; then
    commit_msg="`date` $1"
fi

echo git commit -m \"$commit_msg\"
git commit -m \"$commit_msg\"

echo git push -u origin master
git push -u origin master
