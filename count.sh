set -e
for dir in akka-*; do
  echo $dir || 0
  loc $dir || 0
done
