save and restore
================

Situation 1 (Success):

git add .

git commit -m "checkpoint"

(do change - it's ok)

git add .

git commit -m "worked!"

git push


Situation 2 (Failure):

git add .

git commit -m "checkpoint"

(do change - it's not ok)

git checkout -- .
