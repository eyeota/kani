#!/bin/bash

files1=()
files2=()

for f in $1/*.csv; do
  files1+=($f);
done

for f in $2/*.csv; do
  files2+=($f);
done

# Sort file order
files1=($(sort <<<"${files1[*]}"))
files2=($(sort <<<"${files2[*]}"))

if [ ${#files1[@]} != ${#files2[@]} ]; then
  echo "Number of files differs"
  exit
fi

echo "Comparing CSV files..."
for(( i=0; i<${#files1[@]}; i++)); do
  printf "  Comparing ${files1[i]} and ${files2[i]}... "
  result=$(diff <(sort "${files1[i]}") <(sort "${files2[i]}"))
  if [ "$result" != '' ]; then
    printf "X - file differs"
  fi
  printf "\n"
done
echo "Done"
