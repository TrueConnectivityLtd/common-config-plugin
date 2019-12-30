#!/bin/bash 

git config --local credential.username $GITHUB_USR
git config --local credential.helper 'store --file=${eCREDENTIALS}'
git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
git config branch.master.remote origin
git config branch.master.merge refs/heads/master